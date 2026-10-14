package tech.illuin.wombat.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tech.illuin.wombat.core.activity.commons.TimeRange;

import java.time.Instant;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import io.pebbletemplates.pebble.PebbleEngine;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRendererTest
{
    private final TemplateRenderer renderer = new TemplateRenderer(new UIConfig().templateEngine(), new ObjectMapper(), null);

    @Test
    void templatesRemainVisibleWhenDependenciesCannotSeeApplicationResources() throws Exception
    {
        URL library = PebbleEngine.class.getProtectionDomain().getCodeSource().getLocation();
        URL application = UIConfig.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader dependencyLoader = new URLClassLoader(new URL[] {library}, UIConfig.class.getClassLoader())
        {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
            {
                if (!name.startsWith("io.pebbletemplates.")) return super.loadClass(name, resolve);
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) loaded = findClass(name);
                if (resolve) resolveClass(loaded);
                return loaded;
            }

            @Override
            public InputStream getResourceAsStream(String name)
            {
                return name.startsWith("templates/") ? null : super.getResourceAsStream(name);
            }
        };
             URLClassLoader applicationLoader = new URLClassLoader(new URL[] {application}, dependencyLoader)
             {
                 @Override
                 protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
                 {
                     if (!name.equals(UIConfig.class.getName()) && !name.startsWith(WombatPebbleExtension.class.getName()))
                         return super.loadClass(name, resolve);
                     Class<?> loaded = findLoadedClass(name);
                     if (loaded == null) loaded = findClass(name);
                     if (resolve) resolveClass(loaded);
                     return loaded;
                 }
             })
        {
            assertNull(dependencyLoader.getResourceAsStream("templates/pages/impact/index.peb"));
            Class<?> configType = applicationLoader.loadClass(UIConfig.class.getName());
            Object engine = configType.getMethod("templateEngine").invoke(configType.getConstructor().newInstance());
            assertNotNull(engine.getClass().getMethod("getTemplate", String.class).invoke(engine, "pages/impact/index"));
        }
    }

    @Test
    void jsonCannotTerminateItsScriptElement() throws Exception
    {
        String label = "</script><img src=x onerror=alert(1)>\"&";
        String json = this.renderer.scriptSafeJson(Map.of("label", label));
        assertFalse(json.contains("<"));
        assertEquals(label, new ObjectMapper().readTree(json).get("label").asText());
    }

    @Test
    void signedOutPageDoesNotShowAUser() throws IOException
    {
        String html = this.renderer.loggedOut();
        assertTrue(html.contains("Signed out"));
        assertFalse(html.contains("header-user"));
        writePreview("logged-out.html", html);
    }

    @Test
    void errorPageUsesSharedDateControlAndLocalAssets() throws IOException
    {
        String html = this.renderer.impactError(
            new TimeRange(Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-10-02T00:00:00Z")),
            new Templates.MaxSpan(86_400_000, "1 day")
        );
        assertTrue(html.contains("2026-10-01T00:00:00Z"));
        assertTrue(html.contains("/vendor/easepick.umd.min.js"));
        assertTrue(html.contains("No CPU usage found"));
        writePreview("error.html", html);
    }

    @Test
    void userNamesAreEscapedAndNotRetainedAcrossRequests()
    {
        Map<String, Object> data = Map.of(
            "timeRange", new TimeRange(Instant.EPOCH, Instant.EPOCH.plusSeconds(86_400)),
            "maxSpan", new Templates.MaxSpan(86_400_000, "1 day")
        );
        String authenticated = this.renderer.render("pages/impact/error", data, new CurrentUser(true, "<script>bad</script>"));
        assertTrue(authenticated.contains("&lt;script&gt;bad&lt;/script&gt;"));
        assertFalse(authenticated.contains("<script>bad</script>"));
        assertFalse(this.renderer.render("pages/impact/error", data, CurrentUser.ANONYMOUS).contains("header-user"));
    }

    private static void writePreview(String name, String html) throws IOException
    {
        String previewDirectory = System.getProperty("test.preview.directory");
        if (previewDirectory != null)
        {
            Path directory = Path.of(previewDirectory);
            Files.createDirectories(directory);
            Files.writeString(directory.resolve(name), html);
        }
    }
}
