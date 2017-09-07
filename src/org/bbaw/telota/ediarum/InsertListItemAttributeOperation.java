/**
 * InsertListItemAttributeOperation.java - is a class to add an attribute to a selected element.
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
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.awt.Frame;

import javax.swing.text.BadLocationException;

public class InsertListItemAttributeOperation implements AuthorOperation{
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
	 * Argument describing the separating string.
	 */
	private static final String ARGUMENT_SEPARATION = "item separator";

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
				ARGUMENT_ATTRIBUTENAME,
				ArgumentDescriptor.TYPE_STRING,
				"The name of the new attribute, e.g.: " +
				"key"),
		new ArgumentDescriptor(
				ARGUMENT_XPATHFROMSELECTION,
				ArgumentDescriptor.TYPE_STRING,
				"A relative XPath expression from current context node "
				+ "to the element with the new attribute: " +
				"./child"),
		new ArgumentDescriptor(
				ARGUMENT_ATTRIBUTEVALUE,
				ArgumentDescriptor.TYPE_STRING,
				"The content of the new attribute, e.g.: " +
				"some text.. $ITEMS")
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
		Object attributenameArgVal = args.getArgumentValue(ARGUMENT_ATTRIBUTENAME);
		Object xpathfromselectionArgVal = args.getArgumentValue(ARGUMENT_XPATHFROMSELECTION);
		Object attributevalArgVal = args.getArgumentValue(ARGUMENT_ATTRIBUTEVALUE);
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
				AuthorElement selElement;
				try {
					AuthorNode selNode = authorAccess.getDocumentController().getNodeAtOffset(selStart);
					selElement = (AuthorElement) (authorAccess.getDocumentController().findNodesByXPath((String) xpathfromselectionArgVal, selNode, false, true, true, false))[0];
					String newAttrValue = (String) attributevalArgVal;
					String IDitems = String.join((String)separationArgVal, RegisterDialog.getSelectedIDs());
					newAttrValue = newAttrValue.replaceAll("[$]ITEMS", IDitems);

					authorAccess.getDocumentController().setAttribute((String) attributenameArgVal, new AttrValue(newAttrValue), selElement);
				} catch (BadLocationException e) {}
			}
		} else {
			throw new IllegalArgumentException(
					"One or more of the argument values are not declared, they are: url - " + urlArgVal
					+ ", node - " + nodeArgVal + ", namespaces - " + namespacesArgVal + ", expression - " + expressionArgVal
					+ ", variable - " + variableArgVal + ", separation - " + separationArgVal
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
				" ausgewählt werden kann. Die entsprechende ID wird an der markierten" +
				" Stelle eingefügt.";
	}
}
