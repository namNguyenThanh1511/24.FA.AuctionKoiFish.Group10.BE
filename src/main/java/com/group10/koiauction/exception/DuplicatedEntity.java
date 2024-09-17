package com.group10.koiauction.exception;

public class DuplicatedEntity extends RuntimeException {
    public DuplicatedEntity(String message) {
        super(message);
    }
}
