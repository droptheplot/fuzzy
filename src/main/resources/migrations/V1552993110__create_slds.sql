CREATE TABLE IF NOT EXISTS
  slds(
        id SERIAL NOT NULL CONSTRAINT slds_pk PRIMARY KEY,
        value VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS slds_value_index ON slds (value);

