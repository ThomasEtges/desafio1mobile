package com.example.desafio1_mobile;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.desafio1_mobile.TaskApp;
import com.example.desafio1_mobile.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Random;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FirebaseFirestore mStore;

    private LinearLayout layoutTasks;

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
        mStore = FirebaseFirestore.getInstance();

        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        layoutTasks = findViewById(R.id.layoutTasks);

        btnGoToLogin.setOnClickListener(v -> showLoginBottomSheet());
        btnAddTask.setOnClickListener(v -> showAddTask());

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        loadTasks();
    }

    //////////////----TUDO DE LOGIN E CADASTRO----//////////////////

    private void showLoginBottomSheet() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_login);
        EditText inputEmail = bottomSheetDialog.findViewById(R.id.inputEmail);
        EditText inputPassword = bottomSheetDialog.findViewById(R.id.inputPassword);
        Button btnLogin = bottomSheetDialog.findViewById(R.id.btnLogin);
        Button btnClose = bottomSheetDialog.findViewById(R.id.btnClose);
        Button btnGoToRegister = bottomSheetDialog.findViewById(R.id.btnGoToRegister);

        assert btnLogin != null;
        btnLogin.setOnClickListener(v-> {
            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();
            validateInputLogin(email,password,bottomSheetDialog);
        });

        assert btnClose != null;
        btnClose.setOnClickListener(v -> {
            closBottomSheet(bottomSheetDialog);
        });

        assert btnGoToRegister != null;
        btnGoToRegister.setOnClickListener(v -> {
            closBottomSheet(bottomSheetDialog);
            showRegisterBottomSheet();
        });

        bottomSheetDialog.show();
    }

    private void closBottomSheet(BottomSheetDialog bottomSheetDialog){
        bottomSheetDialog.dismiss();
    }

    private void validateInputLogin(String email, String password, BottomSheetDialog bottomSheetDialog){
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }else{
            login(email,password,bottomSheetDialog);
        }
    }

    private void validateInputRegister(String email, String password, String name, BottomSheetDialog bottomSheetDialog) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(getApplicationContext(), "Email inválido", Toast.LENGTH_SHORT).show();
        }
        else if(password.length() < 6){
            Toast.makeText(getApplicationContext(), "Senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
        }
        else{
            registerUser(email,password, name ,bottomSheetDialog);
        }
    }

    private void showRegisterBottomSheet() {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_register);
        EditText inputEmailRegister = bottomSheetDialog.findViewById(R.id.inputEmailRegister);
        EditText inputPasswordRegister = bottomSheetDialog.findViewById(R.id.inputPasswordRegister);
        EditText inputNameRegister = bottomSheetDialog.findViewById(R.id.inputNameRegister);

        Button btnRegister = bottomSheetDialog.findViewById(R.id.btnRegister);
        Button btnClose = bottomSheetDialog.findViewById(R.id.btnClose);
        Button btnGoToLogin = bottomSheetDialog.findViewById(R.id.btnGoToLogin);

        assert btnRegister != null;
        btnRegister.setOnClickListener( v-> {
            String email = inputEmailRegister.getText().toString();
            String password = inputPasswordRegister.getText().toString();
            String name = inputNameRegister.getText().toString();
            validateInputRegister(email,password,name,bottomSheetDialog);
        });

        assert btnClose != null;
        btnClose.setOnClickListener(v -> {
            closBottomSheet(bottomSheetDialog);
        });

        assert btnGoToLogin != null;
        btnGoToLogin.setOnClickListener(v -> {
            closBottomSheet(bottomSheetDialog);
                showLoginBottomSheet();
        });

        bottomSheetDialog.show();
    }


    public void login(String email, String password, BottomSheetDialog bottomSheetDialog){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCustomToken:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            closBottomSheet(bottomSheetDialog);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Informações inválidas, email ou senha errados.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void registerUser(String email, String password,  String name, BottomSheetDialog bottomSheetDialog) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(),"Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                            String uid = user.getUid();

                            User newUser = new User(uid, name);

                            mStore.collection("users").document(uid)
                                    .set(newUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });



                            closBottomSheet(bottomSheetDialog);
                            showLoginBottomSheet();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(),
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Bem-vindo " + user.getEmail(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Você não está logado.", Toast.LENGTH_SHORT).show();
        }
    }

    //////////////----TAREFAS----//////////////////

    private void showAddTask(){

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_task);

        EditText inputTilteTask = dialog.findViewById(R.id.inputTitleTask);
        EditText inputDescriptionTask = dialog.findViewById(R.id.inputDescriptionTask);
        DatePicker inputDateTask = dialog.findViewById(R.id.inputDateTask);
        Button btnCreateTask = dialog.findViewById(R.id.btnCreateTask);

        btnCreateTask.setOnClickListener(v->{

            String title = inputTilteTask.getText().toString();
            String description = inputDescriptionTask.getText().toString();
            int day = inputDateTask.getDayOfMonth();
            int month = inputDateTask.getMonth();
            int year = inputDateTask.getYear();

            if(title.isEmpty() || description.isEmpty() || day == 0 || month == 0 || year == 0){
                Toast.makeText(getApplicationContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            } else {
                createTask(title, description, day, month, year);
                dialog.dismiss();
            }

        });

        dialog.show();

    }

    private void createTask(String title, String description, int day, int month, int year) {

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        if (!uid.isEmpty()) {

            TaskApp newTaskApp = new TaskApp(title, description, uid);

            mStore.collection("tasks").document()
                    .set(newTaskApp)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        } else {
            
        }

        loadTasks();

    }

    private void loadTasks(){

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        mStore.collection("tasks")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    layoutTasks.removeAllViews();

                    for(QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        View taskView = getLayoutInflater().inflate(R.layout.task_item, layoutTasks, false);
                        TextView taskTitle = taskView.findViewById(R.id.taskTitle);
                        TextView taskDescription = taskView.findViewById(R.id.taskDescription);


                        taskTitle.setText(document.getString("title"));
                        taskDescription.setText(document.getString("description"));

                        layoutTasks.addView(taskView);

                    }})
                    .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();

                });

    }



}
