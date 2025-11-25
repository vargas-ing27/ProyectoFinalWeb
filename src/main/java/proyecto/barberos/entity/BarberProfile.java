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

    @Column(nullable = false)
    private String address; // Dirección

    @Column(name = "profile_image_url")
    private String profileImageUrl; // URL Foto

    // --- NUEVO: AQUÍ GUARDAMOS EL TELÉFONO AHORA ---
    private String phone;

    // --- CONSTRUCTORES ---

    public BarberProfile() {
    }

    // Constructor actualizado con el campo 'phone'
    public BarberProfile(User user, String shopName, String bio, String address, String profileImageUrl, String phone) {
        this.user = user;
        this.shopName = shopName;
        this.bio = bio;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.phone = phone;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}