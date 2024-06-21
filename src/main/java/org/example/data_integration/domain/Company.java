package org.example.data_integration.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long companyCode;
    private String name;
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Regulation> regulations;
}
