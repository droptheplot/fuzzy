CREATE TABLE IF NOT EXISTS
  domains(
           id SERIAL NOT NULL CONSTRAINT domains_pk PRIMARY KEY,
           sld_id INTEGER NOT NULL CONSTRAINT domains_slds_id_fk REFERENCES slds ON DELETE CASCADE,
           tld_id INTEGER NOT NULL CONSTRAINT domains_tlds_id_fk REFERENCES tlds ON DELETE CASCADE,
           status VARCHAR(255),
           checked_at TIME NOT NULL,
           raw TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS domains_sld_id_tld_id_index ON domains (sld_id, tld_id);

