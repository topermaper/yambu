package com.marcosedo.yambu.login;
import com.marcosedo.yambu.R;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Registered extends ActionBarActivity {

    Dialog dialogo;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registered);

        //set actionbar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);

        mActionBar.setIcon(R.drawable.actionbaricon48);
        mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbarbackground));
        mActionBar.setSubtitle("Successful registered");
        //////////////////////////////////


        Bundle extras = getIntent().getExtras();

        /**
         * Displays the registration details in Text view
         **/
        final TextView fname = (TextView)findViewById(R.id.fname);
        final TextView lname = (TextView)findViewById(R.id.lname);
        final TextView uname = (TextView)findViewById(R.id.uname);
        final TextView email = (TextView)findViewById(R.id.email);
        final TextView created_at = (TextView)findViewById(R.id.regat);
        fname.setText(extras.getString("fname"));
        lname.setText(extras.getString("lname"));
        uname.setText(extras.getString("uname"));
        email.setText(extras.getString("email"));
        created_at.setText(extras.getString("created_at"));

        ShowRegisteredDialog();

        Button login = (Button) findViewById(R.id.button1);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Login.class);
                startActivityForResult(myIntent, 0);
                finish();
            }

        });


    }

    private void ShowRegisteredDialog() {//si no hay ningun grupo a nuestro nombre no podremos crear ningun evento

        dialogo = new Dialog(this);
        //deshabilitamos el título por defecto
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Se establece el layout del diálogo
        dialogo.setContentView(R.layout.dialog_title_text_accept_cancel);
        dialogo.findViewById(R.id.BtnCancelar).setVisibility(View.GONE);

        TextView titulo = (TextView) dialogo.findViewById(R.id.TvTitulo);
        TextView tvTexto = (TextView) dialogo.findViewById(R.id.TvTexto);
        titulo.setText("Activate your account");
        tvTexto.setText(R.string.register_msg);

        /////////////LISTENERS DE LOS BOTONES DEL DIALOGO////////////////
        ((Button) dialogo.findViewById(R.id.BtnAceptar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             dialogo.dismiss();
            }
        });
         ///////////////////////////////////////////

        dialogo.show();
    }
}
