package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * GYM_ADMIN's reach stops at the weight room: every endpoint shared between ADMIN and GYM_ADMIN
 * that is keyed by a spaceId and exposes something an ADMIN-only audience was assumed to see
 * (another space's roster, closure history, or the ability to check a student in or book for one)
 * enforces this so a GYM_ADMIN account can't reach past its own room.
 *
 * <p>There is no {@code SpaceCategory.GYM} — one known room doesn't earn a taxonomy, and the gym's
 * own {@code space_types.category} is {@code SPORTS}, same as every court, so category couldn't
 * distinguish it anyway. This constant is the one place the gym's identity is spelled out; V15
 * already hardcodes the same literal id directly into a migration.
 */
final class GymSpaceAccessGuard {

    /** 'Gimnasio' / 'Gimnasio de Pesas' — the only gym space today (V15, V17). */
    static final UUID GYM_SPACE_ID = UUID.fromString("9dabc028-466a-4ac0-8693-5c78b4b59929");

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    private static final String GYM_ADMIN_AUTHORITY = "ROLE_GYM_ADMIN";

    private GymSpaceAccessGuard() {
    }

    /**
     * True when the caller may act on {@code spaceId}: ADMIN always can; GYM_ADMIN only for the gym
     * space itself. False for anyone else — unreachable past the controller's {@code @PreAuthorize},
     * but written to fail closed rather than assume that.
     */
    static boolean permits(Authentication authentication, UUID spaceId) {
        boolean isAdmin = false;
        boolean isGymAdmin = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (ADMIN_AUTHORITY.equals(authority.getAuthority())) {
                isAdmin = true;
            } else if (GYM_ADMIN_AUTHORITY.equals(authority.getAuthority())) {
                isGymAdmin = true;
            }
        }
        return isAdmin || (isGymAdmin && GYM_SPACE_ID.equals(spaceId));
    }
}
