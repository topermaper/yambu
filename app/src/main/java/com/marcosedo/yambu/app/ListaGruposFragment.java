package com.marcosedo.yambu.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;


public class ListaGruposFragment extends Fragment {

    private static final String TAG = "YAMBU";
    JSONArray grupos;

    private int itemposicion;
    private ListView listview;
    private ArrayList<Grupo> listaGrupos = new ArrayList<Grupo>();
    private ListaGruposCustomAdapter adapter;
    private OnEditarGrupoListener mCallback;
    private OnMostrarImagenListener mostrarImagenCallback;

    private ProgressDialog pDialog; // Progress Dialog
    private BuildOptionsMenu buildoptionsmenu;

    // Container Activity must implement this interface
    public interface OnEditarGrupoListener {
        public void onEditarGrupo(Grupo grupo);
    }

    // Container Activity must implement this interface
    public interface OnMostrarImagenListener {
        public void onMostrarImagen(String path);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnEditarGrupoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEditarGrupoListener");
        }

        try {
            mostrarImagenCallback = (OnMostrarImagenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMostrarImagenListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        buildoptionsmenu = new BuildOptionsMenu();
        Log.i("onCreate Config", "Se mete en el onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "Se mete en el onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView Config", "Se mete en el onCreateView");

        View view = inflater.inflate(R.layout.my_groups, container, false);

        //INICIALIZAMOS VARIABLES GLOBALES
        listview = (ListView) view.findViewById(android.R.id.list);
        //listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        registerForContextMenu(listview);

        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setSubtitle("My groups");

        /////////////////LIST VIEW Y ADAPTADOR/////////////////////
        adapter = new ListaGruposCustomAdapter(getActivity(),
                R.layout.linea_lista_grupos, listaGrupos);
        listview.setAdapter(adapter);


        //LISTENER DEL LIST VIEW esto nos saca el context menu con un solo click
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View v, int position,
                                    long arg3) {
                v.showContextMenu();
            }
        });

        new CargarGrupos().execute();

        return view;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        EditarGruposFragment fragment;
        FragmentTransaction transaction;

        Grupo grupo;

        //handle action bar
        switch (item.getItemId()) {
            case R.id.action_add_group:
                transaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragment = new EditarGruposFragment();
                transaction.replace(R.id.contenedorfragment, fragment, "add_new_group");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
            /*
            case R.id.action_modificar:

                //transaction = getActivity().getSupportFragmentManager().beginTransaction();
                grupo = listaGrupos.get(selectedPosition);
                try{//PEdimos la imagen a tamaño completo
                    if (grupo.getImg() == null){//si no hay cartel full HD pedimos la imagen a tamaño completo
                        HttpUploader uploader = new HttpUploader(Constantes.DOMINIO+Constantes.ADMIN_GRUPOS_FILE);
                        uploader.añadirArgumento(Constantes.EV_ID,grupo.getId());
                        uploader.añadirArgumento("mode","0");
                        JSONObject json = uploader.enviar();

                        grupos = json.getJSONArray(Constantes.JSON_GRUPOS);
                        if (grupos.length() > 0){
                            json = grupos.getJSONObject(0);
                            String imgBase64 = json.getString(Constantes.GP_IMG);
                            byte[] img = Base64.decode(imgBase64, Base64.DEFAULT);
                            grupo.setImg(img);
                        }
                    }
                    mCallback.onEditarGrupo(grupo);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                break;

            case R.id.action_borrar:
                grupo = listaGrupos.get(selectedPosition);
                new BorrarGrupo().execute(grupo.getId());
                */
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        buildoptionsmenu.setOptions(new boolean[]{false, false, true});//this is for add groups
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_listaGrupos);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    //-------------CONTEXT MENU---------------------------
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Grupo evento = listaGrupos.get(info.position);

        //Cuando pulsamos nos aparece como título el nombre del grupo para saber cual seleccionamos
        menu.setHeaderTitle(evento.getNombre());
        ByteArrayInputStream is = new ByteArrayInputStream(evento.getThumb());
        menu.setHeaderIcon(Drawable.createFromStream(is, "cartel"));
        //inflater.inflate(R.menu.menu_contextual_layout, menu);
        menu.add(0, R.id.action_view_image, 1, "View image");
        menu.add(0, R.id.action_edit_group, 2, "Edit group");
        menu.add(0, R.id.action_delete_group, 3, "Delete group");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.e("context", "entra a la seleccion");
        Context contexto;
        Intent intent;
        JSONObject json = new JSONObject();
        Bundle extras = new Bundle();
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        //se guarda la posicion para borrar de la lv directamente y no
        //tener que llamar a la base de datos de nuevo
        itemposicion = info.position;

