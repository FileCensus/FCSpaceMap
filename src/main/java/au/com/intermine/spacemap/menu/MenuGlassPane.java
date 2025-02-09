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

package au.com.intermine.spacemap.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileSystemView;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.Animator.Direction;

import au.com.intermine.spacemap.SpaceMap;
import au.com.intermine.spacemap.SpaceMapHelper;
import au.com.intermine.spacemap.SpaceMapMenuItem;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;
import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.NumberRenderer;
import au.com.intermine.spacemap.util.NumberType;
import au.com.intermine.spacemap.util.Utils;

public class MenuGlassPane extends JComponent implements ItemListener, TimingTarget {

	private static final long serialVersionUID = 1L;

	private static final int ANIMATION_DURATION = 200;

	private static final int MAX_SEGMENTS = 6;

	private TreeNode _node;

	private int _finalDiameter = 150;

	private int _currentDiameter = 0;

	private int _arcWidth = 0;

	private List<Segment> _segments;

	private Segment _selectedSegment = null;

	private List<SpaceMapMenuItem> _menuItems;

	private Color _menuColor = new Color(0, 0, 0, 180);

	private Color _statusPanelColor = new Color(0, 0, 0, 210);

	private BasicStroke _lineStroke = new BasicStroke(3);

	private int _insideCircleR;

	private int _outsideCircleR;

	private Point _centre;

	private double _arcRatio = 2.8;

	private static NumberRenderer _NumberRender = new NumberRenderer(NumberType.Bytes, 1024);

	private static SimpleDateFormat _DateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private Point _startPoint;

	private Font _font = new JLabel().getFont().deriveFont(Font.BOLD);

	public MenuGlassPane(TreeNode node, List<SpaceMapMenuItem> menuitems) {
		GlassPaneListener listener = new GlassPaneListener(this, SpaceMap.getInstance().getContentPane(), node);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		addKeyListener(listener);
		_node = node;
		_menuItems = menuitems;
		_startPoint = MouseInfo.getPointerInfo().getLocation();
	}

	public SpaceMapMenuItem getSelectedMenuItem() {
		if (_selectedSegment != null) {
			return _selectedSegment.getMenuItem();
		}
		return null;
	}

	// React to change button clicks.
	public void itemStateChanged(ItemEvent e) {
		setVisible(e.getStateChange() == ItemEvent.SELECTED);
	}

	public void openMenu() {
		setVisible(true);
		this.requestFocus();
		Animator anim = new Animator(ANIMATION_DURATION, this);
		anim.start();
	}

	public void closeMenu() {
		Animator anim = new Animator(ANIMATION_DURATION, this);
		anim.setStartFraction(1);
		anim.setStartDirection(Direction.BACKWARD);
		anim.addTarget(new TimingTargetAdapter() {
			@Override
			public void end() {
				setVisible(false);
			}
		});
		anim.start();
	}

	protected void paintComponent(Graphics oldg) {

		Graphics2D g2d = (Graphics2D) oldg;
		if (_currentDiameter == _finalDiameter) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}

		TreeMapRectangle rect = _node.getRectangle();

		Point nodeTopLeft = SwingUtilities.convertPoint(SpaceMap.getVisualisation().getWidget(), rect.getX(), rect.getY(), this);

		// Work out a few key numbers for later
		int radius = _currentDiameter / 2;
		_arcWidth = (int) (_currentDiameter / _arcRatio);
		int halfArcWidth = _arcWidth / 2;

		Point p = new Point(_startPoint);
		SwingUtilities.convertPointFromScreen(p, this);

