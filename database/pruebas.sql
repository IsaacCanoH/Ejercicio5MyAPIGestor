-- Pruebas sobre un esquema vacio, exclusivamente en la MariaDB temporal de CI.
SET NAMES utf8mb4;
DELIMITER //
BEGIN NOT ATOMIC
    IF DATABASE() IS NULL OR DATABASE() NOT IN ('dbgestor_ci', 'dbgestor_restore') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Estas pruebas solo se ejecutan en una base temporal de CI';
    END IF;
END//
DELIMITER ;

SET @ci_total = 0;
DROP PROCEDURE IF EXISTS ci_assert;
DELIMITER //
CREATE PROCEDURE ci_assert(IN p_ok BOOLEAN, IN p_caso VARCHAR(255))
BEGIN
    IF NOT COALESCE(p_ok, FALSE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = p_caso;
    END IF;
    SET @ci_total = @ci_total + 1;
    SELECT CONCAT('PASS | ', p_caso);
END//
DELIMITER ;

SELECT COUNT(*) INTO @n FROM information_schema.tables
WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
AND table_name IN ('usuario', 'proyectos', 'tareas', 'asignaciones', 'comentarios', 'hibernate_sequence');
CALL ci_assert(@n = 6, 'DB01 - Existen las seis tablas');

SELECT COUNT(*) INTO @n FROM information_schema.tables
WHERE table_schema = DATABASE() AND engine = 'InnoDB'
AND table_name IN ('usuario', 'proyectos', 'tareas', 'asignaciones', 'comentarios', 'hibernate_sequence');
CALL ci_assert(@n = 6, 'DB02 - Las tablas permiten transacciones InnoDB');

SELECT COUNT(*) INTO @n FROM information_schema.table_constraints
WHERE table_schema = DATABASE() AND constraint_type = 'PRIMARY KEY'
AND table_name IN ('usuario', 'proyectos', 'tareas', 'asignaciones', 'comentarios');
CALL ci_assert(@n = 5, 'DB03 - Las cinco tablas de negocio tienen clave primaria');

SELECT COUNT(*) INTO @n FROM information_schema.columns
WHERE table_schema = DATABASE() AND extra LIKE '%auto_increment%'
AND table_name IN ('usuario', 'proyectos', 'tareas', 'asignaciones', 'comentarios');
CALL ci_assert(@n = 5, 'DB04 - Los identificadores de negocio son autoincrementales');

START TRANSACTION;

INSERT INTO usuario(nombre, correo_electronico, contrasena, rol, imagen_usuario)
VALUES ('CI Usuario', 'ci@example.test', 'solo-pruebas-ci', 'Estudiante', '#FFFFFF');
SET @usuario = LAST_INSERT_ID();
SELECT COUNT(*) INTO @n FROM usuario
WHERE id_usuario = @usuario AND correo_electronico = 'ci@example.test' AND rol = 'Estudiante';
CALL ci_assert(@n = 1 AND @usuario > 0, 'DB05 - Insertar y consultar usuario');

INSERT INTO proyectos(nombre, descripcion, fecha_inicio, fecha_finalizacion, estado_proyecto, fondo)
VALUES ('CI Proyecto', 'Prueba de base de datos', '2026-10-25', '2026-10-30', 'En proceso', '#FFFFFF');
SET @proyecto = LAST_INSERT_ID();
SELECT COUNT(*) INTO @n FROM proyectos
WHERE id_proyecto = @proyecto AND nombre = 'CI Proyecto' AND fecha_inicio = '2026-10-25';
CALL ci_assert(@n = 1 AND @proyecto > 0, 'DB06 - Insertar y consultar proyecto');

INSERT INTO tareas(nombre, descripcion, fecha_vencimiento, lista, estado, id_proyecto)
VALUES ('CI Tarea', 'Descripcion de prueba', '2026-10-30', 'pendientes', 'primary', @proyecto);
SET @tarea = LAST_INSERT_ID();
SELECT COUNT(*) INTO @n FROM tareas
WHERE id_tarea = @tarea AND id_proyecto = @proyecto AND nombre = 'CI Tarea';
CALL ci_assert(@n = 1 AND @tarea > 0, 'DB07 - Insertar y consultar tarea del proyecto');

INSERT INTO asignaciones(id_usuario, id_tarea, fecha_asignacion)
VALUES (@usuario, @tarea, '2026-10-25');
SET @asignacion = LAST_INSERT_ID();
SELECT COUNT(*) INTO @n FROM asignaciones
WHERE id_asignacion = @asignacion AND id_usuario = @usuario AND id_tarea = @tarea;
CALL ci_assert(@n = 1 AND @asignacion > 0, 'DB08 - Insertar y consultar asignacion');

-- El esquema actual guarda el nombre como texto en comentarios.id_usuario.
INSERT INTO comentarios(texto_comentario, fecha_hora, id_usuario, id_tarea)
VALUES ('CI Comentario', '2026-10-25 12:00:00', 'CI Usuario', @tarea);
SET @comentario = LAST_INSERT_ID();
SELECT COUNT(*) INTO @n FROM comentarios
WHERE id_comentario = @comentario AND id_tarea = @tarea AND texto_comentario = 'CI Comentario';
CALL ci_assert(@n = 1 AND @comentario > 0, 'DB09 - Insertar y consultar comentario');

SELECT COUNT(*) INTO @n
FROM proyectos p JOIN tareas t ON t.id_proyecto = p.id_proyecto
JOIN asignaciones a ON a.id_tarea = t.id_tarea
JOIN usuario u ON u.id_usuario = a.id_usuario
JOIN comentarios c ON c.id_tarea = t.id_tarea AND c.id_usuario = u.nombre
WHERE p.id_proyecto = @proyecto AND u.id_usuario = @usuario AND c.id_comentario = @comentario;
CALL ci_assert(@n = 1, 'DB10 - Consultar las cinco tablas mediante JOIN');

UPDATE usuario SET nombre = 'CI Usuario editado' WHERE id_usuario = @usuario;
SELECT COUNT(*) INTO @n FROM usuario WHERE id_usuario = @usuario AND nombre = 'CI Usuario editado';
CALL ci_assert(@n = 1, 'DB11 - Actualizar usuario');

UPDATE proyectos SET estado_proyecto = 'Finalizado' WHERE id_proyecto = @proyecto;
SELECT COUNT(*) INTO @n FROM proyectos WHERE id_proyecto = @proyecto AND estado_proyecto = 'Finalizado';
CALL ci_assert(@n = 1, 'DB12 - Actualizar proyecto');

UPDATE tareas SET descripcion = 'Descripcion actualizada', lista = 'encurso' WHERE id_tarea = @tarea;
SELECT COUNT(*) INTO @n FROM tareas
WHERE id_tarea = @tarea AND descripcion = 'Descripcion actualizada' AND lista = 'encurso';
CALL ci_assert(@n = 1, 'DB13 - Actualizar tarea');

UPDATE asignaciones SET fecha_asignacion = '2026-10-26' WHERE id_asignacion = @asignacion;
SELECT COUNT(*) INTO @n FROM asignaciones
WHERE id_asignacion = @asignacion AND fecha_asignacion = '2026-10-26';
CALL ci_assert(@n = 1, 'DB14 - Actualizar asignacion');

UPDATE comentarios SET texto_comentario = 'CI Comentario editado' WHERE id_comentario = @comentario;
SELECT COUNT(*) INTO @n FROM comentarios
WHERE id_comentario = @comentario AND texto_comentario = 'CI Comentario editado';
CALL ci_assert(@n = 1, 'DB15 - Actualizar comentario');

DELETE FROM comentarios WHERE id_comentario = @comentario;
SELECT COUNT(*) INTO @n FROM comentarios WHERE id_comentario = @comentario;
CALL ci_assert(@n = 0, 'DB16 - Eliminar comentario');

DELETE FROM asignaciones WHERE id_asignacion = @asignacion;
SELECT COUNT(*) INTO @n FROM asignaciones WHERE id_asignacion = @asignacion;
CALL ci_assert(@n = 0, 'DB17 - Eliminar asignacion');

DELETE FROM tareas WHERE id_tarea = @tarea;
SELECT COUNT(*) INTO @n FROM tareas WHERE id_tarea = @tarea;
CALL ci_assert(@n = 0, 'DB18 - Eliminar tarea');

DELETE FROM proyectos WHERE id_proyecto = @proyecto;
SELECT COUNT(*) INTO @n FROM proyectos WHERE id_proyecto = @proyecto;
CALL ci_assert(@n = 0, 'DB19 - Eliminar proyecto');

DELETE FROM usuario WHERE id_usuario = @usuario;
SELECT COUNT(*) INTO @n FROM usuario WHERE id_usuario = @usuario;
CALL ci_assert(@n = 0, 'DB20 - Eliminar usuario');

INSERT INTO hibernate_sequence(next_val) VALUES (1);
UPDATE hibernate_sequence SET next_val = 2;
SELECT COUNT(*) INTO @n FROM hibernate_sequence WHERE next_val = 2;
CALL ci_assert(@n = 1, 'DB21 - Leer y actualizar secuencia Hibernate');
ROLLBACK;

START TRANSACTION;
INSERT INTO usuario(nombre, correo_electronico, contrasena, rol)
VALUES ('CI Rollback', 'rollback@example.test', 'solo-pruebas-ci', 'Estudiante');
SET @rollback_usuario = LAST_INSERT_ID();
ROLLBACK;
SELECT COUNT(*) INTO @n FROM usuario WHERE id_usuario = @rollback_usuario;
CALL ci_assert(@n = 0, 'DB22 - ROLLBACK revierte una insercion');

SELECT (
    (SELECT COUNT(*) FROM usuario) + (SELECT COUNT(*) FROM proyectos)
    + (SELECT COUNT(*) FROM tareas) + (SELECT COUNT(*) FROM asignaciones)
    + (SELECT COUNT(*) FROM comentarios) + (SELECT COUNT(*) FROM hibernate_sequence)
) INTO @n;
CALL ci_assert(@n = 0, 'DB23 - La base temporal termina sin registros');

SELECT CONCAT('RESULTADO: ', @ci_total, ' pruebas SQL aprobadas en ', DATABASE());
DROP PROCEDURE ci_assert;
