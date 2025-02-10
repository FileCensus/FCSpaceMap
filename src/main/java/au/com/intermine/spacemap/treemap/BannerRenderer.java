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

package au.com.intermine.spacemap.treemap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JLabel;

import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.ResourceManager;

public class BannerRenderer {

	private static int DEFAULT_BANNER_HEIGHT = 100; //Use this height for the banner if no image is being drawn in the banner.
	
    private Image _image;
    private String _text;
    private Font _font;
    private int _height;

    public BannerRenderer(String text, String imagepath) {
        _text = text;
        _image = ResourceManager.getIcon(imagepath).getImage();
        _height = _image.getHeight(null);
        JLabel lbl = new JLabel();
        _font = lbl.getFont();
    }

    public BannerRenderer(String text, Image image) {
        _text = text;
        _image = image;
        _height = _image.getHeight(null);
        JLabel lbl = new JLabel();
        _font = lbl.getFont();
    }
    
    public BannerRenderer(String text) {
        _text = text;
        _image = null;
        _height = DEFAULT_BANNER_HEIGHT;
        JLabel lbl = new JLabel();
        _font = lbl.getFont();
    	
    }

    public void setFontSize(int size) {
        _font = _font.deriveFont(size);
    }

    public void setFont(Font f) {
        _font = f;
    }

    public Font getFont() {
        return _font;
    }

    //draw banner onto canvas with specified dimensions. The space on this canvas not taken up 
    //by the banner will be filled by the space map content.
    public void drawBanner(Graphics g, int canvasWidth, int canvasHeight) {
        g.setColor(Color.white);
        g.fillRect(0, 0, canvasWidth, canvasHeight);
        g.setColor(Color.black);
        Rectangle rect;
        if (_image != null) {
	        int startx = canvasWidth - _image.getWidth(null);
	        g.drawImage(_image, startx, 0, null);
	        rect = new Rectangle(0, 0, canvasWidth - _image.getWidth(null), _height);
        } else {
        	rect = new Rectangle(0, 0, canvasWidth, _height);
        }
        DrawingUtils.drawString(g, _font, _text, rect, DrawingUtils.TEXT_ALIGN_LEFT);
    }

    public int getHeight() {
        return _height;
    }

}
