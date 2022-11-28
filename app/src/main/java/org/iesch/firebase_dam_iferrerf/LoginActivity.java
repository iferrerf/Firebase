package org.iesch.firebase_dam_iferrerf;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

enum ProviderType {
    BASIC,
    GOOGLE
}

public class LoginActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 100;
    EditText etEmail;
    EditText etPassword;
    Button btnRegistrar, btnLogin, btnLoginGoogle;


    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login Firebase");

        etEmail = findViewById(R.id.EmailEditText);
        etPassword = findViewById(R.id.PasswordEditText);
        btnRegistrar = findViewById(R.id.RegisterButton);
        btnLogin = findViewById(R.id.AccessButton);
        btnLoginGoogle = findViewById(R.id.btnGoogle);

        iniciarAnalytics();

        iniciarAuthentication();

        // Comprobar si tenemos la sesion abierta
        comprobarSiEstaLogueado();

    }

    private void comprobarSiEstaLogueado() {
        SharedPreferences sesion = getSharedPreferences("sesion", Context.MODE_PRIVATE);
        String _email = sesion.getString("email", null);
        String _metodo = sesion.getString("metodo", null);

        if (_email != null && _metodo != null) {
            iraHomeActivity(_email, ProviderType.valueOf(_metodo));
        }
    }

    private void iniciarAnalytics() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString("DAM2", "Aplicacion Iniciada");
        mFirebaseAnalytics.logEvent("DAM2", bundle);

    }

    private void iraHomeActivity(String email, ProviderType tipoLogueo) {
        Intent i = new Intent(LoginActivity.this, HomeActivity.class);
        i.putExtra("email", email);
        i.putExtra("metodo", tipoLogueo.toString());
        startActivity(i);
    }

    private void iniciarAuthentication() {
        mAuth = FirebaseAuth.getInstance();

        btnRegistrar.setOnClickListener(view -> registrarConEmailPassword());
        btnLogin.setOnClickListener(view -> loguearConEmailyPassword());
        btnLoginGoogle.setOnClickListener(view -> loguearConGoogle());
    }

    private void loguearConGoogle() {
        // Al hacer clic en el boton de login con Google:
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Nos creamos el GoogleSignInClient
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            // Esto significa que venimos de loguearnos con google
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
             GoogleSignInAccount account = task.getResult(ApiException.class);
             Log.w("FIREBASE", "FirebaseAuthConGoogle: " + account.getId());
                firebaseAuthConGoogle(account.getIdToken());
            } catch (ApiException e) {
                //e.printStackTrace();
                Log.w("FIREBASE", "Google SignIN ha fallado", e);
            }
        }
    }

    private void firebaseAuthConGoogle (String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = mAuth.getCurrentUser();
                            iraHomeActivity(user.getEmail(), ProviderType.GOOGLE);
                        }else {
                            Log.w("FIREBASE", "Google SignIn ha fallado");
                        }
                    }
                });
    }

    private void loguearConEmailyPassword() {
        String _email = etEmail.getText().toString();
        String _password = etPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(_email, _password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FIREBASE", "signInUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            iraHomeActivity(_email, ProviderType.BASIC);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE", "signInUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Error al loguear usuario",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void registrarConEmailPassword() {

        String _email = etEmail.getText().toString();
        String _password = etPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(_email, _password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("FIREBASE", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "El usuario se ha registrado correctamente",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FIREBASE", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Error al registrar usuario",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}