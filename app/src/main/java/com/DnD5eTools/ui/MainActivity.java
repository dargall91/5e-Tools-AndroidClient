package com.DnD5eTools.ui;

import android.content.res.Resources;
import android.os.Bundle;

import com.DnD5eTools.R;

import com.DnD5eTools.interfaces.AbstractInterface;
import com.DnD5eTools.models.ServerConnection;
import com.DnD5eTools.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.DnD5eTools.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(3);

        AbstractInterface.init();

        displaySelectConnectionDialog();
    }

    /**
     * Opens the dialog to select the server connection
     */
    private void displaySelectConnectionDialog() {
        boolean[] isConnected = { false };

        //build spinner list
        List<ServerConnection> serverConnections = getServerConnections();
        List<String> connectionNames = new ArrayList<>();

        for (ServerConnection con : serverConnections) {
            connectionNames.add(con.getName());
        }

        ArrayAdapter<String> conNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                connectionNames);

        View serverSelect = LayoutInflater.from(this).inflate(R.layout.select_server_dialog,
                findViewById(R.id.main_activity), false);
        TextView invalidCon = serverSelect.findViewById(R.id.invalid_connection);
        Spinner select = serverSelect.findViewById(R.id.select);
        select.setAdapter(conNameAdapter);

        final android.app.AlertDialog.Builder connectionDialog = new android.app.AlertDialog.Builder(MainActivity.this);
        connectionDialog.setView(serverSelect);
        connectionDialog.setTitle("Select Server Connection");
        connectionDialog.setCancelable(false);
        connectionDialog.setPositiveButton("Select", null);

        android.app.AlertDialog connect = connectionDialog.create();
        connect.setOnShowListener(dialogInterface -> {
            Button change = connect.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            change.setOnClickListener(view -> {
                Util.setServerConnection(serverConnections.get(select.getSelectedItemPosition()));

                //validate connection, display custom connection dialog if no connection string set
                if (Util.getServerConnection().getUrl().isEmpty()) {
                    isConnected[0] = false;
                    displayCustomConnectionDialog();
                    connect.dismiss();
                } else {
                    isConnected[0] = Util.isConnectedToServer();
                }

                //if connected dismiss, otherwise display error message
                if (isConnected[0]) {
                    connect.dismiss();
                } else {
                    invalidCon.setVisibility(View.VISIBLE);
                }
            });
        });

        connect.setOnDismissListener(dialog -> {
            //if connected load the tabs
            if (isConnected[0]) {
                loadAllTabs();
            }
        });

        connect.show();
    }

    private void displayCustomConnectionDialog() {
        final boolean[] cancelled = { false };
        final boolean[] connected = { false };

        View customConnectionView = LayoutInflater.from(this).inflate(R.layout.custom_connection_dialog,
                (ViewGroup) findViewById(R.id.main_activity), false);
        TextView invalidCon = customConnectionView.findViewById(R.id.invalid_connection);
        EditText host = customConnectionView.findViewById(R.id.host);
        EditText port = customConnectionView.findViewById(R.id.port);

        final android.app.AlertDialog.Builder customConnectionDialog =
                new android.app.AlertDialog.Builder(MainActivity.this);
        customConnectionDialog.setView(customConnectionView);
        customConnectionDialog.setTitle("Custom Server Connection");
        customConnectionDialog.setCancelable(true);
        customConnectionDialog.setPositiveButton("Set Connection", null);
        customConnectionDialog.setNegativeButton("Cancel", null);

        android.app.AlertDialog custom = customConnectionDialog.create();
        custom.setOnShowListener(dialogInterface -> {
            Button set = custom.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            Button cancel = custom.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

            set.setOnClickListener(view -> {
                String url = "https://" + host.getText() + ":" + port.getText() + "/";
                Util.getServerConnection().setUrl(url);
                connected[0] = Util.isConnectedToServer();

                //do not dismiss if not connected
                if (connected[0]) {
                    custom.dismiss();
                } else {
                    invalidCon.setVisibility(View.VISIBLE);
                }
            });

            cancel.setOnClickListener(v -> {
                cancelled[0] = true;
                custom.dismiss();
            });
        });

        custom.setOnDismissListener(dialog -> {
            //if cancelled, return to normal connection selection, otherwise load views
            if (cancelled[0]) {
                displaySelectConnectionDialog();
            } else {
                loadAllTabs();
            }
        });

        custom.show();
    }

    private void loadAllTabs() {
        sectionsPagerAdapter.getCombatTracker().initViews();
        sectionsPagerAdapter.getMonsterBuilder().initViews();
        sectionsPagerAdapter.getEncounterBuilder().initViews();
    }

    private List<ServerConnection> getServerConnections() {
        Resources resources = getResources();
        String[] jsonConnections = resources.getStringArray(R.array.serverConnections);
        List<ServerConnection> serverConnections = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        for (String jsonCon : jsonConnections) {
            try {
                ServerConnection connection = mapper.readValue(jsonCon, ServerConnection.class);
                serverConnections.add(connection);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return serverConnections;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }
}