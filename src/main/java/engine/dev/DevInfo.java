package engine.dev;

import engine.gl.text.Text;

public class DevInfo {
	private static StringBuilder sb = new StringBuilder();
	
	public static void append(String header, Object data) {
		sb.append(header);
		sb.append(": ");
		sb.append(data.toString());
		sb.append("<br>");
	}
	
	public static void append(String header, Object data, String color) {
		sb.append("<");
		sb.append(color);
		sb.append(">");
		sb.append(header);
		sb.append(": ");
		sb.append(data.toString());
		sb.append("<br>");
	}
	
	public static void drawInfo() {
		Text.draw(sb.toString(), -1, 1);
		sb.setLength(0);
	}
}
