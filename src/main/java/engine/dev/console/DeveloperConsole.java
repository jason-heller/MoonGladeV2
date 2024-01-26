package engine.dev.console;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import org.lwjgl.glfw.GLFW;

import engine.Window;
import engine.dev.Log;
import engine.io.Input;

public class DeveloperConsole {

	private JFrame frame;
	private static ConsoleTextPane textArea;
	private JFormattedTextField input;
	private ConsoleSuggestions suggestions;

	public static boolean isVisible;

	// Basically a giant mess for testing. I hate swing.

	public DeveloperConsole() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception ignored) {
		}

		setEnabled(true);
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					createFrame();
				}
			});

			Command.initCommands();
		} else {
			close();
		}
	}

	private void createFrame() {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame = new JFrame("Console");

		Font font = new Font("Cascadia Code", Font.PLAIN, 14);
		FontMetrics metrics = frame.getFontMetrics(font);
		final int rowHeight = metrics.getHeight();
		int numRows = (int) Math.floor(343 / rowHeight);

		// Create and set up the window.
		frame.setPreferredSize(new Dimension(730, numRows * rowHeight));
		// frame.setResizable(false);

		frame.setLocationRelativeTo(null);
		frame.setBackground(Color.BLACK);
		frame.setForeground(Color.BLACK);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(false);

		Toolkit.getDefaultToolkit().setDynamicLayout(false);

		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		panel.setForeground(Color.BLACK);
		panel.setLayout(new GridBagLayout());

		// Set up the console components
		textArea = new ConsoleTextPane(numRows - 1, 16);
		textArea.setBackground(Color.BLACK);
		textArea.setCaretColor(Color.WHITE);
		textArea.setFont(font);
		textArea.setEditable(false);

		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		input = new JFormattedTextField();
		input.setBackground(Color.BLACK);
		input.setForeground(Color.WHITE);
		input.setCaretColor(Color.WHITE);
		input.setFont(font);
		
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		textAreaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		textAreaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JScrollPane inputScrollPane = new JScrollPane(input);
		inputScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		inputScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

		// Layout
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);

		panel.setLayout(layout);
		panel.add(textAreaScrollPane);
		panel.add(inputScrollPane, BorderLayout.SOUTH);
		
		// Finish the frame
		frame.add(panel);
		frame.pack();
		frame.setAlwaysOnTop(true);
		frame.setVisible(false);
		
		textArea.setRows((frame.getHeight() / textArea.getRowHeight()) - 1);
		textArea.setPreferredSize(textArea.getSize());
		textArea.setMaximumSize(textArea.getSize());
		
		Dimension taspDim = new Dimension(frame.getWidth() + 20, frame.getHeight() - 10);
		textAreaScrollPane.setPreferredSize(taspDim);
		textAreaScrollPane.setMaximumSize(taspDim);
		Log.info("WTF");
		
		// Add the logger
		Log.instance.addListener(new DebugLogListener());

		// Add the system streams
		//System.setOut(new PrintStream(new ConsoleOutputStream(textArea)));
		//System.setErr(new PrintStream(new ConsoleOutputStream(textArea)));
		
		Set<AWTKeyStroke> noKeys = Collections.emptySet();
		input.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, noKeys);
		input.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, noKeys);

		input.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            	switch(e.getKeyChar()) {
            	case KeyEvent.VK_ENTER:
            		JTextField txtArea = (JTextField) e.getSource();
            		String txt = txtArea.getText();
    				Command.processCommand(txt.substring(0, txt.length()));
    				txtArea.setText("");

    				suggestions.hide();
            		break;
            	case KeyEvent.VK_TAB:
            		if (suggestions != null && suggestions.isVisible()) {
            			input.setText(suggestions.getSuggestion());
        				suggestions.hide();
    	            }
            		break;
            	case KeyEvent.VK_BACK_SPACE:
            		if (suggestions == null)
                		suggestions = new ConsoleSuggestions(input);
                	
                    suggestions.showSuggestions(Command.getSuggestion(input.getText()));
            		break;
            	default:
            		char c = e.getKeyChar();
            		
            		if (Character.isLetterOrDigit(c)) {
                    	if (suggestions == null)
                    		suggestions = new ConsoleSuggestions(input);
                    	
                        suggestions.showSuggestions(Command.getSuggestion(input.getText() + c));

                    } else if (Character.isWhitespace(c)) {
                        suggestions.hide();
                    }
            	}
            	
            }

			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {}
		});

		// The input system doesn't play nicely with two contexts, and the toggle key
		// shares both of them, this is to make sure we can toggle the console (mostly)
		// seamlessly
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
				switch (ke.getID()) {
				case KeyEvent.KEY_PRESSED:
					if (ke.getKeyCode() == KeyEvent.VK_BACK_QUOTE) {
						toggle();
						Input.getInputs()[GLFW.GLFW_KEY_GRAVE_ACCENT] = 0;
					}
					break;
				}
				return false;
			}
		});

		// Handles resizing to keep the inpuit field on screen
		/*frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				
				//textAreaScrollPane.setMaximumSize(new Dimension(frame.getWidth(), frame.getHeight()));
				//SwingUtilities.updateComponentTreeUI(frame);
			}
		});*/
	}

	public void update() {
		// Literally just to get the tilde to cooperate
		if (Input.getInputs()[GLFW.GLFW_KEY_GRAVE_ACCENT] == 1) {
			toggle();
			Input.getInputs()[GLFW.GLFW_KEY_GRAVE_ACCENT] = 2;
		}

		// Hog the focus away from GLFW, since I can't get it back otherwise :(
		if (isVisible()) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					if (!textArea.isFocusOwner())
						input.requestFocus();
				}
			});
		}
	}

	// Flips the console on/off
	public void toggle() {
		boolean visible = !frame.isVisible();
		frame.setVisible(visible);
		isVisible = visible;
		
		if (!isVisible && Window.shouldClose()) {
			close();
		}
	}

	public static void print(String text) {
		print(text, Color.WHITE);
	}

	public static void print(String text, Color color) {
		textArea.append(text, color);
	}

	public static void println(String text) {
		println(text, Color.WHITE);
	}

	public static void println(String text, Color color) {
		textArea.append(text + '\n', color);
	}

	public static void saveLogs() {
		BufferedWriter fileOut;
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm");
			String dateStr = dateFormat.format(cal.getTime());
			String filename = "log_" + dateStr + ".txt";

			fileOut = new BufferedWriter(new FileWriter(filename));
			textArea.write(fileOut);
			fileOut.close();

			println("Logs saved to '" + filename + "'");
		} catch (Exception e) {
			println("Failed to save logs");
		}
	}
	
	public static boolean isVisible() {
		return isVisible;
	}

	public void close() {
		frame.dispose();
		frame = null;
		textArea = null;
		input = null;
		isVisible = false;
	}
}
