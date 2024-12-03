package com.example.qq.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qq.R;
import com.example.qq.adapter.ContactAdapter;
import com.example.qq.api.friendlistapi.FriendApi;
import com.example.qq.api.friendlistapi.impl.FriendApiImpl;
import com.example.qq.domain.Contact;
import com.example.qq.event.FriendDeletedEvent;
import com.example.qq.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人列表Fragment
 * 用于显示和管理用户的联系人列表，支持联系人的展示、更新和删除等操作
 *
 * @author yunxi
 * @version 1.0
 * @see ContactAdapter
 * @see FriendApi
 */
public class ContactListFragment extends Fragment {
    private static final String TAG = "ContactListFragment";

    /** 联系人列表RecyclerView */
    private RecyclerView recyclerView;
    /** 联系人列表适配器 */
    private ContactAdapter contactAdapter;
    /** SharedPreferences管理器 */
    private SharedPreferencesManager sharedPreferencesManager;
    /** 好友API接口 */
    private FriendApi friendApi;
    /** 加载进度条 */
    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferencesManager = SharedPreferencesManager.getInstance();
        friendApi = new FriendApiImpl();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
            @Nullable ViewGroup container, 
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
        progressBar = view.findViewById(R.id.progressBar);
        loadContacts();
    }

    /**
     * 初始化RecyclerView
     * 设置布局管理器和适配器，配置点击事件监听
     *
     * @param view Fragment的根视图
     */
    private void initRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        contactAdapter = new ContactAdapter(requireContext(), new ArrayList<>());
        recyclerView.setAdapter(contactAdapter);

        contactAdapter.setOnItemClickListener((contact, position) -> {
            Toast.makeText(requireContext(), 
                "选择了联系人: " + contact.getNickName(), 
                Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * 加载联系人列表
     * 从服务器获取联系人数据并更新UI
     */
    public void loadContacts() {
        showLoading();
        
        new Thread(() -> {
            try {
                List<Contact> contacts = friendApi.getContactList();
                
                Log.d(TAG, "Loaded contacts: " + contacts.size());
                for (Contact contact : contacts) {
                    Log.d(TAG, "Contact: " + 
                        "username=" + contact.getUsername() +
                        ", nickname=" + contact.getNickName() + 
                        ", avatar=" + contact.getAvatarUrl());
                }

                SharedPreferencesManager.getInstance().setFriendAvatars(contacts);
                
                updateUIWithContacts(contacts);
            } catch (Exception e) {
                Log.e(TAG, "Error loading contacts", e);
                handleLoadError(e);
            }
        }).start();
    }

    /**
     * 使用获取到的联系人数据更新UI
     *
     * @param contacts 联系人列表
     */
    private void updateUIWithContacts(List<Contact> contacts) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                hideLoading();
                if (!contacts.isEmpty()) {
                    contactAdapter.updateData(contacts);
                } else {
                    Toast.makeText(requireContext(), "暂无联系人", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 处理加载联系人时的错误
     *
     * @param e 捕获到的异常
     */
    private void handleLoadError(Exception e) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                hideLoading();
                Toast.makeText(getContext(), 
                    "加载联系人失败: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 显示加载进度条
     */
    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏加载进度条
     */
    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContacts();
    }

    /**
     * 处理好友删除事件
     * 当收到好友删除事件时，从列表中移除对应的联系人
     *
     * @param event 好友删除事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendDeleted(FriendDeletedEvent event) {
        contactAdapter.removeContact(event.getFriendUsername());
    }
} 