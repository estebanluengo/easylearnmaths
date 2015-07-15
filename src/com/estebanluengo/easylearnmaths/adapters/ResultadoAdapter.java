package com.estebanluengo.easylearnmaths.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.estebanluengo.easylearnmaths.R;
import com.estebanluengo.easylearnmaths.beans.Operacion;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Este adaptador está asociado a la vista resultados.xml donde se mapea la respuesta del jugador, el resultado de la operación
 * y un icono que indica si ha sido correcta o no
 * @author eluengo
 * @version 1.0 27/06/2012
 *
 */
public class ResultadoAdapter extends ArrayAdapter<Operacion>{

    Context context; 
    int layoutResourceId;    
    Operacion data[] = null;
    
    public ResultadoAdapter(Context context, int layoutResourceId, Operacion[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ResultadoHolder holder = null;
        
        if(row == null)
        {
        	LayoutInflater inflater = ((Activity)context).getLayoutInflater();          
            row = inflater.inflate(layoutResourceId, parent, false);            
            holder = new ResultadoHolder();
            holder.expresion = (TextView)row.findViewById(R.id.tvExpresion);
            holder.respuestaCorrecta = (TextView)row.findViewById(R.id.tvRespuestaCorrecta);
            holder.exito = (ImageView)row.findViewById(R.id.ivExito);            
            row.setTag(holder);
        }
        else
        {
            holder = (ResultadoHolder)row.getTag();
        }        
        //Se controla que se imprime en la segunda columna
        Operacion resultado = data[position];
        if (Operacion.COMPARA.equals(resultado.getTipoOperacion())){
        	holder.expresion.setText(Utiles.removeExternalParentheses(resultado.getExpresion())+" ("+Operacion.getSimboloComparador(Integer.valueOf(resultado.getRespuesta())) + ")");
        }else if (Operacion.QUENUMEROFALTA.equals(resultado.getTipoOperacion())){
			int r = Operacion.opera(resultado.getOperando1().getResultado(), resultado.getOperando2().getResultado(), resultado.getOperador());			
        	holder.expresion.setText(Utiles.removeExternalParentheses(resultado.getExpresion())+" = " + Utiles.getNumberFormat(r) + " ("+resultado.getRespuesta() + ")");
        }else{
        	holder.expresion.setText(Utiles.removeExternalParentheses(resultado.getExpresion())+" = "+resultado.getRespuesta());
        }
        if (resultado.isExito())
        	holder.respuestaCorrecta.setText(context.getText(R.string.bien));
        else{        	
        	if (Operacion.COMPARA.equals(resultado.getTipoOperacion())){
        		holder.respuestaCorrecta.setText(Operacion.getSimboloComparador(resultado.getResultado()));
        	}else if (Operacion.QUENUMEROFALTA.equals(resultado.getTipoOperacion())){
        		holder.respuestaCorrecta.setText(Utiles.getNumberFormat(resultado.getResultado()));
        	}else{
        		holder.respuestaCorrecta.setText(Utiles.getNumberFormat(resultado.getResultado()));
        	}
        	holder.expresion.setTextColor(Color.RED);
        }
        holder.exito.setImageResource(resultado.getExito());        
        return row;
    }
    
    static class ResultadoHolder
    {
       TextView expresion;
       TextView respuestaCorrecta;
       ImageView exito;
    }
}