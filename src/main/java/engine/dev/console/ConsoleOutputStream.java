package engine.dev.console;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

public class ConsoleOutputStream extends OutputStream {

	private final ConsoleTextPane textPane;
	private final StringBuilder sb = new StringBuilder();

	public ConsoleOutputStream(ConsoleTextPane textPane) {
		this.textPane = textPane;
	}

	@Override
	public void flush() throws IOException {
		super.flush();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public void write(int b) throws IOException {

		if (b == '\r')
			return;

		if (b == '\n') {
			final String text = sb.toString() + "\n";
			textPane.append(text, Color.RED);
			sb.setLength(0);
		} else {
			sb.append((char) b);
		}
	}
}