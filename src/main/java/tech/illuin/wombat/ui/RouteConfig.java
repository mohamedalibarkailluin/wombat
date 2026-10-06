package tech.illuin.wombat.ui;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The dashboard at {@code /} is the app's only page, so any other page request lands there instead of on a
 * 404 — including the former {@code /ui} address. The handler is registered last (highest route order), so it
 * only ever sees requests that nothing else claimed: REST resources, static assets and {@code /q/*} all match
 * earlier and never reach it.
 * <p>
 * Only GET/HEAD is redirected; anything else falls through to the standard 404, and so does a request carrying
 * the {@code code}/{@code state} pair of an OIDC callback — bouncing that to {@code /} would turn a login into
 * a redirect loop.
 */
@ApplicationScoped
public class RouteConfig
{
    private static final String DASHBOARD_PATH = "/";

    private static final Logger logger = LoggerFactory.getLogger(RouteConfig.class);

    public void registerDashboardFallback(@Observes Router router)
    {
        router.route().order(Integer.MAX_VALUE).handler(context -> {
            HttpMethod method = context.request().method();
            boolean readOnly = HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method);
            boolean oidcCallback = context.request().getParam("code") != null && context.request().getParam("state") != null;

            if (!readOnly || oidcCallback)
            {
                context.next();
                return;
            }

            // Carry the query string over so an old /ui?from=…&to=… bookmark keeps its filters.
            String query = context.request().query();
            String target = query == null || query.isBlank() ? DASHBOARD_PATH : DASHBOARD_PATH + "?" + query;

            logger.debug("No route for {}, redirecting to {}", context.request().uri(), target);
            context.response()
                .setStatusCode(302)
                .putHeader(HttpHeaders.LOCATION, target)
                .end();
        });
    }
}
