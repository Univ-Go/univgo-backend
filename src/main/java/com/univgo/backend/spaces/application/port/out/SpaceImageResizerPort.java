package com.univgo.backend.spaces.application.port.out;

/**
 * Turns one uploaded file into the derivatives the product serves. The pixels are the only part of
 * the pipeline that is pure computation, so it is its own port: it can be tested without a bucket,
 * and the widths are product configuration the storage adapter has no business knowing.
 */
public interface SpaceImageResizerPort {

    /**
     * Decodes the source once and emits one derivative per configured width.
     *
     * @throws com.univgo.backend.spaces.domain.UnreadableImageException if the bytes are not a
     *     supported image, or the source is larger than the configured pixel ceiling.
     */
    ResizedImage resize(ImageUpload upload);
}
