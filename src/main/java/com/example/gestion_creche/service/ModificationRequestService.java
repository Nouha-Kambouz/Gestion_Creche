package com.example.gestion_creche.service;

import com.example.gestion_creche.model.Child;
import com.example.gestion_creche.model.ModificationRequest;
import com.example.gestion_creche.model.User;
import java.util.List;

public interface ModificationRequestService {
    ModificationRequest createRequest(ModificationRequest request);
    List<ModificationRequest> getPendingRequestsByParent(User parent);
    List<ModificationRequest> getPendingRequests();
    boolean hasActiveRequest(Child child);
    void approveRequest(Long requestId);
    void rejectRequest(Long requestId);
}
