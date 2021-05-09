package com.DnD5eTools.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DnD5eTools.R;
import com.DnD5eTools.client.DNDClientProxy;
import com.DnD5eTools.monster.Encounter;
import com.DnD5eTools.client.MonsterData;
import com.DnD5eTools.client.PlayerData;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Tabs for creating and editing Encounters.
 */
public class EncounterBuilder extends Fragment {
    private DNDClientProxy proxy;
    private Encounter[] encounter;
    private ArrayList<String> encList;
    private ArrayList<String> musicList;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private static EncounterBuilder builder;
    private View view;
    private LinearLayout playerLevelsContainer;
    private LinearLayout monstersContainer;
    private final String ADD_ENCOUNTER = "   Add Encounter";
    final int DELAY = 250;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        encounter = new Encounter[1];
        this.inflater = inflater;
        proxy = MainActivity.getProxy();
        this.container = container;
        this.savedInstanceState = savedInstanceState;
        builder = this;

        view = inflater.inflate(R.layout.encounter_builder_layout, container, false);
        view.setId(View.generateViewId());
        view.setTag("EncounterBuilder");

        if (MainActivity.isConnected()) {
            playerLevelsContainer = view.findViewById(R.id.encounter_player_levels_container);
            monstersContainer = view.findViewById(R.id.encounter_monsters_container);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        musicList = proxy.getMusicList();
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

            encounterListView(null);
            builderView();
        }


