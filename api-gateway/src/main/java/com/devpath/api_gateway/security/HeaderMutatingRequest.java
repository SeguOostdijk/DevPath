package com.devpath.api_gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the original request añadiendo headers extra (userId, role)
 * que se propagan a los microservicios downstream.
 */
public class HeaderMutatingRequest extends HttpServletRequestWrapper {

    private final Map<String, String> extraHeaders = new HashMap<>();

    public HeaderMutatingRequest(HttpServletRequest request, String... keyValuePairs) {
        super(request);
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            if (keyValuePairs[i + 1] != null) {
                extraHeaders.put(keyValuePairs[i], keyValuePairs[i + 1]);
            }
        }
    }

    @Override
    public String getHeader(String name) {
        if (extraHeaders.containsKey(name)) return extraHeaders.get(name);
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (extraHeaders.containsKey(name)) {
            return Collections.enumeration(Collections.singletonList(extraHeaders.get(name)));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        var names = Collections.list(super.getHeaderNames());
        names.addAll(extraHeaders.keySet());
        return Collections.enumeration(names);
    }
}
