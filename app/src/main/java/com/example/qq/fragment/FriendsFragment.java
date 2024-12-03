package com.example.qq.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qq.R;
import com.example.qq.activity.FriendProfileActivity;
import com.example.qq.activity.NewFriendActivity;
import com.example.qq.api.friendlistapi.FriendApi;
import com.example.qq.api.friendlistapi.impl.FriendApiImpl;
import com.example.qq.api.userapi.UserApi;
import com.example.qq.api.userapi.impl.UserApiImpl;
import com.example.qq.domain.FriendList;
import com.example.qq.domain.FriendRequest;
import com.example.qq.domain.User;
import com.example.qq.event.FriendDeletedEvent;
import com.example.qq.event.FriendRequestEvent;
import com.example.qq.utils.SharedPreferencesManager;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友管理主Fragment
 * 包含好友列表、分组、群聊等多个子功能的导航和管理
 * 支持新朋友请求提醒和未读消息显示
 *
 * @author yunxi
 * @version 1.0
 * @see ContactListFragment
 * @see EmptyFragment
 * @see NewFriendActivity
 */
public class FriendsFragment extends Fragment {
    /** 未读消息数量显示视图 */
    private TextView unreadCountView;
    /** 导航标签数组 */
    private TextView[] tabTextViews;
    /** 当前选中的标签索引 */
    private int currentTabIndex = 0;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private List<User> searchResults = new ArrayList<>();
    private SearchResultAdapter searchAdapter;
    private UserApi userApi;
    private View fragmentContainer2;
    private View touchInterceptor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
            @Nullable ViewGroup container, 
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        initViews(view);
        EventBus.getDefault().register(this);
        updateUnreadCount();
        
        // 设置根视图的点击监听，处理点击空白处
        view.setOnClickListener(v -> clearSearchFocus());
        
