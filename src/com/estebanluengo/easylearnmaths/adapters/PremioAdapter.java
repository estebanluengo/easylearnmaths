package com.estebanluengo.easylearnmaths.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.estebanluengo.easylearnmaths.R;
import com.estebanluengo.easylearnmaths.beans.Premio;

/**
 * Este adaptador está asociado a la vista premios.xml donde se mapea el nombre del premio y los puntos para conseguirlo
 * @author eluengo
 * @version 1.0 27/06/2012
 *
 */
public class PremioAdapter extends ArrayAdapter<Premio>{

    Context context; 
    int layoutResourceId;    
    ArrayList<Premio> data = null;
    
    public PremioAdapter(Context context, int layoutResourceId, ArrayList<Premio> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        PremioHolder holder = null;
        
        if(row == null)
        {
        	LayoutInflater inflater = ((Activity)context).getLayoutInflater();          
            row = inflater.inflate(layoutResourceId, parent, false);            
            holder = new PremioHolder();
            holder.premio = (TextView)row.findViewById(R.id.tvPremio);
            holder.puntos = (TextView)row.findViewById(R.id.tvPuntosPremio);
            
            row.setTag(holder);
        }
        else
        {
            holder = (PremioHolder)row.getTag();
        }
        
        Premio premio = data.get(position);
        holder.premio.setText(premio.getPremio());
        holder.puntos.setText(premio.getPuntos());
        
        return row;
    }
    
    static class PremioHolder
    {
       TextView premio;
       TextView puntos;
    }
}
