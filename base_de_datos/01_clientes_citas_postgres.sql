-- Bambú y Brillo: esquema para PostgreSQL (Neon)
-- Convertido desde base_de_datos/01_clientes_citas.sql (SQLite).

BEGIN;

CREATE TABLE IF NOT EXISTS clientes (
id_cliente SERIAL PRIMARY KEY,
nombre TEXT NOT NULL,
edad INTEGER NOT NULL,
telefono TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS usuarios (
id_usuario SERIAL PRIMARY KEY,
nombre_usuario TEXT NOT NULL UNIQUE,
contrasena_hash TEXT NOT NULL,
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sesiones abiertas: se guarda solo el hash del token de la cookie, nunca el token
CREATE TABLE IF NOT EXISTS sesiones (
token_hash TEXT PRIMARY KEY,
id_usuario INTEGER NOT NULL REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
expira TIMESTAMP NOT NULL,
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS citas (
id_cita SERIAL PRIMARY KEY,
id_cliente INTEGER NOT NULL REFERENCES clientes (id_cliente) ON DELETE NO ACTION,
fecha DATE NOT NULL,
hora_inicio TIME NOT NULL,
hora_fin TIME NOT NULL,
servicio TEXT NOT NULL,
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT chk_horario_cita CHECK (hora_fin > hora_inicio)
);

CREATE TABLE IF NOT EXISTS historia_clinica (
id_historia SERIAL PRIMARY KEY,
id_cliente INTEGER NOT NULL REFERENCES clientes (id_cliente) ON DELETE NO ACTION,
fecha DATE NOT NULL DEFAULT CURRENT_DATE,
observaciones TEXT,
id_cita INTEGER UNIQUE REFERENCES citas (id_cita) ON DELETE CASCADE,
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS productos (
id_producto SERIAL PRIMARY KEY,
nombre TEXT NOT NULL,
descripcion TEXT,
cantidad INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
activo BOOLEAN NOT NULL DEFAULT TRUE,
fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Baja lógica: un producto dado de baja deja de salir en el inventario,
-- pero sigue en la historia clínica de las citas donde se usó.
-- (Para bases que ya existían sin esta columna)
ALTER TABLE productos ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE IF NOT EXISTS cita_producto (
id_cita INTEGER NOT NULL REFERENCES citas (id_cita) ON DELETE CASCADE,
id_producto INTEGER NOT NULL REFERENCES productos (id_producto) ON DELETE NO ACTION,
cantidad_usada INTEGER NOT NULL CHECK (cantidad_usada > 0),
PRIMARY KEY (id_cita, id_producto)
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

DROP TRIGGER IF EXISTS trg_historia_modificacion ON historia_clinica;
CREATE TRIGGER trg_historia_modificacion
BEFORE UPDATE ON historia_clinica
FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_modificacion();

DROP TRIGGER IF EXISTS trg_usuarios_modificacion ON usuarios;
CREATE TRIGGER trg_usuarios_modificacion
BEFORE UPDATE ON usuarios
FOR EACH ROW EXECUTE FUNCTION actualizar_fecha_modificacion();

COMMIT;
