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

package au.com.intermine.spacemap.imageviewer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import au.com.intermine.spacemap.SpaceMapHelper;
import au.com.intermine.spacemap.glasspane.AbstractGlassPane;
import au.com.intermine.spacemap.glasspane.GlassButton;
import au.com.intermine.spacemap.glasspane.GlassButtonBuilder;
import au.com.intermine.spacemap.jobqueue.JobQueue;
import au.com.intermine.spacemap.model.NodeType;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.model.TreeNode.NodeSetIterator;
import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.ResourceManager;
import au.com.intermine.spacemap.util.Utils;

public class ImageViewer extends AbstractGlassPane {

    private enum ViewerState {
        Loading, ViewImage, ViewThumbs
    }

    private static final int MAX_THUMBNAIL_SIZE = 300;
    private static final int MIN_THUMBNAIL_SIZE = 40;
    private static Color THUMBNAIL_BORDER_COLOR = new Color(255, 255, 255, 50);
    private static final long serialVersionUID = 1L;
    private static final int CONTROL_PANEL_HEIGHT = 50;
    private static final double MAX_ZOOM = 8.0;
    private static final double MIN_ZOOM = 0.01;
    private static final int BUTTON_WIDTH = 27;
    private static final int BUTTON_HEIGHT = 21;
    private static final int TOP_ROW_BUTTON_OFFSET = 27;

    private Image _image = null;
    private NodeSetIterator _iter;
    private File _currentFile;
    private TreeNode _currentNode;
    private ViewerState _state;
    private double _zoom = 1.0;
    private Point _focusPoint;
    private Rectangle _lastDest;
    private int _thumbSize = 120;
    private int _thumbPage = 0;
    private Map<TreeNode, ImageInfo> _imageCache = new HashMap<TreeNode, ImageInfo>();
    private JobQueue _thumbnailQueue = new JobQueue(8);
    private int _selectedIndex;
    private Image _loadingImage;
    private Image _brokenImage;
    private int _thumbnailCols;
    private int _thumbnailRows;
    private ImageInfo _selectedThumbnail;
    private List<ThumbnailRectangle> _thumbRects = new ArrayList<ThumbnailRectangle>();
    private int _thumbsPerPage;
    private Point _lastPoint;

    public ImageViewer(final TreeNode node, final File file) {
        super(node, file);
        _state = ViewerState.Loading;

        _loadingImage = ResourceManager.getIcon("image_loading.png").getImage();
        _brokenImage = ResourceManager.getIcon("image_broken.png").getImage();

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ViewerState initialState = ViewerState.ViewImage;
                if (node.getNodeType() == NodeType.File && node.getCluster() > 0) {
                    // Viewing duplicate images - start in thumbnail mode.
                    _iter = node.getClusterIterator();
                    initialState = ViewerState.ViewThumbs;
                    _thumbSize = MAX_THUMBNAIL_SIZE;
                } else {
                    _iter = node.getLeafNodeIterator();
                    
                    // Paint the first image
                    if (_iter.hasNext()) {
                        loadImage(_iter.next());
                    }                    
                }
                
                setState(initialState);
            }

        });

        addKeyAction(new KeyAction(KeyEvent.VK_RIGHT) {
            @Override
            public void execute() {
                if (_state == ViewerState.ViewImage) {
                    nextImage();
                }
            }
        });

        addKeyAction(new KeyAction(KeyEvent.VK_LEFT) {
            @Override
            public void execute() {
                if (_state == ViewerState.ViewImage) {
                    previousImage();
                }
            }
        });

        repaint();

    }

    protected void setState(ViewerState state) {
        _state = state;
        switch (_state) {
            case ViewImage:
                // _thumbnailQueue.clear();
                createViewerButtons();
                break;
            case ViewThumbs:
                createThumbsButtons();
                break;
            default:

        }
        repaint();
    }

    public void close() {
        super.close();
    }

    private void addButton(int x, int y, String actionMethod, String icon, String tooltip) {
        try {
            final java.lang.reflect.Method m = ImageViewer.class.getDeclaredMethod(actionMethod);
            ActionListener action = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JobQueue.pushGlobalJob(new Runnable() {
                        public void run() {
                            try {
                                m.invoke(ImageViewer.this);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            };

            addButton(new GlassButtonBuilder().anchorBottomRight(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).action(action).icon(ResourceManager.getIcon(icon)).tooltip(tooltip).build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createThumbsButtons() {
        clearButtons();

        addButton(3, TOP_ROW_BUTTON_OFFSET, "nextPage", "right_arrow.png", "Next page");
        addButton(35, TOP_ROW_BUTTON_OFFSET, "previousPage", "left_arrow.png", "Previous page");
        addButton(67, TOP_ROW_BUTTON_OFFSET, "showSingleImage", "image_viewer.png", "View image");
        addButton(99, TOP_ROW_BUTTON_OFFSET, "close", "close.png", "Close image viewer");

        addButton(3, 2, "zoomIn", "zoom_in.png", "Zoom in");
        addButton(35, 2, "zoomOut", "zoom_out.png", "Zoom out");

    }

    private void createViewerButtons() {

        clearButtons();

        addButton(3, TOP_ROW_BUTTON_OFFSET, "nextImage", "right_arrow.png", "Next image");
        addButton(35, TOP_ROW_BUTTON_OFFSET, "previousImage", "left_arrow.png", "Previous image");
        addButton(67, TOP_ROW_BUTTON_OFFSET, "showThumbnails", "show_thumbs.png", "Show thumbnails");
        addButton(99, TOP_ROW_BUTTON_OFFSET, "rotateClockwise", "rotate_clockwise.png", "Rotate image clockwise");
        addButton(131, TOP_ROW_BUTTON_OFFSET, "close", "close.png", "Close image viewer");

        addButton(3, 2, "zoomIn", "zoom_in.png", "Zoom in");
        addButton(35, 2, "zoomOut", "zoom_out.png", "Zoom out");
        addButton(67, 2, "resetZoom", "1to1.png", "Show full size");
        addButton(99, 2, "rotateAntiClockwise", "rotate_anticlockwise.png", "Rotate image anticlockwise");
    }

    protected void showThumbnails() {
        setState(ViewerState.ViewThumbs);
    }

    protected void showSingleImage() {
        setState(ViewerState.ViewImage);
    }

    protected void nextPage() {
        _thumbnailQueue.clear();
        _selectedIndex = (_thumbPage + 1) * (_thumbnailCols * _thumbnailRows) + 1;
        if (_selectedIndex >= _iter.getSize()) {
            _selectedIndex = _iter.getSize() - 1;
        }
        repaint();
    }

    protected void previousPage() {
        _thumbnailQueue.clear();
        _selectedIndex = (_thumbPage - 1) * (_thumbnailCols * _thumbnailRows) + 1;
        if (_selectedIndex < 0) {
            _selectedIndex = 0;
        }
        repaint();
    }

    protected void rotateAntiClockwise() {
        rotateImage(-90);
    }

    protected void rotateClockwise() {
        rotateImage(90);
    }

    private void rotateImage(int angle) {

        if (_image != null) {
            _zoom = -1;

            int w = _image.getWidth(null);
            int h = _image.getHeight(null);
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg = (Graphics2D) bi.createGraphics();
            bg.rotate(Math.toRadians(angle), w / 2, h / 2);
            bg.drawImage(_image, 0, 0, w, h, 0, 0, w, h, null);

            bg.dispose(); // cleans up resources
            _image = bi;

            repaint();
        }
    }

    protected void resetZoom() {
        _focusPoint = null;
        _lastDest = null;
        _zoom = 1.0;
        repaint();
    }

    protected void zoomIn() {
        zoomIn(null);
    }

    protected void zoomIn(Point focuspoint) {
        switch (_state) {
            case ViewImage:
                if (focuspoint == null) {
                    focuspoint = new Point(getWidth() / 2, getHeight() / 2);
                }
                _focusPoint = focuspoint;

                boolean checkLock = _zoom < 1.0;

                _zoom *= 1.1;

                if (checkLock) {
                    if (_zoom > 1.0) {
                        _zoom = 1.0;
                    }
                }

                if (_zoom > MAX_ZOOM) {
                    _zoom = MAX_ZOOM;
                }
                break;
            case ViewThumbs:
                _thumbSize *= 1.1;
                if (_thumbSize > MAX_THUMBNAIL_SIZE) {
                    _thumbSize = MAX_THUMBNAIL_SIZE;
                }
                break;
        }

        repaint();
    }

    protected void zoomOut() {
        zoomOut(null);
    }

    protected void zoomOut(Point focuspoint) {
        switch (_state) {
            case ViewImage:
                if (focuspoint == null) {
                    focuspoint = new Point(getWidth() / 2, getHeight() / 2);
                }
                _focusPoint = focuspoint;

                boolean checkLock = _zoom > 1.0;

                _zoom *= 0.9;

                if (checkLock) {
                    if (_zoom < 1.0) {
                        _zoom = 1.0;
                    }
                }
                if (_zoom < MIN_ZOOM) {
                    _zoom = MIN_ZOOM;
                }
                break;
            case ViewThumbs:
                _thumbSize *= 0.9;
                if (_thumbSize < MIN_THUMBNAIL_SIZE) {
                    _thumbSize = MIN_THUMBNAIL_SIZE;
                }
                break;
        }
        repaint();
    }

    protected void nextImage() {
        if (_iter.hasNext()) {
            loadImage(_iter.next());
            _zoom = -1;
            repaint();
        }
    }

    protected void previousImage() {
        if (_iter.hasPrevious()) {
            loadImage(_iter.previous());
            _zoom = -1;
            repaint();
        }
    }

    private ImageInfo getImageForNode(final TreeNode node, final int targetWidth, final int targetHeight) {

        if (_imageCache.containsKey(node)) {
            return _imageCache.get(node);
        }
        
        Runnable j = new RetrieveThumbnailJob(node, targetWidth, targetHeight);
        if (!_thumbnailQueue.contains(j)) {
            _thumbnailQueue.pushJob(j);
        }

        return null;
    }

    private void loadImage(TreeNode node) {

        File f = Utils.getFileFromTreeNode(node);
        _currentFile = f;
        _currentNode = node;
        _zoom = -1;
        if (_currentFile != null) {
            try {
                _image = getImageForFile(_currentFile);
            } catch (Exception ex) {
                _image = null;
                ex.printStackTrace();
            }
        }

        try {
            // ImageInputStream iis = ImageIO.createImageInputStream(f);
            // Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            //
            // while (readers.hasNext()) {
            //
            // // pick the first available ImageReader
            // ImageReader reader = readers.next();
            //
            // // attach source to the reader
            // reader.setInput(iis, true);
            //
            // // read metadata of first image
            // IIOMetadata metadata = reader.getImageMetadata(0);
            //
            // String[] names = metadata.getMetadataFormatNames();
            // int length = names.length;
            // for (int i = 0; i < length; i++) {
            // // System.out.println("Format name: " + names[i]);
            // // displayMetadata(metadata.getAsTree(names[i]));
            // }
            // }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    void indent(int level) {
        for (int i = 0; i < level; i++)
            System.out.print("    ");
    }

    void displayMetadata(Node node, int level) {
        // print open tag of element
        indent(level);
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) {

            // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
            }
        }

        Node child = node.getFirstChild();
        if (child == null) {
            // no children, so close element and return
            System.out.println("/>");
            return;
        }

        // children, so close current tag
        System.out.println(">");
        while (child != null) {
            // print children recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
        }

        // print close tag of element
        indent(level);
        System.out.println("</" + node.getNodeName() + ">");
    }

    @Override
    public void paint(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        g.setColor(_background);
        g.fillRect(0, 0, getWidth(), getHeight());
        switch (_state) {
            case Loading:
                paintLoading(g);
                break;
            case ViewImage:
                paintViewing(g);
                break;
            case ViewThumbs:
                paintThumbnails(g);
            default:

        }
    }

    private void paintThumbnails(Graphics2D g) {
        _thumbnailCols = getWidth() / _thumbSize;
        int thumbWidth = getWidth() / _thumbnailCols;
        _thumbnailRows = (getHeight() - CONTROL_PANEL_HEIGHT) / _thumbSize;
        int thumbHeight = (getHeight() - CONTROL_PANEL_HEIGHT) / _thumbnailRows;

        _thumbsPerPage = _thumbnailCols * _thumbnailRows;
        // Work out, based on the current index, what page we should be showing

        _thumbPage = 0;
        while ((_thumbPage + 1) * _thumbsPerPage < _selectedIndex) {
            _thumbPage++;
        }

        _iter.goTo((_thumbPage * _thumbsPerPage) - 1);
        int margin = 20;
        int margin_half = margin / 2;
        int margin_quarter = margin / 4;
        synchronized (_thumbRects) {
            _thumbRects.clear();            
            for (int j = 0; j <= getHeight() - CONTROL_PANEL_HEIGHT; j += thumbHeight) {
                for (int i = 0; i <= getWidth(); i += thumbWidth) {

                    if (j + _thumbSize < getHeight() - CONTROL_PANEL_HEIGHT && i + _thumbSize <= getWidth()) {
                        if (_iter.hasNext()) {
                            TreeNode node = _iter.next();
                            ImageInfo info = getImageForNode(node, MAX_THUMBNAIL_SIZE, MAX_THUMBNAIL_SIZE);                            
                            Image img = null;
                            if (info != null) {
                                img = info.getThumbnail();
                            } else {
                                img = _loadingImage;
                            }

                            if (img != null) {

                                ThumbnailRectangle r = new ThumbnailRectangle(info, i + margin_quarter, j + margin_quarter, thumbWidth - margin_half, thumbHeight - margin_half);
                                Rectangle destRect = DrawingUtils.resizeImage(img, r.width - margin, r.height - margin);

                                if (_lastPoint != null && r.contains(_lastPoint)) {
                                    _selectedIndex = _iter.getCurrentPosition();
                                    _selectedThumbnail = info;
                                    _currentNode = node;
                                    g.setColor(Color.black);
                                    g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
                                }

                                g.setColor(_statusPanelColor);
                                _thumbRects.add(r);
                                g.fillRect(r.x, r.y, r.width, r.height);
                                // g.setStroke(_thumbBorderStroke );
                                g.setColor(THUMBNAIL_BORDER_COLOR);
                                g.drawRect(r.x, r.y, r.width, r.height);

                                int x = i + (thumbWidth / 2) - (destRect.width / 2);
                                int y = j + (thumbHeight / 2) - (destRect.height / 2);
                                g.drawImage(img, x, y, x + destRect.width, y + destRect.height, 0, 0, img.getWidth(null), img.getHeight(null), null);
                            }
                        }
                    }
                }
            }

        }

        drawThumbsControlPanel(g);
    }

    private void paintLoading(Graphics2D g) {
        g.setColor(Color.white);
        DrawingUtils.drawString(g, _font, "Searching for images...", 0, 0, getWidth(), getHeight(), DrawingUtils.TEXT_ALIGN_CENTER);
    }

    private void paintViewing(Graphics2D g) {
        if (_currentFile != null && _currentFile.exists() && _currentFile.isFile()) {
            highlightNode(g, _currentNode);
            paintImage(g);
            drawViewingControlPanel(g, _currentNode);
        }
    }

    private void drawThumbsControlPanel(Graphics2D g) {
        g.setColor(_statusPanelColor);
        g.fillRect(0, getHeight() - CONTROL_PANEL_HEIGHT, getWidth(), CONTROL_PANEL_HEIGHT);
        Rectangle statusRect = new Rectangle(0, getHeight() - CONTROL_PANEL_HEIGHT, getWidth(), CONTROL_PANEL_HEIGHT);

        if (_selectedThumbnail != null) {
            TreeNode node = _selectedThumbnail.getTreeNode();
            ImageIcon icon = SpaceMapHelper.getNodeIcon(node);
            int txtoffset = 10;
            if (icon != null) {
                g.drawImage(icon.getImage(), statusRect.x + 10, statusRect.y + (statusRect.height / 2) - (icon.getIconHeight() / 2), null);
                txtoffset += icon.getIconWidth() + 5;
            }

            File f = _selectedThumbnail.getFile();

            String filetype = "";
            if (f.exists()) {
                // get the collection this file would belong in?
                FileSystemView view = FileSystemView.getFileSystemView();
                filetype = view.getSystemTypeDescription(f);
            }

            int width = _selectedThumbnail.getWidth();
            int height = _selectedThumbnail.getHeight();

            int numberOfPages = (_iter.getSize() / _thumbsPerPage);
            if (_iter.getSize() > (numberOfPages * _thumbsPerPage)) {
                numberOfPages++;
            }

            String caption = String.format("[Page %d of %d]  %s\n%s  %s  (%d x %d)", _thumbPage + 1, numberOfPages, f.getAbsolutePath(), _NumberRenderer.render(node.getWeight()), filetype, width, height);

            g.setColor(Color.white);
            DrawingUtils.drawString(g, _font, caption, statusRect.x + txtoffset, statusRect.y, statusRect.width - txtoffset - 125, statusRect.height, DrawingUtils.TEXT_ALIGN_LEFT);

        }

        paintButtons(g);
        paintButtonTooltip(g, getHoveredButton());
    }

    private void paintButtonTooltip(Graphics2D g, GlassButton button) {
        if (button != null) {
            Rectangle tiprect = new Rectangle(getWidth() - 200, getHeight() - CONTROL_PANEL_HEIGHT - 35, 195, 30);
            g.fillRoundRect(tiprect.x, tiprect.y, tiprect.width, tiprect.height, 10, 10);
            g.setColor(Color.white);
            DrawingUtils.drawString(g, _font, button.getTooltip(), tiprect, DrawingUtils.TEXT_ALIGN_CENTER);
        }
    }

    private void drawViewingControlPanel(Graphics2D g, TreeNode node) {
        g.setColor(_statusPanelColor);
        g.fillRect(0, getHeight() - CONTROL_PANEL_HEIGHT, getWidth(), CONTROL_PANEL_HEIGHT);

        Rectangle statusRect = new Rectangle(0, getHeight() - CONTROL_PANEL_HEIGHT, getWidth(), CONTROL_PANEL_HEIGHT);

        ImageIcon icon = SpaceMapHelper.getNodeIcon(node);
        int txtoffset = 10;
        if (icon != null) {
            g.drawImage(icon.getImage(), statusRect.x + 10, statusRect.y + (statusRect.height / 2) - (icon.getIconHeight() / 2), null);
            txtoffset += icon.getIconWidth() + 5;
        }

        File f = Utils.getFileFromTreeNode(node);

        String filetype = "";
        if (f.exists()) {
            // get the collection this file would belong in?
            FileSystemView view = FileSystemView.getFileSystemView();
            filetype = view.getSystemTypeDescription(f);
        }

        int zoomPercent = (int) Math.round(_zoom * 100.0);

        int width = 0;
        int height = 0;
        if (_image != null) {
            width = _image.getWidth(null);
            height = _image.getHeight(null);
        }

        String caption = String.format("[%d/%d]  %s\n%s  %s  (%d x %d)  [%d%%]", _iter.getCurrentPosition(), _iter.getSize(), f.getAbsolutePath(), _NumberRenderer.render(node.getWeight()), filetype, width, height, zoomPercent);

        g.setColor(Color.white);
        DrawingUtils.drawString(g, _font, caption, statusRect.x + txtoffset, statusRect.y, statusRect.width - txtoffset - 160, statusRect.height, DrawingUtils.TEXT_ALIGN_LEFT);

        // paintBorder(g);

        paintButtons(g);
        paintButtonTooltip(g, getHoveredButton());
    }

    private void paintImage(Graphics2D g) {
        try {
            if (_image == null) {
                return;
            }

            int srcWidth = _image.getWidth(null);
            int srcHeight = _image.getHeight(null);
            int dx = 0;
            int dy = 0;

            if (_zoom < 0) {
                _focusPoint = null;
                _lastDest = null;
                _zoom = calculateRequiredZoom(srcWidth, srcHeight, getWidth(), getHeight() - CONTROL_PANEL_HEIGHT);
            }

            int width = (int) (srcWidth * _zoom);
            int height = (int) (srcHeight * _zoom);

            if (_focusPoint == null || _lastDest == null) {
                dx = (getWidth() / 2) - (width / 2);
                dy = ((getHeight() - CONTROL_PANEL_HEIGHT) / 2) - (height / 2);
            } else {

                int xx = 0 - _lastDest.x + _focusPoint.x;
                int yy = 0 - _lastDest.y + _focusPoint.y;

                double xratio = 0.5;
                double yratio = 0.5;

                if (_lastDest.contains(_focusPoint)) {
                    xratio = (double) xx / (double) _lastDest.width;
                    yratio = (double) yy / (double) _lastDest.height;
                }

                int deltax = (int) ((double) (_lastDest.width - width) * xratio);
                int deltay = (int) ((double) (_lastDest.height - height) * yratio);

                dx = _lastDest.x + deltax;
                dy = _lastDest.y + deltay;

            }

            g.drawImage(_image, dx, dy, dx + width, dy + height, 0, 0, srcWidth, srcHeight, null);

            _lastDest = new Rectangle(dx, dy, width, height);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double calculateRequiredZoom(int srcWidth, int srcHeight, int targetWidth, int targetHeight) {

        int w = srcWidth;
        int h = srcHeight;

        while (h > targetHeight || w > targetWidth) {
            if (h > targetHeight) {
                double ratio = (double) (targetHeight) / (double) h;
                h = targetHeight;
                w = (int) ((double) w * ratio);
            }

            if (w > targetWidth) {
                double ratio = (double) (targetWidth) / (double) w;
                w = targetWidth;
                h = (int) ((double) h * ratio);
            }
        }

        double zoom = 1.0;

        if (w != srcWidth) {
            // Some zooming is required for this image to fit in the target rectangle
            zoom = (double) w / (double) srcWidth;
        }

        return zoom;
    }

    public void begin() {
        repaint();
    }

    public void end() {
        repaint();
    }

    public void repeat() {
    }

    public void timingEvent(float arg0) {
        repaint();
    }

    protected void mouseWheelMoved(MouseWheelEvent e) {
        int rot = e.getWheelRotation();
        if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            rot *= 3;
        }

        for (int i = 0; i < Math.abs(rot); ++i) {
            if (rot < 0) {
                zoomIn(e.getPoint());
            } else {
                zoomOut(e.getPoint());
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        if (_lastPoint.y < (getHeight() - CONTROL_PANEL_HEIGHT)) {
            if (_lastDest != null && _lastPoint != null && _lastDest.contains(_lastPoint)) {
                _lastDest.x += p.x - _lastPoint.x;
                _lastDest.y += p.y - _lastPoint.y;
                _focusPoint = p;
                repaint();
            }
        }
        _lastPoint = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        _lastPoint = e.getPoint();
        switch (_state) {
            case ViewImage:
                if (_lastPoint.y < (getHeight() - CONTROL_PANEL_HEIGHT)) {
                    if (_lastDest != null && _lastDest.contains(_lastPoint)) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                break;
            case ViewThumbs:
                for (ThumbnailRectangle r : _thumbRects) {
                    if (r.contains(_lastPoint)) {
                        if (r.getImageInfo() != null && r.getImageInfo().getTreeNode() != _currentNode) {
                            repaint();
                            break;
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        _lastPoint = e.getPoint();
        if (_lastPoint.y < (getHeight() - CONTROL_PANEL_HEIGHT)) {
            switch (_state) {
                case ViewThumbs:
                    if (e.getClickCount() == 2) {
                        setState(ViewerState.ViewImage);
                        _iter.goTo(_selectedIndex - 1);
                        loadImage(_currentNode);
                    }
                    break;
            }
        }

    }

    class RetrieveThumbnailJob implements Runnable {

        private TreeNode _node;

        private int _targetWidth;

        private int _targetHeight;

        public RetrieveThumbnailJob(TreeNode node, int width, int height) {
            _node = node;
            _targetWidth = width;
            _targetHeight = height;
        }

        public void run() {
            File f = Utils.getFileFromTreeNode(_node);
            try {
                if (f != null) {
                    Image img = getImageForFile(f);
                    int width = img.getWidth(null);
                    int height = img.getHeight(null);
                    Rectangle r = DrawingUtils.resizeImage(width, height, _targetWidth, _targetHeight);
                    Image thumb = createImage(r.width, r.height);
                    thumb.getGraphics().drawImage(img, 0, 0, r.width, r.height, 0, 0, img.getWidth(null), img.getHeight(null), null);
                    ImageInfo info = new ImageInfo(_node, f, thumb, width, height);
                    synchronized (_imageCache) {
                        _imageCache.put(_node, info);
                    }
                    repaint();
                }
            } catch (Exception e) {
                ImageInfo info = new ImageInfo(_node, f, _brokenImage, 0, 0);
                synchronized (_imageCache) {
                    _imageCache.put(_node, info);
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RetrieveThumbnailJob) {
                RetrieveThumbnailJob other = (RetrieveThumbnailJob) obj;
                if (other._node == _node && other._targetWidth == _targetWidth && other._targetHeight == _targetHeight) {                    
                    return true;
                }
            }

            return false;
        }

        @Override
        public int hashCode() {
            return (_node.getFullPath("/") + "_" + _targetWidth + "_" + _targetHeight).hashCode();
        }
        
        public String toString() {
            return _node.getLabel();
        }

    }

    private Image getImageForFile(File file) {
        try {
            Image img = ImageIO.read(file);
            if (img == null) {
                img = FilePreviewGenerator.generatePreview(file);
            }
            return img;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}

class ImageInfo {

    private Image _thumbnail;

    private File _file;

    private TreeNode _node;

    private int _height;

    private int _width;

    public ImageInfo(TreeNode node, File file, Image thumbnail, int width, int height) {
        _node = node;
        _file = file;
        _thumbnail = thumbnail;
        _width = width;
        _height = height;
    }

    public TreeNode getTreeNode() {
        return _node;
    }

    public Image getThumbnail() {
        return _thumbnail;
    }

    public File getFile() {
        return _file;
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }
}

class ThumbnailRectangle extends Rectangle {

    private static final long serialVersionUID = 1L;

    private ImageInfo _imageInfo;

    public ThumbnailRectangle(ImageInfo info, int x, int y, int width, int height) {
        super(x, y, width, height);
        _imageInfo = info;
    }

    public ImageInfo getImageInfo() {
        return _imageInfo;
    }
}
