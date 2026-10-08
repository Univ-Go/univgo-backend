package com.univgo.backend.spaces.infrastructure.adapter.out.image;

import com.univgo.backend.shared.config.SpaceImageProperties;
import com.univgo.backend.spaces.application.port.out.SpaceImageLimitsPort;
import org.springframework.stereotype.Component;

@Component
public class SpaceImageLimitsAdapter implements SpaceImageLimitsPort {

    private final SpaceImageProperties properties;

    public SpaceImageLimitsAdapter(SpaceImageProperties properties) {
        this.properties = properties;
    }

    @Override
    public int maxPerSpace() {
        return properties.maxPerSpace();
    }
}
