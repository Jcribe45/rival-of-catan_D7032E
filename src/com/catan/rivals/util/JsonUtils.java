package com.catan.rivals.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Utility class for JSON parsing operations.
 * 
 * SOLID: Single Responsibility - handles only JSON operations
 * Booch: High cohesion - all methods related to JSON
 */
public class JsonUtils {
    
    /**
     * Safely extracts a string from a JSON object.
     * 
     * @param obj The JSON object
     * @param key The key to look up
     * @return The string value, or null if not found
     */
    public static String getString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return null;
        }
        JsonElement element = obj.get(key);
        return (element == null || element.isJsonNull()) ? null : element.getAsString();
    }
    
    /**
     * Safely extracts an integer from a JSON object.
     * 
     * @param obj The JSON object
     * @param key The key to look up
     * @param defaultValue The default value if not found
     * @return The integer value, or defaultValue if not found
     */
    public static int getInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key)) {
            return defaultValue;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Checks if a JSON array contains a string value (case-insensitive).
     * 
     * @param arr The JSON array
     * @param value The value to search for
     * @return True if found, false otherwise
     */
    public static boolean arrayContains(JsonArray arr, String value) {
        if (arr == null || value == null) {
            return false;
        }
        for (JsonElement elem : arr) {
            if (elem.isJsonPrimitive() && elem.getAsString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Parses an integer safely from a string.
     * 
     * @param str The string to parse
     * @param defaultValue The default value if parsing fails
     * @return The parsed integer or default value
     */
    public static int parseIntSafe(String str, int defaultValue) {
        if (str == null || str.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
