package com.example.qq;

import static com.example.qq.websocket.webUtils.controller.WebUtil.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.DAO.UserDAO;
import com.example.qq.websocket.domain.Message;
import com.example.qq.websocket.web.WebClient;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.GetNowUser;
import com.example.qq.websocket.webUtils.controller.Callback;
import com.example.qq.websocket.webUtils.controller.WebUtil;

import java.util.List;
import java.util.Map;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class LoginActivity extends AppCompatActivity {

    private EditText qqNumberEditText;
    private EditText qqPasswordEditText;
    private ImageView loginButton;
    private CheckBox agreeCheckBox;
    private TextView forgotPasswordTextView;
    private UserDAO userDAO;
    private WebClient webClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化WebSocket客户端
        webClient = WebClient.getInstance();

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
                    if (!agreeCheckBox.isChecked()) {
                        Toast.makeText(LoginActivity.this, "请先同意用户协议", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    startLogin(qqNumber,qqPassword);

//                    if (userDAO.validateUser(qqNumber, qqPassword)) {
//                        // 登录成功
//                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
//                        // 这里可以添加登录成功后的操作，比如跳转到主界面
//                    } else {
//                        // 登录失败
//                        Toast.makeText(LoginActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    // 提示用户输入账号和密码
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
    public void startLogin(String username,String password){
        // 登录的逻辑
        login(username, password, new Callback() {
            @Override
            public void onResult(WebResult<Map<String, Object>> result) {
                System.out.println(result);
                if (result.getCode() == 200) {
                    // 登录成功
                    String token = (String) result.getData().get("token");
                    // 保存token到本地
                    SharedPreferences sharedPreferences = getSharedPreferences("MyRefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", token);
                    editor.putString("current_username", username); // 保存用户名

                    editor.apply(); // 或使用 commit() 方法

                    // 创建 GetNowUser 实例
                    GetNowUser getNowUser = new GetNowUser(/* 获取当前界面 */ LoginActivity.this);

                    // 设置当前用户名
                    getNowUser.setCurrentUsername(username);

                    // Connect to WebSocket after successful login
                    webClient.connect(token, new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket webSocket, @NonNull okhttp3.Response response) {
                            // Send online status to the server
                            sendOnline(username);
                        }

                        @Override
                        public void onMessage(WebSocket webSocket, String text) {
                            // Handle incoming messages

                        }

                        // Override other WebSocketListener methods as needed
                        @Override
                        public void onClosing(WebSocket webSocket, int code, String reason) {
                            // Handle WebSocket closing

                        }
                    });
                    Intent intent = new Intent(LoginActivity.this, FrameActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    // 登录失败
                    System.out.println("Error: " + result.getMessage());
                    Toast.makeText(LoginActivity.this, "账号或者密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Send online message to WebSocket server
    private void sendOnline(String username) {
        WebSocket webSocket = webClient.getWebSocket();
        if (webSocket != null) {
            // 创建系统消息对象
            Message onlineMessage = new Message(0, "system", null, username + "上线");
            // 将消息转换为 JSON 格式
            String jsonMessage = onlineMessage.toJson().toString();
            // 发送消息
            webSocket.send(jsonMessage);
        }
    }
}