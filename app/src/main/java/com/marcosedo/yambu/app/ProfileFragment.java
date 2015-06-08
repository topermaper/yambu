package com.marcosedo.yambu.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class ProfileFragment extends Fragment implements OnClickListener {

    private EditText etUsername;
    private EditText etFirstName;
    private EditText etLastName;
    private ImageView ivImage;
    private Button btnSaveChanges;

    private static SharedPreferences preferences;

    Bitmap bitmapAjustado; //aqui meteremos el bitmap de la foto de perfil pero comprimida y con menos resolucion
    File imagenTmp;
    private Uri selectedImage = null;

    private ProgressDialog pDialog; // Progress Dialog
    private BuildOptionsMenu buildoptionsmenu;
    private String imagepath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate Config", "Se mete en el onCreate");
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            selectedImage = Uri.parse(savedInstanceState.getString("Uri"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedImage != null)
            outState.putString("Uri", selectedImage.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView Config", "Se mete en el onCreateView");

        View view = inflater.inflate(R.layout.perfil_fragment, container, false);
        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr
        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones
        //INICIALIZAMOS VARIABLES GLOBALES
        etUsername = (EditText) view.findViewById(R.id.etuname);
        etFirstName = (EditText) view.findViewById(R.id.etfname);
        etLastName = (EditText) view.findViewById(R.id.etlname);
        ivImage = (ImageView) view.findViewById(R.id.ivProfileImage);
        btnSaveChanges = (Button) view.findViewById(R.id.btnSaveChanges);

        //PONEMOS EL LISTENER EN LOS BOTONES
        ivImage.setOnClickListener(this);
        btnSaveChanges.setOnClickListener(this);

        //Cargamos preferencias
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);

        new LoadProfile().execute();//carga la foto leyendo el archivo devid.jpg del telefono

        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setSubtitle("User Profile");

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivProfileImage: {
                MostrarDialogoImagen();
                break;
            }
            case R.id.btnSaveChanges:
                new SaveProfile().execute();
                break;
        }
    }

    private int SELECT_IMAGE = 1;
    private int TAKE_PICTURE = 2;

    private void MostrarDialogoImagen() {
        try {
            final String[] items = {"Galería", "Cámara", "Eliminar foto"};
            final Integer[] icons = new Integer[]{R.drawable.ic_menu_gallery,
                    R.drawable.ic_menu_camera, R.drawable.ic_menu_delete};

            ArrayAdapterWithIcon adapter = new ArrayAdapterWithIcon(getActivity(), items, icons, null);

            new AlertDialog.Builder(getActivity()).setTitle("Foto de perfil")
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            Intent intent;
                            switch (item) {
                                case 0:
                                    intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, SELECT_IMAGE);
                                    break;
                                case 1:

                                    Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    File file = new File(Environment.getExternalStorageDirectory(), "camera.jpg");
                                    selectedImage = Uri.fromFile(file);
                                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                                    imageCaptureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                    startActivityForResult(imageCaptureIntent, TAKE_PICTURE);

                                    break;

                                case 2:
                                    break;
                            }
                        }
                    }).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("onACtivityResul", "code = " + Integer.toString(requestCode));
        try {
            ContentResolver cr = getActivity().getContentResolver();

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_IMAGE) {
                    selectedImage = data.getData();
                }

                if (requestCode == TAKE_PICTURE) {
                    getActivity().getContentResolver().notifyChange(selectedImage, null);
                }
                imagepath = Utilidades.getRealPathFromURI(cr, selectedImage);//nos guarfamos el path a la foto
                //bmOriginal = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
            }

            //Cargamos la foto a partir de la uri de la foto
            if (imagepath != null) {

                Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                int[] screenResolution = Utilidades.getScreenResolution(getActivity());//nos saca la resloucion de nuestra pantalla
                int[] dimension = Utilidades.calculaDimensionImagen(bitmap.getWidth(), bitmap.getHeight(), screenResolution[0], screenResolution[1]);//la resolucion maxima sera la de la pantalla nuestra
                bitmapAjustado = Utilidades.redimensionarBitmap(bitmap,dimension[0],dimension[1]);//creamos un bitmap ajustado a la resolucion maxima de pantalla. esto es lo que guardamos en el server
                ivImage.setImageBitmap(Utilidades.redimensionarBitmap(bitmap,ivImage.getWidth(),ivImage.getHeight()));//Ponemos en el imageView ek bitmap ajustado al tamaño del imageview

                String storepath = getActivity().getFilesDir()+ "/tmp/";
                Utilidades.storeBitmap(bitmapAjustado, storepath, preferences.getString(Constantes.SP_EMAIL_REG, "") + ".jpeg", 0, 0);//creamos un temporal
                imagenTmp = new File(storepath + preferences.getString(Constantes.SP_EMAIL_REG, "")+".jpeg");

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // Ponemos nuestros botones en la actionbar
    //onOptionsItemSelected esta en el main. asi tendremosjunto lo de todos los fragments
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        buildoptionsmenu.setOptions(new boolean[]{false, false, false});//this is for the add, modify and remove buttons
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_profile);
        super.onCreateOptionsMenu(menu, inflater);
    }


    //////////////////////////////////////////////////////
