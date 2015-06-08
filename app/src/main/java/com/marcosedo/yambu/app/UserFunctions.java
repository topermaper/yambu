package com.marcosedo.yambu.app;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.login.Login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;




public class UserFunctions {

	/**
	 * Function to Login
	 **/
    public JSONObject loginUser(String email, String password){

        HttpUploader uploader = null;
        try {
            uploader = new HttpUploader(BuildConfig.DOMAIN+Constantes.LOGIN_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploader.añadirArgumento("tag", Constantes.LOGIN_TAG_LOGIN);
    	uploader.añadirArgumento("email", email);
    	uploader.añadirArgumento("password", password);
        JSONObject json = uploader.enviar();
        return json;
    }

    /**
     * Function to change password
     **/
    public JSONObject chgPass(String newpas, String email){

        HttpUploader uploader = null;
        try {
            uploader = new HttpUploader(BuildConfig.DOMAIN+Constantes.LOGIN_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploader.añadirArgumento("tag", Constantes.LOGIN_TAG_CHGPASS);
    	uploader.añadirArgumento("newpas", newpas);
    	uploader.añadirArgumento("email", email);
        JSONObject json = uploader.enviar();
        return json;
    }


    /**
     * Function to reset the password
     **/
    public JSONObject forPass(String forgotpassword){

        HttpUploader uploader = null;
        try {
            uploader = new HttpUploader(BuildConfig.DOMAIN+Constantes.LOGIN_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploader.añadirArgumento("tag", Constantes.LOGIN_TAG_FORPASS);
    	uploader.añadirArgumento("forgotpassword", forgotpassword);
        JSONObject json = uploader.enviar();
        return json;  	
    }


    /**
     * Function to  Register
     **/
    public JSONObject registerUser(String fname, String lname, String email, String uname, String password){

        HttpUploader uploader = null;
        try {
            uploader = new HttpUploader(BuildConfig.DOMAIN+Constantes.LOGIN_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uploader.añadirArgumento("tag", Constantes.LOGIN_TAG_REGISTER);
    	uploader.añadirArgumento("fname", fname);
    	uploader.añadirArgumento("lname", lname);
    	uploader.añadirArgumento("email", email);
    	uploader.añadirArgumento("uname", uname);
    	uploader.añadirArgumento("password", password);
        JSONObject json = uploader.enviar();
        return json;
    }

    /**
    /// borra el email con el que nos hemos registrado de las shared preferences y vuelve
    /// a la pantalla de login
    ///*/
    public boolean logoutUser(Context context){
        //borramos el email con el que nos hemos registrado
        SharedPreferences preferences = context.getSharedPreferences(Constantes.SP_FILE,Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(Constantes.SP_EMAIL_REG);
        editor.commit();

        //Y volvemos a la pantalla de login
        Intent loginActivity = new Intent(context, Login.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(loginActivity);
        ((Activity) context).finish();//Cerramos la ventana del login
        return true;
    }

}