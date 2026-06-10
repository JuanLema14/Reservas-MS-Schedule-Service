#language: es
@Agenda @HU04
Característica: Configuración de agenda y horarios
  Como proveedor de servicios
  Quiero definir mi agenda y horarios disponibles
  Para que los clientes puedan reservar en los tiempos que tengo disponibles

  Antecedentes:
    Dado que el proveedor está autenticado como "salon@bellavida.com"

  @CP-04-001 @HappyPath
  Escenario: Creación exitosa de horario laboral para un empleado
    Dado que el empleado "Ana Estilista" pertenece al proveedor autenticado
    Cuando el proveedor accede a la sección "Gestión de Agenda"
    Y selecciona el día disponible "LUNES"
    Y define el horario de inicio "08:00" y fin "17:00"
    Y establece la duración por cita de 60 minutos
    Y guarda la configuración de agenda
    Entonces el sistema registra el horario laboral exitosamente
    Y los clientes pueden ver ese horario al momento de reservar
    Y el código de respuesta HTTP es 201

  @CP-04-002 @HappyPath @BloqueoFecha
  Escenario: Bloqueo de un día específico del calendario
    Dado que el empleado "Ana Estilista" tiene configurada su agenda
    Cuando el proveedor selecciona la fecha "2026-08-01"
    Y el tipo de bloqueo es "VACACIONES"
    Y confirma el bloqueo del día
    Entonces el sistema marca ese día como no disponible
    Y no permite reservas para esa fecha
    Y el código de respuesta HTTP es 201

  @CP-04-003 @Error @SinDias
  Escenario: Intento de guardar agenda sin días seleccionados
    Dado que el proveedor está en la sección "Gestión de Agenda"
    Cuando no selecciona ningún día disponible
    Y intenta guardar la configuración de agenda
    Entonces el sistema muestra el error "El día de la semana es requerido"
    Y no registra ningún horario

  @CP-04-004 @Error @HorarioConflicto
  Escenario: Conflicto de horario al crear un segundo horario superpuesto
    Dado que el empleado "Ana Estilista" ya tiene horario el "LUNES" de "08:00" a "17:00"
    Cuando el proveedor intenta crear otro horario el "LUNES" de "10:00" a "14:00"
    Entonces el sistema muestra el error de conflicto de horario
    Y no registra el horario duplicado

  @CP-04-005 @Error @EmpleadoNoPertenece
  Escenario: Intento de configurar agenda de empleado de otro proveedor
    Dado que el empleado "Dr. Ramírez" pertenece a otro proveedor
    Cuando el proveedor intenta crear un horario para ese empleado
    Entonces el sistema muestra el error de acceso denegado
    Y no registra el horario
