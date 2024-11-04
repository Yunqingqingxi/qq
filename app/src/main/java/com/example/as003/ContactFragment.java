package com.example.as003;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ContactFragment extends Fragment {

    private ViewPager viewPager;
    private String[] Titles = new String[]{"好友", "分组", "群聊", "频道", "机器人", "设备", "通讯录"};

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        // 初始化ViewPager
        viewPager = view.findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new Fragment(); // 好友
                    case 1:
                        return new GroupsFragment(); // 分组
                    case 2:
                        return new ChatsFragment(); // 群聊
                    case 3:
                        return new ChannelsFragment(); // 频道
                    case 4:
                        return new RobotsFragment(); // 机器人
                    case 5:
                        return new DevicesFragment(); // 设备
                    case 6:
                        return new ContactsFragment(); // 通讯录
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return Titles.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return Titles[position];
            }
        });

        return view;
    }
}