        return view;
    }

    public static EncounterBuilder getEncBuilder() {
        return builder;
    }

    public void refresh() {
        EncounterBuilder builder = this;
        getFragmentManager().findFragmentById(builder.getId());

        getFragmentManager().beginTransaction()
                .detach(builder)
                .attach(builder)
                .commit();

        onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Sets up the ListView that contains a list of all the encounters on the server
     *
     * @param name the name of the monster to load, null should be used to load the first encounter in the encounter lists
     */
    private void encounterListView(String name) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    encList = proxy.getEncounterList();
                    encList.add(ADD_ENCOUNTER);
                    Collections.sort(encList);

                    if (name == null)
                        encounter[0] = proxy.getEncounter(encList.get(1));

                    else
                        encounter[0] = proxy.getEncounter(name);

                } catch (Exception e) {
                    Log.i("EncList", e.getMessage());
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ListView encListView = view.findViewById(R.id.encounter_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_view, encList);
        encListView.setAdapter(adapter);
        encListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //check if add encounter was selected, should be first encounter in list
                if (encList.get(position).equals(ADD_ENCOUNTER)) {
                    View addView = inflater.inflate(R.layout.rename_add_encounter_dialog, null);
                    TextView exists = addView.findViewById(R.id.encounter_exists);
                    TextView text = addView.findViewById(R.id.name_textview);
                    text.setText("Enter the new encounter's name:");
                    EditText newName = addView.findViewById(R.id.name_entry);

                    final AlertDialog.Builder renameEncounterDialog = new AlertDialog.Builder(getContext());
                    renameEncounterDialog.setView(addView);
                    renameEncounterDialog.setTitle("Add Encounter");
                    renameEncounterDialog.setPositiveButton("OK", null);
                    renameEncounterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("CANCEL", "cancel");
                        }
                    });

                    AlertDialog add = renameEncounterDialog.create();
                    add.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            Button ok = add.getButton(AlertDialog.BUTTON_POSITIVE);
                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean[] success = new boolean[1];
                                    final String name = newName.getText().toString();
                                    Thread thread = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                success[0] = proxy.addEncounter(name);
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

                                    if (success[0]) {
                                        add.dismiss();
                                        encounterListView(name);
                                        builderView();
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
                //if the selected encounter is the current one, do nothing and exit
                if (encounter[0].getName().equals(encList.get(position)))
                    return;

                //update encounter on server, get new encounter
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.updateEncounter(encounter[0]);
                            encounter[0] = proxy.getEncounter(encList.get(position));
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

                builderView();
            }
        });
    }

    private void builderView() {
        nameView();
        playerLevels();
        difficulty();
        musicLair();
        monsters();
    }

    private void nameView() {
        View nameView = view.findViewById(R.id.encounter_name_buttons_layout);
        TextView name = nameView.findViewById(R.id.name);
        name.setText(encounter[0].getName());
        Button save = nameView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.updateEncounter(encounter[0]);
                            proxy.saveEncounter(encounter[0].getName());
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
            }
        });

        Button delete = nameView.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: confirmation, then delete (also needs done in mon builder
            }
        });

        Button rename = nameView.findViewById(R.id.rename);
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View renameView = inflater.inflate(R.layout.rename_add_encounter_dialog, null);
                TextView exists = renameView.findViewById(R.id.encounter_exists);
                TextView text = renameView.findViewById(R.id.name_textview);
                text.setText("Enter a new name for " + encounter[0].getName());
                EditText newName = renameView.findViewById(R.id.name_entry);

                final AlertDialog.Builder renameEncounterDialog = new AlertDialog.Builder(getContext());
                renameEncounterDialog.setView(renameView);
                renameEncounterDialog.setTitle("Rename " + encounter[0].getName());
                renameEncounterDialog.setPositiveButton("OK", null);
                renameEncounterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("CANCEL", "cancel");
                    }
                });

                AlertDialog rename = renameEncounterDialog.create();
                rename.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button ok = rename.getButton(AlertDialog.BUTTON_POSITIVE);
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean[] success = new boolean[1];
                                final String oldName = encounter[0].getName();
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            encounter[0].setName(newName.getText().toString());
                                            success[0] = proxy.renameEncounter(oldName, encounter[0]);
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

                                if (success[0]) {
                                    rename.dismiss();
                                    encounterListView(encounter[0].getName());
                                    nameView();
                                }

                                else {
                                    encounter[0].setName(oldName);
                                    exists.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });

                rename.show();
            }
        });
    }

    private void playerLevels() {
        if (playerLevelsContainer.getChildCount() > 1)
            playerLevelsContainer.removeViewsInLayout(1, playerLevelsContainer.getChildCount() - 1);

        ArrayList<PlayerData> playerData = encounter[0].getPlayerData();

        for (int i = 0; i < playerData.size(); i++) {
            final int index = i;
            View playerLevels = inflater.inflate(R.layout.encounter_player_levels_layout, playerLevelsContainer);
            Spinner players = playerLevels.findViewById(R.id.num_players);
            players.setId(index);
            players.setTag(index);
            players.setSelection(playerData.get(index).getPlayers() - 1, false);
            System.out.println("index: " + index);
            players.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    encounter[0].updateNumPlayers(index, position + 1);
                    difficulty();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            Spinner level = playerLevels.findViewById(R.id.level);
            level.setId(index);
            level.setTag(index);
            level.setSelection(playerData.get(index).getLevel() - 1, false);
            level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    encounter[0].updatePlayerLevel(index, position + 1);
                    difficulty();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            Button delete = playerLevels.findViewById(R.id.delete);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    encounter[0].deletePlayerData(index);
                    playerLevels();
                    difficulty();
                }
            });
        }

        inflater.inflate(R.layout.add_player_level_button, playerLevelsContainer);
        Button add = view.findViewById(R.id.add_player_level);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encounter[0].addPlayerData();
                playerLevels();
                difficulty();
            }
        });
    }

    //TODO: music selector, add lair action functionality (also requires update of combat tracker to mark lair actions), add option for second combat track (also requires update of tracker)
    private void difficulty() {
        View difficultyView = view.findViewById(R.id.encounter_difficulty_layout);
        TextView easy = difficultyView.findViewById(R.id.easy);
        easy.setText("Easy: " + encounter[0].getEasyThreshold());
        TextView medium = difficultyView.findViewById(R.id.medium);
        medium.setText("Medium: " + encounter[0].getMediumThreshold());
        TextView hard = difficultyView.findViewById(R.id.hard);
        hard.setText("Hard: " + encounter[0].getHardThreshold());
        TextView deadly = difficultyView.findViewById(R.id.deadly);
        deadly.setText("Deadly: " + encounter[0].getDeadlyThreshold());
        TextView budget = difficultyView.findViewById(R.id.budget);
        budget.setText("Daily Budget: " + encounter[0].getDailyBudget());
        TextView total = difficultyView.findViewById(R.id.total);
        total.setText("Total XP: " + encounter[0].getXPTotal());

        switch (encounter[0].getDifficulty()) {
            case "Trivial":
                easy.setTypeface(Typeface.DEFAULT);
                medium.setTypeface(Typeface.DEFAULT);
                hard.setTypeface(Typeface.DEFAULT);
                deadly.setTypeface(Typeface.DEFAULT);
                break;
            case "Easy":
                easy.setTypeface(Typeface.DEFAULT_BOLD);
                medium.setTypeface(Typeface.DEFAULT);
                hard.setTypeface(Typeface.DEFAULT);
                deadly.setTypeface(Typeface.DEFAULT);
                break;
            case "Medium":
                easy.setTypeface(Typeface.DEFAULT);
                medium.setTypeface(Typeface.DEFAULT_BOLD);
                hard.setTypeface(Typeface.DEFAULT);
                deadly.setTypeface(Typeface.DEFAULT);
                break;
            case "Hard":
                easy.setTypeface(Typeface.DEFAULT);
                medium.setTypeface(Typeface.DEFAULT);
                hard.setTypeface(Typeface.DEFAULT_BOLD);
                deadly.setTypeface(Typeface.DEFAULT);
                break;
            case "Deadly":
                easy.setTypeface(Typeface.DEFAULT);
                medium.setTypeface(Typeface.DEFAULT);
                hard.setTypeface(Typeface.DEFAULT);
                deadly.setTypeface(Typeface.DEFAULT_BOLD);
                break;
        }
    }

    private void musicLair() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, musicList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View musicLairView = view.findViewById(R.id.encounter_music_lair_layout);
        Spinner musicSpinner = musicLairView.findViewById(R.id.music);
        musicSpinner.setAdapter(adapter);
        musicSpinner.setSelection(musicList.indexOf(encounter[0].getTheme()), false);
        musicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                encounter[0].setTheme(musicSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        CheckBox lair = musicLairView.findViewById(R.id.lair_action);
        lair.setChecked(encounter[0].hasLairAction());
        lair.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                encounter[0].setLairAction(isChecked);
            }
        });
    }

    private void monsters() {
        if (monstersContainer.getChildCount() > 1)
            monstersContainer.removeViewsInLayout(1, monstersContainer.getChildCount() - 1);

        ArrayList<MonsterData> monData = encounter[0].getMonsterData();

        /*TODO: finish this method, add music player to sound board, update ServerCombatScreen for music
        player, update DNDClientProxy for playing any music*/

        for (int i = 0; i < monData.size(); i++) {
            final int index = i;
            View monster = inflater.inflate(R.layout.encounter_monster_layout, monstersContainer);
            TextView name = monster.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(monData.get(index).getMonster());

            EditText quantity = monster.findViewById(R.id.quantity);
            quantity.setId(index);
            quantity.setTag(index);
            quantity.setText(Integer.toString(monData.get(index).getQuantity()));
            quantity.addTextChangedListener(new TextWatcher() {
                Handler handler;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (handler != null)
                        handler.removeCallbacks(null);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int monQuantity = Integer.parseInt(quantity.getText().toString());

                            if (monQuantity == 0) {
                                encounter[0].deleteMonsterData(index);
                                monsters();
                            }

                            else
                                encounter[0].updateMonsterQuantity(index, monQuantity);

                            difficulty();
                        }
                    }, DELAY);
                }
            });

            Button plus = monster.findViewById(R.id.plus_quantity);
            plus.setId(index);
            plus.setTag(index);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int monQuantity = monData.get(index).getQuantity() + 1;
                    encounter[0].updateMonsterQuantity(index, monQuantity);
                    quantity.setText(Integer.toString(monQuantity));
                    difficulty();
                }
            });

            Button minus = monster.findViewById(R.id.minus_quantity);
            minus.setId(index);
            minus.setTag(index);
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int monQuantity = monData.get(index).getQuantity() - 1;

                    if (monQuantity == 0) {
                        encounter[0].deleteMonsterData(index);
                        monsters();
                    }

                    else {
                        encounter[0].updateMonsterQuantity(index, monQuantity);
                        quantity.setText(Integer.toString(monQuantity));
                    }

                    difficulty();
                }
            });

            CheckBox reinforcement = monster.findViewById(R.id.reinforcement);
            reinforcement.setId(index);
            reinforcement.setTag(index);
            reinforcement.setChecked(monData.get(i).isReinforcement());
            reinforcement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    encounter[0].setReinforcement(index, isChecked);
                }
            });

            CheckBox minion = monster.findViewById(R.id.minion);
            minion.setId(index);
            minion.setTag(index);
            minion.setChecked(monData.get(i).isMinion());
            minion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    encounter[0].setMinion(index, isChecked);
                    difficulty();
                }
            });

            Spinner initative = monster.findViewById(R.id.initiative);
            initative.setId(index);
            initative.setTag(index);
            initative.setSelection(monData.get(index).getInitiative() - 1, false);
            initative.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    encounter[0].setInitiative(index,  initative.getSelectedItemPosition() + 1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        inflater.inflate(R.layout.add_monster_in_encounter_button, monstersContainer);
        Button add = view.findViewById(R.id.add_monster_in_encounter);
        add.setOnClickListener(new View.OnClickListener() {
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

                View addMonster = inflater.inflate(R.layout.add_monster_in_encounter_layout, null);
                AutoCompleteTextView monName = addMonster.findViewById(R.id.monster_text);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, monList[0]);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                monName.setAdapter(adapter);

                monName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        monName.showDropDown();
                    }
                });

                final AlertDialog.Builder addMonDialog = new AlertDialog.Builder(getContext());
                addMonDialog.setTitle("Add Monster to Encounter");
                addMonDialog.setView(addMonster);
                addMonDialog.setNegativeButton("Cancel", null);
                addMonDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = monName.getText().toString();
                        if (!monList[0].contains(name)) {
                            System.out.println("not contains");
                            return;
                        }

                        int[] xp = new int[1];

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    xp[0] = proxy.getMonster(name).getXP();
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

                        encounter[0].addMonsterData(name, xp[0]);
                        monsters();
                        difficulty();
                    }
                });

                AlertDialog alert = addMonDialog.create();
                alert.show();
            }
        });
    }
}
