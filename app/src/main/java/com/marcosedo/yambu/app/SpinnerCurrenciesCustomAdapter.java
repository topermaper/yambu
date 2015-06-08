package com.marcosedo.yambu.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.marcosedo.yambu.R;

import java.util.ArrayList;


public class SpinnerCurrenciesCustomAdapter extends ArrayAdapter<String> {

    private Activity activity;
    private ArrayList data;
    public Resources res;
    Currency tempValues = null;
    LayoutInflater inflater;

    /**
     * **********  CustomAdapter Constructor ****************
     */
    public SpinnerCurrenciesCustomAdapter(
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
        View row = inflater.inflate(R.layout.linea_spinner_currencies, parent, false);

        /***** Get each Model object from Arraylist ********/
        tempValues = (Currency) data.get(position);

        TextView code = (TextView) row.findViewById(R.id.tvCode);
        TextView symbol = (TextView) row.findViewById(R.id.tvSymbol);

        // Set values for spinner each row
        code.setText(tempValues.getCode());
        symbol.setText(tempValues.getSymbol());

        return row;
    }
}