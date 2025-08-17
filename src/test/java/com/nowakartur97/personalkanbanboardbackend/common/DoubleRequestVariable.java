package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.Getter;

@Getter
public class DoubleRequestVariable extends RequestVariable {

    private final String name2;
    private final Object value2;

    public DoubleRequestVariable(String name, Object value, String name2, Object value2) {
        super(name, value);
        this.name2 = name2;
        this.value2 = value2;
    }
}
