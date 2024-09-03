package com.example.facedetectionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ResultDialog extends DialogFragment {   //to make it behave as a fragmant
    Button btn;
    TextView txt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {//first step in making fragment class
        View view = inflater.inflate(R.layout.fragmant_resultdialog,container,false);
        String text = "";
        btn = view.findViewById(R.id.ok);
        txt = view.findViewById(R.id.dialog);

        //Getting the bundle
        Bundle bundle = getArguments(); // gets all the arguments from your fragmant, communicates with fragmant.
        text = bundle.getString("RESULT_TEXT");
        txt.setText(text);

        //Handling click listeners
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;

    }
}
