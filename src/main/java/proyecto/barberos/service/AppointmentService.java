package proyecto.barberos.service;

import org.springframework.beans.factory.annotation.Autowired;


import proyecto.barberos.entity.Appointment;
import proyecto.barberos.entity.Availability;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.User;
// Importamos tu entidad Service explícitamente
import proyecto.barberos.entity.Service; 

import proyecto.barberos.repository.AppointmentRepository;
import proyecto.barberos.repository.AvailabilityRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service 
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    // --- GUARDAR CITA ---
    public void agendarCita(User cliente, BarberProfile barbero, proyecto.barberos.entity.Service servicio, LocalDateTime fechaHora) {
        Appointment cita = new Appointment();
        cita.setClient(cliente);
        cita.setBarber(barbero);
        cita.setService(servicio);
        cita.setAppointmentDate(fechaHora);
        cita.setStatus("CONFIRMED"); 
        
        appointmentRepository.save(cita);
    }

   // --- MODIFICADO: CANCELAR CITA (Oculta automáticamente al que cancela) ---
    public void cancelarCita(Long citaId, String motivo, User quienCancela) throws Exception {
        Optional<Appointment> citaOpt = appointmentRepository.findById(citaId);
        
        if (citaOpt.isPresent()) {
            Appointment cita = citaOpt.get();
            
            // Regla: Cancelar con 1 hora de anticipación
            LocalDateTime horaLimite = LocalDateTime.now().plusHours(1);
            
            if (horaLimite.isAfter(cita.getAppointmentDate())) {
                // AQUI ESTA EL CAMBIO: Mensaje según el rol
                if ("BARBER".equals(quienCancela.getRole())) {
                    throw new Exception("Lo siento, es demasiado tarde para cancelar esta cita, contactate directamente con el cliente.");
                } else {
                    throw new Exception("Lo siento, es demasiado tarde para cancelar esta cita, contactate directamente con el barbero");
                }
            }

            cita.setStatus("CANCELLED");
            cita.setCancellationReason(motivo);
            
            // Ocultar cita automáticamente para quien cancela
            if ("CLIENT".equals(quienCancela.getRole())) {
                cita.setVisibleToClient(false);
            } else if ("BARBER".equals(quienCancela.getRole())) {
                cita.setVisibleToBarber(false);
            }

            appointmentRepository.save(cita);
        } else {
            throw new Exception("Cita no encontrada");
        }
    }

    // --- NUEVO: OCULTAR CITA MANUALMENTE (Botón X) ---
    public void ocultarCita(Long citaId, User quienOculta) {
        Optional<Appointment> citaOpt = appointmentRepository.findById(citaId);
        if (citaOpt.isPresent()) {
            Appointment cita = citaOpt.get();
            
            if ("CLIENT".equals(quienOculta.getRole())) {
                cita.setVisibleToClient(false);
            } else if ("BARBER".equals(quienOculta.getRole())) {
                cita.setVisibleToBarber(false);
            }
            appointmentRepository.save(cita);
        }
    }

    // --- LÓGICA MAESTRA: CALCULAR HUECOS ---
    public List<LocalTime> obtenerHorasDisponibles(BarberProfile barbero, LocalDate fecha, int duracionServicioMinutos) {
        List<LocalTime> huecosLibres = new ArrayList<>();

        if (fecha.isBefore(LocalDate.now())) {
            return huecosLibres;
        }

        int diaSemana = fecha.getDayOfWeek().getValue(); 
        List<Availability> horarios = availabilityRepository.findByBarberIdOrderByDayOfWeekAsc(barbero.getId());
        
        Availability horarioDia = horarios.stream()
                .filter(h -> h.getDayOfWeek() == diaSemana && h.isActive())
                .findFirst()
                .orElse(null);

        if (horarioDia == null) return huecosLibres;

        LocalDateTime inicioDia = fecha.atTime(horarioDia.getStartTime());
        LocalDateTime finDia = fecha.atTime(horarioDia.getEndTime());
        
        // OJO: Solo nos importan las citas que NO estén canceladas para calcular espacio
        List<Appointment> todasLasCitas = appointmentRepository.findByBarberIdAndDateRange(barbero.getId(), inicioDia, finDia);
        List<Appointment> citasOcupadas = new ArrayList<>();
        for(Appointment a : todasLasCitas) {
            if(!"CANCELLED".equals(a.getStatus())) {
                citasOcupadas.add(a);
            }
        }

        LocalTime horaActual = horarioDia.getStartTime();
        LocalTime horaCierre = horarioDia.getEndTime();

        while (!horaActual.plusMinutes(duracionServicioMinutos).isAfter(horaCierre)) {
            
            boolean esHoraPasada = false;
            boolean ocupado = false;

            if (fecha.isEqual(LocalDate.now()) && horaActual.isBefore(LocalTime.now())) {
                esHoraPasada = true;
            }

            if (!esHoraPasada) {
                LocalDateTime posibleInicio = fecha.atTime(horaActual);
                LocalDateTime posibleFin = posibleInicio.plusMinutes(duracionServicioMinutos);

                for (Appointment cita : citasOcupadas) {
                    LocalDateTime citaInicio = cita.getAppointmentDate();
                    LocalDateTime citaFin = citaInicio.plusMinutes(cita.getService().getDurationMinutes());

                    if (posibleInicio.isBefore(citaFin) && posibleFin.isAfter(citaInicio)) {
                        ocupado = true;
                        break;
                    }
                }
            }

            if (!ocupado && !esHoraPasada) {
                huecosLibres.add(horaActual);
            }

            horaActual = horaActual.plusMinutes(duracionServicioMinutos);
        }

        return huecosLibres;
    }
    
    // --- ACTUALIZA ESTAS LLAMADAS EN LOS MÉTODOS DE ABAJO ---
   public List<Appointment> obtenerCitasPorCliente(Long clientId) {
        // Calculamos la hora de corte: "Hace 5 minutos"
        LocalDateTime horaCorte = LocalDateTime.now().minusMinutes(5);
        
        return appointmentRepository.findByClientIdAndVisibleToClientTrueAndAppointmentDateAfterOrderByAppointmentDateDesc(clientId, horaCorte);
    }
    
    public List<Appointment> obtenerCitasPorBarbero(Long barberId) {
        // Calculamos la hora de corte: "Hace 5 minutos"
        LocalDateTime horaCorte = LocalDateTime.now().minusMinutes(5);
        
        return appointmentRepository.findByBarberIdAndVisibleToBarberTrueAndAppointmentDateAfterOrderByAppointmentDateDesc(barberId, horaCorte);
    }
}