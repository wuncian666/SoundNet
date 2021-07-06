package com.example.SoundNet.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.SoundNet.MainActivity;
import com.example.SoundNet.R;
import com.example.SoundNet.ReceiveProcess;
import com.example.SoundNet.SoundGenerator;

public class FragmentList_Two extends Fragment {
    private final static String TAG = "FragmentList_Two";
    SoundGenerator soundGenerator;
    Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View view = inflater.inflate(R.layout.fragmentlist_two, container, false);
        context = container.getContext();
        TextView textMode = (TextView)view.findViewById(R.id.tv_mode);
        EditText editMessage = (EditText)view.findViewById(R.id.edit_message);

        Button btn_generate = (Button) view.findViewById(R.id.btn_generate);
        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 獲取edit message 字串
                String message = editMessage.getText().toString();

                soundGenerator = new SoundGenerator(message, context);
                soundGenerator.encode();
                soundGenerator.generatorSound();
                soundGenerator.playSound();
            }
        });

        Button btn_master = (Button) view.findViewById(R.id.btn_master);
        btn_master.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isOnlyReceiveMode = true;
                MainActivity.isMaster = true;
                textMode.setText("Receive only");
                ReceiveProcess rec = new ReceiveProcess(context);
                Thread t = new Thread(rec);
                Log.i(TAG, "btn_receive: thread start");
                t.start();
            }
        });

        Button btn_slave = (Button) view.findViewById(R.id.btn_slave);
        btn_slave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.isOnlyReceiveMode = false;
                MainActivity.isMaster = false;
                textMode.setText("Slave");
                ReceiveProcess rec = new ReceiveProcess(context);

                Thread t = new Thread(rec);
                Log.i(TAG, "btn_slave: thread start");
                t.start();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


}
