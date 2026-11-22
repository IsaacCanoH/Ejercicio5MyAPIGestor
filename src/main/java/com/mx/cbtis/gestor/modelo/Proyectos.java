package com.mx.cbtis.gestor.modelo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDate;

@Entity
public class Proyectos {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id_proyecto;
	private String nombre;
	private String descripcion;
	private LocalDate fecha_inicio;
	private LocalDate fecha_finalizacion;
	private String estado_proyecto;
	private String fondo;

	public Proyectos() {
		super();
	}

	public Proyectos(Integer id_proyecto, String nombre, String descripcion, String fecha_inicio,
			String fecha_finalizacion, String estado_proyecto, String fondo) {
		super();
		this.id_proyecto = id_proyecto;
		this.nombre = nombre;
		this.descripcion = descripcion;
		setFecha_inicio(fecha_inicio);
		setFecha_finalizacion(fecha_finalizacion);
		this.estado_proyecto = estado_proyecto;
		this.fondo = fondo;
	}

	public Integer getId_proyecto() {
		return id_proyecto;
	}

	public void setId_proyecto(Integer id_proyecto) {
		this.id_proyecto = id_proyecto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getFecha_inicio() {
		return fecha_inicio == null ? null : fecha_inicio.toString();
	}

	public void setFecha_inicio(String valor) {
		this.fecha_inicio = valor == null || valor.isBlank()
				? null
				: LocalDate.parse(valor.trim());
	}

	public String getFecha_finalizacion() {
		return fecha_finalizacion == null ? null : fecha_finalizacion.toString();
	}

	public void setFecha_finalizacion(String valor) {
		this.fecha_finalizacion = valor == null || valor.isBlank()
				? null
				: LocalDate.parse(valor.trim());
	}

	public String getEstado_proyecto() {
		return estado_proyecto;
	}

	public void setEstado_proyecto(String estado_proyecto) {
		this.estado_proyecto = estado_proyecto;
	}

	public String getFondo() {
		return fondo;
	}

	public void setFondo(String fondo) {
		this.fondo = fondo;
	}
}
