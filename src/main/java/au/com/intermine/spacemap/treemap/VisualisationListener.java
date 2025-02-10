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

import java.awt.event.MouseEvent;

import au.com.intermine.spacemap.model.TreeNode;

/**
 * @author djb
 */
public interface VisualisationListener {

    void displayRootChanged(TreeNode newroot);

    void requireMoreData(TreeNode parentnode);

    void mouseOverNode(TreeNode node);

    void mouseExited(MouseEvent node);

    void nodeClicked(MouseEvent e, TreeNode node);

    void selectionChanged(TreeNode activenode);
    
    void mouseDown(MouseEvent e, TreeNode node);
    
    void mouseUp(MouseEvent e, TreeNode node);

    void mouseDragged(MouseEvent e);

}
