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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.Utils;

public class RadioButtonGroup extends AbstractNavigatorWidget {

    private static final long serialVersionUID = 1L;

    private List<ToggleButton> _buttons;

    private ActionListener _listener;

    public RadioButtonGroup() {
        _buttons = new ArrayList<ToggleButton>();
        Dimension d = new Dimension(100, _preferredHeight);
        setPreferredSize(d);
        setSize(d);
        this.setBorderPainted(false);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 1) {
                    deselectAll();
                    ToggleButton item = (ToggleButton) getItemAt(_buttons, e.getX(), e.getY());
                    if (item != null) {
                        item.setSelected(true);
                        if (_listener != null) {
                            _listener.actionPerformed(new ActionEvent(this, 23, "Changed"));
                        }
                    }
                    calculatePreferredSize(_buttons, getGraphics());
                    RadioButtonGroup.this.repaint();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseMoved(MouseEvent e) {
                ToggleButton item = (ToggleButton) getItemAt(_buttons, e.getX(), e.getY());
                if (item != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    item.Selected = true;
                    setToolTipText(item.getTooltip());
                } else {
                    Utils.cursorDefault(RadioButtonGroup.this);
                }
                calculatePreferredSize(_buttons, getGraphics());
                RadioButtonGroup.this.repaint();
            }
        });

    }

    public void setValue(String value) {
        for (ToggleButton b : _buttons) {
            if (b.getTooltip().equals(value)) {
                deselectAll();
                b.setSelected(true);
                if (_listener != null) {
                    _listener.actionPerformed(new ActionEvent(this, 23, "Changed"));
                }
            }
        }
    }

    public String getValue() {
        for (ToggleButton b : _buttons) {
            if (b.isSelected()) {
                return b.getTooltip();
            }
        }
        return null;
    }


    private void deselectAll() {
        for (ToggleButton b : _buttons) {
            b.setSelected(false);
        }
    }

    public void disengageListener() {
        if (_listener == null) {
            return;
        }
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton) {
                ((AbstractButton) c).removeActionListener(_listener);
            }
        }
    }

    public void reEngageListener() {
        if (_listener == null) {
            return;
        }
        for (Component c : getComponents()) {
            if (c instanceof AbstractButton) {
                ((AbstractButton) c).addActionListener(_listener);
            }
        }
    }

    public void setActionListener(ActionListener listener) {
        _listener = listener;
        reEngageListener();
    }

    private int getPreferredWidth() {
        FontMetrics fm = getGraphics().getFontMetrics();
        int width = 0;
        for (NavigatorItem item : _buttons) {
            if (item.getIcon() == null) {
                width += fm.stringWidth(item.getLabel()) + 30;
            } else {
                width += item.getIcon().getIconWidth() + 20;
            }
        }
        return width;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getPreferredWidth(), getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(_controlColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (_buttons.size() == 0) {
            return;
        }

        int width = getPreferredWidth();
        if (width > getWidth()) {
            width = getWidth();
        }
        fillGradient(g, width, getHeight());

        int offset = 0;
        for (int i = 0; i < _buttons.size(); ++i) {
            ToggleButton item = _buttons.get(i);
            Rectangle r = null;
            ImageIcon icon = item.getIcon();
            r = new Rectangle(offset, 0, icon.getIconWidth() + 20, getHeight());
            int top = (getHeight() / 2) - (icon.getIconHeight() / 2);
            if (top < 0) {
                top = 0;
            }

            if (item.isSelected()) {
                fillGradient(g, r.x + 1, r.y + 2, r.width - 1, r.height - 4, new GradientPaint(0, 1, _darkShadeColor, 0, getHeight() - 3, (Color) UIManager.getLookAndFeelDefaults().get("controlShadow")));
            }

            g.drawImage(icon.getImage(), offset + 11, top, null);



            if (i > 0 && i < _buttons.size()) {
                drawSeperator(g, r.x, r.y, r.width, r.height);
            }
            item.setHitRect(r);
            offset += r.width;
        }
        drawBorder(g, 0, 0, width, this.getHeight());
    }

    private void drawSeperator(Graphics g, int x, int y, int width, int height) {
        DrawingUtils.setPreferredAliasingMode(g);
        g.setColor(_borderColor);
        g.drawLine(x, y + 2, x, height - 2);
    }

    public void addButton(String tooltip, ImageIcon icon) {
        ToggleButton tb = new ToggleButton(tooltip, icon);
        _buttons.add(tb);
        if (getGraphics() != null) {
            calculatePreferredSize(_buttons, getGraphics());
        }
        repaint();
    }

    public void setItems(List<String> items, Map<String, ImageIcon> icons) {
        clearItems();
        for (String item : items) {
            addButton(item, icons.get(item));
        }
    }

    public void clearItems() {
        _buttons.clear();
    }

}
