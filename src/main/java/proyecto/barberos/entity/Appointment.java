package proyecto.barberos.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "barber_id", nullable = false)
    private BarberProfile barber;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private LocalDateTime appointmentDate;

    // PENDING, CONFIRMED, COMPLETED, CANCELLED
    private String status = "CONFIRMED"; 

    // --- NUEVO CAMPO: Razón de cancelación ---
    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    // NUEVOS CAMPOS DE VISIBILIDAD
    @Column(nullable = false)
    private boolean visibleToClient = true; // Por defecto el cliente la ve

    @Column(nullable = false)
    private boolean visibleToBarber = true; // Por defecto el barbero la ve

    public Appointment() {}

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public BarberProfile getBarber() { return barber; }
    public void setBarber(BarberProfile barber) { this.barber = barber; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isVisibleToClient() { return visibleToClient; }
    public void setVisibleToClient(boolean visibleToClient) { this.visibleToClient = visibleToClient; }

    public boolean isVisibleToBarber() { return visibleToBarber; }
    public void setVisibleToBarber(boolean visibleToBarber) { this.visibleToBarber = visibleToBarber; }
}