package com.marcosedo.yambu.login;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;
import com.marcosedo.yambu.app.Constantes;
import com.marcosedo.yambu.app.HttpUploader;
import com.marcosedo.yambu.app.Utilidades;

import org.json.JSONException;
import org.json.JSONObject;

public class checkUpdateActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i("checkUpdate", "onCreate");
        super.onCreate(savedInstanceState);
        new checkUpdateApp().execute();

    }


    class checkUpdateApp extends AsyncTask<String, String, JSONObject> {

        private JSONObject json;

        protected JSONObject doInBackground(String... args) {

            json = new JSONObject();
            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.LAST_VERSIONCODE);
                uploader.añadirArgumento("versioncode", Integer.toString(Utilidades.getAppVersion(getApplicationContext())));//enviamos la version de nuestra app
                json = uploader.enviar();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    json.put(Constantes.JSON_MESSAGE, "");
                    json.put(Constantes.JSON_SUCCESS, 0);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
            return json;
        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(JSONObject json) {

            int success;
            String mensaje;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                //mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien

                    int lastVersionCode = Integer.parseInt(json.getString("versioncode"));
                    if (lastVersionCode > Utilidades.getAppVersion(getApplicationContext())) {//NEED TO DO AN UPDATE
                        //oh yeah we do need an upgrade, let the user know send an alert message
                        AlertDialog.Builder builder = new AlertDialog.Builder(checkUpdateActivity.this);
                        builder.setCancelable(false).setMessage("There is newer version of this application available, would you like to upgrade now?")
                                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    //if the user agrees to upgrade
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton(getResources().getString(R.string.remind_later), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });
                        //show the alert message
                        builder.create().show();

                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}