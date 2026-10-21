package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class GymSpaceAccessGuardTest {

    private static final UUID GYM_SPACE_ID = GymSpaceAccessGuard.GYM_SPACE_ID;
    private static final UUID OTHER_SPACE_ID = UUID.randomUUID();

    @Test
    void adminPermitsAnySpace() {
        Authentication admin = authFor("ROLE_ADMIN");
        assertThat(GymSpaceAccessGuard.permits(admin, GYM_SPACE_ID)).isTrue();
        assertThat(GymSpaceAccessGuard.permits(admin, OTHER_SPACE_ID)).isTrue();
    }

    @Test
    void gymAdminPermitsOnlyTheGymSpace() {
        Authentication gymAdmin = authFor("ROLE_GYM_ADMIN");
        assertThat(GymSpaceAccessGuard.permits(gymAdmin, GYM_SPACE_ID)).isTrue();
        assertThat(GymSpaceAccessGuard.permits(gymAdmin, OTHER_SPACE_ID)).isFalse();
    }

    @Test
    void neitherRoleDeniesEverything() {
        Authentication student = authFor("ROLE_STUDENT");
        assertThat(GymSpaceAccessGuard.permits(student, GYM_SPACE_ID)).isFalse();
        assertThat(GymSpaceAccessGuard.permits(student, OTHER_SPACE_ID)).isFalse();
    }

    private static Authentication authFor(String authority) {
        return new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null, List.of(new SimpleGrantedAuthority(authority)));
    }
}
