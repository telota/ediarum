/**
 * EdiarumLinkTextResolver.java - is a class to link text to a node.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung).
 * @author Martin Fechner
 * @version 1.0.1
 */
package org.bbaw.telota.ediarum.extensions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.link.InvalidLinkException;
import ro.sync.ecss.extensions.api.link.LinkTextResolver;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class EdiarumLinkTextResolver extends LinkTextResolver implements ContentHandler {

	/**
	 * Der AuthorAccess zu der geöffneten Datei
	 */
	private AuthorAccess authorAccess;

	/**
	 * Der Name des referenzierenden Elementes
	 */
	private String refElement = "";

	/**
	 * Der Name des referenzierenden Attributes
	 */
	private String refAttr = "";

	/**
	 * Ein eventueller Prefix vor der referenzierenden ID
	 */
	private String refIDprefix = ":";

	/**
	 * Die URL zu der referenzierten Datei
	 */
	private String indexURI = "";

	/**
	 * Der Name des referenzierten Elementes
	 */
	private String indexElem = "";

	/**
	 * Der Name des referenzierten Attributes
	 */
	private String indexAttr = "";

	/**
	 * Ein eventueller Prefix vor der referenzierten ID
	 */
	private String indexIDprefix = "";

	/**
	 * Eine Variable für die referenzierte ID
	 */
	private String id;

	/**
	 * Eine Variable für den auszugebenden Text
	 */
	private String linkText;

	/**
	 * Eine Variable für die Verarbeitung der zu verlinkenden Datei
	 */
	private Boolean getText;


	/**
	 * Wird aufgerufen wenn eine entsprechende Datei geöffnet wird und übergibt den AuthorAccess.
	 * Liest zudem die entsprechenden Editor Variablen aus.
	 */
	@Override
	public void activated(AuthorAccess authorAcc) {
		this.authorAccess = authorAcc;
		String linktext_url = authorAccess.getUtilAccess().expandEditorVariables("${EDIARUM_LINKTEXT_URL}", null);
		if (!linktext_url.startsWith("http")) {
//			String errorMessage = "Die ${EDIARUM_LINKTEXT_URL} ist nicht korrekt gesetzt.";
//			authorAccess.getWorkspaceAccess().showErrorMessage(errorMessage);
		} else {
			indexURI = linktext_url;
		}
		String[] linktext_vars = (" "+authorAccess.getUtilAccess().expandEditorVariables("${EDIARUM_LINKTEXT_VARS}", null)+" ").split(",");
		if (linktext_vars.length != 6) {
//			String errorMessage = "Die ${EDIARUM_LINKTEXT_VARS} sind nicht korrekt gesetzt: " +
//					"refElement, refAttr, refIDprefix, indexElem, indexAttr, indexIDprefix";
//			authorAccess.getWorkspaceAccess().showErrorMessage(errorMessage);
		} else {
			refElement = linktext_vars[0].trim();
			refAttr = linktext_vars[1].trim();
			refIDprefix = linktext_vars[2].trim();
			indexElem = linktext_vars[3].trim();
			indexAttr = linktext_vars[4].trim();
			indexIDprefix = linktext_vars[5].trim();
		}
	}

	/**
	 * Die Funktion wird über die CSS Funktion oxy_linktext() aufgerufen und übergibt den momentanen Knoten.
	 * Sie verarbeitet die referenzierte Datei und gibt einen passenden Ausgabetext zurück.
	 *
	 * @param node: der übergebene Knoten
	 * @return der passende Ausgabetext
	 */
	@Override
	public String resolveReference(AuthorNode node) throws InvalidLinkException {
		// Die Variablen werden vorbereitet.
		this.clearReferencesCache();
		id = "";
		getText = false;
		String result = "";
		// Wenn der zu verarbeitende Knoten ein Elementknoten ist, ..
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			AuthorElement element = (AuthorElement) node;
			// .. von der richtigen Sorte ist, ..
			if (refElement.equals(element.getLocalName())) {
				// .. und das richtige Attribut besitzt, ..
				AttrValue attribute = element.getAttribute(refAttr);
				if (attribute != null) {
					if (refIDprefix.equals(attribute.getValue().substring(0, refIDprefix.length()))) {
						// .. dann lese zunächst die ID aus dem Attribut aus.
						id = attribute.getValue().substring(refIDprefix.length());
 					try {
 						linkText = "";
 						// Erzeuge eine XML-Verarbeitung ..
						XMLReader xmlReader = XMLReaderFactory.createXMLReader();
						// .. mit den richtig definierten Verarbeitungsroutinen, ..
						xmlReader.setContentHandler(this);
						// .. lese die referenzierte Datei ein, ..
						URL absoluteUrl = new URL(indexURI);
						InputSource inputSource = new InputSource(absoluteUrl.toString());
						// .. und verarbeite diese mit der definierten Routine.
 						xmlReader.parse(inputSource);
 						// Kürze schließlich überflüssige Leerzeichen im auszugebenden Text, ..
 						result = linkText.replace("\n", "").replace("\r", "").replaceAll("\\s+", " ").trim();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
 					}
 				}
			}
			// Fallback für alle Elemente ..
			else {
				// .. ist das @ana-Attribut, ..
				AttrValue attribute = element.getAttribute("ana");
				if (attribute != null) {
					// .. nimm alle Werte, ..
					String[] values = attribute.getValue().split(" ");
					// .. und lese dann für jeden ..
					for (int i=0; i<values.length; i++) {
						if ("#".equals(values[i].substring(0, "#".length()))) {
							// .. zunächst die ID aus dem Attribut aus.
							id = values[i].substring("#".length());
		 					try {
		 						linkText = "";
		 						// Erzeuge eine XML-Verarbeitung ..
								XMLReader xmlReader = XMLReaderFactory.createXMLReader();
								// .. mit den richtig definierten Verarbeitungsroutinen, ..
								xmlReader.setContentHandler(this);
								// .. lese die referenzierte Datei ein, ..
								URL absoluteUrl = new URL(indexURI);
								InputSource inputSource = new InputSource(absoluteUrl.toString());
								// .. und verarbeite diese mit der definierten Routine.
		 						xmlReader.parse(inputSource);
		 						// Kürze schließlich überflüssige Leerzeichen im auszugebenden Text, ..
		 						result += linkText.replace("\n", "").replace("\r", "").replaceAll("\\s+", " ").trim() + "; ";
							} catch (SAXException e) {
								e.printStackTrace();
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
 					}
 				}
			}
		}
		// .. und gebe ihn aus.
		return result;
	}

	/**
	 * Verarbeitungsroutine für die XML-Starttags
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// Wenn das Element vom referenzierten Typ ist ..
		if (indexElem.equals(qName)) {
			// und die passende ID im Attribut hat, ..
			String attribute = attributes.getValue("", indexAttr);
			if ( (indexIDprefix+id).equals(attribute) ) {
				// .. dann verarbeite den kommenden Text.
				getText = true;
			}
		}
	}

	/**
	 * Verarbeitungsroutine für Textknoten
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// Wenn der Text verarbeitet werden soll, ..
		if(getText) {
			// .. übertrage ihn in den auszugebenden Text.
			String characterData = (new String(ch, start, length));
			linkText += characterData;
		}
	}

	/**
	 * Verarbeitungsroutine für XML-Endtags
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// Wenn das Element vom referenzierten Typ ist, ..
		if (indexElem.equals(qName)) {
			// beende die Textverarbeitung.
			getText = false;
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub

	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}
}
