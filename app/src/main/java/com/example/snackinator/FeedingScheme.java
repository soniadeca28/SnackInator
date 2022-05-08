package com.example.snackinator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FeedingScheme extends AppCompatActivity {

    private static final String TAG = "TAG";
    Button breakfastTimeButton, lunchTimeButton, dinnerTimeButton, backToHomeButton, saveButton;
    ToggleButton waterChoiceButton;
    int hourBreakfast,minuteBreakfast,hourLunch,minuteLunch,hourDinner,minuteDinner,
    backupBreakfastH = -1, getBackupBreakfastM = -1, backupLunchH = -1, getBackupLunchM = -1, backupDinnerH = -1, getBackupDinnerM = -1;
    boolean breakfastTimeButtonClicked, lunchTimeButtonClicked, dinnerTimeButtonClicked, waterChoiceButtonClicked;
    TextView servingBreakfast, servingLunch, servingDinner;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_scheme);

        firebaseDatabase = FirebaseDatabase.getInstance("https://snackinator-lic-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = firebaseDatabase.getReference();

        retrieveDataFromFirebase();

        breakfastTimeButtonClicked = false;
        breakfastTimeButton = findViewById(R.id.breakfastTime);
        breakfastTimeButton.setOnClickListener(v->timePickerBreakfast(v,breakfastTimeButton));

        lunchTimeButtonClicked = false;
        lunchTimeButton = findViewById(R.id.lunchTime);
        lunchTimeButton.setOnClickListener(v->timePickerLunch(v,lunchTimeButton));

        dinnerTimeButtonClicked = false;
        dinnerTimeButton = findViewById(R.id.dinnerTime);
        dinnerTimeButton.setOnClickListener(v->timePickerDinner(v,dinnerTimeButton));
        
        backToHomeButton = findViewById(R.id.backButton);
        backToHomeButton.setOnClickListener(v->backToHomePage());

        waterChoiceButtonClicked = false;
        waterChoiceButton = findViewById(R.id.waterChoice);

        servingBreakfast = findViewById(R.id.servingSize1);
        servingLunch = findViewById(R.id.servingSize2);
        servingDinner = findViewById(R.id.servingSize3);

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v->saveFeedingScheme());

    }



    private void retrieveDataFromFirebase()
    {
        databaseReference.child("/dataFromApp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long hBkf = snapshot.child("/breakfastHour").getValue(Long.class);
                Long mBkf = snapshot.child("/breakfastMinute").getValue(Long.class);
                backupBreakfastH = hBkf == null? 0: Math.toIntExact(hBkf); //if there's nothing in the database it shall set the dafault value, which is 0
                getBackupBreakfastM = mBkf == null? 0: Math.toIntExact(mBkf);
                //saves the backup so when you see the time picker ui, you can start scrolling from the last time saved

                Long hLch = snapshot.child("/lunchHour").getValue(Long.class);
                Long mLch = snapshot.child("/lunchMinute").getValue(Long.class);
                backupLunchH = hLch == null? 0: Math.toIntExact(hLch);
                getBackupLunchM = mLch == null? 0: Math.toIntExact(mLch);

                Long hDn = snapshot.child("/dinnerHour").getValue(Long.class);
                Long mDn = snapshot.child("/dinnerMinute").getValue(Long.class);
                backupDinnerH = hDn == null? 0: Math.toIntExact(hDn);
                getBackupDinnerM = mDn == null? 0:  Math.toIntExact(mDn);

                String sBkf = snapshot.child("/servingBreakfast").getValue(String.class);

                String sLch = snapshot.child("/servingLunch").getValue(String.class);

                String sDn = snapshot.child("/servingDinner").getValue(String.class);

               Long fountain = snapshot.child("/fountainAllDay").getValue(Long.class);

                if (hBkf != null && mBkf != null) {
                    breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hBkf, mBkf));
                } else {
                    breakfastTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",0, 0));
                }

                if (hLch != null && mLch != null) {
                    lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hLch, mLch));
                } else {
                    lunchTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",0, 0));
                }

                if (hDn != null && mDn != null) {
                    dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hDn, mDn));
                } else {
                    dinnerTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",0, 0));
                }

                if(sBkf == null)
                {
                    servingBreakfast.setText("0");
                }
                else
                {
                    servingBreakfast.setText(sBkf.trim());
                }

                if(sLch == null)
                {
                    servingLunch.setText("0");
                }
                else
                {
                    servingLunch.setText(sLch.trim());
                }

                if(sDn == null)
                {
                    servingDinner.setText("0");
                }
                else
                {
                    servingDinner.setText(sDn.trim());
                }

                if(fountain == null || fountain == 1)
                {
                    waterChoiceButton.setText(getString(R.string.all_day));
                }
                else
                {
                    waterChoiceButton.setText(getString(R.string.once_a_day));
                    waterChoiceButton.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FeedingScheme.this, "Some of your data could not be retrieved from our database! \n", Toast.LENGTH_SHORT).show();
                breakfastTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",0, 0));
                lunchTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",0, 0));
                dinnerTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",0, 0));
            }
        });
    }

    private void saveFeedingScheme() {

        if (breakfastTimeButtonClicked) // checks if the button has been clicked
        {
            databaseReference.child("/dataFromApp").child("/breakfastHour").setValue(hourBreakfast); //if it was clicked it saves the new time
            databaseReference.child("/dataFromApp").child("/breakfastMinute").setValue(minuteBreakfast);
            breakfastTimeButtonClicked = false;
        }
        //if the button wasn't clicked it doesn't save anything, bc if it did save it would be 0 for both!!

        if(lunchTimeButtonClicked) {
            databaseReference.child("/dataFromApp").child("/lunchHour").setValue(hourLunch);
            databaseReference.child("/dataFromApp").child("/lunchMinute").setValue(minuteLunch);
            lunchTimeButtonClicked = false;
        }

        if(dinnerTimeButtonClicked) {
            databaseReference.child("/dataFromApp").child("/dinnerHour").setValue(hourDinner);
            databaseReference.child("/dataFromApp").child("/dinnerMinute").setValue(minuteDinner);
            dinnerTimeButtonClicked = false;
        }

        if (waterChoiceButton.getText().toString().trim().equals("All day")) {
            databaseReference.child("/dataFromApp").child("/fountainAllDay").setValue(1);
        } else {
                databaseReference.child("/dataFromApp").child("/fountainAllDay").setValue(0);
        }

        databaseReference.child("/dataFromApp").child("/servingBreakfast").setValue(servingBreakfast.getText().toString().trim());

        databaseReference.child("/dataFromApp").child("/servingLunch").setValue(servingLunch.getText().toString().trim());

        databaseReference.child("/dataFromApp").child("/servingDinner").setValue(servingDinner.getText().toString().trim());

        Toast.makeText(FeedingScheme.this, "Saved successfully! \n", Toast.LENGTH_SHORT).show();

    }

    private void backToHomePage() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void timePickerBreakfast(View view, Button btn)
    {
        breakfastTimeButtonClicked = true; //if you've arrived here it means the button has been clicked and so the bool is set to true
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hourBreakfast = hourPicker; //sets the time with what you picked
            minuteBreakfast = minutePicker;
            btn.setText(String.format(Locale.getDefault(),"%02d:%02d",hourBreakfast,minuteBreakfast)); // saves the text to what you picked
        };
        int timePickerStyle = AlertDialog.THEME_HOLO_DARK; // !!!!!!!!!! deprecated

        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this, timePickerStyle, onTimeSetListener, backupBreakfastH, getBackupBreakfastM, true); // what you see when you open the time picker
        //it is set to the backup values so you don't have to start scrolling from 00:00, but from the last values saved
        timePickerDialog.show(); //shows the ui where you can pick the time
    }

    public void timePickerLunch(View view, Button btn)
    {
        lunchTimeButtonClicked = true;
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hourLunch = hourPicker;
            minuteLunch = minutePicker;
            btn.setText(String.format(Locale.getDefault(),"%02d:%02d",hourLunch,minuteLunch));
        };
        int timePickerStyle = AlertDialog.THEME_HOLO_DARK; // !!!!!!!!!! deprecated
        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this,timePickerStyle,onTimeSetListener,backupLunchH,getBackupLunchM,true);
        timePickerDialog.show();
    }

    public void timePickerDinner(View view, Button btn)
    {
        dinnerTimeButtonClicked = true;
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hourDinner = hourPicker;
            minuteDinner = minutePicker;
            btn.setText(String.format(Locale.getDefault(),"%02d:%02d",hourDinner,minuteDinner));
        };
        int timePickerStyle = AlertDialog.THEME_HOLO_DARK; // !!!!!!!!!! deprecated
        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this,timePickerStyle,onTimeSetListener,backupDinnerH,getBackupDinnerM,true);
        timePickerDialog.show();
    }

}