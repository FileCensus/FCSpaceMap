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

package au.com.intermine.spacemap.scanner.iso.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.intermine.spacemap.scanner.iso.PathTableRecord;

public class Directory {
    
    private PathTableRecord _pathTableRec;
    
    private List _files;
    private List _subdirectories;
    private Directory _parent;

    public Directory(PathTableRecord pathTableRec) {        
        _pathTableRec = pathTableRec;
        _parent = null;
        _files = new ArrayList();
        _subdirectories = new ArrayList();
    }
    
    public PathTableRecord getPathTableRecord() {
        return _pathTableRec;
    }
    
    public List getFiles() {
        return _files;
    }
    
    public List getSubdirectories() {
        return _subdirectories;
    }

    public String getName() {
        return _pathTableRec.getDirectoryIdentifier();
    }
    
    public Directory getParent() {
        return _parent;
    }
    
    public void addSubdirectory(Directory child) {
        child.setParent(this);
        this.getSubdirectories().add(child);
    }

    private void setParent(Directory directory) {
        _parent = directory;
    }
    
    public void traverse(IDirectoryVisitor v) {
        v.visit(this);
        Iterator iter = this.getSubdirectories().iterator();
        while (iter.hasNext()) {
            Directory child = (Directory) iter.next();
            child.traverse(v);
        }
    }
    
}
