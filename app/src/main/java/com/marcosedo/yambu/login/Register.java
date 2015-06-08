package com.marcosedo.yambu.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.marcosedo.yambu.R;
import com.marcosedo.yambu.app.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Register extends ActionBarActivity {

    /**
     * Defining layout items.
     */
    EditText inputFirstName;
    EditText inputLastName;
    EditText inputUsername;
    EditText inputEmail;
    EditText inputPassword;
    EditText inputRepeatPassword;
    Button btnRegister;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        //Ponemos el logo ,fondo y subtitulo a la actionbar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        mActionBar.setIcon(R.drawable.actionbaricon48);
        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbackground));
        mActionBar.setSubtitle("Register");
        //////////////////////

        //Defining all layout items
        inputFirstName = (EditText) findViewById(R.id.fname);
        inputLastName = (EditText) findViewById(R.id.lname);
        inputUsername = (EditText) findViewById(R.id.uname);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        inputRepeatPassword = (EditText) findViewById(R.id.repeatpassword);
        btnRegister = (Button) findViewById(R.id.register);

        /**
         * Button which Switches back to the login screen on clicked
         **/

        Button login = (Button) findViewById(R.id.bktologin);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Login.class);
                startActivityForResult(myIntent, 0);
                finish();
            }

        });

        /**
         * Register Button click event.
         * A Toast is set to alert when the fields are empty.
         * Another toast is set to alert Username must be 5 characters.
         **/

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFields()) {
                    NetAsync(view);//esto comprueba la red y luego registra
                }

            }
        });
    }

    private boolean checkFields() {
        if ((inputUsername.getText().toString().equals("")) || (inputFirstName.getText().toString().equals("")) || (inputLastName.getText().toString().equals("")) || (inputEmail.getText().toString().equals(""))) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_fields_error), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (inputPassword.getText().toString().length() < 6) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_lenght_error), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (inputPassword.getText().toString().compareTo(inputRepeatPassword.getText().toString()) != 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.password_mismatch_error), Toast.LENGTH_SHORT).show();
            return false;
        }


        if (inputUsername.getText().toString().length() <= 4) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.username_lenght_error), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void NetAsync(View view) {
        new NetCheck().execute();
    }

    /**
     * Async Task to check whether internet connection is working
     */

    private class NetCheck extends AsyncTask<String, String, Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(Register.this);
            nDialog.setMessage("Loading...");
            nDialog.setTitle("Checking Network");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... args) {


/**
 * Gets current device state and checks for working internet connection by trying Google.
 **/
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
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean th) {

            if (th == true) {
                nDialog.dismiss();
                new ProcessRegister().execute();
            } else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error en la conexión de red", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ProcessRegister extends AsyncTask<String, String, JSONObject> {

        String email, fname, lname, uname, password;
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            fname = inputFirstName.getText().toString();
            lname = inputLastName.getText().toString();
            email = inputEmail.getText().toString();
            uname = inputUsername.getText().toString();
            password = inputPassword.getText().toString();

            pDialog = new ProgressDialog(Register.this);
            pDialog.setTitle("Connecting server ...");
            pDialog.setMessage("Registering ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            UserFunctions userFunction = new UserFunctions();
            //Log.i("DEBUG",fname+lname+email+uname);
            JSONObject json = userFunction.registerUser(fname, lname, email, uname, password);

            return json;
        }


        @Override
        protected void onPostExecute(JSONObject json) {
            /**
             * Checks for success message.
             **/
            try {
                if (json.getString("success") != null) {

                    String success = json.getString("success");
                    String error = json.getString("error");

                    //Toast.makeText(getApplicationContext(),"codigo de success : "+res,Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"codigo de error : "+red,Toast.LENGTH_LONG).show();


                    if (Integer.parseInt(success) == 1) {
                        pDialog.setTitle("Getting Data");
                        pDialog.setMessage("Loading Info");

                        Toast.makeText(getApplicationContext(), "Successful registered", Toast.LENGTH_LONG).show();

                        JSONObject json_user = json.getJSONObject("user");

                        // Launch Registered screen
                        Intent registered = new Intent(getApplicationContext(), Registered.class);


                        Bundle args = new Bundle();
                        args.putString("fname", json_user.getString("fname"));
                        args.putString("lname", json_user.getString("lname"));
                        args.putString("uname", json_user.getString("uname"));
                        args.putString("email", json_user.getString("email"));
                        args.putString("created_at", json_user.getString("created_at"));

                        registered.putExtras(args);

                        //Close all views before launching Registered screen
                        registered.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        pDialog.dismiss();
                        startActivity(registered);

                        finish();
                    } else if (Integer.parseInt(error) == 1) {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Email o contraseña incorrectos.", Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(error) == 2) {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "El email ya está en uso por otro usuario.", Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(error) == 3) {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "E-mail no válido.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    pDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Ha ocurrido un error durante el registro.", Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
    }
}