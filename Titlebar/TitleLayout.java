package com.example.zncu.maketoast;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


public class TitleLayout extends LinearLayout {

    public TitleLayout(Context context, AttributeSet attrs){
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.title,this);
        //重写了LinearLayout中带有两个参数带构造函数，from（）方法构建出一个LayoutInflater对象，然后调用inflate（）方法就可以动态加载一个布局文件，
        //inflate（）方法接收两个参数，第一个参数是要加载带布局文件带id，第二个是给加载好的布局再添加一个父布局，我们指定为TitleLayout，传入this


        Button titleBack = (Button) findViewById(R.id.title_back);
        Button titleEdit = (Button) findViewById(R.id.title_edit);

        titleBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).finish();
            }
        });

        titleEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Hello Toast!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
