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

package au.com.intermine.spacemap.jobqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author baird
 * 
 */
public class JobQueue {

	/** Singleton instance */
	private static JobQueue _instance;

	/** static initializer */
	static {
		_instance = new JobQueue(1);
	}

	/** the actual queue */
	private LinkedBlockingQueue<Runnable> _queue;
	
	private List<Runnable> _jobBoard;

	/** The thread pool that will service the queue */
	private ThreadPoolExecutor _executor;

	/**
	 * Private ctor
	 */
	public JobQueue(int threadcount) {
		_queue = new LinkedBlockingQueue<Runnable>();
		_executor = new ThreadPoolExecutor(threadcount, threadcount, 0L, TimeUnit.MILLISECONDS, _queue);
		_jobBoard = new ArrayList<Runnable>();
	}

	/** singleton accessor method */
	public static JobQueue getInstance() {
		return _instance;
	}

	public synchronized void pushJob(Runnable job) {
		_executor.submit(new RunnableJobAdapter(job));
	}

	public static void pushGlobalJob(Runnable job) {
		getInstance().pushJob(job);
	}

	public synchronized void clear() {
		List<Runnable> drained = new ArrayList<Runnable>();
		_queue.drainTo(drained);
	}

	public synchronized void waitForQueueToEmpty() {
		try {
			_executor.awaitTermination(0, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public int getSize() {
		return _queue.size();
	}

	public synchronized boolean contains(Runnable job) {
		return _jobBoard.contains(job);
	}
	
	private synchronized void registerJob(Runnable job) {
	    _jobBoard.add(job);
	}
	
	private synchronized void unregisterJob(Runnable job) {	   
	    _jobBoard.remove(job);
	}
	
	class RunnableJobAdapter implements Runnable {
	    
	    private Runnable _realJob;

        public RunnableJobAdapter(Runnable realJob) {
	        _realJob = realJob;
	        registerJob(realJob);
	    }

        public void run() {
            try {
                _realJob.run();
            } finally {
                unregisterJob(_realJob);
            }
        }
	    
	}

}


