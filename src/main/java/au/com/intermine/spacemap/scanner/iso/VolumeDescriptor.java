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

public abstract class VolumeDescriptor extends ISO9660Record {
    
    public static final int BOOT_RECORD = 0;
    public static final int PRIMARY_VOLUME = 1;
    public static final int SUPPLEMENTARY_VOLUME = 2;
    public static final int VOLUME_PARTITION = 3;
    public static final int VDS_TERMINATOR = 255;
    
    private int _volumeDescriptorType;
    private String _standardIdentifier;
    private int _volumeDescriptorVersion;
    
    public VolumeDescriptor(Sector sector) {
        _volumeDescriptorType = sector.getInt(1);
        _standardIdentifier = sector.getString(2, 6);
        _volumeDescriptorVersion = sector.getInt(7);
    }
    
    public int getVolumeDescriptorType() {
        return _volumeDescriptorType;
    }
    
    public String getStandardIdentifier() {
        return _standardIdentifier;
    }
    
    public int getVolumeDescriptorVersion() {
        return _volumeDescriptorVersion;
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Volume Descriptor Type: ").append(_volumeDescriptorType).append("\n");
        b.append("Standard identifier   : ").append(_standardIdentifier).append("\n");
        b.append("Vol. Desc. Version    : ").append(_volumeDescriptorVersion).append("\n");
        
        return b.toString();
    }

}
