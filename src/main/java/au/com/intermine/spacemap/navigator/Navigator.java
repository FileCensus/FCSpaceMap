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

package au.com.intermine.spacemap.navigator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.Utils;

public class Navigator extends AbstractNavigatorWidget {

    private static final int SEPERATOR_WIDTH = 6;

    private static final long serialVersionUID = 1L;
    private List<NavigatorItem> _items;
    private List<NavigatorItemClickedListener> _listeners;

    public Navigator() {
        _items = new ArrayList<NavigatorItem>();
        _listeners = new ArrayList<NavigatorItemClickedListener>();
        Dimension d = new Dimension(100, _preferredHeight);
        this.setPreferredSize(d);
        this.setSize(d);
        this.setBorderPainted(false);
        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        MyMouseAdapater mm = new MyMouseAdapater();
        ToolTipManager.sharedInstance().setEnabled(true);
        this.addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseMoved(MouseEvent e) {
                deselectAll();
                NavigatorItem item = getItemAt(_items, e.getX(), e.getY());
                if (item != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    item.Selected = true;
                    setToolTipText(item.getLabel());
                } else {
                    Utils.cursorDefault(Navigator.this);
                }
                Navigator.this.repaint();
            }
        });
        addMouseListener(mm);
    }

    public void clear() {
        _items.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(_controlColor);
        g.fillRect(0,0, getWidth(), getHeight());

        if (_items.size() == 0) {
            return;
        }

        int width = 0;
        FontMetrics fm = g.getFontMetrics();
        for (NavigatorItem item : _items) {
            if (item.getIcon() == null) {
                width += fm.stringWidth(item.getLabel()) + 30;
            } else {
                width += item.getIcon().getIconWidth() + 20;
            }
        }
        int logicalwidth = width;
        if (width > getWidth()) {
            width = getWidth();
        }

        fillGradient(g, width, getHeight());

        drawBorder(g, 0, 0, width, this.getHeight());

        int offset = 0;
        for (int i = 0; i < _items.size(); ++i) {
            NavigatorItem item = _items.get(i);
            Rectangle r = null;
            if (item.getIcon() == null) {
                int itemwidth = fm.stringWidth(item.getLabel()) + 30;
                if (logicalwidth > width) {
                    itemwidth = (int) ((float) width * ((float) itemwidth / (float) logicalwidth));
                }
                r = new Rectangle(offset, 0, itemwidth, getHeight());
                if (item.Selected) {
                    g.setColor(Color.blue);
                } else {
                    g.setColor(Color.black);
                }
                DrawingUtils.drawString(g, _labelFont, item.getLabel(), r.x + 10, r.y, r.width - 15, r.height, DrawingUtils.TEXT_ALIGN_CENTER);
            } else {
                ImageIcon icon = item.getIcon();
                r = new Rectangle(offset, 0, icon.getIconWidth() + 20, getHeight());
                int top = (getHeight() / 2) - (icon.getIconHeight() / 2);
                if (top < 0) {
                    top = 0;
                }
                g.drawImage(icon.getImage(), offset + 10, top, null);

            }
            if (i > 0 && i < _items.size()) {

                drawSeperator(g, r.x, r.y, r.width, r.height);
            }
            item.setHitRect(r);
            offset += r.width;
        }
    }

    protected void drawSeperator(Graphics g, int x, int y, int width, int height) {
        DrawingUtils.setPreferredAliasingMode(g);
        g.setColor(_borderColor);
        // g.drawLine(x, y + 2, x + SEPERATOR_WIDTH, height / 2);
        g.drawLine(x, height - 3, x + SEPERATOR_WIDTH, y + 2);
    }

    public void addItem(NavigatorItem item) {
        _items.add(item);
        if (getGraphics() != null) {
            calculatePreferredSize(_items, getGraphics());
        }
    }

    private void deselectAll() {
        for (NavigatorItem item : _items) {
            item.Selected = false;
        }
    }

    protected void raiseItemClicked(NavigatorItem item) {
        for (NavigatorItemClickedListener l : _listeners) {
            l.itemClicked(item);
        }
    }

    class MyMouseAdapater extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            deselectAll();
            Navigator.this.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == 1) {
                NavigatorItem item = getItemAt(_items, e.getX(), e.getY());
                if (item != null) {
                    raiseItemClicked(item);
                }
            }
        }
    }

    public void addNavigatorItemClickedListener(NavigatorItemClickedListener listener) {
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }

}
