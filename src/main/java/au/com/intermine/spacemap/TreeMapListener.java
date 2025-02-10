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

package au.com.intermine.spacemap;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;

import au.com.intermine.spacemap.action.DeleteFileAction;
import au.com.intermine.spacemap.action.HideNodeAction;
import au.com.intermine.spacemap.action.OpenFileAction;
import au.com.intermine.spacemap.action.OpenInExplorerAction;
import au.com.intermine.spacemap.action.ResolveNodeAction;
import au.com.intermine.spacemap.action.RhinoNodeAction;
import au.com.intermine.spacemap.action.ShowImageViewerAction;
import au.com.intermine.spacemap.action.ShowPropertiesAction;
import au.com.intermine.spacemap.action.ToggleFreeSpaceAction;
import au.com.intermine.spacemap.menu.MenuGlassPane;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.VisualisationListenerAdapter;
import au.com.intermine.spacemap.util.NumberRenderer;
import au.com.intermine.spacemap.util.NumberType;
import au.com.intermine.spacemap.util.RhinoScript;
import au.com.intermine.spacemap.util.Utils;

public class TreeMapListener extends VisualisationListenerAdapter {

	private static NumberRenderer _NumberRender = new NumberRenderer(NumberType.Bytes, 1024);

	private MenuGlassPane _menu;

	public TreeMapListener() {
	}

	@Override
	public void mouseOverNode(TreeNode node) {
		File f = Utils.getFileFromTreeNode(node);
		Date lastmodified = new Date(f.lastModified());

		String caption = String.format("<html>%s <b>%s</b> %s</html>", f.getAbsolutePath(), _NumberRender.render(node.getWeight()), lastmodified);

		SpaceMap.statusMsg(caption);
	}

	@Override
	public void mouseDown(final MouseEvent e, final TreeNode node) {

		if (e.getButton() == 3) {
			if (node != null) {
				File file = Utils.getFileFromTreeNode(node);
				List<SpaceMapMenuItem> items = getDefaultMenus(node, file);
				getCustomMenus(items, node, file);
				_menu = new MenuGlassPane(node, items);
				SpaceMap.getInstance().setGlassPane(_menu);
				_menu.openMenu();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (_menu != null) {
			_menu.dispatchEvent(SwingUtilities.convertMouseEvent((Component) e.getSource(), e, _menu));
		}
		super.mouseDragged(e);
	}

	@Override
	public void mouseUp(MouseEvent e, TreeNode node) {
		if (e.getButton() == 3) {
			if (_menu != null && _menu.isVisible()) {
				SpaceMapMenuItem item = _menu.getSelectedMenuItem();
				if (item != null) {
					_menu.closeMenu();
					item.performAction();
				}
			}
		}
	}

	@Override
	public void nodeClicked(MouseEvent e, final TreeNode node) {

		File file = Utils.getFileFromTreeNode(node);

		if (e.getClickCount() == 2 && e.getButton() == 1) {
			if (node.getNodeType() == NodeType.File || node.getNodeType() == NodeType.Folder) {
				SpaceMapHelper.launchDefaultApp(file);
			}
			return;
		}

	}

	private List<SpaceMapMenuItem> getCustomMenus(List<SpaceMapMenuItem> items, TreeNode node, File file) {

		RhinoScript rh = SpaceMap.getUserScript();
		if (rh != null) {
			try {
				NativeObject obj = rh.getGlobal("FileMenuItems");
				if (obj != null) {
					Object[] ids = NativeObject.getPropertyIds(obj);
					for (Object id : ids) {
						String label = id.toString();
						Object func = obj.get(id.toString(), rh.getScope());
						if (func instanceof Function) {
							RhinoNodeAction action = new RhinoNodeAction(label, (Function) func);
							items.add(new SpaceMapMenuItem(action, node, file));
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return items;
	}

	private List<SpaceMapMenuItem> getDefaultMenus(TreeNode node, File file) {
		List<SpaceMapMenuItem> items = new ArrayList<SpaceMapMenuItem>();

		
		items.add(new SpaceMapMenuItem(new ShowImageViewerAction(), node, file));
		items.add(new SpaceMapMenuItem(new ToggleFreeSpaceAction(), node, file));
		items.add(new SpaceMapMenuItem(new DeleteFileAction(), node, file));
		items.add(new SpaceMapMenuItem(new ShowPropertiesAction(), node, file));
		items.add(new SpaceMapMenuItem(new OpenInExplorerAction(), node, file));
		items.add(new SpaceMapMenuItem(new OpenFileAction(), node, file));
		items.add(new SpaceMapMenuItem(new HideNodeAction(), node, file));
		items.add(new SpaceMapMenuItem(new ResolveNodeAction(), node, file));

		return items;
	}

}
