package com.marcosedo.yambu.app;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;

import com.marcosedo.yambu.R;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	
	private EditText etFecha;
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
					
		//Obtenemos la fecha actual para mostrarla en caso de que que 
		//no estemos editando una fecha anteriormente introducida
		final Calendar c = Calendar.getInstance();
        int año = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);	
        int dia = c.get(Calendar.DAY_OF_MONTH);	
				
		etFecha = (EditText) this.getActivity().findViewById(R.id.etFecha);
		
		String fecha = etFecha.getText().toString();
		String[] fechasplit = fecha.split("/");
		int tamaño = fechasplit.length;
				
		if (tamaño == 3){ 
			año = Integer.parseInt(fechasplit[2]);
			mes = Integer.parseInt(fechasplit[1])-1;
			dia = Integer.parseInt(fechasplit[0]);
		}
		
		//Crear nueva instancia de DatePickerDialog y devolverla
		// Create a new instance of DatePickerDialog and return it
       	return new DatePickerDialog(getActivity(), this, año, mes, dia);
				
	}
	
	public void onDateSet(DatePicker view, int año, int mes,int dia)  {
		
		etFecha = (EditText) this.getActivity().findViewById(R.id.etFecha);
		etFecha.setText(dia+"/"+(mes+1)+"/"+año);
			
	}
		
}	