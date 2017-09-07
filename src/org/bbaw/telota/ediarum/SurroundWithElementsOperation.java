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

public class SurroundWithElementsOperation implements AuthorOperation {
	/**
	 * Argument describing the elements.
	 */
	private static final String ARGUMENT_ELEMENTS = "elements";
	
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
				"Eine in den Elementen mehrfach zu verwendende ID"),
		new ArgumentDescriptor(
				ARGUMENT_ELEMENTS,
				ArgumentDescriptor.TYPE_STRING,
				"Die vor und hinter der Markierung einzufügenden Elemente durch '$[SELECTION]' getrennt, die ID wird mit '$[ID]' eingefügt ")
	};
	  
	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		String elementsArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_ELEMENTS, args);
		String idArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_ID, args, "");

		// Falls im Text nichts selektiert ist, wird das aktuelle Word ausgewählt. 
		if (!authorAccess.getEditorAccess().hasSelection()) {
    		authorAccess.getEditorAccess().selectWord();
    	}
    	int selStart = authorAccess.getEditorAccess().getSelectionStart();
    	int selEnd = authorAccess.getEditorAccess().getSelectionEnd();
		
    	// Die ID wird an den entsprechenden Stellen eingefügt.
    	String[] elementsInParts = ((String)elementsArgVal).split("\\$\\[ID\\]");
    	String elementsWithID = elementsInParts[0];
    	for (int i=1; i<elementsInParts.length; i++) {
    		elementsWithID += idArgVal + elementsInParts[i];
    	}	    	
    	// Die beiden Elemente im übergebenen Argument werden getrennt, ..
    	String[] elements = elementsWithID.split("\\$\\[SELECTION\\]"); 
		
    	// .. das erste wird vor der Selektion eingefügt, und das zweite dahinter.
    	authorAccess.getDocumentController().insertXMLFragment((String) elements[1], selEnd);
    	authorAccess.getDocumentController().insertXMLFragment((String) elements[0], selStart);
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
		return "Fügt vor und hinter der Markierung verschiedene Elemente ein.";
	}
}
