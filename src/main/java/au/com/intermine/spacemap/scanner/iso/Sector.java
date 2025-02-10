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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Sector {

    private byte[] _data;

    public Sector(byte[] data) {
        _data = data;
    }
    
    public int getSize() {
        return _data.length;
    }

    public byte getByte(int offset) {
        return _data[offset-1];
    }

    public String getString(int start, int end) {
        return new String(_data, start - 1, end - start + 1);
    }

    /**
     * Reads one byte as an integer
     * @param offset
     * @return
     */
    public int getInt(int offset) {
        return (int) (0x00FF & _data[offset - 1]);
    }

    public int getInt32Both(int start, int end) {
        test(end - start + 1 == 8, "need 8 bytes!");
        int a = getInt32BE(start + 4);
        int b = getInt32LE(start);
        test(a == b, "LE != BE version of number!");
        return a;
    }

    public int getInt32LE(int start) {
        byte b1 = _data[start - 1];
        byte b2 = _data[start];
        byte b3 = _data[start + 1];
        byte b4 = _data[start + 2];

        int result = ((int) (0x00FF & b4) << 24) + ((int) (0x00FF & b3) << 16) + ((int) (0x00FF & b2) << 8) + (int) (0x00FF & b1);
        return result;
    }

    public int getInt32BE(int start) {
        byte b4 = _data[start - 1];
        byte b3 = _data[start];
        byte b2 = _data[start + 1];
        byte b1 = _data[start + 2];

        int result = ((int) (0x00FF & b4) << 24) + ((int) (0x00FF & b3) << 16) + ((int) (0x00FF & b2) << 8) + (int) (0x00FF & b1);
        return result;
    }

    public int getInt16Both(int start, int end) {
        test(end - start + 1 == 4, "need 4 bytes!");
        int a = getInt16BE(start + 2);
        int b = getInt16LE(start);
        test(a == b, "LE != BE version of number!");
        return a;
    }

    public int getInt16LE(int start) {
        byte b1 = _data[start - 1];
        byte b2 = _data[start];

        int result = ((int) (0x00FF & b2) << 8) + (int) (0x00FF & b1);
        return result;
    }

    public int getInt16BE(int start) {
        byte b2 = _data[start - 1];
        byte b1 = _data[start];

        int result = ((int) (0x00FF & b2) << 8) + (int) (0x00FF & b1);
        return result;
    }

    public void assertByte(int value, int offset) {
        if (_data[offset - 1] != (byte) value) {
            throw new RuntimeException("Expected value at offset " + offset + " to be " + value + ". It was actually " + _data[offset - 1]);
        }
    }

    private void test(boolean pred, String desc) {
        if (!pred) {
            throw new RuntimeException(desc);
        }
    }

    public void assertByte(int value, int start, int end) {
        for (int i = start - 1; i < end - 1; ++i)
            if (_data[i] != (byte) value) {
                throw new RuntimeException("Expected value at offset " + i + " to be " + value + ". It was actually " + _data[i]);
            }
    }

    public Date getDate(int i) {
        int year = getInt(i) + 1900;
        int month = getInt(i + 1);
        int day = getInt(i + 2);
        int hour = getInt(i + 3);
        int minute = getInt(i + 4);
        int second = getInt(i + 5);
        int gmtoffset = getByte(i + 6) * 15;

        Calendar c = Calendar.getInstance();
        c.set(year, month, day, hour, minute, second);
        String[] ids = TimeZone.getAvailableIDs(gmtoffset);
        if (ids != null && ids.length > 0) {
            c.setTimeZone(TimeZone.getTimeZone(ids[0]));
        }

        return c.getTime();
    }

    public byte[] getBytes(int start, int end) {
        byte[] result = new byte[end - start + 1];

        int i = 0;
        for (int j = start - 1; j < end - 1; ++j) {
            result[i++] = _data[j];
        }

        return result;
    }

    public Date getDateTimeStr(int i) {

        int year = Integer.parseInt(getString(i, i + 3));
        int month = Integer.parseInt(getString(i + 4, i + 5));
        int day = Integer.parseInt(getString(i + 6, i + 7));
        int hour = Integer.parseInt(getString(i + 8, i + 9));
        int minute = Integer.parseInt(getString(i + 10, i + 11));
        int second = Integer.parseInt(getString(i + 12, i + 13));
        // int hundredths = Integer.parseInt(getString(i+14, i+15));
        int gmtoffset = getInt(i + 16) / 15;

        Calendar c = Calendar.getInstance();
        c.set(year, month, day, hour, minute, second);
        String[] ids = TimeZone.getAvailableIDs(gmtoffset);
        if (ids != null && ids.length > 0) {
            c.setTimeZone(TimeZone.getTimeZone(ids[0]));
        }

        return c.getTime();
    }

}
