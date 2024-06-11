package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String post;

    View loadingView;
    TextView forgotPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        forgotPassword = findViewById(R.id.forgotPasswordTextView);
        loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.GONE);

        findViewById(R.id.emailSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mail = findViewById(R.id.idEdtEmail);
                EditText password = findViewById(R.id.idEdtPassword);
                String login = mail.getText().toString();
                String pas = password.getText().toString();
                if (!login.isEmpty() && !pas.isEmpty()) {
                    authSignInWithEmailAndPassword(login, pas);
                } else {
                    Toast.makeText(SignInActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
                EditText emailBox = dialogView.findViewById(R.id.emailBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = emailBox.getText().toString();

                        if (TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            Toast.makeText(SignInActivity.this, "Укажите почту для сброса пароля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "Письмо для сброса пароля отправлено на Вам почту", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else{
                                    Toast.makeText(SignInActivity.this, "Не удалось отправить письмо", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                dialogView.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                dialog.show();
            }
        });
    }

    protected final void authSignInWithEmailAndPassword(String emailId, String password) {
        mAuth.signInWithEmailAndPassword(emailId, password).addOnCompleteListener((@NonNull Task<AuthResult> task) -> {
            if (task.isSuccessful()) {
                checkIfEmailVerified();
            } else {
                Toast.makeText(SignInActivity.this, "Ошибка входа: неверный логин или пароль.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            loadingView.setVisibility(View.VISIBLE);
            proceedToNextActivity(user);
        }
    }

    private void checkIfEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            proceedToNextActivity(user);
        } else {
            Toast.makeText(this, "Пожалуйста, подтвердите свою почту по полученному письму.", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
        }
    }

    private void proceedToNextActivity(FirebaseUser user) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(user.getUid()).child("post").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                post = String.valueOf(task.getResult().getValue());
                Intent intent;
                if (Objects.equals(post, "Ученик")) {
                    intent = new Intent(getApplicationContext(), ThemesActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), TeacherTasksActivity.class);
                }
                intent.putExtra("user_id", user.getUid());
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}