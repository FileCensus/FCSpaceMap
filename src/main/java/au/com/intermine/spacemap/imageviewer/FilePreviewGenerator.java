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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import au.com.intermine.spacemap.util.DrawingUtils;
import au.com.intermine.spacemap.util.Utils;

public class FilePreviewGenerator {

	private static Dimension TARGET_SIZE = new Dimension(1024, 768);

	private static PreviewGenerator[] _GENERATOR_LIST = new PreviewGenerator[] { new EMLFilePreviewGenerator() };

	private static Map<String, PreviewGenerator> _GENERATORS = new HashMap<String, PreviewGenerator>();

	static {
		for (PreviewGenerator g : _GENERATOR_LIST) {
			String[] exts = g.getExtensions();
			for (String ext : exts) {
				_GENERATORS.put(ext.toLowerCase(), g);
			}
		}
	}

	public static Image generatePreview(File file) {
		Image img = null;
		String name = file.getName();
		if (name.contains(".")) {
			String extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
			if (_GENERATORS.containsKey(extension)) {
				img = new BufferedImage(TARGET_SIZE.width, TARGET_SIZE.height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) img.getGraphics();

				_GENERATORS.get(extension).drawPreview(file, g, TARGET_SIZE);
			}
		}

		return img;
	}

}

abstract class PreviewGenerator {

	private String[] _extenstions;

	protected PreviewGenerator(String... extensions) {
		_extenstions = extensions;
	}

	public abstract void drawPreview(File file, Graphics2D g, Dimension size);

	public String[] getExtensions() {
		return _extenstions;
	}

}

class EMLFilePreviewGenerator extends PreviewGenerator {

	private Font _normalFont = new Font("Tahoma", 0, 13);
	private Font _boldFont = new Font("Tahoma", Font.BOLD, 13);

	public EMLFilePreviewGenerator() {
		super("eml");
	}

	@SuppressWarnings("unchecked")
    @Override
	public void drawPreview(File file, Graphics2D g, Dimension size) {
		try {
			Session session = Session.getInstance(new Properties());
			FileInputStream is = new FileInputStream(file);
			MimeMessage m = new MimeMessage(session, is);
			g.setColor(Color.white);
			g.fillRect(0, 0, size.width, size.height);
			g.setColor(Color.BLACK);

			Enumeration<Header> e = m.getAllHeaders();
			int y = 5;
			while (e.hasMoreElements()) {
				Header h = e.nextElement();
				drawHeader(g, y += 20, size, h.getName() + ":", h.getValue());
			}

			Object objContent = m.getContent();
			String msg = "";
			if (objContent instanceof Multipart) {
				Multipart mp = (Multipart) objContent;
				for (int i = 0; i < mp.getCount(); ++i) {
					BodyPart bp = mp.getBodyPart(i);
					if (bp.getContent() instanceof String) {
						msg = Utils.readFlowingStringFromStream(bp.getInputStream());
					}
				}
			} else {
				msg = objContent.toString();
			}

			DrawingUtils.drawString(g, _normalFont, msg, 10, y + 30, size.width - 20, size.height - (y + 30), DrawingUtils.TEXT_ALIGN_LEFT, true);

			// drawHeader(g, 10, size, "To:", m.getRecipients(RecipientType.TO).toString());
			// drawHeader(g, 30, size, "From:", m.getFrom().toString());
			// drawHeader(g, 50, size, "Subject:", m.getSubject());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void drawHeader(Graphics2D g, int y, Dimension size, String header, String value) {
		DrawingUtils.drawString(g, _boldFont, header, 10, y, 100, 20, DrawingUtils.TEXT_ALIGN_RIGHT);
		DrawingUtils.drawString(g, _normalFont, value, 115, y, size.width - 165, 20, DrawingUtils.TEXT_ALIGN_LEFT);
	}

}
