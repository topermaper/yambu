package com.marcosedo.yambu.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import java.util.Collections;


public class ListaEventosFragment extends Fragment {

    int SORTED_BY_GROUPNAME = 0;
    int SORTED_BY_DATE = 1;
    int SHOW_ALL = 2;
    int SHOW_ONLY_FOLLOWED = 3;
    JSONArray eventos = null;
    private int sortedby = SORTED_BY_DATE;
    private int show = SHOW_ALL;

    private BuildOptionsMenu buildoptionsmenu;//una clase muy maja para que nos rellene el optionsmenu como toca
    private ProgressDialog pDialog;
    private ListView lv;
    private ArrayList<Evento> listaEventos;
    private OnEditarEventoListener mCallback;
    private OnMostrarImagenListener mostrarImagenCallback;
    private int itemposicion;
    public static ListaEventosCustomAdapter adapter;
    SharedPreferences preferences;

    //Mi onclicklistener para la estrellita
    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = (Integer) v.getTag();

            switch (listaEventos.get(position).getFollowed()) {

                case 0:
                    new FollowGroup().execute(listaEventos.get(position).getIdGrupo(), "follow");
                    break;
                case 1:
                    new FollowGroup().execute(listaEventos.get(position).getIdGrupo(), "unfollow");
                    break;
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("LIST_INSTANCE_STATE", listaEventos);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
    }

    public void onCreate (Bundle savedInstanceState){
        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("ListaEventosFragment", "onCreateView");
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.lista_eventos, container, false);
        lv = (ListView) view.findViewById(android.R.id.list);
        registerForContextMenu(lv);
        //lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr


        /////////////////LIST VIEW Y ADAPTADOR/////////////////////
        listaEventos = new ArrayList<Evento>();//instanciamos la lv de eventos para que no pete
        adapter = new ListaEventosCustomAdapter(getActivity(), R.layout.linea_lista_eventos, listaEventos, new MyOnClickListener());

        lv.setAdapter(adapter);

        if (savedInstanceState != null) {
            listaEventos = savedInstanceState.getParcelableArrayList("LIST_INSTANCE_STATE");
            //adapter.setSelectedIndex(selectedPosition);//enviamos la posicion del que hay que remarcar
            adapter.notifyDataSetChanged();//este es para que refresque la lv//
        }
        if (listaEventos.isEmpty()) {
            new CargarEventos().execute();
        }


