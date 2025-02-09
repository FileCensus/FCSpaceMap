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

package au.com.intermine.spacemap.glasspane;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileSystemView;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import org.jdesktop.animation.timing.Animator.Direction;

import au.com.intermine.spacemap.SpaceMap;
import au.com.intermine.spacemap.SpaceMapHelper;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.treemap.TreeMapRectangle;
import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.NumberRenderer;
import au.com.intermine.spacemap.util.NumberType;
import au.com.intermine.spacemap.util.Utils;

public abstract class AbstractGlassPane extends JComponent implements TimingTarget {

	private static final long serialVersionUID = 1L;

	protected TreeNode _node;

	protected File _file;

	protected Color _background = new Color(0, 0, 0, 200);

	private List<KeyAction> _keyactions;

	protected Color _statusPanelColor = new Color(0, 0, 0, 100);

	// private BasicStroke _lineStroke = new BasicStroke(2);
	protected static SimpleDateFormat _DateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	protected static NumberRenderer _NumberRenderer = new NumberRenderer(NumberType.Bytes, 1024);

	private static final int ANIMATION_DURATION = 100;

	protected Font _font;

	private List<GlassButton> _buttons;

	protected AbstractGlassPane(TreeNode node, File file) {
		_node = node;
		_file = file;
		_keyactions = new ArrayList<KeyAction>();
		_buttons = new ArrayList<GlassButton>();
		GlassPaneListener listener = new GlassPaneListener(this, SpaceMap.getInstance().getContentPane(), node);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		addKeyListener(listener);
		addMouseWheelListener(listener);
		_font = new JLabel().getFont().deriveFont(Font.BOLD);

	}

	protected void addKeyAction(KeyAction action) {
		_keyactions.add(action);
	}

	protected void addButton(GlassButton button) {
		synchronized (_buttons) {
			_buttons.add(button);
		}
	}
	
	protected void clearButtons() {
		synchronized (_buttons) {
			_buttons.clear();
		}
	}

	protected void paintButtons(Graphics2D g) {
		synchronized (_buttons) {
			for (GlassButton b : _buttons) {
				b.paint(g, getWidth(), getHeight());
			}
		}
	}

	public void open() {
		setVisible(true);
		Animator anim = new Animator(ANIMATION_DURATION, this);
		anim.addTarget(new TimingTargetAdapter() {
			@Override
			public void end() {
				requestFocus();
			}
		});

		anim.start();
	}

	public void close() {
		Animator anim = new Animator(ANIMATION_DURATION, this);
		anim.setStartFraction(1);
		anim.setStartDirection(Direction.BACKWARD);
		anim.addTarget(new TimingTargetAdapter() {
			@Override
			public void end() {
				setVisible(false);
			}
		});
		anim.start();
	}

	public abstract class KeyAction {

		private int _keycode;

		public KeyAction(int keycode) {
			_keycode = keycode;
		}

		public int getKeyCode() {
			return _keycode;
		}

		public abstract void execute();

	}

	protected void highlightNode(Graphics2D g, TreeNode node) {
		TreeMapRectangle rect = node.getRectangle();
		Point nodeTopLeft = SwingUtilities.convertPoint(SpaceMap.getVisualisation().getWidget(), rect.getX(), rect.getY(), this);
		Rectangle nrect = new Rectangle(nodeTopLeft.x, nodeTopLeft.y, rect.getWidth(), rect.getHeight());
		g.setStroke(new BasicStroke(3));
		g.setColor(Color.red);
		g.drawRect(nrect.x, nrect.y, nrect.width, nrect.height);
	}

	protected Rectangle drawStatusBackground(Graphics2D g, boolean drawAtBottom) {
		g.setColor(_statusPanelColor);
		int width = (int) (getWidth() * 0.95);
		int yoffset = 10;
		int x = (getWidth() - width) / 2;
		int height = 70;
		// Work out if this will overlap with the menu circle, if so, move it..
		// Start out at the bottom
		int y = getHeight() - height - yoffset;

		if (!drawAtBottom) {
			y = yoffset;
		}

		g.fillRoundRect(x, y, width, height, 20, 20);

		return new Rectangle(x, y, width, height);

	}

