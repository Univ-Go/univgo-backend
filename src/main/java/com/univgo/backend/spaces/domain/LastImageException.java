package com.univgo.backend.spaces.domain;

/**
 * A published space always has at least one photograph — the creation wizard requires one — so an
 * endpoint that let the last one be removed would make that invariant a lie. Replacing the cover is
 * an upload followed by a delete, in that order.
 */
public class LastImageException extends RuntimeException {

    public LastImageException() {
        super("A space must keep at least one photograph; upload a replacement before removing this one");
    }
}
