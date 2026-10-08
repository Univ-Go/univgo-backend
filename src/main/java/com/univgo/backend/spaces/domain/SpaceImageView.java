package com.univgo.backend.spaces.domain;

import java.util.Map;
import java.util.UUID;

/**
 * A photograph as a view renders it: the row's facts plus a fetchable URL per derivative width.
 *
 * <p>One URL per width rather than one URL, because the surfaces differ by an order of magnitude —
 * a card on a phone and a banner on a desktop — and no image optimizer sits in front of these any
 * more. The caller picks the width it actually renders at; the keys are the widths configured in
 * {@code univgo.spaces.images.widths}.
 *
 * <p>The URLs are derived and never stored: they are signed, and the signature expires. Same split
 * as {@code SpaceCatalogItem}, which is a read model rather than a {@code Space}.
 */
public record SpaceImageView(UUID id, int position, Map<Integer, String> urls, int width, int height) {

    public SpaceImageView {
        urls = Map.copyOf(urls);
    }

    /** The narrowest derivative, which is what a thumbnail wants. */
    public String smallestUrl() {
        return urls.entrySet().stream()
                .min(Map.Entry.comparingByKey())
                .orElseThrow(() -> new IllegalStateException("A photograph has no derivative"))
                .getValue();
    }
}
