package com.marcosedo.yambu.app;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TimePicker;

import com.marcosedo.yambu.R;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		
	private EditText etHora;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		///Obtenemos la hora actual para mostrarla en caso de que que 
		//no estemos editando una fecha anteriormente introducida
		final Calendar c = Calendar.getInstance();
		int horas = c.get(Calendar.HOUR_OF_DAY);
		int minutos = c.get(Calendar.MINUTE);
				
		etHora = (EditText) this.getActivity().findViewById(R.id.etHora);
		
		String fecha = etHora.getText().toString();
		String[] fechasplit = fecha.split(":");
		int tamaño = fechasplit.length;
			
		if (tamaño == 2){ 
			minutos = Integer.parseInt(fechasplit[1]);
			horas = Integer.parseInt(fechasplit[0]);
		}
							
		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, horas, minutos,
				DateFormat.is24HourFormat(getActivity()));
	}

	public void onTimeSet(TimePicker view, int hora, int minutos) {
		this.getActivity().findViewById(R.id.etHora);
		
		//añade un cero si los minutos no llegan a 10 para respetar el formato
		if (minutos > 9 )
			etHora.setText(hora+":"+minutos);	
		else 
			etHora.setText(hora+":0"+minutos);	
	}
}