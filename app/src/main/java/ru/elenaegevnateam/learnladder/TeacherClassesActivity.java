package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TeacherClassesActivity extends AppCompatActivity {
    private String user_id;
    private String first_clas;
    private TextView classTextView;
    private LinearLayout linearLayoutClassmates;
    private ScrollView scrollViewClassmates;
    private FrameLayout loadingView;
    private DatabaseReference databaseReferenceUser;
    private ArrayList<String> classes = new ArrayList<>();
    private ArrayList<Student> students = new ArrayList<>();
    private ArrayList<Test> tests = new ArrayList<>();
    private ArrayList<String> tests_indexes = new ArrayList<>();
    private ArrayList<String> task_chosed_students_indexes = new ArrayList<>();
    private ArrayList<String> remove_chosed_students_indexes = new ArrayList<>();
    private int currentClassIndex = 0;
    private DatabaseReference databaseReferenceClasses;
    private DatabaseReference databaseReferenceUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacherclasses);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        first_clas = intent.getStringExtra("first_class");

        loadingView = findViewById(R.id.loadingView);
        loadingView.setVisibility(View.VISIBLE);

        classTextView = findViewById(R.id.classTextView);
        linearLayoutClassmates = findViewById(R.id.linearLayoutClassmates);
        scrollViewClassmates = findViewById(R.id.scrollViewClassmates);

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherClassesActivity.this, TeacherSettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherClassesActivity.this, TeacherTasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUser = database.getReference("users").child(user_id);
        databaseReferenceUsers = database.getReference("users");
        databaseReferenceClasses = database.getReference("classes");

        databaseReferenceUser.child("classes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DataSnapshot> task) {
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    classes.add(lastDataSnapshot.getKey());
                }
                if (classes.isEmpty()) {
                    classes.add(first_clas);
                }
                loadstudents(classes.get(currentClassIndex));
            }
        });


        databaseReferenceUser.child("tasks").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                for (DataSnapshot lastDataSnapshot : iterable) {
                    tests.add(lastDataSnapshot.getValue(Test.class));
                    tests_indexes.add(lastDataSnapshot.getKey());
                }
                Iterator<Test> testIterator = tests.iterator();
                Iterator<String> indexIterator = tests_indexes.iterator();

                while (testIterator.hasNext() && indexIterator.hasNext()) {
                    Test test = testIterator.next();
                    String index = indexIterator.next();

                    if ("yes".equals(test.isDeleted)) {
                        testIterator.remove();
                        indexIterator.remove();
                    }
                }
            }
        });

        findViewById(R.id.forwardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (classes.size() <= 1) {
                    Toast.makeText(TeacherClassesActivity.this, "За Вами закреплён только 1 класс", Toast.LENGTH_SHORT).show();
                }
                else if (currentClassIndex == classes.size() - 1) {
                    Toast.makeText(TeacherClassesActivity.this, "Это последний класс", Toast.LENGTH_SHORT).show();
                }
                else {
                    currentClassIndex++;
                    loadstudents(classes.get(currentClassIndex));
                }
            }
        });

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (classes.size() <= 1) {
                    Toast.makeText(TeacherClassesActivity.this, "За Вами закреплён только 1 класс", Toast.LENGTH_SHORT).show();
                }
                else if (currentClassIndex == 0) {
                    Toast.makeText(TeacherClassesActivity.this, "Это первый класс", Toast.LENGTH_SHORT).show();
                }
                else {
                    currentClassIndex--;
                    loadstudents(classes.get(currentClassIndex));
                }
            }
        });


        findViewById(R.id.addClassButton).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TeacherClassesActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_newclass, null);
            EditText classBox = dialogView.findViewById(R.id.classBox);
            Spinner spinner = dialogView.findViewById(R.id.spinnerClass);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            classBox.setOnClickListener(v1 -> spinner.performClick());

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    String cclas = adapterView.getItemAtPosition(position).toString();
                    classBox.setText(cclas);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            ArrayList<String> classList = new ArrayList<>();
            classList.add("10-1");
            classList.add("10-2");
            classList.add("10-3");
            classList.add("10-4");
            classList.add("10-5");
            classList.add("11-1");
            classList.add("11-2");
            classList.add("11-3");
            classList.add("11-4");
            classList.add("11-5");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(TeacherClassesActivity.this, android.R.layout.simple_spinner_item, classList);
            adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
            spinner.setAdapter(adapter);

            dialogView.findViewById(R.id.btnReset).setOnClickListener(v12 -> {
                String clas = classBox.getText().toString();
                databaseReferenceClasses.child(clas).child("teacher").child("name").get().addOnCompleteListener(task -> {
                    String teacher = task.getResult().getValue(String.class);
                    if (teacher != null) {
                        Toast.makeText(getApplicationContext(), "За этим классом уже закреплён учитель!", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        databaseReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String name = dataSnapshot.child("name").getValue(String.class);
                                    String surname = dataSnapshot.child("surname").getValue(String.class);
                                    databaseReferenceClasses.child(clas).child("teacher").child("name").setValue(surname + " " + name);
                                    databaseReferenceClasses.child(clas).child("teacher").child("id").setValue(user_id);
                                    databaseReferenceUser.child("classes").child(clas).setValue("");
                                    classes.add(clas);
                                    currentClassIndex = classes.size() - 1;
                                    loadstudents(classes.get(currentClassIndex));
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("firebase", "Ошибка чтения данных", error.toException());
                            }
                        });
                    }
                });
            });

            dialogView.findViewById(R.id.btnCancel).setOnClickListener(v13 -> dialog.dismiss());

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            dialog.show();
        });

        findViewById(R.id.addTaskButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherClassesActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_addtask, null);
                EditText testBox = dialogView.findViewById(R.id.testBox);
                Spinner spinner = dialogView.findViewById(R.id.spinnerTest);
                ScrollView studentsScrollView = dialogView.findViewById(R.id.studentsScrollView);
                LinearLayout linearLayoutStudents = dialogView.findViewById(R.id.linearLayoutStudents);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                testBox.setOnClickListener(v1 -> spinner.performClick());

                if (tests.isEmpty()) {
                    Toast.makeText(TeacherClassesActivity.this, "Создайте хотя бы один тест!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else {

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                            String test = adapterView.getItemAtPosition(position).toString();
                            testBox.setText(test);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    ArrayList<String> testsList = new ArrayList<>();
                    for (int i = 0; i < tests.size(); i++) {
                        testsList.add(tests.get(i).name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(TeacherClassesActivity.this, android.R.layout.simple_spinner_item, testsList);
                    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    spinner.setAdapter(adapter);
                    addStudents(linearLayoutStudents, studentsScrollView);


                    dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String test_name = testBox.getText().toString();
                            String test_id = null;
                            String test_theme = null;
                            for (int i = 0; i < tests.size(); i++) {
                                if (Objects.equals(tests.get(i).name, test_name)) {
                                    test_id = tests_indexes.get(i);
                                    test_theme = tests.get(i).theme;
                                }
                            }
                            if (task_chosed_students_indexes.isEmpty()) {
                                Toast.makeText(TeacherClassesActivity.this, "Выберите хотя бы одного ученика!", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                for (String id : task_chosed_students_indexes) {
                                    databaseReferenceUsers.child(id).child("tasks").child(test_id).child("name").setValue(test_name);
                                    databaseReferenceUsers.child(id).child("tasks").child(test_id).child("theme").setValue(test_theme);
                                    ArrayList<Question> questions = new ArrayList<Question>();
                                    String finalTest_id = test_id;
                                    databaseReferenceUser.child("tasks").child(test_id).child("questions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                                            for (DataSnapshot lastDataSnapshot : iterable) {
                                                questions.add(lastDataSnapshot.getValue(Question.class));
                                            }
                                            questions.removeIf(question -> Objects.equals(question.isDeleted, "yes"));
                                            if (questions.isEmpty()) {
                                                Toast.makeText(TeacherClassesActivity.this, "Нельзя задать пустой тест!", Toast.LENGTH_SHORT).show();
                                                return;
                                            } else {
                                                for (int i = 0; i < questions.size(); i++) {
                                                    databaseReferenceUsers.child(id).child("tasks").child(finalTest_id).child("questions").child("question" + (i + 1)).child("q_word").setValue(questions.get(i).q_word);
                                                    databaseReferenceUsers.child(id).child("tasks").child(finalTest_id).child("questions").child("question" + (i + 1)).child("ans1").setValue(questions.get(i).ans1);
                                                    databaseReferenceUsers.child(id).child("tasks").child(finalTest_id).child("questions").child("question" + (i + 1)).child("ans2").setValue(questions.get(i).ans2);
                                                    databaseReferenceUsers.child(id).child("tasks").child(finalTest_id).child("questions").child("question" + (i + 1)).child("ans3").setValue(questions.get(i).ans3);
                                                    databaseReferenceUsers.child(id).child("tasks").child(finalTest_id).child("questions").child("question" + (i + 1)).child("ans4").setValue(questions.get(i).ans4);
                                                    databaseReferenceUsers.child(id).child("tasks").child(finalTest_id).child("questions").child("question" + (i + 1)).child("answer").setValue(questions.get(i).answer);

                                                }
                                                Toast.makeText(TeacherClassesActivity.this, "Задание отправлено выбранным ученикам", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            }
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
            }
        });

        findViewById(R.id.removeClassButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherClassesActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_removeclass, null);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String clas = classes.get(currentClassIndex);
                        databaseReferenceUser.child("classes").child(clas).removeValue();
                        databaseReferenceClasses.child(clas).child("teacher").removeValue();
                        classes.remove(currentClassIndex);
                        dialog.dismiss();
                        if (classes.isEmpty()) {
                            Intent intent = new Intent(TeacherClassesActivity.this, TeacherEmptyClassActivity.class);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                        }
                        else {
                            if (currentClassIndex > 0) {
                                currentClassIndex--;
                            }
                            loadstudents(classes.get(currentClassIndex));
                        }
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
        
        findViewById(R.id.deleteStudentsTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TeacherClassesActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_removestudents, null);
                ScrollView studentsScrollView = dialogView.findViewById(R.id.studentsScrollView);
                LinearLayout linearLayoutStudents = dialogView.findViewById(R.id.linearLayoutStudents);

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                removeStudents(linearLayoutStudents, studentsScrollView);
                dialogView.findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String clas = classes.get(currentClassIndex);
                        if (remove_chosed_students_indexes.isEmpty()) {
                            Toast.makeText(TeacherClassesActivity.this, "Выберите хотя бы одного ученика!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            for (String student_id : remove_chosed_students_indexes) {
                                students.remove(student_id);
                                databaseReferenceClasses.child(clas).child("students").child(student_id).removeValue();
                                databaseReferenceUsers.child(student_id).child("class").setValue("");
                            }
                            loadstudents(clas);
                            Toast.makeText(TeacherClassesActivity.this, "Выбранные ученики удалены из класса", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
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
    }

    public void updateCoins (String clas) {
        databaseReferenceClasses.child(clas).child("students").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int totalChildren = (int) snapshot.getChildrenCount();
                final AtomicInteger counter = new AtomicInteger(0);

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String id = ds.getKey();
                    databaseReferenceUsers.child(id).child("coins").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String coins = String.valueOf(task.getResult().getValue());
                            Integer coins_int = Integer.parseInt(coins);
                            databaseReferenceClasses.child(clas).child("students").child(id).child("coins").setValue(coins_int);
                            for (Student student : students) {
                                if (student.getId().equals(id)) {
                                    student.setCoins(coins_int);
                                    break;
                                }
                            }
                            if (counter.incrementAndGet() == totalChildren) {
                                refreshStudentList();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("firebase", "Ошибка чтения данных", error.toException());
            }
        });
    }

    private void refreshStudentList() {
        linearLayoutClassmates.removeAllViews();
        for (int i = 0; i < students.size(); i++) {
            addStudent(i);
        }
        loadingView.setVisibility(View.GONE);
    }
    private void removeStudents(LinearLayout linearLayoutStudents, ScrollView scrollViewStudents) {
        for (int i=0; i<students.size(); i++) {
            CheckBox checkBox = new CheckBox(this);
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            checkBox.setLayoutParams(checkBoxParams);
            checkBox.setText(students.get(i).name);
            checkBox.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
            int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        remove_chosed_students_indexes.add(students.get(finalI).id);
                    } else{
                        remove_chosed_students_indexes.remove(students.get(finalI).id);
                    }
                }
            });
            linearLayoutStudents.addView(checkBox);
        }
        scrollViewStudents.removeAllViews();
        scrollViewStudents.addView(linearLayoutStudents);
    }

    private void addStudents(LinearLayout linearLayoutStudents, ScrollView scrollViewStudents) {
        for (int i=0; i<students.size(); i++) {
            CheckBox checkBox = new CheckBox(this);
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            checkBox.setLayoutParams(checkBoxParams);
            checkBox.setText(students.get(i).name);
            checkBox.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
            int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        task_chosed_students_indexes.add(students.get(finalI).id);
                    } else{
                        task_chosed_students_indexes.remove(students.get(finalI).id);
                    }
                }
            });
            linearLayoutStudents.addView(checkBox);
        }
        scrollViewStudents.removeAllViews();
        scrollViewStudents.addView(linearLayoutStudents);
    }

    private void loadstudents(String clas) {
        loadingView.setVisibility(View.VISIBLE);
        classTextView.setText("Класс: " + clas);
        scrollViewClassmates.removeAllViews();
        students.clear();
        databaseReferenceClasses.child(clas).child("students").get().addOnCompleteListener(task -> {
            Log.d("firebase", String.valueOf(task.getResult().getValue()));
            Iterable<DataSnapshot> iterable = task.getResult().getChildren();
            for (DataSnapshot lastDataSnapshot : iterable) {
                students.add(lastDataSnapshot.getValue(Student.class));
            }
            if (students.isEmpty()) {
                findViewById(R.id.emptyStudents).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.emptyStudents).setVisibility(View.GONE);
                Collections.sort(students);
                updateCoins(clas);
                linearLayoutClassmates.removeAllViews();
                for (int i = 0; i < students.size(); i++) {
                    addStudent(i);
                }
            }
            loadingView.setVisibility(View.GONE);
        });
    }

    private void addStudent(int i){
        LinearLayout studentLayout = new LinearLayout(this);
        studentLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        if (Objects.equals(user_id, students.get(i).getId())) {
            studentLayout.setBackgroundColor(getResources().getColor(R.color.light_orange));
        }
        studentLayout.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParamsStudent = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsStudent.setMargins(30, 0, 30, 0);
        layoutParamsStudent.gravity = Gravity.CENTER;

        TextView placeTextView = new TextView(this);
        placeTextView.setText(String.valueOf(i+1));
        placeTextView.setTextSize(20);
        placeTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        placeTextView.setLayoutParams(layoutParamsStudent);

        TextView nameTextView = new TextView(this);
        nameTextView.setText(students.get(i).getName());
        LinearLayout.LayoutParams layoutParamsName = new LinearLayout.LayoutParams(550, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParamsName.setMargins(30, 0, 30, 0);
        layoutParamsName.gravity = Gravity.CENTER;
        nameTextView.setTextSize(20);
        nameTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        nameTextView.setLayoutParams(layoutParamsName);

        LinearLayout iconCoinsLayout = new LinearLayout(this);
        iconCoinsLayout.setOrientation(LinearLayout.HORIZONTAL);
        iconCoinsLayout.setLayoutParams(layoutParamsStudent);

        ImageView iconImageView = new ImageView(this);
        iconImageView.setImageResource(R.drawable.coin);
        LinearLayout.LayoutParams layoutParamsCoin = new LinearLayout.LayoutParams(60, 60);
        layoutParamsCoin.gravity = Gravity.CENTER;
        iconImageView.setLayoutParams(layoutParamsCoin);

        TextView coinsTextView = new TextView(this);
        coinsTextView.setText(String.valueOf(students.get(i).getCoins()));
        coinsTextView.setTextSize(15);
        coinsTextView.setTypeface(ResourcesCompat.getFont(this, R.font.myfont));
        coinsTextView.setLayoutParams(layoutParamsStudent);

        iconCoinsLayout.addView(coinsTextView);
        iconCoinsLayout.addView(iconImageView);

        studentLayout.addView(placeTextView);
        studentLayout.addView(nameTextView);
        studentLayout.addView(iconCoinsLayout);

        linearLayoutClassmates.addView(studentLayout);
        scrollViewClassmates.removeAllViews();
        scrollViewClassmates.addView(linearLayoutClassmates);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TeacherClassesActivity.this, TeacherTasksActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
