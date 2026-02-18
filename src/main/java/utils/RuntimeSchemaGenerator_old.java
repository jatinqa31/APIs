package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class RuntimeSchemaGenerator_old {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static JsonNode generateSchema(String jsonResponse) throws Exception {
        JsonNode responseNode = mapper.readTree(jsonResponse);
        return buildSchema(responseNode);
    }

    private static JsonNode buildSchema(JsonNode node) {
        ObjectNode schema = mapper.createObjectNode();

        String type = getType(node);
        schema.put("type", type);

        if (node.isObject()) {
            ObjectNode properties = mapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            Set<String> required = new HashSet<>();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode fieldNode = entry.getValue();

                properties.set(fieldName, buildSchema(fieldNode));

                if (!fieldNode.isNull() && !fieldNode.isMissingNode()) {
                    required.add(fieldName);
                }
            }

            schema.set("properties", properties);

            if (!required.isEmpty()) {
                var requiredArray = mapper.createArrayNode();
                required.forEach(requiredArray::add);
                schema.set("required", requiredArray);
            }
        }

        if (node.isArray()) {
            schema = handleArray(node, schema);
        }

        addValidationRules(schema, node);
        return schema;
    }

    private static ObjectNode handleArray(JsonNode arrayNode, ObjectNode schema) {
        // Empty array – cannot infer items type, so keep generic
        if (arrayNode.size() == 0) {
            schema.put("type", "array");
            schema.set("items", mapper.createObjectNode().put("type", "unknown"));
            schema.put("minItems", 0);
            schema.put("maxItems", 0);
            schema.put("uniqueItems", false);
            return schema;
        }

        Set<String> types = new HashSet<>();
        List<JsonNode> uniqueItems = new ArrayList<>();
        Set<JsonNode> seen = new HashSet<>();

        for (JsonNode item : arrayNode) {
            JsonNode copy = item.deepCopy();
            if (seen.add(copy)) { // Track unique structures
                uniqueItems.add(item);
                types.add(getType(item));
            }
        }

        if (uniqueItems.size() == 1) {
            // Homogeneous array
            schema.set("items", buildSchema(uniqueItems.get(0)));
        } else {
            // Heterogeneous array - union types
            var anyOf = mapper.createArrayNode();
            for (JsonNode item : uniqueItems) {
                anyOf.add(buildSchema(item));
            }
            ObjectNode itemsNode = mapper.createObjectNode();
            itemsNode.set("anyOf", anyOf);
            schema.set("items", itemsNode);
        }

        schema.put("minItems", 0);
        schema.put("maxItems", arrayNode.size());
        schema.put("uniqueItems", true);

        // Ensure type is array
        schema.put("type", "array");
        return schema;
    }

    private static void addValidationRules(ObjectNode schema, JsonNode node) {
        JsonNode typeNode = schema.get("type");
        if (typeNode == null || typeNode.isNull()) {
            return;
        }

        String type = typeNode.asText();

        if ("string".equals(type) && node.isTextual()) {
            String value = node.asText();

            if (value.contains("@") && value.contains(".")) {
                schema.put("format", "email");
            } else if (value.matches("\\+?\\d[-.\\s]?\\(?\\d{1,4}\\)?[-.\\s]?\\d{1,4}")) {
                schema.put("format", "phone");
            }

            schema.put("minLength", 1);
            schema.put("maxLength", value.length());
        }

        if ("integer".equals(type) && node.isIntegralNumber()) {
            schema.put("minimum", node.asInt());
            schema.put("maximum", node.asInt());
        }

        if ("number".equals(type) && node.isNumber()) {
            schema.put("minimum", node.asDouble());
            schema.put("maximum", node.asDouble());
        }

        if ("array".equals(type) && node.isArray()) {
            schema.put("uniqueItems", true);
        }
    }

    private static String getType(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            // Never return null here; use a safe default
            return "unknown";
        }
        if (node.isNull()) return "null";
        if (node.isObject()) return "object";
        if (node.isArray()) return "array";
        if (node.isTextual()) return "string";
        if (node.isInt() || node.isLong()) return "integer";
        if (node.isFloatingPointNumber()) return "number";
        if (node.isBoolean()) return "boolean";
        return "unknown";
    }
}
