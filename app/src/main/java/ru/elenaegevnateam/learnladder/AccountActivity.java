package ru.elenaegevnateam.learnladder;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {

    private TextView stressesSTextView;
    private TextView wordsSTextView;
    private TextView tropsSTextView;
    private TextView stressesPTextView;
    private TextView wordsPTextView;
    private TextView tropsPTextView;
    private TextView rootsPTextView;
    private TextView prefixesPTextView;
    private TextView verbsPTextView;
    private TextView coinsTextView;
    private int stresses_known_cards = 0;
    private int stresses_cards = 0;
    private int words_known_cards = 0;
    private int words_cards = 0;
    private int trops_known_cards = 0;
    private int trops_cards = 0;
    private int stresses_test = 0;
    private int words_test = 0;
    private int trops_test = 0;
    private int roots_test = 0;
    private int prefixes_test = 0;
    private int verbs_test = 0;
    private int stresses_right_answers = 0;
    private int words_right_answers = 0;
    private int trops_right_answers = 0;
    private int roots_right_answers = 0;
    private int prefixes_right_answers = 0;
    private int verbs_right_answers = 0;
    private boolean isDataLoaded = false;

    private String clas;
    private String coins;
    private String user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceUsers = database.getReference("users").child(user_id);

        update();

        findViewById(R.id.classImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUsers.child("class").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        clas = String.valueOf(task.getResult().getValue());
                        if (clas.isEmpty()) {
                            Intent intent = new Intent(AccountActivity.this, EmptyClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(AccountActivity.this, ClassActivity.class);
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
                Intent intent = new Intent(AccountActivity.this, SettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.themesImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, TasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });


        databaseReferenceUsers.child("stresses").child("known_cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stresses_known_cards = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("words").child("known_cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                words_known_cards = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("task_26").child("known_cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trops_known_cards = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("stresses_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stresses_right_answers = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("words_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                words_right_answers = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("task_26_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trops_right_answers = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("roots_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roots_right_answers = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("prefixes_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                prefixes_right_answers = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUsers.child("verbs_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                verbs_right_answers = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("words").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                words_cards = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("task_26").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trops_cards = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("stresses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stresses_cards = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("trops_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trops_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("words_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                words_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("roots_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roots_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("prefixes_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                prefixes_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("task_26_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trops_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("verbs_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                verbs_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("stresses_test").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                stresses_test = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        stressesSTextView = findViewById(R.id.stressesSTextView);
        wordsSTextView = findViewById(R.id.wordsSTextView);
        tropsSTextView = findViewById(R.id.tropsSTextView);
        stressesPTextView = findViewById(R.id.stressesPTextView);
        wordsPTextView = findViewById(R.id.wordsPTextView);
        tropsPTextView = findViewById(R.id.tropsPTextView);
        rootsPTextView = findViewById(R.id.rootsPTextView);
        prefixesPTextView = findViewById(R.id.prefixesPTextView);
        verbsPTextView = findViewById(R.id.verbsPTextView);
        coinsTextView = findViewById(R.id.coinsTextView);
        TextView achievementsTextView = findViewById(R.id.achievementsTextView);

        findViewById(R.id.themesImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        achievementsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this, AchievementsActivity.class);
                intent.putExtra("stresses_cards", String.valueOf(stresses_cards-stresses_known_cards));
                intent.putExtra("words_cards", String.valueOf(words_cards-words_known_cards));
                intent.putExtra("trops_cards", String.valueOf(trops_cards-trops_known_cards));
                intent.putExtra("stresses_test", String.valueOf(stresses_test-stresses_right_answers));
                intent.putExtra("words_test", String.valueOf(words_test-words_right_answers));
                intent.putExtra("trops_test", String.valueOf(trops_test-trops_right_answers));
                intent.putExtra("roots_test", String.valueOf(roots_test-roots_right_answers));
                intent.putExtra("prefixes_test", String.valueOf(prefixes_test-prefixes_right_answers));
                intent.putExtra("verbs_test", String.valueOf(verbs_test-verbs_right_answers));
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void update() {
        if (!isDataLoaded)
            return;
        stressesSTextView.setText("Ударения: " + stresses_known_cards + "/" + stresses_cards);
        wordsSTextView.setText("Словарные слова: " + words_known_cards + "/" + words_cards);
        tropsSTextView.setText("Средства выразительности: " + trops_known_cards + "/" + trops_cards);
        stressesPTextView.setText("Ударения: " + stresses_right_answers + "/" + stresses_test);
        wordsPTextView.setText("Словарные слова: " + words_right_answers + "/" + words_test);
        tropsPTextView.setText("Средства выразительности: " + trops_right_answers + "/" + trops_test);
        rootsPTextView.setText("Корни: " + roots_right_answers + "/" + roots_test);
        prefixesPTextView.setText("Приставки: " + prefixes_right_answers + "/" + prefixes_test);
        verbsPTextView.setText("Правописание глаголов и причастий: " + verbs_right_answers + "/" + verbs_test);
        coinsTextView.setText(coins);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AccountActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}