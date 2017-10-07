package com.example.administrator.downloadtool10;

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

class Task {
    static String nowPath = "/storage/emulated/0/dl";
    URL url;
    String fileName;
    String fileExtension;
    File fileNow = null;
    private String size = "Unknown";
    private long totalByte;
    private long byte0;
    long finished;
    double percentage = 0;
    boolean completed = false;
    boolean notified = false;
    public int id = -1;
    private final int bufSize = 1024;
    boolean started;
    long speed;

    private int threadNum = 4;

    private Thread[] t = new Thread[threadNum];

    private boolean paused;
    private RandomAccessFile raf;
    private long finished0[] = new long[threadNum];
    private boolean resumed;

    Task(URL url)
    {
        File fileDir = new File(nowPath);
        this.url = url;
        fileName = getFileName();
        fileExtension = getFileExtension();
        paused = false;
      //  FileOutputStream fos;
        fileNow = new File(fileDir + File.separator + fileName + fileExtension);
        for (int i = 1; ! new File(fileNow + ".dat0").exists() && fileNow.exists(); i++)
        {
            fileNow = new File(fileDir + File.separator + fileName + "_" + i + fileExtension);//防止覆盖
        }



        //       MainActivity.task0.setText("File name: " + fileName + "   File extension: " + fileExtension);
    }

    void pause()
    {
        if(!Objects.equals(percentage(), "99.99 %"))
        paused = true;
        resumed = false;
    }
    boolean isPaused()
    {
        return paused;
    }

    private Thread getSpeed = new Thread()
    {
        @Override
        public void run()
        {
            for(; !completed;)
            {
                long finished0 = finished;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                speed = finished - finished0;
            }
        }
    };

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
                byte0 = totalByte / threadNum / bufSize * bufSize;
            }
        }

        return size;
    }

    private String getFileName()
    {
        String strName = url.getPath();
        strName = strName.substring(strName.lastIndexOf("/") + 1, strName.lastIndexOf("."));
        return strName;
    }

    private String getFileExtension()
    {
        String strExtension = url.getPath();
        strExtension = strExtension.substring(Math.max(strExtension.lastIndexOf("."), strExtension.lastIndexOf("/") + 1)).toLowerCase();
        if (strExtension.equals(getFileName())) strExtension = "";
        return strExtension;
    }

    void start()
    {
        getSize();
        resumed = false;
        if(new File(fileNow + ".dat0").exists())
        {
            paused = true;
            resume();
            getSpeed.start();
            return;
        }
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
                    try
                    {
                        download(begin, end, finalI);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t[i].start();
        }
        getSpeed.start();
    }

    void resume()
    {
        if(paused && !resumed)
        {
            resumed = true;
            try {
                Thread.sleep(300);//延迟300毫米，防止切换过快而出错
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            paused = false;
            try
            {
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
                        e.printStackTrace();
                    }
                    finished0[i] = loc;
                    final int finalI = i;
                    final long begin = i * byte0 + loc;
                    final long end;
                    if (i == threadNum - 1) end = totalByte + 1;
                    else end = (i + 1) * byte0 - 1;
                    raf = new RandomAccessFile(fileNow, "rwd");
                    raf.setLength(totalByte);
                    t[i] = new Thread()
                    {
                        public void run()
                        {
                            try
                            {
                                download(begin, end, finalI);

                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };
                    t[i].start();
                }
                finished = 0;
                for (int i = 0; i < threadNum; i++)
                {
                    finished += finished0[i];
                }
                percentage();

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

    private void download(long begin, long end, int thread)
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(fileNow, "rwd");
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
                assert is != null;
                int numread = is.read(buf);
                if (numread <= 0)
                {
                    return;
                }
                raf.write(buf, 0, numread);
                finished += bufSize;
                finished0[thread] += bufSize;
            }
            File data = new File(fileNow + ".dat" + thread);
            FileOutputStream fos = new FileOutputStream(data);
            finished0[thread] -= bufSize * 512;
            fos.write((finished0[thread] + "").getBytes());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    String percentage()
    {
        percentage = (finished+0.0) / totalByte * 100;
        if (percentage < 100) return((int)(percentage * 100)/ 100.0 + " %");
        boolean done = true;
        for(int i = 0; i < threadNum; i++)
        {
            if (t[i].isAlive()) done = false;
        }
        if(done)
        {
            completed = true;
            speed = 0;
            for(int i = 0; i < threadNum; i++)
            {
                try
                {
                    new File(fileNow + ".dat" + i).delete();
                }
                catch(Exception e)
                {
                }
            }
            return ("Download successfully!");
        }
        return "99.99 %";
    }
}
