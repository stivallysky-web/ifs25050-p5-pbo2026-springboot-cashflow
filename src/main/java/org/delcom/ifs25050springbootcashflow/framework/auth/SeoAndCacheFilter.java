package org.delcom.ifs25050springbootcashflow.framework.auth;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SeoAndCacheFilter implements Filter {

    // 1x1 transparent ICO agar request /favicon.ico tidak 404 di Console
    private static final byte[] FAVICON_BYTES = Base64.getDecoder().decode(
            "AAABAAEAAQEAAAEAIAAwAAAAFgAAACgAAAABAAAAAgAAAAEAIAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAA////AAAAAA=="
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        if (uri == null) {
            chain.doFilter(request, response);
            return;
        }

        // 1. Tangani /robots.txt (SEO 100)
        if ("/robots.txt".equals(uri)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("text/plain;charset=UTF-8");
            res.setHeader("Cache-Control", "public, max-age=86400");
            res.getWriter().write("User-agent: *\nAllow: /\n");
            res.getWriter().flush();
            return;
        }

        // 2. Tangani /favicon.ico agar tidak memicu error 404 di Browser Console (Best Practices 100)
        if ("/favicon.ico".equals(uri)) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("image/x-icon");
            res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            res.getOutputStream().write(FAVICON_BYTES);
            res.getOutputStream().flush();
            return;
        }

        // 3.Tangani Source Map (.map) yang diminta Lighthouse agar tidak error 404 di Console
        if (uri.endsWith(".map")) {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json;charset=UTF-8");
            res.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            res.getWriter().write("{\"version\":3,\"sources\":[],\"names\":[],\"mappings\":\"\"}");
            res.getWriter().flush();
            return;
        }

        // 4. Kunci Header Cache-Control untuk semua aset statis Vaadin & Aura
        boolean isStaticAsset = uri.startsWith("/VAADIN/")
                || uri.startsWith("/aura/")
                || uri.endsWith(".js")
                || uri.endsWith(".css")
                || uri.endsWith(".woff2")
                || uri.endsWith(".woff")
                || uri.endsWith(".ttf")
                || uri.endsWith(".png")
                || uri.endsWith(".svg");

        if (isStaticAsset) {
            final String cacheValue = "public, max-age=31536000, immutable";
            res.setHeader("Cache-Control", cacheValue);

            // Gunakan Wrapper agar VaadinServlet tidak bisa menimpa/menghapus Cache-Control kita
            HttpServletResponseWrapper wrappedRes = new HttpServletResponseWrapper(res) {
                @Override
                public void setHeader(String name, String value) {
                    if ("Cache-Control".equalsIgnoreCase(name) || "Pragma".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name)) {
                        return;
                    }
                    super.setHeader(name, value);
                }

                @Override
                public void addHeader(String name, String value) {
                    if ("Cache-Control".equalsIgnoreCase(name) || "Pragma".equalsIgnoreCase(name) || "Expires".equalsIgnoreCase(name)) {
                        return;
                    }
                    super.addHeader(name, value);
                }
            };
            chain.doFilter(request, wrappedRes);
            // Pastikan sekali lagi setelah filter selesai
            res.setHeader("Cache-Control", cacheValue);
            return;
        }

        // 5. Tambahkan Early Preload Hint pada halaman HTML untuk mempercepat LCP & FCP
        if ("GET".equalsIgnoreCase(req.getMethod()) && !uri.startsWith("/?v-r=")) {
            res.setHeader("Link", "</aura/aura.css>; rel=preload; as=style, </aura/fonts/InstrumentSans/InstrumentSans.woff2>; rel=preload; as=font; crossorigin");
        }

        chain.doFilter(request, response);
    }
}