package com.abp.pkr.pkrVista.dto;

import java.util.Map;

public class AccionInfoDto {

	private String hand;
	private String posHero;
	private Double stackHero;
	private String numJug;
	private String defAccion;

	private String izqVsPlayer;
	private String derVsPlayer;
	private Double izqVsEffStack;
	private Double derVsEffStack;
	private Long tiempo;

	private Map<String, AccionVsPlayer> accionVsPlayer;

	public String getHand() {
		return hand;
	}

	public void setHand(String hand) {
		this.hand = hand;
	}

	public String getPosHero() {
		return posHero;
	}

	public void setPosHero(String posHero) {
		this.posHero = posHero;
	}

	public Double getStackHero() {
		return stackHero;
	}

	public void setStackHero(Double stackHero) {
		this.stackHero = stackHero;
	}

	public String getNumJug() {
		return numJug;
	}

	public void setNumJug(String numJug) {
		this.numJug = numJug;
	}

	public Map<String, AccionVsPlayer> getAccionVsPlayer() {
		return accionVsPlayer;
	}

	public void setAccionVsPlayer(Map<String, AccionVsPlayer> accionVsPlayer) {
		this.accionVsPlayer = accionVsPlayer;
	}

	public String getIzqVsPlayer() {
		return izqVsPlayer;
	}

	public void setIzqVsPlayer(String izqVsPlayer) {
		this.izqVsPlayer = izqVsPlayer;
	}

	public String getDerVsPlayer() {
		return derVsPlayer;
	}

	public void setDerVsPlayer(String derVsPlayer) {
		this.derVsPlayer = derVsPlayer;
	}

	public Double getIzqVsEffStack() {
		return izqVsEffStack;
	}

	public void setIzqVsEffStack(Double izqVsEffStack) {
		this.izqVsEffStack = izqVsEffStack;
	}

	public Double getDerVsEffStack() {
		return derVsEffStack;
	}

	public void setDerVsEffStack(Double derVsEffStack) {
		this.derVsEffStack = derVsEffStack;
	}

	public String getDefAccion() {
		return defAccion;
	}

	public void setDefAccion(String defAccion) {
		this.defAccion = defAccion;
	}

	public Long getTiempo() {
		return tiempo;
	}

	public void setTiempo(Long tiempo) {
		this.tiempo = tiempo;
	}

}
