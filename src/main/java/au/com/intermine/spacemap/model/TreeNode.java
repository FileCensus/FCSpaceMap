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
import java.util.NoSuchElementException;
import java.util.Stack;

import au.com.intermine.spacemap.model.visitor.FindClusteredNodesVisitor;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;

public class TreeNode {

	public static final int MODE_FILE = 0;

	public static final int MODE_FOLDER = 1;

	public static final int MODE_UNRESOLVED = 2;

	private long _weight = 0;
	
	private long _cluster = -1;

	private String _label;

	private NodeType _type;

	private List<TreeNode> _children;

	private TreeNode _parent;

	private TreeMapRectangle _region;

	private boolean _selected;

	private long _leafNodeCount; // total leafnode count

	private Object _baggage;

	public TreeNode(String label, long weight, NodeType nodetype, long cluster) {
		_label = label;
		_weight = weight;
		_children = new ArrayList<TreeNode>();
		_parent = null;
		_type = nodetype;
		_region = new TreeMapRectangle(this);
		_cluster = cluster;
	}
	
	public long getCluster() {
	    return _cluster;
	}
	
	public void setCluster(long cluster) {
	    _cluster = cluster;
	}

	public boolean isChildOf(TreeNode node) {
		TreeNode parent = _parent;

		while (parent != null) {
			if (parent == node) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBaggage() {
		return (T) _baggage;
	}

	public void setBaggage(Object baggage) {
		_baggage = baggage;
	}

	public long getWeight() {
		return _weight;
	}

	public void setWeight(long weight) {
		_weight = weight;
	}

	public TreeNode getParent() {
		return _parent;
	}

	public NodeType getNodeType() {
		return _type;
	}

	public void setNodeType(NodeType nodetype) {
		_type = nodetype;
	}

	public String getLabel() {
		return _label;
	}

	public List<TreeNode> getChildren() {
		return _children;
	}

	public TreeNode firstChild() {
		if (_children != null && _children.size() > 0) {
			return _children.get(0);
		}
		return null;
	}

	public TreeMapRectangle getRectangle() {
		return _region;
	}

	public void setParent(TreeNode parent) {
		_parent = parent;
	}
	
	public TreeNode getRootParent() {
	    TreeNode n = this;
	    while (n.getParent() != null) {
	        n = n.getParent();
	    }
	    return n;
	}

	public List<String> getAncestry() {
		Stack<String> s = new Stack<String>();
		TreeNode n = this;
		while (n != null && n.getNodeType() != NodeType.Container) {
			s.add(n.getLabel());
			n = (TreeNode) n.getParent();
		}

		List<String> ret = new ArrayList<String>();
		while (s.size() > 0) {
			ret.add(s.pop());
		}

		return ret;
	}

	public void replaceChild(TreeNode oldnode, TreeNode newnode) {
		if (_children.contains(oldnode)) {
			removeChild(oldnode);
			addChild(newnode);
		}
	}

	public int getMaxDepth() {
		if (_children.size() == 0) {
			return 1;
		} else {
			int maxdepth = 0;
			for (TreeNode n : _children) {
				int depth = n.getMaxDepth();
				if (depth > maxdepth) {
					maxdepth = depth;
				}
			}
			return 1 + maxdepth;
		}
	}

	public int getLevel() {
		TreeNode p = getParent();
		int level = 0;
		while (p != null) {
			p = p.getParent();
			level++;
		}
		return level;
	}

	public long getSumChildWeight() {
		long w = 0;
		for (TreeNode n : _children) {
			w += n.getWeight();
		}
		return w;
	}

	public boolean isLeaf() {
		return _children.size() == 0;
	}

	public long countAllChildNodes() {
		NodeCounter v = new NodeCounter();
		traverse(v);
		return v.getCount();
	}

	public void traverseLeafNodes(ITreeNodeVisitor visitor) {
		if (isLeaf()) {
			visitor.visit(this);
		} else {
			for (TreeNode child : _children) {
				if (child.isLeaf()) {
					visitor.visit(child);
				} else {
					child.traverseLeafNodes(visitor);
				}
			}
		}
	}

	public boolean hasChildWithNodeType(NodeType nodetype) {
		for (TreeNode child : _children) {
			if (child.getNodeType() == nodetype) {
				return true;
			}
		}
		return false;
	}

	public void traverse(ITreeNodeVisitor visitor) {
		visitor.visit(this);
		synchronized (_children) {
			int size = _children.size();
			for (int i = 0; i < size; ++i) {
				TreeNode child = _children.get(i);
				if (child != null) {
					child.traverse(visitor);
				}
			}
		}
	}

	public void setLabel(String label) {
		_label = label;
	}

	public String getFullPath(String delimiter) {
		if (_parent == null) {
			return "";
		} else {
			return _parent.getFullPath(delimiter) + delimiter + _label;
		}
	}

	public TreeNode getChildNode(String label) {
		for (TreeNode n : _children) {
			if (n.getLabel().equalsIgnoreCase(label)) {
				return n;
			}
		}
		return null;
	}

	public boolean hasChildNode(String label) {
		return getChildNode(label) != null;
	}

	public TreeNode removeChild(TreeNode child) {
		synchronized (_children) {
			if (_children.contains(child)) {
				_children.remove(child);
				long weight = child.getWeight();
				subtractWeight(weight);
				_leafNodeCount -= child.getLeafNodeCount();
				TreeNode parent = getParent();
				while (parent != null) {
					parent.subtractWeight(weight);
					parent._leafNodeCount -= child.getLeafNodeCount();
					parent = parent.getParent();
				}
			}
		}

		return child;
	}

	public TreeNode addChild(TreeNode child) {
		synchronized (_children) {
			_children.add(child);
			child.setParent(this);
			long weight = child.getWeight();
			NodeType nodetype = child.getNodeType();
			addWeight(weight);
			if (nodetype == NodeType.File) {
				_leafNodeCount++;
			} else {
				_leafNodeCount += child.getLeafNodeCount();
			}
			TreeNode parent = getParent();
			while (parent != null) {
				parent.addWeight(weight);
				if (nodetype == NodeType.File) {
					parent._leafNodeCount++;
				} else {
					parent._leafNodeCount += child.getLeafNodeCount();
				}
				parent = parent.getParent();
			}
		}
		return child;
	}

	public TreeNode addChild(String label, long weight, NodeType mode, long cluster) {
		TreeNode child = new TreeNode(label, weight, mode, cluster);
		addChild(child);
		return child;
	}

	public long getLeafNodeCount() {
		return _leafNodeCount;
	}

	public boolean isSelected() {
		return _selected;
	}

	public void setSelected(boolean selected) {
		_selected = selected;
	}

	public boolean toggleSelected() {
		_selected = !_selected;
		return _selected;
	}

	public void addWeight(long weight) {
		_weight += weight;
	}

	private void subtractWeight(long weight) {
		_weight -= weight;
	}

	@Override
	public String toString() {
		return String.format("%s %s", _label, _type.toString());
	}

	public TreeNode ensurePathExists(String[] bits) {
		TreeNode pNode = this;
		if (bits[0].equals(getLabel())) {
			for (int i = 1; i < bits.length; ++i) {
				TreeNode child = pNode.getChildNode(bits[i]);
				if (child == null) {
					pNode = pNode.addChild(bits[i], 0, NodeType.Folder, -1);
				} else {
					pNode = child;
				}
			}
		} else {
			throw new RuntimeException("Root path element does not match root node label!");
		}
		return pNode;
	}

	public String getAncestryAsString() {
		return getAncestryAsString("/");
	}

	public String getAncestryAsString(final String delim) {
		List<String> a = getAncestry();
		StringBuilder sb = new StringBuilder();
		for (String s : a) {
			sb.append(delim).append(s);
		}
		return sb.toString();
	}
	
	public NodeSetIterator getLeafNodeIterator() {
		return new LeafNodeIterator(this);
	}
	
    public NodeSetIterator getClusterIterator() {
        return new NodeClusterIterator(this);
    }
    
    /**
     * 
     * @author baird
     *
     */
    public abstract class NodeSetIterator {
        private List<TreeNode> _items;
        private int _index = -1;
                
        protected void setItems(List<TreeNode> items) {
            _items = items;
        }
        
        public boolean hasNext() {
            return (_index == -1 ? _items.size() > 0 : _index < _items.size()-1);
        }
        
        public boolean hasPrevious() {
            return _index > 0;
        }
        
        public TreeNode next() {
            _index++;
            
            if (_index < -1) {
                _index = -1;
            }
            
            if (_index >= _items.size()) {
                _index = _items.size() - 1;
                throw new NoSuchElementException();
            }

            return _items.get(_index);
        }
        
        public TreeNode current() {
            return _items.get(_index);
        }

        public TreeNode previous() {
            _index--;
            if (_index < 0) {
                _index = 0;
                throw new NoSuchElementException();
            }
            return _items.get(_index);
        }
        
        public int getCurrentPosition() {
            return _index + 1;
        }
        
        public int getSize() {
            return _items.size();
        }

        public void reset() {
            _index = -1;
        }

        public void goTo(int i) {
            _index = i;
        }
        
        
    }

	
	/** Simple implementation - maybe could be smarter/more efficient? */
	public class LeafNodeIterator extends NodeSetIterator {
		
		private LeafNodeIterator(TreeNode node) {
            TreeNodeTypeHarvester v = new TreeNodeTypeHarvester(NodeType.File);
            node.traverseLeafNodes(v);		    
			setItems(v.getNodes());
		}
		
	}
	
	public class NodeClusterIterator extends NodeSetIterator {
	    private NodeClusterIterator(TreeNode node) {
	        FindClusteredNodesVisitor v = new FindClusteredNodesVisitor(node.getCluster());
	        node.getRootParent().traverseLeafNodes(v);
	        setItems(v.getClusterNodes());
	    }
	}
	
	

	
}
