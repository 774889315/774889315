package com.example.administrator.downloadtool10;

import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/3.
 */
public class Task {
    File fileDir = new File("/storage/emulated/0/dl");
    static String nowPath = "/storage/emulated/0/dl";
    URL url;
    String fileName;
    String fileExtension;
    File fileNow = null;
    public String size = "Unknown";
    long totalByte;
    long byte0;
    long finished;
    double percentage = 0;
    boolean completed = false;
    boolean notified = false;
    public int id = -1;
    private boolean paused;
    private RandomAccessFile raf;
    final int bufSize = 1024;
    public boolean started;

    int threadNum = 4;

    Thread[] t = new Thread[threadNum];

    private long finished0[] = new long[threadNum];

    Task(URL url)
    {
        fileDir = new File(nowPath);
        this.url = url;
        fileName = getFileName();
        fileExtension = getFileExtension();
        paused = false;
      //  FileOutputStream fos;
        fileNow = new File(fileDir + File.separator + fileName + fileExtension);
        for (int i = 1; fileNow.exists(); i++)
        {
            fileNow = new File(fileDir + File.separator + fileName + "_" + i + fileExtension);//防止覆盖
        }



        //       MainActivity.task0.setText("File name: " + fileName + "   File extension: " + fileExtension);
    }

    public void pause()
    {
        paused = true;
    }
    public boolean isPaused()
    {
        return paused;
    }

    String getSize()
    {
        //if(size.equals("Unknown"))
        {
            GetSize getSize = new GetSize(url);
            Thread th = new Thread(getSize);
            th.start();


            while(th.isAlive())
            {
                size = getSize.size0;
                totalByte = (long) getSize.fileSize;
                byte0 =(long) (totalByte / threadNum / bufSize) * bufSize;
            }
        }

        return size;
    }

    String getFileName()
    {
        String strName = url.getPath();
        strName = strName.substring(strName.lastIndexOf("/") + 1, strName.lastIndexOf("."));
        return strName;
    }

    String getFileExtension()
    {
        String strExtension = url.getPath();
        strExtension = strExtension.substring(Math.max(strExtension.lastIndexOf("."), strExtension.lastIndexOf("/") + 1)).toLowerCase();
        if (strExtension.equals(getFileName())) strExtension = "";
        return strExtension;
    }

    public void start()
    {
        getSize();
/*
        try
        {
            FileOutputStream fos = new FileOutputStream(fileNow);
            for(long d = 0; d < totalByte; d++) fos.write(0);
            fos.close();
        }catch(Exception e) {e.printStackTrace();}
*/
        for (int i = 0; i < threadNum; i++)
        {
            final int finalI = i;
            final long begin = i * byte0;
            final long end;
            if(i == threadNum - 1) end = totalByte + 1;
            else end = (i + 1)* byte0 - 1;
            final File finalFile = fileNow;
            try {
                raf = new RandomAccessFile(finalFile, "rwd");
                raf.setLength(totalByte);
            } catch (Exception e) {
                e.printStackTrace();
            }
            t[i] = new Thread() {
                public void run() {
                    try {
                        InputStream is;

                        download(raf, begin, end, finalI);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t[i].start();
        }
    }

    public void resume()
    {
        if(paused)
        {
            paused = false;
            try
            {
                raf = new RandomAccessFile(fileNow, "rwd");
                raf.setLength(totalByte);
                for (int i = 0; i < threadNum; i++)
                {
                    long loc = 0;
                    int a = 0;
                    File data = new File(fileNow + ".dat" + i);
                    try {
                        FileInputStream fin = new FileInputStream(data);
                        while (a != -1) {
                            a = fin.read();
                            if (a != -1) loc = loc * 10 + a - '0';
                        }
                    }catch(Exception e){
                        loc = 0;
                    }
                    //        Log.e("aaaaaaaaaaaaaaaaaaaaaa ", loc+"");
                    final int finalI = i;
       //             final File finalFile = fileNow;
                    final long begin = i * byte0 + loc;
                    final long end;
                    if (i == threadNum - 1) end = totalByte + 1;
                    else end = (i + 1) * byte0 - 1;
                    t[i] = new Thread()
                    {
                        public void run()
                        {
                            try
                            {
                                download(raf, begin, end, finalI);

                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };
                    t[i].start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void download(InputStream is, FileOutputStream fos)//第一代下载方法，缺点：单线程，没法暂停中断
    {
       try
       {
        //   final int bufSize = 128;
           byte buf[] = new byte[bufSize];
           for(;!paused;)
           {
               int numread = is.read(buf);
               if (numread <= 0)
               {
       //            completed = true;
        //           Message msg = new Message();
        //           msg.what = 10;
        //           handler.sendMessage(msg);
                   break;
               }
               fos.write(buf, 0, numread);
               finished += bufSize;
           }
       }
       catch(Exception e)
       {
           e.printStackTrace();
       }
    }

    private void download(RandomAccessFile raf, long begin, long end, int thread)
    {
        try
        {
            raf = new RandomAccessFile(fileNow, "rwd");
            raf.setLength(totalByte);
            byte buf[] = new byte[bufSize];
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setRequestProperty("Range", "bytes="+begin+"-"+end);

     //       raf.seek(begin);
            InputStream is = conn.getInputStream();
            if (is == null)
            {
                Log.e("conn: ", "a");
            }
            raf.seek(begin);
            while(!paused)
            {
                int numread = is.read(buf);
                if (numread <= 0)
                {
            //        raf.write(buf, 0, numread);
             //       completed = true;
        //            Message msg = new Message();
         //           msg.what = 10;
         //           handler.sendMessage(msg);
                    return;
                }
                raf.write(buf, 0, numread);
                finished += bufSize;
                finished0[thread] += bufSize;
            }
            File data = new File(fileNow + ".dat" + thread);
            FileOutputStream fos = new FileOutputStream(data);
            fos.write(Long.toString(finished0[thread]).getBytes());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    String percentage()
    {

        percentage = (finished+0.0) / totalByte * 100;
        if (percentage < 100) return(percentage + " %");
        boolean done = true;
        for(int i = 0; i < threadNum; i++)
        {
            if (t[i].isAlive()) done = false;
        }
        if(done)
        {
            completed = true;
            return ("Download successfully!");
        }
        return "99.99 %";
    }
}
