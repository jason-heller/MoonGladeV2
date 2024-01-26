package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ColorTest {
	@Test
	public void testProjectionCreation() {
		String str = "Hello";

		int i = 0;
		String out = str.substring(i++, ++i);

		assertEquals(out.equals("He"), out);
	}
}
