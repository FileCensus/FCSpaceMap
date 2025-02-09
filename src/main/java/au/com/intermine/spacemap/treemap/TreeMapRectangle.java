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

import java.awt.Insets;
import java.awt.Point;

import au.com.intermine.spacemap.model.TreeNode;


/**
 * @author djb
 */
public class TreeMapRectangle {

    private static final long serialVersionUID = 742372833853976103L;

    public static Insets _insets = new Insets(17, 3, 3, 3);

    private TreeNode _node;

    private int _height;

    private int _width;

    private int _x;

    private int _y;

    public TreeMapRectangle(TreeNode node) {
        _node = node;
    }

    public static Insets getInsets() {
        return _insets;
    }

    public TreeNode getNodeAt(int x, int y) {
        if (_node.isLeaf()) {
            if ((x >= _x) && (x <= _x + _width) && (y >= _y) && (y <= _y + _height)) {
                return _node;
            }
        } else {
            if ((x >= _x) && (x <= _x + _width) && (y >= _y) && (y <= _y + _height)) {
                // I am a candidate, but one of my child branches, may be the actual active node...
            	for (int i = 0; i < _node.getChildren().size(); ++i) {
            		TreeNode node = _node.getChildren().get(i);
                    TreeMapRectangle child = node.getRectangle();
                    TreeNode candidate = child.getNodeAt(x, y);
                    if (candidate != null) {
                        return candidate;
                    }
                }
                // None of child branches matched, so I'm it!
                return _node;
            }
        }
        // I don't match...
        return null;
    }

    public int getHeight() {
        return _height;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
    }

    public int getX() {
        return _x;
    }

    public void setX(int x) {
        _x = x;
    }

    public int getY() {
        return _y;
    }

    public void setY(int y) {
        _y = y;
    }

    public void setDimension(int x, int y, int width, int height) {
        _x = x;
        _y = y;
        _width = width;
        _height = height;
    }

    @Override
    public String toString() {
        return String.format("X=%d, Y=%d, Width=%d, Height=%d", _x, _y, _width, _height);
    }
    
    public boolean contains(Point p) {
    	if (p.x >= _x && p.x <= _x + _width) {
    		if (p.y >= _y && p.y <= _y + _height) {
    			return true;
    		}
    	}
    	return false;
    }

}
