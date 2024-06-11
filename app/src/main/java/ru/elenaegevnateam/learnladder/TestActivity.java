package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TestActivity extends AppCompatActivity {

    TextView textView;
    TextView counterTextView;
    TextView coinsTextView;
    TextView timerTextView;
    private int currentQuestion = 0;
    private int rightAnswers = 0;
    private ArrayList<Question> questions = new ArrayList<Question>();
    private ArrayList<String> questions_indexes = new ArrayList<String>();
    private String user_id;
    private String theme;
    private String format;
    private String coins;
    private String tiime;
    private Integer coins_updated;
    private DatabaseReference databaseReferenceQuestions;
    private DatabaseReference databaseReferenceRightAnswers;
    private DatabaseReference databaseReferenceUserCoins;
    private boolean isDataLoaded = false;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        theme = intent.getStringExtra("theme");
        format = intent.getStringExtra("format");
        tiime = intent.getStringExtra("time");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        ArrayList<String> base_themes = new ArrayList<>();
        base_themes.add("stresses");
        base_themes.add("words");
        base_themes.add("task_26");
        base_themes.add("roots");
        base_themes.add("prefixes");
        base_themes.add("verbs");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (base_themes.contains(theme)) {
            databaseReferenceQuestions = database.getReference(theme + "_test");
            databaseReferenceRightAnswers = database.getReference("users").child(user_id).child(theme + "_test");
        }
        else {
            databaseReferenceQuestions = database.getReference("users").child(user_id).child("tasks").child(theme).child("questions");
            databaseReferenceRightAnswers = database.getReference("users").child(user_id).child("tasks").child(theme).child("right_answers");
        }
        databaseReferenceUserCoins = database.getReference("users").child(user_id).child("coins");

        databaseReferenceUserCoins.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                coins = String.valueOf(task.getResult().getValue());
                coins_updated = Integer.parseInt(coins);
                update();
            }
        });

        databaseReferenceQuestions.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));

                    Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                    for (DataSnapshot lastDataSnapshot : iterable) {
                        questions.add(lastDataSnapshot.getValue(Question.class));
                        questions_indexes.add(lastDataSnapshot.getKey());
                    }
                    isDataLoaded = true;
                    update();
                    loadingView.setVisibility(View.GONE);
                }
            }
        });

        Collections.shuffle(questions);

        update();

        textView = findViewById(R.id.textView);
        counterTextView = findViewById(R.id.counterTextView);
        coinsTextView = findViewById(R.id.coinsTextView);
        timerTextView = findViewById(R.id.timerTextView);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        long timerDuration = TimeUnit.MINUTES.toMillis(Integer.parseInt(tiime));
        long ticksInterval = 10;

        countDownTimer = new CountDownTimer(timerDuration, ticksInterval){
            long millis = 1000;
            @Override
            public void onTick(long millisUntilFinished) {
                millis = millis - ticksInterval;
                if (millis == 0) {
                    millis = 1000;
                }

                String timerText = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                );

                timerTextView.setText(timerText);
            }

            @Override
            public void onFinish() {
                Toast.makeText(TestActivity.this, "Время вышло!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TestActivity.this, AllCardsViewedActivity.class);
                double progress = ((double)(rightAnswers) / questions.size()) * 100;
                int progress_text = (int) progress;
                intent.putExtra("progress", String.valueOf(progress_text));
                intent.putExtra("user_id", user_id);
                intent.putExtra("theme", theme);
                intent.putExtra("format", format);
                startActivity(intent);
            }
        }.start();


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(questions.get(currentQuestion).ans1, questions.get(currentQuestion).answer)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Правильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button1.setBackgroundResource(R.drawable.button_green);
                    databaseReferenceRightAnswers.child(questions_indexes.get(currentQuestion)).setValue("");
                    databaseReferenceUserCoins.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            coins = String.valueOf(task.getResult().getValue());
                            coins_updated = Integer.parseInt(coins) + 1;
                            databaseReferenceUserCoins.setValue(coins_updated);
                            update();
                        }
                    });
                    rightAnswers++;
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Неправильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button1.setBackgroundResource(R.drawable.button_red);
                    if (Objects.equals(questions.get(currentQuestion).ans2, questions.get(currentQuestion).answer)) {
                        button2.setBackgroundResource(R.drawable.button_green);
                    }
                    else if (Objects.equals(questions.get(currentQuestion).ans3, questions.get(currentQuestion).answer)) {
                        button3.setBackgroundResource(R.drawable.button_green);
                    }
                    else {
                        button4.setBackgroundResource(R.drawable.button_green);
                    }
                }
                TimerTask task = new TimerTask() {
                    public void run() {
                        currentQuestion++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();
                            }
                        });
                    }
                };

                Timer timer = new Timer();

                long delay = 1000L;
                timer.schedule(task, delay);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(questions.get(currentQuestion).ans2, questions.get(currentQuestion).answer)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Правильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button2.setBackgroundResource(R.drawable.button_green);
                    databaseReferenceRightAnswers.child(questions_indexes.get(currentQuestion)).setValue("");
                    databaseReferenceUserCoins.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            coins = String.valueOf(task.getResult().getValue());
                            coins_updated = Integer.parseInt(coins) + 1;
                            databaseReferenceUserCoins.setValue(coins_updated);
                            update();
                        }
                    });
                    rightAnswers++;
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Неправильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button2.setBackgroundResource(R.drawable.button_red);
                    if (Objects.equals(questions.get(currentQuestion).ans1, questions.get(currentQuestion).answer)) {
                        button1.setBackgroundResource(R.drawable.button_green);
                    }
                    else if (Objects.equals(questions.get(currentQuestion).ans3, questions.get(currentQuestion).answer)) {
                        button3.setBackgroundResource(R.drawable.button_green);
                    }
                    else {
                        button4.setBackgroundResource(R.drawable.button_green);
                    }
                }
                TimerTask task = new TimerTask() {
                    public void run() {
                        currentQuestion++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();
                            }
                        });
                    }
                };

                Timer timer = new Timer();

                long delay = 1000L;
                timer.schedule(task, delay);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(questions.get(currentQuestion).ans3, questions.get(currentQuestion).answer)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Правильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button3.setBackgroundResource(R.drawable.button_green);
                    databaseReferenceRightAnswers.child(questions_indexes.get(currentQuestion)).setValue("");
                    databaseReferenceUserCoins.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            coins = String.valueOf(task.getResult().getValue());
                            coins_updated = Integer.parseInt(coins) + 1;
                            databaseReferenceUserCoins.setValue(coins_updated);
                            update();
                        }
                    });
                    rightAnswers++;
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Неправильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button3.setBackgroundResource(R.drawable.button_red);
                    if (Objects.equals(questions.get(currentQuestion).ans2, questions.get(currentQuestion).answer)) {
                        button2.setBackgroundResource(R.drawable.button_green);
                    }
                    else if (Objects.equals(questions.get(currentQuestion).ans1, questions.get(currentQuestion).answer)) {
                        button1.setBackgroundResource(R.drawable.button_green);
                    }
                    else {
                        button4.setBackgroundResource(R.drawable.button_green);
                    }
                }
                TimerTask task = new TimerTask() {
                    public void run() {
                        currentQuestion++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();
                            }
                        });
                    }
                };

                Timer timer = new Timer();

                long delay = 1000L;
                timer.schedule(task, delay);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(questions.get(currentQuestion).ans4, questions.get(currentQuestion).answer)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Правильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button4.setBackgroundResource(R.drawable.button_green);
                    databaseReferenceRightAnswers.child(questions_indexes.get(currentQuestion)).setValue("");
                    databaseReferenceUserCoins.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            coins = String.valueOf(task.getResult().getValue());
                            coins_updated = Integer.parseInt(coins) + 1;
                            databaseReferenceUserCoins.setValue(coins_updated);
                            update();
                        }
                    });
                    rightAnswers++;
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Неправильно!", Toast.LENGTH_SHORT);
                    toast.show();
                    button4.setBackgroundResource(R.drawable.button_red);
                    if (Objects.equals(questions.get(currentQuestion).ans2, questions.get(currentQuestion).answer)) {
                        button2.setBackgroundResource(R.drawable.button_green);
                    }
                    else if (Objects.equals(questions.get(currentQuestion).ans3, questions.get(currentQuestion).answer)) {
                        button3.setBackgroundResource(R.drawable.button_green);
                    }
                    else {
                        button1.setBackgroundResource(R.drawable.button_green);
                    }
                }
                TimerTask task = new TimerTask() {
                    public void run() {
                        currentQuestion++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                update();
                            }
                        });
                    }
                };

                Timer timer = new Timer();

                long delay = 1000L;
                timer.schedule(task, delay);
            }
        });

    }
    private void update() {
        if (!isDataLoaded) {
            return;
        }
        if (currentQuestion < questions.size()) {
            button1.setText(questions.get(currentQuestion).ans1);
            button2.setText(questions.get(currentQuestion).ans2);
            button3.setText(questions.get(currentQuestion).ans3);
            button4.setText(questions.get(currentQuestion).ans4);
            String counterText = (currentQuestion + 1) + "/" + (questions.size());
            counterTextView.setText(counterText);
            button1.setBackgroundResource(R.drawable.button_orange);
            button2.setBackgroundResource(R.drawable.button_orange);
            button3.setBackgroundResource(R.drawable.button_orange);
            button4.setBackgroundResource(R.drawable.button_orange);
            if (theme.equals("stresses")) {
                textView.setText("Выберите слово, в котором правильно стоит ударение");
            }
            else if (theme.equals("words")) {
                textView.setText("Выберите слово, написание которого верно");
            }
            else if (theme.equals("roots") || theme.equals("prefixes") || theme.equals("verbs")) {
                textView.setText("Выберите слово, в котором пропущена буква, отличная от пропущенных букв во всех остальных словах.");
            }
            else if (theme.equals("task_26")) {
                textView.setText(questions.get(currentQuestion).q_word + " это?");
            }
            else {
                textView.setText(questions.get(currentQuestion).q_word);
            }
        }
        else {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            Intent intent = new Intent(TestActivity.this, AllCardsViewedActivity.class);
            double progress = ((double)(rightAnswers) / questions.size()) * 100;
            int progress_text = (int) progress;
            intent.putExtra("progress", String.valueOf(progress_text));
            intent.putExtra("user_id", user_id);
            intent.putExtra("theme", theme);
            intent.putExtra("format", format);
            startActivity(intent);
        }
        coinsTextView.setText(String.valueOf(coins_updated));
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Во время теста переход назад не возможен", Toast.LENGTH_SHORT).show();
    }
}
