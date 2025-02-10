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
import au.com.intermine.spacemap.imageviewer.ImageViewer;
import au.com.intermine.spacemap.model.TreeNode;

public class ShowImageViewerAction extends SpaceMapNodeActionAdapter {
	
	public ShowImageViewerAction() {
		super("image_viewer.png");
	}

	public String getLabel() {
		return "Image viewer";
	}

	public void performAction(TreeNode node, File file) {
		ImageViewer v = new ImageViewer(node, file);
		SpaceMap.getInstance().setGlassPane(v);
		v.open();
	}

}
