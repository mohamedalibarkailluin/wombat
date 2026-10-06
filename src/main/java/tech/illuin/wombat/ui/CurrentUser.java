package tech.illuin.wombat.ui;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.json.JsonString;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Who the templates should say is signed in. Exposed to Qute as the named bean {@code currentUser}
 * (see {@link UIConfig}), so every page inheriting {@code impactLayout.html} can render the indicator
 * through {@code {inject:currentUser.…}} without threading it through each controller.
 * <p>
 * Nothing here is configured: with OIDC disabled no request is ever authenticated, so {@code authenticated}
 * is false and the layout renders no badge at all.
 */
public record CurrentUser(
    boolean authenticated,
    String name
)
{
    /** {@code io.quarkus.oidc.runtime.OidcUtils#USER_INFO_ATTRIBUTE}, inlined to avoid depending on internal OIDC API. */
    private static final String USER_INFO_ATTRIBUTE = "userinfo";
    private static final String EMAIL_CLAIM = "email";

    static final CurrentUser ANONYMOUS = new CurrentUser(false, null);

    static CurrentUser from(SecurityIdentity identity)
    {
        if (identity == null || identity.isAnonymous())
            return ANONYMOUS;

        return new CurrentUser(true, name(identity));
    }

    /**
     * Prefers the email claim, then the UserInfo endpoint — with Google an opaque access token is verified
     * through UserInfo, so the token itself carries nothing — and falls back to the principal name.
     */
    private static String name(SecurityIdentity identity)
    {
        if (identity.getPrincipal() instanceof JsonWebToken token)
        {
            String claim = asString(token.getClaim(EMAIL_CLAIM));
            if (claim != null && !claim.isBlank())
                return claim;
        }

        UserInfo userInfo = identity.getAttribute(USER_INFO_ATTRIBUTE);
        if (userInfo != null && userInfo.getEmail() != null && !userInfo.getEmail().isBlank())
            return userInfo.getEmail();

        return identity.getPrincipal() == null ? null : identity.getPrincipal().getName();
    }

    private static String asString(Object value)
    {
        return switch (value)
        {
            case null -> null;
            case String string -> string;
            case JsonString json -> json.getString();
            default -> value.toString();
        };
    }
}
