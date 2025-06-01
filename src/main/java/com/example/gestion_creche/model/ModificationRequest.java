package com.example.gestion_creche.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "child_id")
    private Child child;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    @Column(columnDefinition = "TEXT")
    private String proposedChanges;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private RequestType type;

    private LocalDateTime requestDate = LocalDateTime.now();

    private LocalDateTime processedDate;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum RequestType {
        UPDATE,
        DELETE
    }
}