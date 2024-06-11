package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class FormatActivity extends AppCompatActivity {
    private String user_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        String theme = intent.getStringExtra("theme");

        findViewById(R.id.studyingTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (Objects.equals(theme, "words") || Objects.equals(theme, "stresses") || Objects.equals(theme, "task_26")) {
                    intent = new Intent(FormatActivity.this, MainActivity.class);
                }
                else {
                    intent = new Intent(FormatActivity.this, TheoryActivity.class);
                }
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", theme);
                intent.putExtra("format", "studying");
                startActivity(intent);
            }
        });

        findViewById(R.id.practiceTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FormatActivity.this, TestActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", theme);
                intent.putExtra("format", "practice");
                intent.putExtra("time", "5");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FormatActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
