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

import java.util.ArrayList;
import java.util.List;

import au.com.intermine.spacemap.IAsyncCallback;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.scanner.filter.IFileFilter;

public class ScanningEngine {

	private ScanTarget _target;

	private List<String> _roots;

	private ThreadPool _threadpool;

	private boolean _running;

	private IFileFilter _filter;

	private List<IScanningEngineObserver> _observers;

	private ScanningEngineThread _controller;

	public ScanningEngine(ScanTarget target, IFileFilter filter, int threadcount) {
		_threadpool = new ThreadPool(threadcount);
		_target = target;
		_roots = target.getRoots();
		_running = false;
		_filter = filter;
		_observers = new ArrayList<IScanningEngineObserver>();
	}

	public ScanTarget getTarget() {
		return _target;
	}

	public List<String> getRoots() {
		return _roots;
	}

	public void addObserver(IScanningEngineObserver observer) {
		_observers.add(observer);
	}

	public void cancel() {
		if (_controller != null && _controller.isAlive()) {
			_controller.cancel();
		}
	}

	void notifyStarted() {
		_running = true;
		for (IScanningEngineObserver observer : _observers) {
			observer.scanStarted();
		}
	}

	void notifyFinished(boolean cancelled) {
		_running = false;
		for (IScanningEngineObserver observer : _observers) {
			observer.scanFinished(cancelled);
		}
	}

	public TreeNode startScan(IAsyncCallback callback) {

		assert _roots != null;
		assert _roots.size() > 0;
		assert !_running;

		String label = _roots.get(0);
		if (_roots.size() > 1) {
			try {
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				label = localMachine.getHostName();
			} catch (java.net.UnknownHostException uhe) {
			}
		}
		_controller = new ScanningEngineThread(this, callback);
		List<TreeNode> rootNodes = _controller.getRootNodes();
		TreeNode model = null;
		if (rootNodes.size() == 1) {
			model = rootNodes.get(0);
		} else {
			model = new TreeNode(label, 0, NodeType.Container, -1);
			for (TreeNode rootnode : rootNodes) {
				model.addChild(rootnode);
			}
		}
		_controller.start();

		return model;
	}

	ThreadPool getThreadPool() {
		return _threadpool;
	}

	public boolean isRunning() {
		return _running;
	}

	public ScannerStatistics getStatistics() {
		if (_controller != null) {
			return _controller.getStatistics();
		} else {
			return new ScannerStatistics();
		}
	}

	public IFileFilter getFilter() {
		return _filter;
	}

}

class ScanningEngineThread extends Thread {
	private ScanningEngine _engine;
	private IAsyncCallback _callback;
	private IFileFilter _filter;
	private List<ScanningTask> _tasks;
	private List<TreeNode> _rootNodes;
	private boolean _cancelled = false;

	public ScanningEngineThread(ScanningEngine engine, IAsyncCallback callback) {
		_engine = engine;
		_filter = engine.getFilter();
		_callback = callback;
		_tasks = new ArrayList<ScanningTask>();
		_rootNodes = new ArrayList<TreeNode>();
		for (String root : engine.getRoots()) {
			FileSystemScanner scanner = new FileSystemScanner(root, _filter);
			
			_rootNodes.add(scanner.getRootNode());
			_tasks.add(new ScanningTask(scanner));
		}
	}

	public void cancel() {
		_cancelled = true;
		for (ScanningTask task : _tasks) {
			task.cancel();
		}
	}

	public List<TreeNode> getRootNodes() {
		return _rootNodes;
	}

	@Override
	public void run() {
		try {
			_engine.notifyStarted();
			for (ScanningTask task : _tasks) {
				if (!_cancelled) {
					_engine.getThreadPool().runTask(task, null);
				}
			}
			_engine.getThreadPool().waitUntilIdle();
			if (_callback != null) {
				_callback.onComplete(null);
			}
			_engine.notifyFinished(_cancelled);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public ScannerStatistics getStatistics() {
		ScannerStatistics stats = new ScannerStatistics();
		int totalpercent = 0;
		for (ScanningTask task : _tasks) {
			ScannerStatistics taskstats = task.getScanner().getStatistics();
			if (taskstats != null) {
				stats.addAllStatistics(taskstats);
				totalpercent += taskstats.getPercentComplete();
			}
		}
		stats.setPercentComplete(totalpercent / _tasks.size());
		return stats;
	}
}

class ScanningTask implements IThreadPoolTask {

	private FileSystemScanner _scanner;

	public ScanningTask(FileSystemScanner scanner) {
		_scanner = scanner;
	}

	public void cancel() {
		if (_scanner != null) {
			_scanner.cancel();
		}
	}

	public Object run() {
		// Get a list of checkpoints to track progress...
		DepthLimitedScanner dls = new DepthLimitedScanner(_scanner.getRoot(), 2);
		_scanner.setProgressProvider(new CheckpointProgressProvider(dls.getLeafFolders()));
		_scanner.scan();
		return null;
	}

	public FileSystemScanner getScanner() {
		return _scanner;
	}

}
