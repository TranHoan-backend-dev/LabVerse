package com.se1853_jv.labverse.presentation.paper.utils;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthorListToStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(@NonNull JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isArray()) {
            List<String> names = new ArrayList<>();
            for (JsonNode author : node) {
                String given = author.path("given").asText("");
                String family = author.path("family").asText("");
                names.add((given + " " + family).trim());
            }
            return String.join(", ", names);
        }
        return node.asText("");
    }
}
