package com.nowakartur97.personalkanbanboardbackend.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RequestVariable {
    private final String name;
    private final Object value;
}
