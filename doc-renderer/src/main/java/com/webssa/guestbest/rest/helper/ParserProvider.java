package com.webssa.guestbest.rest.helper;

import android.location.Location;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory;
import com.webssa.guestbest.config.model.ConfigColor;
import com.webssa.guestbest.rest.jsonadapter.ConfigColorMoshiJsonAdapter;
import com.webssa.guestbest.rest.jsonadapter.LocationMoshiJsonAdapter;
import com.webssa.guestbest.rest.jsonadapter.UriMoshiJsonAdapter;
import com.webssa.guestbest.rest.typeadapter.LocationGsonTypeAdapter;
import com.webssa.guestbest.rest.typeadapter.UriTypeHierarchyAdapter;

public final class ParserProvider {


    public static Gson getRestApiGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriTypeHierarchyAdapter())
                .registerTypeAdapter(Location.class, new LocationGsonTypeAdapter())
                .create();
    }

    public static Moshi getRestApiMoshi() {
        Moshi m = new Moshi.Builder()
                .add(UriMoshiJsonAdapter.FACTORY)
                .add(LocationMoshiJsonAdapter.FACTORY)
                .add(ConfigColor.class, new ConfigColorMoshiJsonAdapter())
                .build();

        return m.newBuilder().add(new KotlinJsonAdapterFactory()).build();
    }

    private ParserProvider() { }
}
