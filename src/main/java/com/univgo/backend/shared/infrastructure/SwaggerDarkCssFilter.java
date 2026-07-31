package com.univgo.backend.shared.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * springdoc-openapi no expone una propiedad para inyectar CSS custom en
 * swagger-ui (esa API existe en swagger-ui-express de Node, no acá). Se
 * intercepta el HTML servido y se mete el <link> a mano antes de </head>.
 */
@Component
public class SwaggerDarkCssFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String uri = request.getRequestURI();

        if (!uri.contains("swagger-ui") || !uri.endsWith(".html")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrapper);

        String html = new String(wrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
        String withDarkCss = html.replace(
                "</head>",
                "<link rel=\"stylesheet\" href=\"/swagger-dark.css\"></head>");

        byte[] modified = withDarkCss.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(modified.length);
        response.getOutputStream().write(modified);
    }
}
