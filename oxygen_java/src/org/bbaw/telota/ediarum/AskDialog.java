/**************************************************************************
 *  Copyright notice
 *	
 *  ediarum - an Oxygen XML Author framework for digital scholarly editions
 *  Copyright (C) 2013 Berlin-Brandenburg Academy of Sciences and Humanities
 *	
 *  This file is part of ediarum; ediarum is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ediarum is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with ediarum.  If not, see <http://www.gnu.org/licenses/>.
***************************************************************************/
/**
 * AskDialog.java - is a class for opening a dialog equal to the Oxygen Ask Dialog.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.1
 */
package org.bbaw.telota.ediarum;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

public class AskDialog extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8712318331059156061L;

	/**
	 * Dies sind die Parameter für die Fenstergröße des Dialogs.
	 */
	static int H_SIZE = 322;
	static int V_SIZE = 153;

	/**
	 * Dies sind die privaten Variablen.
	 * result Enthält den eingegebenen Text nach Bestätigung.
	 * textField Enthält den eingegebenen Text.
	 */
	private String result = "";
	private JTextField textField;

	/**
	 * Der Konstruktor der Klasse erzeugt einen Dialog zur Eingabe eines Textes.
	 * @param parent Das übergeordnete Fenster
	 * @param question Die Frage des Dialogs
	 * @param text Der voreingestellte Antworttext
	 */
	public AskDialog (Frame parent, String question, String text){
		// Calls the parent telling it this dialog is modal(i.e true)
		super(parent,true);

		// Das Layout wird manuell gesetzt.
		setLayout(null);

		// Die Frage und das Eingabefeld werden erzeugt.
		JLabel label = new JLabel(question);
		label.setBounds(11, 8, 292, 21);
		textField = new JTextField(text);
		textField.setBounds(11, 32, 292, 21);
		textField.requestFocus();
		textField.selectAll();
		add(label);
		add(textField);

		// Die Knöpfe zur Bestätigung und zum Abbruch werden erzeugt.
		JButton ok = new JButton("OK");
		ok.setBounds(125, 85, 85, 23);
		ok.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
			okAction();
		}});
		JButton cancel = new JButton("Abbrechen");
		cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e) {
			cancelAction();
		}});
		cancel.setBounds(217, 85, 85, 23);
		add(ok);
		add(cancel);
		getRootPane().setDefaultButton(ok);

		// Die Eigenschaften des Fensters werden festgelegt: die Größe, der Ort in der Bildschirmmitte, die Schließaktion und die Sichtbarkeit.
		setSize(H_SIZE, V_SIZE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setVisible(true);	
	}

	/**
	 * Bei Bestätigung wird der eingegeben Text ausgelesen und das Fenster geschlossen.
	 */
	public void okAction(){
		result = textField.getText();
		dispose();
	}

	/**
	 * Bei Abbruch wird das Fenster nur geschlossen.
	 */
	public void cancelAction(){
		dispose();
	}

	/**
	 * Diese Methode liefert den eingegebenen Text zurück.
	 * @return Der eingegebene Text nach Bestätigung
	 */
	public String getResult(){
		return result;
	}
}
