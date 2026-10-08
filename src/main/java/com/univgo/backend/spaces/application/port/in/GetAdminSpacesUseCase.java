package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.AdminSpaceDetail;
import com.univgo.backend.spaces.domain.AdminSpaceSummary;
import java.util.List;
import java.util.UUID;

public interface GetAdminSpacesUseCase {

    List<AdminSpaceSummary> list(boolean includeArchived);

    AdminSpaceDetail detail(UUID spaceId);
}
