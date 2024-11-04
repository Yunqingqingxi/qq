package com.example.as003;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GroupsFragment extends Fragment {

    private Spinner spinnerFriends;
    private ListView listViewFriends;
    private String[] friendGroups = {"我的好友", "我的家人", "我的同事"};
    private String[][] friendsList = {
            {"念旧", "本群门面", "赣东学院表白墙", "感恋-5111138", "六六alln", "清糖"},
            {"家人1", "家人2", "家人3"},
            {"同事1", "同事2", "同事3", "同事4"}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        spinnerFriends = view.findViewById(R.id.spinner_friends);
        listViewFriends = view.findViewById(R.id.listview_friends);

        // Set up the Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, friendGroups);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFriends.setAdapter(spinnerAdapter);

        // Set up the ListView
        setListViewContents(0); // Default to the first group

        // Set up the Spinner listener
        spinnerFriends.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setListViewContents(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return view;
    }

    private void setListViewContents(int groupPosition) {
        ArrayAdapter<String> listViewAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, friendsList[groupPosition]);
        listViewFriends.setAdapter(listViewAdapter);
    }
}