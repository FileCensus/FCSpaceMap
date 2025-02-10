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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.intermine.spacemap.scanner.iso.model.Directory;
import au.com.intermine.spacemap.scanner.iso.model.File;

public class PathTableReader {

    public static Directory readPathTable(Sector sector) {
        HashMap map = new HashMap();
        // List result = new ArrayList();
        int offset = 0;
        int recidx = 1;
        Directory root = null;

        while (offset < sector.getSize()) {

            PathTableRecord rec = new PathTableRecord(sector, offset, recidx);
            // result.add(rec);

            int pad = (rec.getLengthOfDirectoryIdentifier() % 2 == 0 ? 0 : 1);
            offset += rec.getLengthOfDirectoryIdentifier() + 8 + pad;
            Directory dir = new Directory(rec);
            map.put(recidx + "", dir);
            if (root == null) {
                root = dir;
            } else {
                Directory parent = (Directory) map.get(rec.getParentDirectoryEntry() + "");
                parent.addSubdirectory(dir);
            }

            recidx++;
        }
        
        // dumpTree(root, 0);

        return root;
    }

    protected static void dumpTree(Directory parent, int indent) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < indent; ++i) {
            b.append("  ");
        }
        String pad = b.toString();
        b.append(parent.getPathTableRecord().getDirectoryIdentifier());

        for (int i = 0; i < parent.getSubdirectories().size(); ++i) {
            Directory child = (Directory) parent.getSubdirectories().get(i);
            System.out.println(pad + child.getName());
            dumpTree(child, indent + 1);
        }
    }

    public static List readFilesFromPath(PathTableRecord pathTableRecord, Sector sector) {
        List result = new ArrayList();
        int offset = 0;
        while (offset < sector.getSize()) {
            DirectoryRecord rec = new DirectoryRecord(sector, offset);
            if (rec.getLengthOfDirectoryRecord() == 0) {
                break;
            }
            if (!rec.isDirectory()) {
                File f = new File(rec);
                result.add(f);
            }
            offset += rec.getLengthOfDirectoryRecord();
            if (offset < sector.getSize() && sector.getByte(offset+1) == 0) {
                // we need to round up to the nearest multiple of the block size (2048).
                int d = (offset / 2048) + 1;
                offset = d * 2048;
            }
        }
        
        return result;
    }

}
