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

package au.com.intermine.spacemap.treemap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.VolatileImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import au.com.intermine.spacemap.SaveImageDialog;
import au.com.intermine.spacemap.exception.SystemFatalException;
import au.com.intermine.spacemap.model.ITreeNodeVisitor;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.model.visitor.FindClusteredNodesVisitor;
import au.com.intermine.spacemap.model.visitor.SelectedNodeAccumulator;
import au.com.intermine.spacemap.model.visitor.SetSelectionVisitor;
import au.com.intermine.spacemap.model.visitor.UnknownAreaCounter;
import au.com.intermine.spacemap.treemap.split.SplitSquarified;
import au.com.intermine.spacemap.treemap.split.SplitStrategy;
import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.ImageSource;
import au.com.intermine.spacemap.util.NumberType;
import au.com.intermine.spacemap.util.Utils;

/**
 * 
 * @author djb
 */
public class TreeMapWidget extends JPanel implements Printable, ImageSource {

	private static final long serialVersionUID = 7255952672238300249L;

	private static final Color BRBorderColor = new Color(0, 0, 0, 50);

	private static final Color TLBorderColor = new Color(255, 255, 255, 150);

	private static final Color OutsideBorderColor = new Color(0, 0, 0, 100);

	public static final int MIN_RECT_SIZE = 2;

	/** the node which is currently being displayed as the root */
	private TreeNode _displayedRoot = null;

	/** the root of the model */
	private TreeNode _root = null;

	/** listeners */
	private List<VisualisationListener> _listeners;

	/** the currently hovered node */
	private TreeNode _activeLeaf = null;

	/** the currently hovered branch node */
	private TreeNode _activeBranch = null;

	/** the class responsible for zooming in and out */
	private TreeMapZoom _zoom;

	/** the threshold percentage of unknown data present before triggering a further data request */
	private int _unknownthreshold = -1;

	private VisualisationRenderStrategy _renderStrategy = new FileSizeRenderStrategy(1024, NumberType.Bytes);

	private Font _labelFont;

	private List<TreeNode> _highlightedNodes = new ArrayList<TreeNode>();

	private List<TreeNode> _clearList = new ArrayList<TreeNode>();

	private BasicStroke _Selected_Stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

