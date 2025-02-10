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

public class PrimaryVolumeDescriptor extends VolumeDescriptor {

    protected String _systemIdentifier;
    protected String _volumeIdentifier;
    protected int _volumeSpaceSize;
    protected int _volumeSetSize;
    protected int _volumeSequenceNumber;
    protected int _logicalBlockSize;
    protected int _pathTableSize;
    protected int _locationOfTypeLPathTable;
    protected int _locationOfOptionalTypeLPathTable;
    protected int _locationOfTypeMPathTable;
    protected int _locationOfOptionalMPathTable;
    protected DirectoryRecord _rootDirectoryRecord;
    protected String _volumeSetIdentifier;
    protected String _publisherIdentifier;
    protected String _dataPreparerIdentifier;
    protected String _applicationIdentifier;
    protected String _copyrightFileIdentifier;
    protected String _abstractFileIdentifier;
    protected String _bibliographicFileIdentifier;
    protected Date _volumeCreationDateTime;
    protected Date _volumeModificationDateTime;
    protected Date _volumeExpirationDateTime;
    protected Date _volumeEffectiveDateTime;
    protected int _fileStructureVersion;

    public PrimaryVolumeDescriptor(Sector sector) {
        super(sector);
        sector.assertByte(0, 8);
        _systemIdentifier = sector.getString(9, 40);
        _volumeIdentifier = sector.getString(41, 72);
        sector.assertByte(0, 73, 80);
        _volumeSpaceSize = sector.getInt32Both(81, 88);
        sector.assertByte(0, 89, 120);
        _volumeSetSize = sector.getInt16Both(121, 124);
        _volumeSequenceNumber = sector.getInt16Both(125, 128);
        _logicalBlockSize = sector.getInt16Both(129, 132);
        _pathTableSize = sector.getInt32Both(133, 140);
        _locationOfTypeLPathTable = sector.getInt32LE(141);
        _locationOfOptionalTypeLPathTable = sector.getInt16LE(145);
        _locationOfTypeMPathTable = sector.getInt16LE(149);
        _locationOfOptionalMPathTable = sector.getInt16LE(153);
        _rootDirectoryRecord = new DirectoryRecord(new Sector(sector.getBytes(157, 190)));
        _volumeSetIdentifier = sector.getString(191, 318);
        _publisherIdentifier = sector.getString(319, 446);
        _dataPreparerIdentifier = sector.getString(447, 574);
        _applicationIdentifier = sector.getString(575, 702);
        _copyrightFileIdentifier = sector.getString(703, 739);
        _abstractFileIdentifier = sector.getString(740, 776);
        _bibliographicFileIdentifier = sector.getString(777, 813);
        _volumeCreationDateTime = sector.getDateTimeStr(814);
        _volumeModificationDateTime = sector.getDateTimeStr(831);
        _volumeExpirationDateTime = sector.getDateTimeStr(848);
        _volumeEffectiveDateTime = sector.getDateTimeStr(865);
        _fileStructureVersion = sector.getInt(882);
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(super.toString());
        b.append("System Identifier:").append(_systemIdentifier).append("\n");

        return b.toString();
    }

    public String getAbstractFileIdentifier() {
        return _abstractFileIdentifier;
    }

    public String getApplicationIdentifier() {
        return _applicationIdentifier;
    }

    public String getBibliographicFileIdentifier() {
        return _bibliographicFileIdentifier;
    }

    public String getCopyrightFileIdentifier() {
        return _copyrightFileIdentifier;
    }

    public String getDataPreparerIdentifier() {
        return _dataPreparerIdentifier;
    }

    public int getFileStructureVersion() {
        return _fileStructureVersion;
    }

    public int getLocationOfOptionalMPathTable() {
        return _locationOfOptionalMPathTable;
    }

    public int getLocationOfOptionalTypeLPathTable() {
        return _locationOfOptionalTypeLPathTable;
    }

    public int getLocationOfTypeLPathTable() {
        return _locationOfTypeLPathTable;
    }

    public int getLocationOfTypeMPathTable() {
        return _locationOfTypeMPathTable;
    }

    public int getLogicalBlockSize() {
        return _logicalBlockSize;
    }

    public int getPathTableSize() {
        return _pathTableSize;
    }

    public String getPublisherIdentifier() {
        return _publisherIdentifier;
    }

    public DirectoryRecord getRootDirectoryRecord() {
        return _rootDirectoryRecord;
    }

    public String getSystemIdentifier() {
        return _systemIdentifier;
    }

    public Date getVolumeCreationDateTime() {
        return _volumeCreationDateTime;
    }

    public Date getVolumeEffectiveDateTime() {
        return _volumeEffectiveDateTime;
    }

    public Date getVolumeExpirationDateTime() {
        return _volumeExpirationDateTime;
    }

    public String getVolumeIdentifier() {
        return _volumeIdentifier;
    }

    public Date getVolumeModificationDateTime() {
        return _volumeModificationDateTime;
    }

    public int getVolumeSequenceNumber() {
        return _volumeSequenceNumber;
    }

    public String getVolumeSetIdentifier() {
        return _volumeSetIdentifier;
    }

    public int getVolumeSetSize() {
        return _volumeSetSize;
    }

    public int getVolumeSpaceSize() {
        return _volumeSpaceSize;
    }

}
