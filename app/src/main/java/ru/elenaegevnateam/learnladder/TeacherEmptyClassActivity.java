package ru.elenaegevnateam.learnladder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.util.ArrayList;

public class TeacherEmptyClassActivity extends AppCompatActivity {
    private String user_id;
    private String clas;
    private EditText edtclass;
    DatabaseReference databaseReferenceUser;
    DatabaseReference databaseReferenceClasses;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacheremptyclass);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        Spinner spinner = findViewById(R.id.spinnerClass);
        edtclass = findViewById(R.id.editText);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUser = database.getReference("users").child(user_id);
        databaseReferenceClasses = database.getReference("classes");

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherEmptyClassActivity.this, TeacherSettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherEmptyClassActivity.this, TeacherTasksActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.editText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                clas = adapterView.getItemAtPosition(position).toString();
                edtclass.setText(clas);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clas.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Укажите класс!", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    databaseReferenceClasses.child(clas).child("teacher").child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String teacher = task.getResult().getValue(String.class);
                            if (teacher != null) {
                                Toast toast = Toast.makeText(getApplicationContext(), "За этим классом уже закреплён другой учитель!", Toast.LENGTH_SHORT);
                                toast.show();
                            } else {
                                databaseReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String cclas = edtclass.getText().toString();
                                            String name = dataSnapshot.child("name").getValue(String.class);
                                            String surname = dataSnapshot.child("surname").getValue(String.class);
                                            databaseReferenceClasses.child(cclas).child("teacher").child("name").setValue(surname + " " + name);
                                            databaseReferenceClasses.child(cclas).child("teacher").child("id").setValue(user_id);
                                            databaseReferenceUser.child("classes").child(cclas).setValue("");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                Intent intent = new Intent(TeacherEmptyClassActivity.this, TeacherClassesActivity.class);
                                intent.putExtra("user_id", user_id);
                                intent.putExtra("first_class", edtclass.getText().toString());
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(TeacherEmptyClassActivity.this, TeacherTasksActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
