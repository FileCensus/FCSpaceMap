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

package au.com.intermine.spacemap;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class StatusBar extends JPanel {
	
	private JLabel _messageBar;
	private JProgressBar _progressBar;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public StatusBar() {
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(0,2,3,2));
		_messageBar = new JLabel();
		add(_messageBar, BorderLayout.CENTER);
		_progressBar = new JProgressBar();
		add(_progressBar, BorderLayout.EAST);
		setPreferredSize(new Dimension(300,20));		
	}
	
	public void setMessage(String message) {
		_messageBar.setText(message);
		_messageBar.revalidate();
	}
	
	public JProgressBar getProgressBar() {
	    return _progressBar;
	}

}
