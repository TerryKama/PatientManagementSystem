package com.clinic.api.repositories;

import com.clinic.api.models.Doctor;
import com.clinic.api.models.Doctor.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Integer> {
    // Find doctors by specialization
    List<Doctor> findBySpecialization(Specialization specialization);

    // Find active doctors
    List<Doctor> findByIsActiveTrue();

    // Find doctor by email (unique field)
    Optional<Doctor> findByEmail(String email);

    // Find doctors by name containing (case-insensitive)
    List<Doctor> findByNameContainingIgnoreCase(String namePart);

    // Find doctors by license number
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    // Count doctors by specialization
    long countBySpecialization(Specialization specialization);

    // Custom query for doctors with upcoming appointments
    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.appointments a " +
            "WHERE a.appointmentDate > CURRENT_TIMESTAMP " +
            "AND a.status = 'SCHEDULED'")
    List<Doctor> findDoctorsWithUpcomingAppointments();

    // Search doctors by multiple criteria
    @Query("SELECT d FROM Doctor d WHERE " +
            "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:specialization IS NULL OR d.specialization = :specialization) AND " +
            "(:isActive IS NULL OR d.isActive = :isActive)")
    List<Doctor> searchDoctors(
            @Param("name") String name,
            @Param("specialization") Specialization specialization,
            @Param("isActive") Boolean isActive);

    // Find doctors available at a specific time (no appointments)
    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND d.id NOT IN " +
            "(SELECT a.doctor.id FROM Appointment a WHERE " +
            "a.appointmentDate BETWEEN :startTime AND :endTime " +
            "AND a.status <> 'CANCELLED')")
    List<Doctor> findAvailableDoctors(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
