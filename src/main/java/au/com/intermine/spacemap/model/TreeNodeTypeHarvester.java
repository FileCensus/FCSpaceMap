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

package au.com.intermine.spacemap.model;

import java.util.ArrayList;
import java.util.List;

public class TreeNodeTypeHarvester implements ITreeNodeVisitor {
	
	/** the node types to collect */
	private NodeType _nodeTypes[];
	
	/** holds the list of collected (harvested) nodes */
	private List<TreeNode> _nodes;
	
	/** 
	 * @param nodeTypes
	 */
	public TreeNodeTypeHarvester(NodeType ...nodeTypes) {
		_nodeTypes = nodeTypes;
		_nodes = new ArrayList<TreeNode>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(TreeNode node) {
		for (NodeType t : _nodeTypes) {
			if (node.getNodeType() == t) {
				_nodes.add(node);
				break;
			}
		}
	}

	/**
	 * @return the list of harvested nodes (nodes whose types were specified in the ctor)
	 */
	public List<TreeNode> getNodes() {
		return _nodes;
	}

}