		BasicStroke arcStroke = new BasicStroke(_arcWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

		// // Work out where we are going to put the centre of the menu (adjusting if we run into the edge of the window)
		// int x = nodeTopLeft.x + (rect.getWidth() / 2) - radius;
		// int y = nodeTopLeft.y + (rect.getHeight() / 2) - radius;

		int x = p.x - radius;
		int y = p.y - radius;

		if (x + _currentDiameter + halfArcWidth > getWidth()) {
			x = getWidth() - (_currentDiameter + (halfArcWidth));
		}

		if (y + _currentDiameter + halfArcWidth > getHeight()) {
			y = getHeight() - (_currentDiameter + halfArcWidth);
		}

		if (y < halfArcWidth) {
			y = halfArcWidth;
		}

		if (x < halfArcWidth) {
			x = halfArcWidth;
		}

		_centre = new Point(x + radius, y + radius);
		Rectangle nrect = new Rectangle(nodeTopLeft.x, nodeTopLeft.y, rect.getWidth(), rect.getHeight());
		// Draw highlights
		if (!nrect.contains(_centre)) {
			drawPointer(g2d, nrect);
		}

		g2d.setStroke(arcStroke);
		g2d.setColor(_menuColor);

		// Draw the menu donut
		g2d.drawOval(x, y, _currentDiameter, _currentDiameter);
		// add the segment lines

		_insideCircleR = (_currentDiameter / 2) - (int) (((double) _currentDiameter / _arcRatio) / 2) + 1;
		_outsideCircleR = _insideCircleR + _arcWidth - 1;

		drawSegments(g2d);

		// Draw the inner and outer circles
		g2d.setStroke(_lineStroke);

		g2d.drawOval(_centre.x - _outsideCircleR, _centre.y - _outsideCircleR, _outsideCircleR * 2, _outsideCircleR * 2);
		g2d.drawOval(_centre.x - _outsideCircleR, _centre.y - _outsideCircleR, _outsideCircleR * 2, _outsideCircleR * 2);
		g2d.drawOval(_centre.x - _insideCircleR, _centre.y - _insideCircleR, _insideCircleR * 2, _insideCircleR * 2);
		
		// Draw help/status text..
		if (_currentDiameter == _finalDiameter) {
			drawStatusPanel(g2d);
		}

		// Highlight current segment
		if (_selectedSegment != null) {
			g2d.setColor(_menuColor);
			g2d.setStroke(arcStroke);
			double starta = ((_selectedSegment.getStartAngle() / (2.0 * Math.PI)) * 360.0) - 90;
			int arcangle = 360 / _segments.size();
			g2d.drawArc(x, y, _currentDiameter, _currentDiameter, (int) starta, arcangle);

			g2d.setStroke(new BasicStroke(1));

			Rectangle tiprect = null;
			if (_centre.y + _outsideCircleR + 35 < getHeight()) {
				tiprect = new Rectangle(_centre.x - _outsideCircleR, _centre.y + _outsideCircleR + 10, _outsideCircleR * 2, 25);
			} else {
				tiprect = new Rectangle(_centre.x - _outsideCircleR, _centre.y - _outsideCircleR - 35, _outsideCircleR * 2, 25);
			}
			if (tiprect != null) {
				g2d.fillRoundRect(tiprect.x, tiprect.y, tiprect.width, tiprect.height, 10, 10);
				g2d.setColor(Color.white);
				DrawingUtils.drawString(g2d, _font, _selectedSegment.getText(), tiprect, DrawingUtils.TEXT_ALIGN_CENTER);
			}
		}

		for (Segment seg : _segments) {
			seg.drawLabel(g2d);
		}

	}

	private void drawStatusPanel(Graphics2D g) {
		g.setColor(_statusPanelColor);
		int width = (int) (getWidth() * 0.95);
		int yoffset = 10;
		int x = (getWidth() - width) / 2;
		int height = 70;
		// Work out if this will overlap with the menu circle, if so, move it..
		// Start out at the bottom
		int y = getHeight() - height - yoffset;

		if (_centre.y + _outsideCircleR > y) {
			y = yoffset;
			if ((_centre.y - _outsideCircleR) < (y + height)) {
				// Can't display without colliding1
				return;
			}
		}

		g.fillRoundRect(x, y, width, height, 20, 20);

		ImageIcon icon = SpaceMapHelper.getNodeIcon(_node);
		int txtoffset = 10;
		if (icon != null) {
			g.drawImage(icon.getImage(), x + 10, y + (height / 2) - (icon.getIconHeight() / 2), null);
			txtoffset += icon.getIconWidth() + 5;
		}

		File f = Utils.getFileFromTreeNode(_node);
		String lastmodified = _DateFormatter.format(new Date(f.lastModified()));

		String filetype = "";
		if (f.exists()) {
			if (f.isDirectory()) {
				int filecount = 0;
				int foldercount = 0;
				for (TreeNode child : _node.getChildren()) {
					if (child.getNodeType() == NodeType.File) {
						filecount++;
					} else if (child.getNodeType() == NodeType.Folder) {
						foldercount++;
					}
				}
				filetype = String.format("(Folder with %d files and %d subfolders)", filecount, foldercount);
			} else {
				// get the collection this file would belong in?
				FileSystemView view = FileSystemView.getFileSystemView();
				filetype = "(" + view.getSystemTypeDescription(f) + ")";
			}
		}

		String caption = String.format("%s\n%s %s\nLast Modified:%s", f.getAbsolutePath(), _NumberRender.render(_node.getWeight()), filetype, lastmodified);

		g.setColor(Color.white);
		DrawingUtils.drawString(g, _font, caption, x + txtoffset, y, width - txtoffset, height, DrawingUtils.TEXT_ALIGN_LEFT);

	}

