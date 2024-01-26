package engine.dev.console;

import java.awt.Color;

import engine.dev.ILogListener;
import engine.dev.Level;

public class DebugLogListener implements ILogListener {

	// The colors of the output
	private final Color[] colors = new Color[] {Color.WHITE, Color.WHITE, Color.WHITE, Color.YELLOW, Color.RED, Color.RED, Color.WHITE};


	@Override
	public void log(Level level, String text) {
		DeveloperConsole.print(text, colors[level.ordinal() - 1]);
	}
}
