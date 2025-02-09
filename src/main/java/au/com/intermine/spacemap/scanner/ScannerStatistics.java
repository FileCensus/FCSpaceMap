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

package au.com.intermine.spacemap.scanner;

public class ScannerStatistics {

	private long _totalFiles;
	private long _totalDirectories;
	private long _filteredFiles;
	private int _percentComplete = -1;
	private byte[] _lock = {};

	public long getTotalFiles() {
		synchronized (_lock) {
			return _totalFiles;
		}
	}

	public void addTotalFiles(long amount) {
		synchronized (_lock) {
			_totalFiles += amount;
		}
	}

	public long getTotalDirectories() {
		synchronized (_lock) {
			return _totalDirectories;
		}
	}

	public void addTotalDirectories(long amount) {
		synchronized (_lock) {
			_totalDirectories += amount;
		}
	}

	public long getFilteredFiles() {
		synchronized (_lock) {
			return _filteredFiles;
		}
	}

	public void addFilteredFile(long amount) {
		synchronized (_lock) {
			_filteredFiles += amount;
		}
	}

	public void incrementTotalFiles() {
		synchronized (_lock) {
			_totalFiles++;
		}
	}

	public void incrementFilteredFiles() {
		synchronized (_lock) {
			_filteredFiles++;
		}
	}

	public void incrementTotalDirectories() {
		synchronized (_lock) {
			_totalDirectories++;
		}
	}

	public void addAllStatistics(ScannerStatistics other) {
		if (other != null) {
			synchronized (_lock) {
				_totalFiles += other._totalFiles;
				_totalDirectories += other._totalDirectories;
				_filteredFiles += other._filteredFiles;
			}
		}
	}

	public void incrementFiles(boolean filtered) {
		synchronized (_lock) {
			_totalFiles++;
			if (filtered) {
				_filteredFiles++;
			}
		}
	}
	
	public int getPercentComplete() {
	    return _percentComplete;
	}
	
	public void setPercentComplete(int percent) {
	    _percentComplete = percent;
	}

}
