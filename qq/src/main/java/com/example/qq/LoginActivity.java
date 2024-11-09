package com.example.qq;

import static com.example.qq.websocket.webUtils.controller.WebUtil.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.websocket.WebSocketManager;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.GetNowUser;
import com.example.qq.websocket.webUtils.controller.Callback;

import java.util.Map;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class LoginActivity extends AppCompatActivity {

    private EditText qqNumberEditText;
    private EditText qqPasswordEditText;
    private ImageView loginButton;
    private CheckBox agreeCheckBox;
    private TextView forgotPasswordTextView;
    private ProgressBar loadingProgress;
    private WebClient webClient;
    private Handler mainHandler; // 声明 mainHandler
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化WebSocket客户端
        webClient = WebClient.getInstance();

        qqNumberEditText = findViewById(R.id.qqNumber);
        qqPasswordEditText = findViewById(R.id.qqPassword);
        loginButton = findViewById(R.id.loginButton);
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        forgotPasswordTextView = findViewById(R.id.forgotPassword);
        loadingProgress = findViewById(R.id.loadingProgress);

        // 初始化主线程的Handler
        mainHandler = new Handler(Looper.getMainLooper());

        // 从注册页面接收账号
        if (getIntent().hasExtra("account")) {
            String account = getIntent().getStringExtra("account");
            qqNumberEditText.setText(account);
        }

        checkLoginStatus();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qqNumber = qqNumberEditText.getText().toString();
                String qqPassword = qqPasswordEditText.getText().toString();
                if (!qqNumber.isEmpty() && !qqPassword.isEmpty()) {
                    if (!agreeCheckBox.isChecked()) {
                        Toast.makeText(LoginActivity.this, "请先同意用户协议", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showLoading(true);
                    startLogin(qqNumber, qqPassword);
                } else {
                    Toast.makeText(LoginActivity.this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
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

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        String username = sharedPreferences.getString("current_username", null);

        if (token != null && username != null && webClient.getWebSocket() != null) {
            showLoading(true);
            // 用户已登录且WebSocket连接活跃，跳转到FrameActivity
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoginActivity.this, FrameActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                }
            }, 1500); // 延迟1.5秒，给加载动画时间
        } else {
            // 用户未登录或WebSocket连接不活跃，隐藏加载动画
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showLoading(false);
                }
            }, 2000); // 延迟1.5秒，给加载动画时间
        }
    }

    public void startLogin(String username, String password) {
        // 登录的逻辑
        login(username, password, new Callback() {
            @Override
            public void onResult(WebResult<Map<String, Object>> result) {
                handleLoginResult(result, username);
            }
        });
    }

    private void handleLoginResult(WebResult<Map<String, Object>> result, String username) {
        if (result.getCode() == 200) {
            handleLoginSuccess(result, username);
        } else {
            handleLoginFailure(result);
        }
    }

    private void handleLoginSuccess(WebResult<Map<String, Object>> result, String username) {
        String token = (String) result.getData().get("token");
        saveCredentials(username, token);
        connectWebSocket(username, token);
        navigateToFrameActivity(username);
    }

    private void handleLoginFailure(WebResult<Map<String, Object>> result) {
        String errorMessage = result.getMessage() != null ? result.getMessage() : "账号或者密码错误";
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        showLoading(false);
    }

    private void saveCredentials(String username, String token) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("current_username", username);
        editor.apply();
    }

    private void connectWebSocket(String username, String token) {
        webClient.connect(token, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, @NonNull okhttp3.Response response) {
                sendOnline(username);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Handle incoming messages
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                // Handle WebSocket closing
            }
        });
    }

    private void navigateToFrameActivity(String username) {
        Intent intent = new Intent(LoginActivity.this, FrameActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            // 通过布局文件加载View
            loadingView = getLayoutInflater().inflate(R.layout.layout_loading, null);
            // 创建WindowManager.LayoutParams
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION; // 设置类型
            params.format = PixelFormat.RGBA_F16; // 设置图片格式，透明背景
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 设置FLAG_NOT_FOCUSABLE
            params.gravity = Gravity.CENTER; // 设置位置居中
            params.width = WindowManager.LayoutParams.MATCH_PARENT; // 设置宽度
            params.height = WindowManager.LayoutParams.MATCH_PARENT; // 设置高度
            params.dimAmount = 0.5f; // 设置背后窗口的暗度，如果需要的话

            // 获取WindowManager服务
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            // 显示加载动画
            windowManager.addView(loadingView, params);
        } else {
            // 移除加载动画
            if (loadingView != null) {
                WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                windowManager.removeView(loadingView);
                loadingView = null;
            }
        }
    }
    private void sendOnline(String username) {
        WebSocket webSocket = webClient.getWebSocket();
        if (webSocket != null) {
            Message onlineMessage = new Message(0, "system", null, username + "上线");
            String jsonMessage = onlineMessage.toJson().toString();
            webSocket.send(jsonMessage);
        }
    }
}