package com.webssa.guestbest.rest.jsonadapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.webssa.guestbest.config.model.ConfigColor;

import java.io.IOException;

public class ConfigColorMoshiJsonAdapter extends JsonAdapter<ConfigColor> {

    public static final JsonAdapter.Factory FACTORY = (type, ___, __) -> {
        return (type == ConfigColor.class) ? new ConfigColorMoshiJsonAdapter() : null;
    };

    @Override
    public ConfigColor fromJson(JsonReader reader) throws IOException {
        String s = reader.nextString();
        try {
            return new ConfigColor(s);
        } catch (RuntimeException e) {
            throw new JsonDataException(String.format("Error parse color '%s' - %s", s, e.getMessage()));
        }
    }

    @Override
    public void toJson(JsonWriter writer, ConfigColor value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            writer.value(value.getTextColor());
        }
    }
}
