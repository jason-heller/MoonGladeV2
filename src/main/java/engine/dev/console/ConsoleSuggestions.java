package engine.dev.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;

public class ConsoleSuggestions {
	private JList<String> list;
	private JPopupMenu popupMenu;
	private JTextField textArea;
	
	private String subWord;
	private int insertionPosition;

	public ConsoleSuggestions(JTextField textArea) {
		this.textArea = textArea;
        popupMenu = new JPopupMenu();
        popupMenu.removeAll();
        popupMenu.setOpaque(false);
        popupMenu.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
    }
	
	private void update(int position, String subWord, Point location, List<String> suggestions) {
		this.insertionPosition = position;
        this.subWord = subWord;
        
        popupMenu.removeAll();
        popupMenu.add(list = createSuggestionList(position, suggestions), BorderLayout.CENTER);
        popupMenu.show(textArea, location.x, textArea.getHeight());
	}

	public void hide() {
		popupMenu.setVisible(false);
	}

	private JList<String> createSuggestionList(final int position, final List<String> suggestions) {

		String[] arr = new String[suggestions.size()];
        arr = suggestions.toArray(arr);
		
		JList<String> list = new JList<>(arr);
		list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.setBackground(Color.DARK_GRAY);
		list.setForeground(Color.WHITE);
		
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					insertSelection();
				}
			}
		});
		return list;
	}

	public boolean insertSelection() {
		if (list.getSelectedValue() != null) {
			try {
				final String selectedSuggestion = ((String) list.getSelectedValue()).substring(subWord.length());
				textArea.getDocument().insertString(insertionPosition, selectedSuggestion, null);
				return true;
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			hide();
		}
		return false;
	}

	public void moveUp() {
		int index = Math.min(list.getSelectedIndex() - 1, 0);
		selectIndex(index);
	}

	public void moveDown() {
		int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
		selectIndex(index);
	}

	private void selectIndex(int index) {
		//final int position = textArea.getCaretPosition();
		list.setSelectedIndex(index);
		//textArea.setCaretPosition(position);
	}

	public void showSuggestions(List<String> suggestions) {
		hide();
		final int position = textArea.getCaretPosition();
		Point location;
		try {
			Rectangle2D r = textArea.modelToView2D(position);
			location = new Point();
			location.x = -2;
			location.y = ((int) r.getMinY());
		} catch (BadLocationException e2) {
			e2.printStackTrace();
			return;
		}
		String text = textArea.getText();
		int start = Math.max(0, position - 1);
		while (start > 0) {
			if (!Character.isWhitespace(text.charAt(start))) {
				start--;
			} else {
				start++;
				break;
			}
		}
		
		if (start > position)
			return;

		final String subWord = text.substring(start, position);
		
		if (subWord.length() == 0)
			return;
		
		update(position, subWord, location, suggestions);
	}

	public boolean isVisible() {
		return popupMenu.isVisible();
	}

	public String getSuggestion() {
		if (list.getSelectedValue() != null) {
			String str = ((String) list.getSelectedValue());
			int spaceIndex = str.indexOf(' ');
			int cmdEnd = spaceIndex == -1 ? str.length() : spaceIndex;
			return str.substring(0, cmdEnd) + (spaceIndex == -1 ? "" : " ");
		}
		return "";
	}

	
}