	private BasicStroke _Highlighted_Stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 1 }, (float) 0);

	protected BannerRenderer _banner;

	private SplitStrategy _strategy = null;

	private ArrayList<String> _zoomcache = new ArrayList<String>();

	private NodeImagePair _bufferedImage;

	private Image _oneShotRender;

	private long _lastnodecount = 0;

	private long _lastselectionhash = 0;

	protected boolean _headless = false;

	private boolean _lastPaintOk = false;

	private boolean _busyMode = false;

	private String _busyMessage = "";

	/**
	 * ctor
	 */
	public TreeMapWidget() {
		_listeners = new ArrayList<VisualisationListener>();
		_zoom = createZoom();
		_labelFont = new JLabel().getFont();
		addListeners();
		setSplitStrategy(new SplitSquarified());
		setFontSize(10);
	}

	public void prepareAsyncRender() {
		int width = getWidth();
		int height = getHeight();
		_oneShotRender = createVolatileImage(width, height);
		drawVisualisation(_oneShotRender.getGraphics(), width, height, 10);
	}

	private void addListeners() {
		addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					if (getActiveNode() != null) {
						_zoom.zoomTowards(getActiveNode());
					}
				} else {
					_zoom.unzoom();
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
		    
		    @Override
		    public void mouseDragged(MouseEvent e) {
		        raiseMouseDragged(e);
		    }

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!lastPaintOk() || getRootNode() == null || getDisplayedRoot() == null) {
					return;
				}

				if (getDisplayedRoot().getChildren().size() > 0) {
					TreeNode t = getNodeAt(e.getX(), e.getY());
					if (t != null) {
						if (t.isLeaf()) {
							setActiveLeaf(t);
						} else {
							setActiveLeaf(null);
							setActiveBranch(t);
						}
					}
					TreeNode active = getActiveNode();
					if (active != null) {
						raiseMouseOverNode(active);
					}

					if (!_highlightedNodes.contains(t)) {
						highlightNode(t);
					}
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				// Glass panes do funny things with mouseExited events. The event will fire even if the glass pane is active
				// (which is actually correct according to the even contract, but is not actually want we want (i.e. we want
				// to suppress the event if there is a glass pane active
				Component c = Utils.getParentGlassPane(TreeMapWidget.this);
				if (c != null && !c.isVisible()) {
					_activeLeaf = null;
					_activeBranch = null;
					highlightNode(null);
					raiseMouseExited(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				TreeNode node = getActiveNode();
				if (node == null || !lastPaintOk()) {
					return;
				}
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
					selectNode(node, !node.isSelected());
					return;
				}
				if (e.getButton() == 1) {
					if (e.getClickCount() == 1) {
						if (!node.isLeaf()) {
							if (getActiveBranch() != null) {
								getZoom().zoomTo(getActiveBranch());
							}
						}
					} else if (e.getClickCount() == 2) {
						if (node.getNodeType() == NodeType.Unknown) {
							raiseRequireMoreData(node);
						}
					}
				}
				
				raiseMouseUp(e, getActiveNode());
			}


			@Override
			public void mouseClicked(MouseEvent e) {
				TreeNode node = getActiveNode();
				raiseNodeClicked(e, node);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				raiseMouseDown(e, getActiveNode());
			}

		});
	}

	public void addKeyAction(String keystroke, Action action) {
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		String actionkey = keystroke + "_action";
		inputMap.put(KeyStroke.getKeyStroke(keystroke), actionkey);
		getActionMap().put(actionkey, action);
	}

	public void selectNode(TreeNode node, boolean select) {
		selectNode(node, select, true);
	}

	public void selectNode(TreeNode node, boolean select, boolean drawborder) {
		if (!node.isSelected()) {
			// List<TreeNode> selected = getSelectedNodes();
			// Got to remove any of my parents from the selected list
			TreeNode parent = node.getParent();
			while (parent != null) {
				if (parent.isSelected()) {
					parent.setSelected(false);
				}
				parent = parent.getParent();
			}

			// And I also have to check to see if any of my children are selected as well - we can use the clear list as a shortcut...
			for (TreeNode snode : _clearList) {
				if (snode.isChildOf(node) && snode.isSelected()) {
					snode.setSelected(false);
				}
			}
		}

		node.setSelected(select);

		if (drawborder) {
			highlightNode(node);
			raiseSelectionChanged(node);
		}
	}

	public void highlightNode(TreeNode highlightednode) {
		// First restore any currently highlighted nodes (i.e. clear their borders back to the default)
		for (TreeNode n : _highlightedNodes) {
			paintNodeBorder(n, false);
		}

		for (TreeNode n : _clearList) {
			if (!n.isSelected()) {
				paintNodeBorder(n, false);
			}
		}

		_highlightedNodes.clear();

		if (highlightednode == null) {
			return;
		}

		if (highlightednode.getCluster() > 0) {
		    FindClusteredNodesVisitor v = new FindClusteredNodesVisitor(highlightednode.getCluster());
		    _root.traverse(v);
		    _highlightedNodes.addAll(v.getClusterNodes());
		} else {
		    _highlightedNodes.add(highlightednode);
		}

		for (TreeNode n : _highlightedNodes) {
			paintNodeBorder(n, true);
		}

		// Now repaint the borders of all the selected nodes
		_clearList.clear();
		List<TreeNode> selected = getSelectedNodes();
		for (TreeNode n : selected) {
			paintNodeBorder(n, false);
			_clearList.add(n);
		}
	}

	public long sumSelectedWeight() {
		long result = 0;
		for (TreeNode n : getSelectedNodes()) {
			result += n.getWeight();
		}
		return result;
	}

	public long sumHighlightedWeight() {
		long result = 0;
		for (TreeNode n : _highlightedNodes) {
			result += n.getWeight();
		}
		return result;
	}

	public void setLabelFont(Font f) {
		_labelFont = f;
	}

	public Font getLabelFont() {
		return _labelFont;
	}

	public void setFontSize(int size) {
		_labelFont = _labelFont.deriveFont((float) size);
	}

	public List<TreeNode> getSelectedNodes() {
		if (_root == null) {
			return new ArrayList<TreeNode>();
		}
		SelectedNodeAccumulator v = new SelectedNodeAccumulator();
		_root.traverse(v);
		return v.getNodes();
	}

	public List<TreeNode> getHighlightedNodes() {
		return _highlightedNodes;
	}

	public void clearSelectedNodes() {
		if (_root == null) {
			return;
		}
		SetSelectionVisitor v = new SetSelectionVisitor(false);
		_root.traverse(v);
		raiseSelectionChanged(getActiveNode());
		highlightNode(getActiveNode());
	}

	public TreeMapZoom getZoom() {
		return _zoom;
	}

	public void print(BannerRenderer banner) {
		_banner = banner;
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(this);
		try {
			if (job.printDialog()) {
				Utils.cursorWait(this);
				job.print();
			}
		} catch (Exception ex) {
			new SystemFatalException(ex);
		} finally {
			Utils.cursorDefault(this);
			repaint();
		}
	}

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		Utils.cursorWait(this);
		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
		}
		try {
			if (_banner == null) {
				_banner = new BannerRenderer("", "Icons/Other/intermine3.png");
			}
			Graphics2D g2d = (Graphics2D) graphics;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			int width = (int) pageFormat.getImageableWidth();
			int height = (int) pageFormat.getImageableHeight();
			_banner.setFontSize(8);
			_banner.drawBanner(g2d, width, height);
			height = height - _banner.getHeight();
			g2d = (Graphics2D) g2d.create(0, _banner.getHeight(), width, height);
			drawVisualisation(g2d, width, height, 8);
			return PAGE_EXISTS;
		} finally {
			Utils.cursorDefault(this);
		}
	}

	public void drawImage(Graphics g, int width, int height) {
		drawVisualisation(g, width, height, 10);
	}

	public Dimension getDefaultDimension() {
		return getSize();
	}

	public void saveAsImage(BannerRenderer banner) {
		SaveImageDialog frm = new SaveImageDialog((JFrame) this.getParent(), this, banner);
		frm.setModal(true);
		frm.setVisible(true);
	}

	public void setResolveThreshold(int threshold) {
		_unknownthreshold = threshold;
	}

	public int getResolveThreshold() {
		return _unknownthreshold;
	}

	public TreeNode getActiveBranch() {
		return _activeBranch;
	}

	public void setActiveBranch(TreeNode node) {
		_activeBranch = node;
	}

	public TreeNode getActiveLeaf() {
		return _activeLeaf;
	}

	public TreeNode getActiveNode() {
		if (_activeLeaf != null) {
			return _activeLeaf;
		}
		return _activeBranch;
	}

	protected void setActiveLeaf(TreeNode newActiveLeaf) {
		if (newActiveLeaf == null || newActiveLeaf.isLeaf()) {
			_activeLeaf = newActiveLeaf;
			if (newActiveLeaf != null) {
				_activeBranch = newActiveLeaf.getParent();
			}
		}
	}

	public TreeNode findNode(String nodepath) {
		return findNode(nodepath, null);
	}

	public TreeNode findNode(String nodepath, TreeNodeResolver resolver) {
		TreeNode node = getModel();
		if (node != null) {
			ArrayList<String> bits = new ArrayList<String>(Arrays.asList(nodepath.split("[/]")));
			if (bits.size() > 0) {
				// The first bit should match the root label
				if (bits.get(0).equals("") && !node.getLabel().equals("")) {
					bits.remove(0);
				}
				if (bits.get(0).equals(node.getLabel())) {
					bits.remove(0);
				}

				for (String bit : bits) {
					TreeNode child = node.getChildNode(bit);
					if (child == null) {
						if (resolver != null) {
							// Check to see if there are any unresolved nodes under the parent...
							if (node.hasChildWithNodeType(NodeType.Unknown)) {
								resolver.resolveNode(node);
								// Now try again - we'll only try once
								child = node.getChildNode(bit);
							}
						}
						// if child is still null at this pint we have to return the parent (can't go any further down the node path
						if (child == null) {
							return node;
						}
					} else {
						node = child;
					}
				}
			}
		}
		return node;
	}

	public TreeNode getRootNode() {
		return _root;
	}

	public void setModel(TreeNode model) {
		try {
			_root = model;
			setDisplayedRoot(_root);
			_bufferedImage = null;
			myPaint(getGraphics());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public TreeNode getModel() {
		return _root;
	}

	public TreeNode getDisplayedRoot() {
		return _displayedRoot;
	}

	public void setRenderStrategy(VisualisationRenderStrategy s) {
		if (s == null) {
			s = new FileSizeRenderStrategy(1024, NumberType.Bytes);
		}
		_renderStrategy = s;
		repaint();
	}

	public VisualisationRenderStrategy getRenderStrategy() {
		return _renderStrategy;
	}

	public TreeNode getNodeByNodePath(List<String> nodepath) {
		if (nodepath == null) {
			return getRootNode();
		}
		// First identify the target node...
		TreeNode node = getRootNode();
		nodepath = nodepath.subList(1, nodepath.size());

		for (String s : nodepath) {
			node = (TreeNode) node.getChildNode(s);
			if (node == null) {
				return getRootNode();
			}
		}
		return node;
	}

	public void resolve(TreeNode node) {
		if (node != null) {
			raiseRequireMoreData(node);
		}
	}

	public void setDisplayedRoot(TreeNode newDisplayedRoot) {
		_displayedRoot = newDisplayedRoot;
		raiseDisplayRootChanged(_displayedRoot);
		repaint();
	}

	public void addVisualisationListener(VisualisationListener l) {
		if (!_listeners.contains(l)) {
			_listeners.add(l);
		}
	}

	public void removeVisualisationListener(VisualisationListener l) {
		if (_listeners.contains(l)) {
			_listeners.remove(l);
		}
	}

	public void traverseAllNodes(ITreeNodeVisitor visitor) {
		traverseAllNodes(visitor, getRootNode());
	}

	public void traverseAllNodes(ITreeNodeVisitor visitor, TreeNode startfrom) {
		if (startfrom != null) {
			startfrom.traverse(visitor);
		}
	}

	public void traverseLeafNodes(ITreeNodeVisitor visitor) {
		traverseLeafNodes(visitor, getRootNode());
	}

	public void traverseLeafNodes(ITreeNodeVisitor visitor, TreeNode startfrom) {
		if (startfrom != null) {
			startfrom.traverseLeafNodes(visitor);
		}
	}

	protected void raiseNodeClicked(MouseEvent e, TreeNode node) {
		for (VisualisationListener l : _listeners) {
			l.nodeClicked(e, node);
		}
	}
	
	protected void raiseMouseUp(MouseEvent e, TreeNode activeNode) {
		for (VisualisationListener l : _listeners) {
			l.mouseUp(e, activeNode);
		}
	}

	protected void raiseMouseDown(MouseEvent e, TreeNode activeNode) {
		for (VisualisationListener l : _listeners) {
			l.mouseDown(e, activeNode);
		}
	}

	protected void raiseRequireMoreData(TreeNode node) {
		for (VisualisationListener l : _listeners) {
			l.requireMoreData(node);
		}
	}

	protected void raiseDisplayRootChanged(TreeNode node) {
		for (VisualisationListener l : _listeners) {
			l.displayRootChanged(node);
		}
	}

	protected void raiseMouseOverNode(TreeNode node) {
		for (VisualisationListener l : _listeners) {
			l.mouseOverNode(node);
		}
	}
	
    protected void raiseMouseDragged(MouseEvent e) {
        for (VisualisationListener l : _listeners) {
            l.mouseDragged(e);
        }
    }
	

	protected void raiseMouseExited(MouseEvent e) {
		for (VisualisationListener l : _listeners) {
			l.mouseExited(e);
		}
	}

	protected void raiseSelectionChanged(TreeNode activenode) {
		for (VisualisationListener l : _listeners) {
			l.selectionChanged(activenode);
		}
	}

	/**
	 * Calculate each node's position in the treemap
	 */
	private void calculatePositions() {
		if (getStrategy() != null && getDisplayedRoot() != null) {
			getStrategy().calculatePositions(getDisplayedRoot());
		}
	}

	@Override
	public void paint(Graphics g) {
		myPaint(g);
	}

	public void startBusyMode(String message) {
		_busyMode = true;
		_busyMessage = message;
		forceRepaint();
	}

	public void stopBusyMode() {
		_busyMode = false;
		_busyMessage = "";
	}

	private void myPaint(Graphics g) {
		if (g == null) {
			return;
		}
		if (_oneShotRender != null) {
			Rectangle r = g.getClipBounds();
			if (r != null) {
				g.drawImage(_oneShotRender, r.x, r.y, r.x + r.width, r.y + r.height, r.x, r.y, r.x + r.width, r.y + r.height, null);
			}
			_oneShotRender = null;
			return;
		}

		int width = getWidth();
		int height = getHeight();

		if (_busyMode) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			drawBorder(g2d, 0, 0, width, height, null);
			JLabel lbl = new JLabel();
			g.setColor(Color.black);
			DrawingUtils.drawString(g, lbl.getFont(), _busyMessage, 0, 0, width, height, DrawingUtils.TEXT_ALIGN_CENTER);
			return;
		}

		try {
			boolean repaint = false;
			if (_bufferedImage == null || _bufferedImage.getKey() != getDisplayedRoot()) {
				repaint = true;
			} else {
				Image img = _bufferedImage.getValue();
				if (img.getHeight(null) != height || img.getWidth(null) != width) {
					repaint = true;
				} else {
					if (img instanceof VolatileImage) {
						VolatileImage vimg = (VolatileImage) img;
						if (vimg.contentsLost()) {
							repaint = true;
						}
					}
				}
			}

			if (!repaint) {
				long selectionHash = calculateSelectionHash();
				if (selectionHash != _lastselectionhash) {
					repaint = true;
					_lastselectionhash = selectionHash;
				}
			}

			// the last test -
			if (!repaint && getDisplayedRoot() != null) {
				long currentchildcount = getDisplayedRoot().countAllChildNodes();
				if (currentchildcount != _lastnodecount) {
					repaint = true;
					_lastnodecount = currentchildcount;
				}
			}

			if (repaint) {
				VolatileImage img = createVolatileImage(width, height);
				drawVisualisation(img.getGraphics(), width, height, 10);
				_bufferedImage = new NodeImagePair(getDisplayedRoot(), img);
				g.drawImage(_bufferedImage.getValue(), 0, 0, width, height, null);

				PointerInfo pi = MouseInfo.getPointerInfo();
				Point p = (pi == null ? null : pi.getLocation());
				if (p != null) {
					SwingUtilities.convertPointFromScreen(p, this);
					TreeNode t = getNodeAt(p.x, p.y);
					if (t != null) {
						if (t.isLeaf()) {
							setActiveLeaf(t);
						} else {
							setActiveLeaf(null);
							setActiveBranch(t);
						}
						TreeNode active = getActiveNode();
						if (active != null) {
							raiseMouseOverNode(active);
						}
					}
				}
			} else {
				Rectangle r = g.getClipBounds();
				g.drawImage(_bufferedImage.getValue(), r.x, r.y, r.x + r.width, r.y + r.height, r.x, r.y, r.x + r.width, r.y + r.height, null);
			}
			_lastPaintOk = true;
		} catch (OutOfMemoryError ofme) {
			// just draw a box with the words 'no data' in the centre...
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			drawBorder(g2d, 0, 0, width, height, null);
			JLabel lbl = new JLabel();
			String caption = "Image too large";
			g.setColor(Color.black);
			DrawingUtils.drawString(g, lbl.getFont(), caption, 0, 0, width, height, DrawingUtils.TEXT_ALIGN_CENTER);
			_lastPaintOk = false;
			return;
		}
	}

	private long calculateSelectionHash() {
		return getSelectedNodes().hashCode();
	}

	@Override
	protected void paintComponent(Graphics g) {
		myPaint(g);
	}

	protected void drawVisualisation(Graphics g, int width, int height, int fontsize) {
		// clear the buffer
		_bufferedImage = null;

		setFontSize(fontsize);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		TreeNode root = getRootNode();
		TreeNode displayedroot = getDisplayedRoot();

		if (root == null) {
			// just draw a box with the words 'no data' in the centre...
			super.paintComponent(g);
			drawBorder(g2d, 0, 0, width, height, null);
			JLabel lbl = new JLabel();
			String caption = "No data";
			g.setColor(Color.black);
			DrawingUtils.drawString(g, lbl.getFont(), caption, 0, 0, width, height, DrawingUtils.TEXT_ALIGN_CENTER);
			return;
		}

		Insets insets = getInsets();

		TreeMapRectangle rect = (TreeMapRectangle) root.getRectangle();
		rect.setDimension(rect.getX(), rect.getY(), width - insets.left - insets.right, height - insets.top - insets.bottom);
		if (!root.equals(displayedroot)) {
			TreeMapRectangle displayrect = displayedroot.getRectangle();
			displayrect.setDimension(displayrect.getX(), displayrect.getY(), width - insets.left - insets.right, height - insets.top - insets.bottom);
		}

		calculatePositions();

		if (displayedroot.getChildren().size() > 0) {
			draw(g, displayedroot);
		}

		if (getResolveThreshold() >= 0) {
			UnknownAreaCounter v = new UnknownAreaCounter();
			traverseLeafNodes(v, displayedroot);
			long totalarea = getWidth() * getHeight();
			double percent = ((double) v.getArea() / (double) totalarea) * 100.0;
			if (percent > getResolveThreshold()) {

				if (!_zoomcache.contains(displayedroot.getAncestry().toString())) {
					// force a full repaint

					_zoomcache.add(displayedroot.getAncestry().toString());

					if (_headless) {
						_bufferedImage = null;
						raiseRequireMoreData(getDisplayedRoot());
						repaint();
					} else {
						// request more data...
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								_bufferedImage = null;
								raiseRequireMoreData(getDisplayedRoot());
								repaint();
							}
						});
					}

				}
			}
		}
	}

	/**
	 * Draw an individual node
	 * 
	 * @param g
	 *            graphics
	 * @param node
	 *            the node to draw
	 */
	protected void draw(Graphics g, TreeNode node) {

		TreeMapRectangle rect = (TreeMapRectangle) node.getRectangle();
		int x = rect.getX();
		int y = rect.getY();
		int width = rect.getWidth();
		int height = rect.getHeight();

		if (height < MIN_RECT_SIZE || width < MIN_RECT_SIZE) {
			return;
		}

		VisualisationRenderStrategy renderStrategy = getRenderStrategy();

		Graphics2D g2 = (Graphics2D) g;
		g2.setPaint(renderStrategy.getNodePaint(node));
		g.fillRect(x, y, width, height);

		drawBorder(g2, x, y, width, height, node);

		Font labelFont = getLabelFont();
		FontMetrics fm = g.getFontMetrics(labelFont);
		int titleheight = fm.getHeight() + 4;
		TreeMapRectangle._insets.top = titleheight;

		if (height > titleheight && width > 10) {
			if (node.isLeaf()) {
				g.setColor(renderStrategy.getLeafNodeTextColor(node));
				int heightadjust = 7;
				if (height > 64) {
					heightadjust += 32;
				}
				Rectangle lblrect = DrawingUtils.drawString(g, labelFont, renderStrategy.renderLeafNode(node), x + 3, y + 3, width - 7, height - heightadjust, DrawingUtils.TEXT_ALIGN_CENTER);
				// check to see if the text was actually drawn
				if (lblrect.height > 0) {
					// Is there space for an icon?
					if (y + height - (lblrect.y + lblrect.height) > 32 && width > 40) {
						ImageIcon icon = renderStrategy.getNodeIcon(node);
						if (icon != null) {
							g2.drawImage(icon.getImage(), x + (width / 2) - 16, lblrect.y + lblrect.height, 32, 32, null);
						}
					}
				}

			} else {
				g.setColor(renderStrategy.getBranchTitleTextColor(node));
				String lbl = renderStrategy.renderBranchTitle(node, node == getDisplayedRoot());
				int yoffset = 0;
				Rectangle lblrect = DrawingUtils.drawString(g, labelFont, lbl, x + 4, y + yoffset, width - 7, titleheight, DrawingUtils.TEXT_ALIGN_LEFT);
				int lblwidth = lblrect.width;
				if (lblwidth > 0 || lbl.equals("")) {
					// we might have space to draw the branch caption...
					if (width - lblwidth > 10) {
						g.setColor(renderStrategy.getBranchSummaryTextColor(node));
						String caption = renderStrategy.renderBranchSummary(node);
						DrawingUtils.drawString(g, labelFont, caption, x + lblwidth + 5, y + yoffset, width - (lblwidth + 10), titleheight, DrawingUtils.TEXT_ALIGN_RIGHT);
					}
				}
				for (int i = 0; i < node.getChildren().size(); ++i) {
					TreeNode child = node.getChildren().get(i);
					draw(g, child);
				}
			}
		}
	}

	/**
	 * Draw a border around a node...
	 * 
	 * @param g
	 *            graphics
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void drawBorder(Graphics2D g, int x, int y, int width, int height, TreeNode n) {

		if (height < 4 || width < 4) {
			return;
		}

		if (n != null && n.isSelected()) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			Stroke oldstroke = g.getStroke();
			g.setColor(Color.BLACK);
			g.setStroke(_Selected_Stroke);
			g.drawRect(x + 1, y + 1, width - 2, height - 2);
			g.setStroke(oldstroke);
		} else {
			g.setColor(Color.white);
			g.drawRect(x, y, width - 1, height - 1);
			g.drawRect(x + 1, y + 1, width - 3, height - 3);
			g.setColor(OutsideBorderColor);
			g.drawRect(x, y, width - 1, height - 1);
			int x1 = x + 1;
			int y1 = y + 1;
			int x2 = x + width - 2;
			int y2 = y + height - 2;
			g.setColor(TLBorderColor);
			g.drawLine(x1, y1, x2, y1);
			g.drawLine(x1, y1, x1, y2);
			g.setColor(BRBorderColor);
			g.drawLine(x2, y1, x2, y2);
			g.drawLine(x1, y2, x2, y2);
		}
	}

	public SplitStrategy getStrategy() {
		return _strategy;
	}

	public void setSplitStrategy(SplitStrategy newStrat) {
		_strategy = newStrat;
	}

	protected TreeMapZoom createZoom() {
		return new TreeMapZoom(this);
	}

	public TreeNode getNodeAt(int x, int y) {
		TreeNode ret = null;
		TreeNode displayed = getDisplayedRoot();
		if (displayed != null) {
			TreeMapRectangle n = (TreeMapRectangle) displayed.getRectangle();
			ret = n.getNodeAt(x, y);
		}
		return ret;
	}

	protected void paintNodeBorder(TreeNode node, boolean highlighted) {

		if (node == null) {
			return;
		}

		if (!node.isChildOf(getDisplayedRoot())) {
			return;
		}

		Graphics2D g = (Graphics2D) getGraphics();
		if (g == null) {
			return;
		}
		TreeMapRectangle rect = (TreeMapRectangle) node.getRectangle();
		int x = rect.getX();
		int y = rect.getY();
		int width = rect.getWidth();
		int height = rect.getHeight();
		if (height < MIN_RECT_SIZE || width < MIN_RECT_SIZE) {
			return;
		}
		if (width >= 4 && height >= 4) {
			if (highlighted) {
				Stroke oldstroke = g.getStroke();
                if (node.getCluster() > 0) {
                    g.setColor(Color.black);
                    g.setStroke(_Selected_Stroke);
                } else {
                    g.setColor(new Color(0, 0, 0, 75));
                    g.setStroke(_Highlighted_Stroke);
                }				
				g.drawRect(x + 1, y + 1, width - 2, height - 2);
				g.setStroke(oldstroke);
			} else {
				drawBorder(g, x, y, width, height, node);
			}
		}
	}

	public void forceRepaint() {
		_bufferedImage = null;
		repaint();
	}

	protected boolean lastPaintOk() {
		return _lastPaintOk;
	}

}
