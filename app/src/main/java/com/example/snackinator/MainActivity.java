package com.example.snackinator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button validate;
    TextView code;

    final String databaseLink = "https://snackinator-lic-default-rtdb.europe-west1.firebasedatabase.app";

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(databaseLink);
    private final DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        code = findViewById(R.id.code);

        validate = findViewById(R.id.validate);
        validate.setOnClickListener(this::validateCode);
    }

    public void validateCode(View v)
    {
        boolean codeContainsForbiddenSymbols = code.getText().toString().contains(".") || code.getText().toString().contains("#") ||
                code.getText().toString().contains("$") || code.getText().toString().contains("[") || code.getText().toString().contains("]");

        if(codeContainsForbiddenSymbols)
        {
            code.setText("");
        }

        databaseReference.child("SnackInators").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(code.getText().toString()) || code.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this, "The code you introduced is invalid. Please try again! \n", Toast.LENGTH_SHORT).show();
                    code.setText("");
                    Log.i( "INFO_userWarning", "Did not find the introduced code in Firebase \n");
                }
                else
                {
                    Intent intent = new Intent(MainActivity.this,FeedingScheme.class);
                    intent.putExtra("CODE",code.getText().toString().trim());
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "We encountered an error when verifying your code. Please try again! \n", Toast.LENGTH_SHORT).show();
                Log.w( "WARNING_firebase", "Could not retrieve data from firebase to verify user-introduced code \n");
            }
        });

    }

}