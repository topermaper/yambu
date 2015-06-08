package com.marcosedo.yambu.app;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;
import com.marcosedo.yambu.login.Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends ActionBarActivity implements ListaEventosFragment.OnEditarEventoListener, ListaEventosFragment.OnMostrarImagenListener, ListaGruposFragment.OnEditarGrupoListener,
        ListaGruposFragment.OnMostrarImagenListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String TAG = "YAMBÚ";
    //fragments
    public FragmentManager fragmentManager;
    public MostrarDetalleEventoFragment detalleEventoFragment;
    public GoogleApiClient mGoogleApiClient;
    public Location lastLocation = null;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences preferences;
    Context context;
    Boolean is_activity_first_time = true;
    private Dialog dialogo;
    private BroadcastReceiver broadcastReceiver;
    /////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();

        //////////////////Start GcmIntentService///////////////////////
        Intent gcmIntentService = new Intent(this, GcmIntentService.class);
        startService(gcmIntentService);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                Comment comment = extras.getParcelable(GcmIntentService.EXTRA_COMMENT);
                if (detalleEventoFragment != null) {
                    detalleEventoFragment.addCommentToList(comment);
                }
            }
        };
        //register gcmBroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(GcmIntentService.ACTION_SENDCOMMENT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver, intentFilter);
        //////////////////////////////////////////////////////////////


        setContentView(R.layout.main_tab_layout);

        //DEFINE background, icon, subtitle
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        //////////////////////////////////////////
        mActionBar.setIcon(R.drawable.actionbaricon48);
        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbackground));

        Intent intent = new Intent(this, GcmIntentService.class);
        startService(intent);


        preferences = getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);

        //la actividad se lanza x primera vez. true es el valor que le damos en caso que no exista activityfirsttime

        String idevento = "";

        Bundle extras = getIntent().getExtras();


        if (extras != null) {
            Log.i("EXTRAS", extras.toString());
            String msg = extras.getString("msg");

            if (extras.getString("idevento") != null) {
                Bundle args = new Bundle();
                args.putString("idevento", extras.getString("idevento"));

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                detalleEventoFragment = new MostrarDetalleEventoFragment();
                detalleEventoFragment.setArguments(args);
                transaction.replace(R.id.contenedorfragment, detalleEventoFragment, "detalle evento");
                transaction.commit();// Commit the transaction

            }
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ListaEventosFragment listaFragment = new ListaEventosFragment();
            transaction.replace(R.id.contenedorfragment, listaFragment, "lista");
            //sin transaction.addToBackStack(null); para que no se recuerde la transaccion
            transaction.commit();// Commit the transaction
        }

        //MostrarDialogoNotificacion(msg, imagen);//no se envia null si no que se envia el logo de yambu


        if (is_activity_first_time) {
            Log.i("MainACtivity onCreate", "se lanza la actividad x primera vez");
            is_activity_first_time = false;//ya no será la primera vez a partir de ahora

            //Check device for Play Services APK.
            Log.i("MainACtivity onCreate", "comprobamos si hay servicio googleplay");
            if (checkPlayServices()) {
                Log.i("MainACtivity onCreate", "hay servicio googleplay");
                gcm = GoogleCloudMessaging.getInstance(this);
                registerInBackground();

            } else {
                Toast.makeText(this, "No valid Google Play Services APK found.", Toast.LENGTH_SHORT).show();
                Log.i("MainACtivity onCreate", "no hay servicio googleplay");
            }
        } else//se lanza por segunda vez
        {
            Log.i("MainACtivity onCreate", "se lanza la actividad x segunda vez o mas");

        }

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    //si cuando se lanza el intent la app estaba abierta se viene aqui porque hemos puesto FLAG_ACTIVITY_SINGLE_TOP
    protected void onNewIntent(Intent intent) {
        Log.i("MainActivity", "onNewIntent");

        Bundle extras = intent.getExtras();
        String msg = extras.getString("msg");

        if (extras.getString("idevento") != null) {
            String idevento = extras.getString("idevento");
            Bundle args = new Bundle();
            args.putString("idevento", idevento);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            detalleEventoFragment = new MostrarDetalleEventoFragment();
            detalleEventoFragment.setArguments(args);
            transaction.replace(R.id.contenedorfragment, detalleEventoFragment, "detalle evento");
            transaction.addToBackStack(null);
            transaction.commit();// Commit the transaction
            super.onNewIntent(intent);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("MainActivity", "onDestroy");
        unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.i("MainActivity", "onStart");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.i("MainActicity", "onStop");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {// Connected to Google Play services!
        Log.i("MainActivity", "onConnected");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            lastLocation = mLastLocation;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("TAG", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    private void MostrarDialogoNotificacion(String msgDialog, byte[] imagen) {
        Bitmap cartelBitmap;

        dialogo = new Dialog(this);
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);//Sin titulo, ya ponemo nosotros el nuestro
        dialogo.setContentView(R.layout.notificaciones_dialog);// Se establece el layout del diálogo

        TextView titulo = (TextView) dialogo.findViewById(R.id.TvTitulo);
        TextView tvNotificacion = (TextView) dialogo.findViewById(R.id.TvNotificacion);
        final ImageView ivCartel = (ImageView) dialogo.findViewById(R.id.ivImage);
        titulo.setText("Nueva Notificación");

        if ((imagen != null) && (imagen.length != 0)) {
            cartelBitmap = BitmapFactory.decodeByteArray(imagen, 0, imagen.length);
            int[] dim = Utilidades.calculaDimensionImagen(cartelBitmap.getWidth(), cartelBitmap.getHeight(), Constantes.MAX_WIDTH_NOTIFICACION, Constantes.MAX_HEIGHT_NOTIFICACION);
            cartelBitmap = Utilidades.redimensionarBitmap(cartelBitmap, dim[0], dim[1]);
            //Log.i("DIMENSIONES cartelbitmap",cartelBitmap.getWidth()+"-"+cartelBitmap.getHeight());
            //cartelBitmap = Utilidades.redondearBitmap(cartelBitmap, Constantes.RADIOPERCENT);

            ivCartel.setImageBitmap(cartelBitmap);

        }

        tvNotificacion.setText(msgDialog);

        /////////////LISTENERS DE LOS BOTONES DEL DIALOGO////////////////
        ((Button) dialogo.findViewById(R.id.BtnAceptar)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogo.dismiss();
            }
        });

        ((Button) dialogo.findViewById(R.id.BtnCancelar)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogo.dismiss();
            }
        });
        ///////////////////////////////////////////	
        dialogo.show();
    }


    public void onEditarEvento(Evento evento) {

        EditarEventosFragment editorFragment = new EditarEventosFragment();
        Bundle args = new Bundle();

        //Establecemos los argumentos
        args.putString(Constantes.EV_ID, evento.getId());
        args.putByteArray(Constantes.EV_THUMB, evento.getThumb());
        args.putString("placeid", evento.getPlaceId());
        args.putString(Constantes.EV_FECHA, evento.getFecha());
        args.putString(Constantes.EV_HORA, evento.getHora());
        args.putInt(Constantes.EV_PRECIO, evento.getPrecio());
        args.putString(Constantes.EV_URL, evento.getUrl());
        args.putString("idgrupo", evento.getIdGrupo());
        args.putString("currencycode", evento.getCurrencyCode());

        editorFragment.setArguments(args);

        //REOCGEMOS DATOS DEL FRAGMENT MANAGER//
        /*ragmentManager fragmentmanager = getSupportFragmentManager();
        Log.e("OnEditarEvento","FRAGMENTMANAGER.toSTring()"+fragmentmanager.toString());
    	List<Fragment> lista = fragmentmanager.getFragments();
    	Log.e("Lista FRagments",lista.toString());
      	int i;
    	for (i=0;i<lista.size();i++){
    		Log.e("fragment",lista.get(i).toString()+" i="+i);
    	}*/

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.remove(fragmentmanager.findFragmentByTag("editor"));
        transaction.replace(R.id.contenedorfragment, editorFragment, "editor");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction
    }

    public void onMostrarImagen(String path) {

        MostrarImagenFragment fragment = new MostrarImagenFragment();
        Bundle args = new Bundle();

        //Establecemos los argumentos
        args.putString("imagepath", path);

        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contenedorfragment, fragment, "show image");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction
    }

    public void onEditarGrupo(Grupo grupo) {

        EditarGruposFragment editorFragment = new EditarGruposFragment();
        Bundle args = new Bundle();

        //Establecemos los argumentos
        args.putString(Constantes.GP_ID, grupo.getId());
        args.putString(Constantes.GP_CREADOR, grupo.getCreador());
        args.putByteArray(Constantes.GP_THUMB, grupo.getThumb());
        args.putString(Constantes.GP_NOMBRE, grupo.getNombre());

        editorFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.contenedorfragment, editorFragment, "edit group");
        transaction.addToBackStack(null);
        transaction.commit();// Commit the transaction
    }

    //las opciones de cada fragment especificas estan en su propio fragment
    //las opciones comunes aqui en el main para no tenerlo todo desperdigado
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected del mainActivity");
        // Handle presses on the action bar items
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (item.getItemId()) {

            case R.id.action_listaEventos:
                ListaEventosFragment listaFragment = new ListaEventosFragment();
                transaction.replace(R.id.contenedorfragment, listaFragment, "event list");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;


            case R.id.action_logout:
                //borramos el email con el que nos hemos registrado
                SharedPreferences.Editor editor = preferences.edit();
                Log.i(TAG, preferences.getString(Constantes.SP_EMAIL_REG, "vacio"));
                editor.remove(Constantes.SP_EMAIL_REG);
                editor.commit();
                Log.i(TAG, preferences.getString(Constantes.SP_EMAIL_REG, "vacio"));
                //Y volvemos a la pantalla de login
                Intent loginActivity = new Intent(getApplicationContext(), Login.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginActivity);
                finish();//Cerramos la activity main y volvemos al login
                break;
            case R.id.action_listaGrupos://LISTA DE GRUPOS
                ListaGruposFragment lgfragment = new ListaGruposFragment();
                transaction.replace(R.id.contenedorfragment, lgfragment, "my groups");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
            case R.id.action_notificaciones://Notificaciones
                SendNotificationFragment sendNotificationFragment = new SendNotificationFragment();
                //Establecemos los argumentos
                transaction.replace(R.id.contenedorfragment, sendNotificationFragment, "notificaciones");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
            case R.id.action_profile://Perfil de usuario
                ProfileFragment creaPerfilFragment = new ProfileFragment();
                //Establecemos los argumentos
                transaction.replace(R.id.contenedorfragment, creaPerfilFragment, "notificaciones");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
            case R.id.action_help:
                HelpFragment helpFragment = new HelpFragment();
                //Establecemos los argumentos
                transaction.replace(R.id.contenedorfragment, helpFragment, "help");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
            case R.id.action_preferences:
                PreferencesFragment preferencesFragment = new PreferencesFragment();
                transaction.replace(R.id.contenedorfragment, preferencesFragment, "preferences");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // You need to do the Play Services APK check here too.
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /*private boolean checkAppVersion() {

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = preferences.getInt(Constantes.APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
       //Toast.makeText(this, "registeredVersion:" + Integer.toString(registeredVersion) + " currentVersion:" + currentVersion, Toast.LENGTH_LONG).show();

        if (registeredVersion != currentVersion) {
            Log.i("getRegistrationId", "App version changed.");
            return false;
        }
        return true;
    }*/




    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app.
     */
    private JSONObject sendRegistrationToBackend(String regid) {

        String devid = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        Log.i("GCM REGISTER", "enviamos regid =" + regid);
        Log.i("GCM REGISTER", "enviamos devid =" + devid);
        Log.i("GCM REGISTER", "enviamos email reg =" + preferences.getString(Constantes.SP_EMAIL_REG, ""));
        JSONObject json = null;

        HttpUploader uploader = null;
        try {
            uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.GCMSERVER_FILE);
            uploader.añadirArgumento("devid", devid);
            uploader.añadirArgumento("regid", regid);
            //Pasamos también el email con el que hemos hecho el login
            uploader.añadirArgumento("email_reg", preferences.getString(Constantes.SP_EMAIL_REG, ""));
            uploader.añadirArgumento("mode", "registro");
            json = uploader.enviar();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;

    }

    //Stores the registration ID and app versionCode in the application's
    /*private void storeRegistrationId(String regId) {

        Log.i(TAG, "Saving regId = " + regId + " on app version = " + getAppVersion());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constantes.REG_ID, regId);
        editor.putInt(Constantes.APP_VERSION, getAppVersion());
        editor.commit();
    }*/


    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.i("register in background", "vamos a registrarnos en background");
                String msg = "";
                String regid;
                try {
                    Log.i(TAG, "comprobamos si gcm = null");
                    if (gcm == null) {
                        Log.i(TAG, "gcm = null");
                        gcm = GoogleCloudMessaging.getInstance(context);
                    } else {
                        Log.i(TAG, "gcm no es null");
                    }

                    regid = gcm.register(Constantes.SENDER_ID);//ahora registramos siempre

                    //llamamos al server y nos registramos le enviamos el regid y el email
                    JSONObject respuesta = sendRegistrationToBackend(regid);

                    // Persist the regID - no need to ask for a new one
                    if (respuesta != null) {
                        if (respuesta.getInt(Constantes.JSON_SUCCESS) == 1) {
                            Log.i(TAG, "Guardamos regid = " + regid + " en preferencias");
                            storeRegistrationId(regid);

                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    msg = "Error :" + e.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = "Error :" + e.getMessage();
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!msg.isEmpty())
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }

        }.execute(null, null, null);
    }


    //Stores the registration ID and app versionCode in the application's
    private void storeRegistrationId(String regId) {

        Log.i(TAG, "Saving regId = " + regId + " on app version = " + Utilidades.getAppVersion(this));
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constantes.REG_ID, regId);
        editor.putInt(Constantes.APP_VERSION, Utilidades.getAppVersion(this));
        editor.commit();
    }



}