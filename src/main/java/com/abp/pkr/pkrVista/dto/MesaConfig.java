/**
 * 
 */
package com.abp.pkr.pkrVista.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author abpubuntu
 *
 */

@Component
@ConfigurationProperties(prefix = "mesaconf")
public class MesaConfig {

	// coordenadas
	private List<Zona> stack = new ArrayList<>();
	private List<Zona> palos = new ArrayList<>();
	private List<Zona> mesa = new ArrayList<>();
	private Zona posicion;
	private Zona cartas;

	private String rutaPalos;
	
	private String formato;
	private String margenerror;
	private String sillahero;

	// OCR CONFIG
	private String factorResize;
	private String tessdata;
	private String tessLeng;

	// rutas de archivos Pruebas
	private String rutacaptura;
	private String nombrearchivo;

	// *******************************INNER CLASSES******************************
	public static class Zona {
		private int x;
		private int y;
		private int ancho;
		private int alto;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getAncho() {
			return ancho;
		}

		public void setAncho(int ancho) {
			this.ancho = ancho;
		}

		public int getAlto() {
			return alto;
		}

		public void setAlto(int alto) {
			this.alto = alto;
		}

	}

	// *******************************************************

	public String getRutacaptura() {
		return rutacaptura;
	}

	public void setRutacaptura(String rutacaptura) {
		this.rutacaptura = rutacaptura;
	}

	public String getNombrearchivo() {
		return nombrearchivo;
	}

	public void setNombrearchivo(String nombrearchivo) {
		this.nombrearchivo = nombrearchivo;
	}

	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

	public Double getMargenerror() {
		if (this.margenerror == null || margenerror.trim().equals("")) {
			return 0d; // you want to return double
		} else {
			return (Double.parseDouble(this.margenerror)) / 100;
		}
	}

	public void setMargenerror(String margenerror) {
		this.margenerror = margenerror;
	}

	public List<Zona> getStack() {
		return stack;
	}

	public void setStack(List<Zona> stack) {
		this.stack = stack;
	}

	public List<Zona> getMesa() {
		return mesa;
	}

	public void setMesa(List<Zona> mesa) {
		this.mesa = mesa;
	}

	public Zona getPosicion() {
		return posicion;
	}

	public void setPosicion(Zona posicion) {
		this.posicion = posicion;
	}

	public String getSillahero() {
		return sillahero;
	}

	public void setSillahero(String sillahero) {
		this.sillahero = sillahero;
	}

	public String getFactorResize() {
		return factorResize;
	}

	public void setFactorResize(String factorResize) {
		this.factorResize = factorResize;
	}

	public String getTessdata() {
		return tessdata;
	}

	public void setTessdata(String tessdata) {
		this.tessdata = tessdata;
	}

	public String getTessLeng() {
		return tessLeng;
	}

	public void setTessLeng(String tessLeng) {
		this.tessLeng = tessLeng;
	}

	public Zona getCartas() {
		return cartas;
	}

	public void setCartas(Zona cartas) {
		this.cartas = cartas;
	}

	public List<Zona> getPalos() {
		return palos;
	}

	public void setPalos(List<Zona> palos) {
		this.palos = palos;
	}

	public String getRutaPalos() {
		return rutaPalos;
	}

	public void setRutaPalos(String rutaPalos) {
		this.rutaPalos = rutaPalos;
	}

}
