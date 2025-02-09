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

import java.util.List;

import au.com.intermine.spacemap.model.TreeNode;

public class SplitByNumber extends SplitStrategy {

    @Override
    public void splitElements(List<TreeNode> v, List<TreeNode> v1, List<TreeNode> v2) {
        int size = v.size();
        int middle = size / 2;
        int index = 0;
        // we add first elements to v1
        for (; index < middle; index++) {
            v1.add(v.get(index));
        }
        // we add last elements to v2
        for (; index < size; index++) {
            v2.add(v.get(index));
        }
    }

    @Override
    public long sumWeight(List<TreeNode> v) {
        // all the elements must have the same weight
        int weight = 0;
        for (TreeNode node : v) {
            if (node.isLeaf()) {
                weight += 1;
            } else {
                weight += this.sumWeight(node.getChildren());
            }
        }
        return weight;
    }

}
