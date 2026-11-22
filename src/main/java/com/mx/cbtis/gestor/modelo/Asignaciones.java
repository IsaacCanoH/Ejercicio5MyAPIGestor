package com.mx.cbtis.gestor.modelo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDate;

@Entity
public class Asignaciones {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id_asignacion;
	private Integer id_usuario;
	private Integer id_tarea;
	private LocalDate fecha_asignacion;

	public Asignaciones() {
		super();
	}

	public Asignaciones(Integer id_asignacion, Integer id_usuario, Integer id_tarea, String fecha_asignacion) {
		super();
		this.id_asignacion = id_asignacion;
		this.id_usuario = id_usuario;
		this.id_tarea = id_tarea;
		setFecha_asignacion(fecha_asignacion);
	}

	public Integer getId_asignacion() {
		return id_asignacion;
	}

	public void setId_asignacion(Integer id_asignacion) {
		this.id_asignacion = id_asignacion;
	}

	public Integer getId_usuario() {
		return id_usuario;
	}

	public void setId_usuario(Integer id_usuario) {
		this.id_usuario = id_usuario;
	}

	public Integer getId_tarea() {
		return id_tarea;
	}

	public void setId_tarea(Integer id_tarea) {
		this.id_tarea = id_tarea;
	}

	public String getFecha_asignacion() {
		return fecha_asignacion == null ? null : fecha_asignacion.toString();
	}

	public void setFecha_asignacion(String valor) {
		this.fecha_asignacion = valor == null || valor.isBlank()
				? null
				: LocalDate.parse(valor.trim());
	}
}
