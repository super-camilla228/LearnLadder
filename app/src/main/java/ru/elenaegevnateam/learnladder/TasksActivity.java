package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TasksActivity extends AppCompatActivity {
    private String user_id;
    private String clas;
    private ArrayList<Test> tests = new ArrayList<Test>();
    private ArrayList<String> tests_indexes = new ArrayList<String>();
    private ScrollView tasksScrollView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);
        tasksScrollView = findViewById(R.id.tasksScrollView);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceUser = database.getReference("users").child(user_id);

        findViewById(R.id.classImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUser.child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        clas = String.valueOf(task.getResult().getValue());
                        if (clas.isEmpty()) {
                            Intent intent = new Intent(TasksActivity.this, EmptyClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(TasksActivity.this, ClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, SettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.themesImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.accountImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, AccountActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        databaseReferenceUser.child("tasks").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    tests.add(lastDataSnapshot.getValue(Test.class));
                    tests_indexes.add(lastDataSnapshot.getKey());
                }
                if (tests.isEmpty()) {
                    findViewById(R.id.emptyTasksTextView).setVisibility(View.VISIBLE);
                }
                else {
                    findViewById(R.id.emptyTasksTextView).setVisibility(View.GONE);
                    for (int i = 0; i < tests.size(); i++) {
                        addTask(tests.get(i).name, tests.get(i).theme, i);
                    }
                }
                loadingView.setVisibility(View.GONE);
            }
        });
    }

    public void addTask (String name, String theme, int i) {
        LinearLayout tasksLinearLayout = findViewById(R.id.tasksLinearLayout);
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardViewParams = new LinearLayout.LayoutParams(
                800,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardViewParams.setMargins(15, 40, 15, 15);
        cardViewParams.gravity = Gravity.CENTER;
        cardView.setLayoutParams(cardViewParams);
        cardView.setRadius(16);
        cardView.setCardElevation(20);

        LinearLayout horizontalLayout = new LinearLayout(this);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout verticalLayout = new LinearLayout(this);
        verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                700,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        verticalLayout.setOrientation(LinearLayout.VERTICAL);

        TextView titleTextView = new TextView(this);
        titleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        titleTextView.setText(name);
        titleTextView.setTextSize(20);
        titleTextView.setPadding(20, 10, 20, 10);
        titleTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));

        TextView themeTextView = new TextView(this);
        themeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        themeTextView.setText(theme + " мин");
        themeTextView.setTextSize(18);
        themeTextView.setPadding(30, 10, 20, 10);
        themeTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));

        verticalLayout.addView(titleTextView);
        verticalLayout.addView(themeTextView);

        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                75,
                75
        );
        imageViewParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(imageViewParams);
        imageView.setImageResource(R.drawable.forward_orange);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TasksActivity.this, TestActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", tests_indexes.get(i));
                intent.putExtra("format", "practice");
                intent.putExtra("time", tests.get(i).theme);
                startActivity(intent);
            }
        });

        horizontalLayout.addView(verticalLayout);
        horizontalLayout.addView(imageView);

        cardView.addView(horizontalLayout);

        tasksLinearLayout.addView(cardView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TasksActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
