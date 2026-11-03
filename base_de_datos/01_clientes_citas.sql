BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS citas (
    id_cita INTEGER PRIMARY KEY AUTOINCREMENT,
    id_cliente INTEGER NOT NULL,
    fecha TEXT NOT NULL,
    hora_inicio TEXT NOT NULL,
    hora_fin TEXT NOT NULL,
    servicio TEXT NOT NULL,
    fecha_creacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_cliente) REFERENCES clientes (id_cliente) ON DELETE NO ACTION
);
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    edad INTEGER NOT NULL,
    telefono TEXT NOT NULL
);
CREATE TRIGGER trg_citas_modificacion
AFTER UPDATE ON citas
FOR EACH ROW
WHEN NEW.fecha_modificacion = OLD.fecha_modificacion
BEGIN
    UPDATE citas SET fecha_modificacion = CURRENT_TIMESTAMP WHERE id_cita = NEW.id_cita;
END;
COMMIT;
