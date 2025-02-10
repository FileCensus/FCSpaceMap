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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTextField;

import au.com.intermine.spacemap.model.filter.ITreeModelFilter;
import au.com.intermine.spacemap.model.filter.RegexTreeModelFilter;

public class ExtensionFilterDescriptor implements IFilterDescriptor {

	private JTextField _text;

	public ExtensionFilterDescriptor() {
		_text = new JTextField();
		_text.setPreferredSize(new Dimension(50, 25));
	}

	public JComponent getArgumentsComponent() {
		return _text;
	}

	public ITreeModelFilter getFilter() {
		String ext = _text.getText();
		if (ext != null && ext.length() > 0) {
			return new RegexTreeModelFilter(String.format("^.*[.]%s$", ext));
		}
		return null;
	}

	public String toString() {
		return "Files with extension";
	}

}
