package org.bbaw.telota.ediarum.dates;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

/**
 * Listener, der auf einen normalen Mausklick und Doppelklick reagiert
 * 
 * @author Philipp Belitz
 */
class ClickListener extends MouseAdapter implements ActionListener {
	private final static int clickInterval = (Integer) Toolkit.getDefaultToolkit()
			.getDesktopProperty("awt.multiClickInterval");

	MouseEvent lastEvent;
	Timer timer;

	public ClickListener() {
		this(clickInterval);
	}

	public ClickListener(int delay) {
		timer = new Timer(delay, this);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 2)
			return;

		lastEvent = e;

		if (timer.isRunning()) {
			timer.stop();
			doubleClick(lastEvent);
		} else {
			timer.restart();
		}
	}

	public void actionPerformed(ActionEvent e) {
		timer.stop();
		singleClick(lastEvent);
	}

	public void singleClick(MouseEvent e) {
	}

	public void doubleClick(MouseEvent e) {
	}
}