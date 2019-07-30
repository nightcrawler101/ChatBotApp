package com.example.internchatbot;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

public  class chat_rec extends ViewHolder {
    TextView leftText,rightText;
    //final View itemView;
    public chat_rec(View itemView){
        super(itemView);
        //this.itemView=itemView;
        leftText = itemView.findViewById(R.id.leftText);
        rightText = itemView.findViewById(R.id.rightText);



    }
}
