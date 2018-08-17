package org.bbaw.telota.ediarum.dates;

import javax.swing.JCheckBox;

/**
 * Eine JCheckBox in dem ein Objekt gespeichert werden kann.
 *
 * @author Philipp Belitz
 */
public class JCheckBoxData extends JCheckBox {

	private static final long serialVersionUID = -2572681609910201502L;
	Object data;

	public JCheckBoxData(String label, boolean enabled) {
		super(label, enabled);
	}

	public void setData(Object o) {
		data = o;
	}

	public Object getData() {
		return data;
	}
}
