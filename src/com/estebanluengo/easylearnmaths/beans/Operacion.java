package com.estebanluengo.easylearnmaths.beans;

import java.util.Random;

import com.estebanluengo.easylearnmaths.utils.Utiles;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Representa una operación matemática que el jugador deberá responder. Esta
 * clase se mapea en la tabla Operacion de la BBDD. La clase implementa
 * Parcelable porque necesitamos persistirla en la memoria del teléfono por si
 * la actividad Jugar que presenta las operaciones pierde el contexto.
 * 
 * @author eluengo
 * 
 */
public class Operacion implements Parcelable {

	public static final String TAG = "Operacion";
	public static final int OK = 1;
	public static final int PENDIENTE = 0;
	public static final int NOOK = 2;
	public static final String SUMA = "+";
	public static final String RESTA = "-";
	public static final String MULTIPLICACION = "x";
	public static final String DIVISION = "/";
	public static final String MIXTO = "mixto";
	public static final String UNITARIO = "unitario";
	public static final String COMPARA = "compara";
	public static final String QUENUMEROFALTA = "quenumerofalta";
	public static final String NIVEL_NORMAL = "N";
	public static final String NIVEL_DIFICIL = "D";
	public static final String NIVEL_FACIL = "E";
	public static final String NIVEL_AVANZADO = "A";

	private long id;
	private String tipoOperacion;// puede ser +,-,*,/,mixto, compara queNumeroFalta, unitaria
	private String expresion; // contiene la formula que se debe calcular
	private Operacion operando1; // lo que va a la izquierda del operador
	private Operacion operando2; // lo que va a la derecha del operador
	private int resultado; // contiene el resultado de realizar la operacion con operando1 y operando2 o el numero que falta o el comparador aplicado o un numero
	private int vecesContestadas;// para operaciones básicas cuantas veces se ha propuesto al jugador
	private int exito; // representa el ID del boton que simboliza el exito o el fracaso
	private String respuesta; // contiene lo que se ha contestado desde la UI por parte del jugador
	private int queNumeroFalta; // para operaciones de tipo queNumeroFalta si es 0 falta el operando1, si es 1, falta el operando2. Si es 2 falta el resultado
	private int comparador; // para operaciones de tipo compara puede ser 0 -> igual, 1 -> menor, 2 -> mayor
	private String operador; // contiene el operador entre los operandos: (+, -, *, /)
	private Random random = new Random();

	/**
	 * 
	 * @param parcel
	 */
	public Operacion(Parcel parcel) {
		this();
		readToParcel(parcel);
	}

	public Operacion() {
		super();
	}

	/**
	 * Si la respuesta contestada por el jugador almacenada en respuesta
	 * coincide con la calculada en el objeto entonces se retorna true. En caso
	 * contrario false
	 * 
	 * @return
	 */
	public boolean isExito() {
		try {
			Log.d(TAG, "Miramos el exito. Respuesta dada:" + respuesta + ". Resultado correcto:" + resultado);
			if ("".equals(respuesta))
				return false;
			if (tipoOperacion.equals(COMPARA)) {
				return (Integer.valueOf(respuesta) == resultado);
			} else if (tipoOperacion.equals(QUENUMEROFALTA)) {
				// Si la operacion es del tipo 0 x ? = 0, 0 div ? = 0, ? x 0 = 0
				// entonces damos por bueno cualquier respuesta
				if (((operador.equals(MULTIPLICACION) || operador.equals(DIVISION)))
						&& ((queNumeroFalta == 0 && operando2.getResultado() == 0) || (queNumeroFalta == 1 && operando1.getResultado() == 0))) {
					resultado = Integer.valueOf(respuesta);
					return true;
				} else
					return (Integer.valueOf(respuesta) == resultado);
			} else {
				Integer r = Integer.valueOf(respuesta);
				if (resultado == r)
					return true;
				else
					return false;
			}
		} catch (NumberFormatException nfe) {
			Log.d(TAG, "Error en la conversion " + nfe.toString());
			return false;
		}
	}

	/**
	 * Calcula el resultado de la operacion y lo almacena en la variable
	 * resultado
	 */
	public void setResultado() {
		if (!isNumero())
			resultado = calcula();
		Log.d(TAG, "Establecemos el resultado a " + resultado);
	}

