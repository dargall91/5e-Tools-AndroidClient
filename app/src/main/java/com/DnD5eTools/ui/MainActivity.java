package com.DnD5eTools.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;

import com.DnD5eTools.R;
import com.DnD5eTools.client.DNDClientProxy;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.DnD5eTools.ui.main.SectionsPagerAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static DNDClientProxy proxy;
    private final String HOME_SERVER = "Data/home_server.dat";
    private final String CHRIS_SERVER = "Data/chris_server.dat";
    private final String PC_SERVER = "Data/pc_server.dat";
    private static boolean[] connection = {false};

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

        setConnection();
    }

    private void setConnection() {
        View serverSelect = LayoutInflater.from(this).inflate(R.layout.select_server_dialog, (ViewGroup) findViewById(R.id.main_activity), false);
        Spinner select = serverSelect.findViewById(R.id.select);

        final android.app.AlertDialog.Builder connectionDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        connectionDialog.setView(serverSelect);
        connectionDialog.setTitle("Select Server");
        connectionDialog.setCancelable(false);
        connectionDialog.setPositiveButton("Select", null);

        android.app.AlertDialog connect = connectionDialog.create();
        connect.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button change = connect.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                change.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (select.getSelectedItem().equals("Home")) {
                            initDNDClientProxy(HOME_SERVER);
                        } else if (select.getSelectedItem().equals("Chris's House")) {
                            initDNDClientProxy(CHRIS_SERVER);
                        } else if (select.getSelectedItem().equals("PC")) {
                            initDNDClientProxy(PC_SERVER);
                        } else {
                            //TODO: implement dialog for non-preset server host
                                /*View specifyConn = inflater.inflate(R.layout.specify_connection_dialog, null);
                                EditText host = specifyConn.findViewById(R.id.host);
                                EditText port = specifyConn.findViewById(R.id.port);

                                final android.app.AlertDialog.Builder specifyConnDialog = new android.app.AlertDialog.Builder(null);
                                specifyConnDialog.setView(specifyConn);
                                specifyConnDialog.setTitle("Specify Server");
                                specifyConnDialog.setCancelable(true);
                                specifyConnDialog.setPositiveButton("Set Server", null);
                                specifyConnDialog.setNegativeButton("Cancel", null);

                                android.app.AlertDialog specify = specifyConnDialog.create();
                                specify.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {
                                        Button set = specify.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                                        set.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                proxy = new DNDClientProxy(host.getText().toString(), Integer.parseInt(port.getText().toString()));
                                                checkConnection();
                                                specify.dismiss();
                                            }
                                        });
                                    }
                                });

                                specify.show();*/
                        }

                        connect.dismiss();
                    }
                });
            }
        });

        connect.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //ensure successful connection to server
                MainActivity.checkConnection();

                if (!isConnected()) {
                    setConnection();
                } else {
                    CombatTracker.getTracker().refresh();
                    EncounterBuilder.getEncBuilder().refresh();
                    MonsterBuilder.getMonBuilder().refresh();
                }
                //refresh();
            }
        });

        connect.show();
    }

    /**
     * Reads the host and port data from the specified asset file
     * and initializes the DNDClientProxy using that data
     *
     * @param asset The asset file with the required data
     */
    public void initDNDClientProxy(String asset) {
        BufferedReader reader = null;
        String host;
        int port;

        try {
            AssetManager assets = getApplicationContext().getAssets();
            reader = new BufferedReader(new InputStreamReader(assets.open(asset)));
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

        proxy = new DNDClientProxy(host, port);
    }

    public static void checkConnection() {
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
        //checkConnection();
        System.out.println("connected: " + connection[0]);
        return connection[0];
    }

    public static DNDClientProxy getProxy() {
        return proxy;
    }

    /**
     * Changes the DNDClientProxy connection
     *
     * @param host
     * @param port
     */
    public static void changeProxyServer(String host, int port) {
        proxy.changeConnection(host, port);

        //update connection status
        checkConnection();
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