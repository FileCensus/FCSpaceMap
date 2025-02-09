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

package au.com.intermine.spacemap.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CheckpointProgressProvider implements IScannerProgressProvider {
    
    private List<String> _checkpoints;
    private int _checkpointCount;
    private int _percent;
    
    public CheckpointProgressProvider(List<File> list) {
        _checkpoints = new ArrayList<String>();
        for (File f : list) {
            _checkpoints.add(f.getAbsolutePath());
        }
        _checkpointCount = list.size();        
    }

    public int getPercentComplete(File currentfolder, long filesscanned, long foldersscanned) {
        String path = currentfolder.getAbsolutePath();
        if (_checkpoints.contains(path)) {
            _checkpoints.remove(path);
            double dbl = (double) (_checkpointCount - _checkpoints.size()) / (double) _checkpointCount;            
            _percent = (int) (dbl * 100);
        }
        return _percent;
    }

}
