package com.marcosedo.yambu.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.marcosedo.yambu.R;

public class PreferencesFragment extends Fragment {

    private static SharedPreferences preferences;
    private RadioButton rbKilometers;
    private RadioButton rbMiles;
    private RadioGroup radioGroup;
    private BuildOptionsMenu buildoptionsmenu;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones
        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setSubtitle("Preferences");
        sharedPreferences = getActivity().getSharedPreferences(Constantes.SP_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.preferences_fragment, container, false);
        //INICIALIZAMOS VARIABLES GLOBALES
        rbKilometers = (RadioButton) view.findViewById(R.id.rbKilometers);
        rbMiles = (RadioButton) view.findViewById(R.id.rbMiles);
        radioGroup = (RadioGroup) view.findViewById(R.id.radio_group1);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override

            public void onCheckedChanged(RadioGroup group, int checkedId) {

                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Check which radio button was clicked
                switch (checkedId) {
                    case R.id.rbKilometers:
                        editor.putString(Constantes.SP_DISTANCE_UNITS, "km");
                        break;
                    case R.id.rbMiles:
                        editor.putString(Constantes.SP_DISTANCE_UNITS, "mi");
                        break;
                }

                editor.commit();
            }

        });


        loadPreferences();//carga nuestras preferences


        return view;
    }


    public void loadPreferences() {
        String unit = sharedPreferences.getString(Constantes.SP_DISTANCE_UNITS, "");
        switch (unit) {
            case "km":
                rbKilometers.setChecked(true);
                break;
            case "mi":
                rbMiles.setChecked(true);
                break;
        }


    }

    // Ponemos nuestros botones en la actionbar
    //onOptionsItemSelected esta en el main. asi tendremosjunto lo de todos los fragments
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        buildoptionsmenu.setOptions(new boolean[]{false, false, false});//this is for the add, modify and remove buttons
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_preferences);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
