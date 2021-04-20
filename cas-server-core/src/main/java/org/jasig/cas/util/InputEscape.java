package org.jasig.cas.util;

import java.util.regex.Pattern;

/**
 * User Input Escape
 */
public final class InputEscape {

	private static final Pattern ESC_HTML = Pattern.compile("[<>(){}]");

	private InputEscape() {

	}

	/**
	 * @param input
	 *            User Input
	 * @return HTML escaped input
	 */
	public static String escape(String input) {
		if (input == null) {
			return null;
		}
		// #input kann NICHT getrimmed werden - siehe: ZAD-205. Damit würde die Kerberos Anmeldung nicht funktionieren,
		// da diese im Login-Form ein Leerzeichen angibt.
		return ESC_HTML.matcher(input).replaceAll("");
	}
}
