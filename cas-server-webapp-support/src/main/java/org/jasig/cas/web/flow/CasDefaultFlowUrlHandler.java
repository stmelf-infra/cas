package org.jasig.cas.web.flow;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.webflow.context.servlet.DefaultFlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

/**
 * Provides special handling for parameters in requests made to the CAS login webflow.
 *
 * @author Scott Battaglia
 * @since 3.4
 */
@SuppressWarnings("all")
public final class CasDefaultFlowUrlHandler extends DefaultFlowUrlHandler {

	/**
	 * Default flow execution key parameter name, {@value} . Same as that used by {@link DefaultFlowUrlHandler}.
	 */
	static final String DEFAULT_FLOW_EXECUTION_KEY_PARAMETER = "execution";

	/** Flow execution parameter name. */
	private String flowExecutionKeyParameter = DEFAULT_FLOW_EXECUTION_KEY_PARAMETER;

	/**
	 * Sets the parameter name used to carry flow execution key in request.
	 *
	 * @param parameterName
	 *            Request parameter name.
	 */
	public void setFlowExecutionKeyParameter(String parameterName) {
		this.flowExecutionKeyParameter = parameterName;
	}

	/**
	 * Get the flow execution key.
	 *
	 * @param request
	 *            the current HTTP servlet request.
	 * @return the flow execution key.
	 */
	@Override
	public String getFlowExecutionKey(HttpServletRequest request) {
		return request.getParameter(flowExecutionKeyParameter);
	}

	@Override
	public String createFlowExecutionUrl(
			final String flowId,
			final String flowExecutionKey,
			final HttpServletRequest request) {
		final StringBuilder builder = new StringBuilder();
		builder.append(request.getRequestURI());
		builder.append("?");
		final Map<String, Object> flowParams = new LinkedHashMap<String, Object>(request.getParameterMap());
		flowParams.put(this.flowExecutionKeyParameter, flowExecutionKey);
		appendQueryParameters(builder, flowParams, getEncodingScheme(request));
		return builder.toString();
	}

	@Override
	public String createFlowDefinitionUrl(
			final String flowId,
			@SuppressWarnings("rawtypes") final AttributeMap input,
			final HttpServletRequest request) {
		return request.getRequestURI()
				+ (request.getQueryString() != null ? "?" + request.getQueryString() : "");
	}
}
