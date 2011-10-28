package org.freeciv.packetgen;

public class Field {
    private String variableName;
    private String type;
    private String javatype;

    public Field(String variableName, String type, String javatype) {
        this.variableName = variableName;
        this.type = type;
        this.javatype = javatype;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getType() {
        return type;
    }

    public String getJType() {
        return javatype;
    }
}
