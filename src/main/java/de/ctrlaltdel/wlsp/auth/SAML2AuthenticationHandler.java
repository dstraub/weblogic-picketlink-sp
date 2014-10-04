package de.ctrlaltdel.wlsp.auth;

import de.ctrlaltdel.wlsp.auth.SAML2AuthContext.Type;

import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;
import org.picketlink.identity.federation.web.handlers.saml2.BaseSAML2Handler;

import javax.security.auth.Subject;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import weblogic.security.principal.WLSGroupImpl;
import weblogic.security.principal.WLSUserImpl;
import weblogic.security.services.Authentication;

/**
 * SAML2AuthenticationHandler for weblogic
 */
public class SAML2AuthenticationHandler extends BaseSAML2Handler {


    @Override
    public void handleRequestType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        //
    }

    @Override
    public void handleStatusResponseType(SAML2HandlerRequest request, SAML2HandlerResponse response) throws ProcessingException {
        if (!(request.getSAML2Object() instanceof ResponseType)) {
            return;
        }

        if (getType() == HANDLER_TYPE.IDP) {
            return;
        }

        ResponseType responseType = (ResponseType) request.getSAML2Object();

        StatusType statusType = responseType.getStatus();
        if (statusType == null) {
            response.setError(403, "NULL Status Type from the IDP");
            return;
        }

        String statusValue = statusType.getStatusCode().getValue().toASCIIString();
        if (!JBossSAMLURIConstants.STATUS_SUCCESS.get().equals(statusValue)) {
            response.setError(403, "IDP forbid the user");
            return;
        }

        List<RTChoiceType> assertions = responseType.getAssertions();
        if (assertions.size() == 0) {
            response.setError(403, "IDP forbid the user");
            return;
        }

        AssertionType assertion = assertions.get(0).getAssertion();

        if (isExpired(assertion)) {
            response.setError(403, "Assertion has expired");
            return;
        }


        if (!extractUser(assertion)) {
            response.setError(403, "Assertion contains no user");
            return;
        }

        if (!extractRoles(assertion)) {
            response.setError(403, "Assertion contains no user");
            return;
        }

        SAML2AuthContext.put(Type.DESTINATION, responseType.getDestination());

        try {
            Subject subject = Authentication.assertIdentity(PicketlinkSPAuthenticator.TYPE, Boolean.TRUE);
            SAML2AuthContext.put(Type.SUBJECT, subject);

        } catch (Exception e) {
            response.setError(403, e.getMessage());
        }
    }


    /**
     * isExpired
     *
     * @param assertion
     */
    private boolean isExpired(AssertionType assertion) {
        try {
            return AssertionUtil.hasExpired(assertion);
        } catch (Exception e) {
            //
        }
        return false;
    }

    /**
     * extractUser
     *
     * @param assertion
     */
    private boolean extractUser(AssertionType assertion) {
        try {
            SubjectType subjectType = assertion.getSubject();
            NameIDType nameIDType = (NameIDType) subjectType.getSubType().getBaseID();
            SAML2AuthContext.put(Type.PRINCIPAL, new WLSUserImpl(nameIDType.getValue()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * extractRoles
     *
     * @param assertion
     */
    private boolean extractRoles(AssertionType assertion) {
        List<Principal> roles = new ArrayList<>();
        try {
            for (StatementAbstractType statementType : assertion.getStatements()) {
                if (!(statementType instanceof AttributeStatementType)) {
                    continue;
                }
                AttributeStatementType attributeStatement = (AttributeStatementType) statementType;
                for (ASTChoiceType obj : attributeStatement.getAttributes()) {
                    AttributeType attr = obj.getAttribute();
                    String role = (String) attr.getAttributeValue().get(0);
                    roles.add(new WLSGroupImpl(role));
                }
            }

        } catch (Exception e) {
            return false;
        }
        SAML2AuthContext.put(Type.ROLES, roles);
        return true;
    }
}


