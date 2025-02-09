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

package au.com.intermine.spacemap.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.MenuElement;
import javax.swing.RootPaneContainer;

import au.com.intermine.spacemap.Version;
import au.com.intermine.spacemap.exception.SystemFatalException;
import au.com.intermine.spacemap.model.TreeNode;

/**
 * 
 * @author djb
 */
public class Utils {

	private static ResourceBundle _resourceBundle;

	public static final String EOL;

	private static Map<Component, CaptionDescriptor> ORIGINAL_CAPTIONS;

	static {

		try {
			EOL = System.getProperty("line.separator");
			ORIGINAL_CAPTIONS = new HashMap<Component, CaptionDescriptor>();
		} catch (Exception ex) {
			throw new SystemFatalException(ex);
		}
	}

	public static ResourceBundle getResourceBundle() {
		return _resourceBundle;
	}

	public static synchronized void ReloadResourceBundle() {
		_resourceBundle = ResourceBundle.getBundle("au.com.intermine.spacemap.admin.resource.captions");
	}

	public static void centreWindow(Dialog c, JFrame frame, boolean pack) {
		if (pack) {
			c.pack();
		}
		centreWindow(c, frame);
	}

	public static void centreWindow(Window c, JFrame frame) {
		Dimension app = frame.getSize();
		int x = frame.getX() + (app.width - c.getWidth()) / 2;
		int y = frame.getY() + (app.height - c.getHeight()) / 3;
		if (y < frame.getY()) {
			y = frame.getY();
		}
		c.setLocation(x, y);
	}

	/**
	 * 
	 * @param component
	 * @return
	 */
	public static Component getParentGlassPane(Component component) {
		Component glasspane = null;
		while (component != null) {
			if (component instanceof RootPaneContainer) {
				glasspane = ((RootPaneContainer) component).getGlassPane();
				break;
			}
			component = component.getParent();
		}

		return glasspane;
	}

	@SuppressWarnings("unchecked")
	public static void localizeComponents(Component component, Object... arguments) {
		try {
			Class cls = component.getClass();
			Method get = null;
			Method set = null;
			if (component instanceof Dialog || component instanceof Frame) {
				get = cls.getMethod("getTitle", (Class[]) null);
				set = cls.getMethod("setTitle", new Class[] { String.class });
			} else if (component instanceof JTabbedPane) {
				JTabbedPane tab = (JTabbedPane) component;
				for (int i = 0; i < tab.getTabCount(); ++i) {
					String newCaption = localizeCaption(tab.getTitleAt(i), arguments);
					if (newCaption != null) {
						tab.setTitleAt(i, newCaption);
					}
				}
			} else {
				try {
					get = cls.getMethod("getText", (Class[]) null);
					set = cls.getMethod("setText", String.class);
				} catch (Exception ex) {
					get = null;
					set = null;
				}
			}
			if (get != null && set != null) {
				localizeComponent(component, get, set, arguments);
			}

			if (component instanceof MenuElement) {
				MenuElement menu = (MenuElement) component;
				for (MenuElement child : menu.getSubElements()) {
					localizeComponents((Component) child);
				}
			} else if (component instanceof Container) {
				Container container = (Container) component;
				for (Component c : container.getComponents()) {
					if (c instanceof Container) {
						localizeComponents((Container) c);
					}
				}
			}
		} catch (Exception ex) {
			throw new SystemFatalException(ex);
		}
	}

