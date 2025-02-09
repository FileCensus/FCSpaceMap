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

package au.com.intermine.spacemap.util;


import java.util.Map;

/**
 * Simple placeholder formatter. It accepts a pattern in the form
 * "....%a...%b..%c...", and attempts to match each character following a % in
 * the supplied placeholder map.
 * 
 * @author djb
 * 
 */
public class PlaceholderFormatter {
    
    /**
     * ctor
     *
     */
    protected PlaceholderFormatter() {
        
    }

    /**
     * Format the given pattern, replacing occurances of %<c> with whatever
     * value is stored with the key <c> in placeholders map. Standard c style
     * escape sequences can also be used, such as '\n', '\t', '\r', '\\', '\'' and '\"'.
     * Also special chars can be specified with octal (\777) or hex (\xFF).
     * 
     * @param pattern
     *            the format pattern to apply
     * @param placeholders
     *            the map of placeholder chars to their values
     * @return a formatted string
     */
    public static String format(String pattern, Map<Character, String> placeholders) {
        StringBuffer buf = new StringBuffer();
        int length = pattern.length();
        for (int i = 0; i < length; ++i) {
            char ch = pattern.charAt(i);
            if (ch == '%') {
                ++i;
                ch = pattern.charAt(i);
                if (ch == '%') {
                    buf.append('%');
                } else {
                    if (placeholders.containsKey(ch)) {
                        buf.append(placeholders.get(ch));
                    } else {
                        buf.append('?');
                    }
                }
            } else if (ch == '\\') {
                ch = pattern.charAt(++i);
                if (ch == '\\') {
                    buf.append('\\');
                } else if (ch == 'n') {
                    buf.append('\n');
                } else if (ch == 'r') {
                    buf.append('\r');
                } else if (ch == 't') {
                    buf.append('\t');
                } else if (ch == '\"') {
                    buf.append('\"');
                } else if (ch == '\'') {
                    buf.append('\'');
                } else if (Character.isDigit(ch)) {
                    char d0 = ch;
                    if (d0 < '0' || d0 > '7') {
                        throw new RuntimeException("Syntax error in format string!");
                    }
                    char d1 = pattern.charAt(++i);
                    if (d1 < '0' || d1 > '7') {
                        throw new RuntimeException("Syntax error in format string!");
                    }
                    char d2 = pattern.charAt(++i);
                    if (d2 < '0' || d2 > '7') {
                        throw new RuntimeException("Syntax error in format string!");
                    }
                    int i0 = d0 - '0';
                    int i1 = d1 - '0';
                    int i2 = d2 - '0';

                    int intCh = (i2 << 6) + (i1 << 3) + i0;
                    buf.append((char) intCh);
                } else if (ch == 'x') {

                    int hi = (int) pattern.charAt(++i);
                    int lo = (int) pattern.charAt(++i);

                    if (hi >= '0' && hi <= '9') {
                        hi = hi - '0';
                    } else if (hi >= 'A' && hi <= 'F') {
                        hi = hi - 'A' + 10;
                    } else if (hi >= 'a' && hi <= 'f') {
                        hi = hi - 'a' + 10;
                    } else {
                        throw new RuntimeException("Syntax error in format string!");
                    }

                    if (lo >= '0' && lo <= '9') {
                        lo = lo - '0';
                    } else if (lo >= 'A' && lo <= 'F') {
                        lo = lo - 'A' + 10;
                    } else if (lo >= 'a' && lo <= 'f') {
                        lo = lo - 'a' + 10;
                    } else {
                        throw new RuntimeException("Syntax error in format string!");
                    }
                    int intCh = (hi << 4) + lo;
                    buf.append((char) intCh);
                } else {
                    throw new RuntimeException("Unrecognized escape character following \\ - '" + ch + "'");
                }
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
}
