package proyecto.barberos.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
// NO importamos la anotación Service de Spring aquí para evitar el conflicto de nombres

import proyecto.barberos.entity.Appointment;
import proyecto.barberos.entity.BarberProfile;
import proyecto.barberos.entity.Service; // <--- Importamos TU entidad correctamente

import proyecto.barberos.repository.AppointmentRepository;
import proyecto.barberos.repository.ServiceRepository;

import java.util.List;

// Usamos el nombre completo de la anotación para diferenciarla de tu entidad 'Service'
@org.springframework.stereotype.Service 
public class ServiciosService {

    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    // Guardar un nuevo servicio
    public void guardarServicio(Service servicio, BarberProfile barbero) {
        servicio.setBarber(barbero); 
        serviceRepository.save(servicio);
    }

    // Listar servicios de un barbero
    public List<Service> obtenerServiciosPorBarbero(Long barberId) {
        return serviceRepository.findByBarberId(barberId);
    }

    // Eliminar servicio y su historial (Cascada manual)
    @Transactional 
    public void eliminarServicio(Long serviceId) {
        // 1. Buscamos si hay citas con este servicio
        List<Appointment> citasDelServicio = appointmentRepository.findByServiceId(serviceId);
        
        // 2. Si hay citas, las borramos primero
        if (!citasDelServicio.isEmpty()) {
            appointmentRepository.deleteAll(citasDelServicio);
        }
        
        // 3. Ahora borramos el servicio
        serviceRepository.deleteById(serviceId);
    }
}