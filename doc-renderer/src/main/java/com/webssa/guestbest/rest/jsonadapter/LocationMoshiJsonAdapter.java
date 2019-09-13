package com.webssa.guestbest.rest.jsonadapter;

import android.location.Location;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;

public class LocationMoshiJsonAdapter extends JsonAdapter<Location> {
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_LATITUDE = "lat";
    private static final String FIELD_LONGITUDE = "lon";
    private static final String FIELD_ALTITUDE = "alt";
    private static final String FIELD_ACCURACY = "accuracy";
    private static final String FIELD_TIME = "timestamp";

    private static final JsonReader.Options NAMES = JsonReader.Options.of(FIELD_PROVIDER, FIELD_LATITUDE, FIELD_LONGITUDE, FIELD_ALTITUDE, FIELD_ACCURACY, FIELD_TIME);

    public static final JsonAdapter.Factory FACTORY = (type, ___, __) -> (type == Location.class) ? new LocationMoshiJsonAdapter() : null;

    @Override
    public Location fromJson(JsonReader reader) throws IOException {
        String provider = null;
        Double lat = null;
        Double lon = null;
        double alt = 0f;
        float acc = 0f;
        long time = 0L;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.selectName(NAMES)) {
                case 0:
                    provider = reader.nextString();
                    break;
                case 1:
                    lat = reader.nextDouble();
                    break;
                case 2:
                    lon = reader.nextDouble();
                    break;
                case 3:
                    alt = reader.nextDouble();
                    break;
                case 4:
                    acc = (float) reader.nextDouble();
                    break;
                case 5:
                    time = reader.nextLong();
                    break;
                default:
                    reader.skipName();
                    reader.skipValue();
            }
        }
        reader.endObject();

        if (lat == null) throw new JsonDataException(String.format("Required field '%s' missing at %s", FIELD_LATITUDE, reader.getPath()));
        else if (lon == null) throw new JsonDataException(String.format("Required field '%s' missing at %s", FIELD_LONGITUDE, reader.getPath()));

        Location l = new Location(provider);
        l.setLatitude(lat);
        l.setLongitude(lon);
        l.setAltitude(alt);
        l.setAccuracy(acc);
        l.setTime(time);
        return l;
    }

    @Override
    public void toJson(JsonWriter writer, Location loc) throws IOException {
        writer.beginObject();

        if (loc.getProvider() != null) {
            writer.name(FIELD_PROVIDER);
            writer.value(loc.getProvider());
        }
        writer.name(FIELD_LATITUDE);
        writer.value(loc.getLatitude());
        writer.name(FIELD_LONGITUDE);
        writer.value(loc.getLongitude());
        if (loc.hasAltitude()) {
            writer.name(FIELD_ALTITUDE);
            writer.value(loc.getAltitude());
        }
        if (loc.hasAccuracy()) {
            writer.name(FIELD_ACCURACY);
            writer.value(loc.getAccuracy());
        }
        if (loc.getTime() > 0) {
            writer.name(FIELD_TIME);
            writer.value(loc.getTime());
        }
        writer.endObject();
    }
}
