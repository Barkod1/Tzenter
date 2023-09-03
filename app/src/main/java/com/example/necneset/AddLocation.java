package com.example.necneset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddLocation extends AppCompatActivity {
    private LinearLayout mInputRowsContainer;
    private Button mAddButton;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    Button  firstHour, btnEdit;
    ImageButton btnSubmit;
    EditText name, details, firstPrayer;
    private boolean alreadyHave = false;
    private boolean flag = false;
    private boolean didUploded = false;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.etName);
        details = findViewById(R.id.etDetails);
        firstPrayer = findViewById(R.id.name_input1);
        firstHour = findViewById(R.id.btnSelectFirst);

        // Set the button click listener
        firstHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of the TimePickerDialog fragment
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        AddLocation.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Do something with the selected time
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                Calendar time = Calendar.getInstance();
                                time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                time.set(Calendar.MINUTE, minute);
                                String formattedTime = sdf.format(time.getTime());
                                firstHour.setText(formattedTime);
                            }
                        },
                        // Set the default time to show in the dialog
                        12, 0, false);

                // Show the TimePickerDialog fragment
                timePickerDialog.show();
            }
        });
        mInputRowsContainer = (LinearLayout) findViewById(R.id.input_rows_container);
        mAddButton = (Button) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputRowsContainer.getChildCount() < 7)
                    addInputRow();
            }
        });
        btnSubmit = findViewById(R.id.btnUpload);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;
                Log.d("click",view.toString());
                DatabaseReference userRef = firebaseDatabase.getReference("/locations");

                userRef.addValueEventListener(new ValueEventListener() {


                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!didUploded)
                            databaseReference = firebaseDatabase.getReference("/locations").push();
                        didUploded = true;
                        BeitCneset beitCneset = new BeitCneset(name.getText().toString(), details.getText().toString(),
                                MapsActivity.latitude, MapsActivity.longitude, firebaseAuth.getCurrentUser().getUid());
                        if (name.getText().toString().equals("") || details.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                            flag = true;

                            return;

                        }
                        Prayer prayer = new Prayer(firstPrayer.getText().toString().trim(), firstHour.getText().toString().trim());
                        if (firstHour.getText().toString().equals("בחר שעה")) {
                            Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                            flag = true;

                            return;

                        }
                        if (firstPrayer.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                            flag = true;
                            return;

                        }
                        beitCneset.prayers.add(prayer);
                        Log.d("loopsssss", " " + mInputRowsContainer.getChildCount());
                        for (int i = 0; i < mInputRowsContainer.getChildCount(); i++) {
                            Log.d("loopsssss", "loops");
                            View childView = mInputRowsContainer.getChildAt(i);
                            if (childView instanceof LinearLayout) {
                                EditText editText = (EditText) ((LinearLayout) childView).getChildAt(0);
                                if (editText.getText().toString().equals("")) {
                                    Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                                    flag = true;

                                    return;

                                }
                                Button button = (Button) ((LinearLayout) childView).getChildAt(1);
                                if (button.getText().toString().equals("בחר שעה")) {
                                    Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                                    flag = true;

                                    return;
                                }

                                prayer = new Prayer(editText.getText().toString().trim(), button.getText().toString().trim());
                                beitCneset.prayers.add(prayer);

                            }
                        }
                        if (!flag) {
                            databaseReference.setValue(beitCneset);
                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(intent);
                        }
                        userRef.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


    }


    private void addInputRow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View inputRowView = inflater.inflate(R.layout.input_row, mInputRowsContainer, false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button removeButton = inputRowView.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputRowsContainer.removeView(inputRowView);
            }
        });
        mInputRowsContainer.addView(inputRowView);
        Button button = inputRowView.findViewById(R.id.btnSelectHour);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of the TimePickerDialog fragment
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        AddLocation.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                Calendar time = Calendar.getInstance();
                                time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                time.set(Calendar.MINUTE, minute);
                                String formattedTime = sdf.format(time.getTime());
                                button.setText(formattedTime);
                            }
                        },
                        // Set the default time to show in the dialog
                        0, 0, false);

                // Show the TimePickerDialog fragment
                timePickerDialog.show();
            }
        });

    }

}