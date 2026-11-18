package org.delcom.ifs25050springbootcashflow.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

@Configuration
public class PerformanceOptimizationConfig implements VaadinServiceInitListener {

    private static final String LONG_CACHE_CONTROL = "public, max-age=31536000, immutable";

    private static final byte[] FALLBACK_FAVICON_ICO = Base64.getDecoder().decode(
            "AAABAAEAAQEAAAEAIAAwAAAAFgAAACgAAAABAAAAAgAAAAEAIAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=="
    );

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(new IndexHtmlRequestListener() {
            @Override
            public void modifyIndexHtmlResponse(IndexHtmlResponse response) {
                Document document = response.getDocument();
                Element head = document.head();

                boolean hasFontPreload = !head.select("link[rel=preload][href*=InstrumentSans.woff2]").isEmpty();
                if (!hasFontPreload) {
                    head.appendElement("link")
                            .attr("rel", "preload")
                            .attr("href", "aura/fonts/InstrumentSans/InstrumentSans.woff2")
                            .attr("as", "font")
                            .attr("type", "font/woff2")
                            .attr("crossorigin", "anonymous");
                }

                boolean hasIcon = !head.select("link[rel~=icon]").isEmpty();
                if (!hasIcon) {
                    head.appendElement("link")
                            .attr("rel", "icon")
                            .attr("type", "image/x-icon")
                            .attr("href", "favicon.ico");
                }
            }
        });
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class StaticAssetCacheAndFaviconFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String path = (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath))
                    ? uri.substring(contextPath.length())
                    : uri;

            if ("/favicon.ico".equals(path) && !hasPhysicalFavicon()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("image/x-icon");
                response.setHeader("Cache-Control", "public, max-age=86400");
                response.setContentLength(FALLBACK_FAVICON_ICO.length);
                try (OutputStream out = response.getOutputStream()) {
                    out.write(FALLBACK_FAVICON_ICO);
                }
                return;
            }

            if (isCacheableStaticAsset(path)) {
                response.setHeader("Cache-Control", LONG_CACHE_CONTROL);
                HttpServletResponse protectedResponse = new ImmutableCacheResponseWrapper(response);
                filterChain.doFilter(request, protectedResponse);
                return;
            }

            filterChain.doFilter(request, response);
        }

        private boolean hasPhysicalFavicon() {
            return getClass().getResource("/META-INF/resources/favicon.ico") != null
                    || getClass().getResource("/static/favicon.ico") != null
                    || getClass().getResource("/public/favicon.ico") != null;
        }

        private boolean isCacheableStaticAsset(String path) {
            return path.startsWith("/VAADIN/build/")
                    || path.startsWith("/VAADIN/static/")
                    || path.startsWith("/aura/")
                    || path.endsWith(".woff2")
                    || path.endsWith(".woff");
        }
    }

    private static class ImmutableCacheResponseWrapper extends HttpServletResponseWrapper {

        public ImmutableCacheResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            if (isBlockedCacheHeader(name)) {
                return;
            }
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if (isBlockedCacheHeader(name)) {
                return;
            }
            super.addHeader(name, value);
        }

        @Override
        public void setDateHeader(String name, long date) {
            if ("Expires".equalsIgnoreCase(name)) {
                return;
            }
            super.setDateHeader(name, date);
        }

        private boolean isBlockedCacheHeader(String name) {
            return "Cache-Control".equalsIgnoreCase(name)
                    || "Pragma".equalsIgnoreCase(name)
                    || "Expires".equalsIgnoreCase(name);
        }
    }
}