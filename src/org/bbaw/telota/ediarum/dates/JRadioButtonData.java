package org.bbaw.telota.ediarum.dates;

import javax.swing.JRadioButton;

/**
 * Ein JRadioButton in dem ein Objekt gespeichert werden kann.
 *
 * @author Philipp Belitz
 */
public class JRadioButtonData extends JRadioButton {
	private static final long serialVersionUID = 6996412762217069508L;
	Object data;

	public JRadioButtonData(String label) {
		super(label);
	}

	public void setData(Object o) {
		data = o;
	}

	public Object getData() {
		return data;
	}
}
