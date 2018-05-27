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
	private List<Zona> mesa = new ArrayList<>();
	private List<Zona> history = new ArrayList<>();
	private List<Zona> palos = new ArrayList<>();
	private Zona cartas;
	private Zona numhand;
	private Zona tourneynum;
	private Zona posicion;
	private Zona tipomesa;

	// rutas de archivos
	private String rutacaptura;

	private String rutastacks;
	private String rutamesa;
	private String rutatourneynum;
	private String rutaposicion;
	private String rutahistory;
	private String rutatipomesa;
	private String rutapalos;

	private String rutahandnum;
	private String rutacartas;

	private String nombrearchivo;
	private String formato;
	private String margenerror;
	private String sillahero;
	private String jugadordefault;
	private String tipojuego;

	// OCR CONFIG
	private String factorResize;
	private String tessdata;
	private String tessLeng;

	// *******************************INNER
	// CLASSES******************************
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

	public String getRutastacks() {
		return rutastacks;
	}

	public void setRutastacks(String rutastacks) {
		this.rutastacks = rutastacks;
	}

	public String getRutamesa() {
		return rutamesa;
	}

	public void setRutamesa(String rutamesa) {
		this.rutamesa = rutamesa;
	}

	public String getRutahandnum() {
		return rutahandnum;
	}

	public void setRutahandnum(String rutahandnum) {
		this.rutahandnum = rutahandnum;
	}

	public String getRutaposicion() {
		return rutaposicion;
	}

	public void setRutaposicion(String rutaposicion) {
		this.rutaposicion = rutaposicion;
	}

	public String getRutacartas() {
		return rutacartas;
	}

	public void setRutacartas(String rutacartas) {
		this.rutacartas = rutacartas;
	}

	public String getRutahistory() {
		return rutahistory;
	}

	public void setRutahistory(String rutahistory) {
		this.rutahistory = rutahistory;
	}

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

	public List<Zona> getHistory() {
		return history;
	}

	public void setHistory(List<Zona> history) {
		this.history = history;
	}

	public String getRutatourneynum() {
		return rutatourneynum;
	}

	public void setRutatourneynum(String rutatourneynum) {
		this.rutatourneynum = rutatourneynum;
	}

	public Zona getTourneynum() {
		return tourneynum;
	}

	public void setTourneynum(Zona tourneynum) {
		this.tourneynum = tourneynum;
	}

	public Zona getPosicion() {
		return posicion;
	}

	public void setPosicion(Zona posicion) {
		this.posicion = posicion;
	}

	public String getRutatipomesa() {
		return rutatipomesa;
	}

	public void setRutatipomesa(String rutatipomesa) {
		this.rutatipomesa = rutatipomesa;
	}

	public Zona getTipomesa() {
		return tipomesa;
	}

	public void setTipomesa(Zona tipomesa) {
		this.tipomesa = tipomesa;
	}

	public String getSillahero() {
		return sillahero;
	}

	public void setSillahero(String sillahero) {
		this.sillahero = sillahero;
	}

	public String getJugadordefault() {
		return jugadordefault;
	}

	public void setJugadordefault(String jugadordefault) {
		this.jugadordefault = jugadordefault;
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

	public String getTipojuego() {
		return tipojuego;
	}

	public void setTipojuego(String tipojuego) {
		this.tipojuego = tipojuego;
	}

	public Zona getNumhand() {
		return numhand;
	}

	public void setNumhand(Zona numhand) {
		this.numhand = numhand;
	}

	public Zona getCartas() {
		return cartas;
	}

	public void setCartas(Zona cartas) {
		this.cartas = cartas;
	}

	public String getRutapalos() {
		return rutapalos;
	}

	public void setRutapalos(String rutapalos) {
		this.rutapalos = rutapalos;
	}

	public List<Zona> getPalos() {
		return palos;
	}

	public void setPalos(List<Zona> palos) {
		this.palos = palos;
	}

}
