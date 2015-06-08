package com.marcosedo.yambu.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditTextCharCounter extends EditText{
	
	float escala;
	Paint p1;
		
	public EditTextCharCounter(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs,defStyle);
		inicializacion();
	}
	 
	public EditTextCharCounter(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    inicializacion();
	}
	 
	public EditTextCharCounter(Context context) {	
	    super(context);
	    inicializacion();
	}


	private void inicializacion(){
		
		escala =  getResources().getDisplayMetrics().density;
		
		p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
		p1.setColor(Color.GRAY);
		p1.setTextSize(20*escala);
		
		escala =  getResources().getDisplayMetrics().density;
		
		//FILTRO DE MAXIMA LONGITUD DEL NICK
		EditTextCharCounter tv = this;
		tv.setFilters( new InputFilter[] { new InputFilter.LengthFilter(Constantes.MAX_NICK_LENGHT) } );
		//////////////////////////////////////////////
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
	    //Llamamos al método de la clase base (EditText)
	    super.onDraw(canvas);
	  	 
	    //Dibujamos el número de caracteres sobre el contador
	    //coordenadas x,y contando a paritr de la esquina superior izquierda creo
	    canvas.drawText("" + (Constantes.MAX_NICK_LENGHT - this.getText().toString().length()) ,
	        this.getWidth()-31*escala, 28*escala, p1);
	}

}