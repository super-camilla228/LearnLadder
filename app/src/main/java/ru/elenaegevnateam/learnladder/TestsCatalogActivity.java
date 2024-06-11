package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.List;
import java.util.Objects;

public class TestsCatalogActivity extends AppCompatActivity {
    private String user_id;
    private DatabaseReference databaseReferenceUserTasks;
    private DatabaseReference databaseReferenceTasks;
    private ArrayList<CatalogTest> tests = new ArrayList<>();
    private ArrayList<String> tests_indexes = new ArrayList<>();
    private ScrollView tasksScrollView;
    private LinearLayout tasksLinearLayout;
    private int testAmount;
    private SearchView testSearchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testscatalog);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        View loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        tasksScrollView = findViewById(R.id.tasksScrollView);
        tasksLinearLayout = findViewById(R.id.tasksLinearLayout);
        testSearchView = findViewById(R.id.testSearchView);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUserTasks = database.getReference("users").child(user_id).child("tasks");
        databaseReferenceTasks = database.getReference("tasks");

        databaseReferenceUserTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                testAmount = (int) dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReferenceTasks.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    tests.add(lastDataSnapshot.getValue(CatalogTest.class));
                    tests_indexes.add(lastDataSnapshot.getKey());
                }
                displayTests(tests);
                loadingView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestsCatalogActivity.this, TeacherTasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        testSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTests(newText);
                return false;
            }
        });
    }

    private void displayTests(List<CatalogTest> testsToDisplay) {
        tasksLinearLayout.removeAllViews();
        for (int i = 0; i < testsToDisplay.size(); i++) {
            CatalogTest test = testsToDisplay.get(i);
            int j = tests.indexOf(test);
            showTests(test.name, test.theme, test.author, test.author_id, j, testAmount + 1);
        }
    }

    private void filterTests(String query) {
        List<CatalogTest> filteredTests = new ArrayList<>();
        for (CatalogTest test : tests) {
            if (test.name.toLowerCase().contains(query.toLowerCase())) {
                filteredTests.add(test);
            }
        }
        displayTests(filteredTests);
    }

    private void showTests(String name, String theme, String author, String author_id, int index, int tests_num) {
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

        TextView authorTextView = new TextView(this);
        authorTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        authorTextView.setText("Автор: " + author);
        authorTextView.setTextSize(18);
        authorTextView.setPadding(30, 10, 20, 10);
        authorTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        textLinearLayout.addView(authorTextView);

        TextView questionsTextView = new TextView(this);
        questionsTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        questionsTextView.setText("Посмотреть вопросы");
        questionsTextView.setTextSize(13);
        questionsTextView.setPadding(30, 10, 20, 10);
        questionsTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        questionsTextView.setTextColor(getResources().getColor(R.color.orange));
        textLinearLayout.addView(questionsTextView);
        questionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestsCatalogActivity.this, AddQuestionActivity.class);
                intent.putExtra("test", tests_indexes.get(index));
                intent.putExtra("is_from_catalog", "1");
                intent.putExtra("user_id", user_id);
                intent.putExtra("name", name);
                intent.putExtra("theme", theme);
                startActivity(intent);
            }
        });
        cardLinearLayout.addView(textLinearLayout);

        LinearLayout imagesLinearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams imagesLayoutParams = new LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.WRAP_CONTENT);
        imagesLayoutParams.gravity = Gravity.CENTER;
        imagesLinearLayout.setOrientation(LinearLayout.VERTICAL);
        imagesLinearLayout.setLayoutParams(imagesLayoutParams);

        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(75, 75);
        imageLayoutParams.gravity = Gravity.CENTER;

        ImageView addImageView = new ImageView(this);
        addImageView.setLayoutParams(imageLayoutParams);
        addImageView.setImageResource(R.drawable.add_task);
        addImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUserTasks.child("test" + tests_num).child("name").setValue(name);
                databaseReferenceUserTasks.child("test" + tests_num).child("theme").setValue(theme);
                databaseReferenceUserTasks.child("test" + tests_num).child("isDeleted").setValue("no");
                ArrayList<Question> questions = new ArrayList<>();
                databaseReferenceTasks.child(tests_indexes.get(index)).child("questions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                        for (DataSnapshot lastDataSnapshot : iterable) {
                            questions.add(lastDataSnapshot.getValue(Question.class));
                        }
                        for (int i = 0; i < questions.size(); i++) {
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("q_word").setValue(questions.get(i).q_word);
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("ans1").setValue(questions.get(i).ans1);
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("ans2").setValue(questions.get(i).ans2);
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("ans3").setValue(questions.get(i).ans3);
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("ans4").setValue(questions.get(i).ans4);
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("answer").setValue(questions.get(i).answer);
                            databaseReferenceUserTasks.child("test" + tests_num).child("questions").child("question" + (i + 1)).child("isDeleted").setValue("no");
                        }
                        Toast.makeText(TestsCatalogActivity.this, "Задание добавлено!", Toast.LENGTH_SHORT).show();
                        testAmount++;
                    }
                });
            }
        });
        imagesLinearLayout.addView(addImageView);

        ImageView deleteImageView = new ImageView(this);
        deleteImageView.setLayoutParams(imageLayoutParams);
        deleteImageView.setPadding(0,20, 0,0);
        deleteImageView.setImageResource(R.drawable.dont_know);
        deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceTasks.child(tests_indexes.get(index)).removeValue();
                cardView.setVisibility(View.GONE);
                Toast.makeText(TestsCatalogActivity.this, "Задание удалено из каталога", Toast.LENGTH_SHORT).show();
            }
        });

        if (Objects.equals(user_id, author_id)) {
            imagesLinearLayout.addView(deleteImageView);
        }

        cardLinearLayout.addView(imagesLinearLayout);
        cardView.addView(cardLinearLayout);

        tasksLinearLayout.addView(cardView, 0);

        tasksScrollView.removeAllViews();
        tasksScrollView.addView(tasksLinearLayout);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TestsCatalogActivity.this, TeacherTasksActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
