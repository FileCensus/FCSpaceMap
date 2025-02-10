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

import java.awt.Rectangle;

import javax.swing.ImageIcon;

public class NavigatorItem {

    String _label;
    private Rectangle _hitrect;
    private ImageIcon _icon;

    public boolean Selected = false;

    public NavigatorItem(String label) {
        _label = label;
        _icon = null;
    }

    public void setIcon(ImageIcon icon) {
        _icon = icon;
    }

    public ImageIcon getIcon() {
        return _icon;
    }

    public String getLabel() {
        return _label;
    }
    
    public void setLabel(String label) {
        _label = label;
    }

    public void setHitRect(Rectangle r) {
        _hitrect = r;
    }

    public Rectangle getHitRect() {
        return _hitrect;
    }
}