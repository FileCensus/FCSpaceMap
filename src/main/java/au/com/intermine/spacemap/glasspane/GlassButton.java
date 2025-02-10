/*
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

package au.com.intermine.spacemap.glasspane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import au.com.intermine.spacemap.util.DrawingUtils;

public class GlassButton {
    
    private static final int ARC_SIZE = 10;

	private Rectangle _rect;

	private static Color BORDER_COLOR = new Color(255, 255, 255, 50);

	private static Color BACKGROUND_COLOR = new Color(0, 0, 0, 180);
	private static BasicStroke BORDER_STROKE = new BasicStroke(1);


	private boolean _anchoredLeft = false;
	private boolean _anchoredRight = false;
	private boolean _anchoredTop = false;
	private boolean _anchoredBottom = false;

	private int _preferredX = 0;
	private int _preferredY = 0;
	private int _preferredWidth;
	private int _preferredHeight;

	private int _leftOffset;
	private int _rightOffset;
	private int _topOffset;
	private int _bottomOffset;

	private boolean _hovering;
	private boolean _depressed;
	
    protected Font _font;

	private List<ActionListener> _actionListeners;
	
	private ImageIcon _icon;

	private String _text;
	
	private String _tooltip;

	public GlassButton() {
		_actionListeners = new ArrayList<ActionListener>();
		_hovering = false;
		_font = new JLabel().getFont().deriveFont(Font.BOLD);
	}

	public boolean isAnchoredLeft() {
		return _anchoredLeft;
	}

	public void addActionListener(ActionListener listener) {
		_actionListeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		if (_actionListeners.contains(listener)) {
			_actionListeners.remove(listener);
		}
	}

	protected void fireActionEvent() {
		ActionEvent e = new ActionEvent(this, ActionEvent.RESERVED_ID_MAX, "click");
		for (ActionListener l : _actionListeners) {
			l.actionPerformed(e);
		}
	}

	public void setAnchoredLeft(boolean left) {
		_anchoredLeft = left;
	}

	public boolean isAnchoredRight() {
		return _anchoredRight;
	}

	public void setAnchoredRight(boolean right) {
		_anchoredRight = right;
	}

	public boolean isAnchoredTop() {
		return _anchoredTop;
	}

	public void setAnchoredTop(boolean top) {
		_anchoredTop = top;
	}

	public boolean isAnchoredBottom() {
		return _anchoredBottom;
	}

	public void setAnchoredBottom(boolean bottom) {
		_anchoredBottom = bottom;
	}

	public int getPreferredWidth() {
		return _preferredWidth;
	}

	public void setPreferredWidth(int width) {
		_preferredWidth = width;
	}

	public int getPreferredHeight() {
		return _preferredHeight;
	}

	public void setPreferredHeight(int height) {
		_preferredHeight = height;
	}

	public int getLeftOffset() {
		return _leftOffset;
	}

	public void setLeftOffset(int offset) {
		_leftOffset = offset;
	}

	public int getRightOffset() {
		return _rightOffset;
	}

	public void setRightOffset(int offset) {
		_rightOffset = offset;
	}

	public int getTopOffset() {
		return _topOffset;
	}

	public void setTopOffset(int offset) {
		_topOffset = offset;
	}

	public int getBottomOffset() {
		return _bottomOffset;
	}

	public void setBottomOffset(int offset) {
		_bottomOffset = offset;
	}

	public Rectangle calculateRect(int parentWidth, int parentHeight) {
		Rectangle rect = new Rectangle(_preferredX, _preferredY, _preferredWidth, _preferredHeight);
		if (_anchoredLeft && _anchoredRight) {
			// The width becomes dynamic based on the left and right offsets
			rect.x = _leftOffset;
			rect.width = parentWidth - (_leftOffset + _rightOffset);
		} else if (_anchoredLeft) {
			rect.x = _leftOffset;
		} else if (_anchoredRight) {
			rect.x = parentWidth - (_preferredWidth + _rightOffset);
		}

		if (_anchoredTop && _anchoredBottom) {
			// The control stretches vertically
			rect.y = _topOffset;
			rect.height = parentHeight - (_topOffset + _bottomOffset);
		} else if (_anchoredTop) {
			rect.y = _topOffset;
		} else if (_anchoredBottom) {
			rect.y = parentHeight - (_preferredHeight + _bottomOffset);
		}

		return rect;
	}

	protected void paint(Graphics2D g, Rectangle rect) {
		_rect = rect;
		drawBorder(g);
		if (_icon != null) {
		    int x1 = rect.x + (rect.width / 2) - (_icon.getIconWidth() / 2) + 1;
		    int y1 = rect.y + (rect.height / 2) - (_icon.getIconHeight() / 2) + 1;
			g.drawImage(_icon.getImage(), x1, y1, x1 + _icon.getIconWidth(), y1 + _icon.getIconHeight(), 0, 0, _icon.getIconWidth(), _icon.getIconHeight(), null);
		}
		if (_text != null) {
		    g.setColor(Color.white);
			DrawingUtils.drawString(g, _font, _text, rect, DrawingUtils.TEXT_ALIGN_CENTER);
		}
	}
	
	public void setIcon(ImageIcon icon) {
		_icon = icon;
	}
	
	public ImageIcon getIcon() {
		return _icon;
	}

	protected void drawBorder(Graphics2D g) {
		g.setStroke(BORDER_STROKE);
		if (_hovering) {
		    if (_depressed) {
		        g.setColor(Color.black);
		    } else {
		        _depressed = false;
		        g.setColor(BORDER_COLOR);		       
		    }
			g.fillRoundRect(_rect.x, _rect.y, _rect.width, _rect.height, ARC_SIZE, ARC_SIZE);
			g.drawRoundRect(_rect.x, _rect.y, _rect.width, _rect.height, ARC_SIZE, ARC_SIZE);
		} else {
			g.setColor(BACKGROUND_COLOR);
			g.fillRoundRect(_rect.x, _rect.y, _rect.width, _rect.height, ARC_SIZE, ARC_SIZE);
			g.setColor(BORDER_COLOR);
			g.drawRoundRect(_rect.x, _rect.y, _rect.width, _rect.height, ARC_SIZE, ARC_SIZE);
		}
	}

	public boolean contains(Point p) {
		if (_rect != null) {
			return _rect.contains(p);
		}
		return false;
	}

	public void setHovering(boolean hovering) {
		_hovering = hovering;
	}

	public boolean isHovering() {
		return _hovering;
	}

	public void mouseDown(MouseEvent e) {
	    _depressed =  true;
	}

	public void mouseUp(MouseEvent e) {
	    _depressed = false;
		fireActionEvent();
	}

	public Rectangle paint(Graphics2D g, int parentWidth, int parentHeight) {
		Rectangle r = calculateRect(parentWidth, parentHeight);
		paint(g, r);
		return r;
	}

	public void setText(String text) {
		_text = text;
	}
	
	public String getText() {
		return _text;
	}
	
	public void setTooltip(String tooltip) {
		_tooltip = tooltip;
	}
	
	public String getTooltip() {
		return _tooltip;
	}

}

