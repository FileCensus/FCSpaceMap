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

package au.com.intermine.spacemap.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.com.intermine.spacemap.action.ToggleFreeSpaceAction;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.scanner.filter.IFileFilter;
import au.com.intermine.spacemap.util.OSUtils;

public class FileSystemScanner {

	private String _root;

	private List<IScannerObserver> _observers;

	private TreeNode _rootnode;

	private IFileFilter _filter;

	private boolean _cancelled = false;

	private ScannerStatistics _stats;

	private IScannerProgressProvider _progress = null;
	
	public FileSystemScanner(String root, IFileFilter filter) {
		_root = root;
		_observers = new ArrayList<IScannerObserver>();
		_rootnode = new TreeNode(root, 0, NodeType.Folder, -1);
		_filter = filter;
	}

	public void cancel() {
		_cancelled = true;
	}
	
	public void addObserver(IScannerObserver observer) {
		_observers.add(observer);
	}

	public void scan() {
		_stats = new ScannerStatistics();
		File f = new File(_root);
		scanFolder(f, _rootnode);
	}

	private void notifyOnFolder(File folder, TreeNode node) {
		for (IScannerObserver observer : _observers) {
			observer.onFolder(folder, node);
		}
	}

	private void notifyOnFile(File file, TreeNode node) {
		if (_cancelled) {
			return;
		}
		for (IScannerObserver observer : _observers) {
			observer.onFile(file, node);
		}
	}

	private void scanFolder(File folder, TreeNode parent) {
		assert (folder.isDirectory());

		if (_cancelled) {
			return;
		}

		_stats.incrementTotalDirectories();

		if (folder.getParentFile() == null) {
			// this is a root node
			TreeNode freeSpaceNode = parent.addChild("Free space", folder.getFreeSpace(), NodeType.FreeSpace, -1);
			if (!ToggleFreeSpaceAction.isShowingFreeSpace()) {
				ToggleFreeSpaceAction.hideFreeSpaceNode(freeSpaceNode);
			}
		}

		notifyOnFolder(folder, parent);

		ArrayList<File> folders = new ArrayList<File>();
		String children[] = folder.list();
		if (children != null) {
			for (String child : children) {
				String fullpath = folder.getAbsolutePath() + "/" + child;
				File f = new File(fullpath);
				
				boolean add = false;
				if (_filter != null) {
					if (_filter.accept(f)) {
						add = true;
					}
				} else {
					add = true;
				}
				
				if (add) {
					if (f.isDirectory()) {
						folders.add(f);						
					} else {
						_stats.incrementFiles(add);
						TreeNode filenode = parent.addChild(f.getName(), f.length(), NodeType.File, -1);
						notifyOnFile(f, filenode);
					}
				}
			}			
		}

		if (_progress != null) {
			_stats.setPercentComplete(_progress.getPercentComplete(folder, _stats.getTotalFiles(), _stats.getTotalDirectories()));
		}

		if (!_cancelled) {
			for (File childfolder : folders) {
				if (!OSUtils.isLink(childfolder)) {
					TreeNode childnode = parent.addChild(childfolder.getName(), 0, NodeType.Folder, -1);
					scanFolder(childfolder, childnode);
				}
			}
		}
		
	}

	public TreeNode getRootNode() {
		return _rootnode;
	}

	public boolean wasCancelled() {
		return _cancelled;
	}

	public ScannerStatistics getStatistics() {
		return _stats;
	}

	public String getRoot() {
		return _root;
	}

	public void setProgressProvider(IScannerProgressProvider provider) {
		_progress = provider;
	}

}
