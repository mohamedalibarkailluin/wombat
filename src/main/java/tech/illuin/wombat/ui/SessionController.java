package tech.illuin.wombat.ui;

import io.quarkus.oidc.IdTokenCredential;
import io.quarkus.oidc.OidcSession;
import io.smallrye.common.annotation.Blocking;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Ends the session behind the header's logout link. Google publishes no {@code end_session_endpoint}, so
 * RP-initiated logout ({@code quarkus.oidc.logout.path}) is not an option: this performs the local logout
 * {@link OidcSession#logout()} offers — the session cookie is cleared, the Google account itself stays
 * signed in on the device — and renders the confirmation instead of redirecting to a protected page,
 * which would immediately bounce back through the provider and look like nothing happened.
 * <p>
 * {@code /logout} is granted the {@code permit} policy in {@code application.yaml} so a caller rejected by
 * token validation (right provider, wrong domain) can still drop their session and sign in with another
 * account. Proactive authentication still associates the session with the request on a permitted path, so
 * the logout below has an identity to clear.
 */
@Path("logout")
public class SessionController
{
    private final OidcSession oidcSession;
    private final SecurityIdentity identity;
    private final TemplateRenderer renderer;

    public SessionController(OidcSession oidcSession, SecurityIdentity identity, TemplateRenderer renderer)
    {
        this.oidcSession = oidcSession;
        this.identity = identity;
        this.renderer = renderer;
    }

    @GET
    @Blocking
    @Produces(MediaType.TEXT_HTML)
    public Uni<String> logout()
    {
        // Only a code-flow browser session holds an ID token; anonymous and bearer callers have nothing to clear.
        if (this.identity.getCredential(IdTokenCredential.class) == null)
            return Uni.createFrom().item(this.renderer.loggedOut());

        return this.oidcSession.logout().replaceWith(() -> this.renderer.loggedOut());
    }
}
