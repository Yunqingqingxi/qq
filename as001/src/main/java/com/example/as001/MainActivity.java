package com.example.as001;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private GestureDetector gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            // 处理菜单项点击事件
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // 检测水平方向的滑动
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    // 判断是否滑动到屏幕的三分之一
                    if (distanceX > ViewConfiguration.get(MainActivity.this).getScaledTouchSlop() && distanceX > (getResources().getDisplayMetrics().widthPixels / 3)) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // 将触摸事件传递给 GestureDetector
        boolean handled = gestureDetector.onTouchEvent(event);
        if (handled) {
            // 如果 GestureDetector 处理了事件，返回 true
            return true;
        }
        // 否则，让父类处理事件
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}