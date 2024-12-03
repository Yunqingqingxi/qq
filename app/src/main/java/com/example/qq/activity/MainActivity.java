package com.example.qq.activity;

// Android 框架

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.broadcast.NetworkBroadcastReceiver;
import com.example.qq.domain.ChatMessage;
import com.example.qq.domain.NotificationMessage;
import com.example.qq.domain.User;
import com.example.qq.fragment.AuthFragment;
import com.example.qq.fragment.FriendListFragment;
import com.example.qq.fragment.FriendsFragment;
import com.example.qq.handler.MessageHandler;
import com.example.qq.handler.impl.MessageHandlerImpl;
import com.example.qq.service.NotificationService;
import com.example.qq.service.WebSocketForegroundService;
import com.example.qq.utils.CameraGalleryUtils;
import com.example.qq.utils.ImageUploadUtils;
import com.example.qq.utils.JsonParser;
import com.example.qq.utils.SharedPreferencesManager;
import com.example.qq.websocket.WebSocketService;
import com.example.qq.websocket.impl.WebSocketServiceImpl;
import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 主界面活动类
 * 负责应用的主要功能，包括：
 * - 底部导航栏和Fragment管理
 * - 侧滑菜单
 * - WebSocket连接管理
 * - 消息处理
 * - 用户头像更新
 * - 好友管理
 * 
 * @author yunxi
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // UI组件
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView imageAvatar;
    private TextView textNickname;
    private TextView textViewStatus;
    private ImageButton btnMsg;
    private ImageButton btnFri;
    private ImageButton btnAut;
    private RelativeLayout loadingContainer;

    // Fragment管理
    private Fragment currentFragment;
    private FriendListFragment friendListFragment;
    private FriendsFragment friendsFragment;
    private AuthFragment authFragment;

    // 服务和处理器
    private MessageHandler messageHandler;
    private NotificationService notificationService;
    private WebSocketService webSocketService;
    private NetworkBroadcastReceiver networkReceiver;

    // 图片选择器
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

    private Uri currentPhotoUri;

    /**
     * WebSocket监听器
     * 处理WebSocket连接状态和消息接收
     */
    private final WebSocketService.WebSocketListener webSocketListener = new WebSocketService.WebSocketListener() {
        @Override
        public void onConnected() {
            Log.i(TAG, "WebSocket connected");
            runOnUiThread(() -> {
                // 可以在这里更新UI显示连接状态
//                Toast.makeText(MainActivity.this, "WebSocket已连接", Toast.LENGTH_SHORT).show();
                Log.d("WebSocket connected", "WebSocket connected");
            });
        }

        @Override
        public void onDisconnected() {
            Log.w(TAG, "WebSocket disconnected");
            runOnUiThread(() -> {
                // 可以在这里更新UI显示断开状态
//                Toast.makeText(MainActivity.this, "WebSocket已断开", Toast.LENGTH_SHORT).show();
                Log.d("WebSocket disconnected", "WebSocket disconnected");
            });
        }

        @Override
        public void onMessageReceived(String message) {
            handleWebSocketMessage(message);
        }

        @Override
        public void onError(String error) {
            Log.e(TAG, "WebSocket error: " + error);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "WebSocket错误: " + error, Toast.LENGTH_SHORT).show();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 NotificationService
        notificationService = NotificationService.getInstance();
        
        // 检查用户是否已登录
        if (!isUserLoggedIn()) {
            // 如果用户未登录，跳转到登录页面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // 创建 MessageHandlerImpl 实例
        messageHandler = new MessageHandlerImpl(this, NotificationService.getInstance());

        // 设置状态栏为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // 设置状态栏文字颜色为深色
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_main);

        // 设置状态栏占位视图的高度
        View statusBarBackground = findViewById(R.id.statusBarBackground);
        statusBarBackground.getLayoutParams().height = getStatusBarHeight();

        // 设置返回键处理
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // 使用post延迟初始化非必要的UI
        getWindow().getDecorView().post(() -> {
            initViews();
            setupDrawer();
            setupUserInfo();
            setupListeners();
        });

        // 立即显示主要UI
        showFragment(getFriendListFragment());

        // 注册网络状态广播接收器
        networkReceiver = NetworkBroadcastReceiver.register(this, (isConnected, networkType) -> {
            runOnUiThread(() -> {
                // 更新侧滑菜单中的状态
                if (textViewStatus != null) {
                    textViewStatus.setText(networkType);
                }
                // 更新顶部栏中的状态
                View topBar = findViewById(R.id.top_bar);
                if (topBar != null) {
                    TextView topBarStatus = topBar.findViewById(R.id.textViewStatus);
                    if (topBarStatus != null) {
                        topBarStatus.setText(networkType);
                    }
                }
                SharedPreferencesManager.getInstance().saveNetworkStatus(networkType);
            });
        });

        // 初始化WebSocket时使用成员变量监听器
        webSocketService = WebSocketServiceImpl.getInstance();
        webSocketService.addListener(webSocketListener);
        webSocketService.init();

        // 启动前台服务
        startWebSocketService();
        
        // 请求忽略电池优化
        requestIgnoreBatteryOptimization();
    }

    private boolean isUserLoggedIn() {
        String username = SharedPreferencesManager.getInstance().getCurrentUsername();
        String token = SharedPreferencesManager.getInstance().getToken();
        return username != null && token != null;
    }

    /**
     * 初始化视图组件
     * 包括顶部栏、底部导航栏、侧滑菜单等UI组件
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        // 获取侧滑菜单头部视图
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            imageAvatar = headerView.findViewById(R.id.imageAvatar);
            textNickname = headerView.findViewById(R.id.textNickname);
            textViewStatus = headerView.findViewById(R.id.textViewStatus);

            // 设置侧滑菜单状态栏占位高度
            View statusBarSpace = headerView.findViewById(R.id.statusBarSpaceNav);
            if (statusBarSpace != null) {
                statusBarSpace.getLayoutParams().height = getStatusBarHeight();
            }
        } else {
            Log.e("MainActivity", "Navigation header view is null");
        }

        btnMsg = findViewById(R.id.btnMsg);
        btnFri = findViewById(R.id.btnFri);
        btnAut = findViewById(R.id.btnAut);
        loadingContainer = findViewById(R.id.loadingContainer);

        // 获取顶部栏��设置用户信息
        View topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            // 获取顶部栏中的头像和昵称控件
            ImageView topBarAvatar = topBar.findViewById(R.id.imageAvatar);
            TextView topBarNickname = topBar.findViewById(R.id.textNickname);
            TextView topBarStatus = topBar.findViewById(R.id.textViewStatus);
            
            // 设置头像点击事件
            topBarAvatar.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
            
            // 设置用户信息
            User user = SharedPreferencesManager.getInstance().getUserInfo();
            if (user != null) {
                // 设置头像
                if (user.getUserAvatarUrl() != null) {
                    Glide.with(this)
                        .load(user.getUserAvatarUrl())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(topBarAvatar);
                }
                // 设置昵称
                if (user.getUserNickName() != null) {
                    topBarNickname.setText(user.getUserNickName());
                }
                // 设置在线状态
                String networkStatus = SharedPreferencesManager.getInstance().getNetworkStatus();
                topBarStatus.setText(networkStatus);
            }

            // 设置更多按钮点击事件
            ImageView moreButton = topBar.findViewById(R.id.moreButton);
            if (moreButton != null) {
                moreButton.setOnClickListener(this::showPopupMenu);
            } else {
                Log.e("MainActivity", "moreButton not found in top_bar layout");
            }
        } else {
            Log.e("MainActivity", "top_bar not found in layout");
        }
    }

    /**
     * 设置侧滑菜单
     * 配置侧滑菜单的布局和事件监听
     */
    private void setupDrawer() {
        if (drawerLayout != null) {
            // 确保DrawerLayout可以通过滑动打开
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START);

            // 设置侧滑菜单头像点击事
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                ImageView navAvatar = headerView.findViewById(R.id.imageAvatar);
                if (navAvatar != null) {
                    navAvatar.setOnClickListener(v -> {
                        // TODO: 实现上传头像的方法
                        uploadAvatar();
                    });
                }
            }

            // 设置导航菜单的点击监听
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_logout) {
                    // 处理退出登录
                    logout();
                    return true;
                } else if (id == R.id.nav_profile) {
                    // 启动个人资料页面
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.nav_settings) {
                    // 启动设置页面
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });

            // 添加抽屉监听器
            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }
            });
        }
    }

    /**
     * 设置用户信息
     * 在界面上显示用户头像和昵称
     */
    private void setupUserInfo() {
        User user = SharedPreferencesManager.getInstance().getUserInfo();
        if (user != null && imageAvatar != null) {
            // 设置用户头像
            if (user.getUserAvatarUrl() != null) {
                Glide.with(this)
                    .load(user.getUserAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageAvatar);
            }

            // 设置昵称
            if (textNickname != null) {
                textNickname.setText(user.getUserNickName());
            }
        }
    }

    /**
     * 设置事件监听器
     * 为各个按钮和控件添加点击事件处理
     */
    private void setupListeners() {
     
        
        if (btnMsg != null) {
            btnMsg.setOnClickListener(v -> {
                updateBottomNav(btnMsg);
                showFragment(getFriendListFragment());
            });
        }

        if (btnFri != null) {
            btnFri.setOnClickListener(v -> {
                updateBottomNav(btnFri);
                showFragment(getFriendsFragment());
            });
        }

        if (btnAut != null) {
            btnAut.setOnClickListener(v -> {
                updateBottomNav(btnAut);
                showFragment(getAuthFragment());
            });
        }
    }

    /**
     * 更新底部导航栏状态
     * @param selectedBtn 被选中的按钮
     */
    private void updateBottomNav(ImageButton selectedBtn) {
        // 重置所有按钮状态
        btnMsg.setSelected(false);
        btnFri.setSelected(false);
        btnAut.setSelected(false);

        // 设置选中按钮状态
        selectedBtn.setSelected(true);
    }

    /**
     * 显示指定的Fragment
     * @param fragment 要显示的Fragment
     */
    private void showFragment(Fragment fragment) {
        if (currentFragment != fragment) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // 设置转场动画
            transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out);

            if (currentFragment != null) {
                // 使用hide而不是remove可以保持Fragment状态
                transaction.hide(currentFragment);
            }
            if (!fragment.isAdded()) {
                // 延迟加载Fragment的视图
                fragment.setUserVisibleHint(true);
                transaction.add(R.id.fragment_container, fragment);
            } else {
                transaction.show(fragment);
            }

            // 使用commitAllowingStateLoss()而不是commit()
            transaction.commitAllowingStateLoss();
            currentFragment = fragment;
        }
    }

    private FriendListFragment getFriendListFragment() {
        if (friendListFragment == null) {
            friendListFragment = new FriendListFragment();
        }
        return friendListFragment;
    }

    private FriendsFragment getFriendsFragment() {
        if (friendsFragment == null) {
            friendsFragment = new FriendsFragment();
        }
        return friendsFragment;
    }

    private AuthFragment getAuthFragment() {
        if (authFragment == null) {
            authFragment = new AuthFragment();
        }
        return authFragment;
    }

    public void showLoading() {
        loadingContainer.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        loadingContainer.setVisibility(View.GONE);
    }

    private void showPopupMenu(View view) {
        // 确保 messageHandler 已初始化
        if (messageHandler == null) {
            Toast.makeText(this, "系统未就绪，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

        // 显示菜单图标
        try {
            @SuppressLint("DiscouragedPrivateApi") Field field = PopupMenu.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
            setForceIcons.invoke(menuPopupHelper, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_add_friend) {
                // 显示添加好友对话框
                showAddFriendDialog();
                return true;
            } else if (itemId == R.id.menu_other_option) {
                logout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    /**
     * 显示添加好友对话框
     */
    private void showAddFriendDialog() {
        // 确保 messageHandler 已初始化
        if (messageHandler == null) {
            Toast.makeText(this, "系统未就绪，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加好友");

        // ���建输入框布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 10, 20, 10);

        // 用户名输入框
        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("请输入用户名");
        layout.addView(usernameInput);

        // 验证消息输入框
        final EditText messageInput = new EditText(this);
        messageInput.setHint("请输入验证消息");
        messageInput.setMaxLines(3);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 20;
        messageInput.setLayoutParams(params);
        layout.addView(messageInput);

        builder.setView(layout);

        // 设置按钮
        builder.setPositiveButton("发送", (dialog, which) -> {
            String targetUsername = usernameInput.getText().toString().trim();
            String message = messageInput.getText().toString().trim();

            if (targetUsername.isEmpty()) {
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查是否是自己
            if (targetUsername.equals(SharedPreferencesManager.getInstance().getCurrentUsername())) {
                Toast.makeText(this, "不能添加自己为好友", Toast.LENGTH_SHORT).show();
                return;
            }

            // 检查是否已经是好友
            if (SharedPreferencesManager.getInstance().isFriend(targetUsername)) {
                Toast.makeText(this, "该用户已经是您的好友", Toast.LENGTH_SHORT).show();
                return;
            }

            // 发送好友请求
            messageHandler.sendFriendRequest(targetUsername, message);
            Toast.makeText(this, "好友请求已发送", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout() {
        // 清除用户数据
        SharedPreferencesManager.getInstance().clearForLoginUserInfo();

        // 显示提示信息
        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();

        // 跳转到登录界面
        Intent intent = new Intent(this, SplashActivity.class);
        // 清除任务栈中的其他活动
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // 添加获取状态栏高度的方法
    private int getStatusBarHeight() {
        int result = 0;
        @SuppressLint({"DiscouragedApi", "InternalInsetResource"}) int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销网络状态广播接收器
        NetworkBroadcastReceiver.unregister(this, networkReceiver);
        if (webSocketService != null) {
            webSocketService.removeListener(webSocketListener);
            // 在Activity销毁时断开WebSocket连接
            webSocketService.disconnect();
        }
        if (messageHandler instanceof MessageHandlerImpl) {
            ((MessageHandlerImpl) messageHandler).destroy();
        }
    }

    /**
     * 处理选择的���片并上传
     */
    private void handleSelectedImage(Uri imageUri) {
        ImageUploadUtils.handleSelectedImage(imageUri, this, new ImageUploadUtils.ImageUploadCallback() {
            @Override
            public void onSuccess(Uri processedUri) {
                // 获取当前用户名
                String username = SharedPreferencesManager.getInstance().getCurrentUsername();
                if (username != null) {
                    // 调用API上传头像
                    UserApiImpl userApi = new UserApiImpl();
                    userApi.updateAvatar(username, processedUri, new UserApi.AvatarUpdateCallback() {
                        @Override
                        public void onSuccess(String newAvatarUrl) {
                            runOnUiThread(() -> {
                                // 更新界面上的头像
                                updateAvatarViews(newAvatarUrl);
                                Toast.makeText(MainActivity.this, "头像更新成功", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String message) {
                            runOnUiThread(() -> 
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * 更新界面上的头像
     */
    private void updateAvatarViews(String avatarUrl) {
        // 更新侧滑菜单头像
        if (imageAvatar != null) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(imageAvatar);
        }

        // 更新顶部栏头像
        View topBar = findViewById(R.id.top_bar);
        if (topBar != null) {
            ImageView topBarAvatar = topBar.findViewById(R.id.imageAvatar);
            if (topBarAvatar != null) {
                Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(topBarAvatar);
            }
        }
    }

    /**
     * 上传头像的方法
     */
    private void uploadAvatar() {
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

    // 添加相机启动器
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && currentPhotoUri != null) {
                Log.d(TAG, "Camera result received with URI: " + currentPhotoUri);
                handleSelectedImage(currentPhotoUri);
            }
        }
    );

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

    // 处理WebSocket消息的方法
    private void handleWebSocketMessage(String message) {
        String messageType = JsonParser.parseMessageType(message);
        switch (messageType) {
            case "chat":
                ChatMessage chatMessage = JsonParser.parseChatMessage(message);
                if (chatMessage != null) {
                    handleChatMessage(chatMessage);
                }
                break;
            case "notification":
                NotificationMessage notificationMessage = JsonParser.parseNotificationMessage(message);
                if (notificationMessage != null) {
                    handleNotificationMessage(notificationMessage);
                }
                break;
            default:
                Log.w(TAG, "Unknown message type: " + messageType);
                break;
        }
    }

    // 理聊天消息
    private void handleChatMessage(ChatMessage message) {
        runOnUiThread(() -> {
            // TODO: 实现聊天消息处理逻辑
            String sender = message.getSender();
            String content = message.getContent();
            // 更新聊天界面
        });
    }

    // 处理通知消息
    private void handleNotificationMessage(NotificationMessage message) {
        runOnUiThread(() -> {
            Toast.makeText(this, 
                message.getTitle() + ": " + message.getContent(), 
                Toast.LENGTH_SHORT).show();
        });
    }

    private void startWebSocketService() {
        Intent serviceIntent = new Intent(this, WebSocketForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusBar();
        if (!isUserLoggedIn()) {
            // 如果用户未登录，跳转到登录页面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // 更新用户信息显示
        updateUserInfoDisplay();
    }

    /**
     * 更新用户信息显示
     * 在UI上刷新用户头像和昵称
     */
    private void updateUserInfoDisplay() {
        User user = SharedPreferencesManager.getInstance().getUserInfo();
        if (user != null) {
            // 更新侧滑菜单中的用户信息
            if (imageAvatar != null) {
                Glide.with(this)
                    .load(user.getUserAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageAvatar);
            }
            if (textNickname != null) {
                textNickname.setText(user.getUserNickName());
            }

            // 更新顶部栏中的用户信息
            View topBar = findViewById(R.id.top_bar);
            if (topBar != null) {
                ImageView topBarAvatar = topBar.findViewById(R.id.imageAvatar);
                TextView topBarNickname = topBar.findViewById(R.id.textNickname);
                
                if (topBarAvatar != null) {
                    Glide.with(this)
                        .load(user.getUserAvatarUrl())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(topBarAvatar);
                }
                if (topBarNickname != null) {
                    topBarNickname.setText(user.getUserNickName());
                }
            }
        }
    }

    private void updateStatusBar() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            // 深色模式
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        } else {
            // 浅色模式
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            );
        }
    }
}