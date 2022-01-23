package com.play.authentication;

import com.play.com.play.authentication.TokenHandler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebFilter( "/*" )
public class JwtAuthenticationFilter implements Filter {

    private static final String AUTH_COOKIE_KEY = "authToken";
    private static final int STATUS_CODE_UNAUTHORIZED = 401;

    private static final List<String> excludedUrls = new ArrayList<>();
    private static final List<String> excludedExtensions = new ArrayList();

    static {
        excludedUrls.add("/register");
        excludedUrls.add("/login");
        excludedUrls.add("/index.html");
        excludedUrls.add("/registration.html");

        excludedExtensions.add(".js");
        excludedExtensions.add(".css");
    }

    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {

    }

    private boolean isExtensionExcluded(String path) {
        for (String ext : excludedExtensions) {
            if (StringUtils.endsWith(path, ext)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void doFilter(  ServletRequest servletRequest,
                           ServletResponse servletResponse,
                           FilterChain filterChain ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String servletPath = httpRequest.getServletPath();
        String tokenStatus = "";

        if (!excludedUrls.contains(servletPath) && !isExtensionExcluded(servletPath)) {
            try {
                Cookie cookie;
                Cookie[] cookies = httpRequest.getCookies();

                String jwt = "";
                for (int i = 0; i < cookies.length; i++) {
                    cookie = cookies[i];
                    if (StringUtils.equals(cookie.getName(), AUTH_COOKIE_KEY)) {
                        jwt = cookie.getValue();
                        break;
                    }
                }

                tokenStatus = TokenHandler.verifyToken(jwt);

                if (StringUtils.equals(tokenStatus, TokenHandler.getValidStatus())) {
                    Cookie authCookie = new Cookie("authToken", TokenHandler.refreshToken(jwt));
                    authCookie.setMaxAge((int) TokenHandler.SESSION_TIMEOUT);
                    authCookie.setSecure(true);
                    httpResponse.addCookie(authCookie);
                    filterChain.doFilter(httpRequest, httpResponse);
                } else {
                    unauthorized(httpResponse, tokenStatus);
                }
            } catch (final Exception e) {
                unauthorized(httpResponse, tokenStatus);
            }

        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private void unauthorized(HttpServletResponse response, String tokenStatus) throws IOException {
        response.sendError(STATUS_CODE_UNAUTHORIZED, "Auth Status: " + tokenStatus);
    }

    @Override
    public void destroy() {

    }
}
