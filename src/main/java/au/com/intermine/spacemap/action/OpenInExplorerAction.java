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

import au.com.intermine.spacemap.exception.SystemFatalException;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.util.Utils;

public class OpenInExplorerAction extends SpaceMapNodeActionAdapter {
	
	public OpenInExplorerAction() {
		super("show_in_explorer.png");
	}

    public String getLabel() {
        return "Open in Windows Explorer";
    }

    public void performAction(TreeNode node, File file) {
        if (file.exists()) {
            openInExplorer(file);
        } else {
            if (node.getParent() != null) {
                File parentfile = Utils.getFileFromTreeNode(node.getParent());
                if (parentfile != null) {
                    openInExplorer(parentfile);
                }
            }
        }
    }
    
    public static void openInExplorer(File f) {
        try {
            if (f.exists()) {
                Runtime.getRuntime().exec("explorer.exe /select," + f.getAbsolutePath());
            }
        } catch (Exception ex) {
            throw new SystemFatalException(ex);
        }        
    }

}
