package com.marcosedo.yambu.app;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marcosedo.yambu.R;

import java.text.DecimalFormat;
import java.util.List;

public class ListaEventosCustomAdapter extends ArrayAdapter<Evento> {

    private Context context;
    private View.OnClickListener listener;
    private List<Evento> items;


    public ListaEventosCustomAdapter(Context context, int resourceId, List<Evento> items, View.OnClickListener listener) {
        super(context, resourceId, items);
        this.context = context;
        this.listener = listener;
        this.items = items;

        //if (items.size()>0){
        //this.hidden = new boolean[items.size()];
        //Log.e("", "items.lengh" + Integer.toString(items.size()));
        //for (int i = 0; i < items.size(); i++) {
        //this.hidden[i] = false;
        //Log.e("", "SIZE hidden" + Integer.toString(hidden.length));
        //}
        //}
    }

    @Override
    public int getCount() {
        return (this.items.size() - getHiddenCount());
    }

    private int getHiddenCount() {
        int count = 0;
        Evento evento;
        for (int i = 0; i < items.size(); i++) {
            evento = items.get(i);

            if (evento.getHided() == true)
                count++;
        }
        return count;
    }

    private int getRealPosition(int position) {
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (items.get(position + diff).getHided())
                i--;
        }
        return (position + diff);
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;
        for (int i = 0; i <= location; i++) {
            if (items.get(i).getHided())
                count++;
        }
        return count;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int position = getRealPosition(index);
        Evento rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.linea_lista_eventos, null);

            holder.layout = (RelativeLayout) convertView.findViewById((R.id.relativeLayout));
            holder.ivCartel = (ImageView) convertView.findViewById(R.id.ivCartel);
            holder.tvFecha = (TextView) convertView.findViewById(R.id.tvFecha);
            holder.tvHora = (TextView) convertView.findViewById(R.id.tvHora);
            holder.tvPrecio = (TextView) convertView.findViewById(R.id.tvPrecio);
            holder.tvLugar = (TextView) convertView.findViewById(R.id.tvLugar);
            holder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
            holder.tvGrupo = (TextView) convertView.findViewById(R.id.tvGrupo);
            holder.ivStarOff = (ImageView) convertView.findViewById(R.id.ivStarOff);
            holder.ivStarOn = (ImageView) convertView.findViewById(R.id.ivStarOn);
            holder.tvSeparator = (TextView) convertView.findViewById(R.id.tvSeparator);
            holder.ivStarOn.setTag(position);
            holder.ivStarOff.setTag(position);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (rowItem.getFollowed() == 1) {
            holder.ivStarOff.setVisibility(View.GONE);
            holder.ivStarOn.setVisibility(View.VISIBLE);
        } else {
            holder.ivStarOff.setVisibility(View.VISIBLE);
            holder.ivStarOn.setVisibility(View.GONE);
        }

        if (rowItem.getSeparatorText().equals("")) {
            holder.tvSeparator.setVisibility(View.GONE);
        } else {
            holder.tvSeparator.setVisibility(View.VISIBLE);
            holder.tvSeparator.setText(rowItem.getSeparatorText());
        }

        if (rowItem.getThumb() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(rowItem.getThumb(), 0, rowItem.getThumb().length);
            holder.ivCartel.setImageBitmap(bitmap);
        } else holder.ivCartel.setImageBitmap(null);
        ////////////////////////////////////////////////////////////7

        holder.tvFecha.setText(rowItem.getFecha());
        holder.tvHora.setText(rowItem.getHora());
        holder.tvLugar.setText(rowItem.getPlaceName());
        holder.tvPrecio.setText(rowItem.getPrecio() + " " + Constantes.getCurrencySymbolByCode(rowItem.getCurrencyCode()));
        holder.tvGrupo.setText(rowItem.getNombreGrupo());

        holder.ivStarOff.setOnClickListener(listener);
        holder.ivStarOn.setOnClickListener(listener);
        holder.ivStarOff.setTag(position);//le guardamos la posicion real
        holder.ivStarOn.setTag(position);

        //CALCULAMOS DISTANCIA AL EVENTO
        if (((MainActivity) context).lastLocation == null) {
            //holder.tvDistance.setText(context.getResources().getString(R.string.unknow_distance));
            holder.tvDistance.setText("");
        } else {
            Location placeLocation = new Location("");//provider name is unecessary
            placeLocation.setLatitude(rowItem.getLatitude());
            placeLocation.setLongitude(rowItem.getLongitude());
            float distance = placeLocation.distanceTo(((MainActivity) context).lastLocation);

            holder.tvDistance.setText(formatDistance(distance));//format distance pasa a millas si procede y formatea el texto
        }

        return convertView;
    }

    private String formatDistance(float distance) {
        SharedPreferences preferences = context.getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
        String units = preferences.getString(Constantes.SP_DISTANCE_UNITS, "");

        distance = distance / 1000;//pasamos de metros a kilometros

        switch (units) {//si hay que pasar a milla lo hacemos ahora leyendo las preferencias
            case "mi":
                distance = Utilidades.kmToMiles(distance);
                break;
        }

        DecimalFormat df = new DecimalFormat("#.#");
        String formatted = df.format(distance);


        switch (units) {
            case "km":
                formatted = formatted + " " + units;
                break;
            case "mi":
                formatted = formatted + " " + units;
                break;
            default:
                formatted = formatted + " km";
                break;
        }

        return formatted;
    }

    /*private view holder class*/
    private class ViewHolder {
        RelativeLayout layout;
        ImageView ivCartel;
        TextView tvFecha;
        TextView tvHora;
        TextView tvPrecio;
        TextView tvLugar;
        TextView tvDistance;
        TextView tvGrupo;
        ImageView ivStarOn;
        ImageView ivStarOff;
        TextView tvSeparator;
    }
}