	protected void drawStatusPanel(Graphics2D g, TreeNode node, boolean drawAtBottom) {
		Rectangle statusRect = drawStatusBackground(g, drawAtBottom);

		ImageIcon icon = SpaceMapHelper.getNodeIcon(node);
		int txtoffset = 10;
		if (icon != null) {
			g.drawImage(icon.getImage(), statusRect.x + 10, statusRect.y + (statusRect.height / 2) - (icon.getIconHeight() / 2), null);
			txtoffset += icon.getIconWidth() + 5;
		}

		File f = Utils.getFileFromTreeNode(node);
		String lastmodified = _DateFormatter.format(new Date(f.lastModified()));

		String filetype = "";
		if (f.exists()) {
			if (f.isDirectory()) {
				int filecount = 0;
				int foldercount = 0;
				for (TreeNode child : node.getChildren()) {
					if (child.getNodeType() == NodeType.File) {
						filecount++;
					} else if (child.getNodeType() == NodeType.Folder) {
						foldercount++;
					}
				}
				filetype = String.format("(Folder with %d files and %d subfolders)", filecount, foldercount);
			} else {
				// get the collection this file would belong in?
				FileSystemView view = FileSystemView.getFileSystemView();
				filetype = "(" + view.getSystemTypeDescription(f) + ")";
			}
		}

		String caption = String.format("%s\n%s %s\nLast Modified:%s", f.getAbsolutePath(), _NumberRenderer.render(node.getWeight()), filetype, lastmodified);

		g.setColor(Color.white);
		DrawingUtils.drawString(g, _font, caption, statusRect.x + txtoffset, statusRect.y, statusRect.width - txtoffset, statusRect.height, DrawingUtils.TEXT_ALIGN_LEFT);

	}

	/**
	 * Listen for all events on the glass pane, and basically ignore all but a few
	 */
	class GlassPaneListener extends MouseInputAdapter implements KeyListener {

		private AbstractGlassPane _glassPane;

		private TreeNode _node;

		public GlassPaneListener(AbstractGlassPane glassPane, Container contentPane, TreeNode node) {
			this._glassPane = glassPane;
			_node = node;
		}

		public TreeNode getNode() {
			return _node;
		}

		public void mouseMoved(MouseEvent e) {
			handleMovingEvent(e);
			_glassPane.mouseMoved(e);
		}

		public void mouseDragged(MouseEvent e) {
			handleMovingEvent(e);
			_glassPane.mouseDragged(e);
		}

		private void handleMovingEvent(MouseEvent e) {
			GlassButton old = _glassPane.getHoveredButton();

			synchronized (_buttons) {
				for (GlassButton b : _buttons) {
					b.setHovering(b.contains(e.getPoint()));
				}
			}
			redispatchMouseEvent(e, old != _glassPane.getHoveredButton());
		}

		public void mouseClicked(MouseEvent e) {
			redispatchMouseEvent(e, false);
			_glassPane.mouseClicked(e);
		}

		public void mouseEntered(MouseEvent e) {
			redispatchMouseEvent(e, false);
		}

		public void mouseExited(MouseEvent e) {
			redispatchMouseEvent(e, false);
		}

		public void mousePressed(MouseEvent e) {
			boolean repaint = false;
			synchronized (_buttons) {
				for (GlassButton b : _buttons) {
					if (b.isHovering()) {
						b.mouseDown(e);
						repaint = true;
					}
				}
			}
			redispatchMouseEvent(e, repaint);
		}

		public void mouseReleased(MouseEvent e) {
			boolean repaint = false;
			synchronized (_buttons) {
				for (GlassButton b : _buttons) {
					if (b.isHovering()) {
						b.mouseUp(e);
						repaint = true;
					}
				}
			}
			redispatchMouseEvent(e, repaint);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			redispatchMouseEvent(e, false);
			_glassPane.mouseWheelMoved(e);
		}

		// The version of the redispatch consumes everything!
		protected void redispatchMouseEvent(MouseEvent e, boolean repaint) {
			if (repaint) {
				_glassPane.repaint();
			}
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				_glassPane.close();
			}
			for (KeyAction action : _glassPane._keyactions) {
				if (action.getKeyCode() == e.getKeyCode()) {
					action.execute();
				}
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}

	protected void mouseWheelMoved(MouseWheelEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}
	
	protected GlassButton getHoveredButton() {
		for (GlassButton b : _buttons) {
			if (b.isHovering()) {
				return b;
			}
		}
		return null;
	}

}
