package org.bbaw.telota.ediarum.dates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import org.bbaw.pdr.dates.config.java.DateOccurrence;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperationException;

/**
 * Eine GUI, die alle gefundenen Datumsangaben in einer Liste ausgibt. Zu jeder
 * Angabe wird ihr Text und ISO-Format adrgestellt. Außerdem kann jede Angabe
 * einzeln ausgewählt werden um sie im Dokument taggen zu können.
 *
 * @author Philipp Belitz
 */

public class SearchGUI extends JDialog {
	private static final long serialVersionUID = -4200994439024427240L;

	private static final int WIDTH = 600;
	private static final int HEIGHT = 300;

	// Zugriff auf die OxyGen-Oberfläche
	AuthorAccess aa;
	// Name des Tag-Elements, welches die Datumsangaben umschließen soll
	String elem;
	// Spiechert alle CheckBoxes in einer Liste, um die ausgewählten ermitteln
	// zu können
	List<JCheckBoxData> boxes = new ArrayList<>();
	// Speichert die ErrorPanels, um für einzele Listenelemente Fehler anzeigen
	// zu können
	List<JPanel> errorPanes = new ArrayList<>();
	// Nur für Listenelemente mit doppeldeutigen Angaben!
	// Mappt die CheckBox eines Listenelement, mit der ButtonGroup, in der die
	// verschiedenen ISO-Daten angegeben sind
	HashMap<JCheckBoxData, ButtonGroup> ambiguousGroup = new HashMap<>();
	// Label welches de Anzahl der Fehler wiedergibt
	JLabel errorMessage = new JLabel();

