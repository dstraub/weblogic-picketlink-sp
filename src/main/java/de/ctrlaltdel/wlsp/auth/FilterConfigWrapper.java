package de.ctrlaltdel.wlsp.auth;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import java.util.Enumeration;

/**
 * FilterConfigWrapper
 */
public class FilterConfigWrapper implements FilterConfig {

    private final FilterConfig filterConfig;
    private final ServletContext servletContext;

    public FilterConfigWrapper(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        this.servletContext = new ServletContextWrapper(filterConfig.getServletContext());
    }

    @Override
    public String getFilterName() {
        return filterConfig.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {

        if ("ignoreSignatures".equals(name)) {
            return "false";
        }
        return filterConfig.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return filterConfig.getInitParameterNames();
    }

}
