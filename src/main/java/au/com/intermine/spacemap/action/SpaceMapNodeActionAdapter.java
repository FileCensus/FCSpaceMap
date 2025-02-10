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

import javax.swing.ImageIcon;

import au.com.intermine.spacemap.util.ResourceManager;

public abstract class SpaceMapNodeActionAdapter implements ISpaceMapNodeAction {
	
	private String _iconResource;
	
	public SpaceMapNodeActionAdapter() {
	}
	
	public SpaceMapNodeActionAdapter(String iconResource) {
		_iconResource = iconResource;
	}

    public ImageIcon getIcon() {
    	if (_iconResource != null) {
    		return ResourceManager.getIcon(_iconResource);
    	}
        return null;
    }

}
