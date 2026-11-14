package org.delcom.ifs25050springbootcashflow.framework.auth;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SeoAndCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // 1. Tangani /robots.txt secara langsung sebelum disentuh VaadinServlet
        if ("/robots.txt".equals(uri)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("text/plain;charset=UTF-8");
            res.getWriter().write("User-agent: *\nAllow: /\n");
            res.getWriter().flush();
            return;
        }

        // 2. Tambahkan Cache-Control jangka panjang untuk aset statis Vaadin & Aura
        if (uri != null && (uri.startsWith("/VAADIN/") || uri.startsWith("/aura/") || uri.endsWith(".woff2") || uri.endsWith(".js") || uri.endsWith(".css"))) {
            res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        }

        chain.doFilter(request, response);
    }
}