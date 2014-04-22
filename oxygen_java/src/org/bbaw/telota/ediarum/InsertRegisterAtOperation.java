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
 * InsertRegisterAtOperation.java - is a class for inserting a register element to a specified position.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.0.2
 */
package org.bbaw.telota.ediarum;

//import org.eclipse.swt.widgets.Display;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.schemaaware.SchemaAwareHandlerResult;
import ro.sync.ecss.extensions.api.schemaaware.SchemaAwareHandlerResultInsertConstants;
import ro.sync.ecss.extensions.commons.operations.MoveCaretUtil;
import java.awt.Frame;

public class InsertRegisterAtOperation implements AuthorOperation{
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
	 * Argument describing the insertNode.
	 */
	private static final String ARGUMENT_ELEMENT = "element";

	/**
	 * The insert location argument.
	 * The value is <code>insertLocation</code>.
	 */
	private static final String ARGUMENT_XPATH_LOCATION = "insertLocation";
	/**
	 * The insert position argument.
	 * The value is <code>insertPosition</code>.
	 */
	private static final String ARGUMENT_RELATIVE_LOCATION = "insertPosition";

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
				"Der in der Auswahlliste erscheinende Ausdruck mit Sub-Elementen (beginnend mit '\"\"', '@', '/', '//' oder '.'), etwa: " +
				"//name+\", \"+//vorname+\" \"+//lebensdaten"),
		new ArgumentDescriptor(
				ARGUMENT_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"Das an der Textstelle einzufügende Element, etwa: " +
				"\"<persName xmlns='http://www.tei-c.org/ns/1.0' key='\" + @id + \"' />\""),
		// Argument defining the location where the operation will be executed as an XPath expression.
		new ArgumentDescriptor(
				ARGUMENT_XPATH_LOCATION, 
				ArgumentDescriptor.TYPE_XPATH_EXPRESSION, 
				"An XPath expression indicating the insert location for the fragment.\n" +
				"Note: If it is not defined then the insert location will be at the caret."),
		// Argument defining the relative position to the node obtained from the XPath location.
		new ArgumentDescriptor(
				ARGUMENT_RELATIVE_LOCATION, 
				ArgumentDescriptor.TYPE_CONSTANT_LIST,
				"The insert position relative to the node determined by the XPath expression.\n" +
				"Can be: " +
				AuthorConstants.POSITION_BEFORE + ", " +
				AuthorConstants.POSITION_INSIDE_FIRST + ", " +
				AuthorConstants.POSITION_INSIDE_LAST + " or " +
				AuthorConstants.POSITION_AFTER + ".\n" +
				"Note: If the XPath expression is not defined this argument is ignored",
				new String[] {
				AuthorConstants.POSITION_BEFORE,
				AuthorConstants.POSITION_INSIDE_FIRST,
				AuthorConstants.POSITION_INSIDE_LAST,
				AuthorConstants.POSITION_AFTER,
				}, 
				AuthorConstants.POSITION_INSIDE_FIRST),
		SCHEMA_AWARE_ARGUMENT_DESCRIPTOR		         
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
		Object xpathLocation = args.getArgumentValue(ARGUMENT_XPATH_LOCATION);
		Object relativeLocation = args.getArgumentValue(ARGUMENT_RELATIVE_LOCATION);
		// .. und überprüft.
		if (urlArgVal != null
				&& urlArgVal instanceof String
				&& nodeArgVal != null
				&& nodeArgVal instanceof String
				&& expressionArgVal != null
				&& expressionArgVal instanceof String
				&& elementArgVal != null
				&& elementArgVal instanceof String) {

			// Für die spätere Verwendung werden die Variablen für die Registereinträge und IDs erzeugt.
			String[] eintrag = null, id = null;

			// Dann wird das Registerdokument eingelesen, wobei auf die einzelnen Registerelement und 
			// die Ausdrücke für die Einträge und IDs Rücksicht genommen wird.
			ReadRegister register = new ReadRegister((String)urlArgVal, (String) nodeArgVal, (String) expressionArgVal, (String) elementArgVal);
			// Die Arrays für die Einträge und IDs werden an die lokalen Variablen übergeben.
			eintrag = register.getEintrag();
			id = register.getID();

			// Dafür wird der RegisterDialog geöffnet und erhält die Einträge und IDs als Parameter.
			InsertRegisterDialog RegisterDialog = new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(), eintrag, id);
			// Wenn in dem Dialog ein Eintrag ausgewählt wurde, .. 
			if (!RegisterDialog.getSelectedID().isEmpty()){
				// .. wird die ausgewählte ID als XML-Fragmnet übernommen. 
				String xmlFragment = RegisterDialog.getSelectedID();

				//The XML may contain an editor template for caret positioning.
				boolean moveCaretToSpecifiedPosition =
						MoveCaretUtil.hasImposedEditorVariableCaretOffset(xmlFragment);
				int insertionOffset = authorAccess.getEditorAccess().getCaretOffset();

				Object schemaAwareArgumentValue = args.getArgumentValue(SCHEMA_AWARE_ARGUMENT);
				if (AuthorConstants.ARG_VALUE_FALSE.equals(schemaAwareArgumentValue)) {
					// Insert fragment at specified position.
					if (moveCaretToSpecifiedPosition) {
						//Compute the offset where the insertion will take place.
						if (xpathLocation != null && ((String)xpathLocation).trim().length() > 0) {
							// Evaluate the expression and obtain the offset of the first node from the result
							insertionOffset =
									authorAccess.getDocumentController().getXPathLocationOffset(
											(String) xpathLocation, (String) relativeLocation);
						}
					}

					authorAccess.getDocumentController().insertXMLFragment(
							xmlFragment, (String) xpathLocation, (String) relativeLocation);                
				} else {
					// Insert fragment schema aware.
					SchemaAwareHandlerResult result =
							authorAccess.getDocumentController().insertXMLFragmentSchemaAware(
									xmlFragment, (String) xpathLocation, (String) relativeLocation);
					//Keep the insertion offset.
					if (result != null) {
						Integer off = (Integer) result.getResult(
								SchemaAwareHandlerResultInsertConstants.RESULT_ID_HANDLE_INSERT_FRAGMENT_OFFSET);
						if (off != null) {
							insertionOffset = off.intValue(); 
						}
					}
				}

				if (moveCaretToSpecifiedPosition) {
					//Detect the position in the Author page where the caret should be placed.
					MoveCaretUtil.moveCaretToImposedEditorVariableOffset(authorAccess, insertionOffset);
				}

			}
		} else {
			throw new IllegalArgumentException(
					"One or more of the argument values are not declared, they are: url - " + urlArgVal 
					+ ", node - " + nodeArgVal + ", expression - " + expressionArgVal + ", element - " + elementArgVal);
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
		return "Öffnet einen Dialog, in welchem Einträgen eines Registers" +
				" ausgewählt werden kann. Ein Element mit der entsprechenden ID wird an der vorgegebenen" +
				" Stelle eingefügt.";
	}
}




