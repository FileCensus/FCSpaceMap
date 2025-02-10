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

package au.com.intermine.spacemap.exception;

public class SystemFatalException extends RuntimeException {
	/** serial Version UID */
	private static final long serialVersionUID = 3390271835038954933L;

	/** Creates a new instance of SystemFatalException */
	public SystemFatalException() {
		super();
	}

	public SystemFatalException(String message) {
		super(message);
	}

	public SystemFatalException(Throwable cause) {
		super(cause);
	}
}
