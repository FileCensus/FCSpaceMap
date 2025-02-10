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

package au.com.intermine.spacemap.scanner.iso;

public class BootRecord extends VolumeDescriptor {
    
    private String _bootSystemIdentifier;
    private String _bootIdentifier;
    private String _bootSystemUse;
    
    public BootRecord(Sector sector) {
        super(sector);
        _bootSystemIdentifier = sector.getString(8, 39);
        _bootIdentifier = sector.getString(40, 71);
        _bootSystemUse = sector.getString(72, 2048);
    }
    
    public String getBootSystemIdentifier() {
        return _bootSystemIdentifier;
    }
    
    public String getBootIdentifier() {
        return _bootIdentifier;
    }
    
    public String getBootSystemUse() {
        return _bootSystemUse;
    }

}
