package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.Objects;

public class AddQuestionActivity extends AppCompatActivity {
    private String user_id;
    private String test;
    private String name;
    private String theme;
    private String is_from_catalog;
    private int currentQuestion;
    private ArrayList<Question> questions = new ArrayList<Question>();
    private ArrayList<String> questions_indexes = new ArrayList<String>();
    private ScrollView questionScrollView;
    private LinearLayout questionsLinearLayout;
    private DatabaseReference databaseReferenceUser;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addquestions);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        test = intent.getStringExtra("test");
        name = intent.getStringExtra("name");
        theme = intent.getStringExtra("theme");
        is_from_catalog = intent.getStringExtra("is_from_catalog");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (Objects.equals(is_from_catalog, "1")) {
            findViewById(R.id.newQuestionButton).setVisibility(View.GONE);
            databaseReferenceUser = database.getReference("tasks").child(test).child("questions");
        } else{
            databaseReferenceUser = database.getReference("users").child(user_id).child("tasks").child(test).child("questions");
        }

        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView themeTextView = findViewById(R.id.themeTextView);
        nameTextView.setText(name);
        themeTextView.setText("Время: " + theme + " мин");
        
        questionScrollView = findViewById(R.id.questionScrollView);
        questionsLinearLayout = findViewById(R.id.questionsLinearLayout);

        databaseReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentQuestion = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceUser.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    questions.add(lastDataSnapshot.getValue(Question.class));
                    questions_indexes.add(lastDataSnapshot.getKey());
                }
                for (int i = 0; i < questions.size(); i++) {
                    if (Objects.equals(questions.get(i).isDeleted, "no")) {
                        int j = questions_indexes.indexOf("question"+(i+1));
                        Question question = questions.get(i);
                        addQuestion(question.q_word, question.ans1, question.ans2, question.ans3, question.ans4, question.answer, j+1);
                    }
                }
                loadingView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.newQuestionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddQuestionActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_newquestion, null);
                EditText questionBox = dialogView.findViewById(R.id.questionBox);
                EditText ans1Box = dialogView.findViewById(R.id.ans1Box);
                EditText ans2Box = dialogView.findViewById(R.id.ans2Box);
                EditText ans3Box = dialogView.findViewById(R.id.ans3Box);
                EditText ans4Box = dialogView.findViewById(R.id.ans4Box);
                EditText right_answerBox = dialogView.findViewById(R.id.rightAnswerBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String question = questionBox.getText().toString();
                        String ans1 = ans1Box.getText().toString();
                        String ans2 = ans2Box.getText().toString();
                        String ans3 = ans3Box.getText().toString();
                        String ans4 = ans4Box.getText().toString();
                        String right_answer = right_answerBox.getText().toString();
                        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(ans1) || TextUtils.isEmpty(ans2) || TextUtils.isEmpty(ans3) || TextUtils.isEmpty(ans4) || TextUtils.isEmpty(right_answer)) {
                            Toast.makeText(AddQuestionActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (ans1.equals(ans2) || ans1.equals(ans3) || ans1.equals(ans4) || ans2.equals(ans3) || ans2.equals(ans4) || ans3.equals(ans4)) {
                            Toast.makeText(AddQuestionActivity.this, "Возможен только 1 правильный вариант ответа!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!right_answer.equals(ans1) && !right_answer.equals(ans2) && !right_answer.equals(ans3) && !right_answer.equals(ans4)) {
                            Toast.makeText(AddQuestionActivity.this, "Ответ должен совпадать с одним из вариантов ответа!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int i = currentQuestion + 1;
                        databaseReferenceUser.child("question"+i).child("q_word").setValue(question);
                        databaseReferenceUser.child("question"+i).child("ans1").setValue(ans1);
                        databaseReferenceUser.child("question"+i).child("ans2").setValue(ans2);
                        databaseReferenceUser.child("question"+i).child("ans3").setValue(ans3);
                        databaseReferenceUser.child("question"+i).child("ans4").setValue(ans4);
                        databaseReferenceUser.child("question"+i).child("answer").setValue(right_answer);
                        databaseReferenceUser.child("question"+i).child("isDeleted").setValue("no");
                        addQuestion(question, ans1, ans2, ans3, ans4, right_answer, i);
                        dialog.dismiss();
                        currentQuestion++;
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

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.equals(is_from_catalog, "0")) {
                    Intent intent = new Intent(AddQuestionActivity.this, TeacherTasksActivity.class);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(AddQuestionActivity.this, TestsCatalogActivity.class);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);
                }
            }
        });
    }

    private void addQuestion(String question, String ans1, String ans2, String ans3, String ans4, String right_answer, int index) {
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
        cardLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        cardLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout textLinearLayout = new LinearLayout(this);
        textLinearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                700,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textLinearLayout.setLayoutParams(textLayoutParams);

        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textViewLayoutParams.setMargins(30, 10, 20, 10);

        TextView questionTextView = new TextView(this);
        questionTextView.setLayoutParams(textViewLayoutParams);
        questionTextView.setText(question);
        questionTextView.setTextSize(20);
        questionTextView.setTypeface(getResources().getFont(R.font.myfont));
        textLinearLayout.addView(questionTextView);

        TextView ans1TextView = new TextView(this);
        ans1TextView.setLayoutParams(textViewLayoutParams);
        ans1TextView.setText("1)" + ans1);
        ans1TextView.setTextSize(20);
        ans1TextView.setTypeface(getResources().getFont(R.font.myfont));
        textLinearLayout.addView(ans1TextView);

        TextView ans2TextView = new TextView(this);
        ans2TextView.setLayoutParams(textViewLayoutParams);
        ans2TextView.setText("2)" + ans2);
        ans2TextView.setTextSize(20);
        ans2TextView.setTypeface(getResources().getFont(R.font.myfont));
        textLinearLayout.addView(ans2TextView);

        TextView ans3TextView = new TextView(this);
        ans3TextView.setLayoutParams(textViewLayoutParams);
        ans3TextView.setText("3)" + ans3);
        ans3TextView.setTextSize(20);
        ans3TextView.setTypeface(getResources().getFont(R.font.myfont));
        textLinearLayout.addView(ans3TextView);

        TextView ans4TextView = new TextView(this);
        ans4TextView.setLayoutParams(textViewLayoutParams);
        ans4TextView.setText("4)" + ans4);
        ans4TextView.setTextSize(20);
        ans4TextView.setTypeface(getResources().getFont(R.font.myfont));
        textLinearLayout.addView(ans4TextView);

        TextView rightAnswerTextView = new TextView(this);
        rightAnswerTextView.setLayoutParams(textViewLayoutParams);
        rightAnswerTextView.setText("Ответ: " + right_answer);
        rightAnswerTextView.setTextSize(20);
        rightAnswerTextView.setTypeface(getResources().getFont(R.font.myfont));
        textLinearLayout.addView(rightAnswerTextView);

        LinearLayout imagesLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams imagesLayoutParams = new LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.WRAP_CONTENT);
        imagesLayoutParams.gravity = Gravity.CENTER;
        imagesLinearLayout.setOrientation(LinearLayout.VERTICAL);
        imagesLinearLayout.setLayoutParams(imagesLayoutParams);

        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(75, 75);
        imageLayoutParams.gravity = Gravity.CENTER;
        imageLayoutParams.setMargins(0, 40,0,0);

        ImageView renameImageView = new ImageView(this);
        renameImageView.setLayoutParams(imageLayoutParams);
        renameImageView.setImageResource(R.drawable.pencil_colored);
        renameImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddQuestionActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_renamequestion, null);
                EditText questionBox = dialogView.findViewById(R.id.questionBox);
                EditText ans1Box = dialogView.findViewById(R.id.ans1Box);
                EditText ans2Box = dialogView.findViewById(R.id.ans2Box);
                EditText ans3Box = dialogView.findViewById(R.id.ans3Box);
                EditText ans4Box = dialogView.findViewById(R.id.ans4Box);
                EditText right_answerBox = dialogView.findViewById(R.id.rightAnswerBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String question = questionBox.getText().toString();
                        String ans1 = ans1Box.getText().toString();
                        String ans2 = ans2Box.getText().toString();
                        String ans3 = ans3Box.getText().toString();
                        String ans4 = ans4Box.getText().toString();
                        String right_answer = right_answerBox.getText().toString();
                        if (TextUtils.isEmpty(question) || TextUtils.isEmpty(ans1) || TextUtils.isEmpty(ans2) || TextUtils.isEmpty(ans3) || TextUtils.isEmpty(ans4) || TextUtils.isEmpty(right_answer)) {
                            Toast.makeText(AddQuestionActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (ans1.equals(ans2) || ans1.equals(ans3) || ans1.equals(ans4) || ans2.equals(ans3) || ans2.equals(ans4) || ans3.equals(ans4)) {
                            Toast.makeText(AddQuestionActivity.this, "Возможен только 1 правильный вариант ответа!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!right_answer.equals(ans1) && !right_answer.equals(ans2) && !right_answer.equals(ans3) && !right_answer.equals(ans4)) {
                            Toast.makeText(AddQuestionActivity.this, "Ответ должен совпадать с одним из вариантов ответа!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        databaseReferenceUser.child("question"+index).child("q_word").setValue(question);
                        databaseReferenceUser.child("question"+index).child("ans1").setValue(ans1);
                        databaseReferenceUser.child("question"+index).child("ans2").setValue(ans2);
                        databaseReferenceUser.child("question"+index).child("ans3").setValue(ans3);
                        databaseReferenceUser.child("question"+index).child("ans4").setValue(ans4);
                        databaseReferenceUser.child("question"+index).child("answer").setValue(right_answer);
                        questionTextView.setText(question);
                        ans1TextView.setText("1)" + ans1);
                        ans2TextView.setText("2)" + ans2);
                        ans3TextView.setText("3)" + ans3);
                        ans4TextView.setText("4)" + ans4);
                        rightAnswerTextView.setText("Ответ: " + right_answer);
                        dialog.dismiss();
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

        ImageView deleteImageView = new ImageView(this);
        deleteImageView.setLayoutParams(imageLayoutParams);
        deleteImageView.setImageResource(R.drawable.dont_know);
        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUser.child("question"+index).child("isDeleted").setValue("yes");
                cardView.setVisibility(View.GONE);
                Toast.makeText(AddQuestionActivity.this, "Вопрос удалён", Toast.LENGTH_SHORT).show();
            }
        });

        imagesLinearLayout.addView(renameImageView);
        imagesLinearLayout.addView(deleteImageView);
        
        cardLinearLayout.addView(textLinearLayout);
        if (Objects.equals(is_from_catalog, "0")) {
            cardLinearLayout.addView(imagesLinearLayout);
        }

        cardView.addView(cardLinearLayout);

        questionsLinearLayout.addView(cardView, 0);
        questionScrollView.removeAllViews();
        questionScrollView.addView(questionsLinearLayout);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent;
        if (Objects.equals(is_from_catalog, "0")) {
            intent = new Intent(AddQuestionActivity.this, TeacherTasksActivity.class);
        } else {
            intent = new Intent(AddQuestionActivity.this, TestsCatalogActivity.class);
        }
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
