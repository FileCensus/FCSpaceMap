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

package au.com.intermine.spacemap.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import au.com.intermine.spacemap.model.ITreeNodeVisitor;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.model.filter.ITreeModelFilter;

public class DuplicateFilesFilterDescriptor implements IFilterDescriptor {

    public JComponent getArgumentsComponent() {
        return null;
    }

    public ITreeModelFilter getFilter() {
        return new DuplicateFileFilter();
    }

    public String toString() {
        return "Duplicate files";
    }

}

class DuplicateFileFilter implements ITreeModelFilter {
    
    private Map<String, List<TreeNode>> _hashes;
    private static Pattern DUPLICATE_PATTERN = Pattern.compile("(?i)^(Copy\\sof\\s)*(.*)[.].*$"); 
    
    public DuplicateFileFilter() {
        _hashes = new HashMap<String, List<TreeNode>>();
    }

    public boolean accept(TreeNode node) {
        String hash = computeHash(node);
        return _hashes.containsKey(hash);
    }

    public void begin(TreeNode root) {
        root.traverseLeafNodes(new ITreeNodeVisitor() {

            public void visit(TreeNode node) {
                String hash = computeHash(node);
                if (!_hashes.containsKey(hash)) {
                    List<TreeNode> l = new ArrayList<TreeNode>();
                    l.add(node);
                    _hashes.put(hash, l);
                } else {                
                    _hashes.get(hash).add(node);
                }
            }
        });
        
        List<String> purgeList = new ArrayList<String>();
        long cluster = 1;
        for (String key : _hashes.keySet()) {
            List<TreeNode> l = _hashes.get(key);
            if (l.size() < 2) {
                purgeList.add(key);
            } else {
                for (TreeNode node : l) {
                    node.setCluster(cluster);
                }
                cluster++;
            }
        }
        
        for (String key : purgeList) {
            _hashes.remove(key);
        }
    }
    
    private String computeHash(TreeNode node) {
        String name = node.getLabel().toLowerCase();
        Matcher m = DUPLICATE_PATTERN.matcher(node.getLabel());
        if (m.matches()) {
            name = m.group(2);
        }
        return String.format("%s?%d", name, node.getWeight());
    }

    public void end(TreeNode root) {
    }
    
}
