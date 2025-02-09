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
import java.util.List;

import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;

/**
 * The Squarified split strategy
 */

public class SplitSquarified extends SplitStrategy {

    private int _w1, _h1;

    private int _x, _y, _w, _h;

    private int _x2, _y2, _w2, _h2;

    @Override
    public void splitElements(List<TreeNode> v, List<TreeNode> v1, List<TreeNode> v2) {
        int mid = 0;
        double weight0 = sumWeight(v);
        double a = v.get(mid).getWeight() / weight0;
        double b = a;

        if (_w < _h) {
            // height/width
            while (mid < v.size()) {
                double aspect = normAspect(_h, _w, a, b);
                double q = v.get(mid).getWeight() / weight0;
                if (normAspect(_h, _w, a, b + q) > aspect) {
                    break;
                }
                mid++;
                b += q;
            }
            int i = 0;
            for (; i <= mid && i < v.size(); i++) {
                v1.add(v.get(i));
            }
            for (; i < v.size(); i++) {
                v2.add(v.get(i));
            }
            _h1 = (int) Math.floor(_h * b);
            _w1 = _w;
            _x2 = _x;
            _y2 = (int) Math.floor(_y + _h * b);
            _w2 = _w;
            _h2 = _h - _h1;
        } else {
            // width/height
            while (mid < v.size()) {
                double aspect = normAspect(_w, _h, a, b);
                double q = v.get(mid).getWeight() / weight0;
                if (normAspect(_w, _h, a, b + q) > aspect) {
                    break;
                }
                mid++;
                b += q;
            }
            int i = 0;
            for (; i <= mid && i < v.size(); i++) {
                v1.add(v.get(i));
            }
            for (; i < v.size(); i++) {
                v2.add(v.get(i));
            }
            _h1 = _h;
            _w1 = (int) Math.floor(_w * b);
            _x2 = (int) Math.floor(_x + _w * b);
            _y2 = _y;
            _w2 = _w - _w1;
            _h2 = _h;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.come.intermine.treemap.SplitStrategy#calculatePositionsRec(int, int, int, int, double, java.util.Vector)
     */
    @Override
    protected void calculatePositionsRec(int x0, int y0, int w0, int h0, long weight0, List<TreeNode> v) {
        if (w0 >= 1 && h0 >= 1) {
            List<TreeNode> vClone = new ArrayList<TreeNode>(v);
            sortVector(vClone);
            if (vClone.size() <= 2) {
                SplitBySlice.splitInSlice(x0, y0, w0, h0, vClone, sumWeight(vClone));
                calculateChildren(vClone);
            } else {
                List<TreeNode> v1 = new ArrayList<TreeNode>();
                List<TreeNode> v2 = new ArrayList<TreeNode>();
                _x = x0;
                _y = y0;
                _w = w0;
                _h = h0;
                splitElements(vClone, v1, v2);
                // before the recurse, we have to "save" the values for the 2nd Vector
                int x2 = _x2;
                int y2 = _y2;
                int w2 = _w2;
                int h2 = _h2;
                SplitBySlice.splitInSlice(x0, y0, _w1, _h1, v1, sumWeight(v1));
                calculateChildren(v1);
                calculatePositionsRec(x2, y2, w2, h2, sumWeight(v2), v2);
            }
        } else {
            // need to zero out any existing regions...
            for (int i = 0; i < v.size(); ++i) {
                TreeNode child = v.get(i);
                if (child != null) {
                    TreeMapRectangle rect = child.getRectangle();
                    rect.setDimension(0, 0, 0, 0);
                }
            }
        }

    }

    private double aspect(double big, double small, double a, double b) {
        return (big * b) / (small * a / b);
    }

    /**
     * Execute the recurrence for the children of the elements of the vector.<BR>
     * Add also the borders if necessary
     * 
     * @param v
     *            Vector with the elements to calculate
     */
    private void calculateChildren(List<TreeNode> v) {
        for (TreeNode node : v) {
            TreeMapRectangle rect = (TreeMapRectangle) node.getRectangle();
            if (node.isLeaf()) {
                int w = rect.getWidth();
                if (w < 0) {
                    w = 0;
                }
                int h = rect.getHeight();
                if (h < 0) {
                    h = 0;
                }
                rect.setHeight(h);
                rect.setWidth(w);
            } else {
                int w = rect.getWidth();
                if (w < 0) {
                    w = 0;
                }
                int h = rect.getHeight();
                if (h < 0) {
                    h = 0;
                }

                Insets insets = TreeMapRectangle.getInsets();
                calculatePositionsRec(rect.getX() + insets.left, rect.getY() + insets.top, w - insets.left - insets.right, h - insets.top - insets.bottom, node.getWeight(), node.getChildren());
            }

        }
    }

    private double normAspect(double big, double small, double a, double b) {
        double x = aspect(big, small, a, b);
        if (x < 1) {
            return 1 / x;
        }
        return x;
    }

}
