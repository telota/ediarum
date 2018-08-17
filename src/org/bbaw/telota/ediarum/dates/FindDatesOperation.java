package org.bbaw.telota.ediarum.dates;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.text.Segment;

import org.bbaw.pdr.dates.config.java.AmbiguousDate;
import org.bbaw.pdr.dates.config.java.Configurator;
import org.bbaw.pdr.dates.config.java.DatesResult;
import org.bbaw.pdr.dates.config.java.Tools;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.access.AuthorEditorAccess;
import ro.sync.ecss.extensions.api.access.AuthorWorkspaceAccess;

/**
 * Eine Oxygen Author-Operation, die im ausgewählten Text alle Datumsangaben
 * sucht, sie mit Hilfe einer GUI ausgibt und ausgewählte Angaben im Text taggt
 *
 * @author Philipp Belitz
 */

public class FindDatesOperation implements AuthorOperation {

	private static final int AROUND_LIMIT = 50;

	/**
	 * Der absolute Pfad, in dem die Konfigurationsdatei zu finden ist.
	 */
	private static final String ARGUMENT_PATH = "path";
	/**
	 * Das XML-Fragment, welches die erkannten Datumsangaben umschließen soll.
	 */
	private static final String ARGUMENT_ELEMENT = "element";

	/**
	 * Die Argumente.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
			new ArgumentDescriptor(ARGUMENT_PATH, ArgumentDescriptor.TYPE_STRING,
					"Path to the config file of the date recognition app."),
			new ArgumentDescriptor(ARGUMENT_ELEMENT, ArgumentDescriptor.TYPE_STRING,
					"The XML fragment surrounding the identified dates, e.g.:\n"
							+ "<date xmlns='http://www.tei-c.org/ns/1.0'/>") };

	@Override
	public String getDescription() {
		return "Searches in the selection for dates, the results are shown as selectable list for tagging.";
	}

	@Override
	public void doOperation(AuthorAccess arg0, ArgumentsMap arg1)
			throws IllegalArgumentException, AuthorOperationException {
		// die übergebenen Argumente
		final Object path = arg1.getArgumentValue(ARGUMENT_PATH);
		final Object elem = arg1.getArgumentValue(ARGUMENT_ELEMENT);

		String pattern1 = "<(\\w*)([\\s\\S]*)?\\/>";
		String pattern2 = "<(\\w*)([\\s\\S]*)?><\\/\\w*>";

		// Überprüfung, ob die Argumente angegeben wurden und valide sind
		if (path == null || !(path instanceof String) || elem == null || !(elem instanceof String)
				|| ((String) path).isEmpty() || ((String) elem).isEmpty())
			throw new IllegalArgumentException("One or more of the argument values are not declared, they are: path - "
					+ path + ", elem - " + elem);

		if (!((String) elem).trim().matches(pattern1) && !((String) elem).trim().matches(pattern2))
			throw new IllegalArgumentException("The argument elem doesn't have the required syntax");

		final AuthorAccess aa = arg0;
		final AuthorEditorAccess aea = arg0.getEditorAccess();
		final AuthorDocumentController adc = arg0.getDocumentController();
		final AuthorWorkspaceAccess awa = arg0.getWorkspaceAccess();

		try {

			File f = new File((String) path);
			if (!f.exists())
				throw new FileNotFoundException("Couldn't find file denoted by argument path - " + path);

			// während der Datumserkenner in der Datei such, soll ein Ladebalken
			// die Wartezeit überbrücken. Das wird mit einem SwingWorker gelöst
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() {
					try {
						int start = aea.getBalancedSelectionStart();
						int end = aea.getBalancedSelectionEnd();

						// falls kein Text ausgewählt wurde, wird im gesamten
						// Dokument gesucht
						if (end - start <= 0) {
							start = 0;
							end = adc.getAuthorDocumentNode().getLength();
						}

						Segment seg = new Segment();
						adc.getChars(start, end - start, seg);

						String txt = seg.toString();

						if (txt.isEmpty())
							return null;

						// Datumserkenner initiieren
						Configurator conf = new Configurator();
						conf.read((String) path);

						DatesResult res = new DatesResult(txt);
						Tools tool = Tools.getInstance();
						tool.init(conf.getYearStart(), conf.getYearEnd(), conf.getPatterns(), conf.getToken());

						// Daten erkennen
						tool.findOccurences(res);
						// awa.showInformationMessage(debug(res));
						List<SearchResult> list = new ArrayList<SearchResult>();
						for (int i = 0; i < res.getOccurrencesConfig().size(); i++) {
							AmbiguousDate date = res.getOccurrencesConfig().get(i);
							int date_start = start + date.getStart();
							int date_end = date_start + date.getLength() - 1;

							// parse Text um die erkannte Datumsangabe herum
							int around_start, around_end = 0;
							if (date_start <= AROUND_LIMIT)
								around_start = 0;
							else
								around_start = date_start - AROUND_LIMIT;

							if (date_end + AROUND_LIMIT > adc.getAuthorDocumentNode().getLength())
								around_end = adc.getAuthorDocumentNode().getLength();
							else
								around_end = date_end + AROUND_LIMIT;

							Segment around_seg = new Segment();
							adc.getChars(around_start, around_end - around_start, around_seg);
							String around_txt = around_seg.toString();

							// awa.showInformationMessage("date_start: " +
							// date_start +
							// "\ndate_end: " + date_end + "\naround_start: "
							// + around_start + "\naround_end: " + around_end);

							// speicher erkannte Angaben in Liste
							list.add(
									new SearchResult(date, date_start, date_end, around_txt, around_start, around_end));
						}

						// GUI zur Anzeige der gefundenen Datumsangaben
						new SearchGUI(list, aa, (String) elem);

					} catch (Exception e) {
						StringBuffer sb = new StringBuffer();
						sb.append(e.toString() + "\n");
						for (int i = 0; i < e.getStackTrace().length; i++)
							sb.append(e.getStackTrace()[i] + "\n");
						awa.showErrorMessage(sb.toString());
					}
					return null;
				}
			};

			// sobald alle Datumsangaben erkannt worden sind, soll der
			// Ladebalken-Dialog verschwinden
			final JDialog dialog = new JDialog();
			sw.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getPropertyName().equals("state")) {
						if (evt.getNewValue() == SwingWorker.StateValue.DONE) {
							dialog.dispose();
						}
					}
				}
			});
			sw.execute();

			// Ladebalken-Dialog
			JProgressBar progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			JPanel panel = new JPanel(new BorderLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			panel.add(progressBar, BorderLayout.CENTER);
			panel.add(new JLabel("Bitte warten..."), BorderLayout.PAGE_START);
			dialog.add(panel);
			dialog.setTitle("loading");
			dialog.pack();
			dialog.setLocationRelativeTo(null);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);

			// Ausgabe von Exception in OxyGen
		} catch (Exception e) {
			StringBuffer sb = new StringBuffer();
			sb.append(e.toString() + "\n");
			for (int i = 0; i < e.getStackTrace().length; i++)
				sb.append(e.getStackTrace()[i] + "\n");
			awa.showErrorMessage(sb.toString());
		}
	}

	@Override
	public ArgumentDescriptor[] getArguments() {
		return ARGUMENTS;
	}

}
