package com.codefactory.reservasmsscheduleservice.service.impl;

import com.codefactory.reservasmsscheduleservice.dto.response.EmployeeResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.exception.BusinessException;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.mapper.EmployeeMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - EmployeeServiceImpl (Extended)")
class EmployeeServiceImplExtendedTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private UUID employeeId;
    private UUID providerId;
    private Employee employee;
    private Employee inactiveEmployee;
    private EmployeeResponseDTO employeeDTO;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        employee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Pedro Gómez")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        inactiveEmployee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Pedro Gómez")
                .active(false)
                .hireDate(LocalDateTime.now())
                .build();

        employeeDTO = EmployeeResponseDTO.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("Pedro Gómez")
                .active(true)
                .build();
    }

    // ==================== getActiveEmployeesByProvider ====================

    @Nested
    @DisplayName("getActiveEmployeesByProvider")
    class GetActiveEmployeesByProviderTests {

        @Test
        @DisplayName("Debe retornar empleados activos del proveedor")
        void getActiveEmployeesByProvider_ValidProvider_ReturnsActive() {
            when(employeeRepository.findByProviderIdAndActiveTrue(providerId))
                    .thenReturn(List.of(employee));
            when(employeeMapper.toDto(employee)).thenReturn(employeeDTO);

            List<EmployeeResponseDTO> result =
                    employeeService.getActiveEmployeesByProvider(providerId, providerId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Debe lanzar excepción si accede a proveedor diferente")
        void getActiveEmployeesByProvider_DifferentProvider_ThrowsAccessDenied() {
            UUID otroProveedor = UUID.randomUUID();

            assertThatThrownBy(() ->
                    employeeService.getActiveEmployeesByProvider(providerId, otroProveedor))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay empleados activos")
        void getActiveEmployeesByProvider_NoActiveEmployees_ReturnsEmpty() {
            when(employeeRepository.findByProviderIdAndActiveTrue(providerId))
                    .thenReturn(List.of());

            List<EmployeeResponseDTO> result =
                    employeeService.getActiveEmployeesByProvider(providerId, providerId);

            assertThat(result).isEmpty();
        }
    }

    // ==================== activateEmployee - missing notOwner case ====================

    @Nested
    @DisplayName("activateEmployee - additional cases")
    class ActivateEmployeeAdditionalTests {

        @Test
        @DisplayName("Debe lanzar excepción si no es el dueño")
        void activateEmployee_NotOwner_ThrowsAccessDenied() {
            UUID otroProveedor = UUID.randomUUID();
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(inactiveEmployee));

            assertThatThrownBy(() ->
                    employeeService.activateEmployee(employeeId, otroProveedor))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe al activar")
        void activateEmployee_EmployeeNotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    employeeService.activateEmployee(employeeId, providerId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe activar correctamente un empleado inactivo")
        void activateEmployee_InactiveEmployee_Activates() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(inactiveEmployee));
            when(employeeRepository.save(any())).thenReturn(inactiveEmployee);

            employeeService.activateEmployee(employeeId, providerId);

            verify(employeeRepository).save(any());
        }
    }

    // ==================== getEmployeeByIdPublic - not found case ====================

    @Nested
    @DisplayName("getEmployeeByIdPublic")
    class GetEmployeeByIdPublicTests {

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void getEmployeeByIdPublic_NotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    employeeService.getEmployeeByIdPublic(employeeId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }
    }

    // ==================== isEmployeeActive edge case ====================

    @Nested
    @DisplayName("isEmployeeActive - additional")
    class IsEmployeeActiveAdditionalTests {

        @Test
        @DisplayName("Debe retornar false para empleado inactivo existente")
        void isEmployeeActive_InactiveEmployee_ReturnsFalse() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(inactiveEmployee));

            boolean result = employeeService.isEmployeeActive(employeeId);

            assertThat(result).isFalse();
        }
    }
}
