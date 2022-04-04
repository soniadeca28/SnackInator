package com.example.snackinator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FeedingScheme extends AppCompatActivity {

    Button breakfastTimeButton, lunchTimeButton, dinnerTimeButton, backToHomeButton, waterChoiceButton;
    int hour,minute;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_scheme);
        breakfastTimeButton = findViewById(R.id.breakfastTime);
        breakfastTimeButton.setOnClickListener(v->timePicker(v,breakfastTimeButton));

        lunchTimeButton = findViewById(R.id.lunchTime);
        lunchTimeButton.setOnClickListener(v->timePicker(v,lunchTimeButton));

        dinnerTimeButton = findViewById(R.id.dinnerTime);
        dinnerTimeButton.setOnClickListener(v->timePicker(v,dinnerTimeButton));
        
        backToHomeButton = findViewById(R.id.backButton);
        backToHomeButton.setOnClickListener(v->backToHomePage());

        waterChoiceButton = findViewById(R.id.waterChoice);
    }

    private void backToHomePage() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void timePicker(View view, Button btn)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hour = hourPicker;
            minute = minutePicker;
            btn.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
        };
        int timePickerStyle = AlertDialog.THEME_HOLO_DARK; // !!!!!!!!!! deprecated
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,timePickerStyle,onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle("00:00");
        timePickerDialog.show();
    }
}