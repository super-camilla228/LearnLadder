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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EmptyClassActivity extends AppCompatActivity {
    private String user_id;
    private String clas;
    private EditText edtclass;
    DatabaseReference databaseReferenceUsers;
    DatabaseReference databaseReferenceClasses;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emptyclass);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        Spinner spinner = findViewById(R.id.spinnerClass);
        edtclass = findViewById(R.id.editText);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReferenceUsers = database.getReference("users");
        databaseReferenceClasses = database.getReference("classes");

        findViewById(R.id.themesImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyClassActivity.this, ThemesActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.settingsImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyClassActivity.this, SettingsActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.accountImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyClassActivity.this, AccountActivity.class);
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        findViewById(R.id.tasksImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmptyClassActivity.this, TasksActivity.class);
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
                    Toast toast = Toast.makeText(getApplicationContext(), "Укажи класс!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    databaseReferenceUsers.child(user_id).child("class").setValue(clas);
                    databaseReferenceUsers.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String name = dataSnapshot.child("name").getValue(String.class);
                                String surname = dataSnapshot.child("surname").getValue(String.class);
                                int coins = dataSnapshot.child("coins").getValue(int.class);
                                databaseReferenceClasses.child(clas).child("students").child(user_id).child("name").setValue(surname + " " + name);
                                databaseReferenceClasses.child(clas).child("students").child(user_id).child("coins").setValue(coins);
                                databaseReferenceClasses.child(clas).child("students").child(user_id).child("id").setValue(user_id);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    Intent intent = new Intent(EmptyClassActivity.this, ClassActivity.class);
                    intent.putExtra("user_id", user_id);
                    startActivity(intent);
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EmptyClassActivity.this, ThemesActivity.class);
        intent.putExtra("user_id", user_id);
        startActivity(intent);
    }
}
