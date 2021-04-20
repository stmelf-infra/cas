/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.flow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.Message;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to authenticate credential and retrieve a TicketGrantingTicket for those credential. If there is a request for
 * renew, then it also generates the Service Ticket required.
 *
 * @author Scott Battaglia
 * @since 3.0.4
 */
@SuppressWarnings("all")
public class AuthenticationViaFormAction {

	/** Authentication success result. */
	private static final String SUCCESS = "success";

	/**
	 * Authentication succeeded with warnings from authn subsystem that should be displayed to user.
	 */
	private static final String SUCCESS_WITH_WARNINGS = "successWithWarnings";

	/** Authentication success with "warn" enabled. */
	private static final String WARN = "warn";

	/** Authentication failure result. */
	private static final String AUTHENTICATION_FAILURE = "authenticationFailure";

	/** Error result. */
	private static final String ERROR = "error";

	/**
	 * Binder that allows additional binding of form object beyond Spring defaults.
	 */
	private CredentialsBinder credentialsBinder;

	/** Core we delegate to for handling all ticket related tasks. */
	@NotNull
	private CentralAuthenticationService centralAuthenticationService;

	/** Ticket registry used to retrieve tickets by ID. */
	@NotNull
	private TicketRegistry ticketRegistry;

	@NotNull
	private CookieGenerator warnCookieGenerator;

	/** Flag indicating whether message context contains warning messages. */
	private boolean hasWarningMessages;

	/** Logger instance. **/
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public final void doBind(RequestContext context, Credential credential) {
		final HttpServletRequest request = WebUtils.getHttpServletRequest(context);

		if (this.credentialsBinder != null && this.credentialsBinder.supports(credential.getClass())) {
			this.credentialsBinder.bind(request, credential);
		}
	}

	public final Event submit(
			RequestContext context,
			Credential credential,
			MessageContext messageContext) throws Exception {
		// Validate login ticket
		final String authoritativeLoginTicket = WebUtils.getLoginTicketFromFlowScope(context);
		final String providedLoginTicket = WebUtils.getLoginTicketFromRequest(context);
		if (!authoritativeLoginTicket.equals(providedLoginTicket)) {
			logger.warn("Invalid login ticket {}", providedLoginTicket);
			messageContext.addMessage(new MessageBuilder().code("error.invalid.loginticket").build());
			return newEvent(ERROR);
		}

		final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
		final Service service = WebUtils.getService(context);
		if (StringUtils.hasText(context.getRequestParameters().get("renew"))
				&& ticketGrantingTicketId != null
				&& service != null) {

			try {
				final String serviceTicketId = centralAuthenticationService.grantServiceTicket(
						ticketGrantingTicketId,
						service,
						credential);
				WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
				return newEvent(WARN);
			}
			catch (AuthenticationException e) {
				return newEvent(AUTHENTICATION_FAILURE, e);
			}
			catch (TicketCreationException e) {
				logger.warn(
						"Invalid attempt to access service using renew=true with different credential. "
								+ "Ending SSO session.");
				this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
			}
			catch (final TicketException e) {
				return newEvent(ERROR, e);
			}
		}

		try {
			String tgtId = centralAuthenticationService.createTicketGrantingTicket(credential);
			WebUtils.putTicketGrantingTicketInFlowScope(context, tgtId);
			final TicketGrantingTicket tgt = (TicketGrantingTicket) this.ticketRegistry.getTicket(tgtId);
			for (final Map.Entry<String, HandlerResult> entry : tgt.getAuthentication().getSuccesses()
					.entrySet()) {
				for (final Message message : entry.getValue().getWarnings()) {
					addWarningToContext(messageContext, message);
				}
			}
			if (hasWarningMessages) {
				return newEvent(SUCCESS_WITH_WARNINGS);
			}
			return newEvent(SUCCESS);
		}
		catch (final AuthenticationException e) {
			return newEvent(AUTHENTICATION_FAILURE, e);
		}
	}

	private AuthenticationException getAuthenticationExceptionAsCause(TicketException e) {
		return (AuthenticationException) e.getCause();
	}

	private Event newEvent(String id) {
		return new Event(this, id);
	}

	private Event newEvent(String id, final Exception error) {
		return new Event(this, id, new LocalAttributeMap("error", error));
	}

	public final void setCentralAuthenticationService(
			final CentralAuthenticationService centralAuthenticationService) {
		this.centralAuthenticationService = centralAuthenticationService;
	}

	public void setTicketRegistry(TicketRegistry ticketRegistry) {
		this.ticketRegistry = ticketRegistry;
	}

	/**
	 * Set a CredentialsBinder for additional binding of the HttpServletRequest to the Credential instance, beyond our
	 * default binding of the Credential as a Form Object in Spring WebMVC parlance. By the time we invoke this
	 * CredentialsBinder, we have already engaged in default binding such that for each HttpServletRequest parameter, if
	 * there was a JavaBean property of the Credential implementation of the same name, we have set that property to be
	 * the value of the corresponding request parameter. This CredentialsBinder plugin point exists to allow
	 * consideration of things other than HttpServletRequest parameters in populating the Credential (or more
	 * sophisticated consideration of the HttpServletRequest parameters).
	 *
	 * @param credentialsBinder
	 *            the credential binder to set.
	 */
	public final void setCredentialsBinder(CredentialsBinder credentialsBinder) {
		this.credentialsBinder = credentialsBinder;
	}

	public final void setWarnCookieGenerator(CookieGenerator warnCookieGenerator) {
		this.warnCookieGenerator = warnCookieGenerator;
	}

	/**
	 * Adds a warning message to the message context.
	 *
	 * @param context
	 *            Message context.
	 * @param warning
	 *            Warning message.
	 */
	private void addWarningToContext(MessageContext context, Message warning) {
		final MessageBuilder builder = new MessageBuilder().warning().code(warning.getCode())
				.defaultText(warning.getDefaultMessage()).args(warning.getParams());
		context.addMessage(builder.build());
		this.hasWarningMessages = true;
	}
}
