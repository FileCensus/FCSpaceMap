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

package au.com.intermine.spacemap.model.visitor;

import au.com.intermine.spacemap.model.ITreeNodeVisitor;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;

public class UnknownAreaCounter implements ITreeNodeVisitor {

    private long _area = 0;

    public void visit(TreeNode node) {
        TreeMapRectangle n = (TreeMapRectangle) node.getRectangle();
        if (node.getNodeType() == NodeType.Unknown) {
            _area += n.getHeight() * n.getWidth();
        }
    }

    public long getArea() {
        return _area;
    }

}
