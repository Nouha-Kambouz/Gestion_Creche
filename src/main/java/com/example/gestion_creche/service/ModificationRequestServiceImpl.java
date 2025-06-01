package com.example.gestion_creche.service;

import com.example.gestion_creche.model.Child;
import com.example.gestion_creche.model.ModificationRequest;
import com.example.gestion_creche.model.User;
import com.example.gestion_creche.repository.ModificationRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModificationRequestServiceImpl implements ModificationRequestService {
    private final ModificationRequestRepository modificationRequestRepository;
    private final ChildService childService;
    private final ObjectMapper objectMapper;

    public ModificationRequestServiceImpl(ModificationRequestRepository modificationRequestRepository,
                                        ChildService childService,
                                        ObjectMapper objectMapper) {
        this.modificationRequestRepository = modificationRequestRepository;
        this.childService = childService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ModificationRequest createRequest(ModificationRequest request) {
        request.setStatus(ModificationRequest.RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        return modificationRequestRepository.save(request);
    }

    @Override
    public List<ModificationRequest> getPendingRequestsByParent(User parent) {
        return modificationRequestRepository.findByParent(parent);
    }

    @Override
    public List<ModificationRequest> getPendingRequests() {
        return modificationRequestRepository.findByStatus(ModificationRequest.RequestStatus.PENDING);
    }

    @Override
    public boolean hasActiveRequest(Child child) {
        return getPendingRequests().stream()
                .anyMatch(request -> request.getChild().getId().equals(child.getId()));
    }

    @Override
    @Transactional
    public void approveRequest(Long requestId) {
        ModificationRequest request = modificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        try {
            Child updatedChild = objectMapper.readValue(request.getProposedChanges(), Child.class);
            Child existingChild = request.getChild();
            
            existingChild.setFirstName(updatedChild.getFirstName());
            existingChild.setLastName(updatedChild.getLastName());
            existingChild.setBirthDate(updatedChild.getBirthDate());
            existingChild.setAllergies(updatedChild.getAllergies());
            existingChild.setAddress(updatedChild.getAddress());
            existingChild.setPhoneNumber(updatedChild.getPhoneNumber());
            existingChild.setAdditionalInfo(updatedChild.getAdditionalInfo());
            
            childService.saveChild(existingChild);
            
            request.setStatus(ModificationRequest.RequestStatus.APPROVED);
            request.setProcessedDate(LocalDateTime.now());
            modificationRequestRepository.save(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to process modification request", e);
        }
    }

    @Override
    @Transactional
    public void rejectRequest(Long requestId) {
        ModificationRequest request = modificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        request.setStatus(ModificationRequest.RequestStatus.REJECTED);
        request.setProcessedDate(LocalDateTime.now());
        modificationRequestRepository.save(request);
    }
}
