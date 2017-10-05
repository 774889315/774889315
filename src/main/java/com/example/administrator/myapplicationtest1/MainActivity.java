package com.example.administrator.myapplicationtest1;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends Activity implements View.OnClickListener {
    private Button sendNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendNotice = (Button) findViewById(R.id.send_notice);
        sendNotice.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_notice:
                NotificationManager manager = (NotificationManager)
                        getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new Notification(R.drawable.
                        icon, "This is ticker text", System.currentTimeMillis());


                Notification.Builder builder1 = new Notification.Builder(MainActivity.this);
                builder1.setSmallIcon(R.drawable.icon); //设置图标
                builder1.setTicker("显示第二个通知");
                builder1.setContentTitle("通知"); //设置标题
                builder1.setContentText("点击查看详细内容"); //消息内容
                builder1.setWhen(System.currentTimeMillis()); //发送时间
                builder1.setDefaults(Notification.DEFAULT_ALL); //设置默认的提示音，振动方式，灯光
                builder1.setAutoCancel(true);//打开程序后图标消失
                Intent intent =new Intent (MainActivity.this,MainActivity.class);
                PendingIntent pendingIntent =PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                builder1.setContentIntent(pendingIntent);
                Notification notification1 = builder1.build();
                manager.notify(124, notification1); // 通过通知管理器发送通知
                break;
            default:
                break;
        }
    }
}