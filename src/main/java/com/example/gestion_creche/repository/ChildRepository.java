package com.example.gestion_creche.repository;

import com.example.gestion_creche.model.Child;
import com.example.gestion_creche.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByParent(User parent);
    List<Child> findByParentAndActiveTrue(User parent);
    List<Child> findByActiveTrue();
    List<Child> findByActiveFalse();
    long countByActiveTrue();
    long countByActiveFalse();
    List<Child> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);
}