        //LISTENER DEL LIST VIEW esto nos saca el context menu con un solo click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View v, int position,
                                    long arg3) {
                v.showContextMenu();
            }
        });

       /*//si es diferente de null es que venimos de hacer atras.
       //si es null y no hacemos la comparacion petara
       if (optionsMenu != null){
           BuildOptionsMenu();
       }*/
        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setSubtitle("Events list");
        return view;
    }


    // Container Activity must implement this interface
    public interface OnEditarEventoListener {
        public void onEditarEvento(Evento evento);
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
            mCallback = (OnEditarEventoListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnEditarEventoListener");
        }

        try {
            mostrarImagenCallback = (OnMostrarImagenListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMostrarImagenListener");
        }
    }

    @Override
    //-------------CONTEXT MENU---------------------------
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Evento evento = listaEventos.get(info.position);
        //Cuando pulsamos nos aparece como título la fecha y la hora para saber cual seleccionamos
        menu.setHeaderTitle(evento.getNombreGrupo() + " " + evento.getFecha() + "@" + evento.getPlaceName());
        ByteArrayInputStream is = new ByteArrayInputStream(evento.getThumb());
        menu.setHeaderIcon(Drawable.createFromStream(is, "cartel"));
        //inflater.inflate(R.menu.menu_contextual_layout, menu);


        menu.add(0, R.id.action_view_image, 0, "View image");
        menu.add(0, R.id.action_view_detail, 1, "View detail");
        //si es editable daremos la posibilidad de editar y borrar
        if (evento.getEditable()) {
            menu.add(0, R.id.action_edit_event, 2, "Edit event");
            menu.add(0, R.id.action_delete_event, 3, "Delete event");
            menu.add(0,R.id.action_notificaciones,4,"Send notification");
        }


        switch (evento.getFollowed()) {
            case 0:
                menu.add(0, R.id.action_follow_group, 0, "Follow group");
                break;
            case 1:
                menu.add(0, R.id.action_unfollow_group, 1, "Unfollow group");
                break;
        }
    }


    //aqui tenemos las opciones de este fragment y en el main las comunes a todos los fragments
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i("TAG", "onOptionsItemSelected del listaeventosfragment");
        // Handle presses on the action bar items
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        switch (item.getItemId()) {
            //case R.id.action_añadir:

            case R.id.action_add_event:

                EditarEventosFragment editarFragment = new EditarEventosFragment();
                transaction.replace(R.id.contenedorfragment, editarFragment, "añadir evento");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction

                break;

            case R.id.action_eventslist_options://Notificaciones
                final CharSequence[] items = {"Sort by group name", "Sort by date", "Show all", "Show only followed"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                //builder.setTitle("Pick a color");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                sortedby = SORTED_BY_GROUPNAME;
                                showCategoriesGroupName();
                                adapter.notifyDataSetChanged();
                                break;
                            case 1:
                                sortedby = SORTED_BY_DATE;
                                showCategoriesDate();
                                adapter.notifyDataSetChanged();
                                break;
                            case 2:
                                show = SHOW_ALL;
                                //mostramos todos los elementos
                                for (int i = 0; i < listaEventos.size(); i++) {
                                    Evento evento = listaEventos.get(i);
                                    evento.setHided(false);
                                }
                                if (sortedby == SORTED_BY_DATE) {//si ordenado por fecha
                                    showCategoriesDate();//creampos las categorias de fechas
                                } else if (sortedby == SORTED_BY_GROUPNAME) {//si ordenado por fecha
                                    showCategoriesGroupName();//creampos las categorias de fechas
                                }

                                adapter.notifyDataSetChanged();
                                break;

                            case 3:
                                show = SHOW_ONLY_FOLLOWED;
                                //escondemos los  grupos que no seguimos
                                for (int i = 0; i < listaEventos.size(); i++) {
                                    Evento evento = listaEventos.get(i);
                                    if (evento.getFollowed() == 0) {
                                        evento.setHided(true);
                                    } else {
                                        evento.setHided(false);
                                    }
                                }
                                if (sortedby == SORTED_BY_DATE) {//si ordenado por fecha
                                    showCategoriesDate();//creampos las categorias de fechas
                                } else if (sortedby == SORTED_BY_GROUPNAME) {//si ordenado por fecha
                                    showCategoriesGroupName();//creampos las categorias de fechas
                                }
                                adapter.notifyDataSetChanged();
                                break;
                        }
                        Toast.makeText(getActivity().getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        buildoptionsmenu.setOptions(new boolean[]{false, true, false});
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_listaEventos);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.e("context", "entra a la seleccion");
        Context contexto;
        Intent intent;
        JSONObject json = new JSONObject();
        Bundle args = new Bundle();
        ContextMenuInfo menuInfo = item.getMenuInfo();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        //se guarda la posicion para borrar de la lv directamente y no
        //tener que llamar a la base de datos de nuevo
        itemposicion = info.position;

        Evento evento = listaEventos.get(itemposicion);

        switch (item.getItemId()) {
            ///editamos el evento asi que nos vamos a la pantalla del editor
            //y le pasamos el id del evento como argumento
            case R.id.action_view_image://verIMAGEN

                mostrarImagenCallback.onMostrarImagen("cartel" + evento.getId() + ".jpg");
                return true;

            case R.id.action_view_detail://ver DETALLES

                args.putString("idevento", evento.getId());

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.detalleEventoFragment = new MostrarDetalleEventoFragment();
                mainActivity.detalleEventoFragment.setArguments(args);
                transaction.replace(R.id.contenedorfragment, mainActivity.detalleEventoFragment, "detalle evento");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction

                return true;

            case R.id.action_edit_event://editar evento

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
                    mCallback.onEditarEvento(evento);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;


            case R.id.action_delete_event://Borramos el evento
                //Creamos una tarea asincrona para borrar el evento seleccionado
                new BorrarEvento().execute(evento.getId());
                return true;


            case R.id.action_follow_group://Borramos el evento
                //Creamos una tarea asincrona para borrar el evento seleccionado
                new FollowGroup().execute(evento.getIdGrupo(), "follow");
                return true;

            case R.id.action_unfollow_group:
                new FollowGroup().execute(evento.getIdGrupo(), "unfollow");
                return true;

            case R.id.action_notificaciones:
                FragmentTransaction transaction2 = getActivity().getSupportFragmentManager().beginTransaction();
                SendNotificationFragment sendNotificationFragment = new SendNotificationFragment();

                //Establecemos los argumentos
                args.putString("idevento", evento.getId());
                args.putString("idgrupo", evento.getIdGrupo());
                args.putByteArray("cartel", evento.getThumb());
                sendNotificationFragment.setArguments(args);
                transaction2.replace(R.id.contenedorfragment, sendNotificationFragment, "notificaciones");
                //transaction.addToBackStack(null);
                transaction2.commit();// Commit the transaction
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void showCategoriesDate() {
        Collections.sort(listaEventos, Evento.DateComparator);
        Evento evento;
        String new_cap = null;
        String old_cap = null;
        for (int i = 0; i < listaEventos.size(); i++) {
            listaEventos.get(i).setSeparatorText("");
            evento = listaEventos.get(i);
            if (show == SHOW_ONLY_FOLLOWED) {//si solo queremos ver los eventos de los grupos que seguimos
                if (evento.getFollowed() == 1) {
                    new_cap = evento.getFecha();
                }
            } else
                new_cap = evento.getFecha();

            if (old_cap == null) {//si es null es la primera asi que ponemos chapter
                evento.setSeparatorText(evento.getFecha());
            } else if (!(new_cap.matches(old_cap))) {//si el siguiente elemento  ha cambiado
                evento.setSeparatorText(evento.getFecha());
            }
            old_cap = new_cap;

        }

        adapter.notifyDataSetChanged();

    }

    public void showCategoriesGroupName() {
        Collections.sort(listaEventos, Evento.NameComparator);
        Evento evento;
        String new_cap;
        String old_cap = null;
        for (int i = 0; i < listaEventos.size(); i++) {
            listaEventos.get(i).setSeparatorText("");
            evento = listaEventos.get(i);
            if (evento.getHided() == false) {
                new_cap = evento.getNombreGrupo();
                if (old_cap == null) {//si es null es la primera asi que ponemos chapter
                    evento.setSeparatorText(evento.getNombreGrupo());
                } else if (!(new_cap.matches(old_cap))) {//
                    evento.setSeparatorText(evento.getNombreGrupo());
                }
                old_cap = new_cap;

            }
        }
        adapter.notifyDataSetChanged();
    }

    /////////////////////////////////////////////////////
//////////Async Task para borrar un evento //////////
/////////////////////////////////////////////////////
    class FollowGroup extends AsyncTask<String, String, JSONObject> {
        private String mode;
        private String idgrupo;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Saving ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();
            idgrupo = args[0];
            mode = args[1];

            Log.i("doinbackground", "mode =" + mode + " idgrupo=" + args[0]);
            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.FOLLOWGROUP);
                uploader.añadirArgumento("idgrupo", args[0]);
                uploader.añadirArgumento("email_usuario", preferences.getString(Constantes.SP_EMAIL_REG, ""));
                uploader.añadirArgumento("mode", mode);
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

                    switch (mode) {
                        case "follow":
                            for (int i = 0; i < listaEventos.size(); i++) {
                                if (listaEventos.get(i).getIdGrupo().equals(idgrupo)) {
                                    if (listaEventos.get(i).getFollowed() == 0)
                                        listaEventos.get(i).setFollowed(1);
                                }
                            }
                            break;

                        case "unfollow":
                            for (int j = 0; j < listaEventos.size(); j++) {
                                if (listaEventos.get(j).getIdGrupo().equals(idgrupo)) {
                                    if (listaEventos.get(j).getFollowed() == 1)
                                        listaEventos.get(j).setFollowed(0);
                                }
                            }
                            break;
                    }

                    adapter.notifyDataSetChanged();

                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    /////////////////////////////////////////////////////
//////////Async Task para borrar un evento //////////
/////////////////////////////////////////////////////
    class BorrarEvento extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Deleting event ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.BORRAR_EVENTOS_FILE);
                uploader.añadirArgumento(Constantes.EV_ID, args[0]);
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
                    //quitamos el item de la lv de eventos y rellenamos de nuevo el list view
                    //sin llamar al server
                    listaEventos.remove(itemposicion);
                    //adapter.setSelectedIndex(-1);
                    adapter.notifyDataSetChanged();
                    /*
                    /////////////////LIST VIEW Y ADAPTADOR/////////////////////
                    ListaEventosCustomAdapter adapter = new ListaEventosCustomAdapter(getActivity(),
                            R.layout.linea_lista_layout, listaEventos);
                    lv.setAdapter(adapter);
                    ////////////////////////////////////////7
                    */

                    Toast.makeText(getActivity().getApplicationContext(), getActivity().getResources().getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    /////////////////////////////////////////////////////
///Async Task para obtener la lv eventos//////////
/////////////////////////////////////////////////////
    class CargarEventos extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading events ...");
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

            // float size_pixel = Utilidades.convertDipToPixel(getActivity(), size_dip);
            ////////////////////////////////////////////////////////////////////////////////////7
            SharedPreferences preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.LEER_EVENTOS_FILE);
                uploader.añadirArgumento("thumb_width", Float.toString(size));
                uploader.añadirArgumento("thumb_height", Float.toString(size));
                uploader.añadirArgumento("email_usuario", preferences.getString(Constantes.SP_EMAIL_REG, ""));
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
                    eventos = json.getJSONArray(Constantes.JSON_EVENTOS);
                    //ArrayList<Grupo> grupos =
                    JSONObject listagrupos = json.getJSONObject("groups_list");
                    JSONObject groupsfollowed = json.getJSONObject("groups_followed");

                    if (json.getBoolean("group_owner")) {
                        buildoptionsmenu.setOptions(new boolean[]{true, true, false});//this is for add e
                        buildoptionsmenu.show();
                        buildoptionsmenu.remove(R.id.action_listaEventos);
                    }

                    listaEventos.clear();

                    Boolean editable;
                    // loop recorriendo los eventos
                    for (int i = 0; i < eventos.length(); i++) {

                        JSONObject c = eventos.getJSONObject(i);

                        String id = c.getString(Constantes.EV_ID);

                        JSONObject grupo = listagrupos.getJSONObject(c.getString(Constantes.EV_IDGRUPO));
                        String nombregrupo = grupo.getString("nombre");

                        if (grupo.getString("creador").matches(preferences.getString(Constantes.SP_EMAIL_REG,""))){
                             editable = true;
                        }else{
                            editable = false;
                        }

                        String idgrupo = c.getString(Constantes.EV_IDGRUPO);
                        String placeName = c.getString("placename");
                        String placeId = c.getString("placeid");
                        String fecha = c.getString(Constantes.EV_FECHA);
                        String hora = c.getString(Constantes.EV_HORA);
                        String precio = c.getString(Constantes.EV_PRECIO);
                        String url = c.getString(Constantes.EV_URL);
                        String cartelBase64 = c.getString(Constantes.EV_THUMB);
                        String currencyCode =c.getString("currencycode");
                        String latitude = c.getString("latitude");
                        String longitude = c.getString("longitude");
                        byte[] thumb = Base64.decode(cartelBase64, Base64.DEFAULT);

                        Evento evento = new Evento(id, idgrupo, nombregrupo, null, thumb, placeName, placeId,Double.parseDouble(latitude),Double.parseDouble(longitude), fecha, hora, Integer.parseInt(precio),currencyCode, url);
                        if (groupsfollowed.has(c.getString(Constantes.EV_IDGRUPO))) {
                            evento.setFollowed(1);
                        } else {
                            evento.setFollowed(0);
                        }

                        evento.setEditable(editable);//esto es para que se pueda editar y cancelar el evento si somos los propietarios

                        listaEventos.add(evento);

                    }

                    showCategoriesDate();


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

