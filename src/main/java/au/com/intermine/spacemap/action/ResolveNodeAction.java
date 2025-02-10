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

import au.com.intermine.spacemap.SpaceMap;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.scanner.ScanTarget;

public class ResolveNodeAction extends SpaceMapNodeActionAdapter {
	
	public ResolveNodeAction() {
		super("refresh.png");
	}

	public String getLabel() {
		return "Refresh";
	}

	public void performAction(TreeNode node, File file) {
		if (file.isDirectory() && node.getParent() != null) {
			ScanTarget target = new ScanTarget(file.getAbsolutePath(), file.getAbsolutePath());
			TreeNode newnode = SpaceMap.getInstance().startScan(target, SpaceMap.getScanningEngine().getFilter());
			newnode.setLabel(node.getLabel());
			
			// Now we need to see if any of the 'old' child nodes are in the 'hidden list' - these nodes are now
			// orphans, and should be discarded. We have to do this before we do the replace to keep the ancestry intact.
			HideNodeAction.purgeHiddenNodes(node);
			
			node.getParent().replaceChild(node, newnode);
			if (SpaceMap.getVisualisation().getWidget().getDisplayedRoot() == node) {
				SpaceMap.getVisualisation().getWidget().setDisplayedRoot(newnode);
			}
		}
	}

}
