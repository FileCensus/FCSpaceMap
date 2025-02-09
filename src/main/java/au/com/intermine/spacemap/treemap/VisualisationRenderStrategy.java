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

package au.com.intermine.spacemap.treemap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;

import javax.swing.ImageIcon;

import au.com.intermine.spacemap.model.TreeNode;



public interface VisualisationRenderStrategy {

    /** Appears at the top left hand side of a branch
     *
     * @param node
     * @return
     */
    String renderBranchTitle(TreeNode node, boolean isdisplayroot);

    /** Appears at the top right hand side of a branch
     *
     * @param node
     * @return
     */
    String renderBranchSummary(TreeNode node);

    /**
     *  Returns the text for a given leaf node. Can return multiple lines.
     *
     * @param node
     * @return
     */
    String renderLeafNode(TreeNode node);

    /** the paint style for the given node */
    Paint getNodePaint(TreeNode node);

    /**
     * Return an Icon for the given node. NOT USED YET
     * @param node
     * @return
     */
    ImageIcon getNodeIcon(TreeNode node);

    Color getLeafNodeTextColor(TreeNode node);

    Color getBranchTitleTextColor(TreeNode node);

    Color getBranchSummaryTextColor(TreeNode node);

    Font getFont();
    
}
