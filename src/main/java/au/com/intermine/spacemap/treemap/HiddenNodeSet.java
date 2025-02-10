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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.intermine.spacemap.model.TreeNode;

public class HiddenNodeSet {

	private Map<TreeNode, TreeNode> _hiddenNodes = new HashMap<TreeNode, TreeNode>();

	public void hideNode(TreeNode node) {
		synchronized (_hiddenNodes) {
			_hiddenNodes.put(node, node.getParent());
			node.getParent().removeChild(node);
		}
	}

	public void restoreHiddenNodes() {
		synchronized (_hiddenNodes) {
			for (TreeNode node : _hiddenNodes.keySet()) {
				TreeNode parent = _hiddenNodes.get(node);
				parent.addChild(node);
			}
			_hiddenNodes.clear();
		}
	}

	public Set<TreeNode> getHiddenNodes() {
		return _hiddenNodes.keySet();
	}

	/**
	 * This method will go through each node in the hidden list, and if a hidden node used to belong to the supplied root node (i.e. its ancestry includes the root nodes ancestry), then it is removed from the hidden list.
	 * 
	 * @param rootnode
	 */
	public void purgeHiddenNodes(TreeNode rootnode) {
		synchronized (_hiddenNodes) {
			// Need to keep track of which nodes to delete separately to avoid concurrent modification exceptions
			List<TreeNode> purgelist = new ArrayList<TreeNode>();
			// Ancestry is just the labels of ancestor nodes in an ordered list (ancestor->descendant)
			List<String> rootancestry = rootnode.getAncestry();
			// For every 'hidden' node...
			for (TreeNode node : _hiddenNodes.keySet()) {
				List<String> ancestry = node.getAncestry();
				// now compare the ancestry lists - the ancestry should include the root ancestry
				// System.err.println("comparing " + rootancestry.toString() + " to " + ancestry.toString());
				if (ancestry.size() >= rootancestry.size()) {
					boolean shareancestry = true;
					for (int i = 0; i < rootancestry.size(); ++i) {
						if (!ancestry.get(i).equals(rootancestry.get(i))) {
							shareancestry = false;
							break;
						}
					}
					if (shareancestry) {
						purgelist.add(node);
					}
				}
			}

			for (TreeNode node : purgelist) {
				_hiddenNodes.remove(node);
			}
		}
	}

	public void reset() {
		_hiddenNodes.clear();
	}

}
