package com.example.gestion_creche.service;

import com.example.gestion_creche.model.Child;
import com.example.gestion_creche.model.User;
import com.example.gestion_creche.repository.ChildRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
public class ChildService {
    private final ChildRepository childRepository;
    private final AgeValidationService ageValidationService;

    public ChildService(ChildRepository childRepository, AgeValidationService ageValidationService) {
        this.childRepository = childRepository;
        this.ageValidationService = ageValidationService;
    }

    @Transactional
    public Child saveChild(Child child) {
        // Validate age before saving
        if (!ageValidationService.isAgeValid(child.getBirthDate())) {
            throw new IllegalArgumentException(ageValidationService.getValidationMessage());
        }

        if (child.getId() == null) {
            // New child, ensure it's active by default
            child.setActive(true);
        }
        return childRepository.save(child);
    }

    public List<Child> getChildrenByParent(User parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }
        return childRepository.findByParent(parent);
    }

    public Child getChildById(Long id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Child not found with id: " + id));
    }

    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    public List<Child> getActiveChildren() {
        return childRepository.findByActiveTrue();
    }

    public List<Child> getArchivedChildren() {
        return childRepository.findByActiveFalse();
    }

    public long getTotalChildrenCount() {
        return childRepository.count();
    }

    public long getActiveChildrenCount() {
        return childRepository.countByActiveTrue();
    }

    public long getArchivedChildrenCount() {
        return childRepository.countByActiveFalse();
    }

    @Transactional
    public void archiveChild(Long id) {
        Child child = getChildById(id);
        if (!child.isActive()) {
            throw new IllegalStateException("Child is already archived");
        }
        child.setActive(false);
        childRepository.save(child);
    }

    @Transactional
    public void restoreChild(Long id) {
        Child child = getChildById(id);
        if (child.isActive()) {
            throw new IllegalStateException("Child is already active");
        }
        
        // Validate age before restoring
        if (!ageValidationService.isAgeValid(child.getBirthDate())) {
            throw new IllegalStateException(ageValidationService.getValidationMessage());
        }
        
        child.setActive(true);
        childRepository.save(child);
    }

    @Transactional
    public void deleteChild(Long id) {
        Child child = getChildById(id);
        childRepository.delete(child);
    }

    // Utility method to calculate exact age
    public Period calculateAge(Child child) {
        if (child == null || child.getBirthDate() == null) {
            throw new IllegalArgumentException("Child or birth date cannot be null");
        }
        return Period.between(child.getBirthDate(), LocalDate.now());
    }

    // Utility method to check if child's age is within valid range
    public boolean isAgeValid(Child child) {
        if (child == null || child.getBirthDate() == null) {
            return false;
        }
        return ageValidationService.isAgeValid(child.getBirthDate());
    }

    // Get children by age range
    public List<Child> getChildrenByAgeRange(int minMonths, int maxMonths) {
        LocalDate now = LocalDate.now();
        LocalDate maxDate = now.minusMonths(minMonths);
        LocalDate minDate = now.minusMonths(maxMonths);
        return childRepository.findByBirthDateBetween(minDate, maxDate);
    }

    @Transactional
    public Child updateChild(Long id, Child updatedChild) {
        Child existingChild = getChildById(id);
        
        // Update fields
        existingChild.setFirstName(updatedChild.getFirstName());
        existingChild.setLastName(updatedChild.getLastName());
        existingChild.setBirthDate(updatedChild.getBirthDate());
        existingChild.setAllergies(updatedChild.getAllergies());
        existingChild.setMedicalNotes(updatedChild.getMedicalNotes());
        existingChild.setParent(updatedChild.getParent());
        existingChild.setActive(updatedChild.isActive());
        
        return childRepository.save(existingChild);
    }
}
