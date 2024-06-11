package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TheoryActivity extends AppCompatActivity {

    private TextView mainTextView;
    private TextView titleTextView;
    private TextView infoTextView;
    private TextView counterTextView;
    private ArrayList<Block> blocks = new ArrayList<Block>();
    private ArrayList<String> blocks_indexes = new ArrayList<String>();
    private int currentBlock = 0;
    private DatabaseReference databaseReferenceBlocks;
    private String user_id;
    private String theme;
    private String format;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theory);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        theme = intent.getStringExtra("theme");
        format = intent.getStringExtra("format");

        mainTextView = findViewById(R.id.mainTextView);
        infoTextView = findViewById(R.id.infoTextView);
        titleTextView = findViewById(R.id.titleTextView);
        counterTextView = findViewById(R.id.counterTextView);

        ImageView backButton = findViewById(R.id.backButton);
        ImageView forwardButton = findViewById(R.id.forwardButton);
        ImageView infoIcon = findViewById(R.id.imageView);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceBlocks = database.getReference(theme);

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        databaseReferenceBlocks.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                    Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                    for (DataSnapshot lastDataSnapshot : iterable) {
                        blocks.add(lastDataSnapshot.getValue(Block.class));
                        blocks_indexes.add(lastDataSnapshot.getKey());
                    }
                    isDataLoaded = true;
                    update();
                    loadingView.setVisibility(View.GONE);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentBlock == 0) {
                    Intent intent = new Intent(TheoryActivity.this, FormatActivity.class);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("theme", theme);
                    startActivity(intent);
                }
                currentBlock--;
                update();
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentBlock++;
                update();
            }
        });
    }
    private void update() {
        if(!isDataLoaded)
            return;

        if (currentBlock < blocks.size()) {
            titleTextView.setText(blocks.get(currentBlock).title);
            mainTextView.setText(blocks.get(currentBlock).main);
            infoTextView.setText(blocks.get(currentBlock).info);
            String counterText = (currentBlock + 1) + "/" + (blocks.size());
            counterTextView.setText(counterText);
        }
        else {
            Intent intent = new Intent(TheoryActivity.this, AllCardsViewedActivity.class);
            intent.putExtra("user_id", user_id);
            intent.putExtra("theme", theme);
            intent.putExtra("progress", " ");
            intent.putExtra("format", format);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TheoryActivity.this, FormatActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("theme", theme);
        startActivity(intent);
    }
}
