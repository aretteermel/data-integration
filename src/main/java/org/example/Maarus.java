package org.example;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Maarus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String maaruseNr;
    private String maaruseKpv;
    private String kandeKpv;
    private String lisatahtaeg;
    private String maaruseLiik;
    private String maaruseLiikTekstina;
    private String maaruseOlek;
    private String maaruseOlekTekstina;
    private String kandeliik;
    private String kandeliikTekstina;
    private String joustumiseKpv;
    private String joustOlek;
    private String joustOlekTekstina;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}
