package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private TextView infoTextView;
    private TextView counterTextView;
    private ArrayList<Card> cards = new ArrayList<Card>();
    private ArrayList<Card> unknown_cards = new ArrayList<Card>();
    private ArrayList<String> cards_indexes = new ArrayList<String>();
    private int currentSelectedCard = 0;
    private int unknownCardsCount = 0;
    private boolean isBigLetter = false;
    private DatabaseReference databaseReferenceKnownCards;
    private DatabaseReference databaseReferenceUnknownCards;
    private DatabaseReference databaseReferenceCards;
    private String user_id;
    private String theme;
    private String format;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        theme = intent.getStringExtra("theme");
        format = intent.getStringExtra("format");

        textView = findViewById(R.id.textView);
        infoTextView = findViewById(R.id.infoText);
        counterTextView = findViewById(R.id.counterTextView);

        updateText();

        CardView cardView = findViewById(R.id.cardView);
        ImageView knowButton = findViewById(R.id.buttonKnow);
        ImageView dontknowButton = findViewById(R.id.buttonDontKnow);
        ImageView infoIcon = findViewById(R.id.infoIcon);
        LinearLayout infoLayout = findViewById(R.id.infoLayout);
        ImageView cardsBackButton = findViewById(R.id.cardsBackButton);
        ImageView shuffleButton = findViewById(R.id.shuffleButton);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceKnownCards = database.getReference("users").child(user_id).child(theme).child("known_cards");
        databaseReferenceCards = database.getReference(theme);
        databaseReferenceUnknownCards = database.getReference("users").child(user_id).child(theme).child("unknown_cards");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        databaseReferenceCards.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                    Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                    for (DataSnapshot lastDataSnapshot : iterable) {
                        cards.add(lastDataSnapshot.getValue(Card.class));
                    }
                }
            }
        });

        databaseReferenceUnknownCards.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                    if (unknown_cards.isEmpty()) {
                        databaseReferenceCards.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e("firebase", "Error getting data", task.getException());
                                } else {
                                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                                    Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                                    for (DataSnapshot lastDataSnapshot : iterable) {
                                        unknown_cards.add(lastDataSnapshot.getValue(Card.class));
                                        cards_indexes.add(lastDataSnapshot.getKey());
                                    }
                                    for (int i = 0; i < unknown_cards.size(); i++) {
                                        databaseReferenceUnknownCards.child(cards_indexes.get(i)).child("normal").setValue(unknown_cards.get(i).normal);
                                        databaseReferenceUnknownCards.child(cards_indexes.get(i)).child("oneBigLetter").setValue(unknown_cards.get(i).oneBigLetter);
                                        databaseReferenceUnknownCards.child(cards_indexes.get(i)).child("info").setValue(unknown_cards.get(i).info);
                                        databaseReferenceKnownCards.removeValue();
                                    }
                                    isDataLoaded = true;
                                    updateText();
                                    loadingView.setVisibility(View.GONE);
                                }
                            }
                        });

                    }
                    else {
                        isDataLoaded = true;
                        updateText();
                        loadingView.setVisibility(View.GONE);
                    }
                }
            }
        });

        unknownCardsCount = unknown_cards.size();

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffleButton.setImageDrawable(getResources().getDrawable(R.drawable.shuffle_on));
                Collections.shuffle(unknown_cards);
                infoLayout.setVisibility(View.GONE);
                updateText();
            }
        });

        cardsBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                if (currentSelectedCard == 0) {
                Intent intent = new Intent(MainActivity.this, FormatActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", theme);
                startActivity(intent);
            }

                else {
                    currentSelectedCard--;
                    infoLayout.setVisibility(View.INVISIBLE);
                    isBigLetter = false;
                    databaseReferenceKnownCards.child(cards_indexes.get(currentSelectedCard)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                databaseReferenceKnownCards.child(cards_indexes.get(currentSelectedCard)).removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    databaseReferenceCards.child(cards_indexes.get(currentSelectedCard)).child("normal").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String normal = snapshot.getValue(String.class);
                            databaseReferenceUnknownCards.child(cards_indexes.get(currentSelectedCard)).child("normal").setValue(normal);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    databaseReferenceCards.child(cards_indexes.get(currentSelectedCard)).child("oneBigLetter").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String normal = snapshot.getValue(String.class);
                            databaseReferenceUnknownCards.child(cards_indexes.get(currentSelectedCard)).child("oneBigLetter").setValue(normal);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    databaseReferenceCards.child(cards_indexes.get(currentSelectedCard)).child("info").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String normal = snapshot.getValue(String.class);
                            databaseReferenceUnknownCards.child(cards_indexes.get(currentSelectedCard)).child("info").setValue(normal);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    updateText();
                }
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBigLetter = !isBigLetter;
                updateText();
            }
        });

        knowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceKnownCards.child(cards_indexes.get(currentSelectedCard)).setValue("");
                databaseReferenceUnknownCards.child(cards_indexes.get(currentSelectedCard)).removeValue();
                currentSelectedCard++;
                infoLayout.setVisibility(View.INVISIBLE);
                isBigLetter = false;
                updateText();
            }
        });

        dontknowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelectedCard++;
                unknownCardsCount++;
                infoLayout.setVisibility(View.INVISIBLE);
                isBigLetter = false;
                updateText();
            }
        });

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoLayout.getVisibility() == View.VISIBLE) {
                    infoLayout.setVisibility(View.GONE);
                } else {
                    infoLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void updateText() {
        if(!isDataLoaded)
            return;

        if (currentSelectedCard < unknown_cards.size()) {
            if (isBigLetter)
                textView.setText(unknown_cards.get(currentSelectedCard).oneBigLetter);
            else {
                textView.setText(unknown_cards.get(currentSelectedCard).normal);
                infoTextView.setText(unknown_cards.get(currentSelectedCard).info);
            }

            String counterText = (currentSelectedCard + 1) + "/" + (unknown_cards.size());
            counterTextView.setText(counterText);
        }
        else {
            Intent intent = new Intent(MainActivity.this, AllCardsViewedActivity.class);
            double progress = ((double)(cards.size() - unknownCardsCount) / cards.size()) * 100;
            int progress_text = (int) progress;
            intent.putExtra("progress", String.valueOf(progress_text));
            intent.putExtra("user_id", user_id);
            intent.putExtra("theme", theme);
            intent.putExtra("format", format);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this, FormatActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("theme", theme);
        startActivity(intent);
    }
}
