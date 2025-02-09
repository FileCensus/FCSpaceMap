/*
 *
 * FCSpaceMap
 *
 * Copyright (C) 1997-2025  Intermine Pty Ltd. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package au.com.intermine.spacemap.treemap;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import au.com.intermine.spacemap.SpaceMapHelper;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.colorprovider.ColorProvider;
import au.com.intermine.spacemap.treemap.colorprovider.DepthColorProvider;
import au.com.intermine.spacemap.util.NumberRenderer;
import au.com.intermine.spacemap.util.NumberType;

/**
 * 
 * @author db0
 * 
 */
public class FileSizeRenderStrategy implements VisualisationRenderStrategy {

	private DepthColorProvider _colorprovider = new DepthColorProvider();

	private Font _font;

	private boolean _displayrootancestry = false;

	private Map<Integer, Paint> _unresolved_paint_cache;

	private Map<Integer, Paint> _freespace_paint_cache;

	private NumberRenderer _numberRenderer;

	private static Color _gradientStartColor = new Color(255, 255, 255, 0);

	/**
	 * Ctor
	 * 
	 */
	public FileSizeRenderStrategy(int byteDivisor, NumberType numberType) {
		JLabel lbl = new JLabel();
		_font = lbl.getFont();
		_unresolved_paint_cache = new HashMap<Integer, Paint>();
		_freespace_paint_cache = new HashMap<Integer, Paint>();
		_numberRenderer = new NumberRenderer(numberType, byteDivisor);
	}

	/**
	 * ctor
	 * 
	 * @param displayrootancestry
	 */
	public FileSizeRenderStrategy(boolean displayrootancestry, int byteDivisor, NumberType numberType) {
		this(byteDivisor, numberType);
		_displayrootancestry = displayrootancestry;
	}

	public Color getLeafNodeTextColor(TreeNode node) {
		return Color.black;
	}

	public Paint getNodePaint(TreeNode node) {
		Color color = _colorprovider.getColor(node);
		int rgb = color.getRGB();
		if (node.isLeaf()) {

			switch (node.getNodeType()) {
			case File:
			case Folder:
				TreeMapRectangle n = (TreeMapRectangle) node.getRectangle();
				return new GradientPaint(n.getX(), n.getY(), _gradientStartColor, n.getX() + n.getWidth(), n.getY() + n.getHeight(), color);
			case Unknown:
				if (_unresolved_paint_cache.containsKey(rgb)) {
					return _unresolved_paint_cache.get(rgb);
				}
				BufferedImage unresolved_texture = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
				unresolved_texture.setRGB(0, 0, rgb);
				unresolved_texture.setRGB(1, 0, 0xffffffff);
				unresolved_texture.setRGB(0, 1, 0xffffffff);
				unresolved_texture.setRGB(1, 1, rgb);
				Paint unresolved_p = new TexturePaint(unresolved_texture, new Rectangle(0, 0, 2, 2));
				_unresolved_paint_cache.put(rgb, unresolved_p);
				return unresolved_p;
			case FreeSpace:
				if (_freespace_paint_cache.containsKey(rgb)) {
					return _freespace_paint_cache.get(rgb);
				}

				int size = 5;

				BufferedImage freespace_texture = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

				for (int i = 0; i < size; ++i) {
					for (int j = 0; j < size; ++j) {
						if (i == j) {
							freespace_texture.setRGB(i, j, rgb);
						} else {
							freespace_texture.setRGB(i, j, 0xffffffff);
						}
					}
				}

				Paint free_p = new TexturePaint(freespace_texture, new Rectangle(0, 0, size, size));
				_freespace_paint_cache.put(rgb, free_p);
				return free_p;
			}
		}
		return color;
	}

	public String renderBranchSummary(TreeNode node) {
		long weight = node.getWeight();
		if (node.getParent() == null) {
			weight = node.getSumChildWeight();
		}
		return _numberRenderer.render(weight) + " / " + node.getLeafNodeCount();
	}

	public String renderBranchTitle(TreeNode node, boolean isdisplayroot) {
		if (isdisplayroot && _displayrootancestry) {
			StringBuilder str = new StringBuilder();
			for (String s : node.getAncestry()) {
				str.append("/").append(s);
			}
			return str.toString();
		}
		return node.getLabel();
	}

	public String renderLeafNode(TreeNode node) {
		return node.getLabel() + "\n(" + _numberRenderer.render(node.getWeight()) + ")";
	}

	public Color getBranchSummaryTextColor(TreeNode node) {
		return Color.black;
	}

	public Color getBranchTitleTextColor(TreeNode node) {
		return Color.black;
	}

	public Font getFont() {
		return _font;
	}

	public ColorProvider getColorProvider() {
		return _colorprovider;
	}

	public ImageIcon getNodeIcon(TreeNode node) {
		return SpaceMapHelper.getNodeIcon(node);
	}

}
