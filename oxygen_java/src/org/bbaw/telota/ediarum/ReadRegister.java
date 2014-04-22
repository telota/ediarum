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
 * ReadRegister.java - is a class to load and read external Documents for the RegisterOperations.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.1.0
 */
package org.bbaw.telota.ediarum;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ReadRegister {
	
	/**
	 * interne Variablen, die Einträge und IDs des Registers.
	 */
	private String[] eintrag, id;

	/**
	 * Der Konstruktor liest das Dokument der URL mit den benannten Knoten aus und konstruiert den Eintrag und die Id für jeden Knoten.
	 * @param indexURI Die URL zur Registerdatei
	 * @param node Der X-Path-Ausdruck für die Knoten der einzelnen Registereinträge
	 * @param eintragExp Der Ausdruck um einen Registereintrag zu konstruieren. Er setzt sich aus Strings in "", Attributen beginnend mit @,
	 *  Elementen mit / oder // und aus X-Path-Ausdrücken beginnend mit . zusammen. Die einzelnen Teile werden mit + verbunden.
	 * @param idExp Der Ausdruck um die ID für einen Registereintrag zu konstruieren. Er setzt sich wie eintragExp zusammen.
	 * @throws AuthorOperationException
	 */
	public ReadRegister(String indexURI, String node, String eintragExp, String idExp) {
		
		try {
			// Das neue Dokument wird vorbereitet.
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true); 
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			
			// Wenn es sich um eine URL mit Authentifizierung handelt, ..
			InputStream is;
			if (indexURI.indexOf('@')>-1) {
				// .. werden die Verbindungsdaten gelesen ..
				String authString = indexURI.substring(indexURI.indexOf("://")+3, indexURI.indexOf('@'));
				String webPage = indexURI.substring(0, indexURI.indexOf("://")+3)+indexURI.substring(indexURI.indexOf('@')+1);
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				String authStringEnc = new String(authEncBytes);
				
				// .. und eine Verbindung mit Login geöffnet.
				URL url = new URL(webPage);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
				is = urlConnection.getInputStream();
			} else {
				// Im anderen Fall wird direkt eine Verbindung geöffnet.
				URL url = new URL(indexURI);
				URLConnection urlConnection = url.openConnection();
				is = urlConnection.getInputStream();
			}
			
			// Dann wird die Datei gelesen.
			InputSource inputSource = new InputSource(is);
			Document indexDoc = builder.parse(inputSource);
			// Die xPath-Routinen werden vorbereitet.
			XPath xpath = XPathFactory.newInstance().newXPath();
			// Das XPath-Query wird definiert.
			XPathExpression expr = xpath.compile(node);
			
			// Die Resultate werden ausgelesen..
			Object result = expr.evaluate(indexDoc, XPathConstants.NODESET);
			NodeList registerNodes = (NodeList) result;
			
			// .. dann werden für die Einträge und IDs entsprechend lange Arrays angelegt.
			eintrag = new String[registerNodes.getLength()];
			id = new String[registerNodes.getLength()];

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
			for (int i=0; i < registerNodes.getLength(); i++) {
				Element currentElement = (Element) registerNodes.item(i);
				// .. wird der Eintrag konstruiert.
				eintrag[i] = "";
				// Dazu werden die Glieder des entsprechenden Ausdrucks interpretiert.
				for (int j=0; j < eintragExpression.length; j++) {
					try {
						// Jedes Glied kann entweder als Attribut, ..
						if (eintragExpression[j].startsWith("@")) {
							String attributeName = eintragExpression[j].substring(1);
							eintrag[i] += currentElement.getAttribute(attributeName);
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
						// **
						// .. als nachkommendes Element ..
						if (eintragExpression[j].startsWith("//")) {
							// .. (was direkt gelesen werden kann und schnell geht, ..
							String elementName = eintragExpression[j].substring(2);
							if (currentElement.getElementsByTagName(elementName).getLength()>0){
								eintrag[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
							} else {
								// .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
								XPathExpression queryExpr = xpath.compile("."+eintragExpression[j]);
								NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
								if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
									eintrag[i] += elementNodes.item(0).getTextContent();
								}
							}
							// .. als direktes Kindelement (was schnell geht) ..
						} else if (eintragExpression[j].startsWith("/")) {
							String elementName = eintragExpression[j].substring(1);
							if (currentElement.getElementsByTagName(elementName).getLength()>0){
								eintrag[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
							}
						}
						// .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
						if (eintragExpression[j].startsWith(".")) {
							XPathExpression queryExpr = xpath.compile(eintragExpression[j]);
							NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
							if (elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
								eintrag[i] += elementNodes.item(0).getTextContent();
							}
						}
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
							id[i] += currentElement.getAttribute(attributeName);
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
							if (currentElement.getElementsByTagName(elementName).getLength()>0){
								id[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
							} else {
								// .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
								XPathExpression queryExpr = xpath.compile("."+idExpression[j]);
								NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
								if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
									id[i] += elementNodes.item(0).getTextContent();
								}
							}
							// .. als direktes Kindelement (was schnell geht) ..
						} else if (idExpression[j].startsWith("/")) {
							String elementName = idExpression[j].substring(1);
							if (currentElement.getElementsByTagName(elementName).getLength()>0){
								id[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
							}
						}
						// .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
						if (idExpression[j].startsWith(".")) {
							XPathExpression queryExpr = xpath.compile(idExpression[j]);
							NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
							if (elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
								id[i] += elementNodes.item(0).getTextContent();
							}
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}
				}
			}			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
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
