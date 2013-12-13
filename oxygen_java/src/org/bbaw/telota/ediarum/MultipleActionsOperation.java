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
 * MultipleActionsOperation.java - is a class to execute multiple actions.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.0
 */
package org.bbaw.telota.ediarum;

import javax.swing.Action;

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
		// Die übergebenen Argumente werden eingelesen ..
		Object firstActionArgVal = args.getArgumentValue(ARGUMENT_FIRST_ACTION);
		Object secondActionArgVal = args.getArgumentValue(ARGUMENT_SECOND_ACTION);
		Object thirdActionArgVal = args.getArgumentValue(ARGUMENT_THIRD_ACTION);
		Object fourthActionArgVal = args.getArgumentValue(ARGUMENT_FORTH_ACTION);
		Object fifthActionArgVal = args.getArgumentValue(ARGUMENT_FIFTH_ACTION);
		// .. und überprüft.
		if (firstActionArgVal == null) {
			firstActionArgVal = "";
		}
		if (secondActionArgVal == null) {
			secondActionArgVal = "";
		}
		if (thirdActionArgVal == null) {
			thirdActionArgVal = "";
		}
		if (fourthActionArgVal == null) {
			fourthActionArgVal = "";
		}
		if (fifthActionArgVal == null) {
			fifthActionArgVal = "";
		}
		if (firstActionArgVal instanceof String
				&& secondActionArgVal instanceof String
				&& thirdActionArgVal instanceof String
				&& fourthActionArgVal instanceof String
				&& fifthActionArgVal instanceof String) {
			// Die Aktionen werden geladen ..
			AuthorActionsProvider authorActionsProvider = authorAccess.getEditorAccess().getActionsProvider();
			Action firstAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(firstActionArgVal);
			Action secondAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(secondActionArgVal);
			Action thirdAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(thirdActionArgVal);
			Action fourthAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(fourthActionArgVal);
			Action fifthAction = (Action) authorActionsProvider.getAuthorExtensionActions().get(fifthActionArgVal);
			// .. und ausgeführt.
			if (firstAction != null)
				authorActionsProvider.invokeAction(firstAction);
			if (secondAction != null)
				authorActionsProvider.invokeAction(secondAction);
			if (thirdAction != null)
				authorActionsProvider.invokeAction(thirdAction);
			if (fourthAction != null)
				authorActionsProvider.invokeAction(fourthAction);
			if (fifthAction != null)
				authorActionsProvider.invokeAction(fifthAction);
		} else {
			throw new IllegalArgumentException(
					"One or more of the argument values are not declared, they are: first action - " + firstActionArgVal 
					+ ", second action - " + secondActionArgVal + ", third action - " + thirdActionArgVal
					+ ", fourth action - " + fourthActionArgVal + ", fifth action - " + fifthActionArgVal);
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
		return "Definiert mehrere Aktionen, die nacheinander ausgeführt werden.";
	}
}
