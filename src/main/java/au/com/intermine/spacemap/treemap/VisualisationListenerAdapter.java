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

import java.awt.event.MouseEvent;

import au.com.intermine.spacemap.model.TreeNode;


public class VisualisationListenerAdapter implements VisualisationListener {

    public void displayRootChanged(TreeNode newroot) {
    }

    public void mouseOverNode(TreeNode node) {
    }

    public void requireMoreData(TreeNode parentnode) {
    }

    public void nodeClicked(MouseEvent e, TreeNode node) {
    }

    public void mouseExited(MouseEvent node) {
    }

    public void selectionChanged(TreeNode activenode) {
    }

	public void mouseDown(MouseEvent e, TreeNode node) {
	}

	public void mouseUp(MouseEvent e, TreeNode node) {
	}

    public void mouseDragged(MouseEvent e) {
    }

}
