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

package au.com.intermine.spacemap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.filechooser.FileSystemView;

import au.com.intermine.spacemap.action.HideNodeAction;
import au.com.intermine.spacemap.model.TreeNode;
import au.com.intermine.spacemap.model.filter.ITreeModelFilter;
import au.com.intermine.spacemap.scanner.*;
import au.com.intermine.spacemap.scanner.filter.IFileFilter;
import au.com.intermine.spacemap.treemap.Visualisation;
import au.com.intermine.spacemap.util.ResourceManager;
import au.com.intermine.spacemap.util.RhinoScript;

public class SpaceMap extends JFrame implements IScanningEngineObserver {

    private static final long serialVersionUID = 1L;

    private static SpaceMap _instance;

    private StatusBar _statusBar;

    private Visualisation _visualisation;

    private JButton _startButton;

    private JComponent _toolbar;

    private ScanningEngine _scanningEngine;

    private TreeMapListener _hoverListener;

    private static RhinoScript _userScript;

    /** The complete model, unfiltered */
    private TreeNode _rootModel;

    /** the currently displayed model */
    private TreeNode _displayModel;

    private FilterPanel _filterPanel;

    public static void main(String[] args) {

        try {
            // Set default font based on locale first
            Locale locale = Locale.getDefault();
            String fontName = "Segoe UI Light";
            
            // Check platform and locale for appropriate font
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("windows")) {
                // Use system-appropriate fonts for non-Windows
                if (os.contains("mac")) {
                    fontName = ".AppleSystemUIFont"; // Modern macOS system font
                } else {
                    fontName = "DejaVu Sans Light"; // Modern Linux font
                }
            }
            
            // Override for CJK languages
            if (locale.getLanguage().matches("zh|ja|ko")) {
                fontName = "Microsoft YaHei Light"; // Modern CJK font
            }
            
            // Set the default font for all Swing components
            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof FontUIResource) {
                    UIManager.put(key, new FontUIResource(fontName, Font.PLAIN, 12));
                }
            }

            // Now set the Look and Feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e1) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        _instance = new SpaceMap();
        _instance.setVisible(true);
        processCommandLine(args);
    }
    
    private static void processCommandLine(String[] args) {
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equalsIgnoreCase("-path")) {
                if (args.length >= i + 1) {
                    String path = args[++i];
                    if (path.endsWith("\"")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    if (path.endsWith(":")) {
                        path = path + "\\";
                    }
                    _instance.scanPath(path);
                } else {
                    JOptionPane.showMessageDialog(_instance, "Missing argument for -path", "Missing path", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private IFileFilter createPlatformFileFilter() {

    	String osname = System.getProperty("os.name");

    	if (osname == null) {
    		return null;
    	}

    	if (osname.equalsIgnoreCase("mac os x")) {
    		return new MacOSXFileFilter();
    	}

        if (osname.equalsIgnoreCase("linux")) {
            return new LinuxOSFileFilter();
        }
    	
    	return null;
    }

    private void scanPath(String path) {
        ScanTarget target = new ScanTarget(path, path);
        _rootModel = startScan(target, createPlatformFileFilter());
        _visualisation.setModel(_rootModel);
    }

    public static Color getShadowColor() {
        return (Color) UIManager.getLookAndFeelDefaults().get("controlShadow");
    }

    public static Visualisation getVisualisation() {
        return _instance._visualisation;
    }

    public static void statusMsg(String message, Object... args) {
        if (_instance != null && _instance._statusBar != null) {
            if (args.length > 0) {
                message = String.format(message, args);
            }
            _instance._statusBar.setMessage(message);
        }
    }

    public static StatusBar getStatusBar() {
        if (_instance != null && _instance._statusBar != null) {
            return _instance._statusBar;
        }
        return null;
    }

    public static ScanningEngine getScanningEngine() {
        return _instance._scanningEngine;
    }

    protected SpaceMap() {
        super(Version.getFullVersion());
        try {
            _userScript = new RhinoScript(RhinoScript.getUserScript());
            _userScript.compile();
            _toolbar = createToolbar();
            _statusBar = new StatusBar();
            this.setSize(new Dimension(800, 600));
            setIconImage(ResourceManager.getIcon("application_logo.png").getImage());
            this.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(1);
                }

            });

            _visualisation = new Visualisation();
            _hoverListener = new TreeMapListener();
            _visualisation.getWidget().addVisualisationListener(_hoverListener);
            this.getContentPane().setLayout(new BorderLayout(2, 2));
            this.getContentPane().add(_toolbar, BorderLayout.NORTH);
            this.getContentPane().add(_visualisation, BorderLayout.CENTER);
            _visualisation.setBorder(new EmptyBorder(2, 2, 2, 2));
            this.getContentPane().add(_statusBar, BorderLayout.SOUTH);
        } catch (Exception ex) {
            ExceptionPublisher.publish(ex);
        }
    }

    public TreeNode getRootModel() {
        return _rootModel;
    }

    public TreeNode getDisplayModel() {
        return _displayModel;
    }

    public TreeNode startScan(ScanTarget target, IFileFilter filter) {
        _visualisation.getWidget().removeVisualisationListener(_hoverListener);
        _scanningEngine = new ScanningEngine(target, filter, 5);
        _scanningEngine.addObserver(this);
        SpaceMap.getStatusBar().getProgressBar().setIndeterminate(true);
        statusMsg("Scanning " + target.getLabel());
        Timer timer = new Timer();
        timer.schedule(new UpdateTimer(_visualisation, _scanningEngine), Calendar.getInstance().getTime(), 2000);
        TreeNode model = _scanningEngine.startScan(new TimerComplete(timer, _visualisation, _hoverListener));
        return model;
    }

    public static JComponent getToolbar() {
        return getInstance()._toolbar;
    }

    public static RhinoScript getUserScript() {
        return _userScript;
    }

    private JComboBox<ScanTarget> buildVolumeSelector() {
        FileSystemView fsv = FileSystemView.getFileSystemView();

        File[] roots = File.listRoots();
        List<ScanTarget> targets = new ArrayList<ScanTarget>();
        List<String> alldrives = new ArrayList<String>();
        for (File f : roots) {
            if (!fsv.isFloppyDrive(f) && f.canRead()) {
                String label = f.getPath();

                if (label.endsWith("\\")) {
                    label = label.substring(0, label.length() - 1);
                }
                targets.add(new ScanTarget(label, f.getAbsolutePath()));
                alldrives.add(f.getAbsolutePath());
            }
        }

        StringBuilder alldriveslabel = new StringBuilder("All drives (");
        for (int i = 0; i < alldrives.size(); ++i) {
            String drive = alldrives.get(i);
            if (drive.endsWith("\\")) {
                drive = drive.substring(0, drive.length() - 1);
            }
            alldriveslabel.append(drive);
            if (i < alldrives.size() - 1) {
                alldriveslabel.append(", ");
            }
        }
        alldriveslabel.append(")");
        targets.add(new ScanTarget(alldriveslabel.toString(), alldrives.toArray(new String[] {})));

        JComboBox<ScanTarget> volumes = new JComboBox<ScanTarget>();
        volumes.setModel(new DefaultComboBoxModel<ScanTarget>(targets.toArray(new ScanTarget[] {})));
        return volumes;
    }

    private JComponent createToolbar() {
        final JPanel toolbar = new JPanel(new BorderLayout());
        final JPanel basic = new JPanel(new BorderLayout(0, 0)); 
        final JPanel scancontrols = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 1));

        // Volume selector
        final JComboBox<ScanTarget> volumes = buildVolumeSelector();
        scancontrols.add(volumes);

        // Start button
        _startButton = new JButton("Start");
        _startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_scanningEngine != null && _scanningEngine.isRunning()) {
                    statusMsg("Cancelling...");
                    _scanningEngine.cancel();
                } else {
                    ScanTarget target = (ScanTarget) volumes.getSelectedItem();
                    _rootModel = startScan(target, createPlatformFileFilter());
                    _visualisation.setModel(_rootModel);
                }
            }
        });
        scancontrols.add(_startButton);

        // Right controls panel
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 1));
        
        // Unhide button
        JButton restoreButton = new JButton("Unhide");
        restoreButton.setEnabled(false);
        restoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                HideNodeAction.restoreHiddenNodes();
                restoreButton.setEnabled(false);
            }
        });
        HideNodeAction.setUnhideButton(restoreButton);
        rightControls.add(restoreButton);

        // Layout
        basic.add(scancontrols, BorderLayout.WEST);
        _filterPanel = new FilterPanel();
        
        // Wrap filter panel in a centered container
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(_filterPanel);
        basic.add(centerPanel, BorderLayout.CENTER);
        
        basic.add(rightControls, BorderLayout.EAST);
        toolbar.add(basic, BorderLayout.NORTH);

        return toolbar;
    }

    public void scanFinished(boolean cancelled) {
        if (cancelled) {
            statusMsg("Cancelled!");
        }
        _startButton.setText("Start");
        _filterPanel.setEnabled(true);
    }

    public void scanStarted() {
        _filterPanel.setEnabled(false);
        _startButton.setText("Cancel");
    }

    public static SpaceMap getInstance() {
        return _instance;
    }

    public void applyFilter(ITreeModelFilter filter) {
        TreeNode displayedRoot = _visualisation.getWidget().getDisplayedRoot();
        if (filter == null) {
            _displayModel = _rootModel;
            _visualisation.setModel(_rootModel);
        } else {
            // Prepare the model...
            _displayModel = SpaceMapHelper.createFilteredModel(_rootModel, filter);
            _visualisation.setModel(_displayModel);
        }
        // Now try and restore the current node...
        if (displayedRoot != null) {
            displayedRoot = SpaceMapHelper.findNode(_displayModel, displayedRoot.getAncestryAsString());
            if (displayedRoot != null) {
                _visualisation.getWidget().getZoom().zoomTo(displayedRoot);
            }
        }

    }
}

