package com.example.qq.activity;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.R;
import com.example.qq.network.RequestManager;
import com.example.qq.utils.SharedPreferencesManager;

public class RegisterActivity extends AppCompatActivity {
    private EditText qqNumber;      // 昵称输入框
    private EditText qqPassword;    // 密码输入框
    private ImageView loginButton;  // 注册按钮
    private CheckBox agreeCheckBox; // 同意协议复选框
    private ImageView togglePassword; // 密码可见性切换按钮

    private RequestManager requestManager;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

//        requestManager = RequestManager.getInstance();
        prefsManager = SharedPreferencesManager.getInstance();

        initViews();
        setupListeners();
    }

    private void initViews() {
        qqNumber = findViewById(R.id.qqNumber);
        qqPassword = findViewById(R.id.qqPassword);
        loginButton = findViewById(R.id.loginButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        togglePassword = findViewById(R.id.togglePassword);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> {
            if (!agreeCheckBox.isChecked()) {
                Toast.makeText(this, "请先同意服务协议", Toast.LENGTH_SHORT).show();
                return;
            }
            attemptRegister();
        });

        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void attemptRegister() {
        String nickname = qqNumber.getText().toString().trim();
        String password = qqPassword.getText().toString().trim();

        // 输入验证
        if (TextUtils.isEmpty(nickname)) {
            qqNumber.setError("请输入昵称");
            qqNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            qqPassword.setError("请输入密码");
            qqPassword.requestFocus();
            return;
        }

        // TODO: 实现实际的注册逻辑
        finish();
    }

    private void togglePasswordVisibility() {
        if (qqPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // 显示密码
            qqPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_password_visible);
        } else {
            // 隐藏密码
            qqPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_password_invisible);
        }
        // 保持光标在文本末尾
        qqPassword.setSelection(qqPassword.getText().length());
    }
} 