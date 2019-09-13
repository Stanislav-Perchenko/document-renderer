package com.webssa.guestbest.rest.typeadapter;

import android.location.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by stanislav.perchenko on 10/21/2016.
 */

public class LocationGsonTypeAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_LATITUDE = "lat";
    private static final String FIELD_LONGITUDE = "lon";
    private static final String FIELD_ALTITUDE = "alt";
    private static final String FIELD_ACCURACY = "accuracy";
    private static final String FIELD_TIME = "timestamp";

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jObj = new JsonObject();
        jObj.addProperty(FIELD_LATITUDE, src.getLatitude());
        jObj.addProperty(FIELD_LONGITUDE, src.getLongitude());
        jObj.addProperty(FIELD_ALTITUDE, src.getAltitude());
        jObj.addProperty(FIELD_ACCURACY, src.getAccuracy());
        jObj.addProperty(FIELD_PROVIDER, src.getProvider());
        jObj.addProperty(FIELD_TIME, src.getTime());
        return jObj;
    }

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jObj = json.getAsJsonObject();
        Location loc = new Location(jObj.has(FIELD_PROVIDER) ? jObj.get(FIELD_PROVIDER).getAsString() : null);
        loc.setLatitude(jObj.get(FIELD_LATITUDE).getAsDouble());
        loc.setLongitude(jObj.get(FIELD_LONGITUDE).getAsDouble());
        loc.setAltitude(jObj.has(FIELD_ALTITUDE) ? jObj.get(FIELD_ALTITUDE).getAsDouble() : 0f);
        loc.setAccuracy(jObj.has(FIELD_ACCURACY) ? jObj.get(FIELD_ACCURACY).getAsFloat() : 0f);
        loc.setTime(jObj.has(FIELD_TIME) ? jObj.get(FIELD_TIME).getAsLong() : 0);
        return loc;
    }
}

