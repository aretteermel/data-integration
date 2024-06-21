CREATE TABLE IF NOT EXISTS companies (
    ariregistri_kood BIGINT PRIMARY KEY,
    nimi VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL,
    ariregistri_kood BIGINT REFERENCES companies(ariregistri_kood),
    CONSTRAINT fk_ariregistri_kood FOREIGN KEY (ariregistri_kood) REFERENCES companies (ariregistri_kood),
    maaruse_nr VARCHAR(50) PRIMARY KEY,
    maaruse_kpv DATE,
    kande_kpv DATE,
    lisatahtaeg DATE,
    maaruse_liik VARCHAR(5),
    maaruse_liik_tekstina TEXT,
    maaruse_olek VARCHAR(5),
    maaruse_olek_tekstina TEXT,
    kandeliik VARCHAR(5),
    kandeliik_tekstina TEXT,
    joustumise_kpv DATE,
    joust_olek VARCHAR(5),
    joust_olek_tekstina TEXT
);
