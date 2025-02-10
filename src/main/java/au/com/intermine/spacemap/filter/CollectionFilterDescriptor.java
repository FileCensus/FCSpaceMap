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

package au.com.intermine.spacemap.filter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import au.com.intermine.spacemap.SpaceMap;
import au.com.intermine.spacemap.model.filter.ITreeModelFilter;
import au.com.intermine.spacemap.model.filter.RegexTreeModelFilter;
import au.com.intermine.spacemap.util.RhinoScript;
import au.com.intermine.spacemap.util.Utils;

public class CollectionFilterDescriptor implements IFilterDescriptor {

	private JComboBox _collections;

	public CollectionFilterDescriptor() {
		_collections = new JComboBox();
		_collections.setMaximumRowCount(20);

		// Add mouse motion listener to show extensions in status bar
		_collections.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				javax.swing.JComboBox box = (javax.swing.JComboBox)_collections;
				Object comp = box.getUI().getAccessibleChild(box, 0);
				if (comp instanceof javax.swing.JPopupMenu) {
					javax.swing.JPopupMenu popup = (javax.swing.JPopupMenu)comp;
					javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane)popup.getComponent(0);
					java.awt.event.MouseMotionListener[] listeners = scrollPane.getViewport().getView().getMouseMotionListeners();
					for (java.awt.event.MouseMotionListener l : listeners) {
						scrollPane.getViewport().getView().removeMouseMotionListener(l);
					}
					scrollPane.getViewport().getView().addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
						@Override
						public void mouseMoved(java.awt.event.MouseEvent evt) {
							javax.swing.JList list = (javax.swing.JList)evt.getSource();
							int index = list.locationToIndex(evt.getPoint());
							if (index >= 0) {
								CollectionItems item = (CollectionItems)list.getModel().getElementAt(index);
								SpaceMap.statusMsg("File types: " + Utils.join(item.getExtensions(), ", "));
							}
						}
					});
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				SpaceMap.statusMsg(""); // Clear status when popup closes
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				SpaceMap.statusMsg(""); // Clear status when popup is canceled
			}
		});

		RhinoScript rh = SpaceMap.getUserScript();
		if (rh != null) {
			NativeObject obj = rh.getGlobal("Collections");
			if (obj != null) {
				Object[] ids = NativeObject.getPropertyIds(obj);
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				_collections.setModel(model);
				for (Object id : ids) {
					String label = id.toString();
					NativeArray extensions = (NativeArray) obj.get(label, rh.getScope());
					CollectionItems items = new CollectionItems(label, extensions);
					model.addElement(items);
				}
			}
		}
	}

	public JComponent getArgumentsComponent() {
		return _collections;
	}

	public ITreeModelFilter getFilter() {
		CollectionItems items = (CollectionItems) _collections.getSelectedItem();
		if (items != null) {
			String expr = String.format("^.*[.](%s)$", Utils.join(items.getExtensions(), "|"));
			return new RegexTreeModelFilter(expr);
		}

		return null;
	}

	@Override
	public String toString() {
		return "By type";
	}

}

class CollectionItems {

	private String _label;

	private List<String> _extensions;

	public CollectionItems(String label, NativeArray extensions) {
		_label = label;
		_extensions = new ArrayList<String>();
		for (int i = 0; i < extensions.getLength(); ++i) {
			String ext = (String) extensions.get(i, extensions);
			if (!_extensions.contains(ext)) {
				_extensions.add(ext);
			}
		}
	}

	public List<String> getExtensions() {
		return _extensions;
	}

	@Override
	public String toString() {
		return _label;
	}

}
