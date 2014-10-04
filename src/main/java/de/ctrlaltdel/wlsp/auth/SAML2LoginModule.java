package de.ctrlaltdel.wlsp.auth;

import de.ctrlaltdel.wlsp.auth.SAML2AuthContext.Type;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * IPPLoginModule
 */
public class SAML2LoginModule implements LoginModule {

    private Subject subject;
    private boolean assertionRequested;
    private Principal principal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.assertionRequested = "true".equalsIgnoreCase((String) options.get("assertion"));
    }

    @Override
    public boolean login() throws LoginException {

        if (!assertionRequested) {
            return false;
        }
        principal = SAML2AuthContext.get(Type.PRINCIPAL);
        return principal != null;
    }


    @Override
    public boolean commit() throws LoginException {
        subject.getPrincipals().add(principal);
        List<Principal> roles = SAML2AuthContext.get(Type.ROLES);
        if (roles != null && 0 < roles.size()) {
            subject.getPrincipals().addAll(roles);
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        subject = null;
        principal = null;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        abort();
        return true;
    }


}
