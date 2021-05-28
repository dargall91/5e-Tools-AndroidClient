package com.DnD5eTools.ui;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;

import com.DnD5eTools.R;
import com.DnD5eTools.client.DNDClientProxy;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import com.DnD5eTools.ui.main.SectionsPagerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static DNDClientProxy proxy;
    private final String SERVER = "Data/server.dat";
    private static boolean[] connection = {false};
    private static FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        BufferedReader reader = null;
        String host;
        int port;

        try {
            AssetManager assets = getApplicationContext().getAssets();
            reader = new BufferedReader(new InputStreamReader(assets.open(SERVER)));
            host = reader.readLine();
            System.out.println(host);
            port = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            Log.i("read error", e.getMessage());
            host = "localhost";
            port = 0;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }

        //TODO: cannot write to assets. Current solution for changing server works, but settings cannot be saved; find solution
        //for Rpi: 192.168.1.110, 8000
        //for pc: 192.168.1.118, 8000
        proxy = new DNDClientProxy(host, port);
        checkConnection();
    }

    private static void checkConnection() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connection[0] = proxy.isConnected();
                } catch (Exception e) {
                    Log.i("Connection", e.getMessage());
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        System.out.println("connected: " + connection[0]);
        return connection[0];
    }

    public static DNDClientProxy getProxy() {
        return proxy;
    }

    public static void updateProxy(String host, int port) {
        proxy.changeConnection(host, port);
        checkConnection();
        //updateActivities();
    }

    public static void updateActivities() {
        //CombatTracker tracker = new CombatTracker();
        MonsterBuilder monBuilder = new MonsterBuilder();
        CombatTracker ct = (CombatTracker) fm.findFragmentByTag("CombatTracker");
        //ct.refresh();
        //ct.onCreateView(ct.getLayoutInflater(), )
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}