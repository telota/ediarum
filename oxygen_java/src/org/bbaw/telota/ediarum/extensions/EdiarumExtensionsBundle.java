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
 * EdiarumExtensionsBundle.java - is a class to for all extensions.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.0
 */
package org.bbaw.telota.ediarum.extensions;

import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.link.LinkTextResolver;

/**
 *
 */
public class EdiarumExtensionsBundle extends ExtensionsBundle {

	@Override
	public String getDescription() {
		return "org.bbaw.telota.ediarum.documenttype";
	}

	@Override
	public String getDocumentTypeID() {
		return "A custom extensions bundle used for the Ediarum " + 
                "Framework document type";
	}

	@Override
	public AuthorExtensionStateListener createAuthorExtensionStateListener() {
		return new EdiarumAuthorExtensionStateListener();
	}
	
//	@Override
//	public AuthorReferenceResolver createAuthorReferenceResolver() {
//		return new EdiarumReferencesResolver();
//	}
//	
	@Override
	public LinkTextResolver createLinkTextResolver() {
		return new EdiarumLinkTextResolver();
	}
}
