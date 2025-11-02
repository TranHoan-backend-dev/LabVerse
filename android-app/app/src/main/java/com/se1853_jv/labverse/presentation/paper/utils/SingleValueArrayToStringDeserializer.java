package com.se1853_jv.labverse.presentation.paper.utils;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class SingleValueArrayToStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(@NonNull JsonParser p, DeserializationContext ctxt) throws IOException, IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isArray() && !node.isEmpty()) {
            return node.get(0).asText();
        }
        return node.asText("");
    }
}

