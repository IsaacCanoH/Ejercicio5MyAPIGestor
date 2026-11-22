package com.mx.cbtis.gestor.modelo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import java.time.LocalDate;

@Entity
public class Tareas {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id_tarea;
	private String nombre;
	private String descripcion;
	private LocalDate fecha_vencimiento;
	private String lista;
	private String estado;
	private Integer id_proyecto;

	public Tareas() {
		super();
	}

	public Tareas(Integer id_tarea, String nombre, String descripcion, String fecha_vencimiento, String lista,
			String estado, Integer id_proyecto) {
		super();
		this.id_tarea = id_tarea;
		this.nombre = nombre;
		this.descripcion = descripcion;
		setFecha_vencimiento(fecha_vencimiento);
		this.lista = lista;
		this.estado = estado;
		this.id_proyecto = id_proyecto;
	}

	public Integer getId_tarea() {
		return id_tarea;
	}

	public void setId_tarea(Integer id_tarea) {
		this.id_tarea = id_tarea;
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

	public String getFecha_vencimiento() {
		return fecha_vencimiento == null ? null : fecha_vencimiento.toString();
	}

	public void setFecha_vencimiento(String valor) {
		this.fecha_vencimiento = valor == null || valor.isBlank()
				? null
				: LocalDate.parse(valor.trim());
	}

	public String getLista() {
		return lista;
	}

	public void setLista(String lista) {
		this.lista = lista;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getId_proyecto() {
		return id_proyecto;
	}

	public void setId_proyecto(Integer id_proyecto) {
		this.id_proyecto = id_proyecto;
	}
}
