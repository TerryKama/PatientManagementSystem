package com.clinic.api.services;

import com.clinic.api.models.Doctor;
import com.clinic.api.models.Doctor.Specialization;
import com.clinic.api.repositories.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByIsActiveTrue();
    }

    public Optional<Doctor> getDoctorById(Integer id) {
        return doctorRepository.findById(id);
    }

    public Doctor getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with email: " + email));
    }

    public Doctor createDoctor(Doctor doctor) {
        validateDoctor(doctor);
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        if (doctor.getLicenseNumber() != null &&
                doctorRepository.existsByLicenseNumber(doctor.getLicenseNumber())) {
            throw new BusinessException("License number already exists");
        }
        doctor.setActive(true);
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Integer id, Doctor doctorDetails) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (!existingDoctor.getEmail().equals(doctorDetails.getEmail()) &&
                doctorRepository.existsByEmail(doctorDetails.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        existingDoctor.setName(doctorDetails.getName());
        existingDoctor.setSpecialization(doctorDetails.getSpecialization());
        existingDoctor.setEmail(doctorDetails.getEmail());
        existingDoctor.setPhone(doctorDetails.getPhone());
        existingDoctor.setLicenseNumber(doctorDetails.getLicenseNumber());

        return doctorRepository.save(existingDoctor);
    }

    public void deactivateDoctor(Integer id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctor.setActive(false);
        doctorRepository.save(doctor);
    }

    public void deleteDoctor(Integer id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (hasUpcomingAppointments(id)) {
            throw new BusinessException("Cannot delete doctor with upcoming appointments");
        }

        doctorRepository.delete(doctor);
    }

    public List<Doctor> getDoctorsBySpecialization(Specialization specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<Doctor> searchDoctors(String name, Specialization specialization, Boolean isActive) {
        return doctorRepository.searchDoctors(name, specialization, isActive);
    }

    public long countDoctorsBySpecialization(Specialization specialization) {
        return doctorRepository.countBySpecialization(specialization);
    }

    private boolean hasUpcomingAppointments(Integer doctorId) {
        return doctorRepository.hasUpcomingAppointments(doctorId);
    }

    private void validateDoctor(Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            throw new BusinessException("Doctor name is required");
        }
        if (doctor.getSpecialization() == null) {
            throw new BusinessException("Specialization is required");
        }
        if (doctor.getEmail() == null || !doctor.getEmail().contains("@")) {
            throw new BusinessException("Valid email is required");
        }
    }
}