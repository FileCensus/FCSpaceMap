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
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import au.com.intermine.spacemap.exception.SystemFatalException;

public class ResourceManager {
	
	private static HashMap<String, ImageIcon> _IconCache;
	
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
			Image image = ImageIO.read(url);
			Image[] scaledImages = Arrays.stream(new int[] { 14, 16, 18, 20, 24, 32, 64, 128 }).mapToObj((int size) -> {
				return image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
			}).toArray(Image[]::new);


			ImageIcon result = new ImageIcon(new BaseMultiResolutionImage(scaledImages));
			_IconCache.put(path, result);
			return result;
		} catch (Exception ex) {
			throw new SystemFatalException(ex);
		}
	}

}
