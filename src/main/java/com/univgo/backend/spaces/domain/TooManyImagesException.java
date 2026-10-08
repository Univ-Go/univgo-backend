package com.univgo.backend.spaces.domain;

public class TooManyImagesException extends RuntimeException {

    public TooManyImagesException(int attempted, int allowed) {
        super("A space may hold at most " + allowed + " photographs; this would make " + attempted);
    }
}
