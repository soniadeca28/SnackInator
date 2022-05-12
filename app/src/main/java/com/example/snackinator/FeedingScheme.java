package com.example.snackinator;

import static java.lang.Math.abs;

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
import java.util.TimeZone;

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
    private String CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_scheme);

        Bundle info = getIntent().getExtras();
        CODE = info.getString("CODE");

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
        databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.hasChildren()) {
                    databaseReference.child("SnackInators").child(CODE).child("/Exists").setValue(true);
                }

                Long hBkf = snapshot.child("/breakfastHour").getValue(Long.class);
                Long mBkf = snapshot.child("/breakfastMinute").getValue(Long.class);

                Long hLch = snapshot.child("/lunchHour").getValue(Long.class);
                Long mLch = snapshot.child("/lunchMinute").getValue(Long.class);

                Long hDn = snapshot.child("/dinnerHour").getValue(Long.class);
                Long mDn = snapshot.child("/dinnerMinute").getValue(Long.class);

                Integer sBkf = snapshot.child("/servingBreakfast").getValue(Integer.class);

                Integer sLch = snapshot.child("/servingLunch").getValue(Integer.class);

                Integer sDn = snapshot.child("/servingDinner").getValue(Integer.class);

                Long fountain = snapshot.child("/fountainAllDay").getValue(Long.class);

                if (hBkf != null && mBkf != null) {
                    breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hBkf, mBkf));
                    hourBreakfast = Math.toIntExact(hBkf); minuteBreakfast = Math.toIntExact(mBkf);
                } else {
                    breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                }

                if (hLch != null && mLch != null) {
                    lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hLch, mLch));
                    hourLunch = Math.toIntExact(hLch); minuteLunch = Math.toIntExact(mLch);
                } else {
                    lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                }

                if (hDn != null && mDn != null) {
                    hourDinner = Math.toIntExact(hDn); minuteDinner = Math.toIntExact(mDn);
                    dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hDn, mDn));
                } else {
                    dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                }

                if (sBkf == null) {
                    servingBreakfast.setText("0");
                } else {
                    servingBreakfast.setText(String.valueOf(sBkf));
                }

                if (sLch == null) {
                    servingLunch.setText("0");
                } else {
                    servingLunch.setText(String.valueOf(sLch));
                }

                if (sDn == null) {
                    servingDinner.setText("0");
                } else {
                    servingDinner.setText(String.valueOf(sDn));
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
                Log.println(Log.ERROR, "WARNING_firebase", "Could not retrieve some of the data from firebase to update data in Feeding Scheme\n");
                breakfastTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                lunchTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
                dinnerTimeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", 0, 0));
            }
        });
    }

    private String checkForWarnings() throws ParseException{

        String warning = "";

        long oneHourInMilliseconds = 3600000;

        SimpleDateFormat timeParser = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        timeParser.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date breakfastTime = timeParser.parse(breakfastTimeButton.getText().toString().trim());

            Date lunchTime = timeParser.parse(lunchTimeButton.getText().toString().trim());

            Date dinnerTime = timeParser.parse(dinnerTimeButton.getText().toString().trim());

            assert breakfastTime != null;
            assert lunchTime != null;
            assert dinnerTime != null;

        boolean importantFieldsLeft = false;

        boolean wrongTimeSet = false;

        boolean breakfastDinnerNotSet = false;

        boolean nothingSet = (servingBreakfast.getText().toString().trim().isEmpty() || servingBreakfast.getText().toString().trim().equals(getString(R.string._0)))
                && (servingLunch.getText().toString().trim().isEmpty() || servingLunch.getText().toString().trim().equals(getString(R.string._0)))
                && (servingDinner.getText().toString().trim().isEmpty() || servingDinner.getText().toString().trim().equals(getString(R.string._0)))
        // and all servings are still default values
        && breakfastTimeButton.getText().toString().equals(lunchTimeButton.getText().toString())
                && lunchTimeButton.getText().toString().equals(dinnerTimeButton.getText().toString())
                && dinnerTimeButton.getText().toString().equals(breakfastTimeButton.getText().toString())
                && breakfastTimeButton.getText().toString().equals("00:00");

        boolean noServingSet = (servingBreakfast.getText().toString().trim().isEmpty() || servingBreakfast.getText().toString().trim().equals(getString(R.string._0)))
                && (servingLunch.getText().toString().trim().isEmpty() || servingLunch.getText().toString().trim().equals(getString(R.string._0)))
                && (servingDinner.getText().toString().trim().isEmpty() || servingDinner.getText().toString().trim().equals(getString(R.string._0)));
        // and all servings are still default values

        boolean allMealsSameTime = breakfastTimeButton.getText().toString().equals(lunchTimeButton.getText().toString())
                && lunchTimeButton.getText().toString().equals(dinnerTimeButton.getText().toString())
                && dinnerTimeButton.getText().toString().equals(breakfastTimeButton.getText().toString());

        boolean breakfastOrDinnerNotSet = servingBreakfast.getText().toString().trim().equals(getString(R.string._0)) || servingDinner.getText().toString().trim().equals(getString(R.string._0)) ||
                servingBreakfast.getText().toString().trim().isEmpty() || servingDinner.getText().toString().trim().isEmpty();

        boolean lunchIsSet = !servingLunch.getText().toString().trim().equals(getString(R.string._0)) && !servingLunch.getText().toString().trim().isEmpty();

        boolean gapTooBigWithLunch = lunchIsSet
                && (((breakfastTime.getTime() - dinnerTime.getTime())/oneHourInMilliseconds > -12 && breakfastTime.getTime() < dinnerTime.getTime())
                || ((breakfastTime.getTime() - dinnerTime.getTime())/oneHourInMilliseconds > 12 && breakfastTime.getTime() > dinnerTime.getTime())
                || (lunchTime.getTime() - breakfastTime.getTime())/oneHourInMilliseconds > 12);

        boolean gapTooBigWithoutLunch = !lunchIsSet && ((dinnerTime.getTime() - breakfastTime.getTime())/oneHourInMilliseconds != 12);

        boolean sameHourWithLunch = lunchIsSet && ((abs(breakfastTime.getTime() - dinnerTime.getTime())/oneHourInMilliseconds < 1)
                || (abs(lunchTime.getTime() - breakfastTime.getTime())/oneHourInMilliseconds < 1)
                || (abs(dinnerTime.getTime() - lunchTime.getTime())/oneHourInMilliseconds < 1));

        if(nothingSet)
        {
            importantFieldsLeft = true;
        }
        else
        {
            if(noServingSet)
            {
                importantFieldsLeft = true;
            }

            if(allMealsSameTime)
            {
                wrongTimeSet = true;
            }

            if(breakfastOrDinnerNotSet)
            {
                breakfastDinnerNotSet = true;
            }

            if(gapTooBigWithLunch || gapTooBigWithoutLunch)
            {
                wrongTimeSet = true;
            }

            if(sameHourWithLunch)
            {
                wrongTimeSet = true;
            }
        }

        if(importantFieldsLeft)
        {
            warning += "All mandatory fields must be completed!\n";
        }
        else if(wrongTimeSet)
        {
            warning += "Meals need to be set at least 1 hour apart with a maximum of 12 hours between them!\n";
        }
        else if(breakfastDinnerNotSet)
        {
            warning += "Only lunch is optional, breakfast and dinner are mandatory!\n";
        }

        } catch (ParseException e) {
            Log.println(Log.ERROR, "ERR_parsing", e.getMessage());
            e.printStackTrace();
        }

        return warning;

    }

    private void saveFeedingScheme() {

        try {
            if(!checkForWarnings().isEmpty())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(FeedingScheme.this, android.R.style.Theme_Holo_Panel);
                builder.setMessage(checkForWarnings());
                builder.setTitle("Could not save feeding scheme because:");
                builder.setPositiveButton("Okay", (dialog, which) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                Log.println(Log.INFO, "INFO_userWarning", "Did not save feeding schema due to warnings\n");
                return;
            }
        } catch (ParseException e) {
            Log.println(Log.ERROR, "ERROR_parse", "Error parsing time\n");
            e.printStackTrace();
        }

        if (breakfastTimeButtonClicked) // checks if the button has been clicked
        {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/breakfastHour").setValue(hourBreakfast); //if it was clicked it saves the new time
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/breakfastMinute").setValue(minuteBreakfast);
            breakfastTimeButtonClicked = false;
        }//if the button wasn't clicked it doesn't save anything, bc if it did save it would be 0 for both hour and minutes!!

        if (lunchTimeButtonClicked) {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/lunchHour").setValue(hourLunch);
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/lunchMinute").setValue(minuteLunch);
            lunchTimeButtonClicked = false;
        }

        if (dinnerTimeButtonClicked) {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/dinnerHour").setValue(hourDinner);
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/dinnerMinute").setValue(minuteDinner);
            dinnerTimeButtonClicked = false;
        }

        if (waterChoiceButton.getText().toString().trim().equals("All day")) {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/fountainAllDay").setValue(1);
        } else {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/fountainAllDay").setValue(0);
        }

        databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/servingBreakfast").setValue(Integer.parseInt(servingBreakfast.getText().toString().trim()));

        if (servingLunch.getText().toString().trim().isEmpty()) {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/servingLunch").setValue(0);
        } else {
            databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/servingLunch").setValue(Integer.parseInt(servingLunch.getText().toString().trim()));
        }

        databaseReference.child("SnackInators").child(CODE).child("/dataFromApp").child("/servingDinner").setValue(Integer.parseInt(servingDinner.getText().toString().trim()));

        Toast.makeText(FeedingScheme.this, "Saved successfully! \n", Toast.LENGTH_SHORT).show();

        Log.println(Log.INFO, "SUCCESS_saved", "Successfully saved feeding schema \n");

    }

    private void backToHomePage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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