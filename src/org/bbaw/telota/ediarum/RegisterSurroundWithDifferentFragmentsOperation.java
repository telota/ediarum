/**
 * RegisterSurroundWithDifferentFragmentsOperation.java - is a class to surround a selection with a register elements.
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

import org.bbaw.telota.ediarum.extensions.EdiarumArgumentValidator;


public class RegisterSurroundWithDifferentFragmentsOperation implements AuthorOperation{
	/**
	 * Argument describing the URL.
	 */
	private static final String ARGUMENT_URL = "URL";

	/**
	 * Argument describing the node.
	 */
	private static final String ARGUMENT_NODE = "node";

	/**
	 * Argument describing the namespaces.
	 */
	private static final String ARGUMENT_NAMESPACES = "namespaces";

	/**
	 * Argument describing the expression.
	 */
	private static final String ARGUMENT_EXPRESSION = "expression";

	/**
	 * Argument describing the ID for all elements.
	 */
	private static final String ARGUMENT_ID = "id";

	/**
	 * Argument describing the first node.
	 */
	private static final String ARGUMENT_FIRST_ELEMENT = "first element";

	/**
	 * Argument describing the second node.
	 */
	private static final String ARGUMENT_SECOND_ELEMENT = "second element";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_URL,
				ArgumentDescriptor.TYPE_STRING,
				"The URL of the external index file, e.g. " +
				"http://user:passwort@www.example.com:port/exist/webdav/db/register.xml"),
		new ArgumentDescriptor(
				ARGUMENT_NODE,
				ArgumentDescriptor.TYPE_STRING,
				"An XPath expression for the list items, e.g.: //item"),
		new ArgumentDescriptor(
				ARGUMENT_NAMESPACES,
				ArgumentDescriptor.TYPE_STRING,
				"An whitespace separated list of namespace declarations with QNames before a colon, e.g.: tei:http://www.tei-c.org/ns/1.0"),
		new ArgumentDescriptor(
				ARGUMENT_EXPRESSION,
				ArgumentDescriptor.TYPE_STRING,
				"A string how the items are rendered in the list. "
				+ "Use $XPATH{expression} for xpath expressions (starting with @, /, //, ./, # (for functions)), "
				+ "E.g.: $XPATH{/name}, $XPATH{/forename} ($XPATH{/data})"),
		new ArgumentDescriptor(
				ARGUMENT_ID,
				ArgumentDescriptor.TYPE_STRING,
				"An ID which can be used multiple times at different places"),
		new ArgumentDescriptor(
				ARGUMENT_FIRST_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"Before the selected text this element is inserted."
				+ "Use $ID for  the reusable id, $XPATH{expression} for xpath expressions (starting with @, /, //, ./, # (for functions)), "
				+ "e.g.: <index xmlns='http://www.tei-c.org/ns/1.0' spantTo='$ID' indexName='persons' corresp='$XPATH{@xml:id}'>"
				+ "<term>$XPATH{/name}, $XPATH{/forename}</term>"
				+ "</index>"),
		new ArgumentDescriptor(
				ARGUMENT_SECOND_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"After the selected text this element is inserted."
				+ "Use $ID for  the reusable id, $XPATH{expression} for xpath expressions (starting with @, /, //, ./, # (for functions)), "
				+ "e.g.: <anchor xmlns='http://www.tei-c.org/ns/1.0' xml:id='$ID' />"),
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen.
        String urlArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_URL, args);
        String nodeArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_NODE, args);
        String namespacesArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_NAMESPACES, args);
        String expressionArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_EXPRESSION, args);
        String idArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_ID, args, "");
        String firstElementArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_FIRST_ELEMENT, args);
        String secondElementArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_SECOND_ELEMENT, args);

        // Wenn im aktuellen Dokument nichts selektiert ist, wird das aktuelle Wort ausgewählt.
		if (!authorAccess.getEditorAccess().hasSelection()) {
			authorAccess.getEditorAccess().selectWord();
		}
		int selStart = authorAccess.getEditorAccess().getSelectionStart();
		int selEnd = authorAccess.getEditorAccess().getSelectionEnd();

		// Für die spätere Verwendung werden die Variablen für die Registereinträge und Elemente erzeugt.
		String[] eintrag = null, elements = null;

		//  Der später einzufügende Ausdruck wird gebaut.
		String variable = firstElementArgVal + "$SELECTION" + secondElementArgVal;

		// Dann wird das Registerdokument eingelesen, wobei auf die einzelnen Registerelement und
		// die Ausdrücke für die Einträge und Elemente Rücksicht genommen wird.
		ReadListItems register = new ReadListItems(urlArgVal, nodeArgVal, expressionArgVal, variable, namespacesArgVal);

		// Die Arrays für die Einträge und IDs werden an die lokalen Variablen übergeben.
		eintrag = register.getEintrag();
		elements = register.getID();

		// Dafür wird der RegisterDialog geöffnet und erhält die Einträge und IDs als Parameter.
		InsertRegisterDialog RegisterDialog = new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(), eintrag, elements, false);
		// Wenn in dem Dialog ein Eintrag ausgewählt wurde, ..
		if (!RegisterDialog.getSelectedID().isEmpty()){
			// .. wird in den entsprechenden Elementen die eingestellte ID eingefügt, ..
			String[] selectedIDInParts = RegisterDialog.getSelectedID().split("\\$ID");
			String selectedID = selectedIDInParts[0];
			for (int i=1; i<selectedIDInParts.length; i++) {
				selectedID += idArgVal + selectedIDInParts[i];
			}
			// .. und dann werden im aktuellen Dokument um die Selektion die entsprechenden Elemente eingesetzt.
			String[] surroundElements = selectedID.split("\\$SELECTION");
			authorAccess.getDocumentController().insertXMLFragment(surroundElements[1], selEnd);
			authorAccess.getDocumentController().insertXMLFragment(surroundElements[0], selStart);
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
		return "Opens a dialog to choose an entry from an external index file. The elements with the specified id is inserted around the selection.";
	}
}
