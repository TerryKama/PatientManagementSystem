package com.clinic.api.services;

import com.clinic.api.models.Appointment;
import com.clinic.api.models.Appointment.Status;
import com.clinic.api.repositories.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Integer id) {
        return appointmentRepository.findById(id);
    }

    public Appointment createAppointment(Appointment appointment) {
        validateAppointmentTime(appointment.getAppointmentDate());
        checkForConflicts(appointment);
        appointment.setStatus(Status.SCHEDULED);
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Integer id, Appointment appointmentDetails) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        if (!existingAppointment.getStatus().equals(Status.CANCELLED)) {
            validateAppointmentTime(appointmentDetails.getAppointmentDate());
            checkForConflicts(appointmentDetails);

            existingAppointment.setAppointmentDate(appointmentDetails.getAppointmentDate());
            existingAppointment.setPatient(appointmentDetails.getPatient());
            existingAppointment.setDoctor(appointmentDetails.getDoctor());
            existingAppointment.setStatus(appointmentDetails.getStatus());

            return appointmentRepository.save(existingAppointment);
        }
        throw new IllegalStateException("Cannot update a cancelled appointment");
    }

    public void cancelAppointment(Integer id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));

        if (appointment.getAppointmentDate().isAfter(LocalDateTime.now().plusHours(2))) {
            appointment.setStatus(Status.CANCELLED);
            appointmentRepository.save(appointment);
        } else {
            throw new IllegalStateException("Appointments can only be cancelled at least 2 hours in advance");
        }
    }

    public void deleteAppointment(Integer id) {
        if (!appointmentRepository.existsById(id)) {
            throw new RuntimeException("Appointment not found with id: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    public List<Appointment> getAppointmentsByPatientId(Integer patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository.findByAppointmentDateAfterAndStatus(LocalDateTime.now(), Status.SCHEDULED);
    }

    public List<Appointment> getAppointmentsByDoctorAndDateRange(Integer doctorId, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDoctorIdAndAppointmentDateBetween(doctorId, start, end);
    }

    private void validateAppointmentTime(LocalDateTime appointmentTime) {
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment time cannot be in the past");
        }

        if (appointmentTime.getHour() < 8 || appointmentTime.getHour() > 17) {
            throw new IllegalArgumentException("Appointments must be scheduled between 8AM and 5PM");
        }
    }

    private void checkForConflicts(Appointment appointment) {
        boolean conflictExists = appointmentRepository.existsConflictingAppointment(
                appointment.getPatient().getId(),
                appointment.getAppointmentDate().minusMinutes(30),
                appointment.getAppointmentDate().plusMinutes(30));

        if (conflictExists) {
            throw new IllegalStateException("Patient has a conflicting appointment within 30 minutes");
        }
    }

    public long countCompletedAppointments() {
        return appointmentRepository.countByStatus(Status.COMPLETED);
    }
}
