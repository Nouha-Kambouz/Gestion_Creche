package com.example.gestion_creche.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;

@Service
public class AgeValidationService {
    private static final int MIN_AGE_MONTHS = 3;
    private static final int MAX_AGE_YEARS = 6;

    public boolean isAgeValid(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }

        LocalDate now = LocalDate.now();
        Period age = Period.between(birthDate, now);
        
        // Convert age to months for minimum age check
        int ageInMonths = age.getYears() * 12 + age.getMonths();
        
        // Check if age is between 3 months and 6 years
        return ageInMonths >= MIN_AGE_MONTHS && age.getYears() <= MAX_AGE_YEARS;
    }

    public String getValidationMessage() {
        return String.format("Child's age must be between %d months and %d years", MIN_AGE_MONTHS, MAX_AGE_YEARS);
    }
} 