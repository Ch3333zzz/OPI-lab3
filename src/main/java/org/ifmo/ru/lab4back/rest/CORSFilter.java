package org.ifmo.ru.lab4back.rest;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ifmo.ru.lab4back.util.Messages;

import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class CORSFilter implements Filter {
    private final Set<String> allowed = Set.of(
            Messages.get("link.heliouse"),
            Messages.get("link.home"),
            Messages.get("link.home.another")
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;

        String origin = req.getHeader(Messages.get("filter.origin"));
        if (origin != null && allowed.contains(origin)) {
            resp.setHeader(Messages.get("filter.header.allow.origin"), origin);
            resp.setHeader(Messages.get("filter.header.Vary"), origin);
            resp.setHeader(Messages.get("filter.header.allow.creditals"), Messages.get("filter.true"));
        }
        resp.setHeader(Messages.get("filter.head.allow.method"), Messages.get("filter.header.allow.methods"));
        resp.setHeader(Messages.get("filter.header.allow.header"),  Messages.get("filter.header.allow.headers"));
        resp.setHeader(Messages.get("filter.header.allow.max.age"), Messages.get("filer.header.age"));

        if (Messages.get("filter.header.options").equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        chain.doFilter(request, response);
    }
}
