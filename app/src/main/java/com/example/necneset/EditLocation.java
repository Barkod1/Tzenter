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
import android.widget.ImageView;
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

public class EditLocation extends AppCompatActivity {
    private LinearLayout mInputRowsContainer;
    private Button mAddButton;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    Button  firstHour , btnDelete;
    ImageButton btnSubmit;
    EditText name, details, firstPrayer;
    BeitCneset beitCneset;
    int index = 1;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
        String key = getIntent().getStringExtra("key");
        firebaseDatabase = FirebaseDatabase.getInstance();
        firstPrayer = findViewById(R.id.name_input1);
        firstHour = findViewById(R.id.btnSelectFirst);
        databaseReference = firebaseDatabase.getReference("/locations/"+key);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                beitCneset = snapshot.getValue(BeitCneset.class);
                Log.d("prayers",beitCneset.prayers.toString() + " 00000" + beitCneset.prayers.get(0));
                name = findViewById(R.id.etName);
                details = findViewById(R.id.etDetails);
                name.setText(beitCneset.name);
                details.setText(beitCneset.details);

                firstPrayer.setText(beitCneset.prayers.get(0).name);
                firstHour.setText(beitCneset.prayers.get(0).hour);
                mInputRowsContainer = (LinearLayout) findViewById(R.id.input_rows_container);
                mAddButton = (Button) findViewById(R.id.add_button);
                mAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mInputRowsContainer.getChildCount() < 7)
                            addInputRow();
                    }
                });

                for(int i = 1; i < beitCneset.prayers.size(); i++){
                    addInput();

                    index++;
                }
                databaseReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference = firebaseDatabase.getReference("/locations/");
                databaseReference.child(key).removeValue();
                databaseReference = firebaseDatabase.getReference("/users/");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            if(ds.child(key).exists()){
                                databaseReference.child(ds.getKey()).setValue(1);

                            }
                        }
                        Intent intent = new Intent(EditLocation.this, MapsActivity.class);
                        startActivity(intent);
                        databaseReference.removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        // Set the button click listener
        firstHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of the TimePickerDialog fragment
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EditLocation.this,
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

        btnSubmit = findViewById(R.id.btnUploadEdit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Log.d("submit upload", "upload");
                            databaseReference = firebaseDatabase.getReference("/locations");
                            BeitCneset beitCneset = new BeitCneset(name.getText().toString(), details.getText().toString(),
                                    MapsActivity.latitude, MapsActivity.longitude, firebaseAuth.getCurrentUser().getUid());
                        if(name.getText().toString().equals("") || details.getText().toString().equals("") ){
                            Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                            return;

                        }
                            Prayer prayer = new Prayer(firstPrayer.getText().toString().trim(), firstHour.getText().toString().trim());
                        if(firstHour.getText().toString().equals("בחר שעה")){
                            Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                            return;

                        }
                        if(firstPrayer.getText().toString().equals("")){
                            Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                            return;

                        }
                            beitCneset.prayers.add(prayer);

                            for (int i = 0; i < mInputRowsContainer.getChildCount(); i++) {
                                Log.d("loopsssss", "loops");
                                View childView = mInputRowsContainer.getChildAt(i);
                                if (childView instanceof LinearLayout) {
                                    EditText editText = (EditText) ((LinearLayout) childView).getChildAt(0);
                                    Button button = (Button) ((LinearLayout) childView).getChildAt(1);
                                    if(editText.getText().toString().equals("")){
                                        Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                                        return;

                                    }
                                    if(button.getText().toString().equals("בחר שעה")){
                                        Toast.makeText(getApplicationContext(), "מלא את כל הפרטים!", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    prayer = new Prayer(editText.getText().toString().trim(), button.getText().toString().trim());
                                    beitCneset.prayers.add(prayer);
                            }

                        }
                databaseReference.child(key).setValue(beitCneset);

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                    }


        });
    }



    private void addInput() {
        Log.d(("check"), "add input");
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
        button.setText(beitCneset.prayers.get(index).hour);
        Log.d(("checkthis"), beitCneset.prayers.get(index).hour + " "+ beitCneset.prayers.get(index).name + " "+ index);

        for (int i = 0; i < mInputRowsContainer.getChildCount(); i++) {
            Log.d("loopsssss", "loops");
            View childView = mInputRowsContainer.getChildAt(i);
            if (childView instanceof LinearLayout) {
                EditText editText = (EditText) ((LinearLayout) childView).getChildAt(0);
                editText.setText(beitCneset.prayers.get(i + 1 ).name);
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of the TimePickerDialog fragment
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EditLocation.this,
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
                        EditLocation.this,
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