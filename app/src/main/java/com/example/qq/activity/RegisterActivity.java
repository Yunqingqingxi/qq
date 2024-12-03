package com.example.qq.activity;

// Android 框架

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.R;

import java.util.Random;

/**
 * 注册界面活动类
 * 负责用户注册的第一步，包括：
 * - 昵称和密码的输入验证
 * - 自动生成用户名
 * - 密码可见性控制
 * - 服务协议同意确认
 * 
 * @author yunxi
 * @version 1.0
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // UI组件
    private EditText nicknameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private ImageButton togglePassword;
    private ImageButton toggleConfirmPassword;
    private Button nextButton;
    private CheckBox agreeCheckBox;

    // 状态变量
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String generatedUsername;

    /**
     * 初始化活动
     * 设置布局、初始化视图组件、生成用户名
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
        generateUsername();
    }

    /**
     * 初始化视图组件
     * 查找并绑定所有需要的视图组件，设置初始状态
     */
    private void initViews() {
        nicknameInput = findViewById(R.id.nicknameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        nextButton = findViewById(R.id.nextButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);

        // 设置返回按钮
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // 初始状态下禁用下一步按钮
        nextButton.setEnabled(false);
    }

    /**
     * 设置事件监听器
     * 配置密码可见性切换、下一步按钮和协议复选框的监听器
     */
    private void setupListeners() {
        // 密码可见性切换
        togglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility(passwordInput, togglePassword, isPasswordVisible);
        });

        toggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            updatePasswordVisibility(confirmPasswordInput, toggleConfirmPassword, isConfirmPasswordVisible);
        });

        // 下一步按钮
        nextButton.setOnClickListener(v -> validateAndProceed());

        // 协议复选框
        agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> 
            nextButton.setEnabled(isChecked));
    }

    /**
     * 更新密码字段的可见性
     * @param passwordField 密码输入框
     * @param toggleButton 切换按钮
     * @param isVisible 是否可见
     */
    private void updatePasswordVisibility(EditText passwordField, ImageButton toggleButton, boolean isVisible) {
        if (isVisible) {
            passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.ic_password_visible);
        } else {
            passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.ic_password_invisible);
        }
        passwordField.setSelection(passwordField.getText().length());
    }

    /**
     * 生成随机用户名
     * 生成10位随机数字作为用户名
     */
    private void generateUsername() {
        // 生成10位随机数字作为用户名
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }
        generatedUsername = sb.toString();
    }

    /**
     * 验证输入并进入下一步
     * 检查昵称和密码的有效性，验证通过后跳转到下一步
     */
    private void validateAndProceed() {
        String nickname = nicknameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // 验证输入
        if (nickname.isEmpty()) {
            nicknameInput.setError("请输入昵称");
            return;
        }

        // 验证昵称长度
        if (nickname.length() < 2 || nickname.length() > 20) {
            nicknameInput.setError("昵称长度应在2-20个字符之间");
            return;
        }

        // 验证密码复杂度
        if (!isPasswordValid(password)) {
            passwordInput.setError("密码必须包含数字、字母和特殊字符，长度8-20位");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("两次输入的密码不一致");
            return;
        }

        if (!agreeCheckBox.isChecked()) {
            Toast.makeText(this, "请阅读并同意服务协议", Toast.LENGTH_SHORT).show();
            return;
        }

        // 直接进入下一步
        Intent intent = new Intent(this, RegisterStep2Activity.class);
        intent.putExtra("nickname", nickname);
        intent.putExtra("password", password);
        intent.putExtra("username", generatedUsername);
        startActivity(intent);
    }

    /**
     * 验证密码是否符合要求
     * 密码必须包含数字、字母和特殊字符，长度8-20位
     * @param password 要验证的密码
     * @return 密码是否有效
     */
    private boolean isPasswordValid(String password) {
        // 密码必须包含数字、字母和特殊字符，长度8-20位
        String pattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,20}$";
        return password.matches(pattern);
    }
} 