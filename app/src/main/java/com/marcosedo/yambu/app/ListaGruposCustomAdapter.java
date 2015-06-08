package com.marcosedo.yambu.app;


import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcosedo.yambu.R;

public class ListaGruposCustomAdapter extends ArrayAdapter<Grupo> {

    Context context;
    //private static int selectedIndex = -1;
    //OnClickListener listener;

    public ListaGruposCustomAdapter(Context context, int resourceId, List<Grupo> items) {
        super(context, resourceId, items);
        this.context = context;
        //this.listener = listener;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView ivImg;
        TextView tvNombre;
        TextView tvCreador;
        TextView tvEventCount;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Grupo rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.linea_lista_grupos, null);
            holder = new ViewHolder();
            holder.ivImg = (ImageView) convertView.findViewById(R.id.ivImg);
            holder.tvNombre = (TextView) convertView.findViewById(R.id.tvNombre);
            holder.tvCreador = (TextView) convertView.findViewById(R.id.tvCreador);
            holder.tvEventCount = (TextView) convertView.findViewById(R.id.tvEventCount);

            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        byte[] byteArray;

        if (rowItem.getThumb() != null){
            byteArray = rowItem.getThumb();
        }
        else{
            byteArray = null;
        }
        if (byteArray != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            holder.ivImg.setImageBitmap(bitmap);
        }
        else holder.ivImg.setImageBitmap(null);
        ///////////////////////////////////////////////////////////

        holder.tvNombre.setText(rowItem.getNombre());
        holder.tvCreador.setText(rowItem.getCreador());

        if (rowItem.getNumberOfEvents() == 1)
            holder.tvEventCount.setText(Integer.toString(rowItem.getNumberOfEvents()) + " event");
        else holder.tvEventCount.setText(Integer.toString(rowItem.getNumberOfEvents()) + " events");

        //asignamos el onclicklistener a la vista
        //holder.ivCartel.setOnClickListener(listener);
        return convertView;
    }
}