package com.example.qq.activity;

import static com.example.qq.utils.JsonParser.parseToJson;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.qq.R;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.utils.SharedPreferencesManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private EditText qqNumber;
    private EditText qqPassword;
    private CheckBox agreeCheckBox;
    private ImageButton togglePassword;
    private RelativeLayout loadingContainer;
    private View qqNumberContainer;
    private View passwordContainer;
    private final UserApiImpl userApi = new UserApiImpl();
    
    // 防止重复点击的时间间隔（毫秒）
    private static final long CLICK_INTERVAL = 3000;
    private long lastClickTime = 0;
    
    // 用户名和密码的正则表达式
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,16}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@#$%^&*]{6,20}$");

    // 添加登录超时时间常量
    private static final long LOGIN_TIMEOUT = 15000; // 15秒超时
    private Handler timeoutHandler;
    private Thread loginThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        timeoutHandler = new Handler(Looper.getMainLooper());
        initViews();
        setupListeners();
        
        // 恢复保存的用户名
        String savedUsername = SharedPreferencesManager.getInstance().getSavedUsername();
        if (!TextUtils.isEmpty(savedUsername)) {
            qqNumber.setText(savedUsername);
        }
    }

    private void initViews() {
        qqNumber = findViewById(R.id.qqNumber);
        qqPassword = findViewById(R.id.qqPassword);
        loginButton = findViewById(R.id.loginButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        togglePassword = findViewById(R.id.togglePassword);
        loadingContainer = findViewById(R.id.loadingContainer);
        qqNumberContainer = findViewById(R.id.qqNumberContainer);
        passwordContainer = findViewById(R.id.passwordContainer);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> {
            if (canClickButton()) {
                startLogin();
            }
        });

        togglePassword.setOnClickListener(v -> {
            if (qqPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                qqPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_password_invisible);
            } else {
                qqPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_password_visible);
            }
            qqPassword.setSelection(qqPassword.getText().length());
        });

        findViewById(R.id.forgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loginButton.setEnabled(isChecked);
            if (isChecked) {
                loginButton.setBackgroundResource(R.drawable.button_background);
            } else {
                loginButton.setBackgroundResource(R.drawable.button_background_disabled);
            }
        });

        qqNumber.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                qqNumberContainer.setAlpha(0.8f);
            } else {
                qqNumberContainer.setAlpha(1.0f);
            }
        });

        qqPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordContainer.setAlpha(0.8f);
            } else {
                passwordContainer.setAlpha(1.0f);
            }
        });
    }

    private boolean canClickButton() {
        if (!agreeCheckBox.isChecked()) {
            Toast.makeText(this, "请先同意服务协议和隐私保护指引", Toast.LENGTH_SHORT).show();
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_INTERVAL) {
            Toast.makeText(this, "请勿频繁点击", Toast.LENGTH_SHORT).show();
            return false;
        }
        lastClickTime = currentTime;

        return true;
    }

    private void startLogin() {
        String username = qqNumber.getText().toString().trim();
        String password = qqPassword.getText().toString().trim();
        
        if (!validateInput(username, password)) {
            return;
        }

        // 隐藏键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (focus != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        // 显示加载动画
        loadingContainer.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);

        // 设置登录超时
        timeoutHandler.postDelayed(() -> {
            if (loginThread != null && loginThread.isAlive()) {
                loginThread.interrupt();
                handleLoginFailure("登录超时，请检查网络后重试");
            }
        }, LOGIN_TIMEOUT);

        // 开始登录
        loginThread = new Thread(() -> {
            try {
                boolean success = userApi.login(Objects.requireNonNull(parseToJson(map)).toString());
                
                // 检查线程是否被中断
                if (Thread.interrupted()) {
                    return;
                }

                // 移除超时回调
                timeoutHandler.removeCallbacksAndMessages(null);
                
                runOnUiThread(() -> {
                    if (success) {
                        handleLoginSuccess(username);
                    } else {
                        handleLoginFailure("账号或密码错误");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // 检查具体的异常类型
                String errorMessage = getErrorMessage(e);
                runOnUiThread(() -> handleLoginFailure(errorMessage));
            }
        });
        loginThread.start();
    }

    private void handleLoginSuccess(String username) {
        loadingContainer.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        SharedPreferencesManager.getInstance().saveUsername(username);
        
        // 使用渐变动画
        loginButton.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                startMainActivity();
            })
            .start();
    }

    private void handleLoginFailure(String message) {
        loadingContainer.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        qqPassword.setText("");
        
        // 显示错误提示，带有震动效果
        loginButton.animate()
            .translationX(20f)
            .setDuration(100)
            .withEndAction(() -> {
                loginButton.animate()
                    .translationX(-20f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        loginButton.animate()
                            .translationX(0f)
                            .setDuration(100)
                            .start();
                    })
                    .start();
            })
            .start();
            
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof java.net.SocketTimeoutException) {
            return "连接服务器超时，请检查网络";
        } else if (e instanceof java.net.UnknownHostException) {
            return "无法连接到服务器，请检查网络";
        } else if (e instanceof java.net.ConnectException) {
            return "连接被拒绝，请稍后重试";
        } else {
            return "网络错误，请稍后重试";
        }
    }

    private boolean validateInput(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            Toast.makeText(this, "账号格式不正确（4-16位字母数字）", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toast.makeText(this, "密码格式不正确（6-20位字母数字特殊字符）", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginButton.setEnabled(agreeCheckBox.isChecked());
        qqPassword.setText("");
        loadingContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理超时处理器
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacksAndMessages(null);
        }
        // 中断登录线程
        if (loginThread != null && loginThread.isAlive()) {
            loginThread.interrupt();
        }
    }

    @Override
    public void onBackPressed() {
        if (loadingContainer.getVisibility() == View.VISIBLE) {
            // 取消登录
            if (loginThread != null && loginThread.isAlive()) {
                loginThread.interrupt();
            }
            loadingContainer.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            Toast.makeText(this, "已取消登录", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }
}