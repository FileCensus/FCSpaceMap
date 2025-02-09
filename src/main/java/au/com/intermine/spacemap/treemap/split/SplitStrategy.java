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

package au.com.intermine.spacemap.treemap.split;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;

public abstract class SplitStrategy {

    public void calculatePositions(TreeNode root) {
        if (root == null) {
            return;
        }

        List<TreeNode> v = root.getChildren();
        Insets insets = TreeMapRectangle.getInsets();
        if (v != null) {
            TreeMapRectangle rect = (TreeMapRectangle) root.getRectangle();
            calculatePositionsRec(rect.getX() + insets.left, rect.getY() + insets.top, rect.getWidth() - insets.left - insets.right, rect.getHeight() - insets.top - insets.bottom, sumWeight(v), v);
        }
    }

    protected abstract void splitElements(List<TreeNode> v, List<TreeNode> v1, List<TreeNode> v2);

    protected long sumWeight(List<TreeNode> v) {
        long d = 0;
        if (v != null) {
            for (TreeNode n : v) {
                d += n.getWeight();
            }
        }
        return d;
    }

    protected void calculatePositionsRec(int x0, int y0, int w0, int h0, long weight0, List<TreeNode> v) {

        // if the Vector contains only one element
        if (v.size() == 1) {
            TreeNode f = v.get(0);
            TreeMapRectangle rect = (TreeMapRectangle) f.getRectangle();
            if (f.isLeaf()) {
                rect.setDimension(x0, y0, w0, h0);
            } else {
                // if this is not a leaf, calculation for the children
                rect.setDimension(x0, y0, w0, h0);
                Insets insets = TreeMapRectangle.getInsets();
                calculatePositionsRec(x0 + insets.left, y0 + insets.top, w0 - insets.left - insets.right, h0 - insets.top - insets.bottom, weight0, f.getChildren());
            }
        } else {
            // if there is more than one element
            // we split the Vector according to the selected strategy
            List<TreeNode> v1 = new ArrayList<TreeNode>();
            List<TreeNode> v2 = new ArrayList<TreeNode>();
            long weight1, weight2;
            splitElements(v, v1, v2);
            weight1 = sumWeight(v1);
            weight2 = sumWeight(v2);

            int w1, w2;
            int h1, h2;
            int x2, y2;
            // if width is greater than height, we split the width
            if (w0 > h0) {
                w1 = (int) (w0 * weight1 / weight0);
                w2 = w0 - w1;
                h1 = h0;
                h2 = h0;
                x2 = x0 + w1;
                y2 = y0;
            } else {
                // else we split the height
                w1 = w0;
                w2 = w0;
                h1 = (int) (h0 * weight1 / weight0);
                h2 = h0 - h1;
                x2 = x0;
                y2 = y0 + h1;
            }
            // calculation for the new two Vectors
            calculatePositionsRec(x0, y0, w1, h1, weight1, v1);
            calculatePositionsRec(x2, y2, w2, h2, weight2, v2);
        }
    }

    protected void sortVector(List<TreeNode> l) {
        Collections.sort(l, new TreeNodeComparator());
    }

}

class TreeNodeComparator implements Comparator<TreeNode> {

    public int compare(TreeNode o1, TreeNode o2) {
        long weight1 = o1.getWeight();
        long weight2 = o2.getWeight();
        if (weight1 == weight2) {
            return 0;
        } else if (weight2 < weight1) {
            return -1;
        }
        return 1;
    }

}
