package org.example.data_integration.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Regulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String regulationNumber;
    private String regulationDate;
    private String entryDate;
    private String additionalTerm;
    private String regulationType;
    private String regulationTypeText;
    private String regulationStatus;
    private String regulationStatusText;
    private String entryType;
    private String entryTypeText;
    private String effectiveDate;
    private String effectiveStatus;
    private String effectiveStatusText;
    @ManyToOne
    private Company company;
}
