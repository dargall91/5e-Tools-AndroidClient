package com.DnD5eTools.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.DnD5eTools.interfaces.MonsterInterface;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.DnD5eTools.monster.Ability;
import com.DnD5eTools.monster.Action;
import com.DnD5eTools.monster.LegendaryAction;
import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab for creating and editing Monsters.
 */
public class MonsterBuilder extends Fragment {
    private Monster[] oldMonster;
    private com.DnD5eTools.entities.monster.Monster monster;
    private List<NameIdProjection> monsterList;
    private List<String> monsterNameList = new ArrayList<>();
    private LayoutInflater inflater;
    private ArrayList<Ability> abilityList;
    private ArrayList<Action> actionList;
    private ArrayList<LegendaryAction> legendaryList;
    private LinearLayout abilities;
    private LinearLayout actions;
    private LinearLayout legActions;
    private boolean expertise = false;
    private boolean proficiency = false;
    private View view;
    private String[] STAT;
    private final int DELAY = 250;
    private final String ADD_MONSTER = "Add Monster";
    private CheckBox strSave, dexSave, conSave, intSave, wisSave, chaSave, athleticsProficiency, acrobaticsProficiency, sleightofHandProficiency, stealthProfiency, arcProf, histProf, invProf, natProf,
        relProf, aniProf, insProf, medProf, perProf, surProf, decProf, intimProf, perfProf, persuProf;
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
        oldMonster = new Monster[1];
        this.inflater = inflater;

        view = inflater.inflate(R.layout.monster_builder_layout, container, false);
        view.setId(View.generateViewId());
        view.setTag("MonsterBuilder");

        abilities = view.findViewById(R.id.monster_abilities_layout);
        actions = view.findViewById(R.id.monster_actions_layout);
        legActions = view.findViewById(R.id.monster_legendary_actions_layout);

