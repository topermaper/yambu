package com.marcosedo.yambu.app;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.marcosedo.yambu.BuildConfig;
import com.marcosedo.yambu.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MostrarImagenFragment extends Fragment {

    private String imagename;
    private ImageView ivImage;
    private BuildOptionsMenu buildOptionsMenu;

    @Override
    public void onCreate (Bundle savedInstanceState){
        buildOptionsMenu = new BuildOptionsMenu();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        super.onCreate(savedInstance);

        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr
        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setSubtitle("Show image");

        //Resources res = getResources();
        View view = inflater.inflate(R.layout.mostrar_imagen_layout, container, false);
        ivImage = (ImageView) view.findViewById(R.id.ivImagen);
        Log.i("imagen fragment","entramos en el oncreate view");
        Bundle bundle = getArguments();
        if (bundle != null) {
            imagename = bundle.getString("imagepath");

            new LoadImage().execute(imagename);

            //Bitmap cartelBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            //imageView.setImageBitmap(cartelBitmap);
        }
        else Log.i("TAG","pero el bundle es null");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        buildOptionsMenu.setOptions(new boolean[]{false, false, false});
        buildOptionsMenu.setMenu(menu);
        buildOptionsMenu.show();
        super.onCreateOptionsMenu(menu, inflater);
    }
    /////////////////////////////////////////////////////
    ///Async Task para obtener la imagen que queremos mostrar//////////
    /////////////////////////////////////////////////////
    class LoadImage extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        //Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Loading image...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected JSONObject doInBackground(String... args) {

            JSONObject json = new JSONObject();

            try {
                HttpUploader uploader = new HttpUploader(BuildConfig.DOMAIN + Constantes.GET_IMAGE_FILE);
                Log.e("argumento","args[0] = "+args[0]);
                uploader.añadirArgumento("image", args[0]);
                //uploader.añadirArgumento("image_height", Float.toString(size));
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

                if (success == 1) {//si fue bien
                    // Getting Array of Events
                    String image_string = json.getString("image");
                    byte[] image_byte = Base64.decode(image_string, Base64.DEFAULT);

                    ivImage.setImageBitmap(BitmapFactory.decodeByteArray(image_byte, 0, image_byte.length));

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
