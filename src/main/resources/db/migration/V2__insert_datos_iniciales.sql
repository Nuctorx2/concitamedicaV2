-- Inserción de roles (sin fechas de auditoría, Java se encargará)
INSERT INTO roles (id, nombre) VALUES (1, 'ROLE_PACIENTE');
INSERT INTO roles (id, nombre) VALUES (2, 'ROLE_MEDICO');
INSERT INTO roles (id, nombre) VALUES (3, 'ROLE_ADMIN');

-- Inserción de especialidades (sin fechas de auditoría)
INSERT INTO especialidades (id, nombre) VALUES (1, 'Medicina General');
INSERT INTO especialidades (id, nombre) VALUES (2, 'Odontología');
INSERT INTO especialidades (id, nombre) VALUES (3, 'Dermatología');