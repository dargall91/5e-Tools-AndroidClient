package com.DnD5eTools.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DnD5eTools.R;
import com.DnD5eTools.monster.Ability;
import com.DnD5eTools.monster.Action;
import com.DnD5eTools.client.DNDClientProxy;
import com.DnD5eTools.monster.LegendaryAction;
import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tab for creating and editing Monsters.
 */
public class MonsterBuilder extends Fragment {
    private DNDClientProxy proxy;
    private Monster[] monster;
    private ArrayList<String> monList;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private static MonsterBuilder builder;
    private ArrayList<Ability> abilityList;
    private ArrayList<Action> actionList;
    private ArrayList<LegendaryAction> legendaryList;
    private LinearLayout abilities;
    private LinearLayout actions;
    private LinearLayout legActions;
    private View view;
    private String[] STAT;
    private final int DELAY = 250;
    private final String ADD_MONSTER = "   Add Monster";
    private CheckBox strSave, dexSave, conSave, intSave, wisSave, chaSave, athProf, acroProf, sleightProf, stealthProf, arcProf, histProf, invProf, natProf,
        relProf, aniProf, insProf, medProf, perProf, surProf, decProf, intimProf, perfProf, persuProf;
    private String ATH;
    private String ACRO;
    private String SLEIGHT;
    private String STEALTH;
    private String ARCANA;
    private String HISTORY;
    private String INV;
    private String NATURE;
    private String RELIGION;
    private String ANIMAL;
    private String INSIGHT;
    private String MEDICINE;
    private String PERCEPTION;
    private String SURVIVAL;
    private String DEC;
    private String INTIM;
    private String PERF;
    private String PERSUASION;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //initialize strings
        STAT = new String[] {getString(R.string.str), getString(R.string.dex), getString(R.string.con), getString(R.string.inte), getString(R.string.wis), getString(R.string.cha)};
        ATH = getString(R.string.athletics);
        ACRO = getString(R.string.acrobatics);
        SLEIGHT = getString(R.string.sleight_of_hand);
        STEALTH = getString(R.string.stealth);
        ARCANA = getString(R.string.arcana);
        HISTORY = getString(R.string.history);
        INV = getString(R.string.investigation);
        NATURE = getString(R.string.nature);
        RELIGION = getString(R.string.religion);
        ANIMAL = getString(R.string.animal_handling);
        INSIGHT = getString(R.string.insight);
        MEDICINE = getString(R.string.medicine);
        PERCEPTION = getString(R.string.perception);
        SURVIVAL = getString(R.string.survival);
        DEC = getString(R.string.deception);
        INTIM = getString(R.string.intimidation);
        PERF = getString(R.string.performance);
        PERSUASION = getString(R.string.persuasion);
        monster = new Monster[1];
        this.inflater = inflater;
        this.container = container;
        this.savedInstanceState = savedInstanceState;
        builder = this;

        if (Util.isConnectedToServer()) {
            proxy = MainActivity.getProxy();
            view = inflater.inflate(R.layout.monster_builder_layout, container, false);
            view.setId(View.generateViewId());
            view.setTag("MonsterBuilder");

            abilities = view.findViewById(R.id.monster_abilities_layout);
            actions = view.findViewById(R.id.monster_actions_layout);
            legActions = view.findViewById(R.id.monster_legendary_actions_layout);
            monsterListView(null);
            builderView();
        }

