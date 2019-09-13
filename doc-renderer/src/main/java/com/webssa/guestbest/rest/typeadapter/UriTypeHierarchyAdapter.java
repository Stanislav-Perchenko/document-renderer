package com.webssa.guestbest.rest.typeadapter;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Serialize/Deserialize Android's {@link Uri} class.
 * You can register this by
 * `{@code registerTypeHierarchyAdapter(Uri.class, new UriTypeHierarchyAdapter())}'.
 */
// https://gist.github.com/ypresto/3607f395ac4ef2921a8de74e9a243629
public class UriTypeHierarchyAdapter implements JsonDeserializer<Uri>, JsonSerializer<Uri> {
    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Uri.parse(json.getAsString());
    }

    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        // Note that Uri is abstract class.
        return new JsonPrimitive(src.toString());
    }
}

