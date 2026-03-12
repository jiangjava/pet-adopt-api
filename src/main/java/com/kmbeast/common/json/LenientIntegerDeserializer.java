package com.kmbeast.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 宽松的 Integer 反序列化器，用于处理前端可能发送的 "undefined"、"null" 等非法字符串。
 * 当遇到这些值时返回 null，避免 Jackson 反序列化异常。
 */
public class LenientIntegerDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isEmpty()
                || "undefined".equalsIgnoreCase(value)
                || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
