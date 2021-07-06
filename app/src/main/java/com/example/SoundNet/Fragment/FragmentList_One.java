package com.example.SoundNet.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SoundNet.R;
import com.example.SoundNet.RecyclerViewAdapter;

public class FragmentList_One extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerAdapter;
    private String[] Dataset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmentlist_one, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        setRecyclerView();
        return view;
    }

    public void setRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setData();
        recyclerAdapter = new RecyclerViewAdapter(Dataset);
        recyclerView.setAdapter(recyclerAdapter);
    }

    public void setData() {
        Dataset = new String[21];

        for (int i = 0; i < 21; i++)
            Dataset[i] = Integer.toString(i);
    }
}
