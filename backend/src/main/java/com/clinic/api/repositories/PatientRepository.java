package com.clinic.api.repositories;

import com.clinic.api.models.Patient;
import com.clinic.api.models.Patient.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    // Find patient by exact email match
    Optional<Patient> findByEmail(String email);

    // Search patients by name (case-insensitive)
    List<Patient> findByFullNameContainingIgnoreCase(String name);

    // Find patients by gender
    List<Patient> findByGender(Gender gender);

    // Find active patients
    List<Patient> findByIsActiveTrue();

    // Find patients born between dates
    List<Patient> findByDateOfBirthBetween(LocalDate startDate, LocalDate endDate);

    // Find patients with upcoming appointments
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.appointments a " +
            "WHERE a.appointmentDate > CURRENT_DATE " +
            "AND a.status = 'SCHEDULED'")
    List<Patient> findPatientsWithUpcomingAppointments();

    // Advanced search with multiple optional filters
    @Query("SELECT p FROM Patient p WHERE " +
            "(:name IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:gender IS NULL OR p.gender = :gender) AND " +
            "(:minAge IS NULL OR FUNCTION('YEAR', FUNCTION('AGE', p.dateOfBirth)) >= :minAge) AND " +
            "(:maxAge IS NULL OR FUNCTION('YEAR', FUNCTION('AGE', p.dateOfBirth)) <= :maxAge) AND " +
            "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Patient> searchPatients(
            @Param("name") String name,
            @Param("gender") Gender gender,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    // Find patients by blood type
    List<Patient> findByBloodType(String bloodType);

    // Count patients by gender
    long countByGender(Gender gender);

    // Check if patient exists by email or phone
    boolean existsByEmailOrPhone(String email, String phone);

    // Find patients with specific medication allergies
    @Query("SELECT p FROM Patient p JOIN p.allergies a " +
            "WHERE LOWER(a) LIKE LOWER(CONCAT('%', :allergy, '%'))")
    List<Patient> findByAllergyContaining(@Param("allergy") String allergy);

    // Find patients due for annual checkup
    @Query("SELECT p FROM Patient p WHERE " +
            "(p.lastCheckupDate IS NULL OR p.lastCheckupDate < :cutoffDate) " +
            "AND p.isActive = true")
    List<Patient> findPatientsDueForCheckup(@Param("cutoffDate") LocalDate cutoffDate);
}
