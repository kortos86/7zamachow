package com.artpi.games.a7zamachow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class MemoryView extends RelativeLayout {

    LayoutInflater mInflater;
    public MemoryView(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();

    }
    public MemoryView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public MemoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public void init()
    {
        View v = mInflater.inflate(R.layout.activity_puzzle_game, this, true);
        //TextView tv = (TextView) v.findViewById(R.id.textView1);
        //tv.setText(" Custom RelativeLayout");
    }
}
