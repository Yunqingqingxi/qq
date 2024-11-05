package com.example.qq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.DAO.UserDAO;

public class LoginActivity extends AppCompatActivity {

    private EditText qqNumberEditText;
    private EditText qqPasswordEditText;
    private ImageView loginButton;
    private CheckBox agreeCheckBox;
    private TextView forgotPasswordTextView;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDAO = new UserDAO(this);

        qqNumberEditText = findViewById(R.id.qqNumber);
        qqPasswordEditText = findViewById(R.id.qqPassword);
        loginButton = findViewById(R.id.loginButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);

        // 从注册页面接收账号
        if (getIntent().hasExtra("account")) {
            String account = getIntent().getStringExtra("account");
            qqNumberEditText.setText(account);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qqNumber = qqNumberEditText.getText().toString();
                String qqPassword = qqPasswordEditText.getText().toString();

                if (!qqNumber.isEmpty() && !qqPassword.isEmpty()) {
                    // 验证账号和密码
                    if (userDAO.validateUser(qqNumber, qqPassword)) {
                        // 登录成功
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        // 这里可以添加登录成功后的操作，比如跳转到主界面
                    } else {
                        // 登录失败
                        Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 提示用户输入账号和密码
                }
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                // 设置点击后的颜色，需要在res/values/colors.xml中定义clicked_text_color
                forgotPasswordTextView.setTextColor(getResources().getColor(R.color.clicked_text_color));
            }
        });
    }
}