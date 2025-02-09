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

package au.com.intermine.spacemap.navigator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;

import au.com.intermine.spacemap.util.DrawingUtils;

public abstract class AbstractNavigatorWidget extends JButton {

	private static final long serialVersionUID = 1L;
	
	protected Color _lightShadeColor;
	protected Color _darkShadeColor;
	protected Color _borderColor;
	protected Color _controlColor;
	protected Font _labelFont;
	protected int _preferredHeight = 22;

	public AbstractNavigatorWidget() {
		_lightShadeColor = (Color) UIManager.getLookAndFeelDefaults().get("controlHighlight");
		_darkShadeColor = (Color) UIManager.getLookAndFeelDefaults().get("control");
		_borderColor = (Color) UIManager.getLookAndFeelDefaults().get("controlDkShadow");
		_controlColor = (Color) UIManager.getLookAndFeelDefaults().get("control");
		JLabel lbl = new JLabel();
		_labelFont = lbl.getFont();
		this.setFocusable(false);
	}

	protected void fillGradient(Graphics g, int width, int height) {
		fillGradient(g, 1, 1, width - 2, height - 2, new GradientPaint(1, 1, _lightShadeColor, 1, getHeight() - 2, _darkShadeColor));
	}

	protected void fillGradient(Graphics g, int x, int y, int width, int height, Paint paint) {
		Graphics2D g2 = (Graphics2D) g;
		Paint storedPaint = g2.getPaint();
		g2.setPaint(paint);
		g.fillRect(x, y, width, height);
		g2.setPaint(storedPaint);
	}

	protected void drawBorder(Graphics g, int left, int top, int width, int height) {
		DrawingUtils.setPreferredAliasingMode(g);
		g.setColor(_borderColor);
		DrawingUtils.drawRoundedRect((Graphics2D) g, 0, top + 1, width - 1, height - 2);
		g.setColor(_controlColor);
		g.drawLine(left, top, left, top);
		g.drawLine(left, height - 1, top, height - 1);
		g.drawLine(width - 1, top, width - 1, top);
		g.drawLine(width - 1, height - 1, width - 1, height - 1);
	}

	protected void calculatePreferredSize(List<? extends NavigatorItem> items, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int size = 0;
		for (NavigatorItem item : items) {
			if (item.getIcon() == null) {
				size += fm.stringWidth(item.getLabel()) + 30;
			} else {
				size += item.getIcon().getIconWidth() + 20;
			}
		}
		Dimension d = new Dimension(size, _preferredHeight);
		setPreferredSize(d);
		setSize(d);
	}

	protected NavigatorItem getItemAt(List<? extends NavigatorItem> items, int x, int y) {
		for (NavigatorItem item : items) {
			Rectangle r = item.getHitRect();
			if (r != null) {
				if (x > r.x && x < (r.x + r.width)) {
					return item;
				}
			}
		}
		return null;
	}

}
