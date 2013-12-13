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
 * InsertLinkOperation.java - is a class for inserting a link element to a link target from another open file.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.2
 */
package org.bbaw.telota.ediarum;

import java.awt.Frame;
import java.net.URL;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.filter.AuthorFilteredContent;

import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;

public class InsertLinkOperation implements AuthorOperation {
	/**
	 * Argument describing the root-path.
	 */
	private static final String ARGUMENT_PATH = "root-path";

	/**
	 * Argument describing the xpath to the source-element.
	 */
	private static final String ARGUMENT_XPATH = "xpath";

	/**
	 * Argument describing the id-Attribute of the source-element.
	 */
	private static final String ARGUMENT_ID = "id-attribute";

	/**
	 * Argument describing the prefix of the id-Attribute of the source-element.
	 */
	private static final String ARGUMENT_IDSTARTPREFIX = "id start prefix";

	/**
	 * Argument describing the prefix of the id-Attribute of the source-element.
	 */
	private static final String ARGUMENT_IDSTOPPREFIX = "id stop prefix";

	/**
	 * Argument describing the element.
	 */
	private static final String ARGUMENT_ELEMENT = "element";

	/**
	 * Argument describing the element without marked ID.
	 */
	private static final String ARGUMENT_ALTELEMENT = "altern. element";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_PATH,
				ArgumentDescriptor.TYPE_STRING,
				"Der Pfad zur Datenbank, etwa: /exist/webdav/db/"),
		new ArgumentDescriptor(
				ARGUMENT_XPATH,
				ArgumentDescriptor.TYPE_STRING,
				"Der xPath-Ausdruck zu dem zu verlinkenden Element, etwa: " +
				"//anchor"),
		new ArgumentDescriptor(
				ARGUMENT_ID,
				ArgumentDescriptor.TYPE_STRING,
				"Der Name des ID-Attributes des zu verlinkenden Elements, etwa: " +
				"xml:id"),
		new ArgumentDescriptor(
				ARGUMENT_IDSTARTPREFIX,
				ArgumentDescriptor.TYPE_STRING,
				"Das ID-Prefix des vorderen zu verlinkenden Elements, etwa: " +
				"start_"),
		new ArgumentDescriptor(
				ARGUMENT_IDSTOPPREFIX,
				ArgumentDescriptor.TYPE_STRING,
				"Das ID-Prefix des hinteren zu verlinkenden Elements, etwa: " +
				"stop_"),
		new ArgumentDescriptor(
				ARGUMENT_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"Das einzufügende Element, mit dem $-Zeichen können $FILEPATH, $FILE_ID, $STARTPREFIX, $STOPPREFIX, $ID benutzt werden, etwa: " +
				"<ref xmlns='http://www.tei-c.org/ns/1.0' target='$FILEPATH/#$STARTPREFIX$ID'/>"),
		new ArgumentDescriptor(
				ARGUMENT_ALTELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"Das einzufügende Element wenn auf eine ganze Datei verwiesen wird, mit dem $-Zeichen können $FILEPATH, $FILE_ID benutzt werden, etwa: " +
				"<ref xmlns='http://www.tei-c.org/ns/1.0' target='$FILEPATH'/>")
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen ..
		Object pathArgVal = args.getArgumentValue(ARGUMENT_PATH);
		Object xpathArgVal = args.getArgumentValue(ARGUMENT_XPATH);
		Object idArgVal = args.getArgumentValue(ARGUMENT_ID);
		Object idstartArgVal = args.getArgumentValue(ARGUMENT_IDSTARTPREFIX);
		Object idstopArgVal = args.getArgumentValue(ARGUMENT_IDSTOPPREFIX);
		Object elementArgVal = args.getArgumentValue(ARGUMENT_ELEMENT);
		Object altelementArgVal = args.getArgumentValue(ARGUMENT_ALTELEMENT);
		// .. und überprüft.
		if (pathArgVal != null
				&& pathArgVal instanceof String
				&& xpathArgVal != null
				&& xpathArgVal instanceof String
				&& idArgVal != null
				&& idArgVal instanceof String
				&& idstartArgVal != null
				&& idstartArgVal instanceof String
				&& idstopArgVal != null
				&& idstopArgVal instanceof String
				&& elementArgVal != null
				&& elementArgVal instanceof String
				&& altelementArgVal != null
				&& altelementArgVal instanceof String) {
			// Wenn im aktuellen Dokument nichts selektiert ist, wird das aktuelle Wort ausgewählt.
			if (!authorAccess.getEditorAccess().hasSelection()) {
				authorAccess.getEditorAccess().selectWord();
			}
			int selStart = authorAccess.getEditorAccess().getSelectionStart();
			int selEnd = authorAccess.getEditorAccess().getSelectionEnd()-1;

			// Es werden die URLs aller offenen Dateien gelesen.
			URL[] openFiles = authorAccess.getWorkspaceAccess().getAllEditorLocations();
			// Zum Pfadvergleich wird die Pfadvariable gelesen, ..
			String Pfad = (String)pathArgVal;
			// .. und die Arrays für die Einträge und IDs werden vorbereitet.
			String[] alleDateien = new String[openFiles.length];
			String[] alleDateiID = new String[openFiles.length];
			String[][] alleEintraege = new String[openFiles.length][];
			String[][] alleLinkIDs = new String[openFiles.length][];
			
			// Die Zahl der gültigen Dateien ist zunächst 0.
			int dateiAnzahl = 0;
			// Für jede Datei ..
			for (int i=0; i<openFiles.length; i++) {
				// .. wird überprüft, ob sie in dem deklarierten Pfad liegt, erst dann ..
				if (openFiles[i].getFile().startsWith(Pfad)) {
					// .. wird sie in die Liste aufgenommen.
					alleDateien[i] = openFiles[i].getFile().replace(Pfad, "");
					dateiAnzahl += 1;
					// Von der Datei ..
					WSEditorPage filePage = authorAccess.getWorkspaceAccess().getEditorAccess(openFiles[i]).getCurrentPage();
					if(filePage instanceof WSAuthorEditorPage) 
					{
						WSAuthorEditorPage fileAuthorPage = (WSAuthorEditorPage) filePage;
						// .. wird die ID in die Liste aufgenommen, ..
						alleDateiID[i] = fileAuthorPage.getDocumentController().getAuthorDocumentNode().getRootElement().getAttribute("xml:id").getValue().toString();
						// .. es werden alle Referenzziele entsprechend der gesetzten Variablen herausgefiltert und ..
						AuthorNode[] linkNodes = fileAuthorPage.getDocumentController().findNodesByXPath((String)xpathArgVal + "[starts-with(@" + (String)idArgVal + ",'" + (String)idstartArgVal + "')]", false, true, true);
						// .. die Arrays für die Einträge und IDs entsprechend vorbereitet.
						alleEintraege[i] = new String[linkNodes.length];
						alleLinkIDs[i] = new String[linkNodes.length];
						// Falls es Referenzziele gibt, ..
						if(linkNodes.length!=0 && linkNodes[0].getType() == AuthorNode.NODE_TYPE_ELEMENT) {
							// .. wird für jedes ..
							for (int j=0; j<linkNodes.length; j++) {
								// .. die ID im Array gespeichert, ..
								alleLinkIDs[i][j] = ((AuthorElement)linkNodes[j]).getAttribute((String)idArgVal).getValue().toString().substring(((String)idstartArgVal).length());
								// .. weiterhin werden Beginn und Ende des Verweiszieles gefunden ..
								int linkPosition = linkNodes[j].getEndOffset();
								int endPosition;
								if (fileAuthorPage.getDocumentController().findNodesByXPath((String)xpathArgVal + "[@" + (String)idArgVal + "='" + (String)idstopArgVal + alleLinkIDs[i][j] + "']", false, true, true).length!=0){
									endPosition = fileAuthorPage.getDocumentController().findNodesByXPath((String)xpathArgVal + "[@" + (String)idArgVal + "='" + (String)idstopArgVal + alleLinkIDs[i][j] + "']", false, true, true)[0].getStartOffset();
								} else {
									endPosition = fileAuthorPage.getDocumentController().getXPathLocationOffset("/", AuthorConstants.POSITION_AFTER);
								}
								// .. und der entsprechende Text des Verweiszieles wird gelesen und als Eintrag im Array gespeichert.
								AuthorFilteredContent textNode = fileAuthorPage.getDocumentController().getFilteredContent(linkPosition, endPosition, null);
								alleEintraege[i][j] = textNode.toString();
							}
						}
					}
				} else {
					alleDateien[i] = "";
				}
			}
			// Die Arrays für die Dateien aus dem richtigen Pfad werden vorbereitet, ..
			String[] Datei = new String[dateiAnzahl];
			String[] DateiID = new String[dateiAnzahl];
			String[][] Eintrag = new String[dateiAnzahl][];
			String[][] LinkID = new String[dateiAnzahl][];
			// .. und die entsprechenden Daten übernommen.
			for (int i=0, j=0; i<alleDateien.length; i++) {
				if (!alleDateien[i].isEmpty()) {
					Datei[j] = alleDateien[i];
					DateiID[j] = alleDateiID[i];
					Eintrag[j] = alleEintraege[i];
					LinkID[j] = alleLinkIDs[i]; 
					j++;
				}
			}
			// Ein Dialog zur Auswahl der Datei und der Verweiszieles wird geöffnet.
			InsertLinkDialog LinkDialog = new InsertLinkDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(), Datei, DateiID, Eintrag, LinkID);
			// Falls ein Verweisziel ausgewählt wurde, ..
			if (!LinkDialog.getSelectedID().isEmpty()) {
				// .. wird zunächst der Parameter für das einzufügende Element zerteilt, ..
				String elementString = (String)elementArgVal;
				elementString = elementString.replace("$FILEPATH", "++$FILEPATH++");
				elementString = elementString.replace("$FILE_ID", "++$FILE_ID++");
				elementString = elementString.replace("$STARTPREFIX", "++$STARTPREFIX++");
				elementString = elementString.replace("$STOPPREFIX", "++$STOPPREFIX++");
				elementString = elementString.replace("$ID", "++$ID++");
				String[] elementStrings = elementString.split("[+][+]");
				// .. und dann wird das Element von Null an ..
				elementString = "";
				// .. aus den einzelnen Teilen zusammengesetzt. Die Teile ..
				for (int i=0; i<elementStrings.length; i++) {
					// .. können den Dateipfad hinter dem allgemeine Pfad bezeichnen, ..
					if (elementStrings[i].equals("$FILEPATH")) {
						elementString += LinkDialog.getSelectedFile();
						// .. oder die ID der Datei, ..
					} else if (elementStrings[i].equals("$FILE_ID")) {
						elementString += LinkDialog.getSelectedFileID();
						// .. eventuell auch den Startprefix, ..
					} else if (elementStrings[i].equals("$STARTPREFIX")) {
						elementString += (String)idstartArgVal;
						// .. den Stopprefix ..
					} else if (elementStrings[i].equals("$STOPPREFIX")) {
						elementString += (String)idstopArgVal;
						// .. oder die ID selbst, ..
					} else if (elementStrings[i].equals("$ID")) {
						elementString += LinkDialog.getSelectedID();
						// .. alle übrigen Teile werden als Strings übernommen.
					} else {
						elementString += elementStrings[i];
					}
				}
				// Das so konstruierte Element wird schließlich an der richtigen Stelle eingesetzt.
				authorAccess.getDocumentController().surroundInFragment(elementString, selStart, selEnd);
				// Falls kein Verweisziel ausgewählt wurde, aber eine Datei ausgewählt worden ist, ..
			} else if (!LinkDialog.getSelectedFile().isEmpty()) {
				// .. wird zunächst wieder der Parameter für das einzufügende Element zerteilt, ..
				String elementString = (String)altelementArgVal;
				elementString = elementString.replace("$FILEPATH", "++$FILEPATH++");
				elementString = elementString.replace("$FILE_ID", "++$FILE_ID++");
				String[] elementStrings = elementString.split("[+][+]");
				// .. und dann wird das Element auch von Null an ..
				elementString = "";
				// .. aus den einzelnen Teilen zusammengesetzt. Die Teile ..
				for (int i=0; i<elementStrings.length; i++) {
					// .. können den Dateipfad hinter dem allgemeine Pfad bezeichnen, ..
					if (elementStrings[i].equals("$FILEPATH")) {
						elementString += LinkDialog.getSelectedFile();
						// .. oder die ID der Datei, ..
					} else if (elementStrings[i].equals("$FILE_ID")) {
						elementString += LinkDialog.getSelectedFileID();
						// .. alle übrigen Teile werden als Strings übernommen.
					} else {
						elementString += elementStrings[i];
					}
				}
				// Das so konstruierte Element wird schließlich an der richtigen Stelle eingesetzt.
				authorAccess.getDocumentController().surroundInFragment(elementString, selStart, selEnd);
			}
		} else {
			throw new IllegalArgumentException(
					"One or more of the argument values are not declared, they are: root-path - " + pathArgVal 
					+ ", xpath - " + xpathArgVal + ", id-attribute - " + idArgVal + ", id start prefix - " + idstartArgVal
					+ ", id stop prefix - " + idstopArgVal);
		}
	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
	 */
	public ArgumentDescriptor[] getArguments() {
		return ARGUMENTS;
	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
	 */
	public String getDescription() {
		return "Öffnet einen Dialog, der die möglichen Verweisziele in den geöffneten Dateien anzeigt." +
				" Bei Bestätigung wird an der Markierung ein entsprechendes Link-Element eingefügt.";
	}

}
