package com.webssa.guestbest.rest.jsonadapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.webssa.guestbest.documents.model.DocStyleTextWeight;

import java.io.IOException;

public class DocStyleTextWeightMoshiJsonAdapter extends JsonAdapter<DocStyleTextWeight> {

    public static final JsonAdapter.Factory FACTORY = (type, ___, __) -> {
        return (type == DocStyleTextWeight.class) ? new DocStyleTextWeightMoshiJsonAdapter() : null;
    };


    @Override
    public DocStyleTextWeight fromJson(JsonReader reader) throws IOException {
        return DocStyleTextWeight.fromJson(reader.nextString());
    }

    @Override
    public void toJson(JsonWriter writer, DocStyleTextWeight value) throws IOException {
        if (value == null) {
            writer.nullValue();
        } else {
            writer.value(value.getJson());
        }
    }
}