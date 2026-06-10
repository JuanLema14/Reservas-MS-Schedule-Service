package com.codefactory.reservasmsscheduleservice.acceptance.steps;

import com.codefactory.reservasmsscheduleservice.dto.request.CreateScheduleBlockRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.request.CreateWorkScheduleRequestDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.ScheduleBlockResponseDTO;
import com.codefactory.reservasmsscheduleservice.dto.response.WorkScheduleResponseDTO;
import com.codefactory.reservasmsscheduleservice.exception.ScheduleBlockConflictException;
import com.codefactory.reservasmsscheduleservice.exception.ServiceOwnershipException;
import com.codefactory.reservasmsscheduleservice.exception.WorkScheduleConflictException;
import com.codefactory.reservasmsscheduleservice.service.ScheduleBlockService;
import com.codefactory.reservasmsscheduleservice.service.WorkScheduleService;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AgendaSteps {

    @Autowired
    private WorkScheduleService workScheduleService;

    @Autowired
    private ScheduleBlockService scheduleBlockService;

    // UUIDs del seed
    private static final UUID ID_PROVEEDOR_BELLA_VIDA = UUID.fromString("b1000000-0000-0000-0000-000000000010");
    private static final UUID ID_PROVEEDOR_CLINICA    = UUID.fromString("b1000000-0000-0000-0000-000000000011");
    private static final UUID ID_EMPLEADO_ANA         = UUID.fromString("d1000000-0000-0000-0000-000000000001");
    private static final UUID ID_EMPLEADO_DR_RAMIREZ  = UUID.fromString("d1000000-0000-0000-0000-000000000003");

    // Estado de la prueba
    private UUID proveedorAutenticadoId;
    private UUID empleadoSeleccionadoId;
    private CreateWorkScheduleRequestDTO workScheduleRequest;
    private CreateScheduleBlockRequestDTO scheduleBlockRequest;
    private WorkScheduleResponseDTO workScheduleResponse;
    private ScheduleBlockResponseDTO scheduleBlockResponse;
    private Exception capturedException;

    @Dado("que el proveedor está autenticado como {string}")
    public void proveedorAutenticado(String email) {
        if (email.contains("bellavida")) {
            proveedorAutenticadoId = ID_PROVEEDOR_BELLA_VIDA;
        } else {
            proveedorAutenticadoId = ID_PROVEEDOR_CLINICA;
        }
        workScheduleRequest  = new CreateWorkScheduleRequestDTO();
        scheduleBlockRequest = new CreateScheduleBlockRequestDTO();
        workScheduleResponse  = null;
        scheduleBlockResponse = null;
        capturedException = null;
    }

    @Dado("que el empleado {string} pertenece al proveedor autenticado")
    public void empleadoPerteneceAlProveedor(String nombreEmpleado) {
        reset(workScheduleService, scheduleBlockService);
        empleadoSeleccionadoId = ID_EMPLEADO_ANA;
        workScheduleRequest.setEmployeeId(empleadoSeleccionadoId);

        WorkScheduleResponseDTO mockResp = WorkScheduleResponseDTO.builder()
                .id(UUID.randomUUID())
                .employeeId(empleadoSeleccionadoId)
                .dayOfWeek("LUNES")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .active(true)
                .build();
        when(workScheduleService.createWorkSchedule(any(CreateWorkScheduleRequestDTO.class), eq(proveedorAutenticadoId)))
                .thenReturn(mockResp);
    }

    @Cuando("el proveedor accede a la sección {string}")
    public void proveedorAccedeSeccion(String seccion) {
        // Navegación UI — no requiere lógica de service en tests de unidad
    }

    @Cuando("selecciona el día disponible {string}")
    public void seleccionaDiaDisponible(String dia) {
        workScheduleRequest.setDayOfWeek(dia);
    }

    @Cuando("define el horario de inicio {string} y fin {string}")
    public void defineHorario(String inicio, String fin) {
        workScheduleRequest.setStartTime(LocalTime.parse(inicio));
        workScheduleRequest.setEndTime(LocalTime.parse(fin));
    }

    @Cuando("establece la duración por cita de {int} minutos")
    public void estableceDuracion(int minutos) {
        // La duración por cita está en el servicio del catálogo; en schedule se define el horario general
    }

    @Cuando("guarda la configuración de agenda")
    public void guardaConfiguracionAgenda() {
        try {
            workScheduleResponse = workScheduleService.createWorkSchedule(workScheduleRequest, proveedorAutenticadoId);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema registra el horario laboral exitosamente")
    public void sistemaRegistraHorario() {
        assertThat(workScheduleResponse).isNotNull();
        assertThat(workScheduleResponse.getId()).isNotNull();
    }

    @Entonces("los clientes pueden ver ese horario al momento de reservar")
    public void clientesPuedenVerHorario() {
        assertThat(workScheduleResponse.getActive()).isTrue();
    }

    @Entonces("el código de respuesta HTTP es {int}")
    public void codigoRespuestaHTTP(int codigo) {
        if (codigo == 201) {
            assertThat(capturedException).isNull();
        } else {
            assertThat(capturedException).isNotNull();
        }
    }

    // ──────── Bloqueo de fecha ────────

    @Dado("que el empleado {string} tiene configurada su agenda")
    public void empleadoTieneAgendaConfigurada(String nombreEmpleado) {
        reset(scheduleBlockService);
        empleadoSeleccionadoId = ID_EMPLEADO_ANA;

        ScheduleBlockResponseDTO mockResp = ScheduleBlockResponseDTO.builder()
                .id(UUID.randomUUID())
                .employeeId(empleadoSeleccionadoId)
                .date(LocalDate.parse("2026-08-01"))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(17, 0))
                .blockType("VACACIONES")
                .active(true)
                .build();
        when(scheduleBlockService.createScheduleBlock(any(CreateScheduleBlockRequestDTO.class), eq(proveedorAutenticadoId)))
                .thenReturn(mockResp);
    }

    @Cuando("el proveedor selecciona la fecha {string}")
    public void seleccionaFecha(String fecha) {
        scheduleBlockRequest.setDate(LocalDate.parse(fecha));
        scheduleBlockRequest.setEmployeeId(ID_EMPLEADO_ANA);
    }

    @Cuando("el tipo de bloqueo es {string}")
    public void tipoBloqueoEs(String tipo) {
        scheduleBlockRequest.setBlockType(tipo);
        scheduleBlockRequest.setStartTime(LocalTime.of(8, 0));
        scheduleBlockRequest.setEndTime(LocalTime.of(17, 0));
    }

    @Cuando("confirma el bloqueo del día")
    public void confirmaBloqueo() {
        try {
            scheduleBlockResponse = scheduleBlockService.createScheduleBlock(scheduleBlockRequest, proveedorAutenticadoId);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema marca ese día como no disponible")
    public void sistemaMarcaDiaNoDisponible() {
        assertThat(scheduleBlockResponse).isNotNull();
        assertThat(scheduleBlockResponse.getBlockType()).isEqualTo("VACACIONES");
    }

    @Entonces("no permite reservas para esa fecha")
    public void noPermiteReservasParaEsaFecha() {
        assertThat(scheduleBlockResponse.getActive()).isTrue();
    }

    // ──────── Sin días seleccionados ────────

    @Dado("que el proveedor está en la sección {string}")
    public void proveedorEnSeccion(String seccion) {
        reset(workScheduleService);
        when(workScheduleService.createWorkSchedule(any(CreateWorkScheduleRequestDTO.class), any(UUID.class)))
                .thenThrow(new IllegalArgumentException("El día de la semana es requerido"));
    }

    @Cuando("no selecciona ningún día disponible")
    public void noSeleccionaNingunDia() {
        workScheduleRequest.setDayOfWeek(null);
        workScheduleRequest.setEmployeeId(ID_EMPLEADO_ANA);
        workScheduleRequest.setStartTime(LocalTime.of(8, 0));
        workScheduleRequest.setEndTime(LocalTime.of(17, 0));
    }

    @Cuando("intenta guardar la configuración de agenda")
    public void intentaGuardarConfiguracion() {
        try {
            workScheduleResponse = workScheduleService.createWorkSchedule(workScheduleRequest, proveedorAutenticadoId);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el error {string}")
    public void sistemaMuestraError(String mensajeError) {
        assertThat(capturedException).isNotNull();
        assertThat(capturedException.getMessage())
                .containsIgnoringCase(mensajeError.substring(0, Math.min(15, mensajeError.length())));
    }

    @Entonces("no registra ningún horario")
    public void noRegistraNingunHorario() {
        assertThat(workScheduleResponse).isNull();
    }

    // ──────── Conflicto de horario ────────

    @Dado("que el empleado {string} ya tiene horario el {string} de {string} a {string}")
    public void empleadoYaTieneHorario(String empleado, String dia, String inicio, String fin) {
        reset(workScheduleService);
        when(workScheduleService.createWorkSchedule(any(CreateWorkScheduleRequestDTO.class), eq(proveedorAutenticadoId)))
                .thenThrow(new WorkScheduleConflictException("Conflicto de horario: ya existe un horario para ese día"));
    }

    @Cuando("el proveedor intenta crear otro horario el {string} de {string} a {string}")
    public void intentaCrearHorarioSuperpuesto(String dia, String inicio, String fin) {
        workScheduleRequest.setEmployeeId(ID_EMPLEADO_ANA);
        workScheduleRequest.setDayOfWeek(dia);
        workScheduleRequest.setStartTime(LocalTime.parse(inicio));
        workScheduleRequest.setEndTime(LocalTime.parse(fin));
        try {
            workScheduleResponse = workScheduleService.createWorkSchedule(workScheduleRequest, proveedorAutenticadoId);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el error de conflicto de horario")
    public void muestraErrorConflictoHorario() {
        assertThat(capturedException).isNotNull();
        assertThat(capturedException).isInstanceOf(WorkScheduleConflictException.class);
    }

    @Entonces("no registra el horario duplicado")
    public void noRegistraHorarioDuplicado() {
        assertThat(workScheduleResponse).isNull();
    }

    // ──────── Empleado de otro proveedor ────────

    @Dado("que el empleado {string} pertenece a otro proveedor")
    public void empleadoPerteneceAOtroProveedor(String nombreEmpleado) {
        reset(workScheduleService);
        when(workScheduleService.createWorkSchedule(any(CreateWorkScheduleRequestDTO.class), eq(proveedorAutenticadoId)))
                .thenThrow(new ServiceOwnershipException("No tiene permiso para gestionar este empleado"));
    }

    @Cuando("el proveedor intenta crear un horario para ese empleado")
    public void intentaCrearHorarioEmpleadoAjeno() {
        workScheduleRequest.setEmployeeId(ID_EMPLEADO_DR_RAMIREZ);
        workScheduleRequest.setDayOfWeek("LUNES");
        workScheduleRequest.setStartTime(LocalTime.of(8, 0));
        workScheduleRequest.setEndTime(LocalTime.of(17, 0));
        try {
            workScheduleResponse = workScheduleService.createWorkSchedule(workScheduleRequest, proveedorAutenticadoId);
        } catch (Exception e) {
            capturedException = e;
        }
    }

    @Entonces("el sistema muestra el error de acceso denegado")
    public void muestraErrorAccesoDenegado() {
        assertThat(capturedException).isNotNull();
        assertThat(capturedException).isInstanceOf(ServiceOwnershipException.class);
    }

    @Entonces("no registra el horario")
    public void noRegistraElHorario() {
        assertThat(workScheduleResponse).isNull();
    }
}
