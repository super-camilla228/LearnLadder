package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View loadingView;
    private String user_id;
    private String clas;
    private TextView nameTextView;
    private TextView surnameTextView;
    private DatabaseReference databaseReferenceUser;
    private DatabaseReference databaseReferenceClasses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUser = database.getReference("users").child(user_id);
        databaseReferenceClasses = database.getReference("classes");

        loadingView = findViewById(R.id.loadingView);
        nameTextView = findViewById(R.id.nameTextView);
        surnameTextView = findViewById(R.id.surnameTextView);

        loadingView.setVisibility(View.VISIBLE);

        findViewById(R.id.themesImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.accountImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AccountActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, TasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.classImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUser.child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        clas = String.valueOf(task.getResult().getValue());
                        if (clas.isEmpty()) {
                            Intent intent = new Intent(SettingsActivity.this, EmptyClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(SettingsActivity.this, ClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        databaseReferenceUser.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String name = task.getResult().getValue(String.class);
                nameTextView.setText("Ваше имя: " + name);
            }
        });

        databaseReferenceUser.child("surname").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String surname = task.getResult().getValue(String.class);
                surnameTextView.setText("Ваша фамилия: " + surname);
                loadingView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.editImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename, null);
                EditText nameBox = dialogView.findViewById(R.id.nameBox);
                EditText surnameBox = dialogView.findViewById(R.id.surnameBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = nameBox.getText().toString();
                        String surname = surnameBox.getText().toString();
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(surname)) {
                            Toast.makeText(SettingsActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        nameTextView.setText("Ваше имя: " + name);
                        surnameTextView.setText("Ваша фамилия: " + surname);
                        databaseReferenceUser.child("name").setValue(name);
                        databaseReferenceUser.child("surname").setValue(surname);
                        databaseReferenceUser.child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String clas = task.getResult().getValue(String.class);
                                if (!Objects.equals(clas, "")) {
                                    databaseReferenceClasses.child(clas).child("students").child(user_id).child("name").setValue(surname + " " + name);
                                }
                            }
                        });
                        dialog.dismiss();
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

        findViewById(R.id.signOutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingView.setVisibility(View.VISIBLE);
                mAuth.signOut();
                Toast.makeText(SettingsActivity.this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_deleteaccount, null);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingView.setVisibility(View.VISIBLE);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            databaseReferenceUser.child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        String clas = task.getResult().getValue(String.class);
                                        if (clas != null && !clas.isEmpty()) {
                                            databaseReferenceClasses.child(clas).child("students").child(user_id).removeValue();
                                        }

                                        databaseReferenceUser.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            loadingView.setVisibility(View.GONE);
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(SettingsActivity.this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(SettingsActivity.this, "Не удалось удалить аккаунт", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            loadingView.setVisibility(View.GONE);
                            Toast.makeText(SettingsActivity.this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
                        }
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


        findViewById(R.id.quitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SettingsActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
