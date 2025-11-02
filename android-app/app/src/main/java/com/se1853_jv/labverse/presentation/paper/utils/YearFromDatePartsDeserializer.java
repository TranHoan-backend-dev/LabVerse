package com.se1853_jv.labverse.presentation.paper.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class YearFromDatePartsDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(@NonNull JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode dateParts = node.get("date-parts");
        if (dateParts != null && dateParts.isArray()
                && !dateParts.isEmpty()
                && dateParts.get(0).isArray()
                && dateParts.get(0).get(0).isInt()) {
            return dateParts.get(0).get(0).asInt();
        }
        return null;
    }
}

