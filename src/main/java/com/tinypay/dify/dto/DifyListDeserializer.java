package com.tinypay.dify.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinypay.request.dto.ExecutedServiceDto;
import com.tinypay.request.dto.GeneratedFileDto;

import java.io.IOException;
import java.util.List;

public class DifyListDeserializer {

    public static class RequiredServiceList extends JsonDeserializer<List<RequiredServiceDto>> {
        @Override
        public List<RequiredServiceDto> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String s = p.getText().trim();
                if (s.isEmpty() || s.equals("[]")) return List.of();
                return ((ObjectMapper) p.getCodec()).readValue(s, new TypeReference<>() {});
            }
            return ((ObjectMapper) p.getCodec()).readValue(p, new TypeReference<>() {});
        }
    }

    public static class StringList extends JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String s = p.getText().trim();
                if (s.isEmpty() || s.equals("[]")) return List.of();
                return ((ObjectMapper) p.getCodec()).readValue(s, new TypeReference<>() {});
            }
            return ((ObjectMapper) p.getCodec()).readValue(p, new TypeReference<>() {});
        }
    }

    public static class ExecutedServiceList extends JsonDeserializer<List<ExecutedServiceDto>> {
        @Override
        public List<ExecutedServiceDto> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String s = p.getText().trim();
                if (s.isEmpty() || s.equals("[]")) return List.of();
                return ((ObjectMapper) p.getCodec()).readValue(s, new TypeReference<>() {});
            }
            return ((ObjectMapper) p.getCodec()).readValue(p, new TypeReference<>() {});
        }
    }

    public static class GeneratedFileList extends JsonDeserializer<List<GeneratedFileDto>> {
        @Override
        public List<GeneratedFileDto> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                String s = p.getText().trim();
                if (s.isEmpty() || s.equals("[]")) return List.of();
                return ((ObjectMapper) p.getCodec()).readValue(s, new TypeReference<>() {});
            }
            return ((ObjectMapper) p.getCodec()).readValue(p, new TypeReference<>() {});
        }
    }
}
