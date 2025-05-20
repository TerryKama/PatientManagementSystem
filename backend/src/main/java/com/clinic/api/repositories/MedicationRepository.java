package com.clinic.api.repositories;

import com.clinic.api.models.Medication;
import com.clinic.api.models.Medication.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Integer> {
    // Find medications by name (case-insensitive)
    List<Medication> findByNameContainingIgnoreCase(String name);

    // Find active medications
    List<Medication> findByIsActiveTrue();

    // Find medications by form (tablet, liquid, etc.)
    List<Medication> findByForm(Form form);

    // Find medications requiring prescription
    @Query("SELECT m FROM Medication m WHERE m.category.type <> 'OTC'")
    List<Medication> findPrescriptionMedications();

    // Find medications by category
    List<Medication> findByCategoryId(Integer categoryId);

    // Search medications with multiple optional filters
    @Query("SELECT m FROM Medication m WHERE " +
            "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:form IS NULL OR m.form = :form) AND " +
            "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
            "(:isActive IS NULL OR m.isActive = :isActive)")
    List<Medication> searchMedications(
            @Param("name") String name,
            @Param("form") Form form,
            @Param("categoryId") Integer categoryId,
            @Param("isActive") Boolean isActive);

    // Check if medication exists by name and dosage
    boolean existsByNameAndDosage(String name, String dosage);

    // Find medications with low stock
    @Query("SELECT m FROM Medication m WHERE m.stockQuantity < m.reorderLevel")
    List<Medication> findMedicationsBelowReorderLevel();

    // Count medications by form
    long countByForm(Form form);

    // Find medications frequently prescribed with a given medication
    @Query(value = "SELECT m2.* FROM medications m2 " +
            "JOIN prescription_items pi2 ON m2.id = pi2.medication_id " +
            "JOIN prescriptions p ON pi2.prescription_id = p.id " +
            "WHERE p.id IN (SELECT p.id FROM prescriptions p " +
            "JOIN prescription_items pi ON p.id = pi.prescription_id " +
            "WHERE pi.medication_id = :medicationId) " +
            "AND m2.id <> :medicationId " +
            "GROUP BY m2.id " +
            "ORDER BY COUNT(m2.id) DESC LIMIT 5",
            nativeQuery = true)
    List<Medication> findFrequentlyCoPrescribedMedications(
            @Param("medicationId") Integer medicationId);
}
