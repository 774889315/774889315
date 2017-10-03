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

/**
 * Created by Administrator on 2017/10/3.
 */

public class MyService extends Service
{
    private DownloadBinder mb = new DownloadBinder();
    @Override
    public IBinder onBind(Intent intent)
    {
        stopSelf();
        return null;

    }


    private class DownloadBinder extends Binder
    {
        URL url;
        String fileName;
        String fileExtension;
        FileOutputStream fos;
        String size = "Unknown";
        double totalByte;
        double finished;
        double percentage = 0;
        boolean completed = false;

        DownloadBinder()
        {

        }
        DownloadBinder(URL url)
        {
            this.url = url;
            fileName = getFileName();
            fileExtension = getFileExtension();
            FileOutputStream fos;


            //       MainActivity.task0.setText("File name: " + fileName + "   File extension: " + fileExtension);
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
                    totalByte = getSize.fileSize;
                }
            }

            return size;
        }

        String getFileName()
        {
            String strName = url.getPath();
            strName = strName.substring(strName.lastIndexOf("/") + 1, strName.lastIndexOf(".") - 1);
            return strName;
        }

        String getFileExtension()
        {
            String strExtension = url.getPath();
            strExtension = strExtension.substring(Math.max(strExtension.lastIndexOf("."), strExtension.lastIndexOf("/") + 1)).toLowerCase();
            if (strExtension.equals(getFileName())) strExtension = "";
            return strExtension;
        }

        void start()
        {
            Thread th = new Thread() {
                public void run() {
                    try

                    {
                        InputStream is;
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(3 * 1000);
                        //        conn.connect();
                        //   Log.e("conn: ", conn.toString());
                        is = url.openStream();

                        //    InputStream is = conn.getInputStream();

                        if (is == null) {
                            Log.e("conn: ", "a");
                        }
                        File fileDir = new File("/storage/emulated/0/dl");
//                    if (!fileDir.exists())
                        {
//                        fileDir.mkdir();
                        }
                        File file = new File(fileDir.toString() + File.separator + fileName + fileExtension);
                        for(int i = 1; file.exists(); i++)
                        {
                            file = new File(fileDir + File.separator + fileName + "_" + i + fileExtension);//防止覆盖
                        }
                        fos = new FileOutputStream(file);
                        download(is, fos);

                    } catch (
                            Exception e)

                    {
                        e.printStackTrace();
                    }
                }
            };
            th.start();
        }

        private void download(InputStream is, FileOutputStream fos)
        {
            try
            {
                final int bufSize = 128;
                byte buf[] = new byte[bufSize];
                for(;;)
                {
                    int numread = is.read(buf);
                    if (numread <= 0)
                    {
                        completed = true;
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

        String percentage()
        {

            percentage = finished / totalByte * 100;
            if (percentage < 100) return(percentage + " %");
            return ("Download successfully!");

        }
    }
}
