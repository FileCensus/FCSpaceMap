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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import au.com.intermine.spacemap.SpaceMapMenuItem;
import au.com.intermine.spacemap.util.DrawingUtils;

public class Segment {

	private int _index;
	private double _startAngle;
	private double _endAngle;
	private Point _centre;
	private int _insideRadius;
	private int _outsideRadius;
	private SpaceMapMenuItem _menuItem;

	public Segment(int index, MenuGlassPane menu, double start, double end, SpaceMapMenuItem menuitem) {
		_index = index;
		_startAngle = start;
		_endAngle = end;
		_centre = menu.getCentre();
		_insideRadius = menu.getInsideCircleRadius();
		_outsideRadius = menu.getOutsideCircleRadius();
		_menuItem = menuitem;
	}

	public SpaceMapMenuItem getMenuItem() {
		return _menuItem;
	}
	
	public String getText() {
		return _menuItem.getText();
	}

	public boolean contains(Point p) {

		double distance = p.distance(_centre);

		// first work out if its within our two radii...
		if (distance > _insideRadius && distance < _outsideRadius) {

			// Now see if the angle between the origin (our centre) and the point
			// lies between out start and end angles
			double angle = 0;

			if (p.x >= _centre.x && p.y >= _centre.y) {
				angle = Math.asin((double) (p.x - _centre.x) / distance);
			}

			if (p.x >= _centre.x && p.y < _centre.y) {
				angle = (Math.PI / 2) + Math.asin((double) (_centre.y - p.y) / distance);
			}

			if (p.x < _centre.x && p.y < _centre.y) {
				angle = (Math.PI) + Math.asin((double) (_centre.x - p.x) / distance);
			}

			if (p.x < _centre.x && p.y >= _centre.y) {
				angle = (Math.PI * 1.5) + Math.asin((double) (p.y - _centre.y) / distance);
			}

			if (angle >= _startAngle && angle <= _endAngle) {
				return true;
			}
		}
		return false;
	}

	public int getIndex() {
		return _index;
	}

	public double getStartAngle() {
		return _startAngle;
	}

	public void setStartAngle(double angle) {
		_startAngle = angle;
	}

	public double getEndAngle() {
		return _endAngle;
	}

	public void setEndAngle(double angle) {
		_endAngle = angle;
	}

	public void drawLabel(Graphics2D g) {

		int middleRadius = _insideRadius + ((_outsideRadius - _insideRadius) / 2);

		double angle = _startAngle + ((_endAngle - _startAngle) / 2);
		int x = _centre.x + (int) (Math.sin(angle) * middleRadius);
		int y = _centre.y + (int) (Math.cos(angle) * middleRadius);

		// String msg = String.format("[%d] %.2f-%.2f\n(ctrl+n)", _index, _startAngle, _endAngle);
		ImageIcon icon = _menuItem.getIcon();

		g.setColor(Color.white);
		if (icon != null) {
			int x1 = x - (icon.getIconWidth() / 2);
			int y1 = y - (icon.getIconHeight() / 2);
			g.drawImage(icon.getImage(), x1, y1, null);
		} else {
			String msg = _menuItem.getText();
			Font font = new JLabel().getFont();
			font = font.deriveFont(Font.BOLD);

			DrawingUtils.drawCentredText(g, font, msg, x, y);
		}

		if (false) {
			g.setColor(Color.green);
			g.setStroke(new BasicStroke(1));
			g.drawLine(x, y - 20, x, y + 20);
			g.drawLine(x - 20, y, x + 20, y);
			g.drawOval(_centre.x - middleRadius, _centre.y - middleRadius, middleRadius * 2, middleRadius * 2);
		}
	}

}