        return view;
    }

    public static MonsterBuilder getMonBuilder() {
        return builder;
    }

    public void refresh() {
        MonsterBuilder builder = this;
        getFragmentManager().findFragmentById(builder.getId());

        getFragmentManager().beginTransaction()
                .detach(builder)
                .attach(builder)
                .commit();

        onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Sets up the ListView that contains a list of all the monsters on the server
     *
     * @param name the name of the monster to load, null should be used to load the first monster in the monster lists
     */
    private void monsterListView(String name) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    monList = proxy.getMonsterList();
                    monList.add(ADD_MONSTER);
                    Collections.sort(monList);

                    //TODO: What happens if the list is empty? Should probably set monster[0] to null, then in onCreateView only call builderView() is monster != null
                    if (name == null)
                        monster[0] = proxy.getMonster(monList.get(1));

                    else
                        monster[0] = proxy.getMonster(name);

                } catch (Exception e) {
                    Log.i("MonList", e.getMessage());
                }
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ListView monListView = view.findViewById(R.id.monster_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.simple_list_view, monList);
        monListView.setAdapter(adapter);
        monListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //check if add monster was selected, should be first monster in list
                if (monList.get(position).equals(ADD_MONSTER)) {
                    View addView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
                    TextView exists = addView.findViewById(R.id.monster_exists);
                    TextView text = addView.findViewById(R.id.name_textview);
                    text.setText("Enter the new monster's name:");
                    EditText newName = addView.findViewById(R.id.name_entry);

                    final AlertDialog.Builder renameMonsterDialog = new AlertDialog.Builder(getContext());
                    renameMonsterDialog.setView(addView);
                    renameMonsterDialog.setTitle("Add Monster");
                    renameMonsterDialog.setPositiveButton("OK", null);
                    renameMonsterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("CANCEL", "cancel");
                        }
                    });

                    AlertDialog add = renameMonsterDialog.create();
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
                                                success[0] = proxy.addMonster(name);
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
                                        monsterListView(name);
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

                //if the selected monster is the current one, do nothing and exit
                //list size must be greater than 1 or it tries to update "Add Monster"
                if (monList.size() > 1 && monster[0].getName().equals(monList.get(position)))
                    return;

                //update monster on server, get new monster
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.updateMonster(monster[0]);
                            monster[0] = proxy.getMonster(monList.get(position));
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

    /**
     * Initializes the monster builder view using
     * the first monster in the list. If there are
     * no monsters in the list, it will be blank
     */
    private void builderView() {
        //the list is "empty" if the only item in the
        //list is the "Add Monster" item
        if (monList.size() <= 1) {
            return;
        }

        basicMonsterInfo();
        monsterStats();
        monsterSensesLanguagesCR();
        monsterAbilities();
        monsterActions();
        monsterLegendaryActions();
    }

    /**
     * Basic monster info is the monster's name, display name, size, type, alignment, ac, hp, and speed
     */
    private void basicMonsterInfo() {
        View basicInfo = view.findViewById(R.id.basic_monster_info);
        TextView name = basicInfo.findViewById(R.id.name);
        name.setText(monster[0].getName());

        EditText displayName = basicInfo.findViewById(R.id.display_name);
        displayName.setText(monster[0].getDisplayName());
        displayName.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setDisplayName(displayName.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText size = basicInfo.findViewById(R.id.size);
        size.setText(monster[0].getSize());
        size.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setSize(size.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText type = basicInfo.findViewById(R.id.type);
        type.setText(monster[0].getType());
        type.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setType(type.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText alignment = basicInfo.findViewById(R.id.alignment);
        alignment.setText(monster[0].getAlignment());
        alignment.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setAlignment(alignment.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText ac = basicInfo.findViewById(R.id.ac);
        ac.setText(monster[0].getAC());
        ac.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setAC(ac.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText hitPoints = basicInfo.findViewById(R.id.hit_points);
        hitPoints.setText(monster[0].getHP());
        hitPoints.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setHP(hitPoints.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText speed = basicInfo.findViewById(R.id.speed);
        speed.setText(monster[0].getSpeed());
        speed.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setSpeed(speed.getText().toString());
                    }
                }, DELAY);
            }
        });

        Button save = basicInfo.findViewById(R.id.save_monster);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.updateMonster(monster[0]);
                            proxy.saveMonster(monster[0].getName());
                        } catch (Exception e) {
                            Log.i("save", e.getMessage());
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

        Button delete = basicInfo.findViewById(R.id.delete_monster);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete Monster")
                        .setMessage("Delete " + monster[0].getName() + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final boolean[] deleted = new boolean[1];
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            deleted[0] = proxy.deleteMonster(monster[0].getName());
                                        } catch (Exception e) {
                                            Log.i("delete", e.getMessage());
                                        }
                                    }
                                });

                                thread.start();

                                try {
                                    thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (deleted[0]) {
                                    monsterListView(null);
                                    builderView();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        Button rename = basicInfo.findViewById(R.id.rename_monster);
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View renameView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
                TextView exists = renameView.findViewById(R.id.monster_exists);
                TextView text = renameView.findViewById(R.id.name_textview);
                text.setText("Enter a new name for " + monster[0].getName());
                EditText newName = renameView.findViewById(R.id.name_entry);

                final AlertDialog.Builder renameMonsterDialog = new AlertDialog.Builder(getContext());
                renameMonsterDialog.setView(renameView);
                renameMonsterDialog.setTitle("Rename " + monster[0].getName());
                renameMonsterDialog.setPositiveButton("OK", null);
                renameMonsterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("CANCEL", "cancel");
                    }
                });

                AlertDialog rename = renameMonsterDialog.create();
                rename.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button ok = rename.getButton(AlertDialog.BUTTON_POSITIVE);
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean[] success = new boolean[1];
                                final String oldName = monster[0].getName();
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            monster[0].setName(newName.getText().toString());
                                            success[0] = proxy.renameMonster(oldName, monster[0]);
                                        } catch (Exception e) {
                                            Log.i("rename", e.getMessage());
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
                                    monsterListView(monster[0].getName());
                                    builderView();
                                }

                                else {
                                    monster[0].setName(oldName);
                                    exists.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });

                rename.show();
            }
        });

        Button restore = basicInfo.findViewById(R.id.restore_monster);
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Restore Monster")
                        .setMessage("Restore " + monster[0].getName() + " to the last saved state from the server?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final boolean[] restored = new boolean[1];
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            restored[0] = proxy.restoreMonster(monster[0].getName());
                                        } catch (Exception e) {
                                            Log.i("restore", e.getMessage());
                                        }
                                    }
                                });

                                thread.start();

                                try {
                                    thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (restored[0]) {
                                    monsterListView(monster[0].getName());
                                    builderView();
                                }
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        Button copy = basicInfo.findViewById(R.id.copy_monster);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View renameView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
                TextView exists = renameView.findViewById(R.id.monster_exists);
                TextView text = renameView.findViewById(R.id.name_textview);
                text.setText("Enter a new name for the copy of " + monster[0].getName());
                EditText newName = renameView.findViewById(R.id.name_entry);

                final AlertDialog.Builder renameMonsterDialog = new AlertDialog.Builder(getContext());
                renameMonsterDialog.setView(renameView);
                renameMonsterDialog.setTitle("Copy " + monster[0].getName());
                renameMonsterDialog.setPositiveButton("OK", null);
                renameMonsterDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("CANCEL", "cancel");
                    }
                });

                AlertDialog copy = renameMonsterDialog.create();
                copy.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button ok = copy.getButton(AlertDialog.BUTTON_POSITIVE);
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean[] success = new boolean[1];
                                final Monster[] newMon = new Monster[1];
                                String name = newName.getText().toString();

                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            success[0] = proxy.addMonster(name);

                                            //if successful get the new monster
                                            if (success[0]) {
                                                monster[0].setName(name);
                                                monster[0].setDisplayName(name);
                                                proxy.updateMonster(monster[0]);
                                            }
                                        } catch (Exception e) {
                                            Log.i("rename", e.getMessage());
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
                                    copy.dismiss();
                                    monsterListView(monster[0].getName());
                                    builderView();
                                } else {
                                    exists.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });

                copy.show();
            }
        });
    }

    private void monsterStats() {
        GridLayout stats = view.findViewById(R.id.monster_stats);
        monsterSTR(stats);
        monsterDEX(stats);
        monsterCON(stats);
        monsterINT(stats);
        monsterWIS(stats);
        monsterCHA(stats);
    }

    private void monsterSTR(GridLayout stats) {
        Spinner str = stats.findViewById(R.id.strength);
        TextView strMod = stats.findViewById(R.id.str_mod);

        strSave = stats.findViewById(R.id.str_save);
        strSave.setChecked(monster[0].getAbilityProficiency(STAT[0]));
        strSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrow(strSave, STAT[0]);
            }
        });

        CheckBox athExp = stats.findViewById(R.id.athletics_exp);
        athExp.setChecked(monster[0].getSkillExpertise(ATH));
        athProf = stats.findViewById(R.id.athletics_prof);
        athProf.setChecked(monster[0].getSkillProficient(ATH));

        athExp.setOnCheckedChangeListener(new CustomChangeListener(athProf, athExp, STAT[0], ATH, true));
        athProf.setOnCheckedChangeListener(new CustomChangeListener(athProf, athExp, STAT[0], ATH, false));

        str.setSelection(monster[0].getAbilityScore(STAT[0]));
        str.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setAbilityScore(STAT[0], Integer.parseInt(str.getSelectedItem().toString()));
                updateAbilityModifier(strMod, STAT[0]);
                updateSavingThrow(strSave, STAT[0]);
                updateProfText(athProf, STAT[0], ATH);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterDEX(GridLayout stats) {
        Spinner dex = stats.findViewById(R.id.dexterity);
        TextView dexMod = stats.findViewById(R.id.dex_mod);

        dexSave = stats.findViewById(R.id.dex_save);
        dexSave.setChecked(monster[0].getAbilityProficiency(STAT[1]));
        dexSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrow(dexSave, STAT[1]);
            }
        });

        CheckBox acroExp = stats.findViewById(R.id.acrobatics_exp);
        acroExp.setChecked(monster[0].getSkillExpertise(ACRO));
        acroProf = stats.findViewById(R.id.acrobatics_prof);
        acroProf.setChecked(monster[0].getSkillProficient(ACRO));

        acroExp.setOnCheckedChangeListener(new CustomChangeListener(acroProf, acroExp, STAT[1], ACRO, true));
        acroProf.setOnCheckedChangeListener(new CustomChangeListener(acroProf, acroExp, STAT[1], ACRO, false));

        CheckBox sleightExp = stats.findViewById(R.id.sleight_exp);
        sleightExp.setChecked(monster[0].getSkillExpertise(SLEIGHT));
        sleightProf = stats.findViewById(R.id.sleight_prof);
        sleightProf.setChecked(monster[0].getSkillProficient(SLEIGHT));

        sleightExp.setOnCheckedChangeListener(new CustomChangeListener(sleightProf, sleightExp, STAT[1], SLEIGHT, true));
        sleightProf.setOnCheckedChangeListener(new CustomChangeListener(sleightProf, sleightExp, STAT[1], SLEIGHT, false));

        CheckBox stealthExp = stats.findViewById(R.id.stealth_exp);
        stealthExp.setChecked(monster[0].getSkillExpertise(STEALTH));
        stealthProf = stats.findViewById(R.id.stealth_prof);
        stealthProf.setChecked(monster[0].getSkillProficient(STEALTH));

        stealthExp.setOnCheckedChangeListener(new CustomChangeListener(stealthProf, stealthExp, STAT[1], STEALTH, true));
        stealthProf.setOnCheckedChangeListener(new CustomChangeListener(stealthProf, stealthExp, STAT[1], STEALTH, false));

        dex.setSelection(monster[0].getAbilityScore(STAT[1]));
        dex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setAbilityScore(STAT[1], Integer.parseInt(dex.getSelectedItem().toString()));
                updateAbilityModifier(dexMod, STAT[1]);
                updateSavingThrow(dexSave, STAT[1]);
                updateProfText(acroProf, STAT[1], ACRO);
                updateProfText(sleightProf, STAT[1], SLEIGHT);
                updateProfText(stealthProf, STAT[1], STEALTH);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterCON(GridLayout stats) {
        Spinner con = stats.findViewById(R.id.constitution);
        TextView conMod = stats.findViewById(R.id.con_mod);

        conSave = stats.findViewById(R.id.con_save);
        conSave.setChecked(monster[0].getAbilityProficiency(STAT[2]));
        conSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrow(conSave, STAT[2]);
            }
        });

        con.setSelection(monster[0].getAbilityScore(STAT[2]));
        con.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setAbilityScore(STAT[2], Integer.parseInt(con.getSelectedItem().toString()));
                updateAbilityModifier(conMod, STAT[2]);
                updateSavingThrow(conSave, STAT[2]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterINT(GridLayout stats) {
        Spinner inte = stats.findViewById(R.id.intelligence);
        TextView intMod = stats.findViewById(R.id.int_mod);

        intSave = stats.findViewById(R.id.int_save);
        intSave.setChecked(monster[0].getAbilityProficiency(STAT[3]));
        intSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrow(intSave, STAT[3]);
            }
        });

        CheckBox arcExp = stats.findViewById(R.id.arcana_exp);
        arcExp.setChecked(monster[0].getSkillExpertise(ARCANA));
        arcProf = stats.findViewById(R.id.arcana_prof);
        arcProf.setChecked(monster[0].getSkillProficient(ARCANA));

        arcExp.setOnCheckedChangeListener(new CustomChangeListener(arcProf, arcExp, STAT[3], ARCANA, true));
        arcProf.setOnCheckedChangeListener(new CustomChangeListener(arcProf, arcExp, STAT[3], ARCANA, false));

        CheckBox histExp = stats.findViewById(R.id.history_exp);
        histExp.setChecked(monster[0].getSkillExpertise(HISTORY));
        histProf = stats.findViewById(R.id.history_prof);
        histProf.setChecked(monster[0].getSkillProficient(HISTORY));

        histExp.setOnCheckedChangeListener(new CustomChangeListener(histProf, histExp, STAT[3], HISTORY, true));
        histProf.setOnCheckedChangeListener(new CustomChangeListener(histProf, histExp, STAT[3], HISTORY, false));

        CheckBox invExp = stats.findViewById(R.id.investigation_exp);
        invExp.setChecked(monster[0].getSkillExpertise(INV));
        invProf = stats.findViewById(R.id.investigation_prof);
        invProf.setChecked(monster[0].getSkillProficient(INV));

        invExp.setOnCheckedChangeListener(new CustomChangeListener(invProf, invExp, STAT[3], INV, true));
        invProf.setOnCheckedChangeListener(new CustomChangeListener(invProf, invExp, STAT[3], INV, false));

        CheckBox natExp = stats.findViewById(R.id.nature_exp);
        natExp.setChecked(monster[0].getSkillExpertise(NATURE));
        natProf = stats.findViewById(R.id.nature_prof);
        natProf.setChecked(monster[0].getSkillProficient(NATURE));

        natExp.setOnCheckedChangeListener(new CustomChangeListener(natProf, natExp, STAT[3], NATURE, true));
        natProf.setOnCheckedChangeListener(new CustomChangeListener(natProf, natExp, STAT[3], NATURE, false));

        CheckBox relExp = stats.findViewById(R.id.religion_exp);
        relExp.setChecked(monster[0].getSkillExpertise(RELIGION));
        relProf = stats.findViewById(R.id.religion_prof);
        relProf.setChecked(monster[0].getSkillProficient(RELIGION));

        relExp.setOnCheckedChangeListener(new CustomChangeListener(relProf, relExp, STAT[3], RELIGION, true));
        relProf.setOnCheckedChangeListener(new CustomChangeListener(relProf, relExp, STAT[3], RELIGION, false));

        inte.setSelection(monster[0].getAbilityScore(STAT[3]));
        inte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setAbilityScore(STAT[3], Integer.parseInt(inte.getSelectedItem().toString()));
                updateAbilityModifier(intMod, STAT[3]);
                updateSavingThrow(intSave, STAT[3]);
                updateProfText(arcProf, STAT[3], ARCANA);
                updateProfText(histProf, STAT[3], HISTORY);
                updateProfText(invProf, STAT[3], INV);
                updateProfText(natProf, STAT[3], NATURE);
                updateProfText(relProf, STAT[3], RELIGION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterWIS(GridLayout stats) {
        Spinner wis = stats.findViewById(R.id.wisdom);
        TextView wisMod = stats.findViewById(R.id.wis_mod);

        wisSave = stats.findViewById(R.id.wis_save);
        wisSave.setChecked(monster[0].getAbilityProficiency(STAT[4]));
        wisSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrow(wisSave, STAT[4]);
            }
        });

        CheckBox aniExp = stats.findViewById(R.id.animal_exp);
        aniExp.setChecked(monster[0].getSkillExpertise(ANIMAL));
        aniProf = stats.findViewById(R.id.animal_prof);
        aniProf.setChecked(monster[0].getSkillProficient(ANIMAL));

        aniExp.setOnCheckedChangeListener(new CustomChangeListener(aniProf, aniExp, STAT[4], ANIMAL, true));
        aniProf.setOnCheckedChangeListener(new CustomChangeListener(aniProf, aniExp, STAT[4], ANIMAL, false));

        CheckBox insExp = stats.findViewById(R.id.insight_exp);
        insExp.setChecked(monster[0].getSkillExpertise(INSIGHT));
        insProf = stats.findViewById(R.id.insight_prof);
        insProf.setChecked(monster[0].getSkillProficient(INSIGHT));

        insExp.setOnCheckedChangeListener(new CustomChangeListener(insProf, insExp, STAT[4], INSIGHT, true));
        insProf.setOnCheckedChangeListener(new CustomChangeListener(insProf, insExp, STAT[4], INSIGHT, false));

        CheckBox medExp = stats.findViewById(R.id.medicine_exp);
        medExp.setChecked(monster[0].getSkillExpertise(MEDICINE));
        medProf = stats.findViewById(R.id.medicine_prof);
        medProf.setChecked(monster[0].getSkillProficient(MEDICINE));

        medExp.setOnCheckedChangeListener(new CustomChangeListener(medProf, medExp, STAT[4], MEDICINE, true));
        medProf.setOnCheckedChangeListener(new CustomChangeListener(medProf, medExp, STAT[4], MEDICINE, false));

        CheckBox perExp = stats.findViewById(R.id.perception_exp);
        perExp.setChecked(monster[0].getSkillExpertise(PERCEPTION));
        perProf = stats.findViewById(R.id.perception_prof);
        perProf.setChecked(monster[0].getSkillProficient(PERCEPTION));

        perExp.setOnCheckedChangeListener(new CustomChangeListener(perProf, perExp, STAT[4], PERCEPTION, true));
        perProf.setOnCheckedChangeListener(new CustomChangeListener(perProf, perExp, STAT[4], PERCEPTION, false));

        CheckBox surExp = stats.findViewById(R.id.survival_exp);
        surExp.setChecked(monster[0].getSkillExpertise(SURVIVAL));
        surProf = stats.findViewById(R.id.survival_prof);
        surProf.setChecked(monster[0].getSkillProficient(SURVIVAL));

        surExp.setOnCheckedChangeListener(new CustomChangeListener(surProf, surExp, STAT[4], SURVIVAL, true));
        surProf.setOnCheckedChangeListener(new CustomChangeListener(surProf, surExp, STAT[4], SURVIVAL, false));

        wis.setSelection(monster[0].getAbilityScore(STAT[4]));
        wis.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setAbilityScore(STAT[4], Integer.parseInt(wis.getSelectedItem().toString()));
                updateAbilityModifier(wisMod, STAT[4]);
                updateSavingThrow(wisSave, STAT[4]);
                updateProfText(aniProf, STAT[4], ANIMAL);
                updateProfText(insProf, STAT[4], INSIGHT);
                updateProfText(medProf, STAT[4], MEDICINE);
                updateProfText(perProf, STAT[4], PERCEPTION);
                updateProfText(surProf, STAT[4], SURVIVAL);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterCHA(GridLayout stats) {
        Spinner cha = stats.findViewById(R.id.charisma);
        TextView chaMod = stats.findViewById(R.id.cha_mod);

        chaSave = stats.findViewById(R.id.cha_save);
        chaSave.setChecked(monster[0].getAbilityProficiency(STAT[5]));
        chaSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrow(chaSave, STAT[5]);
            }
        });

        CheckBox decExp = stats.findViewById(R.id.deception_exp);
        decExp.setChecked(monster[0].getSkillExpertise(DEC));
        decProf = stats.findViewById(R.id.deception_prof);
        decProf.setChecked(monster[0].getSkillProficient(DEC));

        decExp.setOnCheckedChangeListener(new CustomChangeListener(decProf, decExp, STAT[5], DEC, true));
        decProf.setOnCheckedChangeListener(new CustomChangeListener(decProf, decExp, STAT[5], DEC, false));

        CheckBox intimExp = stats.findViewById(R.id.intimidation_exp);
        intimExp.setChecked(monster[0].getSkillExpertise(INTIM));
        intimProf = stats.findViewById(R.id.intimidation_prof);
        intimProf.setChecked(monster[0].getSkillProficient(INTIM));

        intimExp.setOnCheckedChangeListener(new CustomChangeListener(intimProf, intimExp, STAT[5], INTIM, true));
        intimProf.setOnCheckedChangeListener(new CustomChangeListener(intimProf, intimExp, STAT[5], INTIM, false));

        CheckBox perfExp = stats.findViewById(R.id.performance_exp);
        perfExp.setChecked(monster[0].getSkillExpertise(PERF));
        perfProf = stats.findViewById(R.id.performance_prof);
        perfProf.setChecked(monster[0].getSkillProficient(PERF));

        perfExp.setOnCheckedChangeListener(new CustomChangeListener(perfProf, perfExp, STAT[5], PERF, true));
        perfProf.setOnCheckedChangeListener(new CustomChangeListener(perfProf, perfExp, STAT[5], PERF, false));

        CheckBox persuExp = stats.findViewById(R.id.persuasion_exp);
        persuExp.setChecked(monster[0].getSkillExpertise(PERSUASION));
        persuProf = stats.findViewById(R.id.persuasion_prof);
        persuProf.setChecked(monster[0].getSkillProficient(PERSUASION));

        persuExp.setOnCheckedChangeListener(new CustomChangeListener(persuProf, persuExp, STAT[5], PERSUASION, true));
        persuProf.setOnCheckedChangeListener(new CustomChangeListener(persuProf, persuExp, STAT[5], PERSUASION, false));

        cha.setSelection(monster[0].getAbilityScore(STAT[5]));
        cha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setAbilityScore(STAT[5], Integer.parseInt(cha.getSelectedItem().toString()));
                updateAbilityModifier(chaMod, STAT[5]);
                updateSavingThrow(chaSave, STAT[5]);
                updateProfText(decProf, STAT[5], DEC);
                updateProfText(intimProf, STAT[5], INTIM);
                updateProfText(perfProf, STAT[5], PERF);
                updateProfText(persuProf, STAT[5], PERSUASION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterSensesLanguagesCR() {
        View senseLanguageCR = view.findViewById(R.id.monster_senses_languages_cr);

        EditText senses = senseLanguageCR.findViewById(R.id.senses);
        senses.setText(monster[0].getSenses());
        senses.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setSenses(senses.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText languages = senseLanguageCR.findViewById(R.id.languages);
        languages.setText(monster[0].getLanguages());
        languages.addTextChangedListener(new TextWatcher() {
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
                        monster[0].setLanguages(languages.getText().toString());
                    }
                }, DELAY);
            }
        });

        TextView xp = senseLanguageCR.findViewById(R.id.xp);
        Spinner cr = senseLanguageCR.findViewById(R.id.challenge);
        List<String> ratings = Arrays.asList((getResources().getStringArray(R.array.challenge_ratings)));
        int value = ratings.indexOf(monster[0].getChallenge());

        cr.setSelection(value, false);
        cr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster[0].setChallenge(cr.getSelectedItem().toString());
                String xpString = "(" + monster[0].getXP() + " XP)";
                xp.setText(xpString);

                //update all saves and skills with new proficiency
                updateProficiencyCalcs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    public void monsterAbilities() {
        if (abilities.getChildCount() > 1)
            abilities.removeViewsInLayout(1, abilities.getChildCount() - 1);

        abilities.requestLayout();

        abilityList = monster[0].getAbilities();

        for (int i = 0; i < abilityList.size(); i++) {
            final int index = i;
            View abilityView = inflater.inflate(R.layout.monster_action_ability, abilities);

            EditText name = abilityView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(abilityList.get(index).getName());
            name.addTextChangedListener(new TextWatcher() {
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
                            abilityList.get(index).setName(name.getText().toString());
                            monster[0].renameAbility(abilityList.get(index).getName(), index);
                        }
                    }, DELAY);
                }
            });

            EditText desc = abilityView.findViewById(R.id.description);
            desc.setId(index);
            desc.setTag(index);
            desc.setText(abilityList.get(index).getDescription());
            desc.addTextChangedListener(new TextWatcher() {
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
                            abilityList.get(index).setDescription(desc.getText().toString());
                            monster[0].setAbilityDescription(abilityList.get(index).getDescription(), index);
                        }
                    }, DELAY);
                }
            });

            Button delete = abilityView.findViewById(R.id.delete_ability_action);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    monster[0].deleteAbility(index);
                    monsterAbilities();
                }
            });
        }

        Button add = view.findViewById(R.id.add_ability);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monster[0].addAbility(new Ability());
                monsterAbilities();
            }
        });
    }

    public void monsterActions() {
        if (actions.getChildCount() > 1)
            actions.removeViewsInLayout(1, actions.getChildCount() - 1);

        actions.requestLayout();

        actionList = monster[0].getActions();

        for (int i = 0; i < actionList.size(); i++) {
            final int index = i;
            View actionView = inflater.inflate(R.layout.monster_action_ability, actions);

            EditText name = actionView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(actionList.get(index).getName());
            name.addTextChangedListener(new TextWatcher() {
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
                            actionList.get(index).setName(name.getText().toString());
                            monster[0].renameAction(actionList.get(index).getName(), index);
                        }
                    }, DELAY);
                }
            });

            EditText desc = actionView.findViewById(R.id.description);
            desc.setId(index);
            desc.setTag(index);
            desc.setText(actionList.get(index).getDescription());
            desc.addTextChangedListener(new TextWatcher() {
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
                            actionList.get(index).setDescription(desc.getText().toString());
                            monster[0].setActionDescription(actionList.get(index).getDescription(), index);
                        }
                    }, DELAY);
                }
            });

            Button delete = actionView.findViewById(R.id.delete_ability_action);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    monster[0].deleteAction(index);
                    monsterActions();
                }
            });
        }

        Button add = view.findViewById(R.id.add_action);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monster[0].addAction(new Action());
                monsterActions();
            }
        });
    }

    public void monsterLegendaryActions() {
        LinearLayout countLayout = legActions.findViewById(R.id.legendary_count_layout);

        if (legActions.getChildCount() > 2)
            legActions.removeViewsInLayout(2, legActions.getChildCount() - 2);

        legActions.requestLayout();

        legendaryList = monster[0].getLegendaryActions();

        if (legendaryList.size() > 0) {
            countLayout.setVisibility(View.VISIBLE);

            Spinner actionCount = countLayout.findViewById(R.id.legendary_count_spinner);
            actionCount.setSelection(monster[0].getLegendaryActionCount());
            actionCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    monster[0].setLegendaryActionCount(actionCount.getSelectedItemPosition());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        else
            countLayout.setVisibility(View.GONE);

        for (int i = 0; i < legendaryList.size(); i++) {
            final int index = i;
            View legView = inflater.inflate(R.layout.monster_legendary_action, legActions);

            EditText name = legView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(legendaryList.get(index).getName());
            name.addTextChangedListener(new TextWatcher() {
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
                            legendaryList.get(index).setName(name.getText().toString());
                            monster[0].renameLegendaryAction(legendaryList.get(index).getName(), index);
                        }
                    }, DELAY);
                }
            });

            EditText desc = legView.findViewById(R.id.description);
            desc.setId(index);
            desc.setTag(index);
            desc.setText(legendaryList.get(index).getDescription());
            desc.addTextChangedListener(new TextWatcher() {
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
                            legendaryList.get(index).setDescription(desc.getText().toString());
                            monster[0].setLegendaryDescription(legendaryList.get(index).getDescription(), index);
                        }
                    }, DELAY);
                }
            });

            Spinner cost = legView.findViewById(R.id.cost);
            cost.setId(index);
            cost.setTag(index);
            cost.setSelection(legendaryList.get(index).getCost());
            cost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    legendaryList.get(index).setCost(cost.getSelectedItemPosition());
                    monster[0].setLegendaryActionCost(legendaryList.get(index).getCost(), index);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            Button delete = legView.findViewById(R.id.delete_ability_action);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    monster[0].deleteLegendaryAction(index);
                    monsterLegendaryActions();
                }
            });
        }


        Button add = view.findViewById(R.id.add_legendary_action);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monster[0].addLegendaryAction(new LegendaryAction());
                monsterLegendaryActions();
            }
        });
    }

    //TODO: find more elegant solution, is redrawing monster_stats better? if so, why didn't it work before?
    /**
     * A monster's CR determines it's proficiency bonus. Therefore, when a monster's CR is changed, it's proficiencies must all be recalculated
     */
    private void updateProficiencyCalcs() {
        updateSavingThrow(strSave, STAT[0]);
        updateProfText(athProf, STAT[0], ATH);

        updateSavingThrow(dexSave, STAT[1]);
        updateProfText(acroProf, STAT[1], ACRO);
        updateProfText(sleightProf, STAT[1], SLEIGHT);
        updateProfText(stealthProf, STAT[1], STEALTH);

        updateSavingThrow(conSave, STAT[2]);

        updateSavingThrow(intSave, STAT[3]);
        updateProfText(arcProf, STAT[3], ARCANA);
        updateProfText(histProf, STAT[3], HISTORY);
        updateProfText(invProf, STAT[3], INV);
        updateProfText(natProf, STAT[3], NATURE);
        updateProfText(relProf, STAT[3], RELIGION);

        updateSavingThrow(wisSave, STAT[4]);
        updateProfText(aniProf, STAT[4], ANIMAL);
        updateProfText(insProf, STAT[4], INSIGHT);
        updateProfText(medProf, STAT[4], MEDICINE);
        updateProfText(perProf, STAT[4], PERCEPTION);
        updateProfText(surProf, STAT[4], SURVIVAL);

        updateSavingThrow(chaSave, STAT[5]);
        updateProfText(decProf, STAT[5], DEC);
        updateProfText(intimProf, STAT[5], INTIM);
        updateProfText(perfProf, STAT[5], PERF);
        updateProfText(persuProf, STAT[5], PERSUASION);
    }

    /**
     * Updates the saving throws text
     *
     * @param save the CheckBox for the saving throw
     * @param stat the stat string
     */
    private void updateSavingThrow(CheckBox save, String stat) {
        monster[0].setAbilityProficiency(stat, save.isChecked());
        save.setText("Saving Throws: " + monster[0].getSignedSavingThrow(stat));
    }

    /**
     * Updates check boxes and texts for skill proficiency/expertise
     * Use when the proficiency box is clicked
     *
     * @param prof proficiency checkbox
     * @param exp expertise checkbox
     * @param stat the stat string
     * @param skill the skill string
     */
    private void updateSkillProf(CheckBox prof, CheckBox exp, String stat, String skill) {
        monster[0].setSkillProficiency(skill, prof.isChecked());

        if (!prof.isChecked()) {
            exp.setChecked(prof.isChecked());
            monster[0].setSkillExpertise(skill, exp.isChecked());
        }

        updateProfText(prof, stat, skill);
    }

    /**
     * Updates check boxes and texts for skill proficiency/expertise
     * Use when the expertise box is clicked
     *
     * @param prof proficiency checkbox
     * @param exp expertise checkbox
     * @param stat the stat string
     * @param skill the skill string
     */
    private void updateSkillExp(CheckBox prof, CheckBox exp, String stat, String skill) {
        monster[0].setSkillExpertise(skill, exp.isChecked());

        if (exp.isChecked()) {
            prof.setChecked(exp.isChecked());
            monster[0].setSkillProficiency(skill, prof.isChecked());
        }

        updateProfText(prof, stat, skill);
    }

    /**
     * Updates the text on the proficiency text box
     *
     * @param prof the proficiency checkbox
     * @param stat the stat string
     * @param skill the skill string
     */
    private void updateProfText(CheckBox prof, String stat, String skill) {
        prof.setText(skill + ": " + monster[0].getSignedSkillModifier(stat, skill));
    }

    /**
     * Updates the ability score modifier
     *
     * @param textView The TextView for the ability score
     * @param stat The stat string
     */
    private void updateAbilityModifier(TextView textView, String stat) {
        textView.setText("(" + monster[0].getSignedAbilityModifier(stat) + ")");
    }

    /**
     * A custom CheckBox ChangeListener used for Expertise and Proficiency checkboxes
     */
    private class CustomChangeListener implements CompoundButton.OnCheckedChangeListener {
        CheckBox prof, exp;
        String stat, skill;
        boolean profExp; //true if expertise, false if proficiency

        /**
         * Constructor that sets up the values used by the listener
         * @param prof Proficiency CheckBox
         * @param exp ExpertiseCheckBox
         * @param stat Stat string
         * @param skill Skill string
         * @param profExp true if this listener is used for an expertise CheckBox, false if used for a proficiency CheckBox
         */
        CustomChangeListener(CheckBox prof, CheckBox exp, String stat, String skill, boolean profExp) {
            this.prof = prof;
            this.exp = exp;
            this.stat = stat;
            this.skill = skill;
            this.profExp = profExp;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (profExp)
                updateSkillExp(prof, exp, stat, skill);

            else
                updateSkillProf(prof, exp, stat, skill);
        }
    }
}