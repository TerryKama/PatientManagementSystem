package com.clinic.api.services;

import com.clinic.api.models.Medication;
import com.clinic.api.models.Medication.Form;
import com.clinic.api.repositories.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional

public class MedicationService {
    private final MedicationRepository medicationRepository;

    @Autowired
    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> getAllMedications() {
        return medicationRepository.findAll();
    }

    public List<Medication> getActiveMedications() {
        return medicationRepository.findByIsActiveTrue();
    }

    public Optional<Medication> getMedicationById(Integer id) {
        return medicationRepository.findById(id);
    }

    public Medication getMedicationByNameAndDosage(String name, String dosage) {
        return medicationRepository.findByNameAndDosage(name, dosage)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medication not found with name: " + name + " and dosage: " + dosage));
    }

    public Medication createMedication(Medication medication) {
        validateMedication(medication);

        if (medicationRepository.existsByNameAndDosage(medication.getName(), medication.getDosage())) {
            throw new BusinessException("Medication with this name and dosage already exists");
        }

        medication.setActive(true);
        return medicationRepository.save(medication);
    }

    public Medication updateMedication(Integer id, Medication medicationDetails) {
        Medication existingMedication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        if (!existingMedication.getName().equals(medicationDetails.getName()) ||
                !existingMedication.getDosage().equals(medicationDetails.getDosage())) {
            if (medicationRepository.existsByNameAndDosage(
                    medicationDetails.getName(), medicationDetails.getDosage())) {
                throw new BusinessException("Medication with this name and dosage already exists");
            }
        }

        existingMedication.setName(medicationDetails.getName());
        existingMedication.setDosage(medicationDetails.getDosage());
        existingMedication.setForm(medicationDetails.getForm());
        existingMedication.setInstructions(medicationDetails.getInstructions());
        existingMedication.setCategory(medicationDetails.getCategory());

        return medicationRepository.save(existingMedication);
    }

    public void deactivateMedication(Integer id) {
        Medication medication = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));
        medication.setActive(false);
        medicationRepository.save(medication);
    }

    public List<Medication> getMedicationsByForm(Form form) {
        return medicationRepository.findByForm(form);
    }

    public List<Medication> searchMedications(String name, Form form, Boolean isActive) {
        return medicationRepository.searchMedications(name, form, isActive);
    }

    public List<Medication> getPrescriptionMedications() {
        return medicationRepository.findPrescriptionMedications();
    }

    public List<Medication> getLowStockMedications() {
        return medicationRepository.findMedicationsBelowReorderLevel();
    }

    public long countMedicationsByForm(Form form) {
        return medicationRepository.countByForm(form);
    }

    private void validateMedication(Medication medication) {
        if (medication.getName() == null || medication.getName().trim().isEmpty()) {
            throw new BusinessException("Medication name is required");
        }
        if (medication.getDosage() == null || medication.getDosage().trim().isEmpty()) {
            throw new BusinessException("Dosage is required");
        }
        if (medication.getForm() == null) {
            throw new BusinessException("Form is required");
        }
    }
}
