package engine.dev.console;

import java.util.List;
import java.util.function.Consumer;

import engine.utils.NotImplementedException;

public class CommandExecutor {
	private Consumer<Object> consumer;
	private Runnable runnable;
	private CommandArgumentType argumentType;
	
	private final String syntax;
	
	private double min = Double.NEGATIVE_INFINITY, max = Double.POSITIVE_INFINITY;
	private boolean requireCheats;

	public CommandExecutor(String syntax, Runnable runnable, boolean isSafe) {
		this.syntax = syntax;
		this.runnable = runnable;
		this.argumentType = CommandArgumentType.NO_ARGS;
	}
	
	public CommandExecutor(String syntax, Consumer<Object> consumer, CommandArgumentType argumentType, boolean requireCheats) {
		this.syntax = syntax;
		this.consumer = consumer;
		this.argumentType = argumentType;
		this.requireCheats = requireCheats;
	}
	
	public void process(List<String> arguments) throws Exception {
		if (arguments.size() == 0) {
			
			if (getArgumentType() != CommandArgumentType.NO_ARGS)
				throw new IllegalArgumentException();
			
			run();
		} else if (arguments.size() == 1) {
			String arg = arguments.get(0);
			
			// Parse arguments
			switch(getArgumentType()) {
			case BOOL:
				boolean b = arg.equals("1") | arg.toLowerCase().equals("true");
				
				if (!b && !(arg.equals("0") | arg.toLowerCase().equals("false")))
					throw new IllegalArgumentException();
				
				run(b);
				break;
				
			case INT:
				int i = Integer.parseInt(arg);
				run(i);
				break;
				
			case FLOAT:
				float f = Float.parseFloat(arg);
				run(f);
				break;
				
			case STRING:
				run(arg);
				break;
				
			default:
				throw new NotImplementedException();
			}	
		} else {
			String[] array = arguments.toArray(new String[0]);
			run(array);
		}
	}

	public CommandArgumentType getArgumentType() {
		return argumentType;
	}

	public void run() throws Exception {
		runnable.run();
	}
	
	public void run(boolean b) throws Exception {
		consumer.accept(b);
	}
	
	public void run(int i) throws Exception {
		if (i < min || i > max)
			throw new Exception();
		consumer.accept(i);
	}
	
	public void run(float f) throws Exception {
		if (f < min || f > max)
			throw new Exception();
		consumer.accept(f);
	}
	
	public void run(String s) throws Exception {
		consumer.accept(s);
	}
	
	public void run(String[] s) throws Exception {
		consumer.accept(s);
	}
	
	public boolean requireCheats() {
		return requireCheats;
	}

	public String getSyntax() {
		return syntax;
	}
}
