package ru.elenaegevnateam.learnladder;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.Objects;

public class TeacherSettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private View loadingView;
    private String user_id;
    private ArrayList<String> classes = new ArrayList<String>();
    private TextView nameTextView;
    private TextView surnameTextView;
    private DatabaseReference databaseReferenceUser;
    private DatabaseReference databaseReferenceClasses;
    private DatabaseReference databaseReferenceTasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachersettings);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUser = database.getReference("users").child(user_id);
        databaseReferenceClasses = database.getReference("classes");
        databaseReferenceTasks = database.getReference("tasks");

        loadingView = findViewById(R.id.loadingView);
        nameTextView = findViewById(R.id.nameTextView);
        surnameTextView = findViewById(R.id.surnameTextView);

        loadingView.setVisibility(View.VISIBLE);

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherSettingsActivity.this, TeacherTasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.classImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUser.child("classes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                        for (DataSnapshot lastDataSnapshot : iterable) {
                            classes.add(lastDataSnapshot.getKey());
                        }
                        Intent intent;
                        if (classes.isEmpty()) {
                            intent = new Intent(TeacherSettingsActivity.this, TeacherEmptyClassActivity.class);
                        }
                        else {
                            intent = new Intent(TeacherSettingsActivity.this, TeacherClassesActivity.class);
                        }
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherSettingsActivity.this);
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
                            Toast.makeText(TeacherSettingsActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        nameTextView.setText("Ваше имя: " + name);
                        surnameTextView.setText("Ваша фамилия: " + surname);
                        databaseReferenceUser.child("name").setValue(name);
                        databaseReferenceUser.child("surname").setValue(surname);
                        ArrayList<String> classes = new ArrayList<>();
                        databaseReferenceUser.child("classes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                                for (DataSnapshot lastDataSnapshot : iterable) {
                                    classes.add(lastDataSnapshot.getKey());
                                }
                                if (!classes.isEmpty()) {
                                    for (String clas: classes) {
                                        databaseReferenceClasses.child(clas).child("teacher").child("name").setValue(surname + " " + name);
                                    }
                                }
                            }
                        });
                        ArrayList<CatalogTest> tests = new ArrayList<>();
                        ArrayList<String> tests_indexes = new ArrayList<>();
                        databaseReferenceTasks.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                                for (DataSnapshot lastDataSnapshot : iterable) {
                                    tests.add(lastDataSnapshot.getValue(CatalogTest.class));
                                    tests_indexes.add(lastDataSnapshot.getKey());
                                }
                                for (int i=0; i<tests.size(); i++) {
                                    if (Objects.equals(tests.get(i).author_id, user_id)) {
                                        databaseReferenceTasks.child(tests_indexes.get(i)).child("author").setValue(surname + " " + name);
                                    }
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
                Toast.makeText(TeacherSettingsActivity.this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TeacherSettingsActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherSettingsActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_deleteaccount, null);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadingView.setVisibility(View.VISIBLE);
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            databaseReferenceUser.child("classes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        ArrayList<String> classes = new ArrayList<>();
                                        Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                                        for (DataSnapshot lastDataSnapshot : iterable) {
                                            classes.add(lastDataSnapshot.getKey());
                                        }
                                        if (!classes.isEmpty()) {
                                            for (String clas : classes) {
                                                databaseReferenceClasses.child(clas).child("teacher").removeValue();
                                            }
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
                                                                Toast.makeText(TeacherSettingsActivity.this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(TeacherSettingsActivity.this, SignInActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(TeacherSettingsActivity.this, "Не удалось удалить аккаунт", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(TeacherSettingsActivity.this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
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

        findViewById(R.id.telegramLinearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ElenaEGEvna_bot"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TeacherSettingsActivity.this, TeacherTasksActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}