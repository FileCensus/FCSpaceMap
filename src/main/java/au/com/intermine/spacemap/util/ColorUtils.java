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

import java.awt.Color;

public class ColorUtils {

    /** bold */
    public static final String BOLD = "bold";

    /** Italic */
    public static final String ITALIC = "italic";

    /** Foreground */
    public static final String FOREGROUND = "fg";

    /** Background */
    public static final String BACKGROUND = "bg";

    /**
     * getColor is responsible for creating a colour object based on a comma seperated string of rgb integers
     * such as "255,255,255", or the 13 basic colour names such as black, blue, red etc.
     * @param value the sring value of the colour
     * @return The Color object
     */
    public static Color getColor(String value) {
        if (value == null) {
            return Color.BLACK;
        }
        if (value.indexOf(',') == -1) {
            value = value.toLowerCase();
            // Lets look for colour names...
            if (value.equals("blue")) {
                return Color.blue;
            } else if (value.equals("cyan")) {
                return Color.cyan;
            } else if (value.equals("darkgray")) {
                return Color.darkGray;
            } else if (value.equals("gray")) {
                return Color.gray;
            } else if (value.equals("green")) {
                return Color.green;
            } else if (value.equals("lightgray")) {
                return Color.lightGray;
            } else if (value.equals("magenta")) {
                return Color.magenta;
            } else if (value.equals("orange")) {
                return Color.orange;
            } else if (value.equals("pink")) {
                return Color.pink;
            } else if (value.equals("red")) {
                return Color.red;
            } else if (value.equals("white")) {
                return Color.white;
            } else if (value.equals("brown")) {
                return new Color(165,42,42);
            } else if (value.equals("chocolate")) {
                return new Color(210,105,30);
            } else if (value.equals("goldenrod")) {
                return new Color(218,165,32);
            }
            return Color.black;
        } else {
            String[] parts = value.split(",");
            if (parts.length < 3) {
                return Color.RED;
            }
            int r = Integer.parseInt(parts[0]);
            int g = Integer.parseInt(parts[1]);
            int b = Integer.parseInt(parts[2]);
            int alpha = 255;
            if (parts.length > 3) {
                alpha = Integer.parseInt(parts[3]);
            }
            return new Color(r, g, b, alpha);
        }
    }
    
    //Return a string containing the comma separated red,green,blue values for the supplied color.
    public static String getRGBString(Color color) {
        return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
    }

}
