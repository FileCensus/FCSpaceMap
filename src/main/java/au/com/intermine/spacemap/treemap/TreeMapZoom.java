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

import java.awt.MouseInfo;
import java.awt.Point;

import au.com.intermine.spacemap.exception.SystemFatalException;
import au.com.intermine.spacemap.model.TreeNode;

public class TreeMapZoom {

    private TreeMapWidget _widget;

    public TreeMapZoom(TreeMapWidget widget) {
        _widget = widget;
    }

    public synchronized void zoomTowards(TreeNode dest) {
        TreeNode root = _widget.getDisplayedRoot();
        TreeNode p = dest;
        TreeNode last = p;
        while (p != root && p != null) {
            last = p;
            p = p.getParent();
        }
        if (p == null) {
            p = _widget.getRootNode();
        }
        if (last != null && last.getChildren() != null && last.getChildren().size() > 0) {
            zoomTo(last);
        } else {
            zoomTo(p);
        }
    }
    
    public synchronized void zoomTo(TreeNode dest) {
        _widget.setActiveLeaf(null);
        onZoomTo(dest);
        _widget.setDisplayedRoot(dest);
        _widget.repaint();
        Point p = MouseInfo.getPointerInfo().getLocation();
        TreeNode newactive = _widget.getNodeAt(p.x, p.y);
        if (newactive != null) {
            if (newactive.isLeaf()) {
                _widget.setActiveLeaf(newactive);
            } else {
                _widget.setActiveLeaf(null);
                _widget.setActiveBranch(newactive);
            }
        }
    }

    public synchronized void unzoom() {
        if (_widget.getDisplayedRoot().getParent() != null) {
            zoomTo(_widget.getDisplayedRoot().getParent());
        }
    }

    public synchronized void unzoomToRoot() {
        zoomTo(_widget.getRootNode());
    }

    protected TreeMapWidget getWidget() {
        return _widget;
    }

    protected void onZoomTo(TreeNode node) {
        TreeMapRectangle dest = (TreeMapRectangle) node.getRectangle();
        try {
            TreeMapRectangle rect = (TreeMapRectangle) getWidget().getRootNode().getRectangle();
            dest.setX(rect.getX());
            dest.setY(rect.getY());
            dest.setHeight(rect.getHeight());
            dest.setWidth(rect.getWidth());
        } catch (Exception ex) {
        	new SystemFatalException(ex);
        }
    	
    }

}
