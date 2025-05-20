package com.clinic.api.services;

import com.clinic.api.models.Patient;
import com.clinic.api.models.Patient.Gender;
import com.clinic.api.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class PatientService {
    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> getActivePatients() {
        return patientRepository.findByIsActiveTrue();
    }

    public Optional<Patient> getPatientById(Integer id) {
        return patientRepository.findById(id);
    }

    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with email: " + email));
    }

    public Patient createPatient(Patient patient) {
        validatePatient(patient);

        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new BusinessException("Email already registered to another patient");
        }

        if (patientRepository.existsByPhone(patient.getPhone())) {
            throw new BusinessException("Phone number already registered to another patient");
        }

        patient.setActive(true);
        return patientRepository.save(patient);
    }

    public Patient updatePatient(Integer id, Patient patientDetails) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        if (!existingPatient.getEmail().equals(patientDetails.getEmail()) &&
                patientRepository.existsByEmail(patientDetails.getEmail())) {
            throw new BusinessException("Email already registered to another patient");
        }

        existingPatient.setFullName(patientDetails.getFullName());
        existingPatient.setEmail(patientDetails.getEmail());
        existingPatient.setPhone(patientDetails.getPhone());
        existingPatient.setDateOfBirth(patientDetails.getDateOfBirth());
        existingPatient.setGender(patientDetails.getGender());
        existingPatient.setAddress(patientDetails.getAddress());
        existingPatient.setBloodType(patientDetails.getBloodType());

        return patientRepository.save(existingPatient);
    }

    public void deactivatePatient(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
        patient.setActive(false);
        patientRepository.save(patient);
    }

    public void deletePatient(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        if (hasActiveAppointments(id)) {
            throw new BusinessException("Cannot delete patient with active appointments");
        }

        patientRepository.delete(patient);
    }

    public List<Patient> searchPatients(String name, Gender gender, LocalDate minBirthDate, LocalDate maxBirthDate) {
        return patientRepository.searchPatients(
                name,
                gender,
                minBirthDate,
                maxBirthDate
        );
    }

    public List<Patient> getPatientsByBloodType(String bloodType) {
        return patientRepository.findByBloodType(bloodType);
    }

    public long countPatientsByGender(Gender gender) {
        return patientRepository.countByGender(gender);
    }

    private boolean hasActiveAppointments(Integer patientId) {
        return patientRepository.hasActiveAppointments(patientId);
    }

    private void validatePatient(Patient patient) {
        if (patient.getFullName() == null || patient.getFullName().trim().isEmpty()) {
            throw new BusinessException("Patient name is required");
        }
        if (patient.getEmail() == null || !patient.getEmail().contains("@")) {
            throw new BusinessException("Valid email is required");
        }
        if (patient.getDateOfBirth() == null || patient.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new BusinessException("Valid date of birth is required");
        }
    }
}
