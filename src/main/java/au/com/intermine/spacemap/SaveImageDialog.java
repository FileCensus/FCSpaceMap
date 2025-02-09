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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import au.com.intermine.spacemap.exception.SystemFatalException;
import au.com.intermine.spacemap.treemap.BannerRenderer;
import au.com.intermine.spacemap.util.ImageSource;
import au.com.intermine.spacemap.util.Utils;

public class SaveImageDialog extends JDialog {

	/**
     *
     */
	private static final long serialVersionUID = 1L;

	private JButton btnChooseFile;

	private JTextField txtFilename;

	private SpringLayout springLayout_3;

	private JComponent separator;

	private JPanel panel_3;

	private JComboBox cmbFileType;

	private SpringLayout springLayout_2;

	private JComponent separator_2;

	private JPanel panel;

	private JLabel lblpixels;

	private JTextField txtHeight;

	private JLabel lblheight;

	private JTextField txtWidth;

	private JLabel lblWidth;

	private JPanel panel_2;

	private SpringLayout springLayout_1;

	private JPanel panel_1;

	private JComponent separator_1;

	private JButton btnOk;

	private JButton btnCancel;

	private SpringLayout springLayout;

	private ArrayList<String> _extension_list;

	private ImageSource _imageSource;

	private BannerRenderer _banner;

	// private JProgressBar jbar;
	private JPanel panel_4;

	private JLabel txtMessage;

	public SaveImageDialog(Frame parent, ImageSource source, BannerRenderer banner) {
		this(parent);
		_banner = banner;
		_imageSource = source;
		Utils.localizeComponents(this);
		if (source != null) {
			Dimension d = source.getDefaultDimension();
			txtHeight.setText(d.height + "");
			txtWidth.setText(d.width + "");
		}
		_extension_list = new ArrayList<String>();
		for (String s : ImageIO.getWriterFormatNames()) {
			if (!_extension_list.contains(s.toLowerCase())) {
				cmbFileType.addItem(s.toLowerCase());
				_extension_list.add(s.toLowerCase());
			}
		}
	}

