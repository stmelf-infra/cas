package org.jasig.cas.util;

import static junit.framework.TestCase.assertEquals;
import static org.jasig.cas.util.InputEscape.escape;

import org.junit.Test;

public class InputEscapeTests {

	@Test
	public void testEscape() {
		assertEquals("test", escape("test"));
		assertEquals("test", escape("<test>"));
		assertEquals("test", escape("{{{}<>te(s)t}"));
		assertEquals("test", escape("{test}"));
		assertEquals(
				"svg/onlad=alert/Booo/svg/onload=confirm/Boesedas/",
				escape("<svg/onlad=alert(/Booo/)><svg/onload=confirm(/Boesedas/)>"));

	}
}
