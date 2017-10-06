package com.example.administrator.downloadtool10;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2017/10/3.
 */
public class GetSize extends Task implements Runnable
{

    GetSize(URL url) {
        super(url);
    }

    String size0 = "Unknown";
    double fileSize = 1e12;
    public void run() {
        try
        {
            URLConnection urlcon = (HttpURLConnection) url.openConnection();
            fileSize = urlcon.getContentLength();
            if(fileSize <= 1000) size0 = fileSize + " B";
            else if(fileSize <= 1e6) size0 = fileSize/1024 + " kB";
            else if(fileSize <= 1e9) size0 = fileSize/1024/1024 + " MB";
            else size0 = fileSize/1024/1024/1024 + " GB";
        } catch (IOException e)

        {
            e.printStackTrace();
        }
    }
}
