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

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import au.com.intermine.spacemap.filter.CollectionFilterDescriptor;
import au.com.intermine.spacemap.filter.DuplicateFilesFilterDescriptor;
import au.com.intermine.spacemap.filter.ExtensionFilterDescriptor;
import au.com.intermine.spacemap.filter.IFilterDescriptor;
import au.com.intermine.spacemap.filter.NoFilterDescriptor;
import au.com.intermine.spacemap.filter.RegularExpressionFilterDescriptor;

public class FilterPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JComboBox<IFilterDescriptor> _filters;
	private JPanel _argspanel;

	public FilterPanel() {
		super(new FlowLayout(FlowLayout.RIGHT, 2, 2));
		this.setOpaque(false);
		add(new JLabel("Filter"));
		_filters = new JComboBox<IFilterDescriptor>();

		_argspanel = new JPanel();
		_argspanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		_argspanel.setOpaque(false);
		_argspanel.setBorder(new EmptyBorder(0, 0, 0, 0));

		_filters.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					_argspanel.removeAll();
					final IFilterDescriptor filterdesc = (IFilterDescriptor) e.getItem();
					JComponent comp = filterdesc.getArgumentsComponent();
					if (comp != null) {
						_argspanel.add(comp);
					}
					JButton apply = new JButton("Apply");
					apply.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							SpaceMap.statusMsg("Applying filter...");
							SpaceMap mainwindow = SpaceMap.getInstance();
							SpaceMap.getStatusBar().getProgressBar().setIndeterminate(true);							
							try {						
								mainwindow.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								mainwindow.applyFilter(filterdesc.getFilter());
							} finally {
								mainwindow.getContentPane().setCursor(Cursor.getDefaultCursor());
								SpaceMap.statusMsg("");
								SpaceMap.getStatusBar().getProgressBar().setIndeterminate(false);
								SpaceMap.getStatusBar().getProgressBar().setValue(0);
							}
						}

					});

					_argspanel.add(apply);

					_argspanel.revalidate();
				}
			}

		});

		DefaultComboBoxModel<IFilterDescriptor> model = new DefaultComboBoxModel<IFilterDescriptor>();
		model.addElement(new NoFilterDescriptor());
		model.addElement(new ExtensionFilterDescriptor());
		model.addElement(new CollectionFilterDescriptor());
		model.addElement(new RegularExpressionFilterDescriptor());
		model.addElement(new DuplicateFilesFilterDescriptor());
		_filters.setModel(model);
		add(_filters);

		IFilterDescriptor filterdesc = (IFilterDescriptor) _filters.getSelectedItem();
		JComponent comp = filterdesc.getArgumentsComponent();
		if (comp != null) {
			_argspanel.add(filterdesc.getArgumentsComponent());
		}
		add(_argspanel);
	}

	public IFilterDescriptor getSelectedItem() {
		return (IFilterDescriptor) _filters.getSelectedItem();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_argspanel.setEnabled(enabled);
		_filters.setEnabled(enabled);
	}

}