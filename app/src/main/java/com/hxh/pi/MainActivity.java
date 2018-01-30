package com.hxh.pi;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);

        initData();
    }

    private void initData()
    {
        String text = getPackage() +
                "\n" + getInfo() +
                "\n" + getHeightAndWidth() +
                "\n" + getCpuInfo() +
                "\n" + getTotalSDCard() +
                "\n" + getAvailSDCard() +
                "\n" + getTotalROM() +
                "\n" + getAvailROM() +
                "\n" + getTotalRAM() +
                "\n" + getAvailRAM() +
                "\n" + isRoot() +
                "\n" + getMacAddress();

        tv.setText(text);
    }

    //获取软件包名,版本名，版本号
    private String getPackage()
    {
        try
        {
            String pkName = getPackageName();
            int versionCode = getPackageManager().getPackageInfo(pkName, 0).versionCode;
            String versionName = getPackageManager().getPackageInfo(pkName, 0).versionName;

            return "包名:" + pkName +
                    "\n版本号:" + versionCode +
                    "\n版本名:" + versionName;
        }
        catch (Exception e) {}

        return null;
    }

    //获取IMEI号，IESI号，手机型号
    private String getInfo()
    {
        TelephonyManager mTm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        String imsi = mTm.getSubscriberId();

        String mtyb = android.os.Build.BRAND;//手机品牌
        String mtype = android.os.Build.MODEL; // 手机型号
        String syscode = android.os.Build.VERSION.RELEASE; // 系统版本号
        String numer = mTm.getLine1Number(); // 手机号码，有的可得，有的不可得

        return "手机IMEI号：" + imei +
                "\n手机IESI号：" + imsi +
                "\n手机品牌：" + mtyb +
                "\n手机型号：" + mtype +
                "\n系统版本号：" + syscode +
                "\n手机号码" + numer;
    }

    //获得手机屏幕宽高
    public String getHeightAndWidth()
    {
        int width = getWindowManager().getDefaultDisplay().getWidth();
        int heigth = getWindowManager().getDefaultDisplay().getHeight();

        String str = "屏幕宽度:" + width +
                "\n屏幕高度:" + heigth + "";

        return str;
    }

    //手机CPU信息
    private String getCpuInfo()
    {
        String str1 = "/proc/cpuinfo";
        String str2 = "";
        String[] cpuInfo = {"", ""}; //1-cpu型号 //2-cpu频率
        String[] arrayOfString;
        try
        {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            for (int i = 2; i < arrayOfString.length; i++)
            {
                cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
            }
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            cpuInfo[1] += arrayOfString[2];
            localBufferedReader.close();
        }
        catch (IOException e) {}

        return "CPU型号:" + cpuInfo[0] +
                "\nCPU频率：" + cpuInfo[1];
    }

    //获取总SDCard
    private String getTotalSDCard()
    {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        return "总SDCard：" + Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
    }

    //获取可用SDCard
    private String getAvailSDCard()
    {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return "可用SDCard：" + Formatter.formatFileSize(this, blockSize * availableBlocks);
    }

    //获取总ROM
    private String getTotalROM()
    {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();

        return "总ROM：" + Formatter.formatFileSize(this, blockSize * totalBlocks);
    }

    //获取可用ROM
    private String getAvailROM()
    {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        return "可用ROM：" + Formatter.formatFileSize(this, blockSize * availableBlocks);
    }

    public String getTotalRAM()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        return "总RAM：" + Formatter.formatFileSize(this, mi.totalMem);
    }

    public String getAvailRAM()
    {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        return "可用RAM：" + Formatter.formatFileSize(this, mi.availMem);
    }

    //获取手机是否root信息
    private String isRoot()
    {
        String bool = "Root:false";

        try
        {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists()))
            {
                bool = "Root:false";
            }
            else
            {
                bool = "Root:true";
            }
        }
        catch (Exception e) {}

        return bool;
    }

    //获取手机MAC地址
    // 只有手机开启wifi才能获取到mac地址
    private String getMacAddress()
    {
        String result = "";
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getMacAddress();

        return "手机macAdd:" + result;
    }
}
