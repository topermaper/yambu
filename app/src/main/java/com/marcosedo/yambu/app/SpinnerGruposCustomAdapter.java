package com.marcosedo.yambu.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.marcosedo.yambu.R;

import java.util.ArrayList;


public class SpinnerGruposCustomAdapter extends ArrayAdapter<String> {

    private Activity activity;
    private ArrayList data;
    public Resources res;
    Grupo tempValues = null;
    LayoutInflater inflater;

    /**
     * **********  CustomAdapter Constructor ****************
     */
    public SpinnerGruposCustomAdapter(
            MainActivity activitySpinner,
            int textViewResourceId,
            ArrayList objects,
            Resources resLocal
    ) {
        super(activitySpinner, textViewResourceId, objects);

        /********** Take passed values **********/
        activity = activitySpinner;
        data = objects;
        res = resLocal;

        /***********  Layout inflator to call external xml layout () **********************/
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // This funtion called for each row ( Called data.size() times )
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(R.layout.linea_spinner_grupos, parent, false);

        /***** Get each Model object from Arraylist ********/
        tempValues = null;
        tempValues = (Grupo) data.get(position);
        byte[] byteArray;

        TextView label = (TextView) row.findViewById(R.id.tvNombre);
        ImageView companyLogo = (ImageView) row.findViewById(R.id.ivImg);

        // Set values for spinner each row
        label.setText(tempValues.getNombre());

        if (tempValues.getThumb() != null) {
            byteArray = tempValues.getThumb();
        } else {
            byteArray = null;
        }
        if (tempValues.getThumb() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            companyLogo.setImageBitmap(bitmap);
        } else companyLogo.setImageBitmap(null);

        return row;
    }
}

