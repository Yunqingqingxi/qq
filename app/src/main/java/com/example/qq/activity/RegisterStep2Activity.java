package com.example.qq.activity;

// Android 框架

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.R;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;

import org.json.JSONObject;

/**
 * 注册第二步界面活动类
 * 负责完成用户注册流程，包括：
 * - 邮箱验证
 * - 验证码发送和验证
 * - 用户注册信息提交
 * - 注册成功后跳转到登录界面
 * 
 * @author yunxi
 * @version 1.0
 */
public class RegisterStep2Activity extends AppCompatActivity {
    private static final String TAG = "RegisterStep2Activity";
    private static final int COUNTDOWN_TIME = 60000; // 60秒倒计时

    // UI组件
    private EditText emailInput;
    private EditText verificationCodeInput;
    private Button sendCodeButton;
    private Button registerButton;
    private TextView accountInfoText;

    // 注册信息
    private String nickname;
    private String password;
    private String username;

    // 工具和服务
    private CountDownTimer countDownTimer;
    private UserApi userApi;

    /**
     * 初始化活动
     * 设置布局、获取注册信息、初始化组件
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_step2);

        // 初始化 UserApi
        userApi = new UserApiImpl();

        // 获取上一步传来的数据
        nickname = getIntent().getStringExtra("nickname");
        password = getIntent().getStringExtra("password");
        username = getIntent().getStringExtra("username");

        initViews();
        setupListeners();
    }

    /**
     * 初始化视图组件
     * 设置输入框限制和显示账号信息
     */
    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        verificationCodeInput = findViewById(R.id.verificationCodeInput);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        registerButton = findViewById(R.id.registerButton);
        accountInfoText = findViewById(R.id.accountInfoText);

        // 显示生成的账号
        accountInfoText.setText("您的账号将是: " + username);

        // 设置返回按钮
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // 限制验证码输入长度为6位
        verificationCodeInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        
        // 设置验证码输入框只能输入数字
        verificationCodeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    /**
     * 设置事件监听器
     * 配置发送验证码和注册按钮的点击事件
     */
    private void setupListeners() {
        sendCodeButton.setOnClickListener(v -> sendVerificationCode());
        registerButton.setOnClickListener(v -> validateAndRegister());
    }

    /**
     * 发送验证码
     * 验证邮箱格式并发送验证码，启动倒计时
     */
    private void sendVerificationCode() {
        String email = emailInput.getText().toString().trim();
        if (!isValidEmail(email)) {
            emailInput.setError("请输入有效的邮箱地址");
            return;
        }

        // 禁用发送按钮
        sendCodeButton.setEnabled(false);
        
        // 使用 UserApi 发送验证码
        userApi.sendVerificationCode(email, new UserApi.VerificationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    startCountDown();
                    Toast.makeText(RegisterStep2Activity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    sendCodeButton.setEnabled(true);
                    Toast.makeText(RegisterStep2Activity.this, "发送验证码失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 启动倒计时
     * 禁用发送按钮并显示倒计时
     */
    private void startCountDown() {
        sendCodeButton.setEnabled(false);
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendCodeButton.setText(millisUntilFinished / 1000 + "秒后重试");
            }

            @Override
            public void onFinish() {
                sendCodeButton.setEnabled(true);
                sendCodeButton.setText("发送验证码");
            }
        }.start();
    }

    /**
     * 验证输入并注册
     * 验证邮箱和验证码，执行注册流程
     */
    private void validateAndRegister() {
        String email = emailInput.getText().toString().trim();
        String code = verificationCodeInput.getText().toString().trim();

        if (!isValidEmail(email)) {
            emailInput.setError("请输入有效的邮箱地址");
            return;
        }

        if (code.length() != 6) {
            verificationCodeInput.setError("请输入6位验证码");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在验证...");
        progressDialog.show();

        // 先验证验证码
        userApi.verifyCode(email, code, new UserApi.VerificationCallback() {
            @Override
            public void onSuccess() {
                // 验证码验证成功，继续注册流程
                runOnUiThread(() -> {
                    progressDialog.setMessage("正在注册...");
                    proceedWithRegistration(email, progressDialog);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterStep2Activity.this, 
                        "验证码验证失败：" + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 执行注册流程
     * 提交注册信息到服务器并处理响应
     * @param email 用户邮箱
     * @param progressDialog 进度对话框
     */
    private void proceedWithRegistration(String email, ProgressDialog progressDialog) {
        new Thread(() -> {
            try {
                // 构建注册请求数据
                JSONObject registerData = new JSONObject();
                registerData.put("username", username);    // 生成的10位账号
                registerData.put("nickname", nickname);    // 用户输入的昵称
                registerData.put("password", password);    // 用户输入的密码
                registerData.put("email", email);         // 用户输入的邮箱

                // 调用注册方法
                boolean success = userApi.register(registerData.toString());

                // 在主线程中处理结果
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                        
                        // 跳转到登录页面并传递账号
                        Intent loginIntent = new Intent(this, LoginActivity.class);
                        loginIntent.putExtra("registered_username", username);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        
                        // 结束所有注册相关的Activity
                        finishAffinity();
                    } else {
                        Toast.makeText(this, "注册失败，请检查输入信息或重试", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("RegisterStep2Activity", "注册失败", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "注册失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 验证邮箱格式
     * @param email 要验证的邮箱地址
     * @return 邮箱格式是否有效
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * 活动销毁时的清理工作
     * 取消倒计时器
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
} 