package org.jasig.cas;

import java.util.Optional;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Liefert die Principal ID.
 */
public interface PrincipalIdSwapper {

	/**
	 * @param principal
	 *            der aktuelle Principal mit einem ID
	 * @return neue ID für den gegebenen Principal, falls die ausgetauscht werden sollte.
	 */
	Optional<String> determinePrincipalId(Principal principal);
}
