package com.DnD5eTools.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DnD5eTools.R;
import com.DnD5eTools.client.DNDClientProxy;
import com.DnD5eTools.monster.Encounter;
import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.client.MonsterData;
import com.DnD5eTools.player.PlayerCharacter;
import com.DnD5eTools.client.Combatant;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Tab for tracking player data, combat, and housing the soundboard.
 */
public class CombatTracker extends Fragment {
    private DNDClientProxy proxy;
    private ArrayList<String> players;
    private Encounter[] encounter;
    private ArrayList<Combatant> combatants;
    private ArrayList<String> musicList;
    private View view;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private static CombatTracker tracker;
    private LinearLayout leftView;
    private final String PLAYER_CRIT = "Data/player_crit.dat";
    private final String MONSTER_CRIT = "Data/monster_crit.dat";
    private final String PLAYER_KILL = "Data/player_kill.dat";
    private final String MONSTER_KILL = "Data/monster_kill.dat";
    private final String TRAP = "Data/trap.dat";
    private final String FEAR = "Data/fear.dat";
    private final String AFFLICTION = "Data/affliction.dat";
    private final String AFFLICTED = "Data/afflicted.dat";
    private final String VIRTUE = "Data/virtue.dat";
    private final String ABUSIVE = "Data/abusive.dat";
    private final String IRRATIONAL = "Data/irrational.dat";
    private final String PARANOID = "Data/paranoid.dat";
    private final String SELFISH = "Data/selfish.dat";
    private final String FEARFUL = "Data/fearful.dat";
    private final String HOPELESS = "Data/hopeless.dat";
    private final String MASOCHISTIC = "Data/masochistic.dat";
    private final String POWERFUL = "Data/powerful.dat";
    private final String COURAGEOUS = "Data/courageous.dat";
    private final String STALWART = "Data/stalwart.dat";
    private final String VIGOROUS = "Data/vigorous.dat";
    private final String FOCUSED = "Data/focused.dat";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        encounter = new Encounter[1];
        this.container = container;
        this.savedInstanceState = savedInstanceState;
        proxy = MainActivity.getProxy();
        tracker = this;

        //inflate view if connected to server
        if (MainActivity.isConnected()) {
            view = inflater.inflate(R.layout.combat_tracker_layout, container, false);
            view.setTag("CombatTracker");
            view.setId(View.generateViewId());

            leftView = view.findViewById(R.id.combat_left_side);

            soundBoard();
            preCombatView();
        }

