package com.example.administrator.downloadtool10;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final int UPDATE = 1;
    public static final int SHOW_NORMAL = 2;
    public static final int SHOW_TIMEOUT = 3;
    public static final int SHOW_COMPLETED = 4;
    public static final int SHOW_PAUSED = 5;
    EditText input;
    TextView task0;
    TextView taskStatus;
    TextView startStatus;
    TextView num;
    Button bt;
    Button bt2;
    Button p;
    Button rs;
    Button prev;
    Button next;
    Button del;
    Button open;
    ProgressBar pb;
    URL url0;
    int taskNum = 0;
    double finished = 0.0;
    double speed = 0.0;
    int time = 0;
    int a = 0;
    EditText inputPath;
    Intent startIntent;

    private MyService.DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            downloadBinder = (MyService.DownloadBinder) service;
        }
    };

    private static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

    public static String getMimeType(File file){
        String suffix = getSuffix(file);
        if (suffix == null) {
            return "file/*";
        }
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
        if (type != null) {
            return type;
        }
        return "file/*";
    }

    void notify0(String fileName, String progress, long speed, int id)
    {
        String speed0;
        if(speed <= 1000) speed0 = speed + " B/s";
        else if(speed <= 1e6) speed0 = (int)(speed/1024.0 * 100)/ 100.0 + " kB/s";
        else speed0 = (int)(speed/1024.0/1024.0 * 100)/ 100.0 + " MB/s";
        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder1 = new Notification.Builder(MainActivity.this);
        builder1.setSmallIcon(R.drawable.icon);
        builder1.setContentTitle(fileName);
        builder1.setContentText("Downloading...  -  " + progress + "\t" + speed0);
        builder1.setWhen(System.currentTimeMillis());
        builder1.setAutoCancel(true);
        Intent intent = getIntent();
        PendingIntent pendingIntent =PendingIntent.getActivity(this, 0, intent, 0);
        builder1.setContentIntent(pendingIntent);
        Notification notification1 = builder1.build();
        manager.notify(id, notification1);
    }

    void notify1(String fileName, int id, File fileNow)
    {

        NotificationManager manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder1 = new Notification.Builder(MainActivity.this);
        builder1.setSmallIcon(R.drawable.icon);
        builder1.setContentTitle(fileName);
        builder1.setContentText("Completed");
        builder1.setWhen(System.currentTimeMillis());
        builder1.setDefaults(Notification.DEFAULT_SOUND);
        long[] vibrates = {0, 800, 1000, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200, 100, 200};
        builder1.setVibrate(vibrates);
        builder1.setAutoCancel(true);
        Intent intent =new Intent(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", fileNow);
            intent.setDataAndType(contentUri, getMimeType(fileNow));
        }
        else
        {
            intent.setDataAndType(Uri.fromFile(fileNow), getMimeType(fileNow));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        PendingIntent pendingIntent =PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        builder1.setContentIntent(pendingIntent);
        Notification notification1 = builder1.build();
        manager.notify(id, notification1);
    }

    public Handler handler = new Handler()
    {
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
                startStatus.setText("Completed.");

                break;
            case SHOW_TIMEOUT:
                startStatus.setTextColor(Color.rgb(250, 120, 50));
                startStatus.setText("Timeout!");
                downloadBinder.pauseTask(taskNum);
                break;
            case SHOW_PAUSED:
                startStatus.setTextColor(Color.rgb(180, 200, 50));
                startStatus.setText("Paused.");
                break;
            case UPDATE:
                refresh();
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        startIntent = new Intent(this, MyService.class);
        bindService(startIntent, connection, BIND_AUTO_CREATE);
        setContentView(R.layout.activity_main);
        input = (EditText)findViewById(R.id.editText);//inputURL
        bt = (Button)findViewById(R.id.button);//start
        p = (Button)findViewById(R.id.button3);//pause
        rs = (Button)findViewById(R.id.button4);//resume
        pb = (ProgressBar) findViewById(R.id.progressBar);
        startStatus = (TextView)findViewById(R.id.textView3);
        num = (TextView)findViewById(R.id.textView4);
        prev = (Button)findViewById(R.id.button5);
        next = (Button)findViewById(R.id.button6);
        del = (Button)findViewById(R.id.button7);
        open = (Button)findViewById(R.id.button8);

        inputPath = (EditText)findViewById(R.id.editText2);
        inputPath.setText(Task.nowPath);




        bt.setOnClickListener(this);

        bt2 = (Button) findViewById(R.id.button2);
        bt2.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                File temp = new File(inputPath.getText().toString());
                if (temp.exists())
                {
                    Task.nowPath = inputPath.getText().toString();
                    Toast.makeText(getApplicationContext(), "Change successfully! New path will come into effect next time!", Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(getApplicationContext(), "Error! Cannot find directory!", Toast.LENGTH_SHORT).show();
            }
        });

        prev.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if (taskNum >= 1) taskNum --;
                else if(downloadBinder.getTaskSize() != 0) taskNum = downloadBinder.getTaskSize() - 1;
                refresh();
            }
        });

        next.setOnClickListener(new Button.OnClickListener()
        {
            public void onClick(View v)
            {
                if (taskNum < downloadBinder.getTaskSize() - 1) taskNum ++;
                else if(downloadBinder.getTaskSize() != 0) taskNum = 0;
                refresh();
            }
        });

        p.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                downloadBinder.pauseTask(taskNum);
            }
        });

        rs.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startService (startIntent);
            }
        });

        del.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(downloadBinder.getTaskSize() != 0)
                {
                    downloadBinder.pauseTask(taskNum);
                    downloadBinder.removeTask(taskNum);
                    refresh();
                }
            }
        });

        open.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(downloadBinder.getTaskSize() > 0 && downloadBinder.getTaskIsCompleted(taskNum))
                {
                    File fileNow = downloadBinder.getFile(taskNum);
                    Intent intent =new Intent(Intent.ACTION_VIEW);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", fileNow);
                        intent.setDataAndType(contentUri, getMimeType(fileNow));
                    }
                    else
                    {
                        intent.setDataAndType(Uri.fromFile(fileNow), getMimeType(fileNow));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    try
                    {
                        startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        Toast.makeText(getApplicationContext(), "Can't open the file!", Toast.LENGTH_SHORT).show();//防止文件类型无法打开
                    }
                }
                else if(downloadBinder.getTaskSize() > 0)
                {
                    Toast.makeText(getApplicationContext(), "Please wait while file is downloading!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void onStart()
    {
        super.onStart();

        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            input.setText(cm.getText());
        } catch (Exception ignored) { } //若剪切板内容不是URL则不会自动粘贴

        taskStatus = (TextView)findViewById(R.id.textView2);

        new Thread()
        {
            public void run()
            {
                for(;;)
                {

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    Message message = new Message();
                    message.what = UPDATE;
                    handler.sendMessage(message);
                }
            }
        }.start();
    }

    void refresh()
    {
        if (downloadBinder.getTaskSize() >= 1)
        {
        //    speed = (realSpeed + speed * 1)/ 2;
            speed = downloadBinder.getSpeed(taskNum);
            if (speed == 0) time ++;
            else
            {
                time = 0;
                Message message = new Message();
                message.what = SHOW_NORMAL;
                handler.sendMessage(message);
            }
            if (time >= 2) speed = 0;
            if (downloadBinder.getTaskIsPaused(taskNum))
            {
                Message message = new Message();
                message.what = SHOW_PAUSED;
                handler.sendMessage(message);
            }
            if (downloadBinder.getTaskIsCompleted(taskNum))
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
            finished = downloadBinder.getTaskFinished(taskNum);
            String speed;
            if(this.speed <= 1000) speed = this.speed + " B/s";
            else if(this.speed <= 1e6) speed = (int)(this.speed/1024.0 * 100)/ 100.0 + " kB/s";
            else speed = (int)(this.speed/1024.0/1024.0 * 100)/ 100.0 + " MB/s";
            taskStatus.setTextColor(Color.rgb(120, 50, 200));
            taskStatus.setText(downloadBinder.getTaskPercentage(taskNum) + "\t\t\t" + speed);
            pb.setMax(10000);
            pb.setProgress((int) (downloadBinder.getTaskPercentageN(taskNum) * 100));
            setTitle("WarlfDownloadTool - Task "+ taskNum);
            task0.setText("File name: " + downloadBinder.getFileWholeName(taskNum)
      //              + "\nURL: " + downloadBinder.getTaskURL(taskNum)
                    + "\nSize: " + downloadBinder.initFileSize(taskNum));
            num.setText("Task " + taskNum);
            for(int i = 0; i < downloadBinder.getTaskSize(); i++)
                if (!downloadBinder.getTaskIsCompleted(i))
                {
                    notify0(downloadBinder.getFileWholeName(i), downloadBinder.getTaskPercentage(i), downloadBinder.getSpeed(i), downloadBinder.getTaskId(i));
                }

                else if (!downloadBinder.getNotified(i))
                {
                    notify1(downloadBinder.getFileWholeName(i), downloadBinder.getTaskId(i), downloadBinder.getFile(i));
                    downloadBinder.setNotified(i);
                }
        }
        else if(task0 != null)
        {
            num.setText("null");
            task0.setText("Hello, world!");
            taskStatus.setText("Hello, Warlf!");
            setTitle("WarlfDownloadTool 1.0 - null");
            pb.setProgress(0);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
        case R.id.button:
            try
            {
                task0 = (TextView)findViewById(R.id.textView);
                url0 = new URL(input.getText().toString());
                downloadBinder.addTask(url0);
                downloadBinder.initTaskId(taskNum, a++);
                startStatus.setTextColor(Color.rgb(0, 150, 0));
                startStatus.setText("Start successfully!");
                input.setText("");

                startService (startIntent);
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
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(downloadBinder.getTaskSize() > 0)
        {
            for (int i = 0; i < downloadBinder.getTaskSize(); i++)
            {
                downloadBinder.pauseTask(i);
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

