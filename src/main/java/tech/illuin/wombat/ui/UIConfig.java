package tech.illuin.wombat.ui;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

@ApplicationScoped
public class UIConfig
{
    /**
     * Named so templates can reach it as {@code {inject:currentUser.…}}. {@code @Dependent} rather than
     * {@code @RequestScoped} because a record is final and so cannot be proxied by a normal scope; each
     * lookup resolves the identity of the request being rendered, which is what the indicator needs.
     */
    @Named("currentUser")
    @Dependent
    public CurrentUser provideCurrentUser(SecurityIdentity identity)
    {
        return CurrentUser.from(identity);
    }
}
