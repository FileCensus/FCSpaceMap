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

import javax.swing.ImageIcon;

public class ToggleButton extends NavigatorItem {

    private String _tooltip;
    private boolean _selected;

    public ToggleButton(String tooltip, ImageIcon icon) {
        super("");
        setIcon(icon);
        _tooltip = tooltip;
        _selected = false;
    }

    public String getTooltip() {
        return _tooltip;
    }

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

}
