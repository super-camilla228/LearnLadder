package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {
    String user_id;
    String test;
    ArrayList<Result> results = new ArrayList<>();
    String questions;
    LinearLayout resultsLinearLayout;
    ScrollView resultsScrollView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        String test_name = intent.getStringExtra("name");
        String theme = intent.getStringExtra("theme");
        user_id = intent.getStringExtra("user_id");
        test = intent.getStringExtra("test");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView themeTextView = findViewById(R.id.themeTextView);
        nameTextView.setText(test_name);
        themeTextView.setText("Время: " + theme + " мин");
        resultsLinearLayout = findViewById(R.id.resultsLinearLayout);
        resultsScrollView = findViewById(R.id.resultscrollView);

        FirebaseDatabase.getInstance().getReference("users").child(user_id).child("tasks").child(test).child("questions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                questions = String.valueOf(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference("users").child(user_id).child("tasks").child(test).child("results").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    results.add(lastDataSnapshot.getValue(Result.class));
                }
                if (results.isEmpty()) {
                    findViewById(R.id.noResultsTextView).setVisibility(View.VISIBLE);
                }
                for (Result result: results) {
                    addResult(result.name, result.clas, result.right_answers + "/" + questions, result.percents);
                }
                loadingView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultsActivity.this, TeacherTasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });
    }

    private void addResult(String name, String clas, String result, String percents) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                830,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(15, 40, 15, 15);
        cardLayoutParams.gravity = Gravity.CENTER;
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setRadius(16);
        cardView.setCardElevation(20);

        LinearLayout cardLinearLayout = new LinearLayout(this);
        cardLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cardLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout textLinearLayout = new LinearLayout(this);
        textLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                700,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textLinearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView nameTextView = new TextView(this);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        nameTextView.setText(name);
        nameTextView.setTextSize(20);
        nameTextView.setPadding(30, 10, 20, 10);
        nameTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        textLinearLayout.addView(nameTextView);

        TextView clasTextView = new TextView(this);
        clasTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        clasTextView.setText("Класс: " + clas);
        clasTextView.setTextSize(20);
        clasTextView.setPadding(30, 10, 20, 10);
        clasTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        textLinearLayout.addView(clasTextView);

        TextView resultTextView = new TextView(this);
        resultTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        resultTextView.setText("Результат: " + result + ", " + percents + "%");
        resultTextView.setTextSize(20);
        resultTextView.setPadding(30, 10, 20, 10);
        resultTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        textLinearLayout.addView(resultTextView);

        cardLinearLayout.addView(textLinearLayout);

        cardView.addView(cardLinearLayout);

        resultsLinearLayout.addView(cardView, 0);

        resultsScrollView.removeAllViews();
        resultsScrollView.addView(resultsLinearLayout);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultsActivity.this, TeacherTasksActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
