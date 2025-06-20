package org.example.smartmuseum.model.enums;

public enum NodeType {
    MENU("menu"),
    QUESTION("question"),
    RESPONSE("response"),
    INPUT("input");

    private final String value;

    NodeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static NodeType fromString(String value) {
        for (NodeType type : NodeType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown node type: " + value);
    }
}