        return view;
    }

    public void initViews() {
        monsterListView();
        builderView();
    }

    /**
     * Sets up the ListView that contains a list of all the monsters on the server
     */
    private void monsterListView() {
        initMonsterList();

        if (monsterList.size() == 1) {
            monsterList.add(MonsterInterface.addMonster("New Monster"));
            initMonsterList();
        }

        int monsterId = monsterList.get(1).getId();
        monster = MonsterInterface.getMonster(monsterId);

        ListView monListView = view.findViewById(R.id.monster_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_view, monsterNameList);
        monListView.setAdapter(adapter);
        monListView.setOnItemClickListener((parent, view, position, id) -> {
            //check if add monster was selected, should be first monster in list
            if (position == 0) {
                View addView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
                TextView text = addView.findViewById(R.id.name_textview);
                text.setText("Enter the new monster's name:");
                EditText newName = addView.findViewById(R.id.name_entry);

                final AlertDialog.Builder renameMonsterDialog = new AlertDialog.Builder(getContext());
                renameMonsterDialog.setView(addView);
                renameMonsterDialog.setTitle("Add Monster");
                renameMonsterDialog.setPositiveButton("OK", null);
                renameMonsterDialog.setNegativeButton("Cancel", null);

                AlertDialog add = renameMonsterDialog.create();
                add.setOnShowListener(dialogInterface -> {
                    Button ok = add.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(v -> {
                        boolean[] success = new boolean[1];
                        final String name = newName.getText().toString();
                        NameIdProjection addedMonster = MonsterInterface.addMonster(name);

                        //reload list then display new encounter in builder
                        initMonsterList();
                        monster = MonsterInterface.getMonster(addedMonster.getId());
                        builderView();
                    });
                });

                add.show();
            }

            //if the selected monster is the current one, do nothing and exit
            if (monsterList.get(position).getId() == monster.getId()) {
                return;
            }

            //update monster on server, get new monster, display in builder
            //todo: is it necessary to update? should be updating on every action now
            MonsterInterface.updateMonster(monster);
            monster = MonsterInterface.getMonster(monsterList.get(position).getId());
            builderView();
        });
    }

    /**
     * Initializes the monster list including an entry at index 0 for adding new monsters
     */
    private void initMonsterList() {
        //todo: make add monster/add encounter actual buttons, eliminates the need for monster list scoped to this class
        NameIdProjection addMonster = new NameIdProjection();
        addMonster.setName(ADD_MONSTER);
        addMonster.setId(0);

        monsterList = new ArrayList<>();
        monsterList.add(addMonster);
        Util.setMonsterList();
        monsterList.addAll(Util.getMonsterList());
        monsterNameList = monsterList.stream()
                .map(NameIdProjection::getName)
                .collect(Collectors.toList());
    }

    /**
     * Initializes the monster builder view using
     * the first monster in the list. If there are
     * no monsters in the list, it will be blank
     */
    private void builderView() {
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
        //todo: add bonus initiative field
        View basicInfo = view.findViewById(R.id.basic_monster_info);
        TextView name = basicInfo.findViewById(R.id.name);
        name.setText(monster.getName());

        EditText displayName = basicInfo.findViewById(R.id.display_name);
        displayName.setText(monster.getDisplayName());
        displayName.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(() -> {
                    monster.setDisplayName(displayName.getText().toString());
                    MonsterInterface.updateMonster(monster);
                }, DELAY);
            }
        });

        EditText size = basicInfo.findViewById(R.id.size);
        size.setText(monster.getSize());
        size.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(() -> {
                    monster.setSize(size.getText().toString());
                    MonsterInterface.updateMonster(monster);
                }, DELAY);
            }
        });

        EditText type = basicInfo.findViewById(R.id.type);
        type.setText(monster.getType());
        type.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(() -> {
                    monster.setType(type.getText().toString());
                    MonsterInterface.updateMonster(monster);
                }, DELAY);
            }
        });

        EditText alignment = basicInfo.findViewById(R.id.alignment);
        alignment.setText(monster.getAlignment());
        alignment.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(() -> {
                    monster.setAlignment(alignment.getText().toString());
                    MonsterInterface.updateMonster(monster);
                }, DELAY);
            }
        });

        EditText ac = basicInfo.findViewById(R.id.ac);
        ac.setText(monster.getArmorClass());
        ac.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(() -> {
                    monster.setArmorClass(Integer.parseInt(ac.getText().toString()));
                    MonsterInterface.updateMonster(monster);
                }, DELAY);
            }
        });

        EditText hitPoints = basicInfo.findViewById(R.id.hit_points);
        hitPoints.setText(monster.getHitPoints());
        hitPoints.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        monster.setHitPoints(Integer.parseInt(hitPoints.getText().toString()));
                        MonsterInterface.updateMonster(monster);
                    }
                }, DELAY);
            }
        });

        EditText speed = basicInfo.findViewById(R.id.speed);
        speed.setText(monster.getSpeed());
        speed.addTextChangedListener(new TextWatcher() {
            Handler handler;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (handler != null) {
                    handler.removeCallbacks(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                handler = new Handler();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        monster.setSpeed(speed.getText().toString());
                        MonsterInterface.updateMonster(monster);
                    }
                }, DELAY);
            }
        });

        //todo: can probably get rid of this button because update endpoint saves
        Button save = basicInfo.findViewById(R.id.save_monster);
        save.setOnClickListener(view -> {
            MonsterInterface.updateMonster(monster);
        });

        Button archive = basicInfo.findViewById(R.id.archive_monster);
        archive.setOnClickListener(view -> new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Monster")
                .setMessage("Delete " + monster.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    MonsterInterface.archiveMonster(monster.getId());
                    monsterListView();
                    builderView();
                })
                .setNegativeButton("No", null)
                .show());

        Button rename = basicInfo.findViewById(R.id.rename_monster);
        rename.setOnClickListener(view -> {
            View renameView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
            TextView text = renameView.findViewById(R.id.name_textview);
            text.setText(MessageFormat.format("Enter a new name for {0}", monster.getName()));
            EditText newName = renameView.findViewById(R.id.name_entry);

            final AlertDialog.Builder renameMonsterDialog = new AlertDialog.Builder(getContext());
            renameMonsterDialog.setView(renameView);
            renameMonsterDialog.setTitle("Rename " + oldMonster[0].getName());
            renameMonsterDialog.setPositiveButton("OK", null);
            renameMonsterDialog.setNegativeButton("Cancel", null);

            AlertDialog renameDialog = renameMonsterDialog.create();
            renameDialog.setOnShowListener(dialogInterface -> {
                Button ok = renameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    monster.setName(newName.getText().toString());
                    name.setText(monster.getName());
                    MonsterInterface.updateMonster(monster);
                    renameDialog.dismiss();
                });
            });

            renameDialog.show();
        });

        Button copy = basicInfo.findViewById(R.id.copy_monster);
        copy.setOnClickListener(view -> {
            View renameView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
            TextView text = renameView.findViewById(R.id.name_textview);
            text.setText(MessageFormat.format("Enter a name for the copy of {0}", monster.getName()));
            EditText newName = renameView.findViewById(R.id.name_entry);

            final AlertDialog.Builder renameMonsterDialog = new AlertDialog.Builder(getContext());
            renameMonsterDialog.setView(renameView);
            renameMonsterDialog.setTitle("Copy " + monster.getName());
            renameMonsterDialog.setPositiveButton("OK", null);
            renameMonsterDialog.setNegativeButton("Cancel", null);

            AlertDialog copyDialog = renameMonsterDialog.create();
            copyDialog.setOnShowListener(dialogInterface -> {
                Button ok = copyDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    //get copy and display in builder
                    monster = MonsterInterface.copyMonster(monster.getId(), newName.getText().toString());
                    initMonsterList();
                    builderView();
                });
            });

            copyDialog.show();
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
        Spinner strengthSpinner = stats.findViewById(R.id.strength);
        TextView strengthModifier = stats.findViewById(R.id.str_mod);

        strSave = stats.findViewById(R.id.str_save);
        strSave.setChecked(monster.getStrength().isProficient());
        strSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            monster.getStrength().setProficient(isChecked);
            MonsterInterface.updateMonster(monster);
            updateSavingThrowText(strSave, monster.getStrength().getScoreModifier());
        });

        CheckBox athleticsExpertise = stats.findViewById(R.id.athletics_exp);
        athleticsExpertise.setChecked(monster.getStrength().getAthletics() == 2);
        athleticsProficiency = stats.findViewById(R.id.athletics_prof);
        athleticsProficiency.setChecked(monster.getStrength().getAthletics() > 0);

        athleticsExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (proficiency) {
                return;
            }

            expertise = true;

            if (isChecked) {
                athleticsProficiency.setChecked(true);
                monster.getStrength().setAthletics(2);
            } else {
                monster.getStrength().setAthletics(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(athleticsProficiency, monster.getStrength().getAthletics(),
                    monster.getStrength().getScoreModifier(), getString(R.string.athletics));
            expertise = false;
        });

        athleticsProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (expertise) {
                return;
            }

            proficiency = true;

            if (!isChecked) {
                athleticsExpertise.setChecked(false);
                monster.getStrength().setAthletics(0);
            } else {
                monster.getStrength().setAthletics(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(athleticsProficiency, monster.getStrength().getAthletics(),
                    monster.getStrength().getScoreModifier(), getString(R.string.athletics));
            proficiency = false;
        });

        strengthSpinner.setSelection(monster.getStrength().getScore());
        strengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster.getStrength().setScore(position);
                MonsterInterface.updateMonster(monster);
                updateSkillProficiencyText(athleticsProficiency, monster.getStrength().getAthletics(),
                        monster.getStrength().getScoreModifier(), getString(R.string.athletics));

                updateAbilityModifier(strengthModifier, monster.getStrength().getScoreModifier());
                updateSavingThrowText(strSave, monster.getStrength().getScoreModifier());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterDEX(GridLayout stats) {
        Spinner dexteritySpinner = stats.findViewById(R.id.dexterity);
        TextView dexterityModifier = stats.findViewById(R.id.dex_mod);

        dexSave = stats.findViewById(R.id.dex_save);
        dexSave.setChecked(monster.getDexterity().isProficient());
        dexSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            monster.getDexterity().setProficient(isChecked);
            MonsterInterface.updateMonster(monster);
            updateSavingThrowText(strSave, monster.getDexterity().getScoreModifier());
        });

        CheckBox acrobaticsExpertise = stats.findViewById(R.id.acrobatics_exp);
        acrobaticsExpertise.setChecked(monster.getDexterity().getAcrobatics() == 2);
        acrobaticsProficiency = stats.findViewById(R.id.acrobatics_prof);
        acrobaticsProficiency.setChecked(monster.getDexterity().getAcrobatics() > 0);

        acrobaticsExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (proficiency) {
                return;
            }

            expertise = true;

            if (isChecked) {
                acrobaticsProficiency.setChecked(true);
                monster.getDexterity().setAcrobatics(2);
            } else {
                monster.getDexterity().setAcrobatics(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getAcrobatics(),
                    monster.getDexterity().getScoreModifier(), getString(R.string.acrobatics));
            expertise = false;
        });

        acrobaticsProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (expertise) {
                return;
            }

            proficiency = true;

            if (!isChecked) {
                acrobaticsExpertise.setChecked(false);
                monster.getDexterity().setAcrobatics(0);
            } else {
                monster.getDexterity().setAcrobatics(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(athleticsProficiency, monster.getDexterity().getAcrobatics(),
                    monster.getDexterity().getScoreModifier(), getString(R.string.acrobatics));
            proficiency = false;
        });

        CheckBox sleightOfHandExpertise = stats.findViewById(R.id.sleight_exp);
        sleightOfHandExpertise.setChecked(monster.getDexterity().getSleightOfHand() == 2);
        sleightofHandProficiency = stats.findViewById(R.id.sleight_prof);
        sleightofHandProficiency.setChecked(monster.getDexterity().getSleightOfHand() > 0);

        sleightOfHandExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (proficiency) {
                return;
            }

            expertise = true;

            if (isChecked) {
                sleightofHandProficiency.setChecked(true);
                monster.getDexterity().setSleightOfHand(2);
            } else {
                monster.getDexterity().setSleightOfHand(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getSleightOfHand(),
                    monster.getDexterity().getScoreModifier(), getString(R.string.sleight_of_hand));
            expertise = false;
        });
        sleightofHandProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (expertise) {
                return;
            }

            proficiency = true;

            if (!isChecked) {
                sleightOfHandExpertise.setChecked(false);
                monster.getDexterity().setSleightOfHand(0);
            } else {
                monster.getDexterity().setSleightOfHand(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(athleticsProficiency, monster.getDexterity().getSleightOfHand(),
                    monster.getDexterity().getScoreModifier(), getString(R.string.sleight_of_hand));
            proficiency = false;
        });

        CheckBox stealthExpertise = stats.findViewById(R.id.stealth_exp);
        stealthExpertise.setChecked(monster.getDexterity().getStealth() == 2);
        stealthProfiency = stats.findViewById(R.id.stealth_prof);
        stealthProfiency.setChecked(monster.getDexterity().getStealth() > 0);

        stealthExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (proficiency) {
                return;
            }

            expertise = true;

            if (isChecked) {
                stealthProfiency.setChecked(true);
                monster.getDexterity().setStealth(2);
            } else {
                monster.getDexterity().setStealth(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getStealth(),
                    monster.getDexterity().getScoreModifier(), getString(R.string.stealth));
            expertise = false;
        });
        stealthProfiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (expertise) {
                return;
            }

            proficiency = true;

            if (!isChecked) {
                stealthExpertise.setChecked(false);
                monster.getDexterity().setStealth(0);
            } else {
                monster.getDexterity().setStealth(1);
            }

            MonsterInterface.updateMonster(monster);
            updateSkillProficiencyText(athleticsProficiency, monster.getDexterity().getStealth(),
                    monster.getDexterity().getScoreModifier(), getString(R.string.stealth));
            proficiency = false;
        });

        dexteritySpinner.setSelection(monster.getDexterity().getScore());
        dexteritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                monster.getDexterity().setScore(position);
                MonsterInterface.updateMonster(monster);
                updateSkillProficiencyText(athleticsProficiency, monster.getDexterity().getAcrobatics(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.athletics));
                updateSkillProficiencyText(athleticsProficiency, monster.getDexterity().getSleightOfHand(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.sleight_of_hand));
                updateSkillProficiencyText(athleticsProficiency, monster.getDexterity().getStealth(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.stealth));

                updateAbilityModifier(dexterityModifier, monster.getDexterity().getScoreModifier());
                updateSavingThrowText(strSave, monster.getDexterity().getScoreModifier());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterCON(GridLayout stats) {
        Spinner con = stats.findViewById(R.id.constitution);
        TextView conMod = stats.findViewById(R.id.con_mod);

        conSave = stats.findViewById(R.id.con_save);
        conSave.setChecked(oldMonster[0].getAbilityProficiency(STAT[2]));
        conSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrowText(conSave, STAT[2]);
            }
        });

        con.setSelection(oldMonster[0].getAbilityScore(STAT[2]));
        con.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                oldMonster[0].setAbilityScore(STAT[2], Integer.parseInt(con.getSelectedItem().toString()));
                updateAbilityModifier(conMod, STAT[2]);
                updateSavingThrowText(conSave, STAT[2]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterINT(GridLayout stats) {
        Spinner inte = stats.findViewById(R.id.intelligence);
        TextView intMod = stats.findViewById(R.id.int_mod);

        intSave = stats.findViewById(R.id.int_save);
        intSave.setChecked(oldMonster[0].getAbilityProficiency(STAT[3]));
        intSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrowText(intSave, STAT[3]);
            }
        });

        CheckBox arcExp = stats.findViewById(R.id.arcana_exp);
        arcExp.setChecked(oldMonster[0].getSkillExpertise(ARCANA));
        arcProf = stats.findViewById(R.id.arcana_prof);
        arcProf.setChecked(oldMonster[0].getSkillProficient(ARCANA));

        arcExp.setOnCheckedChangeListener(new CustomChangeListener(arcProf, arcExp, STAT[3], ARCANA, true));
        arcProf.setOnCheckedChangeListener(new CustomChangeListener(arcProf, arcExp, STAT[3], ARCANA, false));

        CheckBox histExp = stats.findViewById(R.id.history_exp);
        histExp.setChecked(oldMonster[0].getSkillExpertise(HISTORY));
        histProf = stats.findViewById(R.id.history_prof);
        histProf.setChecked(oldMonster[0].getSkillProficient(HISTORY));

        histExp.setOnCheckedChangeListener(new CustomChangeListener(histProf, histExp, STAT[3], HISTORY, true));
        histProf.setOnCheckedChangeListener(new CustomChangeListener(histProf, histExp, STAT[3], HISTORY, false));

        CheckBox invExp = stats.findViewById(R.id.investigation_exp);
        invExp.setChecked(oldMonster[0].getSkillExpertise(INV));
        invProf = stats.findViewById(R.id.investigation_prof);
        invProf.setChecked(oldMonster[0].getSkillProficient(INV));

        invExp.setOnCheckedChangeListener(new CustomChangeListener(invProf, invExp, STAT[3], INV, true));
        invProf.setOnCheckedChangeListener(new CustomChangeListener(invProf, invExp, STAT[3], INV, false));

        CheckBox natExp = stats.findViewById(R.id.nature_exp);
        natExp.setChecked(oldMonster[0].getSkillExpertise(NATURE));
        natProf = stats.findViewById(R.id.nature_prof);
        natProf.setChecked(oldMonster[0].getSkillProficient(NATURE));

        natExp.setOnCheckedChangeListener(new CustomChangeListener(natProf, natExp, STAT[3], NATURE, true));
        natProf.setOnCheckedChangeListener(new CustomChangeListener(natProf, natExp, STAT[3], NATURE, false));

        CheckBox relExp = stats.findViewById(R.id.religion_exp);
        relExp.setChecked(oldMonster[0].getSkillExpertise(RELIGION));
        relProf = stats.findViewById(R.id.religion_prof);
        relProf.setChecked(oldMonster[0].getSkillProficient(RELIGION));

        relExp.setOnCheckedChangeListener(new CustomChangeListener(relProf, relExp, STAT[3], RELIGION, true));
        relProf.setOnCheckedChangeListener(new CustomChangeListener(relProf, relExp, STAT[3], RELIGION, false));

        inte.setSelection(oldMonster[0].getAbilityScore(STAT[3]));
        inte.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                oldMonster[0].setAbilityScore(STAT[3], Integer.parseInt(inte.getSelectedItem().toString()));
                updateAbilityModifier(intMod, STAT[3]);
                updateSavingThrowText(intSave, STAT[3]);
                updateSkillProficiencyText(arcProf, STAT[3], ARCANA);
                updateSkillProficiencyText(histProf, STAT[3], HISTORY);
                updateSkillProficiencyText(invProf, STAT[3], INV);
                updateSkillProficiencyText(natProf, STAT[3], NATURE);
                updateSkillProficiencyText(relProf, STAT[3], RELIGION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterWIS(GridLayout stats) {
        Spinner wis = stats.findViewById(R.id.wisdom);
        TextView wisMod = stats.findViewById(R.id.wis_mod);

        wisSave = stats.findViewById(R.id.wis_save);
        wisSave.setChecked(oldMonster[0].getAbilityProficiency(STAT[4]));
        wisSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrowText(wisSave, STAT[4]);
            }
        });

        CheckBox aniExp = stats.findViewById(R.id.animal_exp);
        aniExp.setChecked(oldMonster[0].getSkillExpertise(ANIMAL));
        aniProf = stats.findViewById(R.id.animal_prof);
        aniProf.setChecked(oldMonster[0].getSkillProficient(ANIMAL));

        aniExp.setOnCheckedChangeListener(new CustomChangeListener(aniProf, aniExp, STAT[4], ANIMAL, true));
        aniProf.setOnCheckedChangeListener(new CustomChangeListener(aniProf, aniExp, STAT[4], ANIMAL, false));

        CheckBox insExp = stats.findViewById(R.id.insight_exp);
        insExp.setChecked(oldMonster[0].getSkillExpertise(INSIGHT));
        insProf = stats.findViewById(R.id.insight_prof);
        insProf.setChecked(oldMonster[0].getSkillProficient(INSIGHT));

        insExp.setOnCheckedChangeListener(new CustomChangeListener(insProf, insExp, STAT[4], INSIGHT, true));
        insProf.setOnCheckedChangeListener(new CustomChangeListener(insProf, insExp, STAT[4], INSIGHT, false));

        CheckBox medExp = stats.findViewById(R.id.medicine_exp);
        medExp.setChecked(oldMonster[0].getSkillExpertise(MEDICINE));
        medProf = stats.findViewById(R.id.medicine_prof);
        medProf.setChecked(oldMonster[0].getSkillProficient(MEDICINE));

        medExp.setOnCheckedChangeListener(new CustomChangeListener(medProf, medExp, STAT[4], MEDICINE, true));
        medProf.setOnCheckedChangeListener(new CustomChangeListener(medProf, medExp, STAT[4], MEDICINE, false));

        CheckBox perExp = stats.findViewById(R.id.perception_exp);
        perExp.setChecked(oldMonster[0].getSkillExpertise(PERCEPTION));
        perProf = stats.findViewById(R.id.perception_prof);
        perProf.setChecked(oldMonster[0].getSkillProficient(PERCEPTION));

        perExp.setOnCheckedChangeListener(new CustomChangeListener(perProf, perExp, STAT[4], PERCEPTION, true));
        perProf.setOnCheckedChangeListener(new CustomChangeListener(perProf, perExp, STAT[4], PERCEPTION, false));

        CheckBox surExp = stats.findViewById(R.id.survival_exp);
        surExp.setChecked(oldMonster[0].getSkillExpertise(SURVIVAL));
        surProf = stats.findViewById(R.id.survival_prof);
        surProf.setChecked(oldMonster[0].getSkillProficient(SURVIVAL));

        surExp.setOnCheckedChangeListener(new CustomChangeListener(surProf, surExp, STAT[4], SURVIVAL, true));
        surProf.setOnCheckedChangeListener(new CustomChangeListener(surProf, surExp, STAT[4], SURVIVAL, false));

        wis.setSelection(oldMonster[0].getAbilityScore(STAT[4]));
        wis.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                oldMonster[0].setAbilityScore(STAT[4], Integer.parseInt(wis.getSelectedItem().toString()));
                updateAbilityModifier(wisMod, STAT[4]);
                updateSavingThrowText(wisSave, STAT[4]);
                updateSkillProficiencyText(aniProf, STAT[4], ANIMAL);
                updateSkillProficiencyText(insProf, STAT[4], INSIGHT);
                updateSkillProficiencyText(medProf, STAT[4], MEDICINE);
                updateSkillProficiencyText(perProf, STAT[4], PERCEPTION);
                updateSkillProficiencyText(surProf, STAT[4], SURVIVAL);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterCHA(GridLayout stats) {
        Spinner cha = stats.findViewById(R.id.charisma);
        TextView chaMod = stats.findViewById(R.id.cha_mod);

        chaSave = stats.findViewById(R.id.cha_save);
        chaSave.setChecked(oldMonster[0].getAbilityProficiency(STAT[5]));
        chaSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSavingThrowText(chaSave, STAT[5]);
            }
        });

        CheckBox decExp = stats.findViewById(R.id.deception_exp);
        decExp.setChecked(oldMonster[0].getSkillExpertise(DEC));
        decProf = stats.findViewById(R.id.deception_prof);
        decProf.setChecked(oldMonster[0].getSkillProficient(DEC));

        decExp.setOnCheckedChangeListener(new CustomChangeListener(decProf, decExp, STAT[5], DEC, true));
        decProf.setOnCheckedChangeListener(new CustomChangeListener(decProf, decExp, STAT[5], DEC, false));

        CheckBox intimExp = stats.findViewById(R.id.intimidation_exp);
        intimExp.setChecked(oldMonster[0].getSkillExpertise(INTIM));
        intimProf = stats.findViewById(R.id.intimidation_prof);
        intimProf.setChecked(oldMonster[0].getSkillProficient(INTIM));

        intimExp.setOnCheckedChangeListener(new CustomChangeListener(intimProf, intimExp, STAT[5], INTIM, true));
        intimProf.setOnCheckedChangeListener(new CustomChangeListener(intimProf, intimExp, STAT[5], INTIM, false));

        CheckBox perfExp = stats.findViewById(R.id.performance_exp);
        perfExp.setChecked(oldMonster[0].getSkillExpertise(PERF));
        perfProf = stats.findViewById(R.id.performance_prof);
        perfProf.setChecked(oldMonster[0].getSkillProficient(PERF));

        perfExp.setOnCheckedChangeListener(new CustomChangeListener(perfProf, perfExp, STAT[5], PERF, true));
        perfProf.setOnCheckedChangeListener(new CustomChangeListener(perfProf, perfExp, STAT[5], PERF, false));

        CheckBox persuExp = stats.findViewById(R.id.persuasion_exp);
        persuExp.setChecked(oldMonster[0].getSkillExpertise(PERSUASION));
        persuProf = stats.findViewById(R.id.persuasion_prof);
        persuProf.setChecked(oldMonster[0].getSkillProficient(PERSUASION));

        persuExp.setOnCheckedChangeListener(new CustomChangeListener(persuProf, persuExp, STAT[5], PERSUASION, true));
        persuProf.setOnCheckedChangeListener(new CustomChangeListener(persuProf, persuExp, STAT[5], PERSUASION, false));

        cha.setSelection(oldMonster[0].getAbilityScore(STAT[5]));
        cha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                oldMonster[0].setAbilityScore(STAT[5], Integer.parseInt(cha.getSelectedItem().toString()));
                updateAbilityModifier(chaMod, STAT[5]);
                updateSavingThrowText(chaSave, STAT[5]);
                updateSkillProficiencyText(decProf, STAT[5], DEC);
                updateSkillProficiencyText(intimProf, STAT[5], INTIM);
                updateSkillProficiencyText(perfProf, STAT[5], PERF);
                updateSkillProficiencyText(persuProf, STAT[5], PERSUASION);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void monsterSensesLanguagesCR() {
        View senseLanguageCR = view.findViewById(R.id.monster_senses_languages_cr);

        EditText senses = senseLanguageCR.findViewById(R.id.senses);
        senses.setText(oldMonster[0].getSenses());
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
                        oldMonster[0].setSenses(senses.getText().toString());
                    }
                }, DELAY);
            }
        });

        EditText languages = senseLanguageCR.findViewById(R.id.languages);
        languages.setText(oldMonster[0].getLanguages());
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
                        oldMonster[0].setLanguages(languages.getText().toString());
                    }
                }, DELAY);
            }
        });

        TextView xp = senseLanguageCR.findViewById(R.id.xp);
        Spinner cr = senseLanguageCR.findViewById(R.id.challenge);
        List<String> ratings = Arrays.asList((getResources().getStringArray(R.array.challenge_ratings)));
        int value = ratings.indexOf(oldMonster[0].getChallenge());

        cr.setSelection(value, false);
        cr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                oldMonster[0].setChallenge(cr.getSelectedItem().toString());
                String xpString = "(" + oldMonster[0].getXP() + " XP)";
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

        abilityList = oldMonster[0].getAbilities();

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
                            oldMonster[0].renameAbility(abilityList.get(index).getName(), index);
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
                            oldMonster[0].setAbilityDescription(abilityList.get(index).getDescription(), index);
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
                    oldMonster[0].deleteAbility(index);
                    monsterAbilities();
                }
            });
        }

        Button add = view.findViewById(R.id.add_ability);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldMonster[0].addAbility(new Ability());
                monsterAbilities();
            }
        });
    }

    public void monsterActions() {
        if (actions.getChildCount() > 1)
            actions.removeViewsInLayout(1, actions.getChildCount() - 1);

        actions.requestLayout();

        actionList = oldMonster[0].getActions();

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
                            oldMonster[0].renameAction(actionList.get(index).getName(), index);
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
                            oldMonster[0].setActionDescription(actionList.get(index).getDescription(), index);
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
                    oldMonster[0].deleteAction(index);
                    monsterActions();
                }
            });
        }

        Button add = view.findViewById(R.id.add_action);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldMonster[0].addAction(new Action());
                monsterActions();
            }
        });
    }

    public void monsterLegendaryActions() {
        LinearLayout countLayout = legActions.findViewById(R.id.legendary_count_layout);

        if (legActions.getChildCount() > 2)
            legActions.removeViewsInLayout(2, legActions.getChildCount() - 2);

        legActions.requestLayout();

        legendaryList = oldMonster[0].getLegendaryActions();

        if (legendaryList.size() > 0) {
            countLayout.setVisibility(View.VISIBLE);

            Spinner actionCount = countLayout.findViewById(R.id.legendary_count_spinner);
            actionCount.setSelection(oldMonster[0].getLegendaryActionCount());
            actionCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    oldMonster[0].setLegendaryActionCount(actionCount.getSelectedItemPosition());
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
                            oldMonster[0].renameLegendaryAction(legendaryList.get(index).getName(), index);
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
                            oldMonster[0].setLegendaryDescription(legendaryList.get(index).getDescription(), index);
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
                    oldMonster[0].setLegendaryActionCost(legendaryList.get(index).getCost(), index);
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
                    oldMonster[0].deleteLegendaryAction(index);
                    monsterLegendaryActions();
                }
            });
        }


        Button add = view.findViewById(R.id.add_legendary_action);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldMonster[0].addLegendaryAction(new LegendaryAction());
                monsterLegendaryActions();
            }
        });
    }

    //TODO: find more elegant solution, is redrawing monster_stats better? if so, why didn't it work before?
    /**
     * A monster's CR determines it's proficiency bonus. Therefore, when a monster's CR is changed, it's proficiencies must all be recalculated
     */
    private void updateProficiencyCalcs() {
        updateSavingThrowText(strSave, STAT[0]);
        updateSkillProficiencyText(athleticsProficiency, STAT[0], getString(R.string.athletics));

        updateSavingThrowText(dexSave, STAT[1]);
        updateSkillProficiencyText(acrobaticsProficiency, STAT[1], ACRO);
        updateSkillProficiencyText(sleightofHandProficiency, STAT[1], SLEIGHT);
        updateSkillProficiencyText(stealthProfiency, STAT[1], STEALTH);

        updateSavingThrowText(conSave, STAT[2]);

        updateSavingThrowText(intSave, STAT[3]);
        updateSkillProficiencyText(arcProf, STAT[3], ARCANA);
        updateSkillProficiencyText(histProf, STAT[3], HISTORY);
        updateSkillProficiencyText(invProf, STAT[3], INV);
        updateSkillProficiencyText(natProf, STAT[3], NATURE);
        updateSkillProficiencyText(relProf, STAT[3], RELIGION);

        updateSavingThrowText(wisSave, STAT[4]);
        updateSkillProficiencyText(aniProf, STAT[4], ANIMAL);
        updateSkillProficiencyText(insProf, STAT[4], INSIGHT);
        updateSkillProficiencyText(medProf, STAT[4], MEDICINE);
        updateSkillProficiencyText(perProf, STAT[4], PERCEPTION);
        updateSkillProficiencyText(surProf, STAT[4], SURVIVAL);

        updateSavingThrowText(chaSave, STAT[5]);
        updateSkillProficiencyText(decProf, STAT[5], DEC);
        updateSkillProficiencyText(intimProf, STAT[5], INTIM);
        updateSkillProficiencyText(perfProf, STAT[5], PERF);
        updateSkillProficiencyText(persuProf, STAT[5], PERSUASION);
    }

    /**
     * Updates the saving throws text
     *
     * @param save the CheckBox for the saving throw
     * @param scoreModifier the score modifier
     */
    private void updateSavingThrowText(CheckBox save, int scoreModifier) {
        int saveBonus;
        if (save.isChecked()) {
            saveBonus = scoreModifier + monster.getChallengeRating().getProficiencyBonus();
        } else {
            saveBonus = scoreModifier;
        }

        if (saveBonus < 0) {
            save.setText(MessageFormat.format("Saving Throws: {0}", saveBonus));
        } else {
            save.setText(MessageFormat.format("Saving Throws: +{0}", saveBonus));
        }
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
        oldMonster[0].setSkillProficiency(skill, prof.isChecked());

        if (!prof.isChecked()) {
            exp.setChecked(prof.isChecked());
            oldMonster[0].setSkillExpertise(skill, exp.isChecked());
        }

        updateSkillProficiencyText(prof, stat, skill);
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
        oldMonster[0].setSkillExpertise(skill, exp.isChecked());

        if (exp.isChecked()) {
            prof.setChecked(exp.isChecked());
            oldMonster[0].setSkillProficiency(skill, prof.isChecked());
        }

        updateSkillProficiencyText(prof, stat, skill);
    }

    /**
     * Updates the text on the proficiency text box
     *
     * @param proficiencyBox the proficiency checkbox
     * @param stat the stat string
     * @param skillName the skill string
     */
    private void updateSkillProficiencyText(CheckBox proficiencyBox, int proficiencyLevel, int scoreModifier,
                                            String skillName) {
        int skillBonus;
        if (proficiencyLevel == 0) {
            skillBonus = scoreModifier;
        } else if (proficiencyLevel == 10) {
            skillBonus = scoreModifier + monster.getChallengeRating().getProficiencyBonus();
        } else if (proficiencyLevel == 2) {
            skillBonus = scoreModifier + monster.getChallengeRating().getProficiencyBonus() * 2;
        } else {
            skillBonus = 0;
        }

        if (skillBonus < 0) {
            proficiencyBox.setText(MessageFormat.format("{0}: {1}", skillName, skillBonus));
        } else {
            proficiencyBox.setText(MessageFormat.format("{0}: +{1}", skillName, skillBonus));
        }
    }

    /**
     * Updates the ability score modifier
     *
     * @param textView The TextView for the ability score
     * @param scoreModifier The score modifier
     */
    private void updateAbilityModifier(TextView textView, int scoreModifier) {
        if (scoreModifier < 0) {
            textView.setText(MessageFormat.format("({0})", scoreModifier));
        } else {
            textView.setText(MessageFormat.format("(+{0})", scoreModifier));
        }
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