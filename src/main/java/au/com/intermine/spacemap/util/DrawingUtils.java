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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import au.com.intermine.spacemap.SpaceMap;

public class DrawingUtils {

    public static boolean DEBUG = false;

    private static final int cornerSize = 2;

    public static final int TEXT_ALIGN_LEFT = 0;
    public static final int TEXT_ALIGN_RIGHT = 1;
    public static final int TEXT_ALIGN_CENTER = 2;

    private static boolean _AntiAliasedFonts = true;

    public static final Object AA_TEXT_PROPERTY_KEY = new StringBuffer(
            "AATextPropertyKey");

    static {
        System.setProperty("swing.aatext", ((Boolean) _AntiAliasedFonts)
                .toString().toLowerCase());
    }

    public static Rectangle drawString(Graphics g, Font font, String text,
                                       Rectangle rect, int align) {
        return drawString(g, font, text, rect.x, rect.y, rect.width,
                rect.height, align);
    }

    public static void setPreferredAliasingMode(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    (_AntiAliasedFonts ? RenderingHints.VALUE_ANTIALIAS_ON
                            : RenderingHints.VALUE_ANTIALIAS_OFF));
        }
    }

    public static Rectangle drawString(Graphics g, Font font, String text,
                                       int x, int y, int width, int height, int align) {
        return drawString(g, font, text, x, y, width, height, align, false);
    }

    public static Rectangle drawString(Graphics g, Font font, String text,
                                       int x, int y, int width, int height, int align, boolean wrap) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);

        setPreferredAliasingMode(g);

        Rectangle ret = new Rectangle(0, 0, 0, 0);

        if (text == null) {
            return ret;
        }
        String[] alines = text.split("\\n");
        ArrayList<String> lines = new ArrayList<String>();
        for (String s : alines) {
            if (wrap && fm.stringWidth(s) > width) {
                // need to split this up into multiple lines...
                List<String> splitLines = wrapString(s, fm, width);
                lines.addAll(splitLines);
            } else {
                lines.add(s);
            }
        }
        int numlines = lines.size();
        while (fm.getHeight() * numlines > height) {
            numlines--;
        }
        if (numlines > 0) {
            int maxwidth = 0;
            int minxoffset = y + width;
            int totalheight = (numlines * fm.getHeight());

            int linestart = ((height / 2) - (totalheight / 2));

            if (!wrap) {
                ret.y = y + linestart;
            } else {
                ret.y = y;
                linestart = 0;
            }
            for (int idx = 0; idx < numlines; ++idx) {
                String line = lines.get(idx);
                int stringWidth = fm.stringWidth(line);
                // the width of the label depends on the font :
                // if the width of the label is larger than the item
                if (stringWidth > 0 && width < stringWidth) {
                    // We have to truncate the label
                    line = clipString(null, fm, line, width);
                    stringWidth = fm.stringWidth(line);
                }

                int xoffset = 0;
                int yoffset = linestart + fm.getHeight() - fm.getDescent();
                if (align == TEXT_ALIGN_RIGHT) {
                    xoffset = (width - stringWidth);
                } else if (align == TEXT_ALIGN_CENTER) {
                    xoffset = (int) Math.round((double) (width - stringWidth)
                            / (double) 2);
                }

                if (xoffset < minxoffset) {
                    minxoffset = xoffset;
                }
                g.drawString(line, x + xoffset, y + yoffset);
                if (stringWidth > maxwidth) {
                    maxwidth = stringWidth;
                }
                linestart += fm.getHeight();
            }

            ret.width = maxwidth;
            ret.height = totalheight;
            ret.x = x + minxoffset;

            // Debug only...
            if (DEBUG) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(1));
                g.setColor(Color.blue);
                g.drawRect(ret.x, ret.y, ret.width, ret.height);
                g.setColor(Color.green);
                g.drawRect(x, y, width, height);
            }

            return ret;
        }
        return ret;
    }

    private static List<String> wrapString(String s, FontMetrics fm, int width) {
        List<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            String test = line.toString() + ch;
            if (fm.stringWidth(test) > width) {
                if (test.length() > 1) {
                    // Backtrack to look for a space...
                    boolean breakFound = false;
                    if (ch != ' ') {

                        for (int j = line.length() - 1; j > 0; --j) {
                            if (line.charAt(j) == ' ') {
                                lines.add(line.substring(0, j));
                                line = new StringBuilder(line.substring(j + 1));
                                breakFound = true;
                                break;
                            }
                        }
                    }
                    if (!breakFound) {
                        lines.add(line.toString());
                        line = new StringBuilder();
                    }
                    line.append(ch);
                } else {
                    lines.add(test);
                    line = new StringBuilder();
                }
            } else {
                line.append(ch);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /**
     * Draws the text by creating a rectange centered around x, y
     *
     * @param g
     * @param font
     * @param msg
     * @param x
     * @param y
     * @return The rectangle that bounds the text
     */
    public static Rectangle drawCentredText(Graphics2D g, Font font,
                                            String text, int x, int y) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);

        setPreferredAliasingMode(g);

        Rectangle ret = new Rectangle(x, y, 0, 0);

        if (text == null) {
            return ret;
        }
        String[] alines = text.split("\\n");
        ArrayList<String> lines = new ArrayList<String>();
        for (String s : alines) {
            if (s.length() > 0) {
                lines.add(s);
            }
        }
        int numlines = lines.size();
        if (numlines > 0) {
            int maxwidth = 0;
            int totalheight = 0;
            for (int idx = 0; idx < numlines; ++idx) {
                String line = lines.get(idx);
                int stringWidth = fm.stringWidth(line);
                if (stringWidth > maxwidth) {
                    maxwidth = stringWidth;
                }
                totalheight += fm.getAscent() + fm.getDescent();
            }
            ret.width = maxwidth;
            ret.height = totalheight;
            ret.x = x - (maxwidth / 2);
            ret.y = y - (totalheight / 2);

            drawString(g, font, text, ret, TEXT_ALIGN_CENTER);

            return ret;
        }
        return ret;

    }

    /**
     * Clips the passed in String to the space provided.
     *
     * @param c              JComponent that will display the string, may be null
     * @param fm             FontMetrics used to measure the String width
     * @param string         String to display
     * @param availTextWidth Amount of space that the string can be drawn in
     * @return Clipped string that can fit in the provided space.
     */
    public static String clipStringIfNecessary(JComponent c, FontMetrics fm,
                                               String string, int availTextWidth) {
        if ((string == null) || (string.equals(""))) {
            return "";
        }
        int textWidth = stringWidth(c, fm, string);
        if (textWidth > availTextWidth) {
            return clipString(c, fm, string, availTextWidth);
        }
        return string;
    }

    /**
     * Clips the passed in String to the space provided. NOTE: this assumes the
     * string does not fit in the available space.
     *
     * @param c              JComponent that will display the string, may be null
     * @param fm             FontMetrics used to measure the String width
     * @param string         String to display
     * @param availTextWidth Amount of space that the string can be drawn in
     * @return Clipped string that can fit in the provided space.
     */
    public static String clipString(JComponent c, FontMetrics fm,
                                    String string, int availTextWidth) {
        // c may be null here.
        String clipString = "...";
        int width = stringWidth(c, fm, clipString);
        // NOTE: This does NOT work for surrogate pairs and other fun
        // stuff
        int nChars = 0;
        for (int max = string.length(); nChars < max; nChars++) {
            width += fm.charWidth(string.charAt(nChars));
            if (width > availTextWidth) {
                break;
            }
        }
        string = string.substring(0, nChars) + clipString;
        return string;
    }

    /**
     * Returns the width of the passed in String.
     *
     * @param c      JComponent that will display the string, may be null
     * @param fm     FontMetrics used to measure the String width
     * @param string String to get the width of
     */
    public static int stringWidth(JComponent c, FontMetrics fm, String string) {
        return fm.stringWidth(string);
    }

    /**
     * Returns the FontMetrics for the current Font of the passed in Graphics.
     * This method is used when a Graphics is available, typically when
     * painting. If a Graphics is not available the JComponent method of the
     * same name should be used.
     * <p>
     * Callers should pass in a non-null JComponent, the exception to this is if
     * a JComponent is not readily available at the time of painting.
     * <p>
     * This does not necessarily return the FontMetrics from the Graphics.
     *
     * @param c JComponent requesting FontMetrics, may be null
     * @param g Graphics Graphics
     */
    public static FontMetrics getFontMetrics(JComponent c, Graphics g) {
        return getFontMetrics(c, g, g.getFont());
    }

    /**
     * Returns the FontMetrics for the specified Font. This method is used when
     * a Graphics is available, typically when painting. If a Graphics is not
     * available the JComponent method of the same name should be used.
     * <p>
     * Callers should pass in a non-null JComonent, the exception to this is if
     * a JComponent is not readily available at the time of painting.
     * <p>
     * This does not necessarily return the FontMetrics from the Graphics.
     *
     * @param c    JComponent requesting FontMetrics, may be null
     * @param c    Graphics Graphics
     * @param font Font to get FontMetrics for
     */
    @SuppressWarnings("deprecation")
    public static FontMetrics getFontMetrics(JComponent c, Graphics g, Font font) {
        if (c != null) {
            // Note: We assume that we're using the FontMetrics
            // from the widget to layout out text, otherwise we can get
            // mismatches when printing.
            return c.getFontMetrics(font);
        }
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    public static void drawRoundedRect(Graphics g, int left, int top,
                                       int right, int bottom) {
        Graphics2D g2d = (Graphics2D) g;
        g.drawLine(left + cornerSize, top, right - cornerSize, top);
        g.drawLine(left + cornerSize, bottom, right - cornerSize, bottom);
        g.drawLine(left, top + cornerSize, left, bottom - cornerSize);
        g.drawLine(right, top + cornerSize, right, bottom - cornerSize);
        final Object previousAntiAliasingHint = g2d
                .getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        final Stroke previousStroke = g2d.getStroke();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER));
        try {
            g.drawLine(left, top + cornerSize, left + cornerSize, top);
            g.drawLine(left, bottom - cornerSize, left + cornerSize, bottom);
            g.drawLine(right, top + cornerSize, right - cornerSize, top);
            g.drawLine(right, bottom - cornerSize, right - cornerSize, bottom);
        } finally {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    previousAntiAliasingHint);
            g2d.setStroke(previousStroke);
        }
    }

    /**
     * convenience method for creating an image from a base64 encoded string
     *
     * @param base64 the data in base64 format
     * @return an ImageIcon, if the data is actually image bytes
     */
    public static Image createImageFromBase64(String base64) throws IOException {
        byte[] bytes = Base64.decode(base64);
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(input);
        return image;
    }

    public static Rectangle resizeImage(Image img, int targetWidth,
                                        int targetHeight) {
        int srcHeight = img.getHeight(null);
        int srcWidth = img.getWidth(null);
        return resizeImage(srcWidth, srcHeight, targetWidth, targetHeight);
    }

    /**
     * @param srcWidth
     * @param srcHeight
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static Rectangle resizeImage(int srcWidth, int srcHeight,
                                        int targetWidth, int targetHeight) {
        while (srcHeight > targetHeight || srcWidth > targetWidth) {
            if (srcHeight > targetHeight) {
                double ratio = (double) (targetHeight) / (double) srcHeight;
                srcHeight = targetHeight;
                srcWidth = (int) ((double) srcWidth * ratio);
            }

            if (srcWidth > targetWidth) {
                double ratio = (double) (targetWidth) / (double) srcWidth;
                srcWidth = targetWidth;
                srcHeight = (int) ((double) srcHeight * ratio);
            }
        }

        return new Rectangle(0, 0, srcWidth, srcHeight);
    }

    public static ImageIcon iconToImageIcon(Icon icon) {

        if (icon == null) {
            return null;
        }

        if (icon instanceof ImageIcon) {
            return (ImageIcon) icon;
        } else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(SpaceMap.getInstance(), g, 0, 0);
            g.dispose();
            return new ImageIcon(image);
        }
    }

}
