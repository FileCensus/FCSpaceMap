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

import au.com.intermine.spacemap.model.TreeNode;

/**
 * Abstract class with the methods who attribute color to the elements of Visualisation.
 */
public abstract class ColorProvider {

    /**
     * get the associated color to the value.
     *
     * @param item
     *            item
     * @return the associated color to the value
     */
    public abstract Color getColor(TreeNode item);

}
