package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class MainActivity2 extends AppCompatActivity {

    private boolean passwordVisible = false;
    ProgressDialog dialog;
    EditText id, mail;
    JSONParser parser = new JSONParser();
    int success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        mail = findViewById(R.id.editTextText);
        id = findViewById(R.id.editTextText1);
        Button buttonn = findViewById(R.id.mon_bouton);

        // Gestion des insets pour les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        buttonn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, dashbord.class);
                startActivity(intent);
            }
        });


        // Gestion de la visibilité du mot de passe
        id.setOnTouchListener((view, event) -> {
            final int RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (id.getCompoundDrawables()[RIGHT] != null &&
                        event.getRawX() >= (id.getRight() - id.getCompoundDrawables()[RIGHT].getBounds().width())) {
                    int selection = id.getSelectionEnd();
                    if (passwordVisible) {
                        id.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.visibilite_off, 0);
                        id.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    } else {
                        id.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.password_icone, 0);
                        id.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }
                    passwordVisible = !passwordVisible;
                    id.setSelection(selection);
                    return true;
                }
            }
            return false;
        });


    }



}
