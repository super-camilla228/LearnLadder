package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ThemesActivity extends AppCompatActivity {

    private String coins;
    private String clas;
    private TextView coinsTextView;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);

        coinsTextView = findViewById(R.id.coinsTextView);

        Intent intent = getIntent();
        String user_id = intent.getStringExtra("user_id");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        update();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceUsers = database.getReference("users").child(user_id);

        findViewById(R.id.accountImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, AccountActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, SettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, TasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.classImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUsers.child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        clas = String.valueOf(task.getResult().getValue());
                        if (clas.isEmpty()) {
                            Intent intent = new Intent(ThemesActivity.this, EmptyClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(ThemesActivity.this, ClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        FirebaseDatabase.getInstance().getReference("users").child(user_id).child("coins").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                coins = String.valueOf(task.getResult().getValue());
                isDataLoaded = true;
                update();
                loadingView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.stressesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", "stresses");
                startActivity(intent);
            }
        });

        findViewById(R.id.wordsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", "words");
                startActivity(intent);
            }
        });

        findViewById(R.id.tropsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", "task_26");
                startActivity(intent);
            }
        });

        findViewById(R.id.rootsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", "roots");
                startActivity(intent);
            }
        });

        findViewById(R.id.prefixesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", "prefixes");
                startActivity(intent);
            }
        });

        findViewById(R.id.verbsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", "verbs");
                startActivity(intent);
            }
        });
    }
    private void update() {
        if (!isDataLoaded)
            return;
        coinsTextView.setText(coins);
    }

    @Override
    public void onBackPressed() {
    }
}
