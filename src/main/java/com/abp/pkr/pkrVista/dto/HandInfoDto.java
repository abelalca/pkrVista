package com.abp.pkr.pkrVista.dto;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;

public class HandInfoDto {

	private String tipoMesa;
	private String numTourney;
	private String numHand;
	private Integer btnPos;
	private Integer posHero;
	private String hand;
	// los arrays deben tener length de los jugadores vivos en la mesa
	private Double[] stacksBb;
	private String[] history;
	private String[] tipoJugador;
	private long tiempoRest;
	private int sillaHero;

	public int getSillaHero() {
		return sillaHero;
	}

	public void setSillaHero(int sillaHero) {
		this.sillaHero = sillaHero;
	}

	public long getTiempoRest() {
		return tiempoRest;
	}

	public void setTiempoRest(long tiempoRest) {
		this.tiempoRest = tiempoRest;
	}

	public Integer getPosHero() {
		return posHero;
	}

	public void setPosHero(Integer posHero) {
		this.posHero = posHero;
	}

	public String getTipoMesa() {
		return tipoMesa;
	}

	public void setTipoMesa(String tipoMesa) {
		this.tipoMesa = tipoMesa;
	}

	public Double[] getStacksBb() {
		return stacksBb;
	}

	public void setStacksBb(Double[] stacksBb) {
		this.stacksBb = stacksBb;
	}

	public String[] getHistory() {
		return history;
	}

	public void setHistory(String[] history) {
		this.history = history;
	}

	public String[] getTipoJugador() {
		return tipoJugador;
	}

	public void setTipoJugador(String[] tipoJugador) {
		this.tipoJugador = tipoJugador;
	}

	public Integer getBtnPos() {
		return btnPos;
	}

	public void setBtnPos(Integer btnPos) {
		this.btnPos = btnPos;
	}

	public String getHand() {
		return hand;
	}

	public void setHand(String hand) {
		this.hand = hand;
	}

	public String getNumHand() {
		return numHand;
	}

	public void setNumHand(String numHand) {
		this.numHand = numHand;
	}

	public String getNumTourney() {
		return numTourney;
	}

	public void setNumTourney(String numTourney) {
		this.numTourney = numTourney;
	}

	public int numJugadores() {
		return this.stacksBb.length;
	}

	// Obtiene una copia de la mano pero ordenada con BB en ultima posicion
	public HandInfoDto ordenarInfo() {
		HandInfoDto hand = new HandInfoDto();
		hand.setHand(this.hand);
		hand.setNumHand(this.numHand);
		hand.setNumTourney(this.numTourney);
		hand.setTipoMesa(this.tipoMesa);

		// colocamos a BU en la posicion 0 y despues lo hacemos con BB
		if (this.btnPos != 0) {
			// BTN en pos 0
			int tam = this.stacksBb.length;
			Double[] antBu = Arrays.copyOfRange(this.stacksBb, 0, this.btnPos - 1);
			Double[] desBu = Arrays.copyOfRange(this.stacksBb, this.btnPos, tam - 1);
			hand.setStacksBb((Double[]) ArrayUtils.addAll(desBu, antBu));
			String[] hantBu = Arrays.copyOfRange(this.history, 0, this.btnPos - 1);
			String[] hdesBu = Arrays.copyOfRange(this.history, this.btnPos, tam - 1);
			hand.setHistory((String[]) ArrayUtils.addAll(hdesBu, hantBu));
			String[] tantBu = Arrays.copyOfRange(this.tipoJugador, 0, this.btnPos - 1);
			String[] tdesBu = Arrays.copyOfRange(this.tipoJugador, this.btnPos, tam - 1);
			hand.setTipoJugador((String[]) ArrayUtils.addAll(tdesBu, tantBu));
		} else {
			hand.setStacksBb(this.stacksBb);
			hand.setHistory(this.history);
			hand.setTipoJugador(this.tipoJugador);
		}
		if (this.btnPos <= this.posHero) {
			hand.setPosHero(this.posHero - this.btnPos);
		} else {
			int posH = (this.stacksBb.length - this.btnPos) + this.posHero;
			hand.setPosHero(posH);
		}
		hand.setBtnPos(0);

		// paso BB a la ultima posicion, la pos 0 queda con el primer jugador en
		// hablar
		int bbPosi = 2;
		if (hand.getStacksBb().length > 3) {
			int tam = this.stacksBb.length;
			Double[] antBB = Arrays.copyOfRange(hand.getStacksBb(), 0, bbPosi);
			Double[] desBB = Arrays.copyOfRange(hand.getStacksBb(), bbPosi + 1, tam - 1);
			hand.setStacksBb((Double[]) ArrayUtils.addAll(desBB, antBB));
			String[] hantBB = Arrays.copyOfRange(hand.getHistory(), 0, bbPosi);
			String[] hdesBB = Arrays.copyOfRange(hand.getHistory(), bbPosi + 1, tam - 1);
			hand.setHistory((String[]) ArrayUtils.addAll(hdesBB, hantBB));
			String[] tantBB = Arrays.copyOfRange(hand.getTipoJugador(), 0, bbPosi);
			String[] tdesBB = Arrays.copyOfRange(hand.getTipoJugador(), bbPosi + 1, tam - 1);
			hand.setTipoJugador((String[]) ArrayUtils.addAll(tdesBB, tantBB));
		}
		if (bbPosi < hand.getPosHero()) {
			hand.setPosHero(hand.getPosHero() - bbPosi - 1);
		} else {
			int posH = (hand.getStacksBb().length - bbPosi - 1) + hand.getPosHero();
			hand.setPosHero(posH);
		}
		if (hand.getStacksBb().length > 2) {
			hand.setBtnPos(hand.getStacksBb().length - 3);
		} else {
			hand.setBtnPos(0);
		}

		return hand;
	}

}
