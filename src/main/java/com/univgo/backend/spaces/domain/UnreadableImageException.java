package com.univgo.backend.spaces.domain;

public class UnreadableImageException extends RuntimeException {

    public UnreadableImageException(String filename, String reason) {
        super("Cannot use '" + filename + "' as a photograph: " + reason);
    }
}
