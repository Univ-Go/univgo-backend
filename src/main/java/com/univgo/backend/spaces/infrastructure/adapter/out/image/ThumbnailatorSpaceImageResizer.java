package com.univgo.backend.spaces.infrastructure.adapter.out.image;

import com.univgo.backend.shared.config.SpaceImageProperties;
import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.application.port.out.ResizedImage;
import com.univgo.backend.spaces.application.port.out.SpaceImageResizerPort;
import com.univgo.backend.spaces.domain.UnreadableImageException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

/**
 * Derives the widths the product serves from one uploaded photograph.
 *
 * <p>Output is JPEG whatever came in. The JDK has no WebP codec at all — not just no writer — so a
 * WebP source cannot even be decoded, which is why the accepted extensions stop at JPEG and PNG.
 * A WebP *writer* would need a native library for a saving measured in tens of kilobytes on an
 * image this small; the derivative being JPEG is a deliberate trade, not an oversight.
 *
 * <p><b>One photograph at a time.</b> A 12 MP source occupies roughly 48 MB as a
 * {@link BufferedImage}, so a handful decoded concurrently exhausts a small instance. The caller
 * must not parallelise a batch across this port, and this class holds no state that would make that
 * look safe.
 */
@Component
public class ThumbnailatorSpaceImageResizer implements SpaceImageResizerPort {

    private static final String OUTPUT_CONTENT_TYPE = "image/jpeg";
    private static final String OUTPUT_FORMAT = "jpg";

    private final SpaceImageProperties properties;

    public ThumbnailatorSpaceImageResizer(SpaceImageProperties properties) {
        this.properties = properties;
    }

    @Override
    public ResizedImage resize(ImageUpload upload) {
        BufferedImage source = decode(upload);
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        if ((long) sourceWidth * sourceHeight > properties.maxPixels()) {
            throw new UnreadableImageException(upload.filename(), "the image is too large to process");
        }

        Map<Integer, byte[]> derivatives = new LinkedHashMap<>();
        for (int width : properties.widths()) {
            derivatives.put(width, encodeAt(upload, source, width, sourceWidth));
        }

        return new ResizedImage(sourceWidth, sourceHeight, Map.copyOf(derivatives), OUTPUT_CONTENT_TYPE);
    }

    /**
     * Read through Thumbnailator rather than {@code ImageIO.read} so the EXIF orientation tag is
     * applied: without it every photograph taken in portrait on a phone is stored on its side.
     */
    private static BufferedImage decode(ImageUpload upload) {
        try {
            return Thumbnails.of(new ByteArrayInputStream(upload.bytes()))
                    .scale(1)
                    .useExifOrientation(true)
                    .asBufferedImage();
        } catch (IOException | IllegalArgumentException notAnImage) {
            throw new UnreadableImageException(upload.filename(), "it is not a readable JPEG or PNG");
        }
    }

    /**
     * Never upscales: a source narrower than the target is encoded at its own width, so a small
     * photograph stays small instead of being blown up into a blurry one.
     */
    private byte[] encodeAt(ImageUpload upload, BufferedImage source, int targetWidth, int sourceWidth) {
        int width = Math.min(targetWidth, sourceWidth);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Thumbnails.of(source)
                    .width(width)
                    .keepAspectRatio(true)
                    .outputFormat(OUTPUT_FORMAT)
                    .outputQuality(properties.jpegQuality())
                    .toOutputStream(out);
        } catch (IOException cannotEncode) {
            throw new UnreadableImageException(upload.filename(), "it could not be re-encoded");
        }
        return out.toByteArray();
    }
}
