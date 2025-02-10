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

package au.com.intermine.spacemap.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DepthLimitedScanner {

    private String _root;

    private int _depth;

    public DepthLimitedScanner(String root, int depth) {
        _root = root;
        _depth = depth;
    }

    public List<File> getLeafFolders() {
        List<File> results = new ArrayList<File>();
        File f = new File(_root);
        scanFolder(f, 1, results);
        return results;
    }

    private void scanFolder(File folder, int depth, List<File> results) {
        String children[] = folder.list();
        if (children != null) {
            for (String child : children) {
                String fullpath = folder.getAbsolutePath() + "/" + child;
                File f = new File(fullpath);
                if (f.isDirectory()) {
                    if (depth == _depth) {
                        results.add(f);
                    } else {
                        scanFolder(f, depth + 1, results);
                    }
                }
            }
        }
    }

}
