package org.iesch.firebase_dam_iferrerf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    TextView tvEmail, tvMetodo;
    Button logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home Firebase");

        tvEmail = findViewById(R.id.EmailTextView);
        tvMetodo = findViewById(R.id.MetodoTextView);
        logOutButton = findViewById(R.id.LogOutButton);

        obtenerInfoDeLogin();
        eventoBoton();

    }

    private void eventoBoton() {
        logOutButton.setOnClickListener(view -> cerrarSesión());
    }

    private void cerrarSesión() {

        SharedPreferences sesion = getSharedPreferences("sesion", Context.MODE_PRIVATE);
        SharedPreferences.Editor Obj_Editor = sesion.edit();
        Obj_Editor.clear();
        Obj_Editor.apply();

        // Cerramos la sesión en Firebase
        FirebaseAuth.getInstance().signOut();
        // Volver a la página anterior. Lo mismo que boton de atrás
        onBackPressed();
    }

    private void obtenerInfoDeLogin() {
        // Recuperamos la información de LoginActivity
        Bundle datos = this.getIntent().getExtras();
        String email = datos.getString("email");
        String metodo = datos.getString("metodo");
        tvEmail.setText(email);
        tvMetodo.setText(metodo);

        // Guardo los datos al llegar a Home
        SharedPreferences sesion = getSharedPreferences("sesion", Context.MODE_PRIVATE);
        SharedPreferences.Editor Obj_Editor = sesion.edit();
        Obj_Editor.putString("email", email);
        Obj_Editor.putString("metodo", metodo);
        Obj_Editor.apply();
    }
}