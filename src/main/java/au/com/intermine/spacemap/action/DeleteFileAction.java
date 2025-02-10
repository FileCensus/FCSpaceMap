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
package au.com.intermine.spacemap.action;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.com.intermine.spacemap.ExceptionPublisher;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.util.Utils;

public class DeleteFileAction extends SpaceMapNodeActionAdapter {
	
	public DeleteFileAction() {
		super("delete.png");
	}

	public String getLabel() {
		return "Delete";
	}

	private boolean deleteDir(TreeNode node, List<TreeNode> failures) {
		File f = Utils.getFileFromTreeNode(node);
		if (f.isDirectory()) {
			List<TreeNode> children = node.getChildren();
			for (TreeNode child : children) {
				deleteDir(child, failures);
			}
		}

		// The directory is now empty so delete it
		if (f.delete()) {
			return true;
		} else {
			failures.add(node);
			return false;
		}
	}

	public void performAction(TreeNode node, File file) {
		ArrayList<TreeNode> failures = new ArrayList<TreeNode>();
		if (file.exists()) {
			try {
				if (Utils.areYouSure("Are you sure you wish to delete '%s'", file.getAbsolutePath())) {
					deleteDir(node, failures);
					new ResolveNodeAction().performAction(node.getParent(), Utils.getFileFromTreeNode(node.getParent()));
					if (failures.size() > 0) {
						StringBuilder str = new StringBuilder("Failed to delete:");
						for (TreeNode n : failures) {
							str.append(Character.LINE_SEPARATOR).append(Utils.getFileFromTreeNode(n).getAbsolutePath());
						}
						throw new RuntimeException(str.toString());
					}
				}
			} catch (Exception ex) {
				ExceptionPublisher.publish(ex);
			}
		}
	}

}
