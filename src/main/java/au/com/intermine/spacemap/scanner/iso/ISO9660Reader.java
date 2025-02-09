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

package au.com.intermine.spacemap.scanner.iso;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.intermine.spacemap.scanner.iso.model.Directory;
import au.com.intermine.spacemap.scanner.iso.model.File;
import au.com.intermine.spacemap.scanner.iso.model.IDirectoryVisitor;

public class ISO9660Reader {

    public static final int LOGICAL_SECTOR_SIZE = 2048;

    private String _filename;
    private List _volumeDescriptors;

    public ISO9660Reader(String filename) {
        _filename = filename;
    }

    public void read() {
        RandomAccessFile file = null;

        try {
            file = new RandomAccessFile(_filename, "r");
            final RandomAccessFile filearg = file;

            // Skip over the system area
            file.skipBytes(16 * LOGICAL_SECTOR_SIZE);

            _volumeDescriptors = new ArrayList();

            byte[] sector_bytes = new byte[2048];
            file.read(sector_bytes);
            Sector sector = new Sector(sector_bytes);
            VolumeDescriptor vd = VolumeDescriptorFactory.createFromSector(sector);
            while (vd.getVolumeDescriptorType() != VolumeDescriptor.VDS_TERMINATOR) {
                _volumeDescriptors.add(vd);
                file.read(sector_bytes);
                sector = new Sector(sector_bytes);
                vd = VolumeDescriptorFactory.createFromSector(sector);
            }
            for (int i = 0; i < _volumeDescriptors.size(); ++i) {
                vd = (VolumeDescriptor) _volumeDescriptors.get(i);
                if (vd.getVolumeDescriptorType() == VolumeDescriptor.PRIMARY_VOLUME || vd.getVolumeDescriptorType() == VolumeDescriptor.SUPPLEMENTARY_VOLUME) {
                    final PrimaryVolumeDescriptor pvd = (PrimaryVolumeDescriptor) vd;
                    Sector pathtablesector = null;
                    if (pvd.getLocationOfTypeLPathTable() != 0) {
                        pathtablesector = readSector(file,
                                pvd.getLocationOfTypeLPathTable() * pvd.getLogicalBlockSize(),
                                pvd.getPathTableSize());
                    } else if (pvd.getLocationOfTypeMPathTable() != 0) {
                        pathtablesector = readSector(file,
                                pvd.getLocationOfTypeLPathTable() * pvd.getLogicalBlockSize(),
                                pvd.getPathTableSize());
                    }

                    if (pathtablesector != null) {
                        Directory root = PathTableReader.readPathTable(pathtablesector);
                        root.traverse(new IDirectoryVisitor() {
                            public void visit(Directory dir) {
                                PathTableRecord rec = dir.getPathTableRecord();
                                int offset = rec.getLocationOfExtent() * pvd.getLogicalBlockSize();
                                Sector dirsector = readSector(filearg, offset, 2048);
                                DirectoryRecord dirrecord = new DirectoryRecord(dirsector);
                                if (dirrecord.getDataLength() > dirsector.getSize()){
                                    dirsector = readSector(filearg, offset, dirrecord.getDataLength());
                                }
                                                                
                                List files = PathTableReader.readFilesFromPath(dir.getPathTableRecord(), dirsector);
                                
                                Iterator iter = files.iterator();
                                while (iter.hasNext()) {
                                    File f = (File) iter.next();                                    
                                }
                                
                            }                            
                        });
                    }

                }

            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }

    }

    private Sector readSector(RandomAccessFile file, int offset, int size) {
        Sector s = null;
        try {
            file.seek(offset);
            byte[] sector_bytes = new byte[size];
            file.read(sector_bytes);
            s = new Sector(sector_bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return s;
    }

}
