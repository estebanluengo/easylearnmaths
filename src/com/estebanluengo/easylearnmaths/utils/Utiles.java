package com.estebanluengo.easylearnmaths.utils;

import java.text.NumberFormat;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class Utiles {

	public static final String TAG = "Utils";
	
	/**
	 * Transforma el string especificado en un entero.
	 * @param s cadena a transformar a entero
	 * @param defaultValue en caso de que la cadena no pueda transformarse a entero se coge este valor por defecto
	 * @return retorna la cadena en formato integer
	 */
	public static int getInt(String s, int defaultValue){
		int n;
		try{
			n = Integer.valueOf(s);
		}catch(NumberFormatException e){
			Log.e(TAG,"Error convirtiendo a entero la cadena:"+s, e);
			n = defaultValue;
		}
		return n;
	}
	
	/**
	 * @return
	 */
	public static int getRandomNumber(Random random, int n) {
		int r = random.nextInt() % n;
		if (r < 0) r = r * -1;
		return r;
	}
	
	/**
	 * @return
	 */
	public static int getRandomNumberNotzero(Random random, int n) {
		int r = 0;
		while ((r = getRandomNumber(random, n)) == 0){
			
		}
		return r;
	}
	
	/**
	 * 
	 * @param expresion
	 * @return
	 */
	public static String removeExternalParentheses(String expresion){
		if (expresion == null) return "";
		int start = (expresion.startsWith("("))?1:0;
		int end = (expresion.endsWith(")"))?expresion.length()-1:expresion.length();
		return expresion.substring(start, end);
	}
	
	/**
	 * Construye un AlertDialog con los parámetros especificado
	 * @param c representa el contexto de la actividad desde la cual se va a crear el diálogo
	 * @param title título que tendrá el diálogo
	 * @param message mensaje a mostrar en el cuadro de diáolgo
	 * @param cancelable true o false si debe ser cancelable o no
	 * @param confirmarListener listener para poder invocar al método registrado cuando se pulse aceptar. Puede ser null
	 * @param cancelarListener listener para poder invocar al método registrado cuando se pulse cancelar. Puede ser null
	 * @return retorna un objeto AlertDialog
	 */
	public static AlertDialog createAlertDialog(Context c, String title, String message, boolean cancelable,
			DialogInterface.OnClickListener confirmarListener, DialogInterface.OnClickListener cancelarListener,int icon){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(c);  
        builder.setTitle(title);  
        builder.setMessage(message);            
        builder.setCancelable(cancelable);
        builder.setIcon(icon);
        if (confirmarListener != null)
        	builder.setPositiveButton("Aceptar", confirmarListener);
        if (cancelarListener != null)
            builder.setNegativeButton("Cancelar", cancelarListener);    
        return builder.create();
	}
	
	public static String getNumberFormat(long i){
		NumberFormat format = NumberFormat.getInstance(); 
		format.setGroupingUsed(true);
		String s = format.format(i);
		return s;
	}
	
	
}
