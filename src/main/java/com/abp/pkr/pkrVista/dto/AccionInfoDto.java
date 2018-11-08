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
	private String mesaNombre;

	private Double eff3WayStack;
	private String LL;
	private String LR;
	private String LS;
	private String RC;
	private String RS;
	private String SS;

	private long tiempo;

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

	public String getMesaNombre() {
		return mesaNombre;
	}

	public void setMesaNombre(String mesaNombre) {
		this.mesaNombre = mesaNombre;
	}

	public Double getEff3WayStack() {
		return eff3WayStack;
	}

	public void setEff3WayStack(Double eff3WayStack) {
		this.eff3WayStack = eff3WayStack;
	}

	public String getLL() {
		return LL;
	}

	public void setLL(String lL) {
		LL = lL;
	}

	public String getLR() {
		return LR;
	}

	public void setLR(String lR) {
		LR = lR;
	}

	public String getLS() {
		return LS;
	}

	public void setLS(String lS) {
		LS = lS;
	}

	public String getRC() {
		return RC;
	}

	public void setRC(String rC) {
		RC = rC;
	}

	public String getRS() {
		return RS;
	}

	public void setRS(String rS) {
		RS = rS;
	}

	public String getSS() {
		return SS;
	}

	public void setSS(String sS) {
		SS = sS;
	}

	public long getTiempo() {
		return tiempo;
	}

	public void setTiempo(long tiempo) {
		this.tiempo = tiempo;
	}

}
