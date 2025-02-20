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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import au.com.intermine.spacemap.exception.SystemFatalException;

public class ResourceManager {
	
	private static HashMap<String, ImageIcon> _IconCache;
	private static final int[] ICON_SIZES = { 14, 16, 18, 20, 24, 32, 64, 128 };
	
	static {
		_IconCache = new HashMap<String, ImageIcon>();
	}

	public static synchronized ImageIcon getIcon(String name) {
		String path = String.format("/au/com/intermine/spacemap/resource/%s", name);
		if (_IconCache.containsKey(path)) {
			return _IconCache.get(path);
		}
		try {
			URL url = ResourceManager.class.getResource(path);
			if (url == null) {
				System.err.println("Warning: Resource not found: " + path);
				return null;
			}
			BufferedImage image = ImageIO.read(url);
			if (image == null) {
				System.err.println("Warning: Failed to read image: " + path);
				return null;
			}

			// Get the system scale factor (1.0 for standard displays, 2.0 for HiDPI/Retina)
			double scaleFactor = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0;
			
			// Choose the appropriate size based on scale factor
			int baseSize = 16; // Default icon size
			int targetSize = (int) Math.round(baseSize * scaleFactor);
			
			// Find the best matching size from our predefined sizes
			int bestSize = ICON_SIZES[0];
			for (int size : ICON_SIZES) {
				if (size >= targetSize) {
					bestSize = size;
					break;
				}
			}
			
			// Create a scaled version of the image
			Image scaledImage = image.getScaledInstance(bestSize, bestSize, Image.SCALE_SMOOTH);
			
			ImageIcon result = new ImageIcon(scaledImage);
			_IconCache.put(path, result);
			return result;
		} catch (Exception ex) {
			System.err.println("Warning: Error loading resource: " + path + " - " + ex.getMessage());
			return null;
		}
	}

}
