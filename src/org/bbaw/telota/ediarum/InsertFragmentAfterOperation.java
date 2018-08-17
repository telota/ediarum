/**
 * InsertFragmentAfterOperation.java - is a class add an element at a specified position after other elements.
 * So, that a predefined order of elements is preserved.
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
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.ecss.extensions.api.schemaaware.SchemaAwareHandlerResult;
import ro.sync.ecss.extensions.api.schemaaware.SchemaAwareHandlerResultInsertConstants;
import ro.sync.ecss.extensions.commons.operations.MoveCaretUtil;

import java.awt.Frame;
import java.util.Arrays;

import javax.swing.text.BadLocationException;

import org.bbaw.telota.ediarum.extensions.EdiarumArgumentValidator;

public class InsertFragmentAfterOperation implements AuthorOperation{
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
	private static final String ARGUMENT_XPATH_BEFORE_LOCATIONS = "insertAfter";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
			new ArgumentDescriptor(
					ARGUMENT_ELEMENT,
					ArgumentDescriptor.TYPE_STRING,
					"The XML fragment which should be inserted at the insert location."),
			// Argument defining the location where the operation will be executed as an XPath expression.
			new ArgumentDescriptor(
					ARGUMENT_XPATH_LOCATION,
					ArgumentDescriptor.TYPE_XPATH_EXPRESSION,
					"An XPath expression indicating the insert location for the fragment.\n" +
					"Note: If it is not defined then the insert location will be at the caret."),
			// Argument defining the relative position to the node obtained from the XPath location.
			new ArgumentDescriptor(
					ARGUMENT_XPATH_BEFORE_LOCATIONS,
					ArgumentDescriptor.TYPE_STRING,
					"A comma separated list of XPath expressions which are allowed as preceding siblings."
					),
			SCHEMA_AWARE_ARGUMENT_DESCRIPTOR
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen ..
		// .. und überprüft.
		String elementArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_ELEMENT, args);
		Object xpathLocation = args.getArgumentValue(ARGUMENT_XPATH_LOCATION);
		String xpathBeforeLocations = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_XPATH_BEFORE_LOCATIONS, args);

		String xmlFragment = (String) elementArgVal;

		int insertionOffset = authorAccess.getEditorAccess().getCaretOffset();

		// Insert fragment at specified position.
		//Compute the offset where the insertion will take place.
		if (xpathLocation != null && ((String)xpathLocation).trim().length() > 0) {
			// Das Element soll als letzte Möglichkeit als letztes im Elternelement eingefügt (xpathLocation) werden.
			// Evaluate the expression and obtain the offset of the first node from the result
			insertionOffset = authorAccess.getDocumentController().getXPathLocationOffset((String) xpathLocation, (String) AuthorConstants.POSITION_INSIDE_LAST);
		}
		AuthorNode parentNode;
		try {
			parentNode = authorAccess.getDocumentController().getNodeAtOffset(insertionOffset);
			// Es wird geprüft, ob es weiter vorn eingefügt werden soll.
			// Dazu werden der Reihe nach alle Kinder geprüft, ..
			AuthorNode[] childElements = authorAccess.getDocumentController().findNodesByXPath("./node()",parentNode, true, true, true, true);
			for (int i=0; i<childElements.length; i++) {
				AuthorNode child = childElements[i];
				// .. wenn sie nicht als gültige Vorgänger in Frage kommen, ..
				AuthorNode[] xpathBeforeNodes = authorAccess.getDocumentController().findNodesByXPath((String) xpathBeforeLocations, parentNode, true, true, true, true);
				boolean childIsInBeforeNodes = Arrays.asList(xpathBeforeNodes).contains(child);
				if (!childIsInBeforeNodes) {
					// .. soll das neue Element vor dem letzten ungültigen Kind eingefügt werden.
					int offsetBeforeChild = child.getStartOffset();
					insertionOffset = offsetBeforeChild;
					break;
				}
			}
			// Füge das Element an entsprechende Position ein.
			authorAccess.getDocumentController().insertXMLFragment(xmlFragment, insertionOffset);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return "Insert a document fragment after allowed preceding siblings.";
	}
}
