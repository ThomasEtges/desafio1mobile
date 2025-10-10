package com.example.desafio1_mobile;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;

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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private FirebaseFirestore mStore;

    private LinearLayout layoutTasks;

    private TaskSqlite dbHelper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Button btnGoToLogin;
    private int CounterTasksInTrhreeDays = 0;
    private int CounterTasksInOneWeek = 0;
    private int CounterTasksDoLater = 0;

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
        dbHelper = new TaskSqlite(this);

        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        layoutTasks = findViewById(R.id.layoutTasks);

        btnAddTask.setOnClickListener(v -> showAddTask());

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
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


    public void login(String email, String password, BottomSheetDialog bottomSheetDialog) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCustomToken:success");
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        if (currentUser != null) {
                            String uid = currentUser.getUid();
                            mStore.collection("users").document(uid).get()
                                    .addOnCompleteListener(docTask -> {
                                        if (docTask.isSuccessful()) {
                                            DocumentSnapshot document = docTask.getResult();
                                            if (document.exists()) {
                                                boolean isFirstLogin = document.getBoolean("first_login");
                                                if (isFirstLogin) {
                                                    transferSqliteTaskFirestore(uid);
                                                    mStore.collection("users").document(uid).update("first_login", false);
                                                    Toast.makeText(getApplicationContext(), "primeiro login", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "não primeiro login", Toast.LENGTH_SHORT).show();
                                            }
                                            closBottomSheet(bottomSheetDialog);
                                            updateUI(currentUser);
                                        }

                                    });
                        }

                    } else {
                        Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Informações inválidas, email ou senha errados.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
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
        Toast.makeText(this, "Deslogado", Toast.LENGTH_SHORT).show();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Bem-vindo " + user.getEmail(), Toast.LENGTH_SHORT).show();
            btnGoToLogin.setText("Logout");
            btnGoToLogin.setOnClickListener(v-> logout());
        } else {
            Toast.makeText(this, "Você não está logado.", Toast.LENGTH_SHORT).show();
            btnGoToLogin.setText("Login");
            btnGoToLogin.setOnClickListener(v-> showLoginBottomSheet());
        }

        loadTasks();

    }

    ///////////////////////////////////////////////
    //////////////----TAREFAS----//////////////////
    ///////////////////////////////////////////////

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

        FirebaseUser curentUser = mAuth.getCurrentUser();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();
        Timestamp timestamp = new Timestamp(date);

        if (curentUser != null) {

            String uid = curentUser.getUid();

            TaskApp newTaskApp = new TaskApp(title, description, timestamp, uid);

            mStore.collection("tasks").document()
                    .set(newTaskApp)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loadTasks();
                            Toast.makeText(getApplicationContext(), "Salvo Firestore", Toast.LENGTH_SHORT).show();
                        }

                    });

        } else {

            int countTasks = dbHelper.getTasksCount();
            if (countTasks >= 5){
                Toast.makeText(getApplicationContext(), "Limite de 5 tarefas, faça login para mais tarefas", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.createtTaskSqlite(title, description, timestamp, false);
                Toast.makeText(getApplicationContext(), "Salvo Sqlite", Toast.LENGTH_SHORT).show();
                loadTasks();
            }
        }

    }

    private void loadTasks() {

        layoutTasks.removeAllViews();
        resetCounterTasks();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            loadTasksFromFirestore(currentUser.getUid());
        } else {
            loadTasksFromSqlite();
        }

    }

    private void loadTasksFromFirestore(String uid) {
        mStore.collection("tasks")
                .whereEqualTo("uid", uid)
                .orderBy("completed", Query.Direction.ASCENDING)
                .orderBy("conclusion_date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String title = document.getString("title");
                        String description = document.getString("description");
                        Timestamp timestamp = document.getTimestamp("conclusion_date");
                        Boolean completed = document.getBoolean("completed");
                        String task_id = document.getId();


                        addTaskView(title, description, timestamp.toDate(), completed, task_id);
                    }

                    updateCounterTasks();
                });
    }

    private void loadTasksFromSqlite() {

        Cursor cursor = dbHelper.getAllTasks();

            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    long dateInMillis = cursor.getLong(cursor.getColumnIndexOrThrow("conclusion_date"));
                    int completedInfo = cursor.getColumnIndexOrThrow("completed");
                    boolean completed = cursor.getInt(completedInfo) == 1;
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                    String task_id = String.valueOf(id);

                    addTaskView(title, description, new Date(dateInMillis), completed, task_id);

                } while (cursor.moveToNext());
            }

            updateCounterTasks();
            cursor.close();
    }

    private void resetCounterTasks(){
        CounterTasksInTrhreeDays = 0;
        CounterTasksInOneWeek = 0;
        CounterTasksDoLater = 0;
    }

    private void updateCounterTasks(){

        TextView TasksInThreeDays = findViewById(R.id.TasksInTrhreeDays);
        TasksInThreeDays.setText(String.valueOf(CounterTasksInTrhreeDays));

        TextView TasksInOneWeek = findViewById(R.id.TasksInOneWeek);
        TasksInOneWeek.setText(String.valueOf(CounterTasksInOneWeek));

        TextView TasksDoLater = findViewById(R.id.TasksDoLater);
        TasksDoLater.setText(String.valueOf(CounterTasksDoLater));

    }

    private void addTaskView(String title, String description, Date conclusionDate, Boolean completed, String task_id) {

        View taskView = getLayoutInflater().inflate(R.layout.task_item, layoutTasks, false);

        TextView taskTitle = taskView.findViewById(R.id.taskTitle);
        TextView taskDescription = taskView.findViewById(R.id.taskDescription);
        TextView taskConclusionDate = taskView.findViewById(R.id.taskConclusionDate);
        Switch taskStatus = taskView.findViewById(R.id.taskStatus);

        taskTitle.setText(title);
        taskDescription.setText(description);
        taskConclusionDate.setText(dateFormat.format(conclusionDate));
        taskStatus.setChecked(completed);

        taskStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTaskStatus(task_id, isChecked);
        });

        Date now = new Date();
        long diffMillis = conclusionDate.getTime() - now.getTime();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);

        if (diffDays < 0) {
            taskView.setBackgroundColor(Color.parseColor("#77acdc"));
        } else if (diffDays <= 3) {
            if(!completed) {
                CounterTasksInTrhreeDays++;
            }
            taskView.setBackgroundColor(Color.parseColor("#ec274a"));
        } else if (diffDays <= 7) {
            if(!completed) {
                CounterTasksInOneWeek++;
            }
            taskView.setBackgroundColor(Color.parseColor("#FFF9C4"));
        } else {
            if(!completed) {
                CounterTasksDoLater++;
            }
            taskView.setBackgroundColor(Color.parseColor("#C8E6C9"));
        }

        if(completed){
            taskView.setBackgroundColor(Color.parseColor("#608809"));
        }

        layoutTasks.addView(taskView);
    }

    private void updateTaskStatus(String task_id, boolean completed) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            mStore.collection("tasks").document(task_id)
                    .update("completed", completed)
                    .addOnSuccessListener(v -> {
                        loadTasks();
                    });
        } else {
            dbHelper.updateTaskStatus(task_id, completed);
            loadTasks();
        }

    }

    private void transferSqliteTaskFirestore(String uid) {

        Cursor cursor = dbHelper.getAllTasks();

        if(cursor != null){
            if (cursor.moveToFirst()) {
                do{

                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    Date date = new Date(cursor.getLong(cursor.getColumnIndexOrThrow("conclusion_date")));
                    Timestamp conclusion_date = new Timestamp(date);

                    TaskApp taskToTransfer = new TaskApp(title, description, conclusion_date, uid);

                    mStore.collection("tasks").document()
                            .set(taskToTransfer);

                } while (cursor.moveToNext());
                cursor.close();

            }

            dbHelper.deleteAllTasks();

        }

    }



}