        return view;
    }

    /**
     * 初始化视图组件
     * @param view Fragment的根视图
     */
    private void initViews(View view) {
        // 初始化未读数视图
        unreadCountView = view.findViewById(R.id.unread_count);
        
        // 设置新朋友入口点击事件
        View newFriendLayout = (View) view.findViewById(R.id.textview_new_friend).getParent();
        newFriendLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewFriendActivity.class);
            startActivity(intent);
        });

        // 初始化标签
        initTabs(view);

        // 初始化搜索相关的视图
        searchEditText = view.findViewById(R.id.searchEditText);
        recyclerView = view.findViewById(R.id.searchRecyclerView);
        fragmentContainer2 = view.findViewById(R.id.fragment_container2);
        
        // 初始化搜索结果适配器
        searchAdapter = new SearchResultAdapter(searchResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 初始化UserApi
        userApi = new UserApiImpl();
        
        // 设置搜索监听
        setupSearch();

        touchInterceptor = view.findViewById(R.id.touchInterceptor);
        
        // 设置搜索框焦点变化监听
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // 获得焦点时显示透明背景，用于拦截点击事件
                touchInterceptor.setVisibility(View.VISIBLE);
            } else {
                // 失去焦点时隐藏透明背景
                touchInterceptor.setVisibility(View.GONE);
                resetSearchState();
            }
        });

        // 设置透明背景的点击事件
        touchInterceptor.setOnClickListener(v -> clearSearchFocus());
    }

    /**
     * 初始化导航标签
     * @param view Fragment的根视图
     */
    private void initTabs(View view) {
        // 初始化标签数组
        tabTextViews = new TextView[]{
            view.findViewById(R.id.textview1),
            view.findViewById(R.id.textview2),
            view.findViewById(R.id.textview3),
            view.findViewById(R.id.textview4),
            view.findViewById(R.id.textview5),
            view.findViewById(R.id.textview6),
            view.findViewById(R.id.textview7)
        };

        // 设置标签点击事件
        for (int i = 0; i < tabTextViews.length; i++) {
            final int index = i;
            tabTextViews[i].setOnClickListener(v -> switchTab(index));
        }

        // 默认选中第一个标签
        switchTab(0);
    }

    /**
     * 切换导航标签
     * @param index 要切换到的标签索引
     */
    private void switchTab(int index) {
        // 更新标签样式
        updateTabStyles(index);
        // 切换内容Fragment
        switchContent(index);
        currentTabIndex = index;
    }

    /**
     * 更新标签样式
     * @param selectedIndex 选中的标签索引
     */
    private void updateTabStyles(int selectedIndex) {
        for (int i = 0; i < tabTextViews.length; i++) {
            tabTextViews[i].setTextColor(getResources().getColor(
                i == selectedIndex ? R.color.normal_text_color : R.color.textColorSecondary
            ));
        }
    }

    /**
     * 切换内容Fragment
     * @param index 标签索引
     */
    private void switchContent(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = new ContactListFragment();
                break;
            default:
                fragment = createEmptyFragment(getTabMessage(index));
                break;
        }

        if (fragment != null) {
            getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container2, fragment)
                .commit();
        }
    }

    /**
     * 获取标签对应的提示消息
     * @param index 标签索引
     * @return 提示消息
     */
    private String getTabMessage(int index) {
        switch (index) {
            case 1: return "分组功能开发中...";
            case 2: return "群聊功能开发中...";
            case 3: return "频道功能开发中...";
            case 4: return "机器人功能开发中...";
            case 5: return "设备功能开发中...";
            case 6: return "通讯录功能开发中...";
            default: return "功能开发中...";
        }
    }

    /**
     * 创建空状态Fragment
     * @param message 显示的消息
     * @return 配置好的EmptyFragment
     */
    private Fragment createEmptyFragment(String message) {
        Bundle args = new Bundle();
        args.putString("message", message);
        EmptyFragment fragment = new EmptyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次页面恢复时重置搜索状态
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        resetToOriginalState();
        updateUnreadCount();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendRequestEvent(FriendRequestEvent event) {
        updateUnreadCount();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFriendDeleted(FriendDeletedEvent event) {
        // 只需要刷新当前显示的 Fragment
        if (currentTabIndex == 0) { // 好友列表标签
            Fragment fragment = getChildFragmentManager()
                .findFragmentById(R.id.fragment_container2);
            if (fragment instanceof ContactListFragment) {
                ((ContactListFragment) fragment).loadContacts();
            }
        }
    }

    /**
     * 更新未读消息数量显示
     */
    private void updateUnreadCount() {
        List<FriendRequest> requests = SharedPreferencesManager.getInstance().getFriendRequests();
        int unreadCount = (int) requests.stream()
            .filter(request -> request.getStatus() == 0)
            .count();
        
        if (unreadCount > 0) {
            unreadCountView.setVisibility(View.VISIBLE);
            unreadCountView.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
        } else {
            unreadCountView.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    resetToOriginalState();
                } else {
                    performSearch(query);
                }
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (query.isEmpty()) {
                    resetToOriginalState();
                } else {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * 重置到原始状态
     */
    private void resetToOriginalState() {
        // 清空搜索结果
        searchResults.clear();
        if (searchAdapter != null) {
            searchAdapter.notifyDataSetChanged();
        }
        // 隐藏搜索结果列表
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        // 显示原始内容
        if (fragmentContainer2 != null) {
            fragmentContainer2.setVisibility(View.VISIBLE);
        }
    }

    private void performSearch(String query) {
        new Thread(() -> {
            try {
                // 从服务器获取所有用户列表
                FriendApi friendApi = new FriendApiImpl();
                List<FriendList> allFriends = friendApi.getFriendList();
                
                // 转换搜索关键词为小写
                String lowercaseQuery = query.toLowerCase();
                // 获取搜索关键词的拼音
                String pinyinQuery = toPinyin(query).toLowerCase();
                
                // 在本地进行搜索匹配
                List<User> results = new ArrayList<>();
                if (allFriends != null) {
                    for (FriendList friend : allFriends) {
                        if (isMatch(friend, lowercaseQuery, pinyinQuery)) {
                            User user = new User();
                            user.setUserName(friend.getFriendUsername());
                            user.setUserNickName(friend.getFriendNickName());
                            user.setUserAvatarUrl(friend.getAvatarUrl());
                            results.add(user);
                        }
                    }
                }
                
                // 在主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    searchResults.clear();
                    searchResults.addAll(results);
                    searchAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(searchAdapter);
                    recyclerView.setVisibility(results.isEmpty() ? View.GONE : View.VISIBLE);
                    fragmentContainer2.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
                });
                
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "搜索失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * 判断好友是否匹配搜索条件
     */
    private boolean isMatch(FriendList friend, String query, String pinyinQuery) {
        String username = friend.getFriendUsername().toLowerCase();
        String nickname = friend.getFriendNickName().toLowerCase();
        
        // 获取昵称的拼音（全拼和首字母）
        String nicknamePinyin = toPinyin(friend.getFriendNickName()).toLowerCase();
        String nicknameInitials = toPinyinInitials(friend.getFriendNickName()).toLowerCase();
        
        return username.contains(query) || // 用户名匹配
               nickname.contains(query) || // 昵称匹配
               nicknamePinyin.contains(pinyinQuery) || // 拼音全拼匹配
               nicknameInitials.contains(pinyinQuery) || // 拼音首字母匹配
               // 支持单个汉字的拼音匹配
               isChineseCharacterMatch(friend.getFriendNickName(), query);
    }

    /**
     * 将中文转换为拼音
     */
    private String toPinyin(String chinese) {
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            
            StringBuilder pinyin = new StringBuilder();
            char[] chars = chinese.toCharArray();
            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        pinyin.append(pinyinArray[0]);
                    }
                } else {
                    pinyin.append(c);
                }
            }
            return pinyin.toString();
        } catch (Exception e) {
            return chinese;
        }
    }

    /**
     * 获取拼音首字母
     */
    private String toPinyinInitials(String chinese) {
        try {
            StringBuilder initials = new StringBuilder();
            char[] chars = chinese.toCharArray();
            for (char c : chars) {
                if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        initials.append(pinyinArray[0].charAt(0));
                    }
                } else {
                    initials.append(c);
                }
            }
            return initials.toString();
        } catch (Exception e) {
            return chinese;
        }
    }

    /**
     * 检查单个汉字的拼音是否匹配
     */
    private boolean isChineseCharacterMatch(String nickname, String query) {
        char[] chars = nickname.toCharArray();
        for (char c : chars) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                String pinyin = toPinyin(String.valueOf(c));
                if (pinyin.contains(query)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 清除搜索框焦点
     */
    private void clearSearchFocus() {
        if (searchEditText != null) {
            searchEditText.clearFocus();
            // 隐藏软键盘
            InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            // 隐藏透明背景
            touchInterceptor.setVisibility(View.GONE);
        }
    }

    /**
     * 重置搜索状态
     */
    private void resetSearchState() {
        // 清空搜索框
        if (searchEditText != null) {
            searchEditText.setText("");
        }
        // 重置到原始状态
        resetToOriginalState();
        // 隐藏透明背景
        if (touchInterceptor != null) {
            touchInterceptor.setVisibility(View.GONE);
        }
    }

    // 搜索结果适配器
    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        private List<User> users;

        public SearchResultAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            
            // 设置头像
            if (user.getUserAvatarUrl() != null) {
                Glide.with(requireContext())
                    .load(user.getUserAvatarUrl())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(holder.imageAvatar);
            }
            
            // 设置昵称和用户名
            holder.textNickname.setText(user.getUserNickName());
            holder.textUsername.setText(user.getUserName());
            
            // 点击事件
            holder.itemView.setOnClickListener(v -> {
                // 打开用户资料页面
                Intent intent = new Intent(getActivity(), FriendProfileActivity.class);
                intent.putExtra("friend_username", user.getUserName());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageAvatar;
            TextView textNickname;
            TextView textUsername;

            ViewHolder(View itemView) {
                super(itemView);
                imageAvatar = itemView.findViewById(R.id.imageAvatar);
                textNickname = itemView.findViewById(R.id.textNickname);
                textUsername = itemView.findViewById(R.id.textUsername);
            }
        }
    }
} 
