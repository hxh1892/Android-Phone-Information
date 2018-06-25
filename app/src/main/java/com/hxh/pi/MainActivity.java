package com.hxh.pi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
{
    private Context mContext = this;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tv);

        getPermission();
    }

    @SuppressLint("CheckResult")
    private void getPermission()
    {
        new RxPermissions(this)
                .requestEach(Manifest.permission.READ_PHONE_STATE)
                .subscribe(new io.reactivex.functions.Consumer<Permission>()
                {
                    @Override
                    public void accept(Permission permission)
                    {
                        if (permission.granted)
                        {
                            //用户已经同意该权限
                            initData();
                        }
                        else if (permission.shouldShowRequestPermissionRationale)
                        {
                            //用户拒绝了该权限，没有选中『不再询问』,再次启动时，还会提示请求权限的对话框
                            Toast.makeText(mContext, "无该权限无法查看信息", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                        else
                        {
                            //用户拒绝了该权限，并且选中『不再询问』
                            //启动系统权限设置界面
                            Toast.makeText(mContext, "在该页面中点击“权限”进入，开启“电话”权限", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);

                            finish();
                        }
                    }
                });
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
        @SuppressLint({"MissingPermission", "HardwareIds"}) String imei = mTm.getDeviceId();
        @SuppressLint({"MissingPermission", "HardwareIds"}) String imsi = mTm.getSubscriberId();

        String mtyb = android.os.Build.BRAND;//手机品牌
        String mtype = android.os.Build.MODEL; // 手机型号
        String syscode = android.os.Build.VERSION.RELEASE; // 系统版本号
        @SuppressLint({"MissingPermission", "HardwareIds"}) String number = mTm.getLine1Number(); // 手机号码，有的可得，有的不可得

        return "手机IMEI号：" + imei +
                "\n手机IESI号：" + imsi +
                "\n手机品牌：" + mtyb +
                "\n手机型号：" + mtype +
                "\n系统版本号：" + syscode +
                "\n手机号码" + number;
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
        catch (Exception ignored) {}

        return bool;
    }

    //获取手机MAC地址
    // 只有手机开启wifi才能获取到mac地址
    @SuppressLint("HardwareIds")
    private String getMacAddress()
    {
        String result = "";
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getMacAddress();

        return "手机macAdd:" + result;
    }
}
