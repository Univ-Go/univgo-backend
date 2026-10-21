package com.univgo.backend.spaces.application.port.out;

import java.util.Map;

/**
 * @param sourceWidth   the source's width, recorded so a layout can reserve the right box.
 * @param sourceHeight  the source's height.
 * @param derivatives   one entry per configured width, each the encoded bytes of that derivative.
 * @param contentType   media type the derivatives are encoded in.
 */
public record ResizedImage(
        int sourceWidth, int sourceHeight, Map<Integer, byte[]> derivatives, String contentType) {
}
