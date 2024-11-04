package com.example.as003;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private String[] Titles = new String[]{"好友", "分组", "群聊", "频道", "机器人", "设备", "通讯录"};

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

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
}