package com.webssa.guestbest.rest.jsonadapter;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;

public class UriMoshiJsonAdapter extends JsonAdapter<Uri> {

    public static final JsonAdapter.Factory FACTORY = (type, ___, __) -> (type == Uri.class) ? new UriMoshiJsonAdapter() : null;



    @Override
    public Uri fromJson(JsonReader reader) throws IOException {
        Object value = reader.readJsonValue();
        return (value instanceof String) ? Uri.parse((String) value) : null;
    }

    @Override
    public void toJson(JsonWriter writer, Uri value) throws IOException {
        writer.value(value == null ? null : value.toString());
    }

    @NonNull
    @Override
    public String toString() {
        return "JsonAdapter(Uri)";
    }
}
