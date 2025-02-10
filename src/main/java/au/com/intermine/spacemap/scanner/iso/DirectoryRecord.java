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

import java.util.Date;

public class DirectoryRecord {

    private int _lengthOfDirectoryRecord;
    private int _extendedAttributeRecordLength;
    private int _locationOfExtent;
    private int _dataLength;
    private Date _recordingDateTime;
    private byte _fileFlags;
    private int _fileUnitSize;
    private int _interleaveGapSize;
    private int _volumeSequenceNumber;
    private int _lengthOfFileIdentifier;
    private String _fileIdentifier;
    
    public DirectoryRecord(Sector sector) {
        this(sector, 0);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_fileIdentifier);
        sb.append(" ");
        sb.append(_recordingDateTime);
        sb.append(" ");
        sb.append(_dataLength);
        sb.append(" [").append(_lengthOfDirectoryRecord).append("]");
        return sb.toString();
    }

    public DirectoryRecord(Sector sector, int offset) {
        _lengthOfDirectoryRecord = sector.getInt(1 + offset);
        _extendedAttributeRecordLength = sector.getInt(2 + offset);
        _locationOfExtent = sector.getInt32Both(3 + offset, 10 + offset);
        _dataLength = sector.getInt32Both(11 + offset, 18 + offset);
        _recordingDateTime = sector.getDate(19 + offset);
        _fileFlags = sector.getByte(26 + offset);
        _fileUnitSize = sector.getInt(27 + offset);
        _interleaveGapSize = sector.getInt(28 + offset);
        _volumeSequenceNumber = sector.getInt16Both(29 + offset, 32 + offset);
        _lengthOfFileIdentifier = sector.getInt(33 + offset);
        _fileIdentifier = sector.getString(34 + offset, 33 + _lengthOfFileIdentifier + offset);
    }

    public int getLengthOfDirectoryRecord() {
        return _lengthOfDirectoryRecord;
    }

    public int getExtendedAttributeRecordLength() {
        return _extendedAttributeRecordLength;
    }

    public int getDataLength() {
        return _dataLength;
    }

    public byte getFileFlags() {
        return _fileFlags;
    }

    public String getFileIdentifier() {
        return _fileIdentifier;
    }

    public int getFileUnitSize() {
        return _fileUnitSize;
    }

    public int getInterleaveGapSize() {
        return _interleaveGapSize;
    }

    public int getLengthOfFileIdentifier() {
        return _lengthOfFileIdentifier;
    }

    public int getLocationOfExtent() {
        return _locationOfExtent;
    }

    public Date getRecordingDateTime() {
        return _recordingDateTime;
    }

    public int getVolumeSequenceNumber() {
        return _volumeSequenceNumber;
    }
    
    public boolean isDirectory() {
        return (_fileFlags & 0x02) != 0;
    }
    
    public boolean existence() {
        return (_fileFlags & 0x01) != 0;
    }
    
    public boolean isAssociatedFile() {
        return (_fileFlags & 0x04) != 0;
    }
    
    public boolean isRecord() {
        return (_fileFlags & 0x08) != 0;
    }
    
    public boolean hasProtection() {
        return (_fileFlags & 0x010) != 0;
    }
    
    public boolean isFinalRecord() {
        return (_fileFlags & 0x080) != 0;
    }

}
