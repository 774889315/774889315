package com.example.administrator.downloadtool10;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final int UPDATE = 1;
    public static final int SHOW_NORMAL = 2;
    public static final int SHOW_TIMEOUT = 3;
    public static final int SHOW_COMPLETED = 4;
    public static final int NOTIFY = 10;
    EditText input;
    TextView task0;
    TextView taskStatus;
    TextView startStatus;
    TextView num;
    Button bt;
    Button bt2;
    Button prev;
    Button next;
    ProgressBar pb;
    URL url0;
    ArrayList<Task> task = new ArrayList<Task>();
    int taskNum = 0;
    double finished = 0.0;
    double speed = 0.0;
    int time = 0;
    int a = 0;
    EditText inputPath;


/*
    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notf;

    notf = new Notification(R.drawable.icon, "DownloadTool", System.currentTimeMillis());

        notf.setLatestEventInfo(context, "", "", null);
*/
    void notify0(String fileName, String progress, int id)
    {
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder1 = new Notification.Builder(MainActivity.this);
        builder1.setSmallIcon(R.drawable.icon); //设置图标
        //     builder1.setTicker("显示第二个通知");
        builder1.setContentTitle(fileName); //设置标题
        builder1.setContentText("正在下载  -  " + progress); //消息内容
        builder1.setWhen(System.currentTimeMillis()); //发送时间
        builder1.setAutoCancel(true);//打开程序后图标消失
        Intent intent =new Intent (MainActivity.this, MainActivity.class);
        PendingIntent pendingIntent =PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        builder1.setContentIntent(pendingIntent);
        Notification notification1 = builder1.build();
        manager.notify(id, notification1); // 通过通知管理器发送通知
    }

    void notify1(String fileName, int id)
    {

        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder1 = new Notification.Builder(MainActivity.this);
        builder1.setSmallIcon(R.drawable.icon); //设置图标
   //     builder1.setTicker("显示第二个通知");
        builder1.setContentTitle(fileName); //设置标题
        builder1.setContentText("下载完成"); //消息内容
        builder1.setWhen(System.currentTimeMillis()); //发送时间
        builder1.setDefaults(Notification.DEFAULT_SOUND); //设置默认的提示音，振动方式，灯光
        long[] vibrates = {0, 800, 1000, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200};
        builder1.setVibrate(vibrates);
        builder1.setAutoCancel(true);//打开程序后图标消失
        Intent intent =new Intent (MainActivity.this,MainActivity.class);
        PendingIntent pendingIntent =PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        builder1.setContentIntent(pendingIntent);
        Notification notification1 = builder1.build();
        manager.notify(id, notification1); // 通过通知管理器发送通知
    }

    public Handler handler = new Handler()
    {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case SHOW_NORMAL:
                startStatus.setTextColor(Color.rgb(80, 200, 50));
                startStatus.setText("Downloading...");
                break;
            case SHOW_COMPLETED:
                startStatus.setTextColor(Color.rgb(80, 200, 50));
                startStatus.setText("COMPLETED.");

                break;
            case SHOW_TIMEOUT:
                startStatus.setTextColor(Color.rgb(250, 120, 50));
                startStatus.setText("Timeout!");
                break;
            case UPDATE:
                refresh();
                break;
            case NOTIFY:
     //           notify1();
                break;
            default:
                break;
            }
        }
    };
 //   String task[] = new String[100];
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        input = (EditText)findViewById(R.id.editText);
        bt = (Button)findViewById(R.id.button);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        startStatus = (TextView)findViewById(R.id.textView3);
        num = (TextView)findViewById(R.id.textView4);
        prev = (Button)findViewById(R.id.button5);
        next = (Button)findViewById(R.id.button6);

        inputPath = (EditText)findViewById(R.id.editText2);
        inputPath.setText(Task.fileDir.toString());





        bt.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View view)
            {

                try
                {
                    task0 = (TextView)findViewById(R.id.textView);
                    url0 = new URL(input.getText().toString());
                    task.add(0, new Task(url0));
                    task.get(taskNum).id = a ++;
                    task.get(taskNum).start();
                //    if(i != 0) Log.e("Failed!!", "Exception "+i);
                //    else
                    {
                  //      taskStatus.setText("Download successfully!");
                    }
                    startStatus.setTextColor(Color.rgb(0, 150, 0));
                    startStatus.setText("Start successfully!");
                    input.setText("");
                }
                catch (MalformedURLException e)
                {
                    task0.setText("Please input valid URL!");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    taskStatus.setText("Failed!");
                }
            }
        });

        bt2 = (Button) findViewById(R.id.button2);
        bt2.setOnClickListener(new Button.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                File temp = new File(inputPath.getText().toString());
                if (temp.exists())
                {
                    Task.fileDir = new File(inputPath.getText().toString());
                    Toast.makeText(getApplicationContext(), "Change successfully!", Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(getApplicationContext(), "Error! Cannot find directory!", Toast.LENGTH_SHORT).show();
            }
        });

        prev.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if (taskNum >= 1) taskNum --;
                else if(task.size() != 0) taskNum = task.size() - 1;
                refresh();
            }
        });

        next.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if (taskNum < task.size() - 1) taskNum ++;
                else if(task.size() != 0) taskNum = 0;
                refresh();
            }
        });


    }

    protected void onStart()
    {
        super.onStart();

        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            URL u = new URL(cm.getText().toString());
            input.setText(cm.getText());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        taskStatus = (TextView)findViewById(R.id.textView2);
/*
        Thread th = new Thread(){
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (task.size() >= 1) taskStatus.setText(task.get(taskNum).percentage());
            }
        };
*/
     //   th.start();


//        new LooperThread("").run();

        new Thread()
        {
            public void run()
            {
                for(;;)
                {

                    Message message = new Message();
                    message.what = UPDATE;
                    handler.sendMessage(message);
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }
/*
    private class LooperThread extends Thread {

        private String text;

        public LooperThread(String text) {
            this.text = text;
        }

        @Override
        public void run() {
             if (task.size() >= 1) taskStatus.setText(task.get(taskNum).percentage());
        }
    }
*/
    double realSpeed;
    void refresh()
    {
        if (task.size() >= 1)
        {
            speed = realSpeed;
        //    speed = (realSpeed + speed * 1)/ 2;
            realSpeed = task.get(taskNum).finished - finished;
            if (realSpeed == 0) time ++;
            else
            {
                time = 0;
                Message message = new Message();
                message.what = SHOW_NORMAL;
                handler.sendMessage(message);
            }
            if (time >= 2) speed = 0;
            if (task.get(taskNum).completed)
            {
                Message message = new Message();
                message.what = SHOW_COMPLETED;
                handler.sendMessage(message);
            }
            else if (time >= 7)
            {
                Message message = new Message();
                message.what = SHOW_TIMEOUT;
                handler.sendMessage(message);
            }
            finished = task.get(taskNum).finished;
            String speed = "0.0 B/s";
            if(this.speed <= 1000) speed = this.speed + " B/s";
            else if(this.speed <= 1e6) speed = this.speed/1024 + " kB/s";
            else speed = this.speed/1024/1024 + " MB/s";
            taskStatus.setTextColor(Color.rgb(120, 50, 200));
            taskStatus.setText(task.get(taskNum).percentage() + "\t\t\t" + speed);
            pb.setMax(10000);
            pb.setProgress((int) (task.get(taskNum).percentage * 100));
            setTitle("MyDownloadTool - Task "+ taskNum);
            task0.setText("File name: " + task.get(taskNum).getFileName() + "   File extension: " + task.get(taskNum).getFileExtension()
                    + "\nURL: " + task.get(taskNum).url + "\nSize: " + task.get(taskNum).getSize());
            num.setText("Task " + taskNum);
            for(int i = 0; i < task.size(); i++)
                if (!task.get(i).completed)
                {
                    notify0(task.get(i).fileName + task.get(i).fileExtension, task.get(i).percentage(), task.get(i).id);
                }

                else if (!task.get(i).notified)
                {
                    notify1(task.get(i).fileName + task.get(i).fileExtension, task.get(i).id);
                    task.get(i).notified = true;
                }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
        case R.id.button:
       //     Intent startIntent = new Intent(this, MyService.class);
        //    startService(startIntent);
        }

    }
}

