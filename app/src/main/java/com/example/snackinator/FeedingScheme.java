package com.example.snackinator;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedingScheme extends AppCompatActivity {

    private Button breakfastTimeButton;
    private Button lunchTimeButton;
    private Button dinnerTimeButton;
    private ToggleButton waterChoiceButton;
    private int hourBreakfast;
    private int minuteBreakfast;
    private int hourLunch;
    private int minuteLunch;
    private int hourDinner;
    private int minuteDinner;
    private boolean breakfastTimeButtonClicked;
    private boolean lunchTimeButtonClicked;
    private boolean dinnerTimeButtonClicked;
    private TextView servingBreakfast, servingLunch, servingDinner;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_scheme);

        final String databaseLink = "https://snackinator-lic-default-rtdb.europe-west1.firebasedatabase.app";

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(databaseLink);
        databaseReference = firebaseDatabase.getReference();

        retrieveDataFromFirebase();

        breakfastTimeButtonClicked = false;
        breakfastTimeButton = findViewById(R.id.breakfastTime);
        breakfastTimeButton.setOnClickListener(v -> timePickerBreakfast(breakfastTimeButton));

        lunchTimeButtonClicked = false;
        lunchTimeButton = findViewById(R.id.lunchTime);
        lunchTimeButton.setOnClickListener(v -> timePickerLunch(lunchTimeButton));

        dinnerTimeButtonClicked = false;
        dinnerTimeButton = findViewById(R.id.dinnerTime);
        dinnerTimeButton.setOnClickListener(v -> timePickerDinner(dinnerTimeButton));

        Button backToHomeButton = findViewById(R.id.backButton);
        backToHomeButton.setOnClickListener(v -> backToHomePage());

        waterChoiceButton = findViewById(R.id.waterChoice);

        servingBreakfast = findViewById(R.id.servingSize1);
        servingLunch = findViewById(R.id.servingSize2);
        servingDinner = findViewById(R.id.servingSize3);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveFeedingScheme());

    }


    private void retrieveDataFromFirebase() {
        databaseReference.child("/dataFromApp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long hBkf = snapshot.child("/breakfastHour").getValue(Long.class);
                Long mBkf = snapshot.child("/breakfastMinute").getValue(Long.class);

                Long hLch = snapshot.child("/lunchHour").getValue(Long.class);
                Long mLch = snapshot.child("/lunchMinute").getValue(Long.class);

                Long hDn = snapshot.child("/dinnerHour").getValue(Long.class);
                Long mDn = snapshot.child("/dinnerMinute").getValue(Long.class);

                String sBkf = snapshot.child("/servingBreakfast").getValue(String.class);

                String sLch = snapshot.child("/servingLunch").getValue(String.class);

                String sDn = snapshot.child("/servingDinner").getValue(String.class);

                Long fountain = snapshot.child("/fountainAllDay").getValue(Long.class);

                if (hBkf != null && mBkf != null) {
                    breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hBkf, mBkf));
                } else {
                    breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                }

                if (hLch != null && mLch != null) {
                    lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hLch, mLch));
                } else {
                    lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                }

                if (hDn != null && mDn != null) {
                    dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hDn, mDn));
                } else {
                    dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                }

                if (sBkf == null) {
                    servingBreakfast.setText("0");
                } else {
                    servingBreakfast.setText(sBkf.trim());
                }

                if (sLch == null) {
                    servingLunch.setText("0");
                } else {
                    servingLunch.setText(sLch.trim());
                }

                if (sDn == null) {
                    servingDinner.setText("0");
                } else {
                    servingDinner.setText(sDn.trim());
                }

                if (fountain == null || fountain == 1) {
                    waterChoiceButton.setText(getString(R.string.all_day));
                } else {
                    waterChoiceButton.setText(getString(R.string.once_a_day));
                    waterChoiceButton.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FeedingScheme.this, "Some of your data could not be retrieved from our database! \n", Toast.LENGTH_SHORT).show();
                Log.println(Log.WARN, "WARNING_firebase", "Could not retrieve some of the data from firebase \n");
                breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
            }
        });
    }

    private String checkForWarnings() throws ParseException {

        long oneHourInMilliseconds = 3600000;

        String warningMessages = "";

        SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        try {
            Date breakfastTime = timeParser.parse(breakfastTimeButton.getText().toString());

            Date lunchTime = timeParser.parse(lunchTimeButton.getText().toString());

            Date dinnerTime = timeParser.parse(dinnerTimeButton.getText().toString());

            assert breakfastTime != null;
            assert lunchTime != null;
            assert dinnerTime != null;

            if (breakfastTime.after(lunchTime) || breakfastTime.after(dinnerTime) || lunchTime.after(dinnerTime)) {
                warningMessages += "The times you set for the meals are not properly ordered! \n\n";
            }

            if (breakfastTime.getTime() / (double) oneHourInMilliseconds + 24 - dinnerTime.getTime() / (double) oneHourInMilliseconds > 12) // time gap between dinner and next day's breakfast
            {
                warningMessages += "There is too much time between dinner and breakfast. This could be dangerous for your pet! \n\n";
            }

            if (lunchTime.getTime() / (double) oneHourInMilliseconds - breakfastTime.getTime() / (double) oneHourInMilliseconds > 12) //time gap between lunch and breakfast of the same day
            {
                warningMessages += "There is too much time between breakfast and lunch. This could be dangerous for your pet! \n\n";
            }

            if (dinnerTime.getTime() / (double) oneHourInMilliseconds - lunchTime.getTime() / (double) oneHourInMilliseconds > 12) {
                warningMessages += "There is too much time between lunch and dinner. This could be dangerous for your pet! \n\n";
            }

        } catch (ParseException e) {
            Log.println(Log.ERROR, "ERR_parsing", e.getMessage());
            e.printStackTrace();
        }

        return warningMessages;
    }

    private void saveFeedingScheme() {

        try {
            if (!checkForWarnings().equals("")) {
                Log.println(Log.INFO, "INFO_userWarning", "Did not save feeding scheme because of the warnings received: \n" + checkForWarnings());

                AlertDialog.Builder builder = new AlertDialog.Builder(FeedingScheme.this, android.R.style.Theme_Holo_Panel);
                builder.setMessage(checkForWarnings());
                builder.setTitle("Could not save feeding scheme because:");
                builder.setPositiveButton("Okay", (dialog, which) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return;
            }
        } catch (ParseException e) {
            Log.println(Log.ERROR, "ERR_parsing", e.getMessage() + "\n");
            e.printStackTrace();
        }

        if (breakfastTimeButtonClicked) // checks if the button has been clicked
        {
            databaseReference.child("/dataFromApp").child("/breakfastHour").setValue(hourBreakfast); //if it was clicked it saves the new time
            databaseReference.child("/dataFromApp").child("/breakfastMinute").setValue(minuteBreakfast);
            breakfastTimeButtonClicked = false;
        }//if the button wasn't clicked it doesn't save anything, bc if it did save it would be 0 for both hour and minutes!!

        if (lunchTimeButtonClicked) {
            databaseReference.child("/dataFromApp").child("/lunchHour").setValue(hourLunch);
            databaseReference.child("/dataFromApp").child("/lunchMinute").setValue(minuteLunch);
            lunchTimeButtonClicked = false;
        }

        if (dinnerTimeButtonClicked) {
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

        Log.println(Log.INFO, "SUCCESS_saved", "Successfully saved feeding schema \n");

    }

    private void backToHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void timePickerBreakfast(Button btn) {
        breakfastTimeButtonClicked = true; //if you've arrived here it means the button has been clicked and so the bool is set to true
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hourBreakfast = hourPicker; //sets the time with what you picked
            minuteBreakfast = minutePicker;
            btn.setText(String.format(Locale.getDefault(), "%02d:%02d", hourBreakfast, minuteBreakfast)); // saves the text to what you picked
        };

        String[] splitPreviouslySetTime = btn.getText().toString().split(":");
        int backupBreakfastH = Integer.parseInt(splitPreviouslySetTime[0]);
        int backupBreakfastM = Integer.parseInt(splitPreviouslySetTime[1]);

        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Dialog, onTimeSetListener, backupBreakfastH, backupBreakfastM, true); // what you see when you open the time picker
        //it is set to the backup values so you don't have to start scrolling from 00:00, but from the last values saved
        timePickerDialog.show(); //shows the ui where you can pick the time
    }

    public void timePickerLunch(Button btn) {
        lunchTimeButtonClicked = true;
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hourLunch = hourPicker;
            minuteLunch = minutePicker;
            btn.setText(String.format(Locale.getDefault(), "%02d:%02d", hourLunch, minuteLunch));
        };

        String[] splitPreviouslySetTime = btn.getText().toString().split(":");
        int backupLunchH = Integer.parseInt(splitPreviouslySetTime[0]);
        int backupLunchM = Integer.parseInt(splitPreviouslySetTime[1]);

        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Dialog, onTimeSetListener, backupLunchH, backupLunchM, true);
        timePickerDialog.show();
    }

    public void timePickerDinner(Button btn) {
        dinnerTimeButtonClicked = true;
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hourDinner = hourPicker;
            minuteDinner = minutePicker;
            btn.setText(String.format(Locale.getDefault(), "%02d:%02d", hourDinner, minuteDinner));
        };

        String[] splitPreviouslySetTime = btn.getText().toString().split(":"); //the previously set hour that would be 0 if nothing was set
        int backupDinnerH = Integer.parseInt(splitPreviouslySetTime[0]);
        int backupDinnerM = Integer.parseInt(splitPreviouslySetTime[1]);

        TimePickerDialog timePickerDialog;
        timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Dialog, onTimeSetListener, backupDinnerH, backupDinnerM, true);
        timePickerDialog.show();
    }

}