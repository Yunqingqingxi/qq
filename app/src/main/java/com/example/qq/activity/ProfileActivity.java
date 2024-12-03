package com.example.qq.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.User;
import com.example.qq.utils.CameraGalleryUtils;
import com.example.qq.utils.ImageUploadUtils;
import com.example.qq.utils.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imageAvatar;
    private TextView textNickname;
    private TextView textUsername;
    private Uri currentPhotoUri;
    private UserApi userApi;
    private TextView textEmail;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    handleSelectedImage(selectedImageUri);
                }
            }
        }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && currentPhotoUri != null) {
                handleSelectedImage(currentPhotoUri);
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 设状态栏
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // 初始化状态栏高度
        View statusBarBackground = findViewById(R.id.statusBarBackground);
        statusBarBackground.getLayoutParams().height = getStatusBarHeight();

        // 初始化视图
        initViews();
        
        // 初始化API
        userApi = new UserApiImpl();
        
        // 加载用户信息
        loadUserInfo();
    }

    private void initViews() {
        // 初始化返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 初始化头像相关视图
        imageAvatar = findViewById(R.id.imageAvatar);
        View layoutAvatar = findViewById(R.id.layoutAvatar);
        layoutAvatar.setOnClickListener(v -> showAvatarOptions());

        // 初始化昵称相关视图
        textNickname = findViewById(R.id.textNickname);
        View layoutNickname = findViewById(R.id.layoutNickname);
        layoutNickname.setOnClickListener(v -> showNicknameDialog());

        // 初始化用户名视图
        textUsername = findViewById(R.id.textUsername);

        // 初始化邮箱视图
        textEmail = findViewById(R.id.textEmail);

        // 初始化修改密码视图
        View layoutPassword = findViewById(R.id.layoutPassword);
        layoutPassword.setOnClickListener(v -> showUpdatePasswordDialog());
    }

    private void loadUserInfo() {
        User user = SharedPreferencesManager.getInstance().getUserInfo();
        if (user != null) {
            // 设置头像
            if (user.getUserAvatarUrl() != null) {
                Glide.with(this)
                    .load(user.getUserAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageAvatar);
            }

            // 设置昵称
            textNickname.setText(user.getUserNickName());

            // 设置用户名
            textUsername.setText(user.getUserName());

            // 设置邮箱
            textEmail.setText(user.getEmail());
        }
    }

    private void showAvatarOptions() {
        new AlertDialog.Builder(this)
            .setTitle("选择图片来源")
            .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                if (which == 0) {
                    // 选择拍照
                    currentPhotoUri = ImageUploadUtils.createTempImageFile(this);
                    if (currentPhotoUri != null) {
                        CameraGalleryUtils.checkCameraPermissionAndCapture(
                            this,
                            currentPhotoUri,
                            cameraLauncher
                        );
                    }
                } else {
                    // 选择相册
                    CameraGalleryUtils.checkGalleryPermissionAndPick(
                        this,
                        imagePickerLauncher
                    );
                }
            })
            .show();
    }

    private void showNicknameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改昵称");

        final EditText input = new EditText(this);
        input.setText(textNickname.getText());
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String newNickname = input.getText().toString().trim();
            if (!newNickname.isEmpty()) {
                updateNickname(newNickname);
            }
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private void updateNickname(String newNickname) {
        String username = SharedPreferencesManager.getInstance().getCurrentUsername();
        if (username != null) {
            try {
                // 构建JSON请求
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("nickname", newNickname);
                
                userApi.updateUserInfo(jsonObject.toString(), new UserApi.UserInfoUpdateCallback() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        runOnUiThread(() -> {
                            // 使用返回的用户对象更新UI
                            textNickname.setText(updatedUser.getUserNickName());
                            // 更新SharedPreferences中的用户信息
                            SharedPreferencesManager.getInstance().saveUserInfo(updatedUser);
                            Toast.makeText(ProfileActivity.this, "昵称更新成功", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> 
                            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "数据格式错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleSelectedImage(Uri imageUri) {
        ImageUploadUtils.handleSelectedImage(imageUri, this, new ImageUploadUtils.ImageUploadCallback() {
            @Override
            public void onSuccess(Uri processedUri) {
                String username = SharedPreferencesManager.getInstance().getCurrentUsername();
                if (username != null) {
                    userApi.updateAvatar(username, processedUri, new UserApi.AvatarUpdateCallback() {
                        @Override
                        public void onSuccess(String newAvatarUrl) {
                            runOnUiThread(() -> {
                                // 更新头像显示
                                Glide.with(ProfileActivity.this)
                                    .load(newAvatarUrl)
                                    .placeholder(R.drawable.default_avatar)
                                    .error(R.drawable.default_avatar)
                                    .circleCrop()
                                    .into(imageAvatar);

                                // 更新SharedPreferences中的用户信息
                                User user = SharedPreferencesManager.getInstance().getUserInfo();
                                if (user != null) {
                                    user.setUserAvatarUrl(newAvatarUrl);
                                    SharedPreferencesManager.getInstance().saveUserInfo(user);
                                }
                                
                                Toast.makeText(ProfileActivity.this, "头像更新成功", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() -> 
                                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onProgress(int progress) {
                            // 可以在这里更新上传进度
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> 
                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CameraGalleryUtils.handlePermissionResult(
            this,
            requestCode,
            permissions,
            grantResults,
            cameraLauncher,
            imagePickerLauncher,
            currentPhotoUri
        );
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void showUpdatePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_password, null);
        EditText oldPasswordInput = dialogView.findViewById(R.id.editTextOldPassword);
        EditText newPasswordInput = dialogView.findViewById(R.id.editTextNewPassword);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.editTextConfirmPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setTitle("修改密码")
            .setView(dialogView)
            .setPositiveButton("确定", null)
            .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 重写确定按钮的点击事件，以便进行输入验证
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // 验证输入
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "请填写所有密码字段", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "新密码与确认密码不匹配", Toast.LENGTH_SHORT).show();
                return;
            }

            // 验证新密码长度和复杂度
            if (newPassword.length() < 6) {
                Toast.makeText(this, "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
                return;
            }

            // 调用API更新密码
            updatePassword(oldPassword, newPassword, dialog);
        });
    }

    private void updatePassword(String oldPassword, String newPassword, AlertDialog dialog) {
        String username = SharedPreferencesManager.getInstance().getCurrentUsername();
        if (username != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("oldPassword", oldPassword);
                jsonObject.put("newPassword", newPassword);

                userApi.updatePassword(jsonObject.toString(), new UserApi.PasswordUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            dialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                            // 删除登录状态回到登录界面
                            SharedPreferencesManager.getInstance().clearForLoginUserInfo();
                            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> 
                            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "数据格式错误", Toast.LENGTH_SHORT).show();
            }
        }
    }
}