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
	private List<Zona> cartas = new ArrayList<>();
	private List<Zona> accionesJug = new ArrayList<>();
	private List<Zona> posicion = new ArrayList<>();
	private Zona ciegas;

	private String rutaPalos;
	private String rutaCartas;
	private String rutaStacks;
	private String rutaPosicion;
	private String rutaCiegas;

	private String formato;
	private String margenerror;
	private String sillahero;
	private String tipoOCR;
	private String catalanoError;
	private String capturazonas;
	private String numIteraCaptura;

	private String numCiegas;

	// OCR CONFIG
	private String factorResize;
	private String tessdata;
	private String tessLeng;
	private String waitAnalisis;

	// rutas de archivos Pruebas
	private String rutacaptura;
	private String rutaBugs;
	private String nombrearchivo;
	private String rutaJson;
	private String nombreJson;

	// usuario y estrategia
	private String usuario;
	private String estrategia;

	private String activarHudDinamico;

	// *******************************INNER CLASSES******************************
	public static class Zona {
		private int x;
		private int y;
		private int ancho;
		private int alto;
		private boolean lecturaValida;
		private String nombre;

		public boolean isLecturaValida() {
			return lecturaValida;
		}

		public void setLecturaValida(boolean lecturaValida) {
			this.lecturaValida = lecturaValida;
		}

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

		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
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

	public List<Zona> getPosicion() {
		return posicion;
	}

	public void setPosicion(List<Zona> posicion) {
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

	public List<Zona> getCartas() {
		return cartas;
	}

	public void setCartas(List<Zona> cartas) {
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

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getEstrategia() {
		return estrategia;
	}

	public void setEstrategia(String estrategia) {
		this.estrategia = estrategia;
	}

	public String getNombreJson() {
		return nombreJson;
	}

	public void setNombreJson(String nombreJson) {
		this.nombreJson = nombreJson;
	}

	public String getRutaJson() {
		return rutaJson;
	}

	public void setRutaJson(String rutaJson) {
		this.rutaJson = rutaJson;
	}

	public String getRutaBugs() {
		return rutaBugs;
	}

	public void setRutaBugs(String rutaBugs) {
		this.rutaBugs = rutaBugs;
	}

	public String getRutaCartas() {
		return rutaCartas;
	}

	public void setRutaCartas(String rutaCartas) {
		this.rutaCartas = rutaCartas;
	}

	public String getTipoOCR() {
		return tipoOCR;
	}

	public void setTipoOCR(String tipoOCR) {
		this.tipoOCR = tipoOCR;
	}

	public String getRutaStacks() {
		return rutaStacks;
	}

	public void setRutaStacks(String rutaStacks) {
		this.rutaStacks = rutaStacks;
	}

	public String getCapturazonas() {
		return capturazonas;
	}

	public void setCapturazonas(String capturazonas) {
		this.capturazonas = capturazonas;
	}

	public String getRutaPosicion() {
		return rutaPosicion;
	}

	public void setRutaPosicion(String rutaPosicion) {
		this.rutaPosicion = rutaPosicion;
	}

	public String getRutaCiegas() {
		return rutaCiegas;
	}

	public void setRutaCiegas(String rutaCiegas) {
		this.rutaCiegas = rutaCiegas;
	}

	public String getWaitAnalisis() {
		return waitAnalisis;
	}

	public void setWaitAnalisis(String waitAnalisis) {
		this.waitAnalisis = waitAnalisis;
	}

	public String getCatalanoError() {
		return catalanoError;
	}

	public void setCatalanoError(String catalanoError) {
		this.catalanoError = catalanoError;
	}

	public String getNumIteraCaptura() {
		return numIteraCaptura;
	}

	public void setNumIteraCaptura(String numIteraCaptura) {
		this.numIteraCaptura = numIteraCaptura;
	}

	public String getActivarHudDinamico() {
		return activarHudDinamico;
	}

	public void setActivarHudDinamico(String activarHudDinamico) {
		this.activarHudDinamico = activarHudDinamico;
	}

	public List<Zona> getAccionesJug() {
		return accionesJug;
	}

	public void setAccionesJug(List<Zona> accionesJug) {
		this.accionesJug = accionesJug;
	}

	public Zona getCiegas() {
		return ciegas;
	}

	public void setCiegas(Zona ciegas) {
		this.ciegas = ciegas;
	}

	public String getNumCiegas() {
		return numCiegas;
	}

	public void setNumCiegas(String numCiegas) {
		this.numCiegas = numCiegas;
	}

}
