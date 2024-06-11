package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class TeacherTasksActivity extends AppCompatActivity {
    private String user_id;
    private int currentTask = 0;
    private ScrollView tasksScrollView;
    private LinearLayout tasksLinearLayout;
    private DatabaseReference databaseReferenceUser;
    private DatabaseReference databaseReferenceTasks;
    private ArrayList<Test> tests = new ArrayList<Test>();
    private ArrayList<String> tests_indexes = new ArrayList<>();
    private ArrayList<String> classes = new ArrayList<String>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachertasks);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        tasksScrollView = findViewById(R.id.tasksScrollView);
        tasksLinearLayout = findViewById(R.id.tasksLinearLayout);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUser = database.getReference("users").child(user_id);
        databaseReferenceTasks = database.getReference("tasks");

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherTasksActivity.this, TeacherSettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.classImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUser.child("classes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                        for (DataSnapshot lastDataSnapshot : iterable) {
                            classes.add(lastDataSnapshot.getKey());
                        }
                        Intent intent;
                        if (classes.isEmpty()) {
                            intent = new Intent(TeacherTasksActivity.this, TeacherEmptyClassActivity.class);
                        }
                        else {
                            intent = new Intent(TeacherTasksActivity.this, TeacherClassesActivity.class);
                        }
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                });
            }
        });

        databaseReferenceUser.child("tasks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentTask = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
                for (int i = 0; i < tests.size(); i++) {
                    if (Objects.equals(tests.get(i).isDeleted, "no")) {
                        int j = tests_indexes.indexOf("test"+(i+1));
                        addTask(tests.get(j).name, tests.get(j).theme, tests_indexes.get(j));
                    }
                }
                loadingView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.newTestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherTasksActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_newtest, null);
                EditText nameBox = dialogView.findViewById(R.id.nameBox);
                EditText themeBox = dialogView.findViewById(R.id.themeBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = nameBox.getText().toString();
                        String theme = themeBox.getText().toString();
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(theme)) {
                            Toast.makeText(TeacherTasksActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int i = currentTask + 1;
                        addTask(name, theme, "test" + i);
                        databaseReferenceUser.child("tasks").child("test" + i).child("name").setValue(name);
                        databaseReferenceUser.child("tasks").child("test" + i).child("theme").setValue(theme);
                        databaseReferenceUser.child("tasks").child("test" + i).child("questions").setValue("");
                        databaseReferenceUser.child("tasks").child("test" + i).child("isDeleted").setValue("no");
                        dialog.dismiss();
                        currentTask++;
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

        findViewById(R.id.catalogTestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherTasksActivity.this, TestsCatalogActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });
    }

    private void addTask(String name, String theme, String test) {
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

        TextView themeTextView = new TextView(this);
        themeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        themeTextView.setText(theme + " мин");
        themeTextView.setTextSize(18);
        themeTextView.setPadding(30, 10, 20, 10);
        themeTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        textLinearLayout.addView(themeTextView);

        TextView questionsTextView = new TextView(this);
        questionsTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        questionsTextView.setText("Редактировать вопросы");
        questionsTextView.setTextSize(13);
        questionsTextView.setPadding(30, 10, 20, 10);
        questionsTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        questionsTextView.setTextColor(getResources().getColor(R.color.orange));
        textLinearLayout.addView(questionsTextView);
        questionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherTasksActivity.this, AddQuestionActivity.class);
                intent.putExtra("test", test);
                intent.putExtra("user_id", user_id);
                intent.putExtra("name", name);
                intent.putExtra("is_from_catalog", "0");
                intent.putExtra("theme", theme);
                startActivity(intent);
            }
        });

        TextView resultsTextView = new TextView(this);
        resultsTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        resultsTextView.setText("Посмотреть результаты");
        resultsTextView.setTextSize(13);
        resultsTextView.setPadding(30, 10, 20, 10);
        resultsTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        resultsTextView.setTextColor(getResources().getColor(R.color.blue));
        textLinearLayout.addView(resultsTextView);
        resultsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherTasksActivity.this, ResultsActivity.class);
                intent.putExtra("test", test);
                intent.putExtra("user_id", user_id);
                intent.putExtra("name", name);
                intent.putExtra("theme", theme);
                startActivity(intent);
            }
        });

        TextView publishTextView = new TextView(this);
        publishTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        publishTextView.setText("Опубликовать тест");
        publishTextView.setTextSize(20);
        publishTextView.setPadding(30, 10, 20, 10);
        publishTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        textLinearLayout.addView(publishTextView);
        publishTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceTasks.child(test + "_by_" + user_id).child("name").setValue(nameTextView.getText().toString());
                databaseReferenceTasks.child(test + "_by_" + user_id).child("theme").setValue(themeTextView.getText().toString().replace(" мин", ""));
                databaseReferenceTasks.child(test + "_by_" + user_id).child("author_id").setValue(user_id);
                databaseReferenceUser.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        String a_name = task.getResult().getValue(String.class);
                        databaseReferenceUser.child("surname").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String a_surname = task.getResult().getValue(String.class);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("author").setValue(a_surname + " " + a_name);
                            }
                        });
                    }
                });
                databaseReferenceUser.child("tasks").child(test).child("questions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        ArrayList<Question> questions = new ArrayList<>();
                        Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                        for (DataSnapshot lastDataSnapshot : iterable) {
                            questions.add(lastDataSnapshot.getValue(Question.class));
                        }
                        for (int i=0; i<questions.size(); i++) {
                            if (Objects.equals(questions.get(i).isDeleted, "no")) {
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("q_word").setValue(questions.get(i).q_word);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("ans1").setValue(questions.get(i).ans1);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("ans2").setValue(questions.get(i).ans2);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("ans3").setValue(questions.get(i).ans3);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("ans4").setValue(questions.get(i).ans4);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("answer").setValue(questions.get(i).answer);
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).child("isDeleted").setValue("no");
                            }
                            else {
                                databaseReferenceTasks.child(test + "_by_" + user_id).child("questions").child("question" + (i + 1)).removeValue();
                            }
                        }
                        Toast.makeText(TeacherTasksActivity.this, "Новая версия теста опубликована!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        cardLinearLayout.addView(textLinearLayout);

        LinearLayout imagesLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams imagesLayoutParams = new LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.WRAP_CONTENT);
        imagesLayoutParams.gravity = Gravity.CENTER;
        imagesLinearLayout.setOrientation(LinearLayout.VERTICAL);
        imagesLinearLayout.setLayoutParams(imagesLayoutParams);

        ImageView renameImageView = new ImageView(this);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(75, 75);
        imageLayoutParams.gravity = Gravity.CENTER;
        imageLayoutParams.setMargins(0, 40,0,0);
        renameImageView.setLayoutParams(imageLayoutParams);
        renameImageView.setImageResource(R.drawable.pencil_colored);
        renameImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherTasksActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_renametest, null);
                EditText nameBox = dialogView.findViewById(R.id.nameBox);
                EditText themeBox = dialogView.findViewById(R.id.themeBox);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = nameBox.getText().toString();
                        String theme = themeBox.getText().toString();
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(theme)) {
                            Toast.makeText(TeacherTasksActivity.this, "Заполните все поля!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        nameTextView.setText(name);
                        themeTextView.setText(theme + " мин");
                        databaseReferenceUser.child("tasks").child(test).child("name").setValue(name);
                        databaseReferenceUser.child("tasks").child(test).child("theme").setValue(theme);
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
                databaseReferenceUser.child("tasks").child(test).child("isDeleted").setValue("yes");
                cardView.setVisibility(View.GONE);
                Toast.makeText(TeacherTasksActivity.this, "Задание удалено из вашего личного кабинета", Toast.LENGTH_SHORT).show();
            }
        });

        imagesLinearLayout.addView(renameImageView);
        imagesLinearLayout.addView(deleteImageView);

        cardLinearLayout.addView(imagesLinearLayout);
        cardView.addView(cardLinearLayout);

        tasksLinearLayout.addView(cardView, 0);

        tasksScrollView.removeAllViews();
        tasksScrollView.addView(tasksLinearLayout);
    }

    @Override
    public void onBackPressed() {
    }
}
