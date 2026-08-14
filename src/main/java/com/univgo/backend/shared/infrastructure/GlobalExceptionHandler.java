package com.univgo.backend.shared.infrastructure;

import com.univgo.backend.auth.domain.InvalidCredentialsException;
import com.univgo.backend.reservations.domain.GuestsNotFoundException;
import com.univgo.backend.reservations.domain.InvalidReservationStateException;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import com.univgo.backend.reservations.domain.ReservationOverlapException;
import com.univgo.backend.reservations.domain.SpaceNotFoundException;
import com.univgo.backend.users.domain.UserNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, ReservationNotFoundException.class, SpaceNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler({GuestsNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({InvalidReservationStateException.class, ReservationOverlapException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message));
    }
}