        Grupo grupo = listaGrupos.get(itemposicion);

        switch (item.getItemId()) {
            ///editamos el evento asi que nos vamos a la pantalla del editor
            //y le pasamos el id del evento como argumento
            case R.id.action_view_image://verIMAGEN

                mostrarImagenCallback.onMostrarImagen("img_grupo" + grupo.getId() + ".jpg");
                return true;

            case R.id.action_edit_group://editar evento

                try {//PEdimos la imagen a tamaño completo

                    /*if (evento.getCartel() == null) {//si no hay cartel full HD pedimos la imagen a tamaño completo
                        HttpUploader uploader = new HttpUploader(Constantes.DOMINIO + Constantes.LEER_EVENTOS_FILE);
                        uploader.añadirArgumento(Constantes.EV_ID, evento.getId());
                        json = uploader.enviar();

                        eventos = json.getJSONArray(Constantes.JSON_EVENTOS);
                        if (eventos.length() > 0) {
                            json = eventos.getJSONObject(0);
                            String cartelBase64 = json.getString(Constantes.EV_CAR);
                            byte[] cartel = Base64.decode(cartelBase64, Base64.DEFAULT);
                            evento.setCartel(cartel);
                        }
                    }*/
                    grupo = listaGrupos.get(itemposicion);
                    mCallback.onEditarGrupo(grupo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;


            case R.id.action_delete_group://Borramos el evento
                //Creamos una tarea asincrona para borrar el evento seleccionado
                new BorrarGrupo().execute(grupo.getId());
                return true;


            default:
                return super.onContextItemSelected(item);
        }
    }
    /////////////////////////////////////////////////////
    //////////Async Task para borrar un grupo //////////
    /////////////////////////////////////////////////////
    class BorrarGrupo extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Borrando grupo ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.ADMIN_GRUPOS_FILE);
                uploader.añadirArgumento(Constantes.GP_ID, args[0]);
                uploader.añadirArgumento("mode", "3");//el 3 es el modo de borrar registro
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
            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {


                    //si se ha borrado correctamente de la BBDD
                    //quitamos el item de la lista de eventos y rellenamos de nuevo el list view
                    //sin llamar al server
                    listaGrupos.remove(itemposicion);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(getActivity().getApplicationContext(), mensaje ,Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje ,Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    class CargarGrupos extends AsyncTask<String, String, JSONObject> {
        private JSONObject json;
        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Cargando lista de grupos ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            listaGrupos.clear();

            json = new JSONObject();

            /////ESTO ES PARA OBTENER EL listPreferredItemHeight que hemos definido en el layout
            /*android.util.TypedValue value = new android.util.TypedValue();
            getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
            TypedValue.coerceToString(value.type, value.data);

            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float size = value.getDimension(metrics);*/


            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN+ Constantes.ADMIN_GRUPOS_FILE);
                uploader.añadirArgumento("mode","0");//0 leer 1 crear 2 modificar 3 borrar
                uploader.añadirArgumento("thumb_height",Integer.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("thumb_width",Integer.toString(Constantes.THUMB_SIZE));
                uploader.añadirArgumento("email_reg",getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE).getString(Constantes.SP_EMAIL_REG,""));
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

        /* After completing background task Dismiss the progress dialog
* **/
        protected void onPostExecute(JSONObject json) {

            int success;
            String mensaje;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                if (success == 1) {//si fue bien
                    // Getting Array of Events
                    grupos = json.getJSONArray(Constantes.JSON_GRUPOS);

                    //listaGrupos = new ArrayList<Grupo>();
                    listaGrupos.clear();//resteamos la lista IMPORTANTE si no aumenta de tamañana cada vez que recargamos
                    // loop recorriendo los eventos
                    for (int i = 0; i < grupos.length(); i++) {
                        JSONObject c = grupos.getJSONObject(i);

                        String id = c.getString(Constantes.GP_ID);
                        String nombre = c.getString(Constantes.GP_NOMBRE);
                        String creador = c.getString(Constantes.GP_CREADOR);
                        String thumb_encoded = c.getString(Constantes.GP_THUMB);
                        int numberevents = c.getInt("numberevents");

                        byte[] thumb = Base64.decode(thumb_encoded, Base64.DEFAULT);

                        Grupo grupo = new Grupo(id, thumb, nombre, creador);
                        grupo.setNumberOfEvents(numberevents);

                        listaGrupos.add(grupo);

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
}
    
