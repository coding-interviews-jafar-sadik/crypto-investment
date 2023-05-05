package com.xm.crypto.exceptions;

import lombok.Getter;

@Getter
public class UnknownSymbolRuntimeException extends RuntimeException {

    private final String unknownSymbol;

    public UnknownSymbolRuntimeException(String unknownSymbol) {
        this.unknownSymbol = unknownSymbol;
    }
}
