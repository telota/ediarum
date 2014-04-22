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
 * InsertRegisterAttributeOperation.java - is a class for inserting a register attribute to a selection.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.1.2
 */
package org.bbaw.telota.ediarum;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import java.awt.Frame;
import javax.swing.text.BadLocationException;

public class InsertRegisterAttributeOperation implements AuthorOperation{
	/**
	 * Argument describing the url.
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
	 * Argument describing the new attribute name.
	 */
	private static final String ARGUMENT_ATTRIBUTENAME = "attribute name";

	/**
	 * Argument describing the xpath to the element with the new attribute.
	 */
	private static final String ARGUMENT_XPATHFROMSELECTION = "xpath to element of the attribute";

	/**
	 * Argument describing the new attribute.
	 */
	private static final String ARGUMENT_ATTRIBUTEVALUE = "attribute value";

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
				"Der in der Auswahlliste erscheinende Ausdruck mit Sub-Elementen" +
						" (beginnend mit '\"\"', '@', '/', '//' oder '.'), etwa: " +
				"//name+\", \"+//vorname+\" \"+//lebensdaten"),
		new ArgumentDescriptor(
				ARGUMENT_ATTRIBUTENAME,
				ArgumentDescriptor.TYPE_STRING,
				"Der Name des an der Textstelle einzufügenden Attributes, etwa: " +
				"key"),
		new ArgumentDescriptor(
				ARGUMENT_XPATHFROMSELECTION,
				ArgumentDescriptor.TYPE_STRING,
				"Ein relativer XPath-Ausdruck des die Textstelle umgebenden Elementes zu dem Element, " +
				"wo das Attribut eingefügt werden soll: " +
				"./child"),
		new ArgumentDescriptor(
				ARGUMENT_ATTRIBUTEVALUE,
				ArgumentDescriptor.TYPE_STRING,
				"Der Inhalt des an der Textstelle einzufügenden Attributes, etwa: " +
				"\"etwas Text\" + @id")
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen ..
		Object urlArgVal = args.getArgumentValue(ARGUMENT_URL);
		Object nodeArgVal = args.getArgumentValue(ARGUMENT_NODE);
		Object expressionArgVal = args.getArgumentValue(ARGUMENT_EXPRESSION);
		Object attributenameArgVal = args.getArgumentValue(ARGUMENT_ATTRIBUTENAME);
		Object xpathfromselectionArgVal = args.getArgumentValue(ARGUMENT_XPATHFROMSELECTION);
		Object attributevalArgVal = args.getArgumentValue(ARGUMENT_ATTRIBUTEVALUE);
		// .. und überprüft.
		if (urlArgVal != null
				&& urlArgVal instanceof String
				&& nodeArgVal != null
				&& nodeArgVal instanceof String
				&& expressionArgVal != null
				&& expressionArgVal instanceof String
				&& attributenameArgVal != null
				&& attributenameArgVal instanceof String
				&& xpathfromselectionArgVal != null
				&& xpathfromselectionArgVal instanceof String
				&& attributevalArgVal != null
				&& attributevalArgVal instanceof String) {
			// Wenn im aktuellen Dokument nichts selektiert ist, wird das aktuelle Wort ausgewählt.
			if (!authorAccess.getEditorAccess().hasSelection()) {
				authorAccess.getEditorAccess().selectWord();
			}
			int selStart = authorAccess.getEditorAccess().getSelectionStart();

			// Für die spätere Verwendung werden die Variablen für die Registereinträge und IDs erzeugt.
			String[] eintrag = null, id = null;
			
			// Dann wird das Registerdokument eingelesen, wobei auf die einzelnen Registerelement ..
			// .. und die Ausdrücke für die Einträge und IDs Rücksicht genommen wird.
			ReadRegister register = new ReadRegister((String)urlArgVal, (String) nodeArgVal, (String) expressionArgVal, (String) attributevalArgVal);
			// Die Arrays für die Einträge und IDs werden an die lokalen Variablen übergeben.
			eintrag = register.getEintrag();
			id = register.getID();

			// Dafür wird der RegisterDialog geöffnet und erhält die Einträge und IDs als Parameter.
			InsertRegisterDialog RegisterDialog = new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(), eintrag, id);
			// Wenn in dem Dialog ein Eintrag ausgewählt wurde, .. 
			if (!RegisterDialog.getSelectedID().isEmpty()){
				// wird im aktuellen Dokument dem die Selektion umgebenden Element das entsprechende Attribut mit der ID hinzugefügt.
				AuthorElement selElement;
				try {
					AuthorNode selNode = authorAccess.getDocumentController().getNodeAtOffset(selStart);
					selElement = (AuthorElement) (authorAccess.getDocumentController().findNodesByXPath((String) xpathfromselectionArgVal, selNode, false, true, true, false))[0];
					authorAccess.getDocumentController().setAttribute((String) attributenameArgVal, new AttrValue(RegisterDialog.getSelectedID()), selElement);
				} catch (BadLocationException e) {}
			}
		} else {
			throw new IllegalArgumentException(
					"One or more of the argument values are not declared, they are: url - " + urlArgVal 
					+ ", node - " + nodeArgVal + ", expression - " + expressionArgVal
					+ ", attribute name - " + attributenameArgVal + ", xpath element to attribute - " + xpathfromselectionArgVal
					+ ", attribute value - " + attributevalArgVal);
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
		return "Öffnet einen Dialog, in welchem Einträge eines Registers" +
				" ausgewählt werden kann. Ein Element mit der entsprechenden ID wird an der markierten" +
				" Stelle eingefügt.";
	}
}




