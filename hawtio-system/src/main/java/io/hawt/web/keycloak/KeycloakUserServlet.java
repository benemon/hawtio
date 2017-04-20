package io.hawt.web.keycloak;

import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.hawt.system.Authenticator;
import io.hawt.web.AuthenticationConfiguration;
import io.hawt.web.AuthenticationFilter;
import io.hawt.web.LoginServlet;
import io.hawt.web.UserServlet;


public class KeycloakUserServlet extends UserServlet {

    private boolean keycloakEnabled;

    @Override
    public void init() throws ServletException {
        super.init();
        keycloakEnabled = KeycloakServlet.isKeycloakEnabled(config);
    }

    @Override
    protected String getUsername(HttpServletRequest req, HttpServletResponse resp) {
        if (keycloakEnabled) {
            return getKeycloakUsername(req, resp);
        } else {
            return super.getUsername(req, resp);
        }
    }

    /**
     * With Keycloak integration, the Authorization header is available in the request to the UserServlet.
     */
    protected String getKeycloakUsername(final HttpServletRequest req, HttpServletResponse resp) {
        AuthenticationConfiguration configuration = (AuthenticationConfiguration) getServletContext().getAttribute(AuthenticationFilter.AUTHENTICATION_CONFIGURATION);

        class Holder {
            String username = null;
        }
        final Holder usernameHolder = new Holder();

        Authenticator.authenticate(configuration.getRealm(), configuration.getRole(), configuration.getRolePrincipalClasses(),
            configuration.getConfiguration(), req, (subject) -> {
                usernameHolder.username = LoginServlet.getUsernameFromSubject(subject, Arrays.asList(LoginServlet.KNOWN_PRINCIPALS));

                // Start httpSession
                req.getSession(true);
            }
        );

        return usernameHolder.username;

    }


}
