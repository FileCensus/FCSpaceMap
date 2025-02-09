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

import java.text.NumberFormat;


public class NumberRenderer {

    private static NumberFormat _sizespaceformatter = NumberFormat.getInstance();

    static {
        _sizespaceformatter.setMaximumFractionDigits(2);
    }

    private NumberType _numberType;
    private int _ByteDivisor;

    public NumberRenderer(NumberType type, int byteDivisor) {
        _ByteDivisor = byteDivisor;
        _numberType = type;
    }

    public String render(long size) {
        return render(size, _ByteDivisor, _numberType);
    }

    public static String render(long size, int bytedivisor, NumberType type) {

        switch (type) {
        case Bytes:
            long KBYTE = bytedivisor;
            long MBYTE = KBYTE * bytedivisor;
            long GBYTE = MBYTE * bytedivisor;
            long TBYTE = GBYTE * bytedivisor;

            if (size < KBYTE) {
                return size + "B";
            } else if (size < MBYTE) {
                return round((double) size / (double) KBYTE) + " KB";
            } else if (size < GBYTE) {
                return round((double) size / (double) MBYTE) + " MB";
            } else if (size < TBYTE) {
                return round((double) size / (double) GBYTE) + " GB";
            } else {
                return round((double) size / (double) TBYTE) + " TB";
            }

        default:
            return size + "";
        }

    }

    protected static String round(double val) {
        return _sizespaceformatter.format(val).toString();
    }

}
