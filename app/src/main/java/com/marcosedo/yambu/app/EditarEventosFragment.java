package com.marcosedo.yambu.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class EditarEventosFragment extends Fragment implements OnClickListener {

    /////IMAGEN
    public static final int SELECT_IMAGE = 1;
    public static final int SELECT_LOCATION = 2;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    public Bundle bundle;//BUNDLE
    // Dialog
    private ProgressDialog pDialog;
    private Dialog dialogo;
    ///////////////////////////
    private BuildOptionsMenu buildoptionsmenu;
    private Bitmap bmOriginal;
    private Uri selectedImage = null;
    private String selectedCurrency = "";
    private String idevento = null;
    private String cartelpath = null;
    private byte[] cartelByteArray = null;
    ///////////////////////////////////////
    private EditText etFecha;
    private EditText etHora;
    private TextView urltv;
    private TextView tvLugar;
    private TextView preciotv;
    private Spinner spCurrency;
    private ImageView carteliv;
    private Button okbtn;
    private Button btnLocation;
    private Spinner spinner;
    private TextView tvAttributions;
    private ImageView ivPoweredBy;
    //private ArrayList<String> listaSpinner = new ArrayList<String>();//esta es la lista con la que poblaremos el spinner
    private ArrayList<Grupo> listaGrupos = new ArrayList<Grupo>();
    private ArrayAdapter<String> adapter;//adapter del spinner
    private ArrayAdapter<String> currencyAdapter;
    private String idgrupo;
    //si el evento es nuevo FALSE, si se modifica TRUE
    private boolean editmode = false;//es false si no se demuestra lo contrario
    private Place selectedPlace;//es el lugar que hemos seleccionado
    //private int selected_group_position;//posicion del grupo que estamos editando, -1 es que no hay nada seleccionadp


    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("place", "Place query did not complete. Error: " + places.getStatus().toString());

                return;
            }
            // Get the Place object from the buffer.
            if (places.getCount() > 0) {
                selectedPlace = places.get(0);
                // Format details of the place for display and show it in a TextView.
                //mPlaceDetailsText.
                //Toast.makeText(getActivity(), place.toString(), Toast.LENGTH_LONG).show();
                tvLugar.setText(selectedPlace.getName() + "\n" + selectedPlace.getAddress());

                if (tvLugar.getText().length() > 0) {//si hay algo de texto mostramos attributions
                    ivPoweredBy.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i("TRACE", "onCreateView EditarEventosFragment");

        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones

        View view = inflater.inflate(R.layout.editar_eventos_layout, container, false);

        //INICIALIZAMOS VARIABLES GLOBALES
        etFecha = (EditText) view.findViewById(R.id.etFecha);
        etHora = (EditText) view.findViewById(R.id.etHora);
        urltv = (TextView) view.findViewById(R.id.etUrl);
        tvLugar = (TextView) view.findViewById(R.id.tvLugar2);
        preciotv = (TextView) view.findViewById(R.id.etPrecio);
        spCurrency = (Spinner) view.findViewById(R.id.spCurrency);
        carteliv = (ImageView) view.findViewById(R.id.ivCartel);
        okbtn = (Button) view.findViewById(R.id.btnOk);
        spinner = (Spinner) view.findViewById(R.id.spinner);
        btnLocation = (Button) view.findViewById(R.id.btnLocation);
        tvAttributions = (TextView) view.findViewById(R.id.tvAttributions);
        ivPoweredBy = (ImageView) view.findViewById(R.id.poweredBy);

        //PONEMOS EL LISTENER EN los botones
        carteliv.setOnClickListener(this);
        okbtn.setOnClickListener(this);
        etFecha.setOnClickListener(this);
        etHora.setOnClickListener(this);
        btnLocation.setOnClickListener(this);

        //inicializamos la imagen para que no pete luego
        bmOriginal = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_menu_gallery);


        //Obtenemos los argumentos que le hayamos podido pasar a la actividad
        //bundle = savedInstanceState;
        bundle = getArguments();
        if (bundle != null) {
            idevento = bundle.getString("_id");
            if (idevento != null) {  //Si tenemos la id es porque hay que editar
                editmode = true;

                //capturamos los extras con elbytearray del cartel
                cartelByteArray = bundle.getByteArray(Constantes.EV_THUMB);
                //PONEMOS LA IMAGEN EN EL IMAGEVIEW
                if (cartelByteArray.length > 0) {
                    bmOriginal = BitmapFactory.decodeByteArray(cartelByteArray, 0, cartelByteArray.length);
                    //Bitmap bitmap = Utilidades.redimensionarBitmap(bmOriginal, carteliv.getWidth(), carteliv.getLayoutParams().height);
                    //bitmap = Utilidades.redondearBitmap(bitmap, Constantes.RADIOPERCENT);
                    //carteliv.setImageBitmap(bitmap);
                }


                //rellenamos todos los campos restantes
                etFecha.setText(bundle.getString("fecha"));
                etHora.setText(bundle.getString("hora"));
                //etLugar.setText(bundle.getString("lugar"));
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(((MainActivity) getActivity()).mGoogleApiClient, bundle.getString("placeid"));
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

                preciotv.setText(Integer.toString(bundle.getInt("precio")));
                urltv.setText(bundle.getString("url"));

                idgrupo = bundle.getString("idgrupo");
                selectedCurrency = bundle.getString("currencycode");
            }
        }


        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        if (editmode == true) {
            mActionBar.setSubtitle("Editing event");
        } else {
            mActionBar.setSubtitle("Creating event");
        }

        //cargamos el spinner con nuestra lista
        new CargarSpinner().execute();

        //cargar Spinner de monedas
        Load_spCurrency(selectedCurrency);


        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        cargaCartelImageView();//se tiene que cargar en el onviewcreated porque si no no podemos obtener
        // el widht de la imageview hasta que no se ha creado la vista
    }

    // Ponemos nuevos botones en la actionbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        buildoptionsmenu.setOptions(new boolean[]{false, false, false});
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_editarEvento);

        super.onCreateOptionsMenu(menu, inflater);
    }


    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.etHora:
                showTimePickerDialog(v);
                break;
            case R.id.etFecha:
                showDatePickerDialog(v);
                break;

            case R.id.ivCartel:
                seleccionarCartel();
                break;
            case R.id.btnOk:
                String fecha = etFecha.getText().toString();
                String hora = etHora.getText().toString();

                if (validarFecha(fecha)) {
                    if (validarHora(hora)) {
                        if (selectedPlace != null) {
                            new SaveEvent().execute();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Select a place for the event", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Time format wrong. Use hh/mm", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Date format wrong. Use dd/mm/yyyy", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnLocation:
                // Construct an intent for the place picker
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(getActivity());
                    // Start the intent by requesting a result,
                    // identified by a request code.
                    startActivityForResult(intent, SELECT_LOCATION);

                } catch (GooglePlayServicesRepairableException e) {
                    // ...
                } catch (GooglePlayServicesNotAvailableException e) {
                    // ...
                }
        }
    }


    public void seleccionarCartel() {
        try {
            final String[] items = {"Galería", "Eliminar foto"};
            final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery,
                    R.drawable.ic_menu_delete};

            ArrayAdapterWithIcon adapter = new ArrayAdapterWithIcon(getActivity(), items, icons, null);

            Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Imagen del cartel");
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0://SELECTING A NEW IMAGE FROM GALLERY
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_IMAGE);
                            break;
                        case 1://DELETING THE IMAGE
                            Resources res = getActivity().getResources();
                            cartelpath = null;
                            cartelByteArray = null;
                            selectedImage = null;
                            carteliv.setImageDrawable(res.getDrawable(R.drawable.ic_menu_gallery));
                            break;
                    }
                }
            });

            builder.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("LOG", "OnactivityREsult");

        switch (requestCode) {
            case SELECT_IMAGE:
                try {
                    if (resultCode == Activity.RESULT_OK) {
                        selectedImage = data.getData();
                    }
                    if (selectedImage != null) {
                        bmOriginal = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                    }
                    cargaCartelImageView();
                    /////////////////////////////////////////////////////
                    //Crear fichero con la foto perfil en jpeg
                    /////////////////////////////////////////////////////
                    int[] dim;
                    int WIDTH = 0;
                    int HEIGHT = 1;

                    ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                    File dirImages = cw.getDir("PerfilImages", Context.MODE_PRIVATE);
                    File cartelFile = new File(dirImages, "cartelTmp" + ".jpg");
                    if (cartelFile.exists()) {
                        cartelFile.delete();
                    }
                    cartelpath = cartelFile.getAbsolutePath();
                    cartelFile.createNewFile();

                    dim = Utilidades.calculaDimensionImagen(bmOriginal.getWidth(), bmOriginal.getHeight(), Constantes.MAX_WIDTH_CARTEL, Constantes.MAX_WIDTH_CARTEL);
                    Utilidades.creaJPEG(bmOriginal, cartelpath,
                            dim[WIDTH], dim[HEIGHT], Constantes.JPEG_QUALITY_CARTEL);
                    /////////////////////////////////////////////////////////////////////

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            case SELECT_LOCATION:
                if (resultCode == MainActivity.RESULT_OK) {
                    selectedPlace = PlacePicker.getPlace(data, getActivity());
                    //Toast.makeText(getActivity(), selectedPlace.getId(), Toast.LENGTH_LONG).show();
                    tvLugar.setText(selectedPlace.getName() + "\n" + selectedPlace.getAddress());
                    String attributions = PlacePicker.getAttributions(data);
                    if (attributions != null) {
                        tvAttributions.setText(Html.fromHtml(attributions));
                        tvAttributions.setVisibility(View.VISIBLE);
                    } else {
                        tvAttributions.setVisibility(View.GONE);
                    }

                    ivPoweredBy.setVisibility(View.VISIBLE);
                    break;
                }
        }
    }


    public void cargaCartelImageView() {
        /*//PONEMOS LA IMAGEN EN EL IMAGEVIEW
        RelativeLayout layout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayoutEditarEventos);
        Log.i("cargaCartelImageView", "layout = " + layout.toString());
        float layoutWidth = layout.getWidth();
        float layoutHeight = layout.getHeight();
        Log.i("cargaCartelImageView", Integer.toString(layout.getWidth()) + Integer.toString(layout.getHeight()));
        float bitmapWidth = bmOriginal.getWidth();
        float bitmapHeight = bmOriginal.getHeight();
        float ratio = bitmapWidth / bitmapHeight;
        Log.i("LAyout WIDTH AND HEIGHT", Float.toString(layoutWidth) + " " + Float.toString(layoutHeight));
        Log.i("BITMAP WIDTH AND HEIGHT", Float.toString(bitmapWidth) + " " + Float.toString(bitmapHeight));
        Log.i("ratio", Float.toString(ratio));

        //Hacemos el ImageView igual al tamaño de la foto.-10 para dejar un margen con el RelativeLayout
        //carteliv.getLayoutParams().width = (int) (layoutWidth - 10);
        //carteliv.getLayoutParams().height = (int) ((layoutWidth - 10)/ratio);
        Bitmap bitmap = Utilidades.redimensionarBitmap(bmOriginal, carteliv.getLayoutParams().width, carteliv.getLayoutParams().height);*/
        carteliv.setImageBitmap(Utilidades.redondearBitmap(bmOriginal, Constantes.RADIOPERCENT));

        ///////////////////////////////////////////////////////
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        //newFragment.show(getSupportFragmentManager(),"timePicker");
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        //newFragment.show(getSupportFragmentManager(),"datePicker");
        newFragment.show(getFragmentManager(), "datePicker");
    }


    boolean validarFecha(String fecha) {

        String[] fechaSplit = fecha.split("/");
        int[] fechaSplitInt = new int[3];

        if (fechaSplit.length == 3) {//si longitud 3 es bien :).........SEGUIMOS
            for (int i = 0; i < fechaSplit.length; i++) {//Pasamos los strings a enteros para poder trabajar
                try {
                    fechaSplitInt[i] = Integer.parseInt(fechaSplit[i]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            //1<MES<12
            if ((fechaSplitInt[Constantes.MES] < 1) || (fechaSplitInt[Constantes.MES] > 12)) {
                Log.i("valida", fechaSplitInt[Constantes.MES] + "mes");
                return false;
            }

            //1<DIA<12
            if ((fechaSplitInt[Constantes.DIA] < 1) || (fechaSplitInt[Constantes.DIA] > 31)) {
                Log.i("valida", "dia");
                return false;
            }

        }//longitud != 3 es mal...........
        else {
            Log.i("valida", "return false");
            return false;
        }
        return true;
    }

    boolean validarHora(String hora) {

        String[] horaSplit = hora.split(":");
        int[] horaSplitInt = new int[3];

        if (horaSplit.length == 2) {//si longitud 2 es bien :).........SEGUIMOS
            for (int i = 0; i < horaSplit.length; i++) {//Pasamos los strings a enteros para poder trabajar
                try {
                    horaSplitInt[i] = Integer.parseInt(horaSplit[i]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            //0<HORA<23
            if ((horaSplitInt[Constantes.HOR] < 0) || (horaSplitInt[Constantes.HOR] > 23)) {
                Log.i("valida", horaSplitInt[Constantes.HOR] + "hora");
                return false;
            }

            //0<MIN<59
            if ((horaSplitInt[Constantes.MIN] < 0) || (horaSplitInt[Constantes.MIN] > 59)) {
                Log.i("valida", "dia");
                return false;
            }

        }//longitud != 2 es mal...........
        else {
            Log.i("valida", "return false");
            return false;
        }
        return true;
    }

    /*private void MostrarDialogoCrearGrupo() {//si no hay ningun grupo a nuestro nombre no podremos crear ningun evento

        dialogo = new Dialog(getActivity());
        //deshabilitamos el título por defecto
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Se establece el layout del diálogo
        dialogo.setContentView(R.layout.dialog_title_text_accept_cancel);

        TextView titulo = (TextView) dialogo.findViewById(R.id.TvTitulo);
        TextView tvTexto = (TextView) dialogo.findViewById(R.id.TvTexto);
        titulo.setText("¿Create a group?");
        tvTexto.setText("You need to create a group before adding any event. ¿Do you want to create now?");

        /////////////LISTENERS DE LOS BOTONES DEL DIALOGO////////////////
        ((Button) dialogo.findViewById(R.id.BtnAceptar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle args = new Bundle();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                EditarGruposFragment fragment = new EditarGruposFragment();

                transaction.replace(R.id.contenedorfragment, fragment, "create_group");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction

                dialogo.dismiss();
            }
        });

        ((Button) dialogo.findViewById(R.id.BtnCancelar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogo.dismiss();
            }
        });
        ///////////////////////////////////////////

        dialogo.show();
    }*/
    public void Load_spCurrency(String selectedCurrency) {//selectedCurrency sera distinto de "" si queremos que se seleccione uno por defecto
        currencyAdapter = new SpinnerCurrenciesCustomAdapter((MainActivity) getActivity(), R.layout.linea_spinner_currencies, Constantes.currenciesList, getResources());
        spCurrency.setAdapter(currencyAdapter);
        for (int i = 0; i < spCurrency.getCount(); i++) {
            Currency currency = (Currency) spCurrency.getItemAtPosition(i);
            if (currency.getCode().equals(selectedCurrency)) {
                spCurrency.setSelection(i);
                break;
            }
        }
        currencyAdapter.notifyDataSetChanged();
        //spCurrency.setOnItemSelectedListener(this);
    }

    //////////////////////////////////////////////////////
    ///Async Task para guardar un evento en la BBDD///
    //////////////////////////////////////////////////////
    class SaveEvent extends AsyncTask<String, String, JSONObject> {

        String placename;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            //pDialog = new ProgressDialog(EditarEventosActivity.this);
            pDialog.setMessage("Creating event ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();
            FileInputStream fis;

            LatLng latlng = selectedPlace.getLatLng();

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.EDITAR_EVENTOS_FILE);

                /////enviamos para guardar el Place en nuestra bbdd
                placename = selectedPlace.getName().toString();
                uploader.añadirArgumento("timezoneoffset", Utilidades.getTimeZoneOffset(getActivity()));
                uploader.añadirArgumento("placeid", selectedPlace.getId());
                uploader.añadirArgumento("placename", placename);
                uploader.añadirArgumento("address", selectedPlace.getAddress().toString());
                uploader.añadirArgumento("latitude", Double.toString(latlng.latitude));
                uploader.añadirArgumento("longitude", Double.toString(latlng.longitude));
                //////////////////////////////////////////////
                uploader.añadirArgumento("fecha", etFecha.getText().toString());
                uploader.añadirArgumento("hora", etHora.getText().toString());
                uploader.añadirArgumento("precio", preciotv.getText().toString());
                uploader.añadirArgumento("url", urltv.getText().toString());
                Grupo selectedgroup = (Grupo) spinner.getSelectedItem();
                uploader.añadirArgumento("idgrupo", selectedgroup.getId());
                uploader.añadirArgumento("currencycode", ((Currency) spCurrency.getSelectedItem()).getCode());

                if (editmode) { //parametro que pasamos al script para que cree uno nuevo o modifique uno ya existente
                    uploader.añadirArgumento("editmode", "1");
                    uploader.añadirArgumento("id", idevento);
                } else {
                    uploader.añadirArgumento("editmode", "0");
                }

                if (cartelpath != null) {
                    try { ////////  Leemos el archivo del cartel
                        fis = new FileInputStream(cartelpath);
                        uploader.añadirArchivo("cartel.jpg", fis);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                json = uploader.enviar();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    json.put(Constantes.JSON_MESSAGE, Constantes.MSG_ERROR_CONEXION);
                    json.put(Constantes.JSON_SUCCESS, 0);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            return json;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(JSONObject json) {
            // check for success tag
            int success;
            String mensaje;
            try {
                success = json.getInt(TAG_SUCCESS);
                mensaje = json.getString(TAG_MESSAGE);
                if (success == 1) {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    idevento = json.getString("idevento");
                    idgrupo = json.getString("idgrupo");
                    MostrarDialogoEnvioNotificacion();
                } else {
                    //Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
            //finish();
        }

        private void MostrarDialogoEnvioNotificacion() {

            dialogo = new Dialog(getActivity());
            //deshabilitamos el título por defecto
            dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
            // Se establece el layout del diálogo
            dialogo.setContentView(R.layout.dialog_title_text_accept_cancel);

            TextView titulo = (TextView) dialogo.findViewById(R.id.TvTitulo);
            TextView tvTexto = (TextView) dialogo.findViewById(R.id.TvTexto);
            titulo.setText("Send notification?");
            if (editmode)
                tvTexto.setText("The event has been modified. Would you like to send a notification to your followers?");
            else
                tvTexto.setText("The event has been created. Would you like to send a notification to your followers?");
            /////////////LISTENERS DE LOS BOTONES DEL DIALOGO////////////////
            ((Button) dialogo.findViewById(R.id.BtnAceptar)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle args = new Bundle();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    SendNotificationFragment sendNotificationFragment = new SendNotificationFragment();

                    //Establecemos los argumentos
                    Grupo grupo = (Grupo) spinner.getSelectedItem();
                    String msg = "Live performance of " + grupo.getNombre() + " " + etFecha.getText().toString() + " @ " + placename + ".";
                    args.putString("mensaje", msg);
                    args.putString("idevento", idevento);
                    args.putString("idgrupo", idgrupo);
                    args.putByteArray("cartel", cartelByteArray);
                    sendNotificationFragment.setArguments(args);
                    transaction.replace(R.id.contenedorfragment, sendNotificationFragment, "notificaciones");
                    //transaction.addToBackStack(null);
                    transaction.commit();// Commit the transaction
                    dialogo.dismiss();
                }
            });

            ((Button) dialogo.findViewById(R.id.BtnCancelar)).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogo.dismiss();
                    getActivity().getSupportFragmentManager().popBackStackImmediate();//esto nos lleva al fragment anterior. es como si apretasemos atras
                }
            });
            ///////////////////////////////////////////

            dialogo.show();
        }

    }

    class CargarSpinner extends AsyncTask<String, String, JSONObject> {
        ProgressDialog pDialog;
        private JSONObject json;
        private JSONObject jsonImage;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Cargando ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            json = new JSONObject();
            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.ADMIN_GRUPOS_FILE);
                uploader.añadirArgumento("mode", "0");//0 leer 1 crear 2 modificar 3 borrar
                uploader.añadirArgumento("email_reg", getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE).getString(Constantes.SP_EMAIL_REG, ""));
                uploader.añadirArgumento("thumb_height", Integer.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("thumb_width", Integer.toString(Constantes.THUMB_SIZE));
                json = uploader.enviar();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    json.put(Constantes.JSON_MESSAGE, Constantes.MSG_ERROR_CONEXION);
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
            //int groupPosition = -1;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien

                    listaGrupos.clear();//resteamos la lista IMPORTANTE si no aumenta de tamañana cada vez que recargamos

                    JSONArray grupos = json.getJSONArray(Constantes.JSON_GRUPOS);

                    for (int i = 0; i < grupos.length(); i++) {// loop recorriendo todos los grupos
                        JSONObject c = grupos.getJSONObject(i);

                        String id = c.getString(Constantes.GP_ID);
                        String nombre = c.getString(Constantes.GP_NOMBRE);
                        String creador = c.getString(Constantes.GP_CREADOR);
                        String thumb_encoded = c.getString(Constantes.GP_THUMB);

                        byte[] thumb = Base64.decode(thumb_encoded, Base64.DEFAULT);

                        Grupo grupo = new Grupo(id, thumb, nombre, creador);

                        listaGrupos.add(grupo);

                        if ((idgrupo != null) && (id.equals(idgrupo))) {//si este es el grupo del evento que estamos editando
                            spinner.setSelection(i);
                            //groupPosition = i;//guardamos que lo hemos encontrado en la posicion i
                        }

                    }


                    if (adapter == null) {
                        //adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, listaSpinner);
                        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//esto es el layout del menu desplegable
                        adapter = new SpinnerGruposCustomAdapter((MainActivity) getActivity(), R.layout.linea_spinner_grupos, listaGrupos, getResources());
                    }

                    spinner.setAdapter(adapter);
                    /*if (groupPosition >= 0) {//si hemos encontrado el grupo vamos a esa posicion ahora que ya tenemos el adapter asociado
                        spinner.setSelection(groupPosition);
                    }*/
                    adapter.notifyDataSetChanged();


                    ////////////////////////////////////////

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

}