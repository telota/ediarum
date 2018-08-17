/**
 * SurroundWithElement.java - is a class for inserting elements before and after a selection.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung).
 * @author Martin Fechner
 * @version 1.0.2
 */
package org.bbaw.telota.ediarum;

import org.bbaw.telota.ediarum.extensions.EdiarumArgumentValidator;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class SurroundWithDifferentFragmentsOperation implements AuthorOperation {
	/**
	 * Argument describing the first node.
	 */
	private static final String ARGUMENT_FIRST_ELEMENT = "first element";

	/**
	 * Argument describing the second node.
	 */
	private static final String ARGUMENT_SECOND_ELEMENT = "second element";

	/**
	 * Argument describing the elements.
	 */
	private static final String ARGUMENT_ID = "id";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_ID,
				ArgumentDescriptor.TYPE_STRING,
				"A ID which are usable at multiple locations."),
		new ArgumentDescriptor(
				ARGUMENT_FIRST_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"The XML fragment which should be inserted before the selection."
				+ " The id is inserted with the variable $ID"),
		new ArgumentDescriptor(
				ARGUMENT_SECOND_ELEMENT,
				ArgumentDescriptor.TYPE_STRING,
				"The XML fragment which should be inserted after the selection."
				+ " The id is inserted with the variable $ID"),
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		String firstElementArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_FIRST_ELEMENT, args);
		String secondElementArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_SECOND_ELEMENT, args);
		String idArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_ID, args, "");

		// Falls im Text nichts selektiert ist, wird das aktuelle Word ausgewählt.
		if (!authorAccess.getEditorAccess().hasSelection()) {
    		authorAccess.getEditorAccess().selectWord();
    	}
    	int selStart = authorAccess.getEditorAccess().getSelectionStart();
    	int selEnd = authorAccess.getEditorAccess().getSelectionEnd();

    	// Die ID wird an den entsprechenden Stellen eingefügt.
    	String[] firstElementInParts = firstElementArgVal.split("\\$ID");
    	String firstElementWithID = firstElementInParts[0];
    	for (int i=1; i<firstElementInParts.length; i++) {
    		firstElementWithID += idArgVal + firstElementInParts[i];
    	}

    	// Die ID wird an den entsprechenden Stellen eingefügt.
    	String[] secondElementInParts = secondElementArgVal.split("\\$ID");
    	String secondElementWithID = secondElementInParts[0];
    	for (int i=1; i<secondElementInParts.length; i++) {
    		secondElementWithID += idArgVal + secondElementInParts[i];
    	}

    	// .. das erste wird vor der Selektion eingefügt, und das zweite dahinter.
    	authorAccess.getDocumentController().insertXMLFragment(secondElementWithID, selEnd);
    	authorAccess.getDocumentController().insertXMLFragment(firstElementWithID, selStart);
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
		return "Inserts before and after the selection different elements.";
	}
}
