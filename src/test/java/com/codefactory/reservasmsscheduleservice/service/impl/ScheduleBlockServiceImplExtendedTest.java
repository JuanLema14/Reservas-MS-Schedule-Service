package com.codefactory.reservasmsscheduleservice.service.impl;

import com.codefactory.reservasmsscheduleservice.dto.response.ScheduleBlockResponseDTO;
import com.codefactory.reservasmsscheduleservice.entity.Employee;
import com.codefactory.reservasmsscheduleservice.entity.ScheduleBlock;
import com.codefactory.reservasmsscheduleservice.entity.WorkSchedule;
import com.codefactory.reservasmsscheduleservice.exception.EmployeeNotFoundException;
import com.codefactory.reservasmsscheduleservice.mapper.ScheduleBlockMapper;
import com.codefactory.reservasmsscheduleservice.repository.EmployeeRepository;
import com.codefactory.reservasmsscheduleservice.repository.ScheduleBlockRepository;
import com.codefactory.reservasmsscheduleservice.repository.WorkScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MS-Schedule - ScheduleBlockServiceImpl (Extended)")
class ScheduleBlockServiceImplExtendedTest {

    @Mock private ScheduleBlockRepository scheduleBlockRepository;
    @Mock private WorkScheduleRepository workScheduleRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ScheduleBlockMapper scheduleBlockMapper;

    @InjectMocks
    private ScheduleBlockServiceImpl scheduleBlockService;

    private UUID employeeId;
    private UUID providerId;
    private UUID blockId;
    private UUID reservationId;
    private Employee employee;
    private ScheduleBlock scheduleBlock;
    private ScheduleBlockResponseDTO scheduleBlockDTO;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        employeeId    = UUID.randomUUID();
        providerId    = UUID.randomUUID();
        blockId       = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        tomorrow      = LocalDate.now().plusDays(1);

        employee = Employee.builder()
                .id(employeeId)
                .providerId(providerId)
                .fullName("María López")
                .active(true)
                .hireDate(LocalDateTime.now())
                .build();

        scheduleBlock = ScheduleBlock.builder()
                .id(blockId)
                .employee(employee)
                .reservationId(reservationId)
                .date(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .blockType("RESERVA")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        scheduleBlockDTO = ScheduleBlockResponseDTO.builder()
                .id(blockId)
                .employeeId(employeeId)
                .date(tomorrow)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .blockType("RESERVA")
                .active(true)
                .build();
    }

    // ==================== getScheduleBlocksByEmployeePublic ====================

    @Nested
    @DisplayName("getScheduleBlocksByEmployeePublic")
    class GetBlocksPublicTests {

        @Test
        @DisplayName("Debe retornar bloques activos del empleado")
        void getScheduleBlocksByEmployeePublic_ValidEmployee_ReturnsBlocks() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndActiveTrue(employeeId))
                    .thenReturn(List.of(scheduleBlock));
            when(scheduleBlockMapper.toDto(scheduleBlock)).thenReturn(scheduleBlockDTO);

