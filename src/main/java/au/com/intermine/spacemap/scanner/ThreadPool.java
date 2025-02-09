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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import au.com.intermine.spacemap.IAsyncCallback;

public class ThreadPool {

	private BlockingQueue<ThreadPoolThread> _threads;
	private int _poolsize;

	public ThreadPool(int threadcount) {
		_poolsize = threadcount;
		_threads = new ArrayBlockingQueue<ThreadPoolThread>(threadcount);
		for (int i = 0; i < threadcount; ++i) {
			ThreadPoolThread t = new ThreadPoolThread(this, "TheadPoolThread-" + i);
			t.start();
		}
	}

	public void runTask(IThreadPoolTask task, IAsyncCallback handler) {
		try {
			ThreadPoolThread t = _threads.take();
			synchronized (t) {
				t.setCurrentTask(task, handler);
				t.notify();
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

	}

	void returnToPool(ThreadPoolThread t) {
		try {
			_threads.put(t);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isIdle() {
		return _threads.size() == _poolsize;
	}

	public void waitUntilIdle() {
		while (!isIdle()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

}

class ThreadPoolThread extends Thread {

	private IThreadPoolTask _currentTask;
	private IAsyncCallback _asynchandler;
	private ThreadPool _threadpool;

	public ThreadPoolThread(ThreadPool pool, String name) {
		super(name);
		setDaemon(true);
		_threadpool = pool;
	}

	public void setCurrentTask(IThreadPoolTask task) {
		_currentTask = task;
		_asynchandler = null;
	}

	public void setCurrentTask(IThreadPoolTask task, IAsyncCallback notify) {
		_currentTask = task;
		_asynchandler = notify;
	}

	@Override
	public void run() {
		while (true) {
			synchronized (this) {
				try {
					_threadpool.returnToPool(this);
					this.wait();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}

			if (_currentTask != null) {
				try {
					Object result = _currentTask.run();
					if (_asynchandler != null) {
						_asynchandler.onComplete(result);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if (_asynchandler != null) {
						_asynchandler.onException(ex);
					}
				} finally {
					setCurrentTask(null, null);
				}
			} else {
				System.err.println("task is null!");
			}
		}
	}

}
