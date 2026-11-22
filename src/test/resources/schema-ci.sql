-- Sequence and defined type
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence;

-- Table Definition
CREATE TABLE "public"."usuario" (
    "id_usuario" int4 NOT NULL DEFAULT nextval('hibernate_sequence'::regclass),
    "nombre" varchar(100),
    "correo_electronico" varchar(100),
    "contrasena" varchar(100),
    "rol" varchar(100),
    "imagen_usuario" varchar(255),
    PRIMARY KEY ("id_usuario")
);

-- Table Definition
CREATE TABLE "public"."proyectos" (
    "id_proyecto" int4 NOT NULL DEFAULT nextval('hibernate_sequence'::regclass),
    "nombre" varchar(100),
    "descripcion" varchar(100),
    "fecha_inicio" date,
    "fecha_finalizacion" date,
    "estado_proyecto" varchar(100),
    "fondo" varchar(100),
    PRIMARY KEY ("id_proyecto")
);

-- Table Definition
CREATE TABLE "public"."tareas" (
    "id_tarea" int4 NOT NULL DEFAULT nextval('hibernate_sequence'::regclass),
    "nombre" varchar(100),
    "descripcion" varchar(100),
    "fecha_vencimiento" date,
    "estado" varchar(255),
    "lista" varchar(255),
    "id_proyecto" int4,
    PRIMARY KEY ("id_tarea")
);

-- Table Definition
CREATE TABLE "public"."comentarios" (
    "id_comentario" int4 NOT NULL DEFAULT nextval('hibernate_sequence'::regclass),
    "texto_comentario" varchar(250),
    "fecha_hora" timestamp,
    "id_usuario" varchar(100),
    "id_tarea" int4,
    PRIMARY KEY ("id_comentario")
);

-- Table Definition
CREATE TABLE "public"."asignaciones" (
    "id_asignacion" int4 NOT NULL DEFAULT nextval('hibernate_sequence'::regclass),
    "id_usuario" int4,
    "id_tarea" int4,
    "fecha_asignacion" date,
    PRIMARY KEY ("id_asignacion")
);



-- Indices
CREATE INDEX asignaciones_usuario_idx ON public.asignaciones USING btree (id_usuario);
CREATE INDEX asignaciones_tareas_idx ON public.asignaciones USING btree (id_tarea);

