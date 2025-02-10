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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import au.com.intermine.spacemap.IAsyncCallback;
import au.com.intermine.spacemap.model.NodeCounter;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.navigator.Navigator;
import au.com.intermine.spacemap.navigator.NavigatorItem;
import au.com.intermine.spacemap.navigator.NavigatorItemClickedListener;

public class Visualisation extends JPanel {

    private static final long serialVersionUID = 1L;

    private TreeMapWidget _widget;

    private JPanel _navpanel;

    private JPanel _buttonPanel;

    private Navigator _nav;

    public Visualisation() {
        this.setBorder(new EmptyBorder(2, 0, 0, 0));
        this.setLayout(new BorderLayout(0, 2));
        _widget = new TreeMapWidget();
        addListeners();
        this.add(_widget, BorderLayout.CENTER);
        _nav = new Navigator();
        _nav.addNavigatorItemClickedListener(new NavigatorItemClickedListener() {

            public void itemClicked(NavigatorItem item) {
                if (item instanceof TreeMapNavigatorItem) {
                    _widget.getZoom().zoomTo(((TreeMapNavigatorItem) item).getNode());
                }

            }
        });

        _navpanel = new JPanel(new BorderLayout(0, 0));
        _buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        _navpanel.add(_nav, BorderLayout.CENTER);
        _navpanel.add(_buttonPanel, BorderLayout.EAST);
        this.add(_navpanel, BorderLayout.NORTH);       
    }
    
    public void startAsyncRepaint(IAsyncCallback callback) {
    	try {
	    	_widget.prepareAsyncRender();
	    	callback.onComplete(null);
    	} catch (Exception ex) {
    		callback.onException(ex);
    	}    	
    }
    
    private void addListeners() {
        _widget.addVisualisationListener(new VisualisationListenerAdapter() {

            public void displayRootChanged(TreeNode newroot) {
                _nav.clear();
                Stack<TreeNode> s = new Stack<TreeNode>();
                TreeNode p = newroot;
                while (p != null) {
                    s.push(p);
                    p = p.getParent();
                }
                while (s.size() > 0) {
                    _nav.addItem(new TreeMapNavigatorItem(s.pop()));
                }
                _nav.repaint();
            }

        });        
    }
    
    public TreeMapWidget getWidget() {
        return _widget;
    }

    public void setModel(TreeNode model) {
        _widget.setModel(model);
        this.invalidate();        
        this.repaint();
    }

    public void setDisplayRoot(TreeNode node) {
        _widget.setDisplayedRoot(node);
    }

    public long countNodes() {
        NodeCounter c = new NodeCounter();
        _widget.traverseAllNodes(c);
        return c.getCount();
    }

    public void addButton(JComponent button) {
        _buttonPanel.add(button);
    }

    public void forceRepaint() {
        _widget.forceRepaint();
    }

}

