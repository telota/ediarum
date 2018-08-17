package org.bbaw.telota.ediarum.dates;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Ein Dialog der den Text einer Datumsangabe und Text um die Angabe herum
 * ausgibt.
 *
 * @author Philipp Belitz
 */
public class DetailDialog extends JDialog {
	private static final long serialVersionUID = 5036858534500398049L;

	public DetailDialog(SearchResult sr) {
		setTitle("Result");
		addWindowListener(windowListener());
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JTextPane txtPane = new JTextPane();
		txtPane.setEditable(false);
		StyledDocument doc = txtPane.getStyledDocument();
		SimpleAttributeSet bold = new SimpleAttributeSet();
		StyleConstants.setBold(bold, true);

		String fulltxt = sr.getFullText();
		String date = sr.getText();
		String before = fulltxt.substring(0, fulltxt.indexOf(date));
		String after = fulltxt.substring(fulltxt.indexOf(date) + date.length(), fulltxt.length());

		try {
			doc.insertString(0, "..." + before, null);
			doc.insertString(doc.getLength(), date, bold);
			doc.insertString(doc.getLength(), after + "...", null);
		} catch (BadLocationException e) {
		}

		JScrollPane jscp = new JScrollPane(txtPane);
		jscp.setPreferredSize(new Dimension(400, 100));

		content.add(jscp, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 0)), BorderLayout.CENTER);

		JButton button = new JButton("Ok");
		button.setHorizontalAlignment(SwingConstants.RIGHT);
		button.addActionListener(buttonListener());
		buttonPanel.add(button, BorderLayout.EAST);
		content.add(buttonPanel, BorderLayout.SOUTH);

		add(content);

		pack();
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setVisible(true);
		repaint();
	}

	private WindowListener windowListener() {
		WindowListener wl = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				e.getWindow().dispose();
			}
		};

		return wl;
	}

	private ActionListener buttonListener() {
		ActionListener al = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};

		return al;
	}
}
