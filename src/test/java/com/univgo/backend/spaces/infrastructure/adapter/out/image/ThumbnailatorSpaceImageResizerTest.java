package com.univgo.backend.spaces.infrastructure.adapter.out.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.univgo.backend.shared.config.SpaceImageProperties;
import com.univgo.backend.spaces.application.port.out.ImageUpload;
import com.univgo.backend.spaces.application.port.out.ResizedImage;
import com.univgo.backend.spaces.domain.UnreadableImageException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ThumbnailatorSpaceImageResizerTest {

    private static final SpaceImageProperties PROPERTIES =
            new SpaceImageProperties(List.of(640, 960), 8, 24_000_000L, 0.82f);

    private final ThumbnailatorSpaceImageResizer resizer = new ThumbnailatorSpaceImageResizer(PROPERTIES);

    private static byte[] jpeg(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new Color(120, 0, 20));
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", out);
        } catch (IOException cannotWriteFixture) {
            throw new UncheckedIOException(cannotWriteFixture);
        }
        return out.toByteArray();
    }

    private static ImageUpload upload(byte[] bytes) {
        return new ImageUpload("court.jpg", "image/jpeg", bytes);
    }

    private static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException cannotRead) {
            throw new UncheckedIOException(cannotRead);
        }
    }

    @Test
    void emitsOneDerivativePerConfiguredWidth() {
        ResizedImage resized = resizer.resize(upload(jpeg(2000, 1500)));

        assertThat(resized.derivatives()).containsOnlyKeys(640, 960);
        assertThat(decode(resized.derivatives().get(640)).getWidth()).isEqualTo(640);
        assertThat(decode(resized.derivatives().get(960)).getWidth()).isEqualTo(960);
    }

    @Test
    void recordsTheSourceDimensions() {
        ResizedImage resized = resizer.resize(upload(jpeg(2000, 1500)));

        assertThat(resized.sourceWidth()).isEqualTo(2000);
        assertThat(resized.sourceHeight()).isEqualTo(1500);
    }

    @Test
    void preservesTheAspectRatio() {
        ResizedImage resized = resizer.resize(upload(jpeg(2000, 1000)));

        BufferedImage derivative = decode(resized.derivatives().get(640));
        assertThat(derivative.getWidth()).isEqualTo(640);
        assertThat(derivative.getHeight()).isEqualTo(320);
    }

    @Test
    void neverUpscalesASourceNarrowerThanATargetWidth() {
        ResizedImage resized = resizer.resize(upload(jpeg(400, 300)));

        assertThat(decode(resized.derivatives().get(640)).getWidth()).isEqualTo(400);
        assertThat(decode(resized.derivatives().get(960)).getWidth()).isEqualTo(400);
    }

    @Test
    void derivativesAreJpegWhateverCameIn() {
        ResizedImage resized = resizer.resize(upload(jpeg(800, 600)));

        assertThat(resized.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void rejectsBytesThatAreNotAnImageWithADomainException() {
        ImageUpload notAnImage = new ImageUpload("notes.jpg", "image/jpeg", "this is not a photograph".getBytes());

        assertThatThrownBy(() -> resizer.resize(notAnImage))
                .isInstanceOf(UnreadableImageException.class)
                .hasMessageContaining("notes.jpg");
    }

    @Test
    void rejectsASourceOverThePixelCeiling() {
        ThumbnailatorSpaceImageResizer tightResizer =
                new ThumbnailatorSpaceImageResizer(new SpaceImageProperties(List.of(640), 8, 1_000L, 0.82f));

        assertThatThrownBy(() -> tightResizer.resize(upload(jpeg(800, 600))))
                .isInstanceOf(UnreadableImageException.class)
                .hasMessageContaining("too large");
    }
}
