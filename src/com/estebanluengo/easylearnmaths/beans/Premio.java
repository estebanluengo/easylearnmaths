package com.estebanluengo.easylearnmaths.beans;

/**
 * Representa un premio que puede comprar el jugador. Esta clase se mapea en la tabla premio de la BBDD
 * @author eluengo
 *
 */
public class Premio {
	private long id;
	private String premio;  //Descripción textual del premio
	private String puntos;  //representación textual de los puntos. Es decir, como se verá en la UI
	private int pts;        //puntos en formato numérico para poder calcular si el jugador puede comprarlo o no
	
	public Premio(){
		super();
	}

	
	public Premio(long id, String premio, String puntos, int pts){
		this.id = id;
		this.premio = premio;
		this.puntos = puntos;
		this.pts = pts;
	}


	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}


	/**
	 * @return the premio
	 */
	public String getPremio() {
		return premio;
	}


	/**
	 * @param premio the premio to set
	 */
	public void setPremio(String premio) {
		this.premio = premio;
	}


	/**
	 * @return the puntos
	 */
	public String getPuntos() {
		return puntos;
	}


	/**
	 * @param puntos the puntos to set
	 */
	public void setPuntos(String puntos) {
		this.puntos = puntos;
	}


	/**
	 * @return the pts
	 */
	public int getPts() {
		return pts;
	}


	/**
	 * @param pts the pts to set
	 */
	public void setPts(int pts) {
		this.pts = pts;
	}
	
}
