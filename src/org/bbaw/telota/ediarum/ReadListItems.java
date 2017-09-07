/**
 * ReadRegister.java - is a class to load and read external Documents for the ListItemOperations.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung).
 * @author Martin Fechner
 * @version 1.0.0
 */
package org.bbaw.telota.ediarum;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ro.sync.ecss.extensions.api.AuthorOperationException;

public class ReadListItems {

	/**
	 * interne Variablen, die Einträge und IDs des Registers.
	 */
	private String[] eintrag, id;

	/**
	 * Der Konstruktor liest das Dokument der URL mit den benannten Knoten aus und konstruiert den Eintrag und die Id für jeden Knoten.
	 * @param indexURI Die URL zur Registerdatei
	 * @param node Der X-Path-Ausdruck für die Knoten der einzelnen Registereinträge
	 * @param eintragExp Der Ausdruck um einen Registereintrag zu konstruieren.
	 * @param idExp Der Ausdruck um die ID für einen Registereintrag zu konstruieren. Er setzt sich wie eintragExp zusammen.
	 * @throws AuthorOperationException
	 */
	public ReadListItems(String indexURI, String node, String eintragExp, String idExp, String namespaceDecl) {

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
			// Für Namespaces:
			String[] namespaceSplit = namespaceDecl.split(" ");
			String[][] namespaces = new String[namespaceSplit.length][2];
			for (int i=0; i<namespaceSplit.length; i++) {
				String currentNamespace = namespaceSplit[i];
				int k = currentNamespace.indexOf(":");
				namespaces[i][0] = currentNamespace.substring(0, k);
				namespaces[i][1] = currentNamespace.substring(k+1);
			}
			final String[][] namespacesfinal = namespaces;
//			final PrefixResolver resolver = new PrefixResolverDefault(indexDoc);
			NamespaceContext ctx = new NamespaceContext() {

				@Override
				public String getNamespaceURI(String prefix) {
//					return resolver.getNamespaceForPrefix(prefix);
					String uri = null;
					for (int i=0; i<namespacesfinal.length; i++) {
						if (prefix.equals(namespacesfinal[i][0])) {
							uri = namespacesfinal[i][1];
						}
					}
					return uri;
				}

				@Override
				public String getPrefix(String namespaceURI) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Iterator getPrefixes(String namespaceURI) {
					// TODO Auto-generated method stub
					return null;
				}

			};
			xpath.setNamespaceContext(ctx);
			// Das XPath-Query wird definiert.
			//XPathExpression expr = xpath.compile(node);

			// Die Resultate werden ausgelesen..
			//Object result = expr.evaluate(indexDoc, XPathConstants.NODESET);
			Object result = xpath.evaluate(node, indexDoc, XPathConstants.NODESET);
			NodeList registerNodes = (NodeList) result;

			// .. dann werden für die Einträge und IDs entsprechend lange Arrays angelegt.
			eintrag = new String[registerNodes.getLength()];
			id = new String[registerNodes.getLength()];

			// Für jeden Knoten ..
			for (int i=0; i < registerNodes.getLength(); i++) {
				Element currentElement = (Element) registerNodes.item(i);
				// .. wird der Eintrag konstruiert.
				eintrag[i] = "";
				String currentEintrag = eintragExp;
				// Falls im Ausdruck der Hinweis "$XPATH{..}" vorkommt, ..
				int k = currentEintrag.indexOf("$XPATH{");
				while (k>=0) {
					// .. wird der String davor als Text eingefügt, ..
					eintrag[i] += currentEintrag.substring(0, k);
					currentEintrag = currentEintrag.substring(k);
					int l = currentEintrag.indexOf("}");
					// .. und der Ausdruck selbst ausgewertet:
					String xpathExpression = currentEintrag.substring("$XPATH{".length(), l);
					// Jedes Glied kann entweder als Attribut, ..
					if (xpathExpression.startsWith("@")) {
						String attributeName = xpathExpression.substring(1);
						eintrag[i] += currentElement.getAttribute(attributeName);
					}
					// .. als nachkommendes Element ..
					if (xpathExpression.startsWith("//")) {
						// .. (was direkt gelesen werden kann und schnell geht, ..
						String elementName = xpathExpression.substring(2);
						if (elementName.contains(":")) {
							elementName = elementName.substring(elementName.indexOf(":")+1);
						}
						if (currentElement.getElementsByTagName(elementName).getLength()>0){
							eintrag[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
						} else {
							// .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
							XPathExpression queryExpr = xpath.compile("."+xpathExpression);
							NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
							if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
								eintrag[i] += elementNodes.item(0).getTextContent();
							}
						}
						// .. als direktes Kindelement (was schnell geht) ..
					} else if (xpathExpression.startsWith("/")) {
						String elementName = xpathExpression.substring(1);
						if (elementName.contains(":")) {
							elementName = elementName.substring(elementName.indexOf(":")+1);
						}
						if (currentElement.getElementsByTagName(elementName).getLength()>0){
							eintrag[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
						}
					}
					// .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
					if (xpathExpression.startsWith(".")) {
						XPathExpression queryExpr = xpath.compile(xpathExpression);
						System.out.println(xpathExpression);
						NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
						System.out.println(":"+elementNodes.item(0).getTextContent()+":");
						if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
							eintrag[i] += elementNodes.item(0).getTextContent();
						}
					}
					// Für X-Path-Ausdrücke mit Funktionen:
					if (xpathExpression.startsWith("#")) {
						XPathExpression queryExpr = xpath.compile(xpathExpression.substring(1));
						System.out.println(xpathExpression);
						String elementString = (String) queryExpr.evaluate(registerNodes.item(i), XPathConstants.STRING);
						System.out.println(":"+elementString+":");
						eintrag[i] += elementString;
					}					// Der übrige Ausdruck wird danach ausgewertet.
					currentEintrag = currentEintrag.substring(l+1);
					k = currentEintrag.indexOf("$XPATH{");
				};
				// Falls "$XPATH[..]" nicht mehr auftaucht, wird der Rest angehängt.
				eintrag[i] += currentEintrag;


				// Genauso wird die ID konstruiert.
				id[i] = "";
				String currentID = idExp;
				// Falls im Ausdruck der Hinweis "$XPATH{..}" vorkommt, ..
				k = currentID.indexOf("$XPATH{");
				while (k>=0) {
					// .. wird der String davor als Text eingefügt, ..
					id[i] += currentID.substring(0, k);
					currentID = currentID.substring(k);
					int l = currentID.indexOf("}");
					// .. und der Ausdruck selbst ausgewertet:
					String xpathExpression = currentID.substring("$XPATH{".length(), l);
					// Jedes Glied kann entweder als Attribut, ..
					if (xpathExpression.startsWith("@")) {
						String attributeName = xpathExpression.substring(1);
						id[i] += currentElement.getAttribute(attributeName);
					}
					// .. als nachkommendes Element ..
					if (xpathExpression.startsWith("//")) {
						// .. (was direkt gelesen werden kann und schnell geht, ..
						String elementName = xpathExpression.substring(2);
						if (elementName.contains(":")) {
							elementName = elementName.substring(elementName.indexOf(":")+1);
						}
						if (currentElement.getElementsByTagName(elementName).getLength()>0){
							id[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
						} else {
							// .. oder welches über eine X-Path-Abfrage gelesen werden kann und lange dauert), ..
							XPathExpression queryExpr = xpath.compile("."+xpathExpression);
							NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
							if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
								id[i] += elementNodes.item(0).getTextContent();
							}
						}
						// .. als direktes Kindelement (was schnell geht) ..
					} else if (xpathExpression.startsWith("/")) {
						String elementName = xpathExpression.substring(1);
						if (elementName.contains(":")) {
							elementName = elementName.substring(elementName.indexOf(":")+1);
						}
						if (currentElement.getElementsByTagName(elementName).getLength()>0){
							id[i] += currentElement.getElementsByTagName(elementName).item(0).getTextContent();
						}
					}
					// .. oder als X-Path-Ausdruck (was sehr lange dauert) verstanden werden.
					if (xpathExpression.startsWith(".")) {
						XPathExpression queryExpr = xpath.compile(xpathExpression);
						NodeList elementNodes = (NodeList) queryExpr.evaluate(registerNodes.item(i), XPathConstants.NODESET);
						if (elementNodes.getLength()>0 && elementNodes.item(0).getNodeType() == Node.ELEMENT_NODE){
							id[i] += elementNodes.item(0).getTextContent();
						}
					}
					// Der übrige Ausdruck wird danach ausgewertet.
					currentID = currentID.substring(l+1);
					k = currentID.indexOf("$XPATH{");
				};
				// Falls "$XPATH[..]" nicht mehr auftaucht, wird der Rest angehängt.
				id[i] += currentID;
			};
		} catch (XPathExpressionException e) {
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
