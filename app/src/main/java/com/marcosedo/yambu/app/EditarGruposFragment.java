package com.marcosedo.yambu.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class EditarGruposFragment extends Fragment implements OnClickListener {

    private static final String TAG = "YAMBU";
    private static SharedPreferences preferences;
    private Bundle bundle;
    private TextView tvNombre;
    private ImageView ivImgGrupo;
    private Button btnAceptar;
    private boolean editmode = false;
    private Bitmap bmOriginal;
    private Uri selectedImage = null;
    private String imagepath;
    private ProgressDialog pDialog; // Progress Dialog
    private byte[] imgByteArray = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate Config", "Se mete en el onCreate");
        preferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);//cargamos shared preferences desde oncreate para enerlo ya pa siempre
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//para poder hacer una actionbarcustom dinamicamente. oncreateoptionsmenu
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "Se mete en el onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            selectedImage = Uri.parse(savedInstanceState.getString("Uri"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
        //savedInstanceState.putParcelableArrayList("LIST_INSTANCE_STATE", listaGrupos);
        if (selectedImage != null)
            savedInstanceState.putString("Uri", selectedImage.toString());
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("onCreateView Config", "Se mete en el onCreateView");

        View view = inflater.inflate(R.layout.editar_grupo, container, false);

        //INICIALIZAMOS VARIABLES GLOBALES
        tvNombre = (TextView) view.findViewById(R.id.etNick);
        ivImgGrupo = (ImageView) view.findViewById(R.id.ivImgGrupo);
        //ivEditarNombre = (ImageView) view.findViewById(R.id.ivEditarNombre);
        btnAceptar = (Button) view.findViewById(R.id.btnAccept);


        //PONEMOS EL LISTENER EN LOS BOTONES
        ivImgGrupo.setOnClickListener(this);
        btnAceptar.setOnClickListener(this);

        ivImgGrupo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture));//Ponemos imagen por defecto

        //Obtenemos los argumentos que le hayamos podido pasar a la actividad
        //bundle = savedInstanceState;
        bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(Constantes.GP_ID) != null) {  //Si tenemos la id es porque hay que editar
                editmode = true;

                //capturamos los extras con elbytearray del cartel
                imgByteArray = bundle.getByteArray(Constantes.GP_THUMB);
                //PONEMOS LA IMAGEN EN EL IMAGEVIEW
                if (imgByteArray != null) {
                    bmOriginal = BitmapFactory.decodeByteArray(imgByteArray, 0, imgByteArray.length);
                    ivImgGrupo.setImageBitmap(bmOriginal);

                }
                //rellenamos todos los campos restantes
                tvNombre.setText(bundle.getString(Constantes.GP_NOMBRE));

                /////////////////////////////////////////////////////
                //Crear fichero con la foto perfil en jpeg
                /////////////////////////////////////////////////////
                int[] dim;
                int WIDTH = 0;
                int HEIGHT = 1;

                ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
                File dirImages = cw.getDir("PerfilImages", Context.MODE_PRIVATE);
                File imgFile = new File(dirImages, "imgTmp" + ".jpg");
                if (imgFile.exists()) {
                    imgFile.delete();
                }
                imagepath = imgFile.getAbsolutePath();
                try {
                    imgFile.createNewFile();

                    dim = Utilidades.calculaDimensionImagen(bmOriginal.getWidth(),bmOriginal.getHeight(), Constantes.MAX_WIDTH_FOTO, Constantes.MAX_WIDTH_FOTO);
                    Utilidades.creaJPEG(bmOriginal, imagepath,
                            dim[WIDTH], dim[HEIGHT], Constantes.JPEG_QUALITY_CARTEL);
                    /////////////////////////////////////////////////////////////////////
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (editmode) mActionBar.setSubtitle("Editing group");
        else mActionBar.setSubtitle("Creating group");

        return view;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.ivImgGrupo: {
                MostrarDialogoImagen();
                break;
            }

            case R.id.btnAccept: {
                new GuardarGrupo().execute();
                break;
            }
        }
    }

    private int SELECT_IMAGE = 1;
    private int TAKE_PICTURE = 2;


    public EditarGruposFragment() {
        super();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar. Lo queremos vacio sin nada
        menu.clear();
        //inflater.inflate(R.menu.options_editar_grupos, menu);//vamos a poner nuevas opciones
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected");
        // Handle presses on the action bar items
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        Log.i(TAG, Integer.toString(item.getItemId()));
        switch (item.getItemId()) {
            //case R.id.action_añadir:
            case R.id.action_añadir:
                EditarGruposFragment editarFragment = new EditarGruposFragment();
                transaction.replace(R.id.contenedorfragment, editarFragment, "editor");
                transaction.addToBackStack(null);
                transaction.commit();// Commit the transaction
                break;
        }
        return true;
    }*/

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
                                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
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

                                case 2://DELETING THE IMAGE
                                    Resources res = getActivity().getResources();
                                    imagepath = null;
                                    imgByteArray = null;
                                    selectedImage = null;
                                    ivImgGrupo.setImageDrawable(res.getDrawable(R.drawable.ic_contact_picture));
                                    break;
                            }
                        }
                    }).show();

        } catch (Exception e) {
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
                bmOriginal = MediaStore.Images.Media.getBitmap(cr, selectedImage);
            }

            //Cargamos la foto a partir de la uri de la foto
            if (selectedImage != null) {

                Bitmap bitmap = BitmapFactory.decodeFile(Utilidades.getRealPathFromURI(cr, selectedImage));

                if (bitmap != null) {
                    bitmap = Utilidades.redondearBitmap(bitmap, Constantes.RADIOPERCENT);
                    ivImgGrupo.setImageBitmap(bitmap);
                }

            }
            int[] dim;
            int WIDTH = 0;
            int HEIGHT = 1;

            /////////////////////////////////////////////////////
            //Crear fichero con la foto perfil en jpeg
            /////////////////////////////////////////////////////
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File dirImages = cw.getDir("PerfilImages", Context.MODE_PRIVATE);
            File imgFile = new File(dirImages, "imgTmp" + ".jpg");
            if (imgFile.exists()){
                imgFile.delete();
            }
            imagepath = imgFile.getAbsolutePath();
            imgFile.createNewFile();

            dim = Utilidades.calculaDimensionImagen(bmOriginal.getWidth(),bmOriginal.getHeight(), Constantes.MAX_WIDTH_CARTEL, Constantes.MAX_WIDTH_CARTEL);
            Utilidades.creaJPEG(bmOriginal, imagepath,dim[WIDTH], dim[HEIGHT], Constantes.JPEG_QUALITY_CARTEL);
            /////////////////////////////////////////////////////////////////////
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







   /* private void MostrarDialogoSelectNombre() {
        dialog = new Dialog(getActivity());
        //deshabilitamos el título por defecto
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Se establece el layout del diálogo
        dialog.setContentView(R.layout.select_nick_dialog);

        TextView titulo = (TextView) dialog.findViewById(R.id.TvTitulo);
        titulo.setText("Nombre del grupo");

        // Ahora se puede acceder a los componentes del diálogo, para
        // modificarlos
        edittextnick = (EditText) dialog.findViewById(R.id.EtNick);
        edittextnick.setText(tvNombre.getText());

        /////////////LISTENERS DE LOS BOTONES DEL DIALOGO////////////////
        ((Button) dialog.findViewById(R.id.BtnAceptar)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                tvNombre.setText(edittextnick.getText());
                //new GuardarGrupo().execute();
            }
        });

        ((Button) dialog.findViewById(R.id.BtnCancelar)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ///////////////////////////////////////////

        dialog.show();
    }*/

    //////////////////////////////////////////////////////
    ///Async Task para guardar el nombre de usuario en la BBDD///
    //////////////////////////////////////////////////////
    class GuardarGrupo extends AsyncTask<String, String, JSONObject> {

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            //pDialog = new ProgressDialog(EditarEventosActivity.this);
            pDialog.setMessage("Creando grupo ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            String nombre = tvNombre.getText().toString();

            ////////////////////////HTTP UPLOADER///////////////////////////////////////////
            HttpUploader uploader = null;
            try {
                uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.ADMIN_GRUPOS_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (editmode == false) {
                Log.i(TAG, "editmode false");

                uploader.añadirArgumento("mode", "1");//0 leer 1 crear 2 modificar 3 borrar
                uploader.añadirArgumento(Constantes.GP_CREADOR, preferences.getString(Constantes.SP_EMAIL_REG, ""));
                uploader.añadirArgumento(Constantes.GP_NOMBRE, nombre);

                if (imagepath != null) {
                    try { ////////  Leemos el archivo del cartel
                        FileInputStream fis = new FileInputStream(imagepath);
                        uploader.añadirArchivo("img_grupo.jpg", fis);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.i(TAG, "editmode true");
                Log.i(TAG, "id que pasamos es =" + bundle.getString(Constantes.GP_ID));

                uploader.añadirArgumento("mode", "2");//0 leer 1 crear 2 modificar 3 borrar
                uploader.añadirArgumento(Constantes.GP_ID, bundle.getString(Constantes.GP_ID));
                uploader.añadirArgumento(Constantes.GP_CREADOR, bundle.getString(Constantes.GP_CREADOR));
                uploader.añadirArgumento(Constantes.GP_NOMBRE, nombre);

                if (imagepath != null) {//si fisImagen es distinto de 0 es porque hemos buscado una foto en la galeria o en la camera
                    try { ////////
                        FileInputStream fis = new FileInputStream(imagepath);
                        uploader.añadirArchivo("img_grupo.jpg", fis);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }


            json = uploader.enviar();
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
                    editor.putString(Constantes.NICK, (String) tvNombre.getText().toString());
                    editor.commit();
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                    ListaGruposFragment fragment = (ListaGruposFragment) getActivity().getSupportFragmentManager().findFragmentByTag("my groups");

                    transaction.replace(R.id.contenedorfragment, fragment, "my groups");
                    transaction.addToBackStack(null);
                    transaction.commit();// Commit the transaction


                } else {
                    tvNombre.setText(preferences.getString(Constantes.NICK, ""));
                    Toast.makeText(getActivity().getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            pDialog.dismiss();
            //getActivity().finish();
        }


    }
}