	/**
	 * Calcula la operacion entre los dos operandos
	 * 
	 * @param operando1
	 * @param operando2
	 * @param operacion
	 * @return
	 */
	public int calcula() {
		int r = 0;
		if (UNITARIO.equals(tipoOperacion))
			return resultado;

		if (QUENUMEROFALTA.equals(tipoOperacion)) {
			switch (queNumeroFalta) {
			case 0:
				r = operando1.calcula();
				break;
			case 1:
				r = operando2.calcula();
				break;
			case 2:
				r = opera(operando1.getResultado(), operando2.getResultado(), operador);
				break;
			}
		} else if (COMPARA.equals(tipoOperacion)) {
			int r1 = operando1.calcula();
			int r2 = operando2.calcula();
			if (r1 == r2)
				return 0;
			if (r1 < r2)
				return 1;
			if (r1 > r2)
				return 2;
		}else if (SUMA.equals(operador))
			r = operando1.calcula() + operando2.calcula();
		else if (RESTA.equals(operador))
			r = operando1.calcula() - operando2.calcula();
		else if (MULTIPLICACION.equals(operador))
			r = operando1.calcula() * operando2.calcula();
		else if (DIVISION.equals(operador)) {
			int r1 = operando1.calcula();
			int r2 = operando2.calcula();
			// evitamos divisiones by zero
			if (r2 == 0) {
				Log.d(TAG, "division by zero");
				operando2.setTipoOperacion(UNITARIO);
				operando2.setResultado(Utiles.getRandomNumberNotzero(random, 10));
				r2 = operando2.getResultado();
				Log.d(TAG, "Nuevo numero:" + r);
			}
			r = r1 / r2;
		}
		return r;
	}

	/**
	 * 
	 * @param operando1
	 * @param operando2
	 * @param operador
	 * @return
	 */
	public static int opera(int operando1, int operando2, String operador) {
		int r = 0;
		if (SUMA.equals(operador))
			r = operando1 + operando2;
		else if (RESTA.equals(operador))
			r = operando1 - operando2;
		else if (MULTIPLICACION.equals(operador))
			r = operando1 * operando2;
		else if (DIVISION.equals(operador))
			r = operando1 / operando2;

		return r;
	}

	/**
	 * Construye la expresión en formato String de la operación para presentarla
	 * en pantalla
	 */
	public String getExpresion() {
		if (expresion != null)
			return expresion;
		if (tipoOperacion.equals(UNITARIO)) {
			expresion = Utiles.getNumberFormat(resultado);
		} else if (tipoOperacion.equals(COMPARA)) {
			expresion = "(" + operando1.getExpresion() + " ? " + operando2.getExpresion() + ")";
		} else if (tipoOperacion.equals(QUENUMEROFALTA)) {
			String caracterOperador;
			if (DIVISION.equals(operador))
				caracterOperador = "div";
			else
				caracterOperador = operador;
			switch (queNumeroFalta) {
			case 0:
				expresion = "(? " + caracterOperador + " " + operando2.getExpresion() + ")";
				break;
			case 1:
				expresion = "(" + operando1.getExpresion() + " " + caracterOperador + " ?)";
				break;
			case 2:
				expresion = "(" + operando1.getExpresion() + " " + caracterOperador + " " + operando2.getExpresion() + ")";
				break;
			}
		} else if (DIVISION.equals(operador)) {
			expresion = "(" + operando1.getExpresion() + " div " + operando2.getExpresion() + ")";
		} else
			expresion = "(" + operando1.getExpresion() + " " + operador + " " + operando2.getExpresion() + ")";

		Log.d(TAG, "Establecemos la expresion a :" + expresion);
		return expresion;
	}

	/**
	 * Metodo que se invoca para persistir el objeto en la memoria del teléfono
	 */
	public void writeToParcel(Parcel parcel, int flags) {

		parcel.writeLong(id);
		parcel.writeString(tipoOperacion);
		parcel.writeString(expresion);
		if (!isNumero()) {
			if (operando1 != null)
				operando1.writeToParcel(parcel, flags);
			if (operando2 != null)
				operando2.writeToParcel(parcel, flags);
		}
		parcel.writeInt(resultado);
		parcel.writeInt(vecesContestadas);
		parcel.writeInt(exito);
		parcel.writeString(respuesta);
		parcel.writeInt(queNumeroFalta);
		parcel.writeInt(comparador);
		parcel.writeString(operador);
	}

