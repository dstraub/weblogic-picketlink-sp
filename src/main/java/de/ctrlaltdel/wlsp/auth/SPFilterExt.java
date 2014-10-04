package de.ctrlaltdel.wlsp.auth;

import de.ctrlaltdel.wlsp.auth.SAML2AuthContext.Type;

import org.picketlink.identity.federation.web.filters.SPFilter;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import weblogic.servlet.security.ServletAuthentication;

/**
 * SPFilterExt
 */
public class SPFilterExt extends SPFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(new FilterConfigWrapper(filterConfig)); // todo add idp url
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        String contextPath = servletRequest.getServletContext().getContextPath();
        if (contextPath.contains("console")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        super.doFilter(servletRequest, servletResponse, new FilterChainWrapper(filterChain));
    }

    /**
     * FilterChainWrapper
     */
    private static class FilterChainWrapper implements FilterChain {

        private final FilterChain filterChain;

        private FilterChainWrapper(FilterChain filterChain) {
            this.filterChain = filterChain;
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
            Subject subject = SAML2AuthContext.get(Type.SUBJECT);
            if (subject != null) {
                try {
                    ServletAuthentication.runAs(subject, (HttpServletRequest) servletRequest);
                    String destination = SAML2AuthContext.get(Type.DESTINATION);
                    ((HttpServletResponse) servletResponse).sendRedirect(destination);
                    return;
                } finally {
                    SAML2AuthContext.clear();
                }
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}
