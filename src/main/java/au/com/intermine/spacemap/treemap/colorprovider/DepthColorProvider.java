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

package au.com.intermine.spacemap.treemap.colorprovider;

import java.awt.Color;
import java.util.List;

import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.util.ColorUtils;

public class DepthColorProvider extends ColorProvider {

    private static Color[] _defaultcolors = new Color[] { new Color(255, 144, 144), new Color(255, 192, 128), new Color(255, 255, 0), new Color(128, 255, 128), new Color(128, 255, 255),
            new Color(192, 192, 255), new Color(243, 122, 243) };

    private Color _unknownColor = Color.lightGray;
    private Color[] _colors = _defaultcolors;
    
    public DepthColorProvider() {    	
    }
    
    public DepthColorProvider(Color[] colors) {
    	_colors = colors;
    }

    @Override
    public Color getColor(TreeNode item) {    	
        // unknown object type?
        if (item.getNodeType() == NodeType.Unknown) {
            return _unknownColor;
        }

        // otherwise use the depth
        int l = item.getLevel();
        while (l >= _colors.length) {
            l = l - _colors.length;
        }
        return _colors[l];
    }

    public void setUnknownColor(Color c) {
        _unknownColor = c;
    }

    public void setColors(Color[] colors) {
        _colors = colors;
    }

    public void setColors(List<String> colorNames) {
        if (colorNames != null && !colorNames.isEmpty()) {
            Color[] colorsArray = new Color[colorNames.size()];
            for (int i = 0; i < colorNames.size(); i++) {
                colorsArray[i] = ColorUtils.getColor(colorNames.get(i));
            }
            _colors = colorsArray;
        } else {
            _colors = _defaultcolors;
        }

    }

}