	/**
	 * Método que se invoca para leer el objeto de la memoria del teléfono
	 * 
	 * @param parcel
	 */
	public void readToParcel(Parcel parcel) {

		id = parcel.readLong();
		tipoOperacion = parcel.readString();
		expresion = parcel.readString();
		if (!isNumero()) {
			operando1 = new Operacion();
			operando1.readToParcel(parcel);
			operando2 = new Operacion();
			operando2.readToParcel(parcel);
		}
		resultado = parcel.readInt();
		vecesContestadas = parcel.readInt();
		exito = parcel.readInt();
		respuesta = parcel.readString();
		queNumeroFalta = parcel.readInt();
		comparador = parcel.readInt();
		operador = parcel.readString();
	}

	public static final Parcelable.Creator<Operacion> CREATOR = new Parcelable.Creator<Operacion>() {

		@Override
		public Operacion createFromParcel(Parcel parcel) {
			return new Operacion(parcel);
		}

		@Override
		public Operacion[] newArray(int size) {
			return new Operacion[size];
		}

	};

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the operacion
	 */
	public String getTipoOperacion() {
		return tipoOperacion;
	}

	/**
	 * 
	 * @return
	 */
	public String getNombreOperador() {
		if (DIVISION.equals(operador))
			return "div";
		else
			return operador;
	}

	/**
	 * @param operacion
	 *            the operacion to set
	 */
	public void setTipoOperacion(String tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}

	/**
	 * @return the operando1
	 */
	public Operacion getOperando1() {
		return operando1;
	}

	/**
	 * @param operando1
	 *            the operando1 to set
	 */
	public void setOperando1(Operacion operando1) {
		this.operando1 = operando1;
	}

	/**
	 * @return the operando2
	 */
	public Operacion getOperando2() {
		return operando2;
	}

	/**
	 * @param operando2
	 *            the operando2 to set
	 */
	public void setOperando2(Operacion operando2) {
		this.operando2 = operando2;
	}

	/**
	 * @return the resultado
	 */
	public int getResultado() {
		return resultado;
	}

	public boolean isNumero() {
		return tipoOperacion.equals(UNITARIO);
	}

	/**
	 * @param resultado
	 *            the resultado to set
	 */
	public void setResultado(int resultado) {
		this.resultado = resultado;
	}

	/**
	 * @return the vecesContestadas
	 */
	public int getVecesContestadas() {
		return vecesContestadas;
	}

	/**
	 * @param vecesContestadas
	 *            the vecesContestadas to set
	 */
	public void setVecesContestadas(int vecesContestadas) {
		this.vecesContestadas = vecesContestadas;
	}

	/**
	 * @return the exito
	 */
	public int getExito() {
		return exito;
	}

	/**
	 * @param exito
	 *            the exito to set
	 */
	public void setExito(int exito) {
		this.exito = exito;
	}

	/**
	 * @return the respuesta
	 */
	public String getRespuesta() {
		return respuesta;
	}

	/**
	 * @param respuesta
	 *            the respuesta to set
	 */
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	public int getQueNumeroFalta() {
		return queNumeroFalta;
	}

	public void setQueNumeroFalta(int queNumeroFalta) {
		this.queNumeroFalta = queNumeroFalta;
	}

	public int getComparador() {
		return comparador;
	}

	public static int getCodigoComparador(String caracter) {
		if (caracter.equals("="))
			return 0;
		else if (caracter.equals("<"))
			return 1;
		else if (caracter.equals(">"))
			return 2;
		return -1;
	}

	public static String getSimboloComparador(int comparador) {
		switch (comparador) {
		case 0:
			return "=";
		case 1:
			return "<";
		case 2:
			return ">";
		default:
			return "";
		}
	}

	public void setComparador() {
		if (operando1.getResultado() == operando2.getResultado())
			comparador = 0;
		else if (operando1.getResultado() < operando2.getResultado())
			comparador = 1;
		else
			comparador = 2;
	}

	public String getOperador() {
		return operador;
	}

	public void setOperador(String operador) {
		this.operador = operador;
	}

}
