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

package au.com.intermine.spacemap.scanner.iso.model;

import au.com.intermine.spacemap.scanner.iso.DirectoryRecord;

public class File {
    
    private DirectoryRecord _dirRecord;
    
    public File(DirectoryRecord dirRecord) {
        _dirRecord = dirRecord;
    }
    
    public DirectoryRecord getDirectoryRecord() {
        return _dirRecord;        
    }

    public Object getName() {
        return _dirRecord.getFileIdentifier();
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        
        b.append(_dirRecord.getFileIdentifier()).append(" ");
        b.append(_dirRecord.getDataLength()).append(" [");
        b.append(_dirRecord.getRecordingDateTime()).append("] ");
        b.append(String.format(" <<0x%X-0x%X>>", new Object[] {new Integer(_dirRecord.getLocationOfExtent() * 2048), new Integer((_dirRecord.getLocationOfExtent() * 2048) + _dirRecord.getDataLength())} ));
        
        return b.toString();
    }

}
