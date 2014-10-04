package de.ctrlaltdel.wlsp.auth;


import de.ctrlaltdel.wlsp.auth.SAML2AuthContext.Type;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.servlet.Filter;

import java.io.IOException;
import java.util.Collections;

import weblogic.management.security.ProviderMBean;
import weblogic.security.service.ContextHandler;
import weblogic.security.spi.AuthenticationProviderV2;
import weblogic.security.spi.IdentityAsserterV2;
import weblogic.security.spi.IdentityAssertionException;
import weblogic.security.spi.PrincipalValidator;
import weblogic.security.spi.SecurityServices;
import weblogic.security.spi.ServletAuthenticationFilter;

/**
 * PicketlinkSPAuthenticator
 */
public class PicketlinkSPAuthenticator implements AuthenticationProviderV2, IdentityAsserterV2, ServletAuthenticationFilter {

    static final String TYPE = "SAML2";

    private String idpUrl;

    @Override
    public Filter[] getServletAuthenticationFilters() {
        return new Filter[]{ new SPFilterExt() };
    }

    // IdentityAsserterV2
    @Override
    public CallbackHandler assertIdentity(String type, Object token, ContextHandler contextHandler) throws IdentityAssertionException {
        if (!(TYPE.equals(type))) {
            throw new IdentityAssertionException();
        }
        if (token == null) {
            throw new IdentityAssertionException();
        }
        return new CallbackHandler() {
            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                if (callbacks == null) {
                    return;
                }
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        String name = SAML2AuthContext.get(Type.NAME);
                        ((NameCallback) callback).setName(name);
                        continue;
                    }
                    if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(new char[0]);
                    }
                }
            }
        };
    }

    // AuthenticationProviderV2

    @Override
    @SuppressWarnings("unchecked")
    public AppConfigurationEntry getLoginModuleConfiguration() {
        return new AppConfigurationEntry(SAML2LoginModule.class.getName(), LoginModuleControlFlag.SUFFICIENT, Collections.EMPTY_MAP);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AppConfigurationEntry getAssertionModuleConfiguration() {
        return new AppConfigurationEntry(SAML2LoginModule.class.getName(), LoginModuleControlFlag.SUFFICIENT, Collections.singletonMap("assertion", "true"));
    }

    @Override
    public PrincipalValidator getPrincipalValidator() {
        return null;
    }

    @Override
    public IdentityAsserterV2 getIdentityAsserter() {
        return this;
    }


    @Override
    public void initialize(ProviderMBean providerMBean, SecurityServices securityServices) {
        if (!(providerMBean instanceof PicketlinkSPAuthenticatorMBean)) {
            return;
        }
        PicketlinkSPAuthenticatorMBean mBean = (PicketlinkSPAuthenticatorMBean) providerMBean;
        idpUrl = mBean.getIDPUrl();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void shutdown() {

    }

}