	public SearchGUI(List<SearchResult> list, AuthorAccess aa, String elem) {
		this.aa = aa;
		this.elem = elem;

		setTitle("Search Dates");
		addWindowListener(windowListener());

		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));

		JPanel headerPane = new JPanel();
		headerPane.setLayout(new BoxLayout(headerPane, BoxLayout.LINE_AXIS));
		JLabel chooseLabel = new JLabel("Wählen Sie alle zutreffenden Daten aus:");
		headerPane.add(chooseLabel);
		headerPane.add(Box.createHorizontalGlue());
		content.add(headerPane);

		JPanel back = new JPanel(new BorderLayout());

		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));

		for (SearchResult sr : list) {
			JPanel p = getPanel(sr);
			listPane.add(p);
		}

		back.add(listPane, BorderLayout.PAGE_START);
		back.add(Box.createRigidArea(new Dimension(0, 0)), BorderLayout.CENTER);

		JScrollPane jscp = new JScrollPane(back);
		jscp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		content.add(jscp);

		JPanel buttonPanel = new JPanel();
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(cancelListener());
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(okListener());
		JButton selectAll = new JButton("Select All");
		selectAll.addActionListener(selectAllListener());
		JButton clearAll = new JButton("Clear All");
		clearAll.addActionListener(clearAllListener());
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		buttonPanel.add(selectAll);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(clearAll);
		buttonPanel.add(Box.createHorizontalGlue());
		errorMessage.setForeground(new Color(255, 0, 0));
		buttonPanel.add(errorMessage);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(cancelButton);

		content.add(buttonPanel);

		add(content);

		pack();
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	/**
	 * Erstellt ein Listenelement pro SearchResult. Jedes Listenelement hat eine
	 * Checkbox, welches das Element zum Taggen auswählt. Auserdem wird der Text
	 * der Datumsangabe und das umgewandelte ISO-Format angegeben. Bei
	 * doppeldeutigen Daten, kann zwischen verschiedenen ISO-Daten ausgewählt
	 * werden.
	 */
	private JPanel getPanel(SearchResult sr) {
		GridBagLayout g = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints();
		JPanel p = new JPanel(g);

		JCheckBoxData cb = new JCheckBoxData("", false);
		cb.setData(sr);
		boxes.add(cb);

		gc.gridx = gc.gridy = 1;
		gc.gridheight = 2;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		gc.anchor = GridBagConstraints.WEST;
		p.add(cb, gc);

		String halftxt = sr.getHalfText();
		String date = sr.getText();
		String before = halftxt.substring(0, halftxt.indexOf(date));
		String after = halftxt.substring(halftxt.indexOf(date) + date.length(), halftxt.length());

		JLabel space1 = new JLabel();
		gc.gridx = 2;
		gc.weightx = 0.5;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.WEST;
		p.add(space1, gc);

		JLabel l1 = new JLabel("..." + before);
		gc.gridx = 3;
		gc.weightx = 0.0;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		p.add(l1, gc);

		JLabel l2 = new JLabel(date);
		gc.gridx = 4;
		gc.anchor = GridBagConstraints.CENTER;
		l2.setFont(new Font("Dialog", Font.BOLD, 12));
		p.add(l2, gc);

		JLabel l3 = new JLabel(after + "...");
		gc.gridx = 5;
		gc.anchor = GridBagConstraints.CENTER;
		p.add(l3, gc);

		JLabel space2 = new JLabel();
		gc.gridx = 6;
		gc.weightx = 0.5;
		gc.anchor = GridBagConstraints.EAST;
		p.add(space2, gc);

		if (sr.isAmbiguous()) {
			JPanel isoPane = new JPanel();
			isoPane.setLayout(new BoxLayout(isoPane, BoxLayout.LINE_AXIS));

			isoPane.add(Box.createHorizontalGlue());
			ButtonGroup bg = new ButtonGroup();
			for (DateOccurrence dO : sr.date.getDates()) {
				String l = dO.getISO();
				if (!dO.getName().isEmpty())
					l += " (" + dO.getName() + ")";
				JRadioButtonData jcb = new JRadioButtonData(l);
				jcb.setData(dO.getISO());
				jcb.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
				bg.add(jcb);
				isoPane.add(jcb);
			}
			isoPane.add(Box.createHorizontalGlue());

			gc.gridx = 2;
			gc.weightx = 1.0;
			gc.gridwidth = 5;
			gc.anchor = GridBagConstraints.CENTER;
			gc.gridy = 2;
			p.add(isoPane, gc);

			ambiguousGroup.put(cb, bg);
		} else {
			String iso = sr.getISO();
			JLabel d = new JLabel(iso);
			gc.gridx = 2;
			gc.weightx = 1.0;
			gc.gridwidth = 5;
			gc.anchor = GridBagConstraints.CENTER;
			gc.gridy = 2;
			p.add(d, gc);
		}

		JPanel errorPane = new JPanel();
		errorPane.setPreferredSize(new Dimension(15, 50));
		gc.gridy = 1;
		gc.gridx = 7;
		gc.gridheight = 2;
		gc.weightx = 0.0;
		gc.weighty = 1.0;
		gc.fill = GridBagConstraints.VERTICAL;
		p.add(errorPane, gc);
		errorPanes.add(errorPane);

		p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		p.setPreferredSize(new Dimension(400, 50));
		p.addMouseListener(panelListener(sr));

		return p;
	}

	private WindowListener windowListener() {
		WindowListener wl = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				e.getWindow().dispose();
			}
		};
		return wl;
	}

	private ActionListener cancelListener() {
		ActionListener al = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		return al;
	}

	/**
	 * Bei Doppelklick auf ein Listenelement wird ein weiterer Dialog geöffnet,
	 * indem die Datumsangabe und noch mehr Text um die angabe herum, angezeigt
	 * werden. Ein einfacher Klick, wählt die Datumsangabe im Dokument aus.
	 */
	private MouseListener panelListener(final SearchResult sr) {
		MouseListener ml = new ClickListener() {
			public void doubleClick(MouseEvent e) {
				new DetailDialog(sr);
			}

			public void singleClick(MouseEvent e) {
				aa.getEditorAccess().select(sr.getStart(), sr.getEnd() + 1);
			}
		};

		return ml;
	}

	/**
	 * Ein Klick auf den "Ok"-Button überprüft zunächst, ob die Listenelemente
	 * körrekt ausgewählt wurden und gibt wenn nötig eine Fehlermldung aus.
	 * Andernfalls werden die ausgewählten Elemente im Dokument getaggt.
	 */
	private ActionListener okListener() {
		ActionListener al = new AbstractAction() {
			private static final long serialVersionUID = 3053502849938658073L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int errorCount = 0;

				for (JPanel jp : errorPanes)
					jp.setBackground(UIManager.getColor("Panel.background"));
				errorMessage.setText("");
				repaint();

				List<JCheckBoxData> selected = new ArrayList<>();

				// Überprüfung, ob Listenelemente korrekt ausgewählt
				// wurden (doppeldeutige Daten)
				for (int i = 0; i < boxes.size(); i++) {
					JCheckBoxData box = boxes.get(i);
					if (box.isSelected()) {
						SearchResult sr = (SearchResult) box.getData();
						if (sr.isAmbiguous()) {
							ButtonGroup bg = ambiguousGroup.get(box);
							ButtonModel bm = bg.getSelection();
							if (bm == null) {
								errorPanes.get(i).setBackground(new Color(255, 0, 0));
								errorCount++;
							} else
								selected.add(box);
						} else
							selected.add(box);
					}
				}

				// Ein Elemente nicht korrekt ausgewählt --> Fehler
				if (errorCount > 0) {
					errorMessage.setText(errorCount + " Fehler!");
					repaint();
					return;
				}

				if (selected.isEmpty())
					return;

				// Taggt alle ausgewählten Elemente im Dokument
				for (int i = 0; i < selected.size(); i++) {
					JCheckBoxData box = selected.get(i);
					SearchResult sr = (SearchResult) box.getData();
					String iso = sr.getISO();

					if (sr.isAmbiguous()) {
						iso = getSelectedButtonText(ambiguousGroup.get(box));
					}

					String pattern1 = "<(\\w*)([\\s\\w\\\"\\=\\:\\/\\.\\-]*)?\\/>";
					String pattern2 = "<(\\w*)([\\s\\w\\\"\\=\\:\\/\\.\\-]*)?><\\/\\w*>";

					elem = elem.trim();
					String fragment = "";
					if (elem.matches(pattern1))
						fragment = elem.replaceAll(pattern1, "<$1$2 " + iso + "/>");
					else if (elem.matches(pattern2))
						fragment = elem.replaceAll(pattern2, "<$1$2 " + iso + "></$1>");

					// String fragment = "<" + elem + " " + iso + "></" + elem +
					// ">";
					try {
						aa.getDocumentController().surroundInFragment(fragment, sr.getStart() + (2 * i),
								sr.getEnd() + (2 * i));
					} catch (AuthorOperationException e1) {
						StringBuffer sb = new StringBuffer();
						sb.append(e.toString() + "\n");
						for (int j = 0; j < e1.getStackTrace().length; j++)
							sb.append(e1.getStackTrace()[j] + "\n");
						aa.getWorkspaceAccess().showInformationMessage(sb.toString());
					}
				}
				dispose();
			}
		};
		return al;
	}

	/**
	 * Wählt alle Listenelemente aus.
	 */
	private ActionListener selectAllListener() {
		ActionListener al = new AbstractAction() {
			private static final long serialVersionUID = -8570013691491252808L;

			@Override
			public void actionPerformed(ActionEvent e) {
				for (JCheckBoxData box : boxes)
					box.setSelected(true);
			}
		};
		return al;
	}

	/**
	 * Wählt alle Listenelemente ab.
	 */
	private ActionListener clearAllListener() {
		ActionListener al = new AbstractAction() {
			private static final long serialVersionUID = -2648691595176701135L;

			@Override
			public void actionPerformed(ActionEvent e) {
				for (JCheckBoxData box : boxes)
					box.setSelected(false);
			}
		};
		return al;
	}

	private String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			JRadioButtonData button = (JRadioButtonData) buttons.nextElement();

			if (button.isSelected()) {
				return (String) button.getData();
			}
		}

		return null;
	}
}