	public static void cursorWait(Component c) {
		c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public static void cursorDefault(Component c) {
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public static void errorDialog(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
	}

	public static JFrame getParentFrame(Component c) {
		Component p = c;
		while (p != null) {
			if (p instanceof JFrame) {
				return (JFrame) p;
			}
			p = p.getParent();
		}
		return null;
	}

	private static void localizeComponent(Component component, Method get, Method set, Object... arguments) {
		try {
			String caption = (String) get.invoke(component, (Object[]) null);
			String newCaption = localizeCaption(caption, arguments);
			if (newCaption != null) {
				ORIGINAL_CAPTIONS.put(component, new CaptionDescriptor(component, set, caption));
				set.invoke(component, newCaption);
			}
		} catch (Exception ex) {
		}
	}

	public static void revertCaptions(Component frame) {
		for (CaptionDescriptor c : ORIGINAL_CAPTIONS.values()) {
			if (frame == null) {
				c.restoreCaption();
			} else if (frame == c.getParentFrame()) {
				c.restoreCaption();
			}
		}
	}

	public static String localizeCaption(String caption) {
		return localizeCaption(caption, (Object[]) null);
	}

	public static String localizeCaption(String caption, Object... arguments) {
		if (caption.startsWith("[") && caption.endsWith("]")) {
			String key = caption.substring(1, caption.length() - 1);
			try {
				String text = _resourceBundle.getString(key);
				return MessageFormat.format(text, arguments);
			} catch (Exception ex) {
				System.err.println("Failed to get localized caption for " + caption);
				return caption;
			}
		}
		return null;
	}

	public static void dumpSystemProperties(Writer writer) throws IOException {
		Properties p = System.getProperties();
		writer.write("System Properties" + EOL);
		writer.write("=================" + EOL);
		for (Object key : p.keySet()) {
			String val = p.getProperty(key.toString());
			writer.write(key.toString());
			writer.write("=");
			writer.write(val);
			writer.write(EOL);
		}
		writer.write("Runtime Attributes" + EOL);
		writer.write("==================" + EOL);
		Runtime r = Runtime.getRuntime();
		writer.write("Max Memory        : " + r.maxMemory() + EOL);
		writer.write("Free Memory       : " + r.freeMemory() + EOL);
		writer.write("Total Memory      : " + r.totalMemory() + EOL);
		writer.write("Avail. Processors : " + r.availableProcessors() + EOL);
		writer.flush();
	}

	/**
	 * Format the given pattern, replacing occurances of $<c> with whatever value is stored with the key <c> in placeholders map. Standard c style escape sequences can also be used, such as '\n',
	 * '\t', '\r', '\\', '\'' and '\"'. Also special chars can be specified with octal (\777) or hex (\xFF).
	 * 
	 * @param pattern
	 *            the format pattern to apply
	 * @param placeholders
	 *            the map of placeholder chars to their values
	 * @return a formatted string
	 */
	public static String substitute(String format, Map<String, Object> placeholders) {
		StringBuffer buf = new StringBuffer();
		char prefix = '$';
		int length = format.length();
		for (int i = 0; i < length; ++i) {
			char ch = format.charAt(i);
			if (ch == prefix) {
				++i;
				ch = format.charAt(i);
				if (ch == prefix) {
					buf.append(prefix);
				} else {
					String key = "";
					if (ch == '(') {
						// we're looking for a closing ')'
						++i;
						ch = format.charAt(i);
						while (i < length && ch != ')') {
							key = key + ch;
							++i;
							if (i < length) {
								ch = format.charAt(i);
							}
						}
					} else {
						// we are looking for a non-alphanum char
						// we're looking for a closing ')'
						while (i < length && Character.isLetterOrDigit(ch)) {
							key = key + ch;
							++i;
							if (i < length) {
								ch = format.charAt(i);
							}
						}
						i--;
					}

					if (placeholders.containsKey(key)) {
						buf.append(placeholders.get(key));
					} else {
						buf.append(key);
					}
				}
			} else if (ch == '\\') {
				ch = format.charAt(++i);
				if (ch == '\\') {
					buf.append('\\');
				} else if (ch == 'n') {
					buf.append('\n');
				} else if (ch == 'r') {
					buf.append('\r');
				} else if (ch == 't') {
					buf.append('\t');
				} else if (ch == '\"') {
					buf.append('\"');
				} else if (ch == '\'') {
					buf.append('\'');
				} else if (Character.isDigit(ch)) {
					char d0 = ch;
					if (d0 < '0' || d0 > '7') {
						throw new RuntimeException("Syntax error in format string!");
					}
					char d1 = format.charAt(++i);
					if (d1 < '0' || d1 > '7') {
						throw new RuntimeException("Syntax error in format string!");
					}
					char d2 = format.charAt(++i);
					if (d2 < '0' || d2 > '7') {
						throw new RuntimeException("Syntax error in format string!");
					}
					int i0 = d0 - '0';
					int i1 = d1 - '0';
					int i2 = d2 - '0';

					int intCh = (i2 << 6) + (i1 << 3) + i0;
					buf.append((char) intCh);
				} else if (ch == 'x') {

					int hi = (int) format.charAt(++i);
					int lo = (int) format.charAt(++i);

					if (hi >= '0' && hi <= '9') {
						hi = hi - '0';
					} else if (hi >= 'A' && hi <= 'F') {
						hi = hi - 'A' + 10;
					} else if (hi >= 'a' && hi <= 'f') {
						hi = hi - 'a' + 10;
					} else {
						throw new RuntimeException("Syntax error in format string!");
					}

					if (lo >= '0' && lo <= '9') {
						lo = lo - '0';
					} else if (lo >= 'A' && lo <= 'F') {
						lo = lo - 'A' + 10;
					} else if (lo >= 'a' && lo <= 'f') {
						lo = lo - 'a' + 10;
					} else {
						throw new RuntimeException("Syntax error in format string!");
					}
					int intCh = (hi << 4) + lo;
					buf.append((char) intCh);
				} else {
					throw new RuntimeException("Unrecognized escape character following \\ - '" + ch + "'");
				}
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	public static void cursorFinger(JComponent c) {
		c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	public static File getFileFromTreeNode(TreeNode node) {
		if (node != null) {
			String path = Utils.join(node.getAncestry(), File.separator);
			File f = new File(path);
			return f;
		} else {
			return null;
		}
	}

	public static boolean areYouSure(String format, Object... args) {
		String message = format;
		if (args.length > 0) {
			message = String.format(format, args);
		}
		int result = JOptionPane.showConfirmDialog(null, message, "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}

	public static void launchFile(String filename) {
		try {
			String osname = System.getProperty("os.name");
			if (osname.matches(".*[Ww]indows.*")) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \"" + filename + "\"");
			} else {
				throw new RuntimeException("Not supported on this platform - " + osname);
			}
		} catch (Exception ex) {
			new SystemFatalException(ex);
		}
	}

	public static String join(List<? extends Object> l, String joiner) {

		if (l == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < l.size(); ++i) {
			Object o = l.get(i);
			if (o != null) {
				buf.append(o.toString());
				if (i < l.size() - 1) {
					buf.append(joiner);
				}
			}
		}

		return buf.toString();
	}

	public static Map<String, String> buildVersionMap() {
		Map<String, String> version = new HashMap<String, String>();
		version.put("Major", Version.getMajor());
		version.put("Minor", Version.getMinor());
		version.put("Revision", Version.getRevision());
		version.put("Build", Version.getBuild());
		version.put("Version", Utils.localizeCaption("[lbl.version]", Version.getMajor(), Version.getMinor(), Version.getRevision(), Version.getBuild()));
		return version;
	}

	public static String readFlowingStringFromStream(InputStream inputStream) throws IOException {
		
		InputStreamReader reader = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(reader);
		StringBuilder buf = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			buf.append(line);
			if (!line.endsWith(" ")) {
				buf.append("\n");
			}
			line = br.readLine();
		}
		
		return buf.toString();
	}
	
	/**
	 * Returns the contents of the file in a byte array.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new RuntimeException("File is too large!");
		}

		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}	

}

class CaptionDescriptor {

	private Component _component;

	private Method _set;

	private String _caption;

	private Component _parentFrame;

	public CaptionDescriptor(Component c, Method set, String caption) {
		_component = c;
		_set = set;
		_caption = caption;
		_parentFrame = findParent(c);
	}

	private Component findParent(Component c) {
		Component p = c;
		while (p.getParent() != null) {
			p = p.getParent();
			if (p instanceof Window) {
				return p;
			}
		}
		return p;
	}

	public Component getParentFrame() {
		return _parentFrame;
	}

	public void restoreCaption() {
		try {
			_set.invoke(_component, _caption);
		} catch (Exception ex) {
		}
	}

	public static void installDebuggingAWTEventListener() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

			@Override
			public void eventDispatched(AWTEvent event) {
				System.err.println(event);
			}

		}, 0xFFFFFF);

	}

}