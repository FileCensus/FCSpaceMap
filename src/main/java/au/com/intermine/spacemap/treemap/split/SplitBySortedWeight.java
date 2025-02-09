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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.intermine.spacemap.model.TreeNode;

/**
 * Strategy who split the elements in 2 groups of equivalent weight.
 * <p>
 * The elements are first sorted by descending weight. Then they are splitted in 2 groups of equivalent weight.
 * <p>
 * The heaviest elements are on the top left of the Visualisation. The lightest elements are on the bottom right of the Visualisation
 */

public class SplitBySortedWeight extends SplitStrategy {

    @Override
    public void splitElements(List<TreeNode> v, List<TreeNode> v1, List<TreeNode> v2) {
        List<TreeNode> vClone = new ArrayList<TreeNode>(v);
        double memWeight = 0.0;
        double sumWeight = sumWeight(v);
        double elemWeight = 0.0;

        sortVector(vClone);

        for (Iterator<TreeNode> i = vClone.iterator(); i.hasNext();) {
            TreeNode tmn = i.next();
            elemWeight = tmn.getWeight();
            // if adding the current element pass the middle of total weight
            if (memWeight + elemWeight >= sumWeight / 2) {
                // we look at the finest split (the nearest of the middle of weight)
                if (((sumWeight / 2) - memWeight) > ((memWeight + elemWeight) - (sumWeight / 2))) {
                    // if it is after the add, we add the element to the first Vector
                    memWeight += elemWeight;
                    v1.add(tmn);
                } else {
                    // we must have at least 1 element in the first vector
                    if (v1.isEmpty()) {
                        v1.add(tmn);
                    } else {
                        // if it is before the add, we add the element to the second Vector
                        v2.add(tmn);
                    }
                }
                // then we fill the second Vector qith the rest of elements
                while (i.hasNext()) {
                    tmn = i.next();
                    v2.add(tmn);
                }
            } else {
                // we add in the first vector while we don't reach the middle of weight
                memWeight += elemWeight;
                v1.add(tmn);
            }
        }
    }

}
