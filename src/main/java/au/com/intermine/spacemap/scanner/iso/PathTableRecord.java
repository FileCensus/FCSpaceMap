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

public class PathTableRecord {

    private int _lengthOfDirectoryIdentifier;
    private int _extendedAttributeRecordLength;
    private int _locationOfExtent;
    private int _parentDirectoryEntry;
    private String _directoryIdentifier;

    private int _pathIndex;

    public PathTableRecord(Sector sector, int offset, int pathIndex) {
        _lengthOfDirectoryIdentifier = sector.getInt(offset + 1);
        _extendedAttributeRecordLength = sector.getInt(offset + 2);
        _locationOfExtent = sector.getInt32LE(offset + 3);
        _parentDirectoryEntry = sector.getInt16LE(offset + 7);
        _directoryIdentifier = sector.getString(offset + 9, offset + 8 + _lengthOfDirectoryIdentifier);

        _pathIndex = pathIndex;
    }

    public String getDirectoryIdentifier() {
        return _directoryIdentifier;
    }

    public int getExtendedAttributeRecordLength() {
        return _extendedAttributeRecordLength;
    }

    public int getLengthOfDirectoryIdentifier() {
        return _lengthOfDirectoryIdentifier;
    }

    public int getLocationOfExtent() {
        return _locationOfExtent;
    }

    public int getParentDirectoryEntry() {
        return _parentDirectoryEntry;
    }

    public int getPathIndex() {
        return _pathIndex;
    }

}
