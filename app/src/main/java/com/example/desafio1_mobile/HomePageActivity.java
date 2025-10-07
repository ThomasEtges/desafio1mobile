package com.example.desafio1_mobile;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);

        btnGoToLogin.setOnClickListener(v -> showLoginBottomSheet());
    }

    private void showLoginBottomSheet() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_login);
        EditText inputEmail = bottomSheetDialog.findViewById(R.id.inputEmail);
        EditText inputPassword = bottomSheetDialog.findViewById(R.id.inputPassword);
        Button btnLogin = bottomSheetDialog.findViewById(R.id.btnLogin);
        Button btnClose = bottomSheetDialog.findViewById(R.id.btnClose);
        Button btnGoToRegister = bottomSheetDialog.findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener( v-> {
            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();
            validateInputLogin(email,password);
        });

        btnClose.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        btnGoToRegister.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showRegisterBottomSheet();
        });

        bottomSheetDialog.show();
    }

    private void validateInputLogin(String email, String password){
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }else{
            login(email,password);
        }
    }

    private void showRegisterBottomSheet() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_register);

        Button btnClose = bottomSheetDialog.findViewById(R.id.btnClose);
        Button btnGoToLogin = bottomSheetDialog.findViewById(R.id.btnGoToLogin);

        btnClose.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        btnGoToLogin.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showLoginBottomSheet();
        });

        bottomSheetDialog.show();
    }


    public void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Informações inválidas, email ou senha errados.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }



    public void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }
}


//public void cadastrarUsuario(String email, String password) {
//    mAuth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if (task.isSuccessful()) {
//                        // Cadastro bem-sucedido
//                        Log.d(TAG, "createUserWithEmail:success");
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        Toast.makeText(getApplicationContext(),
//                                "Usuário cadastrado com sucesso!",
//                                Toast.LENGTH_SHORT).show();
//                        //updateUI(user);
//                    } else {
//                        // Falha no cadastro
//                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                        Toast.makeText(getApplicationContext(),
//                                "Erro ao cadastrar: " + task.getException().getMessage(),
//                                Toast.LENGTH_LONG).show();
//                        //updateUI(null);
//                    }
//                }
//            });
//}