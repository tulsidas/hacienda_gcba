-- estructura de la tabla, adaptar segun la DB
CREATE TABLE LICITACION(ID INTEGER NOT NULL PRIMARY KEY,CONTRATACION VARCHAR(255),ACTUACION VARCHAR(255),RUBRO VARCHAR(512),FECHA VARCHAR(16),SOLICITANTE VARCHAR(255),LICITANTE VARCHAR(255),ESTADO VARCHAR(32),LINK VARCHAR(255),ARCHIVO VARCHAR(255),OBSERVACIONES VARCHAR(8192),EMPRESA VARCHAR(255))