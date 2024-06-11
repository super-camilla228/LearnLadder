package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class AllCardsViewedActivity extends AppCompatActivity {

    private TextView progressTextView;
    private TextView textView;
    private String user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allcardsviewed);

        ImageView yesButton = findViewById(R.id.yesButton);
        ImageView noButton = findViewById(R.id.noButton);
        ImageView backButton = findViewById(R.id.backButton);
        progressTextView = findViewById(R.id.progressTextView);
        textView = findViewById(R.id.textView);

        backButton.setVisibility(View.GONE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceUsers = database.getReference("users");
        DatabaseReference databaseReferenceClasses = database.getReference("classes");

        ArrayList<String> base_themes = new ArrayList<>();
        base_themes.add("stresses");
        base_themes.add("words");
        base_themes.add("task_26");
        base_themes.add("roots");
        base_themes.add("prefixes");
        base_themes.add("verbs");

        Intent intent = getIntent();
        String progress = intent.getStringExtra("progress");
        String theme = intent.getStringExtra("theme");
        user_id = intent.getStringExtra("user_id");
        String format = intent.getStringExtra("format");

        if (!base_themes.contains(theme)) {
            noButton.setVisibility(View.GONE);
            yesButton.setVisibility(View.GONE);
            backButton.setVisibility(View.VISIBLE);
            textView.setText("правильных ответов.\nТвой результат уже направлен учителю!");
            progressTextView.setText(progress+"%");
            ArrayList<String> right_answers = new ArrayList<>();
            databaseReferenceUsers.child(user_id).child("tasks").child(theme).child("right_answers").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                    for (DataSnapshot lastDataSnapshot : iterable) {
                        right_answers.add(lastDataSnapshot.getKey());
                    }
                }
            });
            databaseReferenceUsers.child(user_id).child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    String clas = task.getResult().getValue(String.class);
                    databaseReferenceClasses.child(clas).child("teacher").child("id").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String teacher_id = task.getResult().getValue(String.class);
                            databaseReferenceUsers.child(teacher_id).child("tasks").child(theme).child("results").child(user_id).child("percents").setValue(progress);
                            databaseReferenceUsers.child(teacher_id).child("tasks").child(theme).child("results").child(user_id).child("right_answers").setValue(String.valueOf(right_answers.size()));
                            databaseReferenceUsers.child(teacher_id).child("tasks").child(theme).child("results").child(user_id).child("clas").setValue(clas);
                            databaseReferenceClasses.child(clas).child("students").child(user_id).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    String name = task.getResult().getValue(String.class);
                                    databaseReferenceUsers.child(teacher_id).child("tasks").child(theme).child("results").child(user_id).child("name").setValue(name);
                                }
                            });
                        }
                    });
                }
            });
            databaseReferenceUsers.child(user_id).child("tasks").child(theme).removeValue();

        }
        else {
            if (Objects.equals(format, "studying")) {
                findViewById(R.id.mainView).setBackgroundColor(getResources().getColor(R.color.light_blue));
            } else {
                findViewById(R.id.mainView).setBackgroundColor(getResources().getColor(R.color.light_orange));
            }

            if (Objects.equals(progress, " ")) {
                progressTextView.setText("");
                textView.setText("Вы просмотрели всю теорию.\nХотите начать сначала?");
            } else {
                progressTextView.setText(progress + "%");
                if (Objects.equals(progress, "100") && Objects.equals(format, "studying")) {
                    textView.setText("материала изучено.\nВы изучили все карточки по этой теме!\nХотите начать сначала?");
                } else if (!Objects.equals(progress, "100") && Objects.equals(format, "studying")) {
                    textView.setText("материала изучено.\nВы просмотрели все карточки.\nХотите продолжить повторение?");
                } else if (Objects.equals(progress, "100") && Objects.equals(format, "practice")) {
                    textView.setText("правильных ответов.\nВы достигли максимальной цели!\nХотите перепройти тест?");
                } else {
                    textView.setText("правильных ответов.\nХороший результат, но вы можете ещё лучше!\nХотите перепройти тест?");
                }
            }
        }

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(format, "studying") && (Objects.equals(theme, "stresses") || Objects.equals(theme, "words") || Objects.equals(theme, "task_26"))) {
                    Intent intent = new Intent(AllCardsViewedActivity.this, MainActivity.class);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("theme", theme);
                    intent.putExtra("format", format);
                    startActivity(intent);
                }
                else if (Objects.equals(format, "studying") && (Objects.equals(theme, "roots") || Objects.equals(theme, "prefixes") || Objects.equals(theme, "verbs"))) {
                    Intent intent = new Intent(AllCardsViewedActivity.this, TheoryActivity.class);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("theme", theme);
                    intent.putExtra("format", format);
                    startActivity(intent);
                }
                else {
                    Intent intent = new Intent(AllCardsViewedActivity.this, TestActivity.class);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("theme", theme);
                    intent.putExtra("format", format);
                    startActivity(intent);
                }
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllCardsViewedActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllCardsViewedActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AllCardsViewedActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
