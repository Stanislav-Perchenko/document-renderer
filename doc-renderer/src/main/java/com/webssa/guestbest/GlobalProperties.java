package com.webssa.guestbest;

import java.util.Locale;

public final class GlobalProperties {

    private GlobalProperties() { }


    /**
     * This is the Locale value using for formatting any String value within the application
     */
    public static final Locale FORMATTER_LOCALE = Locale.UK;


    /********************  HTTP client-related definitions  ***************************************/
    public static final boolean HTTP_UPSTREAM_USE_GZIP = true;
    public static final String HTTP_USER_AGENT_NAME = "GuestBest/1.0";

    /*************************  Picasso disc caching parameters  **********************************/
    public static final String PICASSO_CACHE_DIR_NAME = "my_picasso_cache";
    public static final float IMAGE_DISC_CACHE_ALLOWED_PART = 0.08f;
    public static final int MIN_IMAGE_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final int MAX_IMAGE_DISK_CACHE_SIZE = 400 * 1024 * 1024; // 160MB
    public static final float MAX_IMAGE_MEM_CACHE_PART = 0.25f;
}
