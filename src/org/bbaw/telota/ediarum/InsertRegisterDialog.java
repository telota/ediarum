/**
 * InsertRegisterDialog.java - is a class for opening a dialog to select a register entry.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.1.3
 */
package org.bbaw.telota.ediarum;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

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
	 * registerItems Enthält alle Registereinträge.
	 * registerIDs Enthält die IDs zu den Registereinträgen.
	 * registerID Enthält die ID des ausgewählten Eintrags. 
	 * filterVerweise Enthält nur gefilterte Einträge mit ihren Originalpositionen.
	 */
	JList<String> registerListe;
	String[] registerItems;
	String[] registerIDs; 
	String registerID = "";
	String[] selectedRegisterIDs;
	HashMap<Integer, Integer> filterVerweise = new HashMap<Integer, Integer>();
	Boolean setFilter = false;
	JTextField globalEingabeFeld;

	/**
	 * Der Konstruktor der Klasse erzeugt einen Dialog zum Auswählen eines Registereintrags.
	 * @param parent Das übergeordnete Fenster
	 * @param eintrag Ein Array, das alle Registereinträge enthält
	 * @param id Ein Array, das die IDs zu den Registereinträgen enthält
	 */
	public InsertRegisterDialog(Frame parent, String[] eintrag, String[] id, boolean multipleSelection) {
		// Calls the parent telling it this dialog is modal(i.e true)
		super(parent, true);
		// Für den Dialog wird das Layout (North, South, .., Center) ausgewählt und der Titel gesetzt.
		setLayout(new BorderLayout());
		setTitle("Registereintrag auswählen");
		
		// Eine Schriftart für die Textfelder wird benutzt, die auch Sonderzeichen darstellen kann.
//	     try {
//		     GraphicsEnvironment ge = 
//		         GraphicsEnvironment.getLocalGraphicsEnvironment();
//				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Resources/FreeSans.ttf")));
//				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("Resources/LinLibertine_Rah.ttf")));
//			} catch (FontFormatException | IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		Font fontWithSpecialCharacters = new Font("Consolas", Font.PLAIN, 12);
	    
		// Oben wird ein Eingabefeld erzeugt, mit welchem man zu den Einträgen springen kann.
		JTextField eingabeFeld = new JTextField();
		globalEingabeFeld = eingabeFeld;
		eingabeFeld.getDocument().addDocumentListener(new eingabeFeldListener());
//		eingabeFeld.setFont(fontWithSpecialCharacters);
		eingabeFeld.setColumns(28);
		eingabeFeld.requestFocus();
		JToggleButton doFilteringButton = new JToggleButton();
		doFilteringButton.setText("Filtern");
		doFilteringButton.addChangeListener(new FilterChangeListener());
		Panel panelNorth = new Panel();
		panelNorth.setLayout(new BorderLayout());
		panelNorth.add(eingabeFeld, BorderLayout.WEST);
		panelNorth.add(doFilteringButton, BorderLayout.EAST);
		add("North", panelNorth);

		// Die Einträge werden initialisiert.
		registerItems = eintrag;
		registerListe = new JList<String>(new DefaultListModel<String>());
		filterRegisterListe("");
		// In der Mitte wird das Auswahlfeld mit den Registereinträgen erzeugt, ..
		if (multipleSelection) {
			registerListe.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else {
			registerListe.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		registerListe.setFont(fontWithSpecialCharacters);
		// Hier wird ein Listener eingefügt, der bei Doppelklick bestätigt.
		registerListe.addMouseListener(new RegisterListeMouseListener());
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(registerListe);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add("Center", scrollPane);
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
		registerID = registerIDs[filterVerweise.get(registerListe.getSelectedIndex()-1)];
//		String registerItem = registerItems[filterVerweise.get(registerListe.getSelectedIndex()-1)];
//		registerID = registerIDs[registerListe.getSelectedIndex()];
//		System.out.print(registerID+":"+registerItem);
		int[] selectedIndices = registerListe.getSelectedIndices();
		selectedRegisterIDs = new String[selectedIndices.length];
		for (int i=0; i<selectedIndices.length; i++) {
			selectedRegisterIDs[i] = registerIDs[filterVerweise.get(selectedIndices[i]-1)];
		}
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
	class eingabeFeldListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			handleTextChange(e);			
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			handleTextChange(e);			
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			handleTextChange(e);			
		}
		
		/**
		 * Wenn etwas im Textfeld eingegeben wird, wird diese Methode aufgerufen.
		 */
//		@Override
		private void handleTextChange(DocumentEvent e) {
			// Das Textfeld und sein momentaner Inhalt werden gelesen.
			try {
				String eingabe;
				eingabe = e.getDocument().getText(0, e.getDocument().getLength()).toLowerCase();
				// Wenn gefiltert werden soll ..
				if (setFilter) {
					// .. wird dies getan, ..
					filterRegisterListe(eingabe);
				} else {
					// .. sonst wird zum entsprechenden Eintrag gesprungen.
					goToItem(eingabe);
				}
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	class FilterChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent changeEvent) {
	        AbstractButton abstractButton = (AbstractButton) changeEvent.getSource();
	        ButtonModel buttonModel = abstractButton.getModel();
//	        boolean armed = buttonModel.isArmed();
//	        boolean pressed = buttonModel.isPressed();
	        boolean selected = buttonModel.isSelected();
	        setFilter = selected;
	        String eingabe;
			try {
				eingabe = globalEingabeFeld.getDocument().getText(0, globalEingabeFeld.getDocument().getLength()).toLowerCase();
		        if (!selected) {
		        	filterRegisterListe("");
		        	goToItem(eingabe);
		        } else {
		        	filterRegisterListe(eingabe);
		        }
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        globalEingabeFeld.requestFocus();
		}
		
	}
	
	class RegisterListeMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			 if (e.getClickCount() == 2) {
				 okAction();
			  }
		}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}
		
	}

	/**
	 * Gibt die ID des ausgewählten Eintrags zurück, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgewählte ID
	 */
	public String getSelectedID() {
		return registerID;
	}

	/**
	 * Gibt die IDs der ausgewählten Einträge zurück, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgewählten IDs
	 */
	public String[] getSelectedIDs() {
		return selectedRegisterIDs;
	}

	// Alternativer Ansatz
	private void filterRegisterListe(String eingabe){
		filterVerweise.clear();
		DefaultListModel<String> registerListModel = (DefaultListModel<String>)registerListe.getModel();
		registerListModel.clear();
		for (int j=0; j<registerItems.length; j++) {
			if (registerItems[j].toLowerCase().indexOf(eingabe.toLowerCase())>-1) {
				filterVerweise.put(filterVerweise.size()-1, j);
				registerListModel.addElement(registerItems[j]);
			}
		}
	}				

	private void goToItem(String eingabe) {
		// Ein Index von -1 wählt zunächst nichts aus.
		int index = -1;
		// Der Zähler i wird auf den Anfang der Liste gesetzt ..
		int i = 0;
		// und die Schleife durchläuft die Liste, solange bis die Liste zu Ende ist oder
		// ein Registereintrag gefunden wurde, dessen Anfang mit dem Text übereinstimmt.
		while (i<registerListe.getModel().getSize() & index == -1){
			if (registerListe.getModel().getElementAt(i).toLowerCase().startsWith(eingabe)){
				index = i;
			}
			i++;
		}
		// Falls ein Eintrag gefunden wurde, wird dieser ausgewählt, sonst wird nichts ausgewählt.
		registerListe.setSelectedIndex(index);
		registerListe.ensureIndexIsVisible(index);
	}
}