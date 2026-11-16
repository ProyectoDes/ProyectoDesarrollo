-- Bambú y Brillo: esquema para PostgreSQL (Neon)
-- Convertido desde base_de_datos/01_clientes_citas.sql (SQLite).

BEGIN;

CREATE TABLE IF NOT EXISTS clientes (
id_cliente SERIAL PRIMARY KEY,
nombre TEXT NOT NULL,
edad INTEGER NOT NULL,
telefono TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS citas (
id_cita SERIAL PRIMARY KEY,
id_cliente INTEGER NOT NULL REFERENCES clientes (id_cliente) ON DELETE NO ACTION,
fecha DATE NOT NULL,
hora_inicio TIME NOT NULL,
hora_fin TIME NOT NULL,
servicio TEXT NOT NULL,
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS productos (
id_producto SERIAL PRIMARY KEY,
nombre TEXT NOT NULL,
descripcion TEXT,
cantidad INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
precio NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION actualizar_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
NEW.fecha_modificacion = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_citas_modificacion ON citas;
CREATE TRIGGER trg_citas_modificacion
BEFORE UPDATE ON citas
FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_modificacion();

DROP TRIGGER IF EXISTS trg_productos_modificacion ON productos;
CREATE TRIGGER trg_productos_modificacion
BEFORE UPDATE ON productos
FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_modificacion();

COMMIT;
