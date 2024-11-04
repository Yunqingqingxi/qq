package com.example.qq;

import static com.example.qq.websocket.webUtils.controller.WebUtil.login;
import static com.example.qq.websocket.webUtils.controller.WebUtil.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

import com.example.qq.websocket.db.FriendDatabaseHelper;
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

public class MainActivity extends BaseActivity {
    private Button loginButton;
    private WebClient webClient;
    private String username;
    private String password;
    private CheckBox checkBoxAgreement;
    private Button buttonAddAccount;
    private Button buttonRegister;
    private String targetName;
    private EditText qqNumber;
    private EditText qqPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化WebSocket客户端
        webClient = WebClient.getInstance();


        // 初始化界面
        initView();

        // 初始话事件
        initEvent();

    }

    private void initEvent() {

            // 登录按钮的点击事件
        loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBoxAgreement.isChecked()) { // 用户已同意协议，执行登录操作

                        // 获取用户名，等真正登录
                        username = qqNumber.getText().toString();
                        password = qqPassword.getText().toString();
//                        Map<String, String> params = new HashMap<>();
//                        params.put("username", username);
//                        params.put("password", password);
                        // 向"http://localhost:8080/api/login"发送登录请求
                        startLogin(username,password);

                    } else {
                        Toast.makeText(MainActivity.this, "请先同意协议", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            // 添加按钮的点击事件
            buttonAddAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 添加账号的逻辑
                    Toast.makeText(MainActivity.this, "添加账号", Toast.LENGTH_SHORT).show();
                }
            });

            // 注册按钮的点击事件
            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 注册的逻辑
                    username = "123456";
                    password = "123456";
                    register(username, password, new Callback() {

                        @Override
                        public void onResult(WebResult<Map<String, Object>> result) {
                            if (result.getCode() == 200) {
                                // 注册成功
                                Toast.makeText(MainActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                startLogin(username,password);
                            }else {
                                // 注册失败
                                Toast.makeText(MainActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Toast.makeText(MainActivity.this, "注册", Toast.LENGTH_SHORT).show();
                }
            });

        }


    public void initView() {
        qqNumber = findViewById(R.id.qqNumber);
        qqPassword = findViewById(R.id.qqPassword);
        checkBoxAgreement = findViewById(R.id.checkBoxAgreement);
        loginButton = findViewById(R.id.buttonLogin);
        buttonAddAccount = findViewById(R.id.buttonAddAccount);
        buttonRegister = findViewById(R.id.buttonRegister);
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
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("token", token);
                    editor.putString("username", username); // 保存用户名

                    editor.apply(); // 或使用 commit() 方法

                    // 创建 GetNowUser 实例
                    GetNowUser getNowUser = new GetNowUser(MainActivity.this);

                    // 设置当前用户名
                    getNowUser.setCurrentUsername(username);

                    WebUtil.getFriendList(username, token, new Callback() {
                        @Override
                        public void onResult(WebResult<Map<String, Object>> result) {
                            if (result.getCode() == 200) {
                                // 获取好友列表成功
                                List<Map<String, Object>> friends = (List<Map<String, Object>>) result.getData().get("friends");
                                // 处理好友列表数据
                                Log.i("friends", friends.toString());
                            }
                        }
                    });

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
                    Intent intent = new Intent(MainActivity.this, FrameActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                } else {
                    // 登录失败
                    System.out.println("Error: " + result.getMessage());
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