package com.webssa.guestbest.rest.jsonadapter;

import android.os.Build;

import androidx.annotation.NonNull;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.webssa.guestbest.GlobalProperties;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateMoshiJsonAdapter extends JsonAdapter<Date> {

    public static final JsonAdapter.Factory FACTORY = (type, ___, __) -> (type == Date.class) ? new DateMoshiJsonAdapter() : null;

    private final DateFormat primaryFormat;

    public DateMoshiJsonAdapter() {
        this.primaryFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", GlobalProperties.FORMATTER_LOCALE);
    }

    @Override
    public Date fromJson(JsonReader reader) throws IOException {
        Object value = reader.readJsonValue();
        return (value instanceof String) ? deserialize((String) value) : null;
    }

    public Date deserialize(String txtDate) throws JsonDataException {
        if (Build.VERSION.SDK_INT < 24) {
            txtDate = conv8601TimezoneTo822(txtDate);
        }


        try{
            return primaryFormat.parse(txtDate);
        } catch (ParseException e){
            throw new JsonDataException("error parse Date - "+txtDate, e);
        }
    }

    private String conv8601TimezoneTo822(String dat) {
        int signIndex = dat.lastIndexOf('+');
        if (signIndex < 0) signIndex = dat.lastIndexOf('-');
        if (signIndex > 0) {
            int colonIndex = dat.indexOf(':', signIndex);
            if (colonIndex > signIndex) {
                return dat.substring(0, colonIndex) + dat.substring(colonIndex + 1);
            }
        }
        return dat;
    }



    @Override
    public void toJson(JsonWriter writer, Date value) throws IOException {
        writer.value(value == null ? null : primaryFormat.format(value));
    }

    @NonNull
    @Override
    public String toString() {
        return "JsonAdapter(Date)";
    }
}