package com.example.administrator.downloadtool10;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/3.
 */

public class MyService extends Service
{
    ArrayList<Task> task  = new ArrayList<>();

    private DownloadBinder db = new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent)
    {
        return db;

    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
     //   for(;;)
        {
            if(task.size() > 0)
            {
                for (int i = 0; i < task.size(); i ++)
                {
                    if (!task.get(i).started)
                    {
                        task.get(i).started = true;
                        task.get(i).start();
                    }
                }
            }
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        return flags;
    }

    @Override
    public void onDestroy()
    {
    }


    class DownloadBinder extends Binder
    {
        int getTaskSize()
        {
            return task.size();
        }

        long getTaskFinished(int num)
        {
            return task.get(num).finished;
        }

        boolean getTaskIsPaused(int num)
        {
            return task.get(num).isPaused();
        }

        boolean getTaskIsCompleted(int num)
        {
            return task.get(num).completed;
        }

        String getTaskPercentage(int num)
        {
            return task.get(num).percentage();
        }

        double getTaskPercentageN(int num)
        {
            return task.get(num).percentage;
        }

        String getFileWholeName(int num)
        {
            return task.get(num).fileName + task.get(num).fileExtension;
        }

        URL getTaskURL(int num)
        {
            return task.get(num).url;
        }

        String initFileSize(int num)
        {
            return task.get(num).getSize();
        }

   //     String getFileSize(int num)
        {
   //         return task.get(num).size;
        }

        int getTaskId(int num)
        {
            return task.get(num).id;
        }

        void initTaskId(int num, int id)
        {
            task.get(num).id = id;
        }

        File getFile(int num)
        {
            return task.get(num).fileNow;
        }

        void pauseTask(int num)
        {
            task.get(num).pause();
        }

        void resumeTask(int num)
        {
            task.get(num).resume();
        }

        void removeTask(int num)
        {
            task.remove(num);
        }

        void setNotified(int num)
        {
            task.get(num).notified = true;
        }

        boolean getNotified(int num)
        {
            return task.get(num).notified;
        }

        void addTask(URL url)
        {
            task.add(0, new Task(url));
        }
    }
}
