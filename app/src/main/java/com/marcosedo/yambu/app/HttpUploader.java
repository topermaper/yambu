package com.marcosedo.yambu.app;


import java.io.BufferedReader;
import java.io.DataOutputStream; 
import java.io.FileInputStream; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection; 
import java.net.MalformedURLException; 
import java.net.URL; 

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class HttpUploader  {
	
	//String responseString;
	//byte[] dataToServer;	

	private static final String CHARSET = "UTF-8";
	private static final String LINEEND = "\r\n"; 
	private static final String TWOHYPHENS = "--"; 
	private static final String BOUNDARY = "*****";
		
	DataOutputStream dos;
	private HttpURLConnection conn;
	
	///////////CONSTRUCTOR//////////
	public HttpUploader(String urlString) throws IOException{
		
		try {
			URL connectURL = new URL(urlString);
			conn = (HttpURLConnection) connectURL.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive"); 
			conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDARY);
			conn.setInstanceFollowRedirects(false); 
			//////////////////////////////////////
		
			OutputStream os = conn.getOutputStream();
			dos = new DataOutputStream(os);
		}
		catch (MalformedURLException ex) {
			Log.e("YAMBU", "MalformedURLException: " + ex.getMessage(), ex);
			ex.printStackTrace();
		}
		/*
		catch (IOException ioe) { 
			Log.e("YAMBU", "IOException: " + ioe.getMessage(), ioe);
			ioe.printStackTrace();
		}*/
        catch (IOException ioe) {
            Log.e("YAMBU", "vamos a lanzar una exception a ver si va");
            ioe.printStackTrace();
            throw ioe;
        }

	}
	        
	
	public void añadirArgumento(String nombre, String valor) {
	    try {
            dos.writeBytes(TWOHYPHENS + BOUNDARY + LINEEND);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + nombre + "\"" + LINEEND);
			dos.writeBytes("Content-Type: text/plain; charset="+CHARSET + LINEEND + LINEEND);
			dos.writeBytes(valor);
			dos.writeBytes(LINEEND);

		} catch (IOException e) {
			Log.e("YAMBU", "IOException: " + e.getMessage(), e);
			e.printStackTrace();
		}

	}	
	 
	
	public void añadirArchivo(String nombre,FileInputStream fileInputStream) {
		
		try{	      
			dos.writeBytes(TWOHYPHENS + BOUNDARY + LINEEND);
			dos.writeBytes("Content-Disposition: form-data; name=\"archivo\";filename=\"" 
					+ nombre +"\"" + LINEEND);
			dos.writeBytes("Content-Type: image/jpeg" + LINEEND + LINEEND);
		
			//Log.i("NOMBRE ARCHIVO",nombre);
			int bytesAvailable = fileInputStream.available();
			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize); 
			byte[] buffer = new byte[bufferSize]; 
			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize); 
				bytesAvailable = fileInputStream.available(); 
				bufferSize = Math.min(bytesAvailable, maxBufferSize); 
				bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
			}
			// Close input stream
			fileInputStream.close();
			dos.writeBytes(LINEEND);
			
		} catch (IOException e) {
			Log.e("YAMBU", "IOException: " + e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	public JSONObject enviar(){
		
		JSONObject jObj = null;
		
		try {
			//esto indica que es el final de lo que se envia
			dos.writeBytes(TWOHYPHENS + BOUNDARY + TWOHYPHENS + LINEEND);
			dos.flush(); 
				
			////////////////////////////////////////////////////////////
            //LEEMOS LA SALIDA DEL SCRIPT PHP Y
            // CREAMOS EL OBJETO JSON A PARTIR DE LA RESPUESTA DEL SERVIDOR
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
         
            String line = null;
            while ((line = reader.readLine()) != null){
            	Log.w("PHP OUTPUT",line);
         	    stringBuilder.append(line + "\n");
            }
            dos.close();
            jObj = new JSONObject(stringBuilder.toString());
            					
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jObj;
			
	}
}
