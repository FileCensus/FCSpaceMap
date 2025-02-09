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

package au.com.intermine.spacemap.action;

import java.io.File;

import au.com.intermine.spacemap.SpaceMap;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.model.TreeNodeTypeHarvester;
import au.com.intermine.spacemap.treemap.HiddenNodeSet;

/**
 * 
 * @author baird
 * 
 */
public class ToggleFreeSpaceAction extends SpaceMapNodeActionAdapter {

	private static HiddenNodeSet _hiddenNodes = new HiddenNodeSet();

	private static boolean _showFreeSpace = false;
	
	public ToggleFreeSpaceAction() {
		super("hide_free_space.png");
	}

	public String getLabel() {
		return (_showFreeSpace ? "Hide free space" : "Show Free Space");
	}

	public static boolean isShowingFreeSpace() {
		return _showFreeSpace;
	}

	public void performAction(TreeNode node, File file) {
		if (_showFreeSpace) {
			// We are already showing the free space nodes, so now we hide them...
			TreeNode model = SpaceMap.getInstance().getRootModel();
			TreeNodeTypeHarvester v = new TreeNodeTypeHarvester(NodeType.FreeSpace);
			model.traverseLeafNodes(v);
			for (TreeNode n : v.getNodes()) {
				_hiddenNodes.hideNode(n);
			}
		} else {
			_hiddenNodes.restoreHiddenNodes();
		}
		SpaceMap.getVisualisation().getWidget().repaint();

		_showFreeSpace = !_showFreeSpace;
	}

}
