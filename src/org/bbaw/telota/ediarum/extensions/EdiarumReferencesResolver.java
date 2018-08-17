/**
 * EdiarumReferencesResolver.java - is a test class.
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

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorReferenceResolver;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class EdiarumReferencesResolver implements AuthorReferenceResolver {

	private String elemName = "persName";
	private String attrName = "ref";

	/*
	 * In the listing below, the XML document contains the ref element:
	 * <ref location="referred.xml">Reference</ref>
	 */

	/*
	 * The hasReferences method verifies if the handler considers the node to have references. It takes as argument
	 * an AuthorNode that represents the node which will be verified. The method will return true if the node is
	 * considered to have references. In our case, to be a reference the node must be an element with the name ref and it
	 * must have an attribute named location.
	 *
	 * 	public boolean hasReferences(AuthorNode node) {
	 * 		boolean hasReferences = false;
	 * 		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	 * 			AuthorElement element = (AuthorElement) node;
	 * 			if ("ref".equals(element.getLocalName())) {
	 * 				AttrValue attrValue = element.getAttribute("location");
	 * 				hasReferences = attrValue != null;
	 * 			}
	 * 			Oxygen XML Author | Author Developer Guide | 308
	 * 		}
	 * 		return hasReferences;
	 * 	}
	 */
	@Override
	public boolean hasReferences(AuthorNode node) {
		boolean hasReferences = false;
 		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
 			AuthorElement element = (AuthorElement) node;
 			if (elemName.equals(element.getLocalName())) {
 				AttrValue attrValue = element.getAttribute(attrName);
 				hasReferences = (attrValue != null);
 			}
 		}
 		return hasReferences;
	}

	/*
	 * The method getDisplayName returns the display name of the node that contains the expanded referred content.
	 * It takes as argument an AuthorNode that represents the node for which the display name is needed. The referred
	 * content engine will ask this AuthorReferenceResolver implementation what is the display name for each
	 * node which is considered a reference. In our case the display name is the value of the location attribute from the ref
	 * element.
	 *
	 * 	public String getDisplayName(AuthorNode node) {
	 * 		String displayName = "ref-fragment";
	 * 		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	 * 			AuthorElement element = (AuthorElement) node;
	 * 			if ("ref".equals(element.getLocalName())) {
	 * 				AttrValue attrValue = element.getAttribute("location");
	 * 				if (attrValue != null) {
	 * 					displayName = attrValue.getValue();
	 * 				}
	 * 			}
	 * 		}
	 * 		return displayName;
	 * 	}
	 */
	@Override
	public String getDisplayName(AuthorNode node) {
		String displayName = "ref-fragment";
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			AuthorElement element = (AuthorElement) node;
			if (elemName.equals(element.getLocalName())) {
				AttrValue attrValue = element.getAttribute(attrName);
				if (attrValue != null) {
					displayName = attrValue.getValue();
				}
			}
		}
		return displayName;
	}

	/*
	 * The method resolveReference resolves the reference of the node and returns a SAXSource with the parser
	 * and the parser's input source. It takes as arguments an AuthorNode that represents the node for which the reference
	 * needs resolving, the systemID of the node, the AuthorAccess with access methods to the Author data model and
	 * a SAX EntityResolver which resolves resources that are already opened in another editor or resolve resources
	 * through the XML catalog. In the implementation you need to resolve the reference relative to the systemID, and
	 * create a parser and an input source over the resolved reference.
	 *
	 * public SAXSource resolveReference(
	 * 			AuthorNode node,
	 * 			String systemID,
	 * 			AuthorAccess authorAccess,
	 * 			EntityResolver entityResolver) {
	 * 	SAXSource saxSource = null;
	 * 	if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	 * 		AuthorElement element = (AuthorElement) node;
	 * 		if ("ref".equals(element.getLocalName())) {
	 * 			AttrValue attrValue = element.getAttribute("location");
	 * 			if (attrValue != null) {
	 * 				String attrStringVal = attrValue.getValue();
	 * 				try {
	 * 					URL absoluteUrl = new URL(new URL(systemID),
	 * 					authorAccess.correctURL(attrStringVal));
	 * 					InputSource inputSource = entityResolver.resolveEntity(null,
	 * 					absoluteUrl.toString());
	 * 					if(inputSource == null) {
	 * 						inputSource = new InputSource(absoluteUrl.toString());
	 * 					}
	 * 					XMLReader xmlReader = authorAccess.newNonValidatingXMLReader();
	 * 					xmlReader.setEntityResolver(entityResolver);
	 * 					saxSource = new SAXSource(xmlReader, inputSource);
	 * 				} catch (MalformedURLException e) {
	 * 					logger.error(e, e);
	 * 				} catch (SAXException e) {
	 * 					logger.error(e, e);
	 * 				} catch (IOException e) {
	 * 					logger.error(e, e);
	 * 				}
	 * 			}
	 * 		}
	 * 	}
	 * 	return saxSource;
	 * }
	 */
	@Override
	public SAXSource resolveReference(AuthorNode node, String systemID,
			AuthorAccess authorAccess, EntityResolver entityResolver) {
		SAXSource saxSource = null;
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			AuthorElement element = (AuthorElement) node;
			if (elemName.equals(element.getLocalName())) {
				AttrValue attrValue = element.getAttribute(attrName);
				if (attrValue != null) {
					String attrStringVal = attrValue.getValue();
					try {
						URL absoluteUrl = new URL(/*new URL(systemID)*/new URL("http://user:pass@www.example.com:port/exist/webdav/db/index.xml"), authorAccess.getUtilAccess().correctURL(attrStringVal));
						InputSource inputSource = entityResolver.resolveEntity(null, absoluteUrl.toString());
						if(inputSource == null) {
							inputSource = new InputSource(absoluteUrl.toString());
						}
						XMLReader xmlReader = authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
						xmlReader.setEntityResolver(entityResolver);
						saxSource = new SAXSource(xmlReader, inputSource);
						saxSource.getXMLReader().getContentHandler();
					} catch (MalformedURLException e) {
//	  					logger.error(e, e);
	  				} catch (SAXException e) {
//	  					logger.error(e, e);
	  				} catch (IOException e) {
//	  					logger.error(e, e);
	  				}
				}
			}
		}
		return saxSource;
	}

	/*
	 * The method getReferenceUniqueID should return an unique identifier for the node reference. The unique
	 * identifier is used to avoid resolving the references recursively. The method takes as argument an AuthorNode that
	 * represents the node with the reference. In the implementation the unique identifier is the value of the location attribute
	 * from the ref element.
	 *
	 * 	public String getDisplayName(AuthorNode node) {
	 * 		String displayName = "ref-fragment";
	 * 			if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	 * 				AuthorElement element = (AuthorElement) node;
	 * 				if ("ref".equals(element.getLocalName())) {
	 * 					AttrValue attrValue = element.getAttribute("location");
	 * 					if (attrValue != null) {
	 * 					displayName = attrValue.getValue();
	 * 				}
	 * 			}
	 * 		}
	 * 		return displayName;
	 * 	}
	 */
	@Override
	public String getReferenceUniqueID(AuthorNode node) {
		String displayName = "ref-fragment";
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			AuthorElement element = (AuthorElement) node;
			if (elemName.equals(element.getLocalName())) {
				AttrValue attrValue = element.getAttribute(attrName);
				if (attrValue != null) {
					displayName = attrValue.getValue();
				}
			}
		}
		return displayName;
	}

	/*
	 * The method getReferenceSystemIDshould return the systemID of the referred content. It takes as arguments
	 * an AuthorNode that represents the node with the reference and the AuthorAccess with access methods to the
	 * Author data model. In the implementation you use the value of the location attribute from the ref element and resolve
	 * it relatively to the XML base URL of the node.
	 *
	 * 	public String getReferenceSystemID(AuthorNode node,
	 * 				AuthorAccess authorAccess) {
	 * 		String systemID = null;
	 * 		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
	 * 			AuthorElement element = (AuthorElement) node;
	 * 			if ("ref".equals(element.getLocalName())) {
	 * 				AttrValue attrValue = element.getAttribute("location");
	 * 				if (attrValue != null) {
	 * 					String attrStringVal = attrValue.getValue();
	 * 					try {
	 * 						URL absoluteUrl = new URL(node.getXMLBaseURL(),
	 * 						authorAccess.correctURL(attrStringVal));
	 * 						systemID = absoluteUrl.toString();
	 * 					} catch (MalformedURLException e) {
	 * 						logger.error(e, e);
	 * 					}
	 * 				}
	 * 			}
	 * 		}
	 * 		return systemID;
	 * 	}
	 *
	 * Das hier ist die Verlinkung. Wenn man auf das entsprechende Symbol klickt wird der Link geöffnet.
	 */
	@Override
	public String getReferenceSystemID(AuthorNode node, AuthorAccess authorAccess) {
		String systemID = null;
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			AuthorElement element = (AuthorElement) node;
			if (elemName.equals(element.getLocalName())) {
				AttrValue attrValue = element.getAttribute(attrName);
				if (attrValue != null) {
					String attrStringVal = attrValue.getValue();
					try {
						URL absoluteUrl = new URL(/*node.getXMLBaseURL()*/new URL("http://user:pass@www.example.com:port/exist/webdav/db/index.xml"), authorAccess.getUtilAccess().correctURL(attrStringVal));
						systemID = absoluteUrl.toString();
					} catch (MalformedURLException e) {
//						logger.error(e, e);
					}
				}
			}
		}
		return systemID;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "TEST";
	}

	@Override
	public boolean isReferenceChanged(AuthorNode arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}



}
