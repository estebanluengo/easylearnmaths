package com.estebanluengo.easylearnmaths.beans;

/**
 * Contiene la información estadística que se muestra en la actividad Estadistica
 * @author eluengo
 *
 */
public class Estadistica {
	private int preguntasContestadas = 0;
	private int preguntasBasicasContestadas = 0;
	private int sumas = 0;
	private int restas = 0;
	private int multiplicaciones = 0;
	private int divisiones = 0;
	private int comparaciones = 0;
	private int aLaPrimera = 0;
	private int aLaSegunda = 0;
	private int aLaTercera = 0;
	private int aciertos = 0;
	
	public Estadistica(){
		
	}

	/**
	 * @return the noAciertos
	 */
	public int getAciertos() {
		return aciertos;
	}

	/**
	 * @param noAciertos the noAciertos to set
	 */
	public void setAciertos(int aciertos) {
		this.aciertos = aciertos;
	}

	/**
	 * @return the preguntasContestadas
	 */
	public int getPreguntasContestadas() {
		return preguntasContestadas;
	}

	/**
	 * @param preguntasContestadas the preguntasContestadas to set
	 */
	public void setPreguntasContestadas(int preguntasContestadas) {
		this.preguntasContestadas = preguntasContestadas;
	}

	public int getPreguntasBasicasContestadas() {
		return preguntasBasicasContestadas;
	}

	public void setPreguntasBasicasContestadas(int preguntasBasicasContestadas) {
		this.preguntasBasicasContestadas = preguntasBasicasContestadas;
	}

	/**
	 * @return the sumas
	 */
	public int getSumas() {
		return sumas;
	}

	/**
	 * @param sumas the sumas to set
	 */
	public void setSumas(int sumas) {
		this.sumas = sumas;
	}

	/**
	 * @return the restas
	 */
	public int getRestas() {
		return restas;
	}

	/**
	 * @param restas the restas to set
	 */
	public void setRestas(int restas) {
		this.restas = restas;
	}

	/**
	 * @return the multiplicaciones
	 */
	public int getMultiplicaciones() {
		return multiplicaciones;
	}

	/**
	 * @param multiplicaciones the multiplicaciones to set
	 */
	public void setMultiplicaciones(int multiplicaciones) {
		this.multiplicaciones = multiplicaciones;
	}

	/**
	 * @return the divisiones
	 */
	public int getDivisiones() {
		return divisiones;
	}

	/**
	 * @param divisiones the divisiones to set
	 */
	public void setDivisiones(int divisiones) {
		this.divisiones = divisiones;
	}

	public int getComparaciones() {
		return comparaciones;
	}

	public void setComparaciones(int comparaciones) {
		this.comparaciones = comparaciones;
	}

	/**
	 * @return the aLaPrimera
	 */
	public int getaLaPrimera() {
		return aLaPrimera;
	}

	/**
	 * @param aLaPrimera the aLaPrimera to set
	 */
	public void setaLaPrimera(int aLaPrimera) {
		this.aLaPrimera = aLaPrimera;
	}

	/**
	 * @return the aLaSegunda
	 */
	public int getaLaSegunda() {
		return aLaSegunda;
	}

	/**
	 * @param aLaSegunda the aLaSegunda to set
	 */
	public void setaLaSegunda(int aLaSegunda) {
		this.aLaSegunda = aLaSegunda;
	}

	/**
	 * @return the aLaTercera
	 */
	public int getaLaTercera() {
		return aLaTercera;
	}

	/**
	 * @param aLaTercera the aLaTercera to set
	 */
	public void setaLaTercera(int aLaTercera) {
		this.aLaTercera = aLaTercera;
	}
	
	/**
	 * Recupera como un float la tasa de aciertos a la primera.
	 * @return
	 */
	public float getTasaALaPrimera(){
		return (((float) getaLaPrimera() / (float) getPreguntasBasicasContestadas()) * 100);
	}

	/**
	 * Recupera como un float la tasa de aciertos a la segunda.
	 * @return
	 */
	public float getTasaALaSegunda(){
		return (((float) getaLaSegunda() / (float) getPreguntasBasicasContestadas()) * 100);
	}
	
	/**
	 * Recupera como un float la tasa de aciertos a la tercera.
	 * @return
	 */
	public float getTasaALaTercera(){
		return (((float) getaLaTercera() / (float) getPreguntasBasicasContestadas()) * 100);
	}
	
	/**
	 * Calcula la tasa de fallos.
	 * @return
	 */
	public int getTasaFallos(){
		int tasaPrimera = (int)getTasaALaPrimera(); 
		int tasaSegunda = (int)getTasaALaSegunda();
		int tasaTercera = (int)getTasaALaTercera();
		if (tasaPrimera == 0 && tasaSegunda == 0 && tasaTercera == 0)
			return 0;
		return 100 - (tasaPrimera + tasaSegunda + tasaTercera);
	}
	
}