///Async Task para guardar el nombre de usuario en la BBDD///
//////////////////////////////////////////////////////
    class SaveProfile extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            //pDialog = new ProgressDialog(EditarEventosActivity.this);
            pDialog.setMessage("Saving profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected JSONObject doInBackground(String... args) {

            JSONObject json = null;
            String username = etUsername.getText().toString();
            String lastname = etLastName.getText().toString();
            String firstname = etFirstName.getText().toString();

            ///////////////////////////////////////////////////////////////////
            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.SAVE_PROFILE);
                uploader.añadirArgumento("uname", username);
                uploader.añadirArgumento("lname", lastname);
                uploader.añadirArgumento("fname", firstname);
                uploader.añadirArgumento("email", preferences.getString(Constantes.SP_EMAIL_REG, ""));


                if (imagepath != null) {//si imagepath es distinto de 0 es porque hemos buscado una foto en la galeria o en la camera
                    try { ////////
                        FileInputStream fis = new FileInputStream(imagenTmp);
                        uploader.añadirArchivo("imgProfile.jpg", fis);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                json = uploader.enviar();
            } catch (IOException ioe) {
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
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);
                if (success == 1) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constantes.NICK, (String) etUsername.getText().toString());
                    editor.commit();
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                    if (imagepath != null) {//si fisImagen es distinto de 0 es porque hemos buscado una foto en la galeria o en la camera
                       Utilidades.storeBitmap(bitmapAjustado, getActivity().getFilesDir() + "/images/", preferences.getString(Constantes.SP_EMAIL_REG, "") + ".jpeg", 0, 0);//bitmapAjsutado ya esta ajustado asi que le pasamos 0,0
                    }

                    } else {
                    etUsername.setText(preferences.getString(Constantes.NICK, ""));
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
            //finish();
        }

    }


    //////////////////////////////////////////////////////
///Async Task para cargar los datos del perfil
//////////////////////////////////////////////////////
    class LoadProfile extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = null;
            HttpUploader uploader;

            /////ESTO ES PARA OBTENER EL listPreferredItemHeight que hemos definido en el layout
            android.util.TypedValue value = new android.util.TypedValue();
            getActivity().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
            TypedValue.coerceToString(value.type, value.data);
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            float size = value.getDimension(metrics);
            /////////////////////////////////////////////////////

            try {
                uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.LEER_USUARIOS_FILE);
                uploader.añadirArgumento("email", preferences.getString(Constantes.SP_EMAIL_REG, ""));
                uploader.añadirArgumento("thumb_width", Float.toString(size));
                uploader.añadirArgumento("thumb_height", Float.toString(size));
                json = uploader.enviar();
            } catch (IOException e) {
                e.printStackTrace();
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
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);

                JSONArray usuariosJSONArray = json.getJSONArray("usuarios");

                // loop recorriendo los usuarios. Solo deberia haber uno con el email que le hemos pasado
                for (int i = 0; i < usuariosJSONArray.length(); i++) {
                    JSONObject c = usuariosJSONArray.getJSONObject(i);

                    String uname = c.getString("username");
                    String fname = c.getString("firstname");
                    String lname = c.getString("lastname");

                    etUsername.setText(uname);
                    etFirstName.setText(fname);
                    etLastName.setText(lname);

                    String imgBase64 = c.getString("image");
                    byte[] imgByteArray = Base64.decode(imgBase64, Base64.DEFAULT);
                    ivImage.setImageBitmap(BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
        }
    }

    /*////////////////////////////////////////////////////
    ///Async Task para guardar la foto de perfil en la BBDD///
    //////////////////////////////////////////////////////
    class GuardarFotoPerfil extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Guardando foto de perfil ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = null;
            FileInputStream fis = null;

            int[] dimensiones;
            int WIDTH = 0;
            int HEIGHT = 1;
            String fotofilepath;

            //String realpath = PhotoUtils.getRealPathFromURI(getActivity().getContentResolver(), selectedImage);

            //Obtenemos bitmap a partir del imageView
            //BitmapDrawable drawable = (BitmapDrawable) ivImage.getDrawable();
            //Bitmap bmOriginal = drawable.getBitmap();
            ///////////////////////////////////////////

            try {
                /////////////////////////////////////////////////////
                //Crear fichero con la foto perfil en jpeg
                /////////////////////////////////////////////////////
                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                File dirImages = cw.getDir("PerfilImages", Context.MODE_PRIVATE);
                File fotoFile = new File(dirImages, "fotoperfil" + ".jpg");

                if (fotoFile.exists()) {
                    fotoFile.delete();
                }
                fotofilepath = fotoFile.getAbsolutePath();
                fotoFile.createNewFile();
                fis = new FileInputStream(fotofilepath);

                dimensiones = Utilidades.calculaDimensionImagen(bmOriginal, Constantes.MAX_WIDTH_FOTO, Constantes.MAX_HEIGHT_FOTO);

                Utilidades.creaJPEG(bmOriginal, fotofilepath, dimensiones[WIDTH], dimensiones[HEIGHT], Constantes.JPEG_QUALITY_FOTO);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //guardamos la foto de perfil en el sercidor
            HttpUploader uploader = null;
            try {
                uploader = new HttpUploader(Constantes.DOMINIO + Constantes.GUARDAR_FOTOPERFIL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String devid = Secure.getString(getActivity().getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
            uploader.añadirArgumento("devid", devid);
            uploader.añadirArchivo(devid + ".jpg", fis);//se guarda en el server con la id del dispositivo .jpg
            json = uploader.enviar();

            return json;
        }


        protected void onPostExecute(JSONObject json) {
            // check for success tag
            int success;
            String mensaje;

            try {
                success = json.getInt(Constantes.JSON_SUCCESS);
                mensaje = json.getString(Constantes.JSON_MESSAGE);


                if (success == 1) {
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                } else {
                    //Borramos el jpg creado para enviar al servidor
                    ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                    File dirImages = cw.getDir("PerfilImages", Context.MODE_PRIVATE);
                    File fotoFile = new File(dirImages, "fotoperfil" + ".jpg");
                    if (fotoFile.exists()) {
                        fotoFile.delete();
                    }
                    //y quitamos las imagen del image view
                    ivImage.setImageResource(R.drawable.ic_contact_picture);

                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
            //finish();
        }

    }*/
}


    
