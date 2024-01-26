package engine.dev.console;

import static engine.dev.console.CommandArgumentType.BOOL;
import static engine.dev.console.CommandArgumentType.INT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import engine.Window;
import engine.dev.DevFlags;
import engine.dev.DevTools;
import engine.dev.Log;
import engine.utils.NotImplementedException;
import engine.utils.math.Trie;

public class Command {
	
	private static Trie commands = new Trie();
	private static Map<String, CommandExecutor> executors = new HashMap<>();

	public static void initCommands() {
		add("QUIT", "", Window::close, false);
		add("EXIT", "", Window::close, false);
		add("CHEATS", "<0,1>", (x) -> DevFlags.setFlag("cheats", (boolean)x), BOOL, false);
		add("NOCLIP", "<0,1>", (x) -> DevTools.noclip((boolean)x), BOOL, true);
		add("WIREFRAME", "<0,1>", (x) -> DevFlags.setFlag("wireframe", (boolean)x), BOOL, true);
		add("CULL_FACE", "<0,1>", (x) -> DevFlags.setFlag("cullFace", (boolean)x), BOOL, true);
		add("LOCK_TERRAIN_GEN", "<0,1>", (x) -> DevFlags.setFlag("lockTerrainGeneration", (boolean)x), BOOL, true);
		add("SAVE_LOGS", "", DeveloperConsole::saveLogs, false);
		add("FPS", "<X>", (x) -> Window.setTargetFramerate((int) x), INT, false);
		add("SHOW_BIOME_BORDERS", "<0,1>", (x) -> DevFlags.setFlag("showBiomeBorders", (boolean)x), BOOL, true);
		add("SHOW_SIGHTLINE", "<0,1>", (x) -> DevFlags.setFlag("los", (boolean)x), BOOL, true);
	}

	
	private static void add(String commandName, String syntax, Consumer<Object> runnable, CommandArgumentType type, boolean requireCheats) {
		addExecutor(commandName, new CommandExecutor(syntax, runnable, type, requireCheats));
	}

	private static void add(String commandName, String syntax, Runnable runnable, boolean isSafe) {
		addExecutor(commandName, new CommandExecutor(syntax, runnable, isSafe));
	}
	
	private static void addExecutor(String commandName, CommandExecutor commandExecutor) {
		if (executors.containsKey(commandName))
			throw new RuntimeException("Failed to init commands, collision of the command name \"" + commandName + "\"");
		
		commands.insert(commandName);
		executors.put(commandName, commandExecutor);
	}

	public static void processCommand(String commandLineInput) {
		if (commandLineInput.length() == 0)
			return;
		
		String head = null;
		
		List<String> arguments = new ArrayList<>();
		String reader = "";
		
		boolean bracketed = false;

		for(int i = 0; i < commandLineInput.length(); i++) {
			char c = commandLineInput.charAt(i);
			
			if (!bracketed)
				c = Character.toUpperCase(c);

			if (c == ' ') {
				if (head == null)
					head = reader;
				else
					arguments.add(reader);
				
				reader = "";
				continue;
			} else if (c == '\"' || c == '\'')
				bracketed = !bracketed;
			else
				reader += c;
		}
		
		if (head == null)
			head = reader;
		else
			arguments.add(reader);
		
		CommandExecutor command = executors.get(head);

		if (command == null) {
			Log.info("No such command: " + head);
			return;
		}
		
		if (command.requireCheats() && !DevFlags.cheats) {
			Log.info("Command requires cheats to be enabled.");
			return;
		}
		
		Log.info("]" + commandLineInput);
		
		try {
			command.process(arguments);

		} catch(IllegalArgumentException e) {
			Log.info("Usage: " + head + " " + command.getSyntax());
		} catch(NotImplementedException e) {
			Log.info("Command has not propelrly been implemented: " + head);
		} catch(Exception e) {
			Log.info("Command failed: " + head);
			e.printStackTrace();
		}
	}


	public static List<String> getSuggestion(String text) {
		List<String> suggestedCmds = commands.getSuggestions(text.toUpperCase());
		
		for(int i = 0; i < suggestedCmds.size(); ++i) {
			String head = suggestedCmds.get(i);
			CommandExecutor cmdExec = executors.get(head);
			
			final String syntaxSeperator = cmdExec.getSyntax().equals("") ? "" : " ";
			suggestedCmds.set(i, head + syntaxSeperator + cmdExec.getSyntax());
		}
		
		return suggestedCmds;
	}
}
