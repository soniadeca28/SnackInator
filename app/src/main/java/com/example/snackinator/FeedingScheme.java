package com.example.snackinator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Locale;

public class FeedingScheme extends AppCompatActivity {

    Button breakfastTimeButton;
    int hour,minute;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding_scheme);
        breakfastTimeButton = findViewById(R.id.breakfastTime);
        breakfastTimeButton.setOnClickListener(this::timePicker);
    }

    public void timePicker(View view)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (view1, hourPicker, minutePicker) -> {
            hour = hourPicker;
            minute = minutePicker;
            breakfastTimeButton.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
        };
        int timePickerStyle = AlertDialog.THEME_HOLO_DARK;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,timePickerStyle,onTimeSetListener,hour,minute,true);
        timePickerDialog.setTitle("00:00");
        timePickerDialog.show();
    }
}