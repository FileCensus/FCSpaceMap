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

package au.com.intermine.spacemap;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import au.com.intermine.spacemap.model.ITreeNodeVisitor;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.model.filter.ITreeModelFilter;
import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.Utils;

public class SpaceMapHelper {

    public static void launchDefaultApp(File f) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(f);
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    public static TreeNode createFilteredModel(TreeNode rootmodel, ITreeModelFilter filter) {

        filter.begin(rootmodel);

        FilterVisitor v = new FilterVisitor(filter);
        rootmodel.traverseLeafNodes(v);
        filter.end(rootmodel);

        // We need to create new nodes for each matching node, plus new nodes for the parent nodes...
        TreeNode newroot = null;

        Map<String, TreeNode> nodecache = new HashMap<String, TreeNode>();

        List<TreeNode> matching = v.getMatchingNodes();

        for (TreeNode node : matching) {
            String key = node.getAncestryAsString();
            if (!nodecache.containsKey(key)) {
                TreeNode newnode = new TreeNode(node.getLabel(), node.getWeight(), node.getNodeType(), node.getCluster());
                nodecache.put(key, newnode);
                // Now check all of my ancestors...
                TreeNode parent = node.getParent();
                TreeNode newchild = newnode;
                boolean found = false;
                while (parent != null && !found) {
                    String parentkey = parent.getAncestryAsString();
                    TreeNode newparent = null;
                    if (nodecache.containsKey(parentkey)) {
                        newparent = nodecache.get(parentkey);
                        found = true;
                    } else {
                        newparent = new TreeNode(parent.getLabel(), 0, parent.getNodeType(), parent.getCluster());
                        nodecache.put(parentkey, newparent);
                        if (parent.getParent() == null && newroot == null) {
                            newroot = newparent;
                        }
                    }
                    newparent.addChild(newchild);
                    newchild = newparent;
                    parent = parent.getParent();
                }
            }
        }
        return newroot;
    }

    public static TreeNode findNode(TreeNode model, String nodepath) {

        TreeNode node = model;

        if (model != null) {
            ArrayList<String> bits = new ArrayList<String>(Arrays.asList(nodepath.split("[/]")));
            if (bits.size() > 0) {
                // The first bit should match the root label
                if (bits.get(0).equals("") && !node.getLabel().equals("")) {
                    bits.remove(0);
                }
                if (bits.get(0).equals(node.getLabel())) {
                    bits.remove(0);
                }

                for (String bit : bits) {
                    TreeNode child = node.getChildNode(bit);
                    if (child == null) {
                        return node;
                    } else {
                        node = child;
                    }
                }
            }
        }
        return node;
    }

    /**
     * TreeNode visitor that accumulates all the tree nodes that are accepted by the specified filter
     * 
     * @author baird
     */
    static class FilterVisitor implements ITreeNodeVisitor {

        private ITreeModelFilter _filter;

        private List<TreeNode> _matching = new ArrayList<TreeNode>();

        public FilterVisitor(ITreeModelFilter filter) {
            _filter = filter;
        }

        public void visit(TreeNode node) {
            if (_filter == null || _filter.accept(node)) {
                _matching.add(node);
            }
        }

        public List<TreeNode> getMatchingNodes() {
            return _matching;
        }

    }

    public static ImageIcon getNodeIcon(TreeNode node) {
        if (node.getNodeType() == NodeType.File) {
            File f = Utils.getFileFromTreeNode(node);
            if (f != null) {
                Icon ico = FileSystemView.getFileSystemView().getSystemIcon(f);
                if (ico != null) {
                    return DrawingUtils.iconToImageIcon(ico);
                }
            }
        }
        return null;
    }

}