	private void drawSegments(Graphics2D g) {

		int segments = _menuItems.size();

		boolean needMoreSegment = false;
		if (segments > MAX_SEGMENTS) {
			segments = MAX_SEGMENTS;
			needMoreSegment = true;
		}

		g.setStroke(_lineStroke);

		ArrayList<Segment> l = new ArrayList<Segment>();

		double deltaangle = (2 * Math.PI) / (double) segments;

		double startangle = 0; // Like to do this,but causes arc weirdness = 0.0 - (deltaangle / 2.0);

		for (int i = 0; i < segments; ++i) {
			double endangle = startangle + deltaangle;

			int x1 = (int) (Math.sin(startangle) * (_insideCircleR + 1));
			int y1 = (int) (Math.cos(startangle) * (_insideCircleR + 1));
			int x2 = (int) (Math.sin(startangle) * (_outsideCircleR - 1));
			int y2 = (int) (Math.cos(startangle) * (_outsideCircleR - 1));
			g.drawLine(_centre.x + x1, _centre.y + y1, _centre.x + x2, _centre.y + y2);

			SpaceMapMenuItem item = null;

			if (i == segments - 1 && needMoreSegment) {
				item = new PopupMenuItem(this, _menuItems.subList(i, _menuItems.size()), _node);
			} else {
				item = _menuItems.get(i);
			}

			Segment s = new Segment(i, this, startangle, endangle, item);

			l.add(s);
			startangle = endangle;
		}
		_segments = l;

	}

	private void drawPointer(Graphics2D g, Rectangle rect) {
		g.setStroke(new BasicStroke(3));
		g.setColor(Color.red);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);

		int dx = rect.x;
		int dy = rect.y + (rect.height / 2);

		if (_centre.x > rect.x + rect.width) {
			dx = rect.x + rect.width;
		}

		if (_centre.y < rect.y) {
			dy = rect.y;
		}

		if (_centre.y > rect.getY() + rect.getHeight()) {
			dy = rect.y + rect.height;
		}

		if (_centre.x >= rect.x && _centre.x <= rect.x + rect.width) {
			dx = rect.x + rect.width / 2;
		}

		g.drawLine(_centre.x, _centre.y, dx, dy);

	}

	public void begin() {
	}

	public void end() {
		_currentDiameter = _finalDiameter;
	}

	public void repeat() {
	}

	public void timingEvent(float fraction) {
		_currentDiameter = (int) ((float) _finalDiameter * fraction);
		repaint();
	}

	public Point getCentre() {
		return _centre;
	}

	public int getInsideCircleRadius() {
		return _insideCircleR;
	}

	public int getOutsideCircleRadius() {
		return _outsideCircleR;
	}

	/**
	 * Listen for all events on the glass pane, and basically ignore all but a few
	 */
	class GlassPaneListener extends MouseInputAdapter implements KeyListener {

		private MenuGlassPane _glassPane;

		private TreeNode _node;

		public GlassPaneListener(MenuGlassPane glassPane, Container contentPane, TreeNode node) {
			this._glassPane = glassPane;
			_node = node;
		}

		public TreeNode getNode() {
			return _node;
		}

		public void mouseMoved(MouseEvent e) {
			handleMovingEvent(e);
		}

		public void mouseDragged(MouseEvent e) {
			handleMovingEvent(e);
		}

		private void handleMovingEvent(MouseEvent e) {
			int oldindex = (_glassPane._selectedSegment != null ? _glassPane._selectedSegment.getIndex() : -1);
			_glassPane._selectedSegment = null;

			if (_segments != null && _glassPane._currentDiameter == _glassPane._finalDiameter) {
				for (Segment s : _glassPane._segments) {
					if (s.contains(e.getPoint())) {
						_glassPane._selectedSegment = s;
						break;
					}
				}
			}
			redispatchMouseEvent(e, oldindex != (_glassPane._selectedSegment != null ? _glassPane._selectedSegment.getIndex() : -1));
		}

		public void mouseClicked(MouseEvent e) {
			redispatchMouseEvent(e, false);
		}

		public void mouseEntered(MouseEvent e) {
			redispatchMouseEvent(e, false);
		}

		public void mouseExited(MouseEvent e) {
			redispatchMouseEvent(e, false);
		}

		public void mousePressed(MouseEvent e) {
			if (e.getButton() == 1) {
				if (_glassPane._selectedSegment != null) {
					_glassPane._selectedSegment.getMenuItem().performAction();
				}
			}
			_glassPane.closeMenu();
			redispatchMouseEvent(e, false);
		}

		public void mouseReleased(MouseEvent e) {
			redispatchMouseEvent(e, true);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			redispatchMouseEvent(e, false);
		}

		// The version of the redispatch consumes everything!
		private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
			if (repaint) {
				_glassPane.repaint();
			}
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				_glassPane.closeMenu();
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}

}

