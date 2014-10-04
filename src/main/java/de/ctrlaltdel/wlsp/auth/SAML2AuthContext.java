package de.ctrlaltdel.wlsp.auth;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * SAML2AuthContext
 */
public class SAML2AuthContext {

    static enum Type {
        PRINCIPAL,
        ROLES,
        DESTINATION,
        SUBJECT, NAME
    }

    private static boolean onlyOnce = true;

    private static final ThreadLocal<Map<Type, Object>> THREAD_CONTEXT = new ThreadLocal<Map<Type, Object>>() {
        @Override
        protected Map<Type, Object> initialValue() {
            return new HashMap<>();
        }
    };

    @SuppressWarnings("unchecked")
    static <T> T get(Type type) {
        if (type == Type.NAME) {
            Principal principal = (Principal) THREAD_CONTEXT.get().get(Type.PRINCIPAL);
            return principal == null ? null : (T) principal.getName();
        }
        return (T)  (onlyOnce ? THREAD_CONTEXT.get().remove(type) : THREAD_CONTEXT.get().get(type));
    }

    static void put(Type type, Object value) {
        THREAD_CONTEXT.get().put(type, value);
    }

    static void clear() {
        THREAD_CONTEXT.get().clear();
    }



}
