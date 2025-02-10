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

package au.com.intermine.spacemap.model.filter;

import java.util.regex.Pattern;

import au.com.intermine.spacemap.model.TreeNode;

public class RegexTreeModelFilter implements ITreeModelFilter {

	private Pattern _pattern;

	public RegexTreeModelFilter(String regex) {
		_pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	public boolean accept(TreeNode node) {
		return (_pattern.matcher(node.getLabel()).matches());
	}

    public void begin(TreeNode root) {
    }

    public void end(TreeNode root) {
    }

}
