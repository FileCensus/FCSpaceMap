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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.ResourceManager;

public class PreviousNextButton extends AbstractNavigatorWidget {

	private static final long serialVersionUID = 1L;

	private String _prevToolTip = "";
	private String _nextToolTip = "";
	private boolean _prevEnabled = true;
	private boolean _nextEnabled = true;
	private boolean _prevEnabledPreClick = true;
	private boolean _nextEnabledPreClick = true;
	private boolean _prevDown = false;
	private boolean _nextDown = false;

	private List<PreviousNextListener> _observers = new ArrayList<PreviousNextListener>();

	public PreviousNextButton() {
		this.setPreferredSize(new Dimension(50, _preferredHeight));
		this.setBorder(new EmptyBorder(0, 0, 0, 0));
		ToolTipManager.sharedInstance().setEnabled(true);
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getX() > (getWidth() / 2)) {
					_nextDown = true && _nextEnabled;
				} else {
					_prevDown = true && _prevEnabled;
				}
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getX() > (getWidth() / 2)) {
					if (_nextEnabled) {
						raiseNextClicked(e.getPoint());
					}
				} else {
					if (_prevEnabled) {
						raisePrevClicked(e.getPoint());
					}
				}
				_prevDown = false;
				_nextDown = false;
				repaint();
			};

			@Override
			public void mouseExited(MouseEvent e) {
				_prevDown = false;
				_nextDown = false;
				repaint();
			}

		});
		this.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				if (e.getX() > (getWidth() / 2)) {
					setToolTipText(_nextToolTip);
				} else {
					setToolTipText(_prevToolTip);
				}
			}

		});

		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public void addPreviousNextListener(PreviousNextListener l) {
		if (!_observers.contains(l)) {
			_observers.add(l);
		}
	}

	public void removePreviousNextListener(PreviousNextListener l) {
		if (_observers.contains(l)) {
			_observers.remove(l);
		}

	}

	protected void raiseNextClicked(Point p) {
		setNextEnabledPreClick(getNextEnabled());
		setPreviousEnabledPreClick(getPreviousEnabled());
		setNextEnabled(false);
		setPreviousEnabled(false);
		for (PreviousNextListener l : _observers) {
			l.nextClicked(p);
		}
	}

	protected void raisePrevClicked(Point p) {
		setNextEnabledPreClick(getNextEnabled());
		setPreviousEnabledPreClick(getPreviousEnabled());
		setNextEnabled(false);
		setPreviousEnabled(false);
		for (PreviousNextListener l : _observers) {
			l.previousClicked(p);
		}
	}

	public void setNextTooltip(String tooltip) {
		_nextToolTip = tooltip;
	}

	public void setPreviousTooltip(String tooltip) {
		_prevToolTip = tooltip;
	}

	public String getNextTooltip() {
		return _nextToolTip;
	}

	public String getPreviousTooltip() {
		return _prevToolTip;
	}

	public void setNextEnabled(boolean enabled) {
		_nextEnabled = enabled;
	}

	public void setPreviousEnabled(boolean enabled) {
		_prevEnabled = enabled;
	}

	public boolean getPreviousEnabled() {
		return _prevEnabled;
	}

	public boolean getNextEnabled() {
		return _nextEnabled;
	}

	public void setNextEnabledPreClick(boolean enabled) {
		_nextEnabledPreClick = enabled;
	}

	public void setPreviousEnabledPreClick(boolean enabled) {
		_prevEnabledPreClick = enabled;
	}

	public boolean getPreviousEnabledPreClick() {
		return _prevEnabledPreClick;
	}

	public boolean getNextEnabledPreClick() {
		return _nextEnabledPreClick;
	}

	public void revertToPreClickState() {
		setPreviousEnabled(getPreviousEnabledPreClick());
		setNextEnabled(getNextEnabledPreClick());
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(_controlColor);
		g.fillRect(0, 0, getWidth(), getHeight());

		fillGradient(g, getWidth(), getHeight());
		if (_prevDown) {
			fillGradient(g, 1, 1, (getWidth() / 2) - 1, getHeight() - 3, new GradientPaint(0, 1, _darkShadeColor, 0, getHeight() - 3, (Color) UIManager.getLookAndFeelDefaults().get("controlShadow")));
		}
		if (_nextDown) {
			fillGradient(g, getWidth() / 2, 1, (getWidth() / 2) - 1, getHeight() - 3, new GradientPaint(0, 1, _darkShadeColor, 0, getHeight() - 3, (Color) UIManager.getLookAndFeelDefaults().get(
					"controlShadow")));
		}
		drawBorder(g, 0, 0, getWidth(), this.getHeight());
		DrawingUtils.setPreferredAliasingMode(g);
		g.setColor(_borderColor);
		int x = getWidth() / 2;
		g.drawLine(x, 1, x, getHeight() - 2);
		String prevImage = "Icons/16x16/back_btn_disabled.png";
		String nextImage = "Icons/16x16/next_btn_disabled.png";
		if (_prevEnabled) {
			prevImage = "Icons/16x16/back_btn.png";
		}
		if (_nextEnabled) {
			nextImage = "Icons/16x16/next_btn.png";
		}
		ImageIcon icon = ResourceManager.getIcon(prevImage);
		int imagey = (getHeight() / 2) - (icon.getIconHeight() / 2);
		g.drawImage(icon.getImage(), 6, imagey, null);
		icon = ResourceManager.getIcon(nextImage);
		g.drawImage(icon.getImage(), 4 + x, imagey, null);
	}

}