	/**
	 * Create the frame
	 */
	protected SaveImageDialog(Frame parent) {
		super(parent);
		springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		setTitle("[frm.save_as_image]");
		setBounds(100, 100, 501, 318);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

		btnCancel = new JButton();
		btnCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnCancel.setText("[btn.cancel]");
		getContentPane().add(btnCancel);
		springLayout.putConstraint(SpringLayout.EAST, btnCancel, -6, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnCancel, -5, SpringLayout.SOUTH, getContentPane());

		btnOk = new JButton();
		btnOk.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (saveImage()) {
					setVisible(false);
				}
			}
		});
		btnOk.setText("[btn.ok]");
		getContentPane().add(btnOk);
		springLayout.putConstraint(SpringLayout.SOUTH, btnOk, -5, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnOk, -5, SpringLayout.WEST, btnCancel);

		panel_1 = new JPanel();
		springLayout_1 = new SpringLayout();
		panel_1.setLayout(springLayout_1);
		getContentPane().add(panel_1);
		springLayout.putConstraint(SpringLayout.EAST, panel_1, -7, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_1, 5, SpringLayout.WEST, getContentPane());

		separator_1 = DefaultComponentFactory.getInstance().createSeparator("[lbl.image_size]");
		panel_1.add(separator_1);
		springLayout_1.putConstraint(SpringLayout.SOUTH, separator_1, 25, SpringLayout.NORTH, panel_1);
		springLayout_1.putConstraint(SpringLayout.NORTH, separator_1, 5, SpringLayout.NORTH, panel_1);
		springLayout_1.putConstraint(SpringLayout.EAST, separator_1, 477, SpringLayout.WEST, panel_1);
		springLayout_1.putConstraint(SpringLayout.WEST, separator_1, 5, SpringLayout.WEST, panel_1);

		panel_2 = new JPanel();
		final GridLayout gridLayout = new GridLayout(1, 0);
		gridLayout.setHgap(2);
		panel_2.setLayout(gridLayout);
		panel_1.add(panel_2);
		springLayout_1.putConstraint(SpringLayout.SOUTH, panel_2, 55, SpringLayout.NORTH, panel_1);
		springLayout_1.putConstraint(SpringLayout.NORTH, panel_2, 5, SpringLayout.SOUTH, separator_1);
		springLayout_1.putConstraint(SpringLayout.EAST, panel_2, 477, SpringLayout.WEST, panel_1);
		springLayout_1.putConstraint(SpringLayout.WEST, panel_2, 15, SpringLayout.WEST, panel_1);

		lblWidth = new JLabel();
		lblWidth.setHorizontalAlignment(SwingConstants.RIGHT);
		lblWidth.setText("[lbl.width]");
		panel_2.add(lblWidth);

		txtWidth = new JTextField();
		txtWidth.setInputVerifier(IntegerInputVerifier.getInstance());
		panel_2.add(txtWidth);

		lblheight = new JLabel();
		lblheight.setHorizontalAlignment(SwingConstants.RIGHT);
		lblheight.setText("[lbl.height]");
		panel_2.add(lblheight);

		txtHeight = new JTextField();
		txtHeight.setInputVerifier(IntegerInputVerifier.getInstance());
		panel_2.add(txtHeight);

		lblpixels = new JLabel();
		lblpixels.setText("[lbl.pixels]");
		panel_2.add(lblpixels);

		panel = new JPanel();
		springLayout_2 = new SpringLayout();
		panel.setLayout(springLayout_2);
		getContentPane().add(panel);
		springLayout.putConstraint(SpringLayout.EAST, panel, -7, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel, 5, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel, 65, SpringLayout.SOUTH, panel_1);
		springLayout.putConstraint(SpringLayout.NORTH, panel, 5, SpringLayout.SOUTH, panel_1);

		separator_2 = DefaultComponentFactory.getInstance().createSeparator("[lbl.image_type]");
		panel.add(separator_2);
		springLayout_2.putConstraint(SpringLayout.SOUTH, separator_2, 25, SpringLayout.NORTH, panel);
		springLayout_2.putConstraint(SpringLayout.NORTH, separator_2, 6, SpringLayout.NORTH, panel);
		springLayout_2.putConstraint(SpringLayout.EAST, separator_2, 477, SpringLayout.WEST, panel);
		springLayout_2.putConstraint(SpringLayout.WEST, separator_2, 5, SpringLayout.WEST, panel);

		cmbFileType = new JComboBox();
		cmbFileType.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String ext = e.getItem().toString();
					String filename = txtFilename.getText();
					int index = filename.lastIndexOf('.');
					if (index > 0 && index < (filename.length() - 1)) {
						String s = filename.substring(index + 1);
						if (!s.equalsIgnoreCase(ext)) {
							txtFilename.setText(filename.substring(0, index + 1) + ext);
						}
					}
				}
			}
		});
		panel.add(cmbFileType);
		springLayout_2.putConstraint(SpringLayout.SOUTH, cmbFileType, 55, SpringLayout.NORTH, panel);
		springLayout_2.putConstraint(SpringLayout.NORTH, cmbFileType, 5, SpringLayout.SOUTH, separator_2);
		springLayout_2.putConstraint(SpringLayout.EAST, cmbFileType, 0, SpringLayout.EAST, separator_2);
		springLayout_2.putConstraint(SpringLayout.WEST, cmbFileType, 5, SpringLayout.WEST, panel);

		panel_3 = new JPanel();
		springLayout_3 = new SpringLayout();
		panel_3.setLayout(springLayout_3);
		getContentPane().add(panel_3);
		springLayout.putConstraint(SpringLayout.EAST, panel_3, -7, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_3, 5, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, panel_1, 135, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, panel_1, 5, SpringLayout.SOUTH, panel_3);
		springLayout.putConstraint(SpringLayout.SOUTH, panel_3, 65, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, panel_3, 5, SpringLayout.NORTH, getContentPane());

		separator = DefaultComponentFactory.getInstance().createSeparator("[lbl.filename]");
		panel_3.add(separator);
		springLayout_3.putConstraint(SpringLayout.EAST, separator, -5, SpringLayout.EAST, panel_3);
		springLayout_3.putConstraint(SpringLayout.WEST, separator, 5, SpringLayout.WEST, panel_3);
		springLayout_3.putConstraint(SpringLayout.SOUTH, separator, 25, SpringLayout.NORTH, panel_3);
		springLayout_3.putConstraint(SpringLayout.NORTH, separator, 5, SpringLayout.NORTH, panel_3);

		txtFilename = new JTextField();
		txtFilename.addCaretListener(new CaretListener() {

			public void caretUpdate(CaretEvent e) {
				String s = txtFilename.getText();
				int index = s.lastIndexOf('.');
				if (index > 0 && index < (s.length() - 1)) {
					String ext = s.substring(index + 1);
					if (_extension_list.contains(ext.toLowerCase())) {
						cmbFileType.setSelectedItem(ext.toLowerCase());
					}
				}
			}

		});
		panel_3.add(txtFilename);
		springLayout_3.putConstraint(SpringLayout.EAST, txtFilename, -47, SpringLayout.EAST, panel_3);
		springLayout_3.putConstraint(SpringLayout.WEST, txtFilename, 5, SpringLayout.WEST, panel_3);
		springLayout_3.putConstraint(SpringLayout.SOUTH, txtFilename, 55, SpringLayout.NORTH, panel_3);
		springLayout_3.putConstraint(SpringLayout.NORTH, txtFilename, 5, SpringLayout.SOUTH, separator);

		btnChooseFile = new JButton();
		btnChooseFile.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				chooseFilename();
			}
		});
		btnChooseFile.setText("...");
		panel_3.add(btnChooseFile);
		springLayout_3.putConstraint(SpringLayout.EAST, btnChooseFile, 42, SpringLayout.EAST, txtFilename);
		springLayout_3.putConstraint(SpringLayout.WEST, btnChooseFile, 5, SpringLayout.EAST, txtFilename);
		springLayout_3.putConstraint(SpringLayout.SOUTH, btnChooseFile, -6, SpringLayout.SOUTH, panel_3);
		springLayout_3.putConstraint(SpringLayout.NORTH, btnChooseFile, 30, SpringLayout.NORTH, panel_3);

		panel_4 = new JPanel();
		panel_4.setLayout(new BorderLayout());
		getContentPane().add(panel_4);
		springLayout.putConstraint(SpringLayout.SOUTH, panel_4, -5, SpringLayout.NORTH, btnCancel);
		springLayout.putConstraint(SpringLayout.NORTH, panel_4, 201, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_4, -7, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_4, 5, SpringLayout.WEST, getContentPane());
		setResizable(false);

		txtMessage = new JLabel();
		txtMessage.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(txtMessage, BorderLayout.CENTER);
	}

	private boolean saveImage() {

		FileOutputStream os;
		try {
			String fileName = txtFilename.getText();
			String fileType = (String) cmbFileType.getSelectedItem();
			File f;
			// append the file extension if it is not already on the end of the input file name.
			if (!fileName.endsWith("." + fileType)) {
				fileName = fileName + "." + fileType;
			}

			if (fileName.contains(System.getProperty("file.separator"))) {
				f = new File(fileName);
			} else {
				File parent = null; // GlobalPreferences.getLastSaveAsFolder();
				if (parent == null) {
					parent = new JFileChooser().getCurrentDirectory();
				}
				f = new File(parent.getAbsolutePath(), fileName);
			}

			fileName = f.getAbsolutePath();

			if (f.exists()) {

				int result = JOptionPane.showConfirmDialog(this, Utils.localizeCaption("[msg.overwrite_file]", fileName), Utils.localizeCaption("[frm.overwrite_file]", fileName),
						JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.OK_OPTION) {
					return false;
				}
			}
			Utils.cursorWait(this);
			int width = Integer.parseInt(txtWidth.getText());
			int height = Integer.parseInt(txtHeight.getText());

			BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = img.getGraphics();
			if (_banner == null) {
				_banner = new BannerRenderer("", "Icons/Other/intermine3.png");
			}
			_banner.drawBanner(g, width, height);
			height = height - _banner.getHeight();
			g = g.create(0, _banner.getHeight(), width, height);

			_imageSource.drawImage(g, width, height);
			os = new FileOutputStream(fileName);
			ImageIO.write(img, cmbFileType.getSelectedItem().toString(), os);
			os.flush();
			os.close();
			return true;
		} catch (OutOfMemoryError oome) {
			txtMessage.setForeground(Color.red);
			String message = Utils.localizeCaption("[error.image.to.large]");
			txtMessage.setText("<html>" + message + "<html>");
			txtMessage.invalidate();
		} catch (Exception e) {
			throw new SystemFatalException(e);
		} finally {
			Utils.cursorDefault(this);
		}
		return false;

	}

	private void chooseFilename() {
		JFileChooser chooser = new JFileChooser();
		int opt = chooser.showSaveDialog(this);
		if (opt == JFileChooser.APPROVE_OPTION) {
			txtFilename.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

}

class IntegerInputVerifier extends InputVerifier {

	private static IntegerInputVerifier _instance = new IntegerInputVerifier();

	public static InputVerifier getInstance() {
		return _instance;
	}

	@Override
	public boolean verify(JComponent input) {
		if (input instanceof JTextComponent) {
			String str = ((JTextComponent) input).getText();
			try {
				Integer.parseInt(str);
				return true;
			} catch (NumberFormatException nfex) {
				return false;
			}
		}
		return false;
	}

}