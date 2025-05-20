package com.clinic.api.controllers;

import com.clinic.api.models.Medication;
import com.clinic.api.services.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medications")

public class MedicationController {
    @Autowired
    private MedicationService medicationService;

    @GetMapping
    public Page<Medication> getAllMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return medicationService.getAllMedications(page, size);
    }

    @GetMapping("/{id}")
    public Medication getMedicationById(@PathVariable Long id) {
        return medicationService.getMedicationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Medication createMedication(@Valid @RequestBody Medication medication) {
        return medicationService.saveMedication(medication);
    }

    @PutMapping("/{id}")
    public Medication updateMedication(@PathVariable Long id,
                                       @Valid @RequestBody Medication medication) {
        medication.setId(id); // Ensure ID matches path variable
        return medicationService.saveMedication(medication);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedication(@PathVariable Long id) {
        medicationService.deleteMedication(id);
    }
}
