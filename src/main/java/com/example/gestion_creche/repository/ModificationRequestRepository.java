package com.example.gestion_creche.repository;

import com.example.gestion_creche.model.ModificationRequest;
import com.example.gestion_creche.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModificationRequestRepository extends JpaRepository<ModificationRequest, Long> {
    List<ModificationRequest> findByParent(User parent);
    List<ModificationRequest> findByStatus(ModificationRequest.RequestStatus status);
}
