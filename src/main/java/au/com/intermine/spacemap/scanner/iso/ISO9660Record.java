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

public class ISO9660Record {
    
    protected String bytesAsString(byte[] data, int start, int end) {
        return new String(data, start - 1, end - start);
    }
    
    protected int byteAsInt(byte[] data, int offset) {
        return data[offset - 1];
    }
    
    protected void assertByte(byte value, byte[] data, int offset) {
        if (data[offset-1] != value) {
            throw new RuntimeException("Expected value at offset " + offset + " to be " + value + ". It was actually " + data[offset-1]);
        }
    }
    
    protected void assertByte(byte value, byte[] data, int start, int end) {
        for (int i = start - 1; i < end - 1; ++i)
        if (data[i] != value) {
            throw new RuntimeException("Expected value at offset " + i + " to be " + value + ". It was actually " + data[i]);
        }
    }
    
    
}
