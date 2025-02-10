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

package au.com.intermine.spacemap.util;

import java.io.File;
import java.nio.file.Files;

import au.com.intermine.spacemap.ExceptionPublisher;

public class OSUtils {

    // Native functions...
    private static native void showFileProperties(String filename);

    /**
     * Flag to check to see if the native library could be located.
     */
    private static boolean _libloaded = false;

    /**
     * File the properties of the specified file
     * 
     * @param f
     */
	public static void showFileProperties(File f) {
        if (_libloaded) {
            try {
                showFileProperties(f.getAbsolutePath());
            } catch (Exception ex) {
                ExceptionPublisher.publish(ex);
            }
        } 
    }

    /**
     * Checks to see if the file/directory pointed to by <code>file</code> is a symbolic link, soft or hard.
     * <p>
     * On windows this would include reparse points.
     *  
     * @param file
     * @return
     */
    public static boolean isLink(File file) {
    	return Files.isSymbolicLink(file.toPath());
    }
    

    /**
     * Attempts to load the native library that is used to provide OS specific support
     */
    static {
        try {
            // TODO: Make a decision about what to about tighter native integration
//            System.loadLibrary("spacemaplib");
//            _libloaded = true;
        } catch (Throwable ex) {
            // do nothing
        	ex.printStackTrace();
            _libloaded = false;
        }
    }

}
