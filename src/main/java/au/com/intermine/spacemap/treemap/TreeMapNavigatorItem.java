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

package au.com.intermine.spacemap.treemap;

import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.navigator.NavigatorItem;
import au.com.intermine.spacemap.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

class TreeMapNavigatorItem extends NavigatorItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private TreeNode _item;

    public TreeMapNavigatorItem(TreeNode item) {
        super(item.getLabel());
        _item = item;
        if (item.getParent() == null) {
            ImageIcon icon = ResourceManager.getIcon("home128.png");
            icon.setImage(icon.getImage());
            setIcon(icon);
        }
    }

    public TreeNode getNode() {
        return _item;
    }
}
