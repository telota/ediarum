/**
 * InsertListItemAtOperation.java - is a class add an element from an selection to an specified position.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung).
 * @author Martin Fechner
 */
package org.bbaw.telota.ediarum;

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

public class InsertListItemAtOperation implements AuthorOperation{
	/**
	 * Argument describing the url.
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
	private static final String ARGUMENT_EXPRESSION = "item rendering";

	/**
	 * Argument describing the item variables.
	 */
	private static final String ARGUMENT_VARIABLE = "item variable";

	/**
	 * Argument describing if multiple selection is possible.
	 */
	private static final String ARGUMENT_MULTIPLE_SELECTION = "multiple selection";

	/**
	 * Argument describing the insertNode.
	 */
	private static final String ARGUMENT_ELEMENT = "element";

	/**
	 * Argument describing the separating string.
	 */
	private static final String ARGUMENT_SEPARATION = "item separator";

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
				"The URL to the .xml file with the list, e.g.: " +
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
				+ "Use $XPATH{expression} for xpath expressions (starting with @, /, //, ./), "
				+ "E.g.: $XPATH{/name}, $XPATH{/vorname} ($XPATH{/lebensdaten})"),
		new ArgumentDescriptor(
				ARGUMENT_VARIABLE,
				ArgumentDescriptor.TYPE_STRING,
				"The item variable which is used for the XML fragment, e.g.: #$XPATH{@id}"),
		new ArgumentDescriptor(
				ARGUMENT_MULTIPLE_SELECTION,
				ArgumentDescriptor.TYPE_CONSTANT_LIST,
				"When is enabled, multiple selection will be possible",
				new String[]{
					AuthorConstants.ARG_VALUE_TRUE,
					AuthorConstants.ARG_VALUE_FALSE,
				},
				AuthorConstants.ARG_VALUE_TRUE),
		new ArgumentDescriptor(
				ARGUMENT_SEPARATION,
				ArgumentDescriptor.TYPE_STRING,
				"The string for separating the item variables. Default value is a space."),
		new ArgumentDescriptor(
				ARGUMENT_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"The XML fragment which should be inserted at current caret position."
				+ "Multiple list selections will be separated through spaces, e.g.: "
				+ "<persName xmlns='http://www.tei-c.org/ns/1.0' key='$ITEMS' />"),
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
		Object namespacesArgVal = args.getArgumentValue(ARGUMENT_NAMESPACES);
		Object expressionArgVal = args.getArgumentValue(ARGUMENT_EXPRESSION);
		Object variableArgVal = args.getArgumentValue(ARGUMENT_VARIABLE);
		Object separationArgVal = args.getArgumentValue(ARGUMENT_SEPARATION);
		Object elementArgVal = args.getArgumentValue(ARGUMENT_ELEMENT);
		Object xpathLocation = args.getArgumentValue(ARGUMENT_XPATH_LOCATION);
		Object relativeLocation = args.getArgumentValue(ARGUMENT_RELATIVE_LOCATION);
		Object multipleSelection = args.getArgumentValue(ARGUMENT_MULTIPLE_SELECTION);
		// .. und überprüft.
		if (urlArgVal != null
				&& urlArgVal instanceof String
				&& nodeArgVal != null
				&& nodeArgVal instanceof String
				&& namespacesArgVal != null
				&& namespacesArgVal instanceof String
				&& expressionArgVal != null
				&& expressionArgVal instanceof String
				&& variableArgVal != null
				&& variableArgVal instanceof String
				&& separationArgVal != null
				&& separationArgVal instanceof String
				&& elementArgVal != null
				&& elementArgVal instanceof String) {

			// Für die spätere Verwendung werden die Variablen für die Registereinträge und IDs erzeugt.
			String[] eintrag = null, id = null;

			// Dann wird das Registerdokument eingelesen, wobei auf die einzelnen Registerelement und ..
			// .. die Ausdrücke für die Einträge und IDs Rücksicht genommen wird.
			ReadListItems register = new ReadListItems((String)urlArgVal, (String) nodeArgVal, (String) expressionArgVal, (String) variableArgVal, (String) namespacesArgVal);
			// Die Arrays für die Einträge und IDs werden an die lokalen Variablen übergeben.
			eintrag = register.getEintrag();
			id = register.getID();

			// Dafür wird der RegisterDialog geöffnet und erhält die Einträge und IDs als Parameter.
			InsertRegisterDialog RegisterDialog = new InsertRegisterDialog((Frame) authorAccess.getWorkspaceAccess().getParentFrame(), eintrag, id, ((String) multipleSelection).equals(AuthorConstants.ARG_VALUE_TRUE));
			// Wenn in dem Dialog ein Eintrag ausgewählt wurde, ..
			if (!RegisterDialog.getSelectedID().isEmpty()){
				// wird im aktuellen Dokument um die Selektion das entsprechende Element mit ID eingesetzt.
				String element = (String) elementArgVal;
				String IDitems = String.join((String)separationArgVal, RegisterDialog.getSelectedIDs());
				String xmlFragment = element.replaceAll("[$]ITEMS", IDitems);

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
					+ ", node - " + nodeArgVal + ", namespaces - " + namespacesArgVal + ", expression - " + expressionArgVal
					+ ", variable - " + variableArgVal + ", separation - " + separationArgVal + ", element - " + elementArgVal);
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
				" ausgewählt werden kann. Die entsprechende ID wird an der markierten" +
				" Stelle eingefügt.";
	}
}
