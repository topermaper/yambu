package com.marcosedo.yambu.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;
import com.marcosedo.yambu.app.Constantes;
import com.marcosedo.yambu.app.HttpUploader;
import com.marcosedo.yambu.app.MainActivity;
import com.marcosedo.yambu.app.UserFunctions;
import com.marcosedo.yambu.app.Utilidades;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Login extends ActionBarActivity {

    private Button btnLogin;
    private EditText inputEmail;
    private EditText inputPassword;
    private RadioGroup radioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);

        //si el email esta guardado en las shared preferences es porque hemos hecho logon
        if (preferences.getString(Constantes.SP_EMAIL_REG, "") != "") {
            launchApp();
        }

        launchUpdateActivity();//tiene que lanzarse el update despues de la app

        setContentView(R.layout.activity_login);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.pword);
        btnLogin = (Button) findViewById(R.id.btn_login);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group1);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        mActionBar.setIcon(R.drawable.actionbaricon48);
        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbackground));
        mActionBar.setSubtitle("Log in");


/**
 * Login button click event
 * A Toast is set to alert when the Email and Password field is empty
 **/
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                int selected = radioGroup.getCheckedRadioButtonId();

                switch (selected) {
                    case R.id.radio_yes:
                        if ((!inputEmail.getText().toString().equals("")) && (!inputPassword.getText().toString().equals(""))) {
                            NetAsync(view);
                        } else if ((!inputEmail.getText().toString().equals(""))) {
                            Toast.makeText(getApplicationContext(),
                                    "Introduzca contraseña", Toast.LENGTH_SHORT).show();
                        } else if ((!inputPassword.getText().toString().equals(""))) {
                            Toast.makeText(getApplicationContext(),
                                    "Introduzca e-mail", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Introduzca e-mail y contraseña", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.radio_no:
                        Intent myIntent = new Intent(view.getContext(), Register.class);
                        startActivityForResult(myIntent, 0);
                        finish();
                        break;
                }
            }
        });
    }

    public void NetAsync(View view) {
        new NetCheck().execute();
    }

    /**
     * Async Task to check whether internet connection is working.
     */
    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(Login.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        /**
         * Gets current device state and checks for working internet connection by trying Google.
         */
        @Override
        protected Boolean doInBackground(String... args) {

            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean th) {

            if (th) {
                nDialog.dismiss();
                new ProcessLogin().execute();
            } else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(),
                        "Error en la conexión de red", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Async Task to get and send data to My Sql database through JSON respone.
     */
    private class ProcessLogin extends AsyncTask<String, String, JSONObject> {


        String email, password;
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            inputEmail = (EditText) findViewById(R.id.email);
            inputPassword = (EditText) findViewById(R.id.pword);
            email = inputEmail.getText().toString();
            password = inputPassword.getText().toString();
            pDialog = new ProgressDialog(Login.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.loginUser(email, password);
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                String success = json.getString(Constantes.JSON_SUCCESS);
                String error = json.getString(Constantes.JSON_ERROR);

                if (Integer.parseInt(success) == 1) {//si ha ido bien
                    //GUArdamos el email con el que nos hemos logeado para asociarlo luego con el dispositivo
                    SharedPreferences preferences = getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constantes.SP_EMAIL_REG, email);//salvamos el email con el que hacemos login
                    editor.commit();

                    pDialog.dismiss();

                    launchApp();

                } else {//si ha ido mal
                    switch (Integer.parseInt(error)) {
                        case 1:
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Nombre de usuario o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "La cuenta no está activada. Revise su buzón de correo", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void launchApp(){
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
        finish();//Finalizamos la actividad
    }

    public void launchUpdateActivity(){
        Intent intent = new Intent(getApplicationContext(), checkUpdateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}