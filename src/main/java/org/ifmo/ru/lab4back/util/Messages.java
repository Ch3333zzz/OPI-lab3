package org.ifmo.ru.lab4back.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class Messages {
    private static final String BASE_NAME = "messages";
    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle(BASE_NAME, Locale.getDefault(), new UTF8Control());

    private Messages() {}

    public static String get(String key, Object... args) {
        String pattern = BUNDLE.containsKey(key) ? BUNDLE.getString(key) : "??" + key + "??";
        return args.length == 0 ? pattern : MessageFormat.format(pattern, args);
    }

    private static class UTF8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) return null;
                return new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        }
    }
}
