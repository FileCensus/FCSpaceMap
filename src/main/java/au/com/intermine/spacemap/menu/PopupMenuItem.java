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

package au.com.intermine.spacemap.menu;

import au.com.intermine.spacemap.SpaceMapMenuItem;
import au.com.intermine.spacemap.action.SpaceMapNodeActionAdapter;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

class PopupMenuItem extends SpaceMapMenuItem {

	public PopupMenuItem(final MenuGlassPane menu, final List<SpaceMapMenuItem> items, final TreeNode node) {
		super(new SpaceMapNodeActionAdapter() {

			public String getLabel() {
				return "More...";
			}

			public void performAction(TreeNode node, File file) {
				JPopupMenu jmenu = new JPopupMenu();

				for (SpaceMapMenuItem item : items) {
					jmenu.add(new SpaceMapJMenuItemAdapter(item, menu));
				}

				jmenu.setInvoker(menu);
				Point p = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(p, menu);

				jmenu.show(menu, p.x, p.y);
			}

		}, node, Utils.getFileFromTreeNode(node));

	}

}
