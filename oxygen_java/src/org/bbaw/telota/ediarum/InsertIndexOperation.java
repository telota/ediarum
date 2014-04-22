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
 * InsertIndexOperation.java - is a class to surround a selection with a register elements.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.0.5
 */
package org.bbaw.telota.ediarum;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import java.awt.Frame;


public class InsertIndexOperation implements AuthorOperation{
	/**
	 * Argument describing the URL.
	 */
	private static final String ARGUMENT_URL = "URL";

	/**
	 * Argument describing the node.
	 */
	private static final String ARGUMENT_NODE = "node";

	/**
	 * Argument describing the expression.
	 */
	private static final String ARGUMENT_EXPRESSION = "expression";

	/**
	 * Argument describing the ID for all elements.
	 */
	private static final String ARGUMENT_ID = "id";

	/**
	 * Argument describing the insertNode.
	 */
	private static final String ARGUMENT_ELEMENT = "element";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_URL,
				ArgumentDescriptor.TYPE_STRING,
				"Die URL der Registerdatei, etwa: " +
				"http://user:passwort@www.example.com:port/exist/webdav/db/register.xml"),
		new ArgumentDescriptor(
				ARGUMENT_NODE,
				ArgumentDescriptor.TYPE_STRING,
				"Der XPath-Ausdruck zu den wählbaren Elementen, etwa: //person"),
		new ArgumentDescriptor(
				ARGUMENT_EXPRESSION,
				ArgumentDescriptor.TYPE_STRING,
				"Der in der Auswahlliste erscheinende Ausdruck mit Sub-Elementen, etwa: " +
				"/name+\", \"+/vorname+\" \"+/lebensdaten"),
		new ArgumentDescriptor(
				ARGUMENT_ID,
				ArgumentDescriptor.TYPE_STRING,
				"Die im Element mehrfach zu verwendende ID"),
		new ArgumentDescriptor(
				ARGUMENT_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"Das an der durch $SELECTION gekennzeichneten Textstelle einzufügende Element, etwa: " +
				"\"<index xmlns='http://www.tei-c.org/ns/1.0' spanTo='$[ID]'" +
				" indexName='personen' corresp='\" + @id + \"'>" +
				"<term xmlns='http://www.tei-c.org/ns/1.0'>\" + /name + \", \" + /vorname + \"</term></index>" +
				"$[SELECTION]"+
				"<anchor xmlns='http://www.tei-c.org/ns/1.0' xml:id=" +
				"'$[ID]' />\""),
		//		"<index xmlns='http://www.tei-c.org/ns/1.0' spanTo='$[ID]' indexName='personen' corresp='$[@id]'>" +
		//		"<term xmlns='http://www.tei-c.org/ns/1.0'>$[/name], $[/vorname]</term></index>" +
		//		"$[SELECTION]"+
		//		"<anchor xmlns='http://www.tei-c.org/ns/1.0' xml:id='$[ID]' />"),
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen ..
		Object urlArgVal = args.getArgumentValue(ARGUMENT_URL);
		Object nodeArgVal = args.getArgumentValue(ARGUMENT_NODE);
		Object expressionArgVal = args.getArgumentValue(ARGUMENT_EXPRESSION);
		Object elementArgVal = args.getArgumentValue(ARGUMENT_ELEMENT);
		Object idArgVal = args.getArgumentValue(ARGUMENT_ID);
		// .. und überprüft.
		if (idArgVal == null) {
			idArgVal = "";
		}
		if (urlArgVal != null
				&& urlArgVal instanceof String
				&& nodeArgVal != null
				&& nodeArgVal instanceof String
				&& expressionArgVal != null
				&& expressionArgVal instanceof String
				&& elementArgVal != null
				&& elementArgVal instanceof String
				&& idArgVal != null
				&& idArgVal instanceof String) {
			// Wenn im aktuellen Dokument nichts selektiert ist, wird das aktuelle Wort ausgewählt.
			if (!authorAccess.getEditorAccess().hasSelection()) {
				authorAccess.getEditorAccess().selectWord();
			}
			int selStart = authorAccess.getEditorAccess().getSelectionStart();
			int selEnd = authorAccess.getEditorAccess().getSelectionEnd();

			// Für die spätere Verwendung werden die Variablen für die Registereinträge und Elemente erzeugt.
			String[] eintrag = null, elements = null;

			// Dann wird das Registerdokument eingelesen, wobei auf die einzelnen Registerelement und 
			// die Ausdrücke für die Einträge und Elemente Rücksicht genommen wird.
			ReadRegister register = new ReadRegister((String)urlArgVal, (String) nodeArgVal, (String) expressionArgVal, (String) elementArgVal);
			// Die Arrays für die Einträge und IDs werden an die lokalen Variablen übergeben.
			eintrag = register.getEintrag();
			elements = register.getID();

			// Dafür wird der RegisterDialog geöffnet und erhält die Einträge und Elemente als Parameter.
			InsertRegisterDialog RegisterDialog = new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(), eintrag, elements);
			// Wenn in dem Dialog ein Eintrag ausgewählt wurde, .. 
			if (!RegisterDialog.getSelectedID().isEmpty()){
				// .. wird in den entsprechenden Elementen die eingestellte ID eingefügt, .. 
				String[] selectedIDInParts = RegisterDialog.getSelectedID().split("\\$\\[ID\\]");
				String selectedID = selectedIDInParts[0];
				for (int i=1; i<selectedIDInParts.length; i++) {
					selectedID += idArgVal + selectedIDInParts[i];
				}
				// .. und dann werden im aktuellen Dokument um die Selektion die entsprechenden Elemente eingesetzt.
				String[] surroundElements = selectedID.split("\\$\\[SELECTION\\]");
				authorAccess.getDocumentController().insertXMLFragment(surroundElements[1], selEnd);
				authorAccess.getDocumentController().insertXMLFragment(surroundElements[0], selStart);
			}
		} else {
			throw new IllegalArgumentException(
					"One or more of the argument values are not declared, they are: url - " + urlArgVal 
					+ ", node - " + nodeArgVal + ", expression - " + expressionArgVal
					+ ", element - " + elementArgVal + ", id - " + idArgVal);
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
		return "Öffnet einen Dialog, in welchem ein Eintrag aus einem Register" +
				" ausgewählt werden kann. Ein Element mit der entsprechenden ID wird um die markierte" +
				" Stelle herum eingefügt.";
	}
}
