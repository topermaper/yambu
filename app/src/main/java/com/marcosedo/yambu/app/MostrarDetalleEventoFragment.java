package com.marcosedo.yambu.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MostrarDetalleEventoFragment extends Fragment implements View.OnClickListener {

    private static Place place;
    private ImageView ivCartel;
    private TextView tvLugar;
    private GoogleMap map;
    private TextView tvFecha;
    private TextView tvHora;
    private TextView tvPrecio;
    private TextView tvUrl;
    private TextView tvGroupName;
    private Button btnPublish;
    private ListView listView;
    private String idevento;
    private ArrayList<Comment> listaComments;
    private ListaCommentsCustomAdapter commentsadapter;
    private SharedPreferences preferences;
    private TextView tvCountComments;
    private EditText etCommentTxt;
    private BuildOptionsMenu buildoptionsmenu;
    private Button btnShowMore;
    private ImageView ivPoweredByGoogle;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("place", "Place query did not complete. Error: " + places.getStatus().toString());

                return;
            }
            Log.e("TAG", "places.toString =" + places.toString() + " places.getcount=" + places.getCount());
            // Get the Place object from the buffer.
            if (places.getCount() > 0) {
                place = places.get(0);
                // Format details of the place for display and show it in a TextView.
                //mPlaceDetailsText.
                tvLugar.setText(place.getName() + "\n" + place.getAddress());
                if (tvLugar.getText().length()>0){//si hay algo de texto mostramos attributions
                    ivPoweredByGoogle.setVisibility(View.VISIBLE);
                }
                cargarMapa();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        listaComments = new ArrayList<Comment>();
        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
        commentsadapter = new ListaCommentsCustomAdapter(getActivity(), R.layout.linea_comments, listaComments);

        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setSubtitle("Show event details");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView", "onCreate mostrar detalle");

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.mostrar_detalle_evento, container, false);
        View header = getActivity().getLayoutInflater().inflate(R.layout.mostrar_detalle_evento_listheader, null);
        View footer = getActivity().getLayoutInflater().inflate(R.layout.mostrar_detalle_evento_listfooter, null);

        /////////////////LIST VIEW Y ADAPTADOR/////////////////////
        listView = (ListView) view.findViewById(android.R.id.list);
        listView.addHeaderView(header);
        listView.addFooterView(footer);
        listView.setAdapter(commentsadapter);

        tvGroupName = (TextView) header.findViewById(R.id.tvGroupName2);
        ivCartel = (ImageView) header.findViewById(R.id.ivCartel);
        tvLugar = (TextView) header.findViewById(R.id.tvLugar2);
        ivPoweredByGoogle = (ImageView) header.findViewById(R.id.ivPoweredBy);
        tvFecha = (TextView) header.findViewById(R.id.tvFecha2);
        tvHora = (TextView) header.findViewById(R.id.tvHora2);
        tvPrecio = (TextView) header.findViewById(R.id.tvPrecio2);
        tvUrl = (TextView) header.findViewById(R.id.tvUrl2);
        tvCountComments = (TextView) header.findViewById(R.id.tvCountComments);
        etCommentTxt = (EditText) header.findViewById(R.id.etCommentTxt);
        btnPublish = (Button) header.findViewById(R.id.btnPublish);
        btnShowMore = (Button) footer.findViewById(R.id.btnShowMore);
        btnShowMore.setOnClickListener(this);
        btnPublish.setOnClickListener(this);//listener del boton de publicar comment


        Bundle bundle = getArguments();
        if (bundle != null) {
            idevento = bundle.getString("idevento");
            new CargarEvento().execute();
            new LoadComments().execute(Integer.toString(Constantes.NUMBER_MAX_COMMENTS_SHOW));
        }

        return view;
    }


    /**
     * * The mapfragment's id must be removed from the FragmentManager
     * *** or else if the same it is passed on the next time then
     * *** app will crash ***
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).commit();
            map = null;
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case (R.id.btnPublish): {
                String text = etCommentTxt.getText().toString().trim();//trim le quita espacios en blanco al principio y al final
                if (text.length() > 0) new PublishComment().execute();
                break;
            }
            case (R.id.btnShowMore): {
                String text = etCommentTxt.getText().toString().trim();//trim le quita espacios en blanco al principio y al final
                new LoadComments().execute();
                break;
            }

        }
    }

    // Ponemos nuevos botones en la actionbar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        buildoptionsmenu.setOptions(new boolean[]{false, false, false});
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void addCommentToList(Comment comment) {
        listaComments.add(0, comment);
        commentsadapter.notifyDataSetChanged();
        if (listaComments.size() == 1) {
            tvCountComments.setText(Integer.toString(listaComments.size()) + " comment");
        } else {
            tvCountComments.setText(Integer.toString(listaComments.size()) + " comments");
        }
    }

    void cargarMapa() {
        /////////////////////////////////////////////////////
        if (place != null) {

            LatLng EVENTPOSITION = place.getLatLng();
            map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            Marker eventMarker = map.addMarker(new MarkerOptions()
                    .position(EVENTPOSITION)
                    .title(place.getName().toString()));

            // Move the camera instantly to hamburg with a zoom of 15.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(EVENTPOSITION, 15));

            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
            map.getUiSettings().setZoomControlsEnabled(true);//ponemos botones de zoom
            map.getUiSettings().setScrollGesturesEnabled(false);//evita hacer scroll ya que estamos dentro de un scrollview y es un desproposito

        }
        //////////////////////////////////////////////////////////////////////////
    }

    class LoadComments extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading messages ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {


            JSONObject json = new JSONObject();

            /////ESTO ES PARA OBTENER EL listPreferredItemHeight que hemos definido en el layout
            android.util.TypedValue value = new android.util.TypedValue();
            getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
            TypedValue.coerceToString(value.type, value.data);
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float size = value.getDimension(metrics);
            /////////////////////////////////////////////////////
            Log.e("TAG", "estamos en doinbackground");
            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.LOAD_MESSAGES_FILE);
                uploader.añadirArgumento("idevento", idevento);//le pasamos el codigo del evento del cual estamos mostrando el detalle
                uploader.añadirArgumento("thumb_width", Float.toString(size));
                uploader.añadirArgumento("thumb_height", Float.toString(size));
                uploader.añadirArgumento("timezoneoffset", Utilidades.getTimeZoneOffset(getActivity()));

                if ((args != null) && (args.length > 0)) {
                    uploader.añadirArgumento("numerocomments", args[0]);
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


        protected void onPostExecute(JSONObject json) {
            int success;
            String mensaje;
            int totalcount;
            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);


                if (success == 1) {//si fue bien
                    totalcount = json.getInt("totalcount");
                    JSONArray messageList = json.getJSONArray("listmessages");
                    listaComments.clear();

                    for (int i = 0; i < messageList.length(); i++) {//se supone que solo tiene que haber uno

                        JSONObject message = messageList.getJSONObject(i);

                        String username = message.getString("username");
                        String txt = message.getString("msg");
                        String timestamp = message.getString("timestamp");
                        String cartelBase64 = message.getString("image");
                        byte[] image = Base64.decode(cartelBase64, Base64.DEFAULT);

                        Comment comment = new Comment(image, txt, username, timestamp);
                        comment.print("comment = ");
                        listaComments.add(comment);

                    }
                    commentsadapter.notifyDataSetChanged();

                    /////PONEMOS VISIBILIDAD ADECUADA AL BOTON DE SHOW MORE COMMENTS
                    Toast.makeText(getActivity(), Integer.toString(totalcount), Toast.LENGTH_LONG);
                    if (totalcount > Constantes.NUMBER_MAX_COMMENTS_SHOW) {//si es mayor que el limite maximo que queremos cargar
                        if (totalcount == listaComments.size())
                            btnShowMore.setVisibility(View.GONE);//
                        else btnShowMore.setVisibility(View.VISIBLE);
                    } else btnShowMore.setVisibility(View.GONE);
                    /////////////////////////////////////////////////7


                    if (listaComments.size() == 1) {
                        tvCountComments.setText(Integer.toString(messageList.length()) + " comment");
                    } else {
                        tvCountComments.setText(Integer.toString(messageList.length()) + " comments");
                    }

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    class CargarEvento extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading event ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.LEER_EVENTOS_FILE);
                uploader.añadirArgumento("_id", idevento);//le pasamos el codigo del evento del cual estamos mostrando el detalle
                uploader.añadirArgumento("timezoneoffset", Utilidades.getTimeZoneOffset(getActivity()));
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


        protected void onPostExecute(JSONObject json) {
            Log.i("cargareventos", "on post execute");
            int success;
            String mensaje;
            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien
                    // Getting Array of Events
                    JSONArray listaEventos = json.getJSONArray(Constantes.JSON_EVENTOS);
                    JSONObject listagrupos = json.getJSONObject("groups_list");

                    // loop recorriendo los eventos
                    for (int i = 0; i < listaEventos.length(); i++) {//se supone que solo tiene que haber uno

                        JSONObject evento = listaEventos.getJSONObject(i);


                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(((MainActivity) getActivity()).mGoogleApiClient, evento.getString("placeid"));
                        placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                        //tvLugar.setText(evento.getString(Constantes.EV_LUGAR));
                        tvFecha.setText(evento.getString(Constantes.EV_FECHA));
                        tvHora.setText(evento.getString(Constantes.EV_HORA));
                        tvPrecio.setText(evento.getString(Constantes.EV_PRECIO));
                        tvUrl.setText(evento.getString(Constantes.EV_URL));

                        tvGroupName.setText(listagrupos.getJSONObject(evento.getString(Constantes.EV_IDGRUPO)).getString("nombre"));

                        String cartelBase64 = evento.getString(Constantes.EV_CAR);
                        byte[] image = Base64.decode(cartelBase64, Base64.DEFAULT);

                        ivCartel.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
                    }


                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            pDialog.dismiss();
        }
    }

    class PublishComment extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Publishing ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.PUBLISH_COMMENT);
                uploader.añadirArgumento("idevento", idevento);//le pasamos el codigo del evento del cual estamos mostrando el detalle
                uploader.añadirArgumento("email", preferences.getString(Constantes.SP_EMAIL_REG, ""));
                uploader.añadirArgumento("msg", etCommentTxt.getText().toString().trim());
                uploader.añadirArgumento("timezoneoffset", Utilidades.getTimeZoneOffset(getActivity()));

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

        protected void onPostExecute(JSONObject json) {
            int success;
            String mensaje;
            byte[] imageByteArray = null;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien

                    JSONObject jsoncomment = json.getJSONObject("comment");

                    String imagepath = getActivity().getFilesDir() + "/images/" + preferences.getString(Constantes.SP_EMAIL_REG, "") + ".jpeg";
                    File imagefile = new File(imagepath);

                    //Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                    //int[] screenResolution = Utilidades.getScreenResolution(getActivity());//nos saca la resloucion de nuestra pantalla
                    //int[] dimension = Utilidades.calculaDimensionImagen(bitmap.getWidth(), bitmap.getHeight(), screenResolution[0], screenResolution[1]);//la resolucion maxima sera la de la pantalla nuestra
                    //Bitmap bitmapAjustado = Utilidades.redimensionarBitmap(bitmap,dimension[0],dimension[1]);//creamos un bitmap ajustado a la resolucion maxima de pantalla. esto es lo que guardamos en el server
                    //ivImage.setImageBitmap(Utilidades.redimensionarBitmap(bitmap,ivImage.getWidth(),ivImage.getHeight()));//Ponemos en el imageView ek bitmap ajustado al tamaño del imageview

                    if (imagefile.exists()) {

                        FileInputStream fis = new FileInputStream(imagefile);
                        //create FileInputStream which obtains input bytes from a file in a file system
                        //FileInputStream is meant for reading streams of raw bytes such as image data. For reading streams of characters, consider using FileReader.

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];

                        for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                            bos.write(buf, 0, readNum);//Writes to this byte array output stream
                            System.out.println("read " + readNum + " bytes,");
                        }

                        imageByteArray = bos.toByteArray();
                    }

                    Comment comment = new Comment(imageByteArray, jsoncomment.getString("msg"), jsoncomment.getString("username"), jsoncomment.getString("timestamp"));
                    listaComments.add(0, comment);//el o es para añadirlo al principio de la lista
                    commentsadapter.notifyDataSetChanged();
                    etCommentTxt.setText("");//borramos el edittext

                    if (listaComments.size() == 1) {
                        tvCountComments.setText(Integer.toString(listaComments.size()) + " comment");
                    } else {
                        tvCountComments.setText(Integer.toString(listaComments.size()) + " comments");
                    }

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            pDialog.dismiss();
        }
    }
}