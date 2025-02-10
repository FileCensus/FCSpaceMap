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

package au.com.intermine.spacemap;

import java.util.HashMap;
import java.util.Properties;

import au.com.intermine.spacemap.util.PlaceholderFormatter;

/**
 * Version class
 * 
 * @author djb
 * 
 */
public final class Version {

	/** The name of the app */
	private static String Name = "FileCensus SpaceMap";

	/** This map is used to format version strings using placeholders */
	private static HashMap<Character, String> _placeholders;

	private static Properties _props;

	/**
	 * Static initializer. Sets up the placeholder map
	 */
	static {
		_props = new Properties();

		try {
			_props.load(Version.class.getResourceAsStream("/au/com/intermine/spacemap/version.properties"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		_placeholders = new HashMap<Character, String>();
		_placeholders.put('n', Name);
		_placeholders.put('M', get("build.major"));
		_placeholders.put('m', get("build.minor"));
		_placeholders.put('r', get("build.revision"));
		_placeholders.put('b', get("build.number"));
		_placeholders.put('d', get("build.date"));
	}

	private static String get(String name) {
		return (String) _props.getProperty(name);
	}

	/**
	 * ctor
	 */
	private Version() {
	}

	/**
	 * @return the build number
	 */
	public static String getBuild() {
		return get("build.number");
	}

	/**
	 * @return the application name
	 */
	public static String getName() {
		return Name;
	}

	/**
	 * @return the major version number
	 */
	public static String getMajor() {
		return get("build.major");
	}

	/**
	 * @return the minor version number
	 */
	public static String getMinor() {
		return get("build.minor");
	}

	/**
	 * @return the Revision number
	 */
	public static String getRevision() {
		return get("build.revision");
	}

	/**
	 * 
	 * @return a full version string
	 */
	public static String getFullVersion() {
		return format("%n %M.%m.%r (%b)");
	}

	/**
	 * Format a version string using the standard placeholders as described below. %n = name, %M = major, %m = minor %r = revision (number only) %R = revision (including SVN markup)
	 * 
	 * @param pattern
	 *            the format pattern
	 * @return a formatted string
	 */
	public static String format(String pattern) {
		return PlaceholderFormatter.format(pattern, _placeholders);
	}

}
