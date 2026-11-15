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
CREATE TABLE IF NOT EXISTS productos (
    id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    descripcion TEXT,
    cantidad INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    precio REAL NOT NULL CHECK (precio >= 0),
    fecha_creacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TRIGGER trg_productos_modificacion
AFTER UPDATE ON productos
FOR EACH ROW
WHEN NEW.fecha_modificacion = OLD.fecha_modificacion
BEGIN
    UPDATE productos SET fecha_modificacion = CURRENT_TIMESTAMP WHERE id_producto = NEW.id_producto;
END;
COMMIT;
