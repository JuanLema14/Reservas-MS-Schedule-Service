package com.codefactory.reservasmsscheduleservice.exception;

import com.codefactory.reservasmsscheduleservice.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("MS-Schedule - GlobalExceptionHandler (Unit)")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/employees");
    }

    @Test
    @DisplayName("Debe manejar EmployeeNotFoundException con 404")
    void handleEmployeeNotFoundException_Returns404() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response = handler.handleEmployeeNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getPath()).isEqualTo("/api/employees");
    }

    @Test
    @DisplayName("Debe manejar BusinessException con 409")
    void handleBusinessException_Returns409() {
        BusinessException ex = new BusinessException("El empleado ya está activo");
        ResponseEntity<ErrorResponseDTO> response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("El empleado ya está activo");
    }

    @Test
    @DisplayName("Debe manejar ExternalServiceException con 503")
    void handleExternalServiceException_Returns503() {
        ExternalServiceException ex = new ExternalServiceException("AUTH", "No disponible", 503);
        ResponseEntity<ErrorResponseDTO> response = handler.handleExternalServiceException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getStatus()).isEqualTo(503);
    }

    @Test
    @DisplayName("Debe manejar AccessDeniedException con 403")
    void handleAccessDeniedException_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Sin permiso");
        ResponseEntity<ErrorResponseDTO> response = handler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("Debe manejar WorkScheduleNotFoundException con 404")
    void handleWorkScheduleNotFoundException_Returns404() {
        WorkScheduleNotFoundException ex = new WorkScheduleNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response = handler.handleWorkScheduleNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Debe manejar WorkScheduleConflictException con 409")
    void handleWorkScheduleConflictException_Returns409() {
        WorkScheduleConflictException ex = new WorkScheduleConflictException("Conflicto de horario");
        ResponseEntity<ErrorResponseDTO> response = handler.handleWorkScheduleConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Debe manejar InvalidWorkScheduleException con 400")
    void handleInvalidWorkScheduleException_Returns400() {
        InvalidWorkScheduleException ex = new InvalidWorkScheduleException("Hora inválida");
        ResponseEntity<ErrorResponseDTO> response = handler.handleInvalidWorkScheduleException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Debe manejar ScheduleBlockNotFoundException con 404")
    void handleScheduleBlockNotFoundException_Returns404() {
        ScheduleBlockNotFoundException ex = new ScheduleBlockNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response = handler.handleScheduleBlockNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Debe manejar ScheduleBlockConflictException con 409")
    void handleScheduleBlockConflictException_Returns409() {
        ScheduleBlockConflictException ex = new ScheduleBlockConflictException("Bloque en conflicto");
        ResponseEntity<ErrorResponseDTO> response = handler.handleScheduleBlockConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Debe manejar InvalidScheduleBlockException con 400")
    void handleInvalidScheduleBlockException_Returns400() {
        InvalidScheduleBlockException ex = new InvalidScheduleBlockException("Bloque inválido");
        ResponseEntity<ErrorResponseDTO> response = handler.handleInvalidScheduleBlockException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Debe manejar EmployeeServiceNotFoundException con 404")
    void handleEmployeeServiceNotFoundException_Returns404() {
        EmployeeServiceNotFoundException ex = new EmployeeServiceNotFoundException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response = handler.handleEmployeeServiceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Debe manejar EmployeeServiceAlreadyExistsException con 409")
    void handleEmployeeServiceAlreadyExistsException_Returns409() {
        EmployeeServiceAlreadyExistsException ex =
                new EmployeeServiceAlreadyExistsException(UUID.randomUUID(), UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleEmployeeServiceAlreadyExistsException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Debe manejar EmployeeServiceAlreadyActiveException con 409")
    void handleEmployeeServiceAlreadyActiveException_Returns409() {
        EmployeeServiceAlreadyActiveException ex =
                new EmployeeServiceAlreadyActiveException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleEmployeeServiceAlreadyActiveException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Debe manejar EmployeeServiceAlreadyInactiveException con 409")
    void handleEmployeeServiceAlreadyInactiveException_Returns409() {
        EmployeeServiceAlreadyInactiveException ex =
                new EmployeeServiceAlreadyInactiveException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleEmployeeServiceAlreadyInactiveException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Debe manejar ServiceNotFoundInCatalogException con 404")
    void handleServiceNotFoundInCatalogException_Returns404() {
        ServiceNotFoundInCatalogException ex =
                new ServiceNotFoundInCatalogException(UUID.randomUUID());
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleServiceNotFoundInCatalogException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Debe manejar EmployeeServiceOwnershipException con 403")
    void handleEmployeeServiceOwnershipException_Returns403() {
        EmployeeServiceOwnershipException ex = new EmployeeServiceOwnershipException();
        ResponseEntity<ErrorResponseDTO> response =
                handler.handleEmployeeServiceOwnershipException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Debe manejar Exception genérica con 500")
    void handleGenericException_Returns500() {
        Exception ex = new RuntimeException("Error inesperado");
        ResponseEntity<ErrorResponseDTO> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException con errores de campo")
    void handleValidationExceptions_Returns400WithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "fullName", "El nombre es obligatorio");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponseDTO> response = handler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getValidationErrors()).containsKey("fullName");
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentTypeMismatchException con 400")
    void handleTypeMismatchException_Returns400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn("UUID format error");

        ResponseEntity<ErrorResponseDTO> response = handler.handleTypeMismatchException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid UUID format");
    }

    @Test
    @DisplayName("La respuesta incluye timestamp y path")
    void errorResponse_IncludesTimestampAndPath() {
        EmployeeNotFoundException ex = new EmployeeNotFoundException("Test");
        ResponseEntity<ErrorResponseDTO> response = handler.handleEmployeeNotFoundException(ex, request);

        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/employees");
    }
}