            List<ScheduleBlockResponseDTO> result =
                    scheduleBlockService.getScheduleBlocksByEmployeePublic(employeeId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(blockId);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void getScheduleBlocksByEmployeePublic_EmployeeNotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    scheduleBlockService.getScheduleBlocksByEmployeePublic(employeeId))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay bloques activos")
        void getScheduleBlocksByEmployeePublic_NoBlocks_ReturnsEmpty() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndActiveTrue(employeeId))
                    .thenReturn(List.of());

            List<ScheduleBlockResponseDTO> result =
                    scheduleBlockService.getScheduleBlocksByEmployeePublic(employeeId);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getScheduleBlocksByEmployeeAndDateRange ====================

    @Nested
    @DisplayName("getScheduleBlocksByEmployeeAndDateRange")
    class GetBlocksByDateRangeTests {

        @Test
        @DisplayName("Debe retornar bloques en el rango de fechas")
        void getScheduleBlocksByEmployeeAndDateRange_ValidRange_ReturnsBlocks() {
            LocalDate start = LocalDate.now();
            LocalDate end   = LocalDate.now().plusDays(7);

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndDateBetweenAndActiveTrue(
                    employeeId, start, end)).thenReturn(List.of(scheduleBlock));
            when(scheduleBlockMapper.toDto(scheduleBlock)).thenReturn(scheduleBlockDTO);

            List<ScheduleBlockResponseDTO> result =
                    scheduleBlockService.getScheduleBlocksByEmployeeAndDateRange(employeeId, start, end);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void getScheduleBlocksByEmployeeAndDateRange_EmployeeNotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    scheduleBlockService.getScheduleBlocksByEmployeeAndDateRange(
                            employeeId, LocalDate.now(), LocalDate.now().plusDays(7)))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay bloques en el rango")
        void getScheduleBlocksByEmployeeAndDateRange_NoBlocks_ReturnsEmpty() {
            LocalDate start = LocalDate.now().plusMonths(1);
            LocalDate end   = LocalDate.now().plusMonths(2);

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndDateBetweenAndActiveTrue(
                    employeeId, start, end)).thenReturn(List.of());

            List<ScheduleBlockResponseDTO> result =
                    scheduleBlockService.getScheduleBlocksByEmployeeAndDateRange(employeeId, start, end);

            assertThat(result).isEmpty();
        }
    }

    // ==================== getScheduleBlocksByEmployeeAndDate ====================

    @Nested
    @DisplayName("getScheduleBlocksByEmployeeAndDate")
    class GetBlocksByDateTests {

        @Test
        @DisplayName("Debe retornar bloques para una fecha específica")
        void getScheduleBlocksByEmployeeAndDate_ValidDate_ReturnsBlocks() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndDateAndActiveTrue(employeeId, tomorrow))
                    .thenReturn(List.of(scheduleBlock));
            when(scheduleBlockMapper.toDto(scheduleBlock)).thenReturn(scheduleBlockDTO);

            List<ScheduleBlockResponseDTO> result =
                    scheduleBlockService.getScheduleBlocksByEmployeeAndDate(employeeId, tomorrow);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Debe lanzar excepción si empleado no existe")
        void getScheduleBlocksByEmployeeAndDate_EmployeeNotFound_ThrowsException() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    scheduleBlockService.getScheduleBlocksByEmployeeAndDate(employeeId, tomorrow))
                    .isInstanceOf(EmployeeNotFoundException.class);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay bloques en la fecha")
        void getScheduleBlocksByEmployeeAndDate_NoBlocks_ReturnsEmpty() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(scheduleBlockRepository.findByEmployeeIdAndDateAndActiveTrue(employeeId, tomorrow))
                    .thenReturn(List.of());

            List<ScheduleBlockResponseDTO> result =
                    scheduleBlockService.getScheduleBlocksByEmployeeAndDate(employeeId, tomorrow);

            assertThat(result).isEmpty();
        }
    }

    // ==================== isEmployeeAvailable - edge cases ====================

    @Nested
    @DisplayName("isEmployeeAvailable - additional cases")
    class IsEmployeeAvailableExtendedTests {

        @Test
        @DisplayName("Debe retornar false si empleado no tiene horario laboral en ese día")
        void isEmployeeAvailable_NoWorkSchedule_ReturnsFalse() {
            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(
                    eq(employeeId), anyString())).thenReturn(List.of());

            boolean result = scheduleBlockService.isEmployeeAvailable(
                    employeeId, tomorrow,
                    LocalTime.of(10, 0), LocalTime.of(11, 0));

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false si hay conflicto con bloque existente")
        void isEmployeeAvailable_WithConflictingBlock_ReturnsFalse() {
            WorkSchedule ws = WorkSchedule.builder()
                    .employee(employee)
                    .dayOfWeek(getDayOfWeek(tomorrow))
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(18, 0))
                    .active(true)
                    .build();

            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(
                    eq(employeeId), anyString())).thenReturn(List.of(ws));
            when(scheduleBlockRepository.findOverlappingBlocks(
                    eq(employeeId), eq(tomorrow), any(), any()))
                    .thenReturn(List.of(scheduleBlock));

            boolean result = scheduleBlockService.isEmployeeAvailable(
                    employeeId, tomorrow,
                    LocalTime.of(10, 30), LocalTime.of(11, 30));

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Debe retornar true si hay horario y sin conflictos")
        void isEmployeeAvailable_WorkingAndNoConflict_ReturnsTrue() {
            WorkSchedule ws = WorkSchedule.builder()
                    .employee(employee)
                    .dayOfWeek(getDayOfWeek(tomorrow))
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(18, 0))
                    .active(true)
                    .build();

            when(workScheduleRepository.findByEmployeeIdAndDayOfWeekAndActiveTrue(
                    eq(employeeId), anyString())).thenReturn(List.of(ws));
            when(scheduleBlockRepository.findOverlappingBlocks(
                    eq(employeeId), eq(tomorrow), any(), any()))
                    .thenReturn(List.of());

            boolean result = scheduleBlockService.isEmployeeAvailable(
                    employeeId, tomorrow,
                    LocalTime.of(14, 0), LocalTime.of(15, 0));

            assertThat(result).isTrue();
        }
    }

    // ==================== EmployeeService - missing coverage ====================

    private String getDayOfWeek(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}
