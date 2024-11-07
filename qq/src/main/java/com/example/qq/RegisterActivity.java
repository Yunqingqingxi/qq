package com.example.qq;

import static com.example.qq.websocket.webUtils.controller.WebUtil.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qq.DAO.UserDAO;
import com.example.qq.websocket.webResult.WebResult;
import com.example.qq.websocket.webUtils.controller.Callback;

import org.json.JSONException;

import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private EditText qqNumberEditText;
    private EditText qqPasswordEditText;
    private ImageView loginButton;
    private CheckBox agreeCheckBox;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDAO = new UserDAO(this);

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

                        // 调用UserDAO的addUser方法将用户信息存入数据库
//                        long result = userDAO.addUser(account, qqNumber, qqPassword);

                        register(qqNumber,account, qqPassword, new Callback() {
                            @Override
                            public void onResult(WebResult<Map<String, Object>> result) throws JSONException {
                                if (result.getCode() == 200) {
                                    // 注册成功，传递账号到LoginActivity
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.putExtra("account", account);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

//                        if (result != -1) {
//                            // 注册成功，传递账号到LoginActivity
//                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                            intent.putExtra("account", account); // 将账号作为额外数据传递
//                            startActivity(intent);
//                            finish(); // 关闭当前活动
                    }
                }
            }
        });
    }
}