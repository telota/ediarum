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
 * ReadRegister.java - is a class for reading a register file to get register entries and IDs.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.1
 */
package org.bbaw.telota.ediarum;

import javax.swing.text.BadLocationException;

import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class ReadRegister {
	
	/**
	 * interne Variablen, die Einträge und IDs des Registers.
	 */
	private String[] eintrag, id;
	
	/**
	 * Der Konstruktor liest im aktuellen Dokument die benannten Knoten aus und konstruiert den Eintrag und die Id für jeden Knoten.
	 * @param currentDocument Das aktuelle Dokument
	 * @param node Der X-Path-Ausdruck für die Knoten der einzelnen Registereinträge
	 * @param eintragExp Der Ausdruck um einen Registereintrag zu konstruieren. Er setzt sich aus Strings in "", Attributen beginnend mit @,
	 *  Elementen mit / oder // und aus X-Path-Ausdrücken beginnend mit . zusammen. Die einzelnen Teile werden mit + verbunden.
	 * @param idExp Der Ausdruck um die ID für einen Registereintrag zu konstruieren. Er setzt sich wie eintragExp zusammen.
	 * @throws AuthorOperationException
	 */
	public ReadRegister(AuthorDocumentController currentDocument, String node, String eintragExp, String idExp) throws AuthorOperationException{
		// Erst werden die Knoten aus dem aktuellen Dokument ausgelesen, ..
		AuthorNode[] registerNodes = currentDocument.findNodesByXPath(node, false, true, true);
		if(registerNodes[0].getType() == AuthorNode.NODE_TYPE_ELEMENT){
			// .. dann werden für die Einträge und IDs entsprechend lange Arrays angelegt.
			eintrag = new String[registerNodes.length];
			id = new String[registerNodes.length];
			
			// Die Ausdrücke für die Einträge ..
			String[] eintragExpression = (eintragExp).split("[+]");
			for (int i=0; i < eintragExpression.length; i++) {
				eintragExpression[i] = eintragExpression[i].trim();
			}
			// .. und IDs werden in einzelne Glieder geteilt. 
			String[] idExpression = (idExp).split("[+]");
			for (int i=0; i < idExpression.length; i++) {
				idExpression[i] = idExpression[i].trim();
			}
			// Für jeden Knoten ..
			for (int i=0; i < registerNodes.length; i++) {
				AuthorElement currentElement = (AuthorElement) registerNodes[i];
				// .. wird der Eintrag konstruiert.
				eintrag[i] = "";
				// Dazu werden die Glieder des entsprechenden Ausdrucks interpretiert.
				for (int j=0; j < eintragExpression.length; j++) {
					try {
						// Jedes Glied kann entweder als Attribut, ..
						if (eintragExpression[j].startsWith("@")) {
							String attributeName = eintragExpression[j].substring(1);
							eintrag[i] += currentElement.getAttribute(attributeName).getValue();
						}
						// .. als String, ..
						if (eintragExpression[j].startsWith("\"") && eintragExpression[j].endsWith("\"")) {
							String stringexpression = eintragExpression[j].substring(1, eintragExpression[j].length()-1);
							eintrag[i] += stringexpression;
						}
						// .. als später zu verarbeitetende Variable, ..
						if (eintragExpression[j].startsWith("$")) {
							String stringexpression = eintragExpression[j];
							eintrag[i] += stringexpression;
						}
						// .. als nachkommendes Element ..
						if (eintragExpression[j].startsWith("//")) {
							// .. (was direkt gelesen werden kann und schnell geht, ..
							String elementName = eintragExpression[j].substring(2);
							if (currentElement.getElementsByLocalName(elementName).length>0){
								eintrag[i] += currentElement.getElementsByLocalName(elementName)[0].getTextContent();
							} else {
								// .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
								AuthorNode[] elementNode = currentDocument.findNodesByXPath("."+eintragExpression[j], registerNodes[i], false, true, true, false);
								if (elementNode[0].getType() == AuthorNode.NODE_TYPE_ELEMENT){
									eintrag[i] += ((AuthorElement) elementNode[0]).getTextContent();
								}
							}
							// .. als direktes Kindelement (was schnell geht) ..
						} else if (eintragExpression[j].startsWith("/")) {
							String elementName = eintragExpression[j].substring(1);
							if (currentElement.getElementsByLocalName(elementName).length>0){
								eintrag[i] += currentElement.getElementsByLocalName(elementName)[0].getTextContent();
							}
						}
						// .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
						if (eintragExpression[j].startsWith(".")) {
							AuthorNode[] elementNode = currentDocument.findNodesByXPath(eintragExpression[j], registerNodes[i], false, true, true, false);
							if (elementNode[0].getType() == AuthorNode.NODE_TYPE_ELEMENT){
								eintrag[i] += ((AuthorElement) elementNode[0]).getTextContent();
							}
						}
					} catch (BadLocationException e) {
					} catch (ArrayIndexOutOfBoundsException e) {
					}
				}
				
				// Genauso wird die ID konstruiert.
				id[i] = "";
				// Dazu werden die Glieder des entsprechenden Ausdrucks interpretiert.
				for (int j=0; j < idExpression.length; j++) {
					try {
						// Jedes Glied kann entweder als Attribut, ..
						if (idExpression[j].startsWith("@")) {
							String attributeName = idExpression[j].substring(1);
							id[i] += currentElement.getAttribute(attributeName).getValue();
						}
						// .. als String, ..
						if (idExpression[j].startsWith("\"") && idExpression[j].endsWith("\"")) {
							String stringexpression = idExpression[j].substring(1, idExpression[j].length()-1);
							id[i] += stringexpression;
						}
						// .. als später zu verarbeitetende Variable, ..
						if (idExpression[j].startsWith("$")) {
							String stringexpression = idExpression[j];
							id[i] += stringexpression;
						}
						// .. als nachkommendes Element ..
						if (idExpression[j].startsWith("//")) {
							// .. (was direkt gelesen werden kann und schnell geht, ..
							String elementName = idExpression[j].substring(2);
							if (currentElement.getElementsByLocalName(elementName).length>0){
								id[i] += currentElement.getElementsByLocalName(elementName)[0].getTextContent();
							} else {
								// .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
								AuthorNode[] elementNode = currentDocument.findNodesByXPath("."+idExpression[j], registerNodes[i], false, true, true, false);
								if (elementNode[0].getType() == AuthorNode.NODE_TYPE_ELEMENT){
									id[i] += ((AuthorElement) elementNode[0]).getTextContent();
								}
							}
							// .. als direktes Kindelement (was schnell geht) ..
						} else if (idExpression[j].startsWith("/")) {
							String elementName = idExpression[j].substring(1);
							if (currentElement.getElementsByLocalName(elementName).length>0){
								id[i] += currentElement.getElementsByLocalName(elementName)[0].getTextContent();
							}
						}
						// .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
						if (idExpression[j].startsWith(".")) {
							AuthorNode[] elementNode = currentDocument.findNodesByXPath(idExpression[j], registerNodes[i], false, true, true, false);
							if (elementNode[0].getType() == AuthorNode.NODE_TYPE_ELEMENT){
								id[i] += ((AuthorElement) elementNode[0]).getTextContent();
							}
						}
					} catch (BadLocationException e) {
					} catch (ArrayIndexOutOfBoundsException e) {
					}
				}
			}			
		}		
	}

	/**
	 * Diese Methode gibt das Array der Einträge aus dem Register zurück.
	 * @return das Array der Einträge
	 */
	public String[] getEintrag(){
		return eintrag;
	}
	
	/**
	 * Diese Methode gibt das Array der IDs aus dem Register zurück.
	 * @return das Array der IDs
	 */
	public String[] getID(){
		return id;
	}
	
}
