package com.github.NGoedix.watchvideo.util.config;

public class TVConfig {
    public static final int MIN_DISTANCE = 5;
    public static final int MAX_DISTANCE = 20;

    public static final int SYNC_TIME = 1500;

    public static final String URL_PATTERN = "^(https?:\\/\\/)" +          // protocol http o https
            "((([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,})|" + // domain
            "localhost|" +                            // or localhost
            "(\\d{1,3}\\.){3}\\d{1,3})" +             // or IP
            "(\\:\\d+)?(\\/[^\\s]*)?$";               // optional port + path
}
