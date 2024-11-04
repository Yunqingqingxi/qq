package com.example.as003;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity2 extends AppCompatActivity {

    private Fragment messageFragment = new MessageFragment();
    private Fragment channelFragment = new ChannelFragment();
    private Fragment videoFragment = new VideoFragment();
    private Fragment contactFragment = new ContactFragment();
    private Fragment dynamicFragment = new DynamicFragment();




    private Fragment currentFragment = messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        // 初始化默认显示的Fragment
        displayFragment(currentFragment);

        // 设置底部导航栏的点击事件监听器
        setupBottomNavigation();

    }

    private void setupBottomNavigation() {
        setNavigationItemSelectedListener(R.id.imageViewMessage, messageFragment);
        setNavigationItemSelectedListener(R.id.imageViewChannel, channelFragment);
        setNavigationItemSelectedListener(R.id.imageViewVideo, videoFragment);
        setNavigationItemSelectedListener(R.id.imageViewContact, contactFragment);
        setNavigationItemSelectedListener(R.id.imageViewDynamic, dynamicFragment);
    }

    private void setNavigationItemSelectedListener(int viewId, Fragment fragment) {
        findViewById(viewId).setOnClickListener(v -> {
            if (currentFragment != fragment) {
                displayFragment(fragment);
            }
        });
    }

    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        currentFragment = fragment;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String lastMessage = data.getStringExtra("lastMessage");
            if (currentFragment instanceof MessageFragment) {
                ((MessageFragment) currentFragment).updateChatMessage(lastMessage);
            }
        }
    }
    // 添加一个方法来更新MessageFragment中的ListView
    public void updateMessageFragment(String message) {
        if (currentFragment instanceof MessageFragment) {
            ((MessageFragment) currentFragment).updateChatMessage(message);
        }
    }
}