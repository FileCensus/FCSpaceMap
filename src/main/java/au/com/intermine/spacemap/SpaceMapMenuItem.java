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

import java.io.File;

import javax.swing.ImageIcon;

import au.com.intermine.spacemap.action.ISpaceMapNodeAction;
import au.com.intermine.spacemap.jobqueue.RunnableActionAdapter;
import au.com.intermine.spacemap.jobqueue.JobQueue;
import au.com.intermine.spacemap.model.TreeNode;

public class SpaceMapMenuItem {

    private static final long serialVersionUID = 1L;

    private ISpaceMapNodeAction _action;

    private TreeNode _node;

    private File _file;

    public SpaceMapMenuItem(final ISpaceMapNodeAction action, final TreeNode node, final File file) {
        _action = action;
        _node = node;
        _file = file;
    }

    public void performAction() {
        JobQueue.pushGlobalJob(new RunnableActionAdapter(_action, _node, _file));
    }
    
    public String getText() {
        return _action.getLabel();
    }
    
    public ISpaceMapNodeAction getAction() {
    	return _action;
    }
    
    public File getFile() {
    	return _file;
    }
    
    public TreeNode getNode() {
    	return _node;
    }
    
    public ImageIcon getIcon() {
        return _action.getIcon();
    }

}
