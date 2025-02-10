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

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

public class GlassButtonBuilder {

    private GlassButton _button;

    public GlassButtonBuilder() {
        _button = new GlassButton();
    }

    public GlassButtonBuilder anchorLeft(int offset) {
        _button.setLeftOffset(offset);
        _button.setAnchoredLeft(true);
        return this;
    }

    public GlassButtonBuilder anchorRight(int offset) {
        _button.setRightOffset(offset);
        _button.setAnchoredRight(true);
        return this;
    }

    public GlassButtonBuilder anchorTop(int offset) {
        _button.setTopOffset(offset);
        _button.setAnchoredTop(true);
        return this;
    }

    public GlassButtonBuilder anchorBottom(int offset) {
        _button.setBottomOffset(offset);
        _button.setAnchoredBottom(true);
        return this;
    }

    public GlassButtonBuilder anchorBottomRight(int rightoffset, int bottomoffset, int width, int height) {
        _button.setBottomOffset(bottomoffset);
        _button.setAnchoredBottom(true);
        _button.setRightOffset(rightoffset);
        _button.setAnchoredRight(true);
        _button.setPreferredWidth(width);
        _button.setPreferredHeight(height);
        
        return this;
    }

    public GlassButtonBuilder width(int width) {
        _button.setPreferredWidth(width);
        return this;
    }

    public GlassButtonBuilder height(int height) {
        _button.setPreferredHeight(height);
        return this;
    }

    public GlassButtonBuilder dimensions(int width, int height) {
        _button.setPreferredWidth(width);
        _button.setPreferredHeight(height);
        return this;
    }
    
    public GlassButtonBuilder action(ActionListener listener) {
        _button.addActionListener(listener);
        return this;
    }
    
    public GlassButtonBuilder icon(ImageIcon icon) {
        _button.setIcon(icon);
        return this;
    }

    public GlassButton build() {
        return _button;
    }

    public GlassButtonBuilder text(String text) {
        _button.setText(text);
        return this;
    }
    
    public GlassButtonBuilder tooltip(String tooltip) {
    	_button.setTooltip(tooltip);
    	return this;
    }

}
