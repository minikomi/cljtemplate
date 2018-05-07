-- migration to be applied

CREATE TABLE users
(
  id          SERIAL        PRIMARY  KEY,
  created_at  TIMESTAMP     NOT NULL      DEFAULT  CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP     NOT NULL      DEFAULT  CURRENT_TIMESTAMP,
  username    VARCHAR(300)  NOT NULL,
  password    VARCHAR(300)  NOT NULL,
);
