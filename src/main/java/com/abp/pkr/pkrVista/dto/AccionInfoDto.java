package com.abp.pkr.pkrVista.dto;

import java.util.Map;

public class AccionInfoDto {

	private String hand;
	private String posHero;
	private Double stackHero;
	private String numJug;

	private String supVsPlayer;
	private String infVsPlayer;
	private Double supVsEffStack;
	private Double infVsEffStack;

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

	public String getSupVsPlayer() {
		return supVsPlayer;
	}

	public void setSupVsPlayer(String supVsPlayer) {
		this.supVsPlayer = supVsPlayer;
	}

	public String getInfVsPlayer() {
		return infVsPlayer;
	}

	public void setInfVsPlayer(String infVsPlayer) {
		this.infVsPlayer = infVsPlayer;
	}

	public Double getSupVsEffStack() {
		return supVsEffStack;
	}

	public void setSupVsEffStack(Double supVsEffStack) {
		this.supVsEffStack = supVsEffStack;
	}

	public Double getInfVsEffStack() {
		return infVsEffStack;
	}

	public void setInfVsEffStack(Double infVsEffStack) {
		this.infVsEffStack = infVsEffStack;
	}

	public Map<String, AccionVsPlayer> getAccionVsPlayer() {
		return accionVsPlayer;
	}

	public void setAccionVsPlayer(Map<String, AccionVsPlayer> accionVsPlayer) {
		this.accionVsPlayer = accionVsPlayer;
	}

}
