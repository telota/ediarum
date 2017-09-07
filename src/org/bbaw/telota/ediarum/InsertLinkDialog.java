/**
 * InsertLinkDialog.java - is a class for opening a dialog to select a file and a reference.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.1.1
 */
package org.bbaw.telota.ediarum;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JDialog;

public class InsertLinkDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6533369934664023577L;

	/**
	 * Dies sind die Parameter für die Fenstergröße des Dialogs.
	 */
	static int H_SIZE = 400;
	static int V_SIZE = 300;

	/**
	 * Dies sind die privaten Variablen.
	 * fileListe Dies ist das Auswahlfeld mit den Dateien.
	 * linkListe Dies ist das Auswahlfeld mit den Verweiszielen.
	 * files Enthält die Dateinamen.
	 * filesIDs Enthält die Dateiidentfikatoren.
	 * links Enthält die Verweiseziele.
	 * IDs Enthält die IDs der Verweisziele.
	 * selectedID Enthält die ID des ausgewählten Verweiszieles.  
	 * selectedFile Enthält den ausgewählten Dateinamen.
	 * selectedFileID Enthält die ID der ausgewählten Datei.
	 */
	List fileListe = new List();
	List linkListe = new List();
	String[] files;
	String[] filesIDs;
	String[][] links;
	String[][] IDs;
	String selectedID = "";
	String selectedFile = "";
	String selectedFileID = "";

	/**
	 * Der Konstruktor erzeugt ein Dialogfenster zur Auswahl eines Verweiszieles aus verschiedenen Dateien.
	 * @param parent Das übergeordnete Fenster
	 * @param file Ein Array, das alle Dateinamen enthält
	 * @param fileID Ein Array, das die IDs zu den Dateien enthält
	 * @param refs Ein mehrdimensionales Array, das die Texte der Verweisziele enthält
	 * @param ids Ein mehrdimensionales Array, das die IDs der Verweisziele enthält
	 */
	public InsertLinkDialog(Frame parent, String[] file, String[] fileID, String[][] refs, String[][] ids) {
		// Ein modales Fenster wird erzeugt.
		super(parent, true);
		// Für den Dialog wird das Layout (North, South, .., Center) ausgewählt und der Titel gesetzt.
		setLayout(new BorderLayout());
		setTitle("Querverweis einfügen");

		// Die übergebenen Parameter werden in die privaten Variablen eingelesen.
		files = file;
		filesIDs = fileID;
		links = refs;
		IDs = ids;

		// Oben wird ein Auswahlfeld mit den offenen Dateien erzeugt, ..
		fileListe.setMultipleMode(false);
		for (int i=0; i<file.length; i++){
			fileListe.add(file[i]);
		}
		fileListe.addItemListener(new fileListListener());
		add("North", fileListe);

		// .. in der Mitte wird ein zunächst leeres Auswahlfeld für die Verweisen einer Datei erzeugt, ..
		linkListe.setMultipleMode(false);
		add("Center", linkListe);

		// und unten werden die Knöpfe "Ok" und "Abbrechen" eingesetzt.
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

		// Die Eigenschaften des Dialogfenster werden angepaßt: die Größe, der Ort und die Sichtbarkeit.
		setSize(H_SIZE, V_SIZE);
		setLocation( (parent.getBounds().width - H_SIZE) /2, (parent.getBounds().height - V_SIZE) /2);
		setVisible(true);

	}

	/**
	 * Bei "Ok" merkt sich der Dialog die ID und den Namen der ausgewählten
	 * Datei, sowie evtl. die ID eines ausgewählten Verweises und schließt das Fenster.
	 */
	public void okAction(){
		selectedFile = files[fileListe.getSelectedIndex()];
		selectedFileID = filesIDs[fileListe.getSelectedIndex()];
		if (linkListe.getSelectedIndex()!=-1){
			selectedID = IDs[fileListe.getSelectedIndex()][linkListe.getSelectedIndex()];
		}
		dispose();
	}

	/**
	 * Bei "Abbrechen" wird der Dialog nur geschlossen.
	 */
	public void cancelAction(){
		dispose();
	}

	/**
	 * Diese Klasse ist der Liste mit den Dateinamen zugeordnet.
	 * @author fechner
	 *
	 */
	class fileListListener implements ItemListener {

		/**
		 * Wenn ein neuer Eintrag in der Liste ausgewählt wurde, wird
		 * diese Methode aufgerufen.
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			// Die Liste und der gewählte Eintrag werden gelesen, ..
			List l = (List)e.getSource();
			int auswahl = l.getSelectedIndex();
			// .. aus der unteren Liste werden alle Einträge entfernt, ..
			linkListe.removeAll();
			// .. und die zur gewählten Datei gehörigen Verweisziele eingefügt.
			for (int j=0; j<links[auswahl].length; j++) {
				linkListe.add(links[auswahl][j]);
			}
		}

	}

	/**
	 * Gibt die ID des ausgewählten Verweises zurück, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgewählte ID
	 */
	public String getSelectedID() {
		return selectedID;
	}

	/**
	 * Gibt den Namen der ausgewählten Datei zurück, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Der ausgewählte Dateiname
	 */
	public String getSelectedFile() {
		return selectedFile;
	}

	/**
	 * Gibt die ID der ausgewählten Datei zurück, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgewählte Datei-ID
	 */
	public String getSelectedFileID() {
		return selectedFileID;
	}

}
