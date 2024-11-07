package com.example.qq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;



import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private EditText qqNumberEditText;
    private EditText qqPasswordEditText;
    private ImageView loginButton;
    private CheckBox agreeCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        qqNumberEditText = findViewById(R.id.qqNumber);
        qqPasswordEditText = findViewById(R.id.qqPassword);
        loginButton = findViewById(R.id.loginButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (agreeCheckBox.isChecked()) {
                    String qqNumber = qqNumberEditText.getText().toString();
                    String qqPassword = qqPasswordEditText.getText().toString();

                    if (!qqNumber.isEmpty() && !qqPassword.isEmpty()) {
                        // 生成随机9位数account
                        String account = String.format("%09d", new Random().nextInt(900000000) + 100000000);
                    }
                }
            }
        });
    }
}