package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface AccessTokenResponseGenerator {

    /**
     * Generate.
     *
     * @param request           the request
     * @param response          the response
     * @param registeredService the registered service
     * @param service           the service
     * @param result            the result
     * @param timeout           the timeout
     * @param responseType      the response type
     */
    void generate(HttpServletRequest request,
                  HttpServletResponse response,
                  OAuthRegisteredService registeredService,
                  Service service,
                  OAuth20TokenGeneratedResult result,
                  long timeout,
                  OAuth20ResponseTypes responseType);
}
