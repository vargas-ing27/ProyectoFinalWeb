package proyecto.barberos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "barber_profiles")
public class BarberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELACIÓN IMPORTANTE ---
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "shop_name")
    private String shopName; // Nombre de la barbería

    @Column(columnDefinition = "TEXT")
    private String bio; // Descripción

    @Column(name = "profile_image_url")
    private String profileImageUrl; // URL Foto

    // --- NUEVO: AQUÍ GUARDAMOS EL TELÉFONO AHORA ---
    private String phone;

    // --- NUEVOS CAMPOS PARA EL MAPA ---
    private Double latitude;
    private Double longitude;

    // --- CONSTRUCTORES ---

    public BarberProfile() {
    }

    // Constructor actualizado con el campo 'phone'
    public BarberProfile(User user, String shopName, String bio, String address, String profileImageUrl, String phone) {
        this.user = user;
        this.shopName = shopName;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // --- NUEVOS GETTER Y SETTER PARA TELÉFONO ---
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // --- NUEVOS GETTERS Y SETTERS ---
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}