class TimerComplete implements IAsyncCallback {

    private Timer _timer;

    private Visualisation _v;

    TreeMapListener _listener;

    public TimerComplete(Timer timer, Visualisation v, TreeMapListener listener) {
        _timer = timer;
        _v = v;
        _listener = listener;
    }

    public void onComplete(Object result) {
        if (_timer != null) {
            _timer.cancel();
        }
        _v.repaint();
        _v.getWidget().addVisualisationListener(_listener);
        SpaceMap.statusMsg("Scan complete");
        JProgressBar prg = SpaceMap.getStatusBar().getProgressBar();
        prg.setIndeterminate(false);
        prg.setValue(0);
        prg.invalidate();
    }

    public void onException(Exception ex) {
    }

}

class UpdateTimer extends TimerTask {

    private Visualisation _c;

    private ScanningEngine _engine;

    public UpdateTimer(Visualisation c, ScanningEngine engine) {
        _c = c;
        _engine = engine;
    }

    @Override
    public void run() {
        _c.startAsyncRepaint(new IAsyncCallback() {

            public void onComplete(Object result) {
                _c.repaint();
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }

        });
        ScannerStatistics stats = _engine.getStatistics();
        if (stats != null) {
            if (stats.getFilteredFiles() == stats.getTotalFiles()) {
                // No filter
                SpaceMap.statusMsg("Scanning %s  [%d files in %d directories]", _engine.getTarget().toString(), stats.getTotalFiles(), stats.getTotalDirectories());
            } else {
                SpaceMap.statusMsg("Scanning %s  [Matched %d files out of %d in %d directories]", _engine.getTarget().toString(), stats.getFilteredFiles(), stats.getTotalFiles(), stats.getTotalDirectories());
            }

            if (SpaceMap.getScanningEngine().isRunning()) {
                if (stats.getPercentComplete() > 0) {
                    JProgressBar prg = SpaceMap.getStatusBar().getProgressBar();
                    if (prg.isIndeterminate()) {
                        prg.setIndeterminate(false);
                        prg.setMaximum(100);
                        prg.setMinimum(0);
                    }
                    prg.setValue(stats.getPercentComplete());
                }
            }
        }

    }
}
