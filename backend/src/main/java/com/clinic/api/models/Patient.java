package com.clinic.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;  // Changed from patient_id to id (JPA convention)

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;  // Added name field

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
    private LocalDate dateOfBirth;  // Changed from String dob to LocalDate

    @Embedded
    private Address address;  // Structured address instead of String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender = Gender.UNSPECIFIED;  // Added default

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // For soft deletion

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    public enum Gender {
        MALE, FEMALE, OTHER, UNSPECIFIED
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state;
        @Column(name = "postal_code")
        private String postalCode;
        private String country;
    }

    // Business logic method
    public int calculateAge() {
        return LocalDate.now().getYear() - this.dateOfBirth.getYear();
    }
}
