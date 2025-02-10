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

package au.com.intermine.spacemap.treemap.split;

import java.awt.Insets;
import java.util.List;

import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;

public class SplitBySlice extends SplitStrategy {

    /**
     * Calculate the dimension of the elements of the Vector.
     * 
     * @param x0
     *            x-coordinate
     * @param y0
     *            y-coordinate
     * @param w0
     *            width
     * @param h0
     *            height
     * @param v
     *            elements to split in the dimensions before
     * @param sumWeight
     *            sum of the weights
     */
    public static void splitInSlice(int x0, int y0, int w0, int h0, List<TreeNode> v, double sumWeight) {
        int offset = 0;
        boolean vertical = h0 > w0;

        for (TreeNode node : v) {

            TreeMapRectangle rect = node.getRectangle();
            if (vertical) {
                rect.setX(x0);
                rect.setWidth(w0);
                rect.setY(y0 + offset);
                rect.setHeight((int) Math.floor(h0 * node.getWeight() / sumWeight));
                offset = offset + rect.getHeight();
            } else {
                rect.setX(x0 + offset);
                rect.setWidth((int) Math.floor(w0 * node.getWeight() / sumWeight));
                rect.setY(y0);
                rect.setHeight(h0);
                offset = offset + rect.getWidth();
            }
        }
    }

    @Override
    public void splitElements(List<TreeNode> v, List<TreeNode> v1, List<TreeNode> v2) {
        // ignore

    }

    @Override
    protected void calculatePositionsRec(int x0, int y0, int w0, int h0, long weight0, List<TreeNode> v, int recurseDepth) {

        SplitBySlice.splitInSlice(x0, y0, w0, h0, v, weight0);
        for (TreeNode node : v) {
            TreeMapRectangle rect = node.getRectangle();
            if (node.isLeaf()) {
                rect.setHeight(rect.getHeight());
                rect.setWidth(rect.getWidth());
            } else {
                // if this is not a leaf, calculation for the children
                Insets insets = TreeMapRectangle.getInsets();
                calculatePositionsRec(rect.getX() + insets.left, rect.getY() + insets.top, rect.getWidth() - insets.left - insets.right, rect.getHeight() - insets.top - insets.bottom, node.getWeight(), node.getChildren(), recurseDepth + 1);
            }
        }
    }

}
