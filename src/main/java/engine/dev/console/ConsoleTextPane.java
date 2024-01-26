package engine.dev.console;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

@SuppressWarnings("serial")
public class ConsoleTextPane extends JTextPane {
	private int rows, columns;
	private int rowHeight, columnWidth;
	
	private Style style;

	public ConsoleTextPane(int rows, int columns) {
		super(new DefaultStyledDocument());

		this.rows = rows;
		this.columns = columns;
		
		StyleContext context = new StyleContext();
		style = context.addStyle("console", null);
		
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		setCharacterAttributes(attributeSet, true);

	}

	public int getRowHeight() {
		if (rowHeight == 0) {
			FontMetrics metrics = getFontMetrics(getFont());
			rowHeight = metrics.getHeight();
		}

		return rowHeight;
	}

	public int getColumnWidth() {
		if (columnWidth == 0) {
			FontMetrics metrics = getFontMetrics(getFont());
			columnWidth = metrics.charWidth('m');
		}

		return columnWidth;
	}
	
	public void setRows(int rows) {
		if (rows != this.rows) {
			this.rows = rows;
			revalidate();
		}
	}

	public void setColumns(int columns) {
		if (columns != this.columns) {
			this.columns = columns;
			revalidate();
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();

		d = (d == null) ? new Dimension(640, 480) : d;
		Insets insets = getInsets();

		if (columns != 0)
			d.width = Math.max(d.width, columns * columnWidth + insets.left + insets.right);

		if (rows != 0)
			d.height = Math.max(d.height, rows * rowHeight + insets.top + insets.bottom);

		return d;
	}

	public void append(String text, Color color) {	
		Document doc = getDocument();
		StyleConstants.setForeground(style,color);
		
		try {
			doc.insertString(doc.getLength(), text, style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}