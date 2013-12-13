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
 * ExecuteCommandOperation.java - is a an operation for executing an external program.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.0
 */
package org.bbaw.telota.ediarum;

import java.io.IOException;
import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ExecuteCommandOperation implements AuthorOperation {
	/**
	 * Argument describing the url.
	 */
	private static final String ARGUMENT_COMMAND = "Command";

	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_COMMAND,
				ArgumentDescriptor.TYPE_STRING,
				"Das auszuführende Kommando")
	};

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	@Override
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws IllegalArgumentException, AuthorOperationException {
		// Die übergebenen Argumente werden eingelesen ..
		Object cmdArgVal = args.getArgumentValue(ARGUMENT_COMMAND);
		// .. und überprüft.
		if (cmdArgVal != null
				&& cmdArgVal instanceof String) {

			try {
				Process p = Runtime.getRuntime().exec((String)cmdArgVal);
				p.waitFor();
			} catch (IOException e) {} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			throw new IllegalArgumentException(
					"The argument value is not declared, it is are: command - " + cmdArgVal);
		}
	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
	 */
	@Override
	public ArgumentDescriptor[] getArguments() {
		return ARGUMENTS;
	}

	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Führt ein externes Programm aus.";
	}
}
