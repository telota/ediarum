/**
 * MultipleActionsOperation.java - is a class to execute multiple actions.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.0.0
 */
package org.bbaw.telota.ediarum;

import java.awt.Frame;

import javax.swing.Action;

import org.bbaw.telota.ediarum.extensions.EdiarumArgumentValidator;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.exml.workspace.api.editor.page.author.actions.AuthorActionsProvider;

public class MultipleActionsOperation implements AuthorOperation{
	/**
	 * Argument describing the first action.
	 */
	private static final String ARGUMENT_FIRST_ACTION = "first action";

	/**
	 * Argument describing the second action.
	 */
	private static final String ARGUMENT_SECOND_ACTION = "second action";

	/**
	 * Argument describing the third action.
	 */
	private static final String ARGUMENT_THIRD_ACTION = "third action";

	/**
	 * Argument describing the fourth action.
	 */
	private static final String ARGUMENT_FORTH_ACTION = "fourth action";

	/**
	 * Argument describing the fifth action.
	 */
	private static final String ARGUMENT_FIFTH_ACTION = "fifth action";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_FIRST_ACTION,
				ArgumentDescriptor.TYPE_STRING,
				"Die ID der ersten Aktion"),
		new ArgumentDescriptor(
				ARGUMENT_SECOND_ACTION,
				ArgumentDescriptor.TYPE_STRING,
				"Die ID der zweiten Aktion"),
		new ArgumentDescriptor(
				ARGUMENT_THIRD_ACTION,
				ArgumentDescriptor.TYPE_STRING,
				"Die ID der dritten Aktion"),
		new ArgumentDescriptor(
				ARGUMENT_FORTH_ACTION,
				ArgumentDescriptor.TYPE_STRING,
				"Die ID der vierten Aktion"),
		new ArgumentDescriptor(
				ARGUMENT_FIFTH_ACTION,
				ArgumentDescriptor.TYPE_STRING,
				"Die ID der fünften Aktion")
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen.
		String firstActionArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_FIRST_ACTION, args, "");
		String secondActionArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_SECOND_ACTION, args, "");
		String thirdActionArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_THIRD_ACTION, args, "");
		String fourthActionArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_FORTH_ACTION, args, "");
		String fifthActionArgVal = EdiarumArgumentValidator.validateStringArgument(ARGUMENT_FIFTH_ACTION, args, "");

		// Die Aktionen werden geladen ..
		AuthorActionsProvider authorActionsProvider = authorAccess.getEditorAccess().getActionsProvider();
		Action firstAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(firstActionArgVal);
		Action secondAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(secondActionArgVal);
		Action thirdAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(thirdActionArgVal);
		Action fourthAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(fourthActionArgVal);
		Action fifthAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(fifthActionArgVal);
		// .. und ausgeführt.
		if (firstAction != null) {
			authorActionsProvider.invokeAction(firstAction);
		}
		if (secondAction != null) {
			authorActionsProvider.invokeAction(secondAction);
		}
		if (thirdAction != null)
			authorActionsProvider.invokeAction(thirdAction);
		if (fourthAction != null)
			authorActionsProvider.invokeAction(fourthAction);
		if (fifthAction != null)
			authorActionsProvider.invokeAction(fifthAction);
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
		return "Definiert mehrere Aktionen, die nacheinander ausgeführt werden.";
	}
}
