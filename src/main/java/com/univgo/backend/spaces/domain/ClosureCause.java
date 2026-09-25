package com.univgo.backend.spaces.domain;

/** Why a space was closed, and whatever free-text the admin added about it. */
public record ClosureCause(ClosureReason reason, String details) {
}
