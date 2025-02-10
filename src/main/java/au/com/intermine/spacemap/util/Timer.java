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

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Hi res timer
 * 
 */
public class Timer {
	
	private static String EOL = System.getProperty("line.separator");

	/** description */
	private String _description;
	/** keeps a running tally of elapsed times */
	private List<Long> _times;

	private long _startCounter;

	private long _endCounter;

	private long _frequency;

	private Writer _writer = null;

	/**
	 * ctor Starts the timer
	 * 
	 * @param description
	 *            a description of this timer
	 */
	public Timer(String description) {
		_description = description;
		_times = new ArrayList<Long>();
		_startCounter = System.nanoTime();
		_frequency = 1000000000;
		_writer = new OutputStreamWriter(System.out);
	}

	/** start/restart the timer */
	public void start() {
		_startCounter = System.nanoTime();
	}

	/** restart the timer and clear the running tally */
	public void reset() {
		_times = new ArrayList<Long>();
		_startCounter = System.nanoTime();
	}

	/**
	 * Stop the timer
	 */
	public void stop() {
		stop(false, false, null);
	}

	/**
	 * Stop the timer. optionally print the elapsed time to standard out
	 * 
	 * @param printelapsed
	 *            if true a message containing the elapsed time will be printed to standard out.
	 */
	public void stop(boolean printelapsed) {
		stop(printelapsed, false, null);
	}
	
	/**
	 * @param printelapsed
	 * @param asdouble
	 */
	public void stop(boolean printelapsed, boolean asdouble) {
		stop(printelapsed, asdouble, null);
	}

	/**
	 * @param printelapsed
	 * @param asdouble
	 * @param auxmsg
	 */
	public void stop(boolean printelapsed, boolean asdouble, String auxmsg) {
		_endCounter = System.nanoTime();
		String msg = null;
		if (printelapsed) {
			if (asdouble) {
				msg = String.format("%s took %f%s", _description, getElapsedMillisDouble(), (auxmsg == null ? "" : " (" + auxmsg + ")"));
			} else {
				msg = String.format("%s took %d%s", _description, getElapsedMillis(), (auxmsg == null ? "" : " (" + auxmsg + ")"));
			}
			if (msg != null && _writer != null) {
				writeln(msg);
			}
		}
		_times.add(getElapsedMillis());
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * @return the number of elapsed milliseconds since start and stop (or now if stop hasn't been called)
	 */
	public long getElapsedMillis() {
		double elapsed = ((double) (_endCounter - _startCounter) / (double) _frequency) * 1000;
		return (long) elapsed;
	}

	public double getElapsedMillisDouble() {
		return ((double) (_endCounter - _startCounter) / (double) _frequency) * 1000;
	}

	/**
	 * @return the average
	 */
	public long getAverage() {
		return getAverage(false);
	}
	
	private void writeln(String msg) {
		try {
			_writer.write(msg + EOL);		
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
			_writer.flush();
			} catch (Exception ex2) {
				// ignore
			}
		}
	}

	/**
	 * @param dump
	 *            if true will print a message to std out
	 * @return the average of all elapsed times recorded in the running tally
	 */
	public long getAverage(boolean dump) {
		long total = 0;
		for (Long l : _times) {
			total += l;
		}
		long avg = (_times.size() == 0 ? 0 : total / _times.size());
		if (dump && _writer != null) {
			writeln("Average : " + avg);
		}
		return avg;
	}

	/**
	 * @return the running tally of elapsed times
	 */
	public List<Long> getElapsedTimes() {
		return _times;
	}

}
