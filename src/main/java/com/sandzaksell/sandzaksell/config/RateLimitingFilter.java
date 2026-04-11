package com.sandzaksell.sandzaksell.config;

import com.sandzaksell.sandzaksell.services.RateLimitService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String ipAddr = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddr != null && !ipAddr.isEmpty()) {
            ipAddr = ipAddr.split(",")[0].trim();
        } else {
            ipAddr = httpRequest.getRemoteAddr();
        }

        Bucket bucket = rateLimitService.resolveBucket(ipAddr);


        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{ \"error\": \"Previše zahteva. Sačekaj minut pa pokušaj ponovo.\", \"site\": \"sandzaksell.com\" }");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}