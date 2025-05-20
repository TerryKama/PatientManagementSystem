package com.clinic.api.repositories;

import com.clinic.api.models.Appointment;
import com.clinic.api.models.Appointment.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface AppointmentRepository extends JpaRepository<Appointment,Integer> {
    // Find appointments by patient ID
    List<Appointment> findByPatientId(Integer patientId);

    // Find appointments by doctor ID
    List<Appointment> findByDoctorId(Integer doctorId);

    // Find appointments by status
    List<Appointment> findByStatus(Status status);

    // Find upcoming appointments for a patient
    List<Appointment> findByPatientIdAndAppointmentDateAfter(Integer patientId, LocalDateTime date);

    // Find appointments between date range
    List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

    // Find appointments by doctor and status
    List<Appointment> findByDoctorIdAndStatus(Integer doctorId, Status status);

    // Custom query with join
    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient p JOIN FETCH a.doctor d " +
            "WHERE a.appointmentDate BETWEEN :start AND :end AND a.status = :status")
    List<Appointment> findDetailedAppointmentsInRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") Status status);

    // Count appointments by status
    long countByStatus(Status status);

    // Check if patient has conflicting appointment
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND a.appointmentDate BETWEEN :start AND :end " +
            "AND a.status NOT IN (com.clinic.api.models.Appointment.Status.CANCELLED)")
    boolean existsConflictingAppointment(
            @Param("patientId") Integer patientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query(value = "SELECT d.name, COUNT(a.id) FROM appointments a " +
            "JOIN doctors d ON a.doctor_id = d.id " +
            "WHERE a.status = 'COMPLETED' AND a.appointment_date BETWEEN :start AND :end " +
            "GROUP BY d.name", nativeQuery = true)
    List<Object[]> findCompletedAppointmentsByDoctor(@Param("start") LocalDate start,
                                                     @Param("end") LocalDate end);

}
