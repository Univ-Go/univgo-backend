package com.univgo.backend.spaces.application.port.out;

import java.util.Map;

/**
 * The bytes of a photograph, as opposed to its row. Separate from the repository because storing
 * bytes and recording an order fail differently and in different places: one is the network, the
 * other the database.
 */
public interface SpaceImageStoragePort {

    void put(String key, byte[] bytes, String contentType);

    /** Removes every derivative of one photograph, which all share its {@code originalKey}. */
    void deletePrefix(String keyPrefix);

    /**
     * A fetchable URL per derivative width for the photograph stored under this prefix. Which widths
     * exist belongs to the adapter, so adding one does not reach up here. Signed, so they expire —
     * callers must not persist what this returns.
     */
    Map<Integer, String> urlsOf(String originalKey);

    /** The key one derivative of a photograph is stored at. */
    String derivativeKey(String originalKey, int width);
}
