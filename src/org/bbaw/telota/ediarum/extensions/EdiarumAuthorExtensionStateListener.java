/**
 * EdiarumAuthorExtensionStateListener.java - is a class for configuring special tasks for a document type.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung).
 * @author Martin Fechner
 * @version 1.0.4
 */
package org.bbaw.telota.ediarum.extensions;

import java.awt.Frame;

import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;

import org.bbaw.telota.ediarum.AskDialog;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorDocumentFilter;
import ro.sync.ecss.extensions.api.AuthorDocumentFilterBypass;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class EdiarumAuthorExtensionStateListener implements AuthorExtensionStateListener {

	private AuthorAccess authorAccess;
	private EdiarumAuthorExtensionStateListenerImage character;

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Diese Methode wird aufgerufen, wenn das Dokument geöffnet wird.
	 */
	@Override
	public void activated(final AuthorAccess authorAcc) {
		this.authorAccess = authorAcc;
		// Ein neuer Filter wird erstellt.
		authorAccess.getDocumentController().setDocumentFilter(new AuthorDocumentFilter() {
			/**
			 * Hiermit wird die Eingaberoutine von Zeichen überschrieben. Dies wird für die Einfügung von Spezialzeichen benötigt.
			 * @see ro.sync.ecss.extensions.api.AuthorDocumentFilter#insertText(ro.sync.ecss.extensions.api.AuthorDocumentFilterBypass, int, java.lang.String)
			 */
			@Override
			public void insertText(AuthorDocumentFilterBypass filterBypass, int offset, String toInsert) {
				// Wenn ein Benutzer ein Anführungszeichen eingibt, ..
				if(toInsert.length() == 1 && "\"".equals(toInsert)) {
					try {
						// .. wird das vorherige Zeichen eingelesen.
						Segment seg = new Segment();
						authorAccess.getDocumentController().getChars(offset-1, 1, seg);
						char ch = seg.toString().charAt(0);
						// Normalerweise sollen keine Anführungsstriche eingefügt werden, ..
						boolean insertStartQuote = false;
						// es sei denn, das vorherige Zeichen ist ein Leerzeichen.
						if (' ' == ch){
							insertStartQuote = true;
						}
						// Das einzusetzende Zeichen wird entsprechend ersetzt.
						if(insertStartQuote) {
							toInsert = "\u201E";
						} else {
							toInsert = "\u201C";
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				// Der folgende Code wurde von Oxygen bereitgestellt und behandelt das Problem leicht anders:
				//User typed a quote but he actually needs a smart quote.
				//So we either have to add \u201E (start smart quote)
				//Or we add \u201C (end smart quote)
				//Depending on whether we already have a start smart quote inserted in the current paragraph.
				/* if(toInsert.length() == 1 && "\"".equals(toInsert)) {
					try {
						AuthorNode currentNode = authorAccess.getDocumentController().getNodeAtOffset(offset);
						int startofTextInCurrentNode = currentNode.getStartOffset();
						if(offset > startofTextInCurrentNode) {
							Segment seg = new Segment();
							authorAccess.getDocumentController().getChars(startofTextInCurrentNode, offset - startofTextInCurrentNode, seg);
							String previosTextInNode = seg.toString();
							boolean insertStartQuote = true;
							for (int i = previosTextInNode.length() - 1; i >= 0; i--) {
								char ch = previosTextInNode.charAt(i);
								if('\u201C' == ch) {
									//Found end of smart quote, so yes, we should insert a start one
									break;
								} else if('\u201E' == ch) {
									//Found start quote, so we should insert an end one.
									insertStartQuote = false;
									break;
								}
							}

							if(insertStartQuote) {
								toInsert = "\u201E";
							} else {
								toInsert = "\u201C";
							}
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}*/
				// Die folgende Funktion wird nur gesetzt, wenn die passende Editor Variable gesetzt ist.
				String pipeToPagebreak = authorAccess.getUtilAccess().expandEditorVariables("${EDIARUM_PIPE_TO_PAGEBREAK}", null);
				if ("true".equals(pipeToPagebreak)) {
					// Falls das einzusetzende Zeichen ein senkrechter Strich ist, ..
					if(toInsert.length() == 1 && "|".equals(toInsert)) {
						try {
							// .. werden zunächst zwei Abfragen gestartet, ..
							Frame parent = (Frame) authorAccess.getWorkspaceAccess().getParentFrame();
							AskDialog askType = new AskDialog(parent, "Foliozählung des Manuskript oder Seitenzählung eines Drucks", "Manuskript");
							AskDialog askPage = new AskDialog(parent, "Folio bzw. Seitenzahl", "");

							// .. und dann wird das xml-Fragment für den Seitenwechsel an der aktuellen Stelle eingefügt.
							String fragment = "<pb xmlns=\"http://www.tei-c.org/ns/1.0\" ed=\"" +
									askType.getResult() +
									"\" n=\"" +
									askPage.getResult() +
									"\" />";
							int caretPos = authorAccess.getEditorAccess().getCaretOffset();
							authorAccess.getDocumentController().insertXMLFragment(fragment, caretPos);
							// Es soll kein weiteres Zeichen eingefügt werden, und die Positionsmarke wird an die richtige Stelle gesetzt.
							toInsert = "";
							offset = authorAccess.getEditorAccess().getCaretOffset();
						} catch (AuthorOperationException e) {}
					}
				}
				System.err.println("INSERT TEXT |" + toInsert + "|");
				// Hier wird schließlich das eingetippte Zeichen geschrieben.
				super.insertText(filterBypass, offset, toInsert);
				character.show(authorAcc, toInsert);
			}
		});

	}

	@Override
	public void deactivated(AuthorAccess authorAccess) {
		// TODO Auto-generated method stub

	}

}
