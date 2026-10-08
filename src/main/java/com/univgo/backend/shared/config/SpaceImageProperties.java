package com.univgo.backend.shared.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * How a space photograph is resized and how many a space may hold.
 *
 * <p>{@code widths} mirrors {@code images.sizes} in the frontend's {@code vercel.json}: the browser
 * asks for one of those widths, so adding one here without adding it there serves a derivative
 * nothing requests.
 *
 * @param widths       widths to derive from each upload, ascending; the first is the cover size.
 * @param maxPerSpace  how many photographs one space may hold.
 * @param maxPixels    largest source a decode is attempted on. A 12 MP JPEG already occupies ~48 MB
 *                     as a {@code BufferedImage}, so this is a memory ceiling, not a quality one.
 * @param jpegQuality  quality of the derived JPEGs, 0..1.
 */
@ConfigurationProperties("univgo.spaces.images")
public record SpaceImageProperties(
        List<Integer> widths,
        int maxPerSpace,
        long maxPixels,
        float jpegQuality) {

    public SpaceImageProperties {
        widths = List.copyOf(widths);
    }
}
