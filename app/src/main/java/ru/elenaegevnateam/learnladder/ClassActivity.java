package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassActivity extends AppCompatActivity {
    private String user_id;
    private TextView classTextView;
    private TextView teacherTextView;
    private LinearLayout linearLayoutClassmates;
    private ScrollView scrollViewClassmates;
    private FrameLayout loadingView;
    private ArrayList<Student> classmates = new ArrayList<Student>();
    DatabaseReference databaseReferenceUserClass;
    DatabaseReference databaseReferenceClasses;
    DatabaseReference databaseReferenceUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUserClass = database.getReference("users").child(user_id).child("class");
        databaseReferenceClasses = database.getReference("classes");
        databaseReferenceUsers = database.getReference("users");

        classTextView = findViewById(R.id.classTextView);
        teacherTextView = findViewById(R.id.teacherTextView);
        linearLayoutClassmates = findViewById(R.id.linearLayoutClassmates);
        scrollViewClassmates = findViewById(R.id.scrollViewClassmates);

        findViewById(R.id.themesImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClassActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.accountImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClassActivity.this, AccountActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClassActivity.this, SettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClassActivity.this, TasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        databaseReferenceUserClass.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String clas = String.valueOf(task.getResult().getValue());
                classTextView.setText("Твой класс: " + clas);

                databaseReferenceClasses.child(clas).child("teacher").child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        String teacher = task.getResult().getValue(String.class);
                        if (teacher != null) {
                            teacherTextView.setText("Твой учитель: " + teacher);
                        } else {
                            teacherTextView.setText("Учитель пока не прикрепился\nк классу");
                        }
                    }
                });

                databaseReferenceClasses.child(clas).child("students").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final int totalChildren = (int) snapshot.getChildrenCount();
                        final AtomicInteger counter = new AtomicInteger(0);

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String id = ds.getKey();
                            databaseReferenceUsers.child(id).child("coins").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    String coins = String.valueOf(task.getResult().getValue());
                                    Integer coins_int = Integer.parseInt(coins);
                                    databaseReferenceClasses.child(clas).child("students").child(id).child("coins").setValue(coins_int).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (counter.incrementAndGet() == totalChildren) {
                                                loadClassmates(clas);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("firebase", "Ошибка чтения данных", error.toException());
                    }
                });
            }
        });

        findViewById(R.id.removeClassButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClassActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_removeclass, null);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        databaseReferenceUserClass.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String clas = task.getResult().getValue(String.class);
                                databaseReferenceClasses.child(clas).child("students").child(user_id).removeValue();
                                databaseReferenceUserClass.setValue("");
                            }
                        });
                        dialog.dismiss();
                        Intent intent = new Intent(ClassActivity.this, EmptyClassActivity.class);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
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

    private void loadClassmates(String clas) {
        databaseReferenceClasses.child(clas).child("students").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    classmates.add(lastDataSnapshot.getValue(Student.class));
                }
                Collections.sort(classmates);
                for (int i = 0; i < classmates.size(); i++) {
                    addStudent(i);
                }
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    private void addStudent(int i){
        LinearLayout studentLayout = new LinearLayout(this);
        studentLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (Objects.equals(user_id, classmates.get(i).getId())) {
            studentLayout.setBackgroundColor(getResources().getColor(R.color.light_orange));
        }
        studentLayout.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParamsStudent = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsStudent.setMargins(30, 0, 30, 0);
        layoutParamsStudent.gravity = Gravity.CENTER;
        TextView placeTextView = new TextView(this);
        placeTextView.setText(String.valueOf(i+1));
        placeTextView.setTextSize(20);
        placeTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        placeTextView.setLayoutParams(layoutParamsStudent);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(classmates.get(i).getName());
        LinearLayout.LayoutParams layoutParamsName = new LinearLayout.LayoutParams(550, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsName.setMargins(30, 0, 30, 0);
        layoutParamsName.gravity = Gravity.CENTER;
        nameTextView.setTextSize(20);
        nameTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        nameTextView.setLayoutParams(layoutParamsName);

        LinearLayout iconCoinsLayout = new LinearLayout(this);
        iconCoinsLayout.setOrientation(LinearLayout.HORIZONTAL);
        iconCoinsLayout.setLayoutParams(layoutParamsStudent);

        ImageView iconImageView = new ImageView(this);
        iconImageView.setImageResource(R.drawable.coin);
        LinearLayout.LayoutParams layoutParamsCoin = new LinearLayout.LayoutParams(60, 60);
        layoutParamsCoin.gravity = Gravity.CENTER;
        iconImageView.setLayoutParams(layoutParamsCoin);

        TextView coinsTextView = new TextView(this);
        coinsTextView.setText(String.valueOf(classmates.get(i).getCoins()));
        coinsTextView.setTextSize(15);
        coinsTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        coinsTextView.setLayoutParams(layoutParamsStudent);

        iconCoinsLayout.addView(coinsTextView);
        iconCoinsLayout.addView(iconImageView);

        studentLayout.addView(placeTextView);
        studentLayout.addView(nameTextView);
        studentLayout.addView(iconCoinsLayout);

        linearLayoutClassmates.addView(studentLayout);
        scrollViewClassmates.removeAllViews();
        scrollViewClassmates.addView(linearLayoutClassmates);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ClassActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
