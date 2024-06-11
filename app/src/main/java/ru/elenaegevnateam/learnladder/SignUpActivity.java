package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText name;
    private EditText surname;
    private EditText edtpost;
    private String post;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.idEdtEmail_reg);
        password = findViewById(R.id.idEdtPassword_reg);
        name = findViewById(R.id.idEdtName_reg);
        surname = findViewById(R.id.idEdtSurname_reg);
        edtpost = findViewById(R.id.editText);
        Spinner spinner = findViewById(R.id.spinnerPost);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");

        findViewById(R.id.editText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                post = adapterView.getItemAtPosition(position).toString();
                edtpost.setText(post);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayList<String> postList = new ArrayList<>();
        postList.add("Ученик");
        postList.add("Учитель");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, postList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!name.getText().toString().isEmpty() && !surname.getText().toString().isEmpty() && !password.getText().toString().isEmpty() && !email.getText().toString().isEmpty()) {
                    createAccount(email.getText().toString(), password.getText().toString());
                }
                else {
                    Toast.makeText(SignUpActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendVerificationEmail();
                        } else {
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Подтвердите почту по полученному письму", Toast.LENGTH_SHORT).show();
                    databaseReferenceUsers.child(user.getUid()).child("name").setValue(name.getText().toString());
                    databaseReferenceUsers.child(user.getUid()).child("surname").setValue(surname.getText().toString());
                    databaseReferenceUsers.child(user.getUid()).child("post").setValue(post);
                    if (Objects.equals(post, "Ученик")) {
                        databaseReferenceUsers.child(user.getUid()).child("class").setValue("");
                        databaseReferenceUsers.child(user.getUid()).child("coins").setValue(0);
                    }
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Не удалось отправить письмо для подтверждения. Попробуйте еще раз.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
    }
}
