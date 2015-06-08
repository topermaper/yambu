package com.marcosedo.yambu.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.marcosedo.yambu.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HelpFragment extends Fragment {


    private ListView listView;
    private ArrayList<Map<String, Object>> data;
    private BuildOptionsMenu buildoptionsmenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//esto indica que vamos a poder modificar la actionBAr
        //PONEMOS SUBTITULO
        ActionBar mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setSubtitle("Help");
        buildoptionsmenu = new BuildOptionsMenu();//Instanciamos nuestra clase que nos hara el favor de llenar nuestras opciones

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.help, container, false);


        //////////LISTVIEW, ADAPTER Y TEXTO DE LOS MENUS
        final Resources resources = getResources();
        listView = (ListView) view.findViewById(android.R.id.list);
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        String[] titles_str =  {resources.getString(R.string.appVersionTxt),resources.getString(R.string.feedbackMail), resources.getString(R.string.OSLicenses)};
        String[] subtitles_str =  {resources.getString(R.string.appVersionNumber),resources.getString(R.string.myEmailAddress), ""};

        for (int i=0; i<titles_str.length; i++) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", titles_str[i]);
            datum.put("subtitle", subtitles_str[i]);
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[]{"title", "subtitle"},
                new int[]{android.R.id.text1,
                        android.R.id.text2});
        listView.setAdapter(adapter);
        //LISTENER DEL LIST VIEW esto nos saca el context menu con un solo click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View v, int position,long arg3) {

                Resources resource = getActivity().getResources();

                switch (position) {
                    case 2:
                        String LicenseInfo = resource.getString(R.string.headerOSLicenses)+GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());
                        AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(getActivity());
                        LicenseDialog.setTitle(resource.getString(R.string.OSLicenses));

                        if (LicenseInfo != null) {
                            LicenseDialog.setMessage(LicenseInfo);
                            LicenseDialog.show();
                            break;
                        }
                }
            }
        });
        ///////////////////////////////////////////////////////////////

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        buildoptionsmenu.setOptions(new boolean[]{false, false, false});
        buildoptionsmenu.setMenu(menu);
        buildoptionsmenu.show();
        buildoptionsmenu.remove(R.id.action_help);
        super.onCreateOptionsMenu(menu, inflater);
    }


}
