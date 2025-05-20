package com.clinic.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;

@Entity
@Table(name = "patients")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE patients SET is_active = false WHERE id=?")
@Where(clause = "is_active=true")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{10,20}$",
            message = "Invalid phone number format")
    @Column(nullable = false, length = 20)
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "Address is required")
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "street", length = 100)),
            @AttributeOverride(name = "city", column = @Column(name = "city", length = 50)),
            @AttributeOverride(name = "state", column = @Column(name = "state", length = 50)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "postal_code", length = 20)),
            @AttributeOverride(name = "country", column = @Column(name = "country", length = 50))
    })
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender = Gender.UNSPECIFIED;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotBlank(message = "Emergency contact is required")
    @Column(name = "emergency_contact", nullable = false, length = 100)
    private String emergencyContact;

    @NotBlank(message = "Emergency phone is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{10,20}$")
    @Column(name = "emergency_phone", nullable = false, length = 20)
    private String emergencyPhone;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        MALE, FEMALE, OTHER, UNSPECIFIED
    }

    @Embeddable
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Address {
        @NotBlank
        @Size(max = 100)
        private String street;

        @NotBlank
        @Size(max = 50)
        private String city;

        @NotBlank
        @Size(max = 50)
        private String state;

        @NotBlank
        @Size(max = 20)
        @Column(name = "postal_code")
        private String postalCode;

        @NotBlank
        @Size(max = 50)
        private String country;
    }

    // Business logic methods
    public int calculateAge() {
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isMinor() {
        return calculateAge() < 18;
    }

    public boolean hasValidEmergencyInfo() {
        return emergencyContact != null && !emergencyContact.isBlank() &&
                emergencyPhone != null && emergencyPhone.matches("^\\+?[0-9\\-\\s()]{10,20}$");
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Patient patient = (Patient) o;
        return getId() != null && Objects.equals(getId(), patient.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}