package com.univgo.backend.spaces.application.port.out;

import java.util.Arrays;

/**
 * An uploaded file as the application sees it. Exists so {@code MultipartFile} — a {@code
 * spring-web} type — stops at the controller instead of reaching a use case.
 */
public record ImageUpload(String filename, String contentType, byte[] bytes) {

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ImageUpload(String thatFilename, String thatContentType, byte[] thatBytes))) {
            return false;
        }
        return filename.equals(thatFilename) && contentType.equals(thatContentType) && Arrays.equals(bytes, thatBytes);
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + contentType.hashCode();
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return "ImageUpload[filename=" + filename + ", contentType=" + contentType + ", bytes=" + Arrays.toString(bytes) + "]";
    }
}
