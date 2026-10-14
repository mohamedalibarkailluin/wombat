package tech.illuin.wombat.ui;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Boots the app with the HTTP permission sets active but the OIDC tenant left off, so authorization can be
 * exercised without a provider: identities come from {@code @TestSecurity}/{@code @OidcSecurity}. In
 * production a single flag drives both — {@code quarkus.oidc.tenant-enabled} is what each permission set's
 * {@code enabled} expression reads.
 * <p>
 * The domain restriction itself is not covered here: {@code quarkus.oidc.token.required-claims.hd} is
 * enforced by Quarkus while validating a signed token, which {@code @TestSecurity} bypasses by constructing
 * the identity directly. Verifying it needs a real provider (a Google login, or a mock OIDC server).
 */
@QuarkusTest
@TestProfile(AuthenticationEnabledTest.AuthenticationEnabledProfile.class)
class AuthenticationEnabledTest
{
    @Test
    void restEndpoint_isRejectedWithoutIdentity()
    {
        given()
            .when().get("/environments")
            .then()
            .statusCode(401);
    }

    @Test
    void dashboard_isRejectedWithoutIdentity()
    {
        given()
            .when().get("/")
            .then()
            .statusCode(401);
    }

    @Test
    void publicPath_staysReachable()
    {
        given()
            .when().get("/q/metrics")
            .then()
            .statusCode(200);
    }

    @Test
    void localUiAssets_stayReachableWithoutIdentity()
    {
        given().when().get("/vendor/petite-vue.es.js").then().statusCode(200);
        given().when().get("/public/js/pages/logged-out.js").then().statusCode(200);
        given().when().get("/icons/docker.svg").then().statusCode(200);
    }

    /** Sign-out must stay reachable without an identity, so a rejected caller is never stuck on a 401/403. */
    @Test
    void logout_staysReachableWithoutIdentity()
    {
        given()
            .when().get("/logout")
            .then()
            .statusCode(200)
            .body(containsString("Signed out"));
    }

    @Test
    @TestSecurity(user = "someone")
    @OidcSecurity(claims = {
        @Claim(key = "email", value = "someone@illuin.tech"),
        @Claim(key = "hd", value = "illuin.tech"),
    })
    void authenticatedCaller_reachesTheApi()
    {
        given()
            .when().get("/environments")
            .then()
            .statusCode(200);
    }

    @Test
    @TestSecurity(user = "someone")
    @OidcSecurity(claims = {
        @Claim(key = "email", value = "someone@illuin.tech"),
        @Claim(key = "hd", value = "illuin.tech"),
    })
    void dashboard_showsSignedInIndicatorAndLogoutLink()
    {
        given()
            .when().get("/")
            .then()
            .statusCode(200)
            .body(containsString("header-user"))
            .body(containsString("someone@illuin.tech"))
            .body(containsString("href=\"/logout\""));
    }

    /** The indicator is authentication-only chrome: nothing is rendered for an unauthenticated render. */
    @Test
    void loggedOutPage_carriesNoIndicator()
    {
        given()
            .when().get("/logout")
            .then()
            .statusCode(200)
            .body(not(containsString("header-user")));
    }

    public static class AuthenticationEnabledProfile implements QuarkusTestProfile
    {
        @Override
        public Map<String, String> getConfigOverrides()
        {
            return Map.of(
                "quarkus.oidc.tenant-enabled", "false",
                "quarkus.http.auth.permission.public.enabled", "true",
                "quarkus.http.auth.permission.authenticated.enabled", "true"
            );
        }
    }
}
