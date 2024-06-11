package com.es.plus.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
public enum JavaTypeEnum {
    BYTE("byte"),
    SHORT("short"),
    INT("int"),
    INTEGER("integer"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BIG_DECIMAL("bigdecimal"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    CHAR("char"),
    STRING("string"),
    DATE("date"),
    LOCAL_DATE("localdate"),
    LOCAL_DATE_TIME("localdatetime"),
    LIST("list"),
    SET("set"),
    OBJECT("object");
    @Getter
    private String type;

    public static JavaTypeEnum getByType(String typeName) {
        return Arrays.stream(JavaTypeEnum.values())
                .filter(v -> Objects.equals(v.type, typeName))
                .findFirst()
                .orElse(OBJECT);
    }
}