        return view;
    }

    public void refresh() {
        Fragment tracker = this;
        getFragmentManager().findFragmentById(tracker.getId());

        getFragmentManager().beginTransaction()
                .detach(tracker)
                .attach(tracker)
                .commit();
    }

    public static CombatTracker getTracker() {
        return tracker;
    }

    /**
     * Sets up the pre combat view (PC names, ac, bonus, delete button)
     */
    private void preCombatView() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    players = proxy.getPlayerCharacterList();
                    Collections.sort(players);
                } catch (Exception e) {
                    Log.i("Combat", e.getMessage());
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //TODO: 99% sure this line is unneeded
        View labels = inflater.inflate(R.layout.pre_combat_labels, leftView);

        //loop and add layouts
        for (int i = 0; i < players.size(); i++) {
            final int index = i;
            final PlayerCharacter[] pc = {null};

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        pc[0] = proxy.getPlayerCharacter(players.get(index));
                    } catch (Exception e) {
                        Log.i("Combat", e.getMessage());
                    }
                }
            });

            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            View playerView = inflater.inflate(R.layout.player_layout, leftView);

            TextView name = playerView.findViewById(R.id.player_name);
            name.setId(i);
            name.setText(players.get(i));
            name.setTag(i);

            //index of ac = ac - 1
            Spinner ac = playerView.findViewById(R.id.ac_spinner);
            ac.setId(i);
            ac.setSelection(pc[0].getAC() - 1, false);
            ac.setTag(i);

            ac.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Thread innerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pc[0].setAC(Integer.parseInt(ac.getSelectedItem().toString()));
                                proxy.updatePlayerCharacter(pc[0]);
                                Log.i("ac item select", "selected");
                            } catch (Exception e) {
                                Log.i("Combat", e.getMessage());
                            }
                        }
                    });

                    innerThread.start();

                    try {
                        innerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Thread innerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pc[0].setAC((Integer) ac.getSelectedItem());
                                proxy.updatePlayerCharacter(pc[0]);
                                Log.i("ac item select", "nothing");
                            } catch (Exception e) {
                                Log.i("Combat", e.getMessage());
                            }
                        }
                    });

                    innerThread.start();

                    try {
                        innerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            //index of bonus = bonus + 5
            Spinner bonus = playerView.findViewById(R.id.bonus_spinner);
            bonus.setId(i);
            bonus.setSelection(pc[0].getBonus() + 5, false);
            bonus.setTag(i);

            bonus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Thread innerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pc[0].setBonus(Integer.parseInt(bonus.getSelectedItem().toString()));
                                proxy.updatePlayerCharacter(pc[0]);
                                Log.i("bonus item select", "selected");
                            } catch (Exception e) {
                                Log.i("Combat", e.getMessage());
                            }
                        }
                    });

                    innerThread.start();

                    try {
                        innerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    //TODO: nothing needed here?
                    Thread innerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //pc[0].setAC((Integer) ac.getSelectedItem());
                                //proxy.updatePlayerCharacter(pc[0]);
                            } catch (Exception e) {
                                Log.i("Combat", e.getMessage());
                            }
                        }
                    });

                    innerThread.start();

                    try {
                        innerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            Button delete = playerView.findViewById((R.id.delete_player));
            delete.setId(i);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Thread innerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proxy.deletePlayerCharacter(pc[0].getName());
                                leftView.removeAllViewsInLayout();
                            } catch (Exception e) {
                                Log.i("Combat", e.getMessage());
                            }
                        }
                    });

                    innerThread.start();

                    try {
                        innerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    preCombatView();
                }
            });
        }

        View buttonLayout = inflater.inflate(R.layout.pre_combat_buttons, leftView);

        Button addPlayer = buttonLayout.findViewById((R.id.add_player));
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean[] addPC = {false};
                View addView = inflater.inflate(R.layout.add_player_dialog, null);
                TextView exists = addView.findViewById((R.id.pc_exists));
                EditText nameEntry = addView.findViewById((R.id.name_entry));

                final AlertDialog.Builder addPlayerDialog = new AlertDialog.Builder(getContext());
                addPlayerDialog.setView(addView);
                addPlayerDialog.setTitle("Add New PC");
                addPlayerDialog.setPositiveButton("OK", null);
                addPlayerDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("CANCEL", "cancel");
                    }
                });

                AlertDialog add = addPlayerDialog.create();
                add.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button ok = add.getButton(AlertDialog.BUTTON_POSITIVE);
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String entry = nameEntry.getText().toString();
                                Thread innerThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (!entry.equals(""))
                                                addPC[0] = proxy.addPlayerCharacter(entry);
                                        } catch (Exception e) {
                                            Log.i("Combat", e.getMessage());
                                        }
                                    }
                                });

                                innerThread.start();

                                try {
                                    innerThread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (addPC[0]) {
                                    add.dismiss();
                                    leftView.removeAllViewsInLayout();
                                    preCombatView();
                                }

                                else {
                                    exists.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });

                add.show();
            }
        });

        Button loadEncounter = buttonLayout.findViewById((R.id.load_encounter));
        loadEncounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String>[] encList = new ArrayList[1];//new ArrayList<String>[1];
                Thread innerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            encList[0] = proxy.getEncounterList();
                        } catch (Exception e) {
                            encList[0].add("No Encounters");
                            Log.i("Combat", e.getMessage());
                        }
                    }
                });

                innerThread.start();

                try {
                    innerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                View loadView = inflater.inflate(R.layout.choose_encounter_dialog, null);
                AutoCompleteTextView encName = loadView.findViewById(R.id.encounter_text);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, encList[0]);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                encName.setAdapter(adapter);

                encName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        encName.showDropDown();
                    }
                });

                final AlertDialog.Builder loadEnc = new AlertDialog.Builder(getContext());
                loadEnc.setTitle("Select an Encounter");
                loadEnc.setView(loadView);
                loadEnc.setNegativeButton("Cancel", null);

                loadEnc.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (encName.getText().toString().equals("")) {
                            return;
                        }

                        Thread innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    encounter[0] = proxy.getEncounter(encName.getText().toString());
                                    combatants = new ArrayList<Combatant>();
                                } catch (JSONException e) {
                                    Log.i("Invalid Encounter Name", e.getMessage());
                                    return;
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        leftView.removeAllViewsInLayout();
                        initiativeView();
                    }
                });

                loadEnc.setNegativeButton("Cancel", null);

                AlertDialog load = loadEnc.create();
                load.show();
            }
        });
    }

    /**
     * Set up the view for setting player initiatives
     */
    private void initiativeView() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    proxy.startCombat(encounter[0].getName());
                } catch (Exception e) {
                    Log.i("Combat", e.getMessage());
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //LinearLayout leftView = (LinearLayout) view.findViewById(R.id.combat_left_side);
        View labels = inflater.inflate(R.layout.initiative_labels, leftView);

        //loop for players
        for (int i = 0; i < encounter[0].getTotalPlayers(); i++) {
            final int index = i;

            View playerView = inflater.inflate(R.layout.player_initiative_layout, leftView);
            Spinner pcSpin = playerView.findViewById(R.id.pc_spinner);
            pcSpin.setId(index);
            pcSpin.setTag(index);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, players);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            pcSpin.setAdapter(adapter);
            pcSpin.setSelection(i, false);

            pcSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Thread innerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                combatants.get(index).setPlayerCharacter(proxy.getPlayerCharacter((String) pcSpin.getSelectedItem()));
                            } catch (Exception e) {
                                Log.i("pcSpin", e.getMessage());
                            }
                        }
                    });

                    innerThread.start();

                    try {
                        innerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            Spinner initRoll = playerView.findViewById(R.id.init_roll_spinner);
            initRoll.setId(index);
            initRoll.setTag(index);
            initRoll.setSelection(0, false);

            initRoll.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    combatants.get(index).setInitiative(Integer.parseInt((String) initRoll.getSelectedItem()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        combatants.add(new Combatant(proxy.getPlayerCharacter((String) pcSpin.getSelectedItem()),
                                Integer.parseInt((String) initRoll.getSelectedItem())));
                        Log.i("Combatant:", combatants.get(index).getName());
                    } catch (Exception e) {
                        Log.i("Combat", e.getMessage());
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

        //loop for adding monsters
        ArrayList<MonsterData> monData = encounter[0].getMonsterData();

        for(MonsterData i : monData) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        combatants.add(new Combatant(i, proxy.getMonster(i.getMonster())));
                    } catch (Exception e) {
                        Log.i("monData", e.getMessage());
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

        if (encounter[0].hasLairAction())
            combatants.add(new Combatant());

        View startEnc = inflater.inflate(R.layout.start_encounter_layout, leftView);
        Button start = startEnc.findViewById(R.id.start_encounter_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder beginEnc = new AlertDialog.Builder(getContext());
                beginEnc.setTitle("Start Encounter?");
                beginEnc.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        weighCombatants();

                        //sort the combatants
                        Collections.sort(combatants, Collections.reverseOrder());
                        JSONArray combatArray = new JSONArray();
                        for (Combatant i : combatants)
                            for (int j = 0; j < i.getQuantity(); j++)
                                if (!i.isReinforcement() && !i.isLairAction() && !i.isInvisible((j))) {
                                    try {
                                        combatArray.put(i.toSimpleJson());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                        Thread innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    proxy.updateCombat(combatArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        combatView();
                    }
                });

                beginEnc.setNegativeButton("No", null);

                AlertDialog alert = beginEnc.create();
                alert.show();
            }
        });
    }

    /**
     * Sorts the combatants, displays a tiebreaker alert dialog when necessary
     */
    private void weighCombatants() {
        for (int i = 0; i < combatants.size() - 1; i++) {
            final int index_i = i;
            for (int j = i + 1; j < combatants.size(); j++) {
                final int index_j = j;

                combatants.get(index_i).weigh(combatants.get(index_j));

                //if tied, run tie breaker alert
                if (combatants.get(index_i).isTied()) {
                    @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message mesg)
                        {
                            throw new RuntimeException();
                        }
                    };

                    View tieView = inflater.inflate(R.layout.tie_breaker_layout, null);
                    TextView textOne = tieView.findViewById(R.id.combatant_1);
                    textOne.setText(combatants.get(index_i).getName());
                    TextView textTwo = tieView.findViewById(R.id.combatant_2);
                    textTwo.setText(combatants.get(index_j).getName());
                    Spinner spinOne = tieView.findViewById(R.id.tie_1);
                    spinOne.setSelection(combatants.get(index_i).getBreaker());
                    Spinner spinTwo = tieView.findViewById(R.id.tie_2);
                    spinTwo.setSelection(combatants.get(index_j).getBreaker());

                    final AlertDialog.Builder tieBreaker = new AlertDialog.Builder(getContext());
                    tieBreaker.setTitle("Tie Breaker")
                            .setView(tieView)
                            .setCancelable(false)
                            .setPositiveButton("OK", null);

                    AlertDialog alert = tieBreaker.create();
                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (!spinOne.getSelectedItem().equals(spinTwo.getSelectedItem()) && !spinOne.getSelectedItem().equals("0") && !spinTwo.getSelectedItem().equals("0")) {
                                        combatants.get(index_i).setBreaker(Integer.parseInt((String) spinOne.getSelectedItem()));
                                        combatants.get(index_j).setBreaker(Integer.parseInt((String) spinTwo.getSelectedItem()));
                                        combatants.get(index_i).weigh(combatants.get(index_j));
                                        handler.sendMessage(handler.obtainMessage());
                                        alert.dismiss();
                                    }
                                }
                            });
                        }
                    });

                    alert.show();

                    try {
                        Looper.loop();
                    } catch(RuntimeException e) {}
                }
            }
        }
    }

    /**
     * Thew view for the combat
     */
    private void combatView() {
        leftView.removeAllViewsInLayout();

        //used for ids and tags of view elements
        int tag_counter = 1;

        for (Combatant i : combatants) {
            if (!i.isReinforcement()) {
                for (int j = 0; j < i.getQuantity(); j++) {
                    final int index = j;
                    
                    int tag = tag_counter * 10 + index;

                    View combatantView = inflater.inflate(R.layout.combatant_layout, leftView);

                    TextView initiative = combatantView.findViewById(R.id.initiative);
                    initiative.setId(tag);
                    initiative.setTag(tag);
                    tag++;

                    TextView name = combatantView.findViewById(R.id.name);
                    name.setId(tag);
                    name.setTag(tag);
                    tag++;

                    TextView ac_label = combatantView.findViewById(R.id.ac_label);
                    ac_label.setId(tag);
                    ac_label.setTag(tag);
                    tag++;

                    EditText ac_text = combatantView.findViewById(R.id.ac_text);
                    ac_text.setId(tag);
                    ac_text.setTag(tag);
                    tag++;

                    TextView hp_label = combatantView.findViewById(R.id.hp_label);
                    hp_label.setId(tag);
                    hp_label.setTag(tag);
                    tag++;

                    EditText hp_text = combatantView.findViewById(R.id.hp_text);
                    hp_text.setId(tag);
                    hp_text.setTag(tag);
                    tag++;

                    Button kill = combatantView.findViewById(R.id.kill_revive);
                    kill.setId(tag);
                    kill.setTag(tag);

                    Button remove = combatantView.findViewById(R.id.remove);
                    remove.setId(tag);
                    remove.setTag(tag);

                    initiative.setText(Integer.toString(i.getInitiative()));
                    name.setText(i.getName());
                    ac_text.setText(i.getAC(index));
                    ac_text.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            i.setAC(index, ac_text.getText().toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            i.setAC(index, ac_text.getText().toString());
                        }
                    });

                    if (i.isLairAction()) {
                        System.out.println(i.getName() + " is lair action");
                        ac_label.setVisibility(View.INVISIBLE);
                        ac_text.setVisibility(View.INVISIBLE);
                        name.setTextSize(22);
                        name.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    }

                    if (!i.isMonster()) {
                        System.out.println(i.getName() + " is not monster");
                        hp_label.setVisibility(View.INVISIBLE);
                        hp_text.setVisibility(View.INVISIBLE);
                        kill.setVisibility(View.INVISIBLE);
                    } else {
                        System.out.println(i.getName() + " is monster");
                        Monster[] monster = new Monster[1];

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    monster[0] = proxy.getMonster(i.getName());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        thread.start();

                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //ac_text.setText(i.getAC(index));
                        hp_text.setText(i.getHP(index));
                        hp_text.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) { }

                            @Override
                            public void afterTextChanged(Editable s) {
                                i.setHP(index, hp_text.getText().toString());
                            }
                        });

                        //set initial text of kill button
                        if (i.isInvisible(index)) {
                            kill.setText("Visible");
                        } else if (i.isAlive(index)) {
                            kill.setText("Kill");
                        } else {
                            kill.setText("Revive");
                        }

                        name.setEnabled(i.isAlive(index));
                        ac_text.setEnabled(i.isAlive(index));
                        hp_text.setEnabled(i.isAlive(index));

                        kill.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (i.isInvisible(index)) {
                                    i.setVisible(index);
                                    kill.setText("Kill");
                                } else if (i.isAlive(index)) {
                                    i.kill(index);
                                    kill.setText("Revive");

                                    playSound(MONSTER_KILL, "OTHER");

                                    remove.setVisibility(View.VISIBLE);
                                } else {
                                    i.revive(index);
                                    kill.setText("Kill");
                                }

                                name.setEnabled(i.isAlive(index));
                                ac_text.setEnabled(i.isAlive(index));
                                hp_text.setEnabled(i.isAlive(index));

                                //update server combat screen
                                JSONArray combatArray = new JSONArray();

                                for (Combatant c : combatants) {
                                    for (int k = 0; k < c.getQuantity(); k++) {
                                        //only show something on server screen if it's not a monster,
                                        //it's alive, and its not invisible
                                        if (!c.isReinforcement() && c.isAlive(k) && !c.isInvisible(k)) {
                                            try {
                                                combatArray.put(c.toSimpleJson());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                Thread innerThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            proxy.updateCombat(combatArray);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                innerThread.start();

                                try {
                                    innerThread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        remove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //if there's only 1 of this monster, get rid of it
                                //otherwise only remove one of this specific monster instance
                                if (i.getQuantity() == 1) {
                                    combatants.remove(i);
                                } else {
                                    i.remove(index);
                                }

                                //should be no need to update server screen because only
                                //dead monsters can be removed (they already don't show up)
                                /*
                                JSONArray combatArray = new JSONArray();

                                for (Combatant c : combatants) {
                                    for (int k = 0; k < c.getQuantity(); k++) {
                                        if (!c.isReinforcement() && c.isAlive(k) && !c.isInvisible(k)) {
                                            try {
                                                combatArray.put(c.toSimpleJson());
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                Thread innerThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            proxy.updateCombat(combatArray);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                innerThread.start();

                                try {
                                    innerThread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                */

                                combatView();
                            }
                        });
                    }
                }
            }

            tag_counter++;
        }

        View combatButtons = inflater.inflate(R.layout.combat_buttons, leftView);
        Button rein = combatButtons.findViewById(R.id.reinforcements_button);
        rein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View reinLayout = inflater.inflate(R.layout.main_reinforcement_layout, null);

                for (Combatant i : combatants) {
                    if (i.isReinforcement()) {
                        View innerLayout = inflater.inflate(R.layout.reinforcement_layout, (ViewGroup) reinLayout);

                        TextView initiative = reinLayout.findViewById(R.id.initiative);
                        initiative.setText(Integer.toString(i.getInitiative()));
                        initiative.setId(View.generateViewId());

                        TextView monName = reinLayout.findViewById(R.id.reinforcement_name);
                        monName.setText(i.getName() + " x" + i.getQuantity());
                        monName.setId(View.generateViewId());

                        CheckBox check = reinLayout.findViewById(R.id.reinforcement_checkbox);
                        check.setId(View.generateViewId());
                        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                //set to opposite of checked state (checked == true, but that would mean isReinforcement() == true)
                                i.setReinforcement(!isChecked);
                            }
                        });
                    }
                }

                final AlertDialog.Builder reinDialog = new AlertDialog.Builder(getContext());
                reinDialog.setTitle("Add Reinforcements");
                reinDialog.setView(reinLayout);
                reinDialog.setCancelable(false);
                reinDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONArray combatArray = new JSONArray();
                        for (Combatant i : combatants)
                            for (int j = 0; j < i.getQuantity(); j++)
                                if (!i.isReinforcement() && i.isAlive(j) && !i.isInvisible(j)) {
                                    try {
                                        combatArray.put(i.toSimpleJson());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                        Thread innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    proxy.updateCombat(combatArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        combatView();
                    }
                });

                AlertDialog alert = reinDialog.create();
                alert.show();
            }
        });

        Button newRein = combatButtons.findViewById(R.id.new_reinforcements_button);
        newRein.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String>[] monList = new ArrayList[]{new ArrayList<String>()};
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            monList[0] = proxy.getMonsterList();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                View outsideReinLayout = inflater.inflate(R.layout.outside_reinforcement_layout, null);
                AutoCompleteTextView monName = outsideReinLayout.findViewById(R.id.monster_text);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, monList[0]);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                monName.setAdapter(adapter);

                monName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        monName.showDropDown();
                    }
                });

                Spinner quantity = outsideReinLayout.findViewById(R.id.quantity_spinner);
                Spinner initiative = outsideReinLayout.findViewById(R.id.init_roll_spinner);

                final AlertDialog.Builder outsideRein = new AlertDialog.Builder(getContext());
                outsideRein.setTitle("Add Reinforcements");
                outsideRein.setView(outsideReinLayout);
                outsideRein.setNegativeButton("Cancel", null);
                outsideRein.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //check that a name was entered, return if not
                        if (monName.getText().toString().equals(""))
                            return;

                        Monster[] monster = new Monster[1];

                        Thread innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    monster[0] = proxy.getMonster(monName.getText().toString());
                                } catch (JSONException e) {
                                    //TODO: If this exception is thrown because of an invalid name, what happens? Should probably make it check that the monster exists here or just return
                                    e.printStackTrace();
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        combatants.add(new Combatant(monster[0], Integer.parseInt((String) quantity.getSelectedItem()), Integer.parseInt((String) initiative.getSelectedItem())));
                        System.out.println(combatants.get(combatants.size() - 1).getName());

                        for (Combatant i : combatants)
                            i.reset();

                        weighCombatants();

                        Collections.sort(combatants, Collections.reverseOrder());
                        JSONArray combatArray = new JSONArray();
                        for (Combatant i : combatants)
                            for (int j = 0; j < i.getQuantity(); j++)
                                if (!i.isReinforcement() && i.isAlive(j) && !i.isInvisible(j)) {
                                    try {
                                        System.out.println(i.getName());
                                        combatArray.put(i.toSimpleJson());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                        innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    proxy.updateCombat(combatArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        combatView();
                    }
                });

                AlertDialog alert = outsideRein.create();
                alert.show();
            }
        });

        //End the encounter and stop the music
        Button finishEndMusic = combatButtons.findViewById(R.id.finish_button_end_music);
        finishEndMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder finishEnc = new AlertDialog.Builder(getContext());
                finishEnc.setTitle("End the Encounter?");
                finishEnc.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Thread innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    proxy.endCombat();
                                    proxy.stopMusic();
                                } catch (Exception e) {
                                    Log.i("Combat", e.getMessage());
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        leftView.removeAllViewsInLayout();
                        preCombatView();
                    }
                });

                finishEnc.setNegativeButton("Cancel", null);

                AlertDialog alert = finishEnc.create();
                alert.show();
            }
        });

        //end the encounter but don't stop the music
        Button finishPlayMusic = combatButtons.findViewById(R.id.finish_button_music);
        finishPlayMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder finishEnc = new AlertDialog.Builder(getContext());
                finishEnc.setTitle("End the Encounter?");
                finishEnc.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Thread innerThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    proxy.endCombat();
                                } catch (Exception e) {
                                    Log.i("Combat", e.getMessage());
                                }
                            }
                        });

                        innerThread.start();

                        try {
                            innerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        leftView.removeAllViewsInLayout();
                        preCombatView();
                    }
                });

                finishEnc.setNegativeButton("Cancel", null);

                AlertDialog alert = finishEnc.create();
                alert.show();
            }
        });
    }

    private void soundBoard() {
        GridLayout clipGrid = view.findViewById(R.id.sound_clip_gird);

        Button enemyCrit = clipGrid.findViewById(R.id.enemy_crit);
        enemyCrit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(MONSTER_CRIT, "OTHER");
            }
        });

        Button playerCrit = clipGrid.findViewById(R.id.player_crit);
        playerCrit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(PLAYER_CRIT, "OTHER");
            }
        });

        Button enemyDeath = clipGrid.findViewById(R.id.enemy_death);
        enemyDeath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(MONSTER_KILL, "OTHER");
            }
        });

        Button playerDeath = clipGrid.findViewById(R.id.player_death);
        playerDeath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(PLAYER_KILL, "OTHER");
            }
        });

        Button trap = clipGrid.findViewById(R.id.trap);
        trap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(TRAP, "OTHER");
            }
        });

        Button fear = clipGrid.findViewById(R.id.fear);
        fear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(FEAR, "OTHER");
            }
        });

        Button irrational = clipGrid.findViewById(R.id.irrational);
        irrational.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(IRRATIONAL, AFFLICTION);
            }
        });

        Button powerful = clipGrid.findViewById(R.id.powerful);
        powerful.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(POWERFUL, VIRTUE);
            }
        });

        Button paranoid = clipGrid.findViewById(R.id.paranoid);
        paranoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(PARANOID, AFFLICTION);
            }
        });

        Button courageous = clipGrid.findViewById(R.id.courageous);
        courageous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(COURAGEOUS, VIRTUE);
            }
        });

        Button selfish = clipGrid.findViewById(R.id.selfish);
        selfish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(SELFISH, AFFLICTION);
            }
        });

        Button stalwart = clipGrid.findViewById(R.id.stalwart);
        stalwart.setOnClickListener(v -> playSound(STALWART, VIRTUE));

        Button abusive = clipGrid.findViewById(R.id.abusive);
        abusive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(ABUSIVE, AFFLICTION);
            }
        });

        Button vigorous = clipGrid.findViewById(R.id.vigorous);
        vigorous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(VIGOROUS, VIRTUE);
            }
        });

        Button fearful = clipGrid.findViewById(R.id.fearful);
        fearful.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(FEARFUL, AFFLICTION);
            }
        });

        Button focused = clipGrid.findViewById(R.id.focused);
        focused.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(FOCUSED, VIRTUE);
            }
        });

        Button hopeless = clipGrid.findViewById(R.id.hopeless);
        hopeless.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(HOPELESS, AFFLICTION);
            }
        });

        Button masochistic = clipGrid.findViewById(R.id.masochistic);
        masochistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(MASOCHISTIC, AFFLICTION);
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    musicList = proxy.getMusicList();
                    Collections.sort(musicList);
                } catch (Exception e) {
                    Log.i("update", e.getMessage());
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LinearLayout musicPlayer = view.findViewById(R.id.music_player);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, musicList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner title = musicPlayer.findViewById(R.id.music_title);
        title.setAdapter(adapter);
        title.setSelection(0,false);

        Button pause = musicPlayer.findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread innerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.pauseMusic();
                        } catch (Exception e) {
                            Log.i("Combat", e.getMessage());
                        }
                    }
                });

                innerThread.start();

                try {
                    innerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button play = musicPlayer.findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread innerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.playMusic(title.getSelectedItem().toString());
                        } catch (Exception e) {
                            Log.i("Combat", e.getMessage());
                        }
                    }
                });

                innerThread.start();

                try {
                    innerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Button stop = musicPlayer.findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread innerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.stopMusic();
                        } catch (Exception e) {
                            Log.i("Combat", e.getMessage());
                        }
                    }
                });

                innerThread.start();

                try {
                    innerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Plays a sound based on the button clicked on the soundboard.
     *
     * @param dat the file path for the appropriate .dat file
     * @param stressType AFFLICTION if an affliction, VIRTUE if a virtue, any other non-null value otherwise
     */
    private void playSound(String dat, String stressType) {
        BufferedReader reader = null;

        if (stressType.equals(AFFLICTION) || stressType.equals(VIRTUE)) {
            try {
                AssetManager assets = getContext().getAssets();
                reader = new BufferedReader(new InputStreamReader(assets.open(stressType)));
                String path = reader.readLine();
                String[] sound = assets.list(path);
                MediaPlayer mp = new MediaPlayer();
                AssetFileDescriptor afd = assets.openFd(path + sound[0]);
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.i("playSound", e.getMessage());
                    }
                }
            }
        }

        try {
            //if it's an affliction, 50% chance of generic voice over, and 50% chance of an affliction specific voice over
            if (stressType.equals(AFFLICTION)) {
                Random rand = new Random();
                int affl = rand.nextInt(2);
                if (affl < 1)
                    dat = AFFLICTED;
            }

            AssetManager assets = getContext().getAssets();
            reader = new BufferedReader(new InputStreamReader(assets.open(dat)));
            String path = reader.readLine();
            String[] fileNames = assets.list(path);
            Random rand = new Random();
            MediaPlayer mp = new MediaPlayer();
            AssetFileDescriptor afd = assets.openFd(path + fileNames[rand.nextInt(fileNames.length)]);
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            Log.i("sound", dat + ": " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }
    }
}