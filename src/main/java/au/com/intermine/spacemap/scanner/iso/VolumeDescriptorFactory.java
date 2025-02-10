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

public class VolumeDescriptorFactory {
    
    public static VolumeDescriptor createFromSector(Sector sector) {
        int type = (int) (0x00FF & (byte) sector.getByte(1));
        VolumeDescriptor result = null;
        switch (type) {
            case VolumeDescriptor.BOOT_RECORD:
                result = new BootRecord(sector);
                break;
            case VolumeDescriptor.PRIMARY_VOLUME:
                result = new PrimaryVolumeDescriptor(sector);
                break;
            case VolumeDescriptor.SUPPLEMENTARY_VOLUME:
                result = new SupplementaryVolumeDescriptor(sector);
                break;                
            case VolumeDescriptor.VDS_TERMINATOR:
                result = new VolumeSetTerminator(sector);
                break;
            default:
                throw new RuntimeException("Undefined/Unhandled Volume Descriptor Type: " + type);
        }
        return result;
    }

}
