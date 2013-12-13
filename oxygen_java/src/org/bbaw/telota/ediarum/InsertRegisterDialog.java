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
 * InsertRegisterDialog.java - is a class for opening a dialog to select a register entry.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.1.2
 */
package org.bbaw.telota.ediarum;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextComponent;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import javax.swing.JButton;
import javax.swing.JDialog;

public class InsertRegisterDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -190895918216985737L;

	/**
	 * Dies sind die Parameter für die Fenstergröße des Dialogs.
	 */
	static int H_SIZE = 400;
	static int V_SIZE = 300;

	/**
	 * Dies sind die privaten Variablen.
	 * registerListe Dies ist das Auswahlfeld mit den Registereinträgen.
	 * registerIDs Enthält die IDs zu den Registereinträgen im Auswahlfeld.
	 * registerID Enthält die ID des ausgewählten Eintrags. 
	 */
	List registerListe = new List();
	String[] registerIDs; 
	String registerID = "";

	/**
	 * Der Konstruktor der Klasse erzeugt einen Dialog zum Auswählen eines Registereintrags.
	 * @param parent Das übergeordnete Fenster
	 * @param eintrag Ein Array, das alle Registereinträge enthält
	 * @param id Ein Array, das die IDs zu den Registereinträgen enthält
	 */
	public InsertRegisterDialog(Frame parent, String[] eintrag, String[] id) {
		// Calls the parent telling it this dialog is modal(i.e true)
		super(parent, true);
		// Für den Dialog wird das Layout (North, South, .., Center) ausgewählt und der Titel gesetzt.
		setLayout(new BorderLayout());
		setTitle("Registereintrag auswählen");

		// Oben wird ein Eingabefeld erzeugt, mit welchem man zu den Einträgen springen kann.
		TextField eingabeFeld = new TextField();
		eingabeFeld.addTextListener(new eingabeFeldListener());
		eingabeFeld.requestFocus();
		add("North", eingabeFeld);


		// In der Mitte wird das Auswahlfeld mit den Registereinträgen erzeugt, ..
		registerListe.setMultipleMode(false);
		for (int i=0; i<eintrag.length; i++){
			registerListe.add(eintrag[i]);
		}
		add("Center", registerListe);
		// .. während die zugehörigen IDs in der entsprechenden Variable hinterlegt werden.
		registerIDs = id;

		// Unten gibt es die zwei Knöpfe "Ok" (als Default) und "Abbrechen".
		Panel panel = new Panel();
		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent arg0) {
			okAction();
		}});
		panel.add(ok);
		JButton cancel = new JButton("Abbrechen");
		cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent arg0) {
			cancelAction();
		}});
		panel.add(cancel);
		add("South", panel);
		getRootPane().setDefaultButton(ok);

		// Die Eigenschaften des Dialogfenster werden angepaßt: die Größe, der Ort in der Bildschirmmitte, die Schließaktion und die Sichtbarkeit.
		setSize(H_SIZE, V_SIZE);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Bei "Ok" wird der aktuelle Registereintrag gemerkt und das Fenster geschlossen.
	 */
	public void okAction(){
		registerID = registerIDs[registerListe.getSelectedIndex()];
		dispose();
	}

	/**
	 * Bei "Cancel" wird das Fenster nur geschlossen. 
	 */
	public void cancelAction(){
		dispose();
	}

	/**
	 * Diese Klasse ist dem Eingabefeld zugeordnet.
	 * @author fechner
	 *
	 */
	class eingabeFeldListener implements TextListener {

		/**
		 * Wenn etwas im Textfeld eingegeben wird, wird diese Methode aufgerufen.
		 */
		@Override
		public void textValueChanged(TextEvent e) {
			// Das Textfeld und sein momentaner Inhalt werden gelesen.
			TextComponent tc = (TextComponent)e.getSource();
			String eingabe = tc.getText().toLowerCase();
			// Ein Index von -1 wählt zunächst nichts aus.
			int index = -1;
			// Der Zähler i wird auf den Anfang der Liste gesetzt ..
			int i = 0;
			// und die Schleife durchläuft die Liste, solange bis die Liste zu Ende ist oder
			// ein Registereintrag gefunden wurde, dessen Anfang mit dem Text übereinstimmt.
			while (i<registerListe.getItems().length & index == -1){
				if (registerListe.getItem(i).toLowerCase().startsWith(eingabe)){
					index = i;
				}
				i++;
			}
			// Falls ein Eintrag gefunden wurde, wird dieser ausgewählt, sonst wird nichst ausgewählt.
			registerListe.select(index);
		}
	}

	/**
	 * Gibt die ID des ausgewählten Eintrags zurück, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgewählte ID
	 */
	public String getSelectedID() {
		return registerID;
	}
}