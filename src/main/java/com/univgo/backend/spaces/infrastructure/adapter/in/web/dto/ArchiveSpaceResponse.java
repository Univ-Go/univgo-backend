package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

/** @param suspendedReservations reservations the archiving suspended; none of them were cancelled. */
public record ArchiveSpaceResponse(int suspendedReservations) {
}
