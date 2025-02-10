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

package au.com.intermine.spacemap.scanner.filter;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexFileFilter implements IFileFilter {
    
    private Pattern _regex;
    
    public RegexFileFilter(String pattern) {
        _regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    public boolean accept(File file) {
        Matcher m = _regex.matcher(file.getName());
        return m.matches(); 
    }

}
