package com.example.snackinator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button validate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        validate = findViewById(R.id.validate);
        validate.setOnClickListener(this::validateCode);
    }

    public void validateCode(View v)
    {
        Intent intent = new Intent(this,FeedingScheme.class);
        startActivity(intent);
    }

}