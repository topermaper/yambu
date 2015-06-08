package com.marcosedo.yambu.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class MessagesFragment extends Fragment implements View.OnClickListener {

    private static String devid;
    private ProgressDialog pDialog;
    private EditText mensajeEt;
    private ListView lvDispositivos;
    private Button enviarBtn;
    private String mensaje;
    private TextView tvInfo;
    private String idevento;
    private String idgrupo;
    private ListaUsuariosCustomAdapter adapter;
    private BuildOptionsMenu buildoptionsmenu;
    private Spinner spinner;
    private ArrayList<Grupo> listaGrupos = new ArrayList<Grupo>();
    private ArrayAdapter<String> spinneradapter;//adapter del spinner
    public ArrayList<Usuario> listaFollowers;//lista donde se guardaran los followers que se muestran en lista
    private SharedPreferences preferences;

    private int spinner_position = -1;//no hay nada todavia

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate Config", "Se mete en el onCreate");
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);//cargamos shared preferences desde oncreate para enerlo ya pa siempre
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//para poder hacer una actionbarcustom dinamicamente. oncreateoptionsmenu
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i("SAVE", "onSAveInstance NotificacionesFragment");
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("LIST_INSTANCE_STATE", listaFollowers);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView Config", "Se mete en el onCreateView");

        View view = inflater.inflate(R.layout.messages_layout, container, false);

        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr
        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones
        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setSubtitle("Messages");

        //INICIALIZAMOS VARIABLES GLOBALES
        //devid = Secure.getString(getActivity().getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        mensajeEt = (EditText) view.findViewById(R.id.mensajeEt);
        enviarBtn = (Button) view.findViewById(R.id.sendBtn);
        lvDispositivos = (ListView) view.findViewById(android.R.id.list);
        tvInfo = (TextView) view.findViewById(R.id.tvinfo);

        //Inicializamos nuestro spinner
        spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                new CargarDestinatarios().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        /////////////////////////////////////////////


        if (listaFollowers == null) {
            Log.i("onCreateView Config", "listaFollowers es null");
            listaFollowers = new ArrayList<Usuario>();

            if (savedInstanceState == null) {//si es null es porque se esta creando el fragment por primera vez. creamos el adapter
                Log.i("onCreateView Config", "se va a poner el adapter nuevo");
            } else {//recuperamos lo que se salvo en el onsavestateinstance
                listaFollowers = savedInstanceState.getParcelableArrayList("LIST_INSTANCE_STATE");
                Log.i("onCreateView Config", "cargamos nuestra lista dispositivos parcelable " + listaFollowers.toString());

            }
        } else {
            Log.i("onCreateView Config", "listaFollowers distinto null");
        }

        ///////////////////ADAPTER///////////////////
        adapter = new ListaUsuariosCustomAdapter(getActivity(),
                R.layout.linea_lista_usuarios, listaFollowers);
        lvDispositivos.setAdapter(adapter);

        //PONEMOS los listeners
        enviarBtn.setOnClickListener(this);
        lvDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int position, long arg3) {

                if (listaFollowers.get(position).isChecked()) {
                    listaFollowers.get(position).setChecked(false);
                } else {
                    listaFollowers.get(position).setChecked(true);
                }

                ((ListaUsuariosCustomAdapter) lvDispositivos.getAdapter()).notifyDataSetChanged();

                RefreshInfo();//refresca el textview de arriba de la lista de usuarios
            }
        });

        lvDispositivos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String[] items = {"Select all", "Unselect all", "Cancel"};
                mostarDialogoLongClick(items);
                return true;
            }
        });

        //Obtenemos los argumentos que le hayamos podido pasar a la actividad
        Bundle bundle = getArguments();

        //Si le hemos pasado argumentos es porque tenemos la id del evento
        //que queremos editar.
        if (bundle != null) {
            mensaje = bundle.getString("mensaje");
            mensajeEt.setText(mensaje);
            idevento = bundle.getString("idevento");
            idgrupo = bundle.getString("idgrupo");
        }


        //cargamos el spinner con nuestra lista
        new CargarSpinner().execute();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        buildoptionsmenu.setOptions(new boolean[]{false, false, false});
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_notificaciones);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void mostarDialogoLongClick(String[] items) {

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.select_dialog_item, items);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: //Marcar todo
                        for (int i = 0; i < listaFollowers.size(); i++) {
                            listaFollowers.get(i).setChecked(true);
                        }
                        ((ListaUsuariosCustomAdapter) lvDispositivos.getAdapter()).notifyDataSetChanged();
                        break;
                    case 1://Desmarcar todo
                        for (int i = 0; i < listaFollowers.size(); i++) {
                            listaFollowers.get(i).setChecked(false);
                        }
                        ((ListaUsuariosCustomAdapter) lvDispositivos.getAdapter()).notifyDataSetChanged();
                        break;
                }

            }
        });

        builder.show();
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case (R.id.sendBtn): {

                //Comprobamos que haya al menos uno seleccionado para continuar
                boolean seleccionado = false;
                for (int i = 0; i < listaFollowers.size(); i++) {//recorremos todo y si lo hemos marcado lo enviamos
                    if (listaFollowers.get(i).isChecked()) {//necesitamos la regid, no la devid para enviar un mensaje
                        seleccionado = true;
                        break;
                    }
                }

                if (seleccionado == true) {
                    new EnviarMensaje().execute();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Seleccione al menos un destinatario.", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    /////////////////////////////////////////////////////
    ///Async Task para envia mensaje////////////////////
    /////////////////////////////////////////////////////
    class EnviarMensaje extends AsyncTask<Void, Void, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Sending message ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            JSONObject json = null;

            String msg = mensajeEt.getText().toString();
            Log.i("YAMBU", "Se va a enviar el mensaje: " + msg);

            HttpUploader uploader = null;
            try {
                uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.GCMSERVER_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Building Parameters se pasan todos como string
            //uploader.añadirArgumento("devid", devid);
            uploader.añadirArgumento("email", preferences.getString(Constantes.SP_EMAIL_REG, ""));
            uploader.añadirArgumento("message", msg);
            uploader.añadirArgumento("collapsekey", msg);
            uploader.añadirArgumento("mode", "mensaje");

            if (idevento != null) uploader.añadirArgumento("idevento", idevento);

            String encodedfollowers="";//metemos los codigos como cadena separando
            String separator =",";//separamos por como como son todo integers nos da igual
            for (int i = 0; i < listaFollowers.size(); i++) {//recorremos todo y si lo hemos marcado lo enviamos
                if (listaFollowers.get(i).isChecked()) {//necesitamos la regid, no la devid para enviar un mensaje
                    if (encodedfollowers.equals("")) {
                        encodedfollowers += listaFollowers.get(i).getId();
                    }else{
                        encodedfollowers += separator + listaFollowers.get(i).getId();
                    }
                }
            }

            uploader.añadirArgumento("destinatarios", encodedfollowers);//pasamos la lista con las ids de usuarios que nos siguen. EL server se encargara de mirar a que dispositivo mandarlo y toda la pesca


            json = uploader.enviar();

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            // check for success tag
            int success;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                if (success == 1) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Message sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "An error has ocurred while sending message", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();

            //getActivity().getSupportFragmentManager().popBackStackImmediate();//esto nos lleva al fragment anterior. es como si apretasemos atras

        }
    }


    /////////////////////////////////////////////////////
    ///Async Task para obtener la lista de destinatarios/
    /////////////////////////////////////////////////////
    class CargarDestinatarios extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading followers list...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject json = new JSONObject();

            /////ESTO ES PARA OBTENER EL listPreferredItemHeight que hemos definido en el layout
            android.util.TypedValue value = new android.util.TypedValue();
            getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
            TypedValue.coerceToString(value.type, value.data);
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float size = value.getDimension(metrics);
            Log.i("SIZE", Float.toString(size));
            //float size_pixel = Utilidades.convertDipToPixel(getActivity(), size_dip);
            ////////////////////////////////////////////////////////////////////////////////////
            Grupo selectedgroup = (Grupo) spinner.getSelectedItem();
            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.LEER_USUARIOS_FILE);
                uploader.añadirArgumento("thumb_width", Float.toString(size));
                uploader.añadirArgumento("thumb_height", Float.toString(size));

                if (selectedgroup != null) {
                    Log.e("idgrupo", selectedgroup.getId());
                    uploader.añadirArgumento("idgrupo", selectedgroup.getId());
                } else {
                    Log.e("TAG", "selected group es null");
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


        @Override
        protected void onPostExecute(JSONObject json) {

            int success;
            String mensaje;
            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);
                if (success == 1) {//si fue bien
                    JSONArray destinatariosJSONArray = json.getJSONArray("usuarios");

                    listaFollowers.clear();//resteamos la lista IMPORTANTE si no aumenta de tamañana cada vez que recargamos


                    // loop recorriendo los dispositivos
                    for (int i = 0; i < destinatariosJSONArray.length(); i++) {
                        JSONObject c = destinatariosJSONArray.getJSONObject(i);

                        String uid = c.getString("uid");
                        String username = c.getString("username");
                        String firstname = c.getString("firstname");
                        String lastname = c.getString("lastname");
                        String email = c.getString("email");

                        String imageBase64 = c.getString("image");
                        byte[] image = Base64.decode(imageBase64, Base64.DEFAULT);

                        Usuario usuario = new Usuario(uid, image, username, firstname, lastname, email);
                        //usuario.print("TAG");
                        usuario.setChecked(true);
                        listaFollowers.add(usuario);

                    }

                    adapter.notifyDataSetChanged();
                    RefreshInfo();

                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    class CargarSpinner extends AsyncTask<String, String, JSONObject> {
        private JSONObject json;
        ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading groups...");
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

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien
                    // Getting Array of Events

                    //listaSpinner.clear();//resteamos la lista IMPORTANTE si no aumenta de tamañana cada vez que recargamos
                    listaGrupos.clear();
                    // loop recorriendo los eventos
                    JSONArray grupos = json.getJSONArray(Constantes.JSON_GRUPOS);

                    // loop recorriendo los eventos

                    for (int i = 0; i < grupos.length(); i++) {
                        JSONObject c = grupos.getJSONObject(i);

                        String id = c.getString(Constantes.GP_ID);
                        String nombre = c.getString(Constantes.GP_NOMBRE);
                        String creador = c.getString(Constantes.GP_CREADOR);
                        String thumb_encoded = c.getString(Constantes.GP_THUMB);

                        byte[] thumb = Base64.decode(thumb_encoded, Base64.DEFAULT);

                        Grupo grupo = new Grupo(id, thumb, nombre, creador);

                        listaGrupos.add(grupo);

                        if ((idgrupo != null) && (id.equals(idgrupo))) {//si este es el grupo del evento del cual mandamos mensaje
                            spinner_position = i;//guardamos que lo hemos encontrado en la posicion i
                        }

                    }

                    if (spinneradapter == null) {
                        spinneradapter = new SpinnerGruposCustomAdapter((MainActivity) getActivity(), R.layout.linea_spinner_grupos, listaGrupos, getResources());
                    }
                    spinner.setAdapter(spinneradapter);
                    if (spinner_position >= 0) {//si hemos encontrado el grupo vamos a esa posicion ahora que ya tenemos el adapter asociado
                        spinner.setSelection(spinner_position);
                    }
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

    private void RefreshInfo() {
        int countcheck = 0;
        String string = "";

        for (int i = 0; i < listaFollowers.size(); i++) {
            if (listaFollowers.get(i).isChecked()) countcheck++;
        }


        if (countcheck == 1) {
            string = "follower";
        } else
            string = "followers";
        {
            tvInfo.setText(Integer.toString(countcheck) + "/" + Integer.toString(listaFollowers.size()) + " " + string+" selected");
        }
    }
}
