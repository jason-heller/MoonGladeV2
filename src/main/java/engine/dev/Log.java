package engine.dev;

import static engine.dev.Level.ALL;
import static engine.dev.Level.DEBUG;
import static engine.dev.Level.ERROR;
import static engine.dev.Level.FATAL;
import static engine.dev.Level.INFO;
import static engine.dev.Level.TRACE;
import static engine.dev.Level.WARN;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;


public enum Log {

	instance;
	
	private List<ILogListener> listeners = new LinkedList<>();
	private static Level level = ALL;
	
	public static void setLevel(Level l) {
		level = l;
	}

	public void addListener(ILogListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	private void log(Level l, String header, Object... data) {
		synchronized (listeners) {
			StringBuilder resultBuilder = new StringBuilder();
			
			for(Object obj : data)
				resultBuilder.append(obj.toString()).append(", ");
			
			resultBuilder.append("\n");
			String str = resultBuilder.toString();
			
			for (ILogListener listener : listeners)
				listener.log(l, str);
		}
	}

	// Wrappers
	
	public static void debug(Object... data) {
		if (level.ordinal() >= DEBUG.ordinal())
			Log.instance.log(DEBUG, "[Debug] ", data);
	}
	
	public static void info(Object... data) {
		if (level.ordinal() >= INFO.ordinal())
			Log.instance.log(INFO, "", data);
	}

	public static void warn(Object... data) {
		if (level.ordinal() >= WARN.ordinal())
			Log.instance.log(WARN, "[Warning] ", data);
	}
	
	public static void error(Object... data) {
		if (level.ordinal() >= ERROR.ordinal())
			Log.instance.log(ERROR, "[Error] ", data);
	}
	
	public static void fatal(Object... data) {
		if (level.ordinal() >= FATAL.ordinal())
			Log.instance.log(FATAL, "[Fatal] ", data);
	}
	
	public static void trace(Object... data) {
		if (level.ordinal() >= TRACE.ordinal())
			Log.instance.log(TRACE, "[Trace] ", data);
	}

	public static void stackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Log.instance.log(FATAL, sw.toString());
	}

	public static String tuple(int... data) {
		StringBuilder s = new StringBuilder().append("(");

		for (int i = 0; i < data.length - 1; ++i)
			s.append(data[i]).append(", ");

		s.append(data[data.length - 1]);
		return s.append(")").toString();
	}
	
	public static String tuple(float... data) {
		StringBuilder s = new StringBuilder().append("(");

		for (int i = 0; i < data.length - 1; ++i)
			s.append(data[i]).append(", ");

		s.append(data[data.length - 1]);
		return s.append(")").toString();
	}
}