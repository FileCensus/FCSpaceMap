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
import javax.swing.JButton;

import au.com.intermine.spacemap.SpaceMap;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.HiddenNodeSet;

public class HideNodeAction extends SpaceMapNodeActionAdapter {

	private static HiddenNodeSet _hiddenNodes = new HiddenNodeSet();
	private static JButton _unhideButton;

	public String getLabel() {
		return "Hide Node";
	}

	public void performAction(TreeNode node, File file) {
		_hiddenNodes.hideNode(node);
		if (_unhideButton != null) {
			_unhideButton.setEnabled(true);
		}
		SpaceMap.getVisualisation().getWidget().repaint();
	}

	public static void restoreHiddenNodes() {
		_hiddenNodes.restoreHiddenNodes();
		if (_unhideButton != null) {
			_unhideButton.setEnabled(false);
		}
		SpaceMap.getVisualisation().getWidget().repaint();
	}

	public static void setUnhideButton(JButton button) {
		_unhideButton = button;
	}

	/**
	 * This method will go through each node in the hidden list, and if a hidden node used to belong to the supplied root node (i.e. its ancestry includes the root nodes ancestry), then it is removed from the hidden list.
	 * 
	 * @param rootnode
	 */
	public static void purgeHiddenNodes(TreeNode rootnode) {
		_hiddenNodes.purgeHiddenNodes(rootnode);
		if (_unhideButton != null) {
			_unhideButton.setEnabled(!_hiddenNodes.getHiddenNodes().isEmpty());
		}
	}

	public static void reset() {
		_hiddenNodes.reset();
	}

}
