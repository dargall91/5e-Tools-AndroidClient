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
import com.DnD5eTools.entities.monster.Ability;
import com.DnD5eTools.entities.monster.Action;
import com.DnD5eTools.entities.monster.LegendaryAction;
import com.DnD5eTools.entities.monster.Monster;
import com.DnD5eTools.interfaces.MonsterInterface;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.DnD5eTools.util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Tab for creating and editing Monsters.
 */
public class MonsterBuilder extends Fragment {
    private Monster monster;
    private List<NameIdProjection> monsterList;
    private List<String> monsterNameList = new ArrayList<>();
    private LayoutInflater inflater;
    private LinearLayout abilityLayout;
    private LinearLayout actionLayout;
    private LinearLayout legendaryActionLayout;
    private boolean expertise = false;
    private boolean proficiency = false;
    private View view;
    private final int DELAY = 250;
    //proficiency checkboxes
    private CheckBox strSave, dexSave, conSave, intSave, wisSave, chaSave, athleticsProficiency, acrobaticsProficiency,
            sleightOfHandProficiency, stealthProficiency, arcanaProficiency, historyProficiency,
            investigationProficiency, natureProficiency, religionProficiency, animalHandlingProficiency,
            insightProficiency, medicineProficiency, perceptionProficiency, survivalProficiency, deceptionProficiency,
            intimidationProficiency, performanceProficiency, persuasionProficiency;

    private final Runnable updateMonster = new Runnable() {
        @Override
        public void run() {
            MonsterInterface.updateMonster(monster);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;

        view = inflater.inflate(R.layout.monster_builder_layout, container, false);
        view.setId(View.generateViewId());
        view.setTag("MonsterBuilder");

        abilityLayout = view.findViewById(R.id.monster_abilities_layout);
        actionLayout = view.findViewById(R.id.monster_actions_layout);
        legendaryActionLayout = view.findViewById(R.id.monster_legendary_actions_layout);

        return view;
    }

    public void initViews() {
        monsterListView(true);
        builderView();
    }

    /**
     * Sets up the ListView that contains a list of all the monsters on the server
     *
     * @param loadNewMonster true if the monster should be set automatically, false if one is set elsewhere
     */
    private void monsterListView(boolean loadNewMonster) {
        initMonsterList();

        if (monsterList.size() == 1) {
            var newMonster = MonsterInterface.addMonster("New Monster");
            monsterList.add(new NameIdProjection(newMonster.getMonsterId(), newMonster.getName()));
            initMonsterList();
        }

        if (loadNewMonster) {
            int monsterId = monsterList.get(1).getId();
            monster = MonsterInterface.getMonster(monsterId);
        }

        ListView monListView = view.findViewById(R.id.monster_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_view, monsterNameList);
        monListView.setAdapter(adapter);
        monListView.setOnItemClickListener((parent, view, position, id) -> {
            //check if add monster was selected, should be first monster in list
            if (position == 0) {
                View addView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
                TextView text = addView.findViewById(R.id.name_textview);
                TextView errorText = addView.findViewById(R.id.add_rename_error_text);
                text.setText("Enter the new monster's name:");
                EditText newName = addView.findViewById(R.id.name_entry);

                final AlertDialog.Builder addMonsterDialog = new AlertDialog.Builder(getContext());
                addMonsterDialog.setView(addView);
                addMonsterDialog.setTitle("Add Monster");
                addMonsterDialog.setPositiveButton("OK", null);
                addMonsterDialog.setNegativeButton("Cancel", null);

                AlertDialog add = addMonsterDialog.create();
                add.setOnShowListener(dialogInterface -> {
                    Button ok = add.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(v -> {
                        final String name = newName.getText().toString();

                        //reload list then display new monster in builder
                        var newMonster = MonsterInterface.addMonster(name);

                        if (newMonster == null) {
                            errorText.setText("A monster with the name " + name + " already exists on this campaign");
                            errorText.setVisibility(View.VISIBLE);
                        } else {
                            monster = newMonster;
                            monsterListView(false);
                            builderView();
                            add.dismiss();
                        }
                    });
                });

                add.show();
            } else if (monsterList.get(position).getId() == monster.getMonsterId()) {
                //if the selected monster is the current one, do nothing and exit
                return;
            } else {
                //get new monster and display in builder
                monster = MonsterInterface.getMonster(monsterList.get(position).getId());
                builderView();
            }
        });
    }

    /**
     * Initializes the monster list including an entry at index 0 for adding new monsters
     */
    private void initMonsterList() {
        //todo: make add monster/add encounter actual buttons, eliminates the need for monster list scoped to this class
        NameIdProjection addMonster = new NameIdProjection(0, "Add Monster");

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
        initMonsterStats();
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
        name.setText(monster.getName());

        EditText displayName = basicInfo.findViewById(R.id.display_name);
        displayName.setText(monster.getDisplayName());
        displayName.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (displayName.hasFocus() && !Objects.equals(monster.getDisplayName(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setDisplayName(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText size = basicInfo.findViewById(R.id.size);
        size.setText(monster.getSize());
        size.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (size.hasFocus() && !Objects.equals(monster.getSize(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setSize(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText type = basicInfo.findViewById(R.id.type);
        type.setText(monster.getType());
        type.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (type.hasFocus() && !Objects.equals(monster.getType(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setType(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText alignment = basicInfo.findViewById(R.id.alignment);
        alignment.setText(monster.getAlignment());
        alignment.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (alignment.hasFocus() && !Objects.equals(monster.getAlignment(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setAlignment(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText ac = basicInfo.findViewById(R.id.ac);
        ac.setText(String.valueOf(monster.getArmorClass()));
        ac.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newAcString = text.toString();
                int newAc = newAcString.isBlank() ? 0 : Integer.parseInt(newAcString);
                if (ac.hasFocus() && monster.getArmorClass() != newAc) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setArmorClass(newAc);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText hitPoints = basicInfo.findViewById(R.id.hit_points);
        hitPoints.setText(String.valueOf(monster.getHitPoints()));
        hitPoints.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newHpString = text.toString();
                int newHitPoints = newHpString.isBlank() ? 0 : Integer.parseInt(newHpString);
                if (hitPoints.hasFocus() && monster.getHitPoints() != newHitPoints) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setHitPoints(newHitPoints);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText speed = basicInfo.findViewById(R.id.speed);
        speed.setText(monster.getSpeed());
        speed.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (speed.hasFocus() && !Objects.equals(monster.getSpeed(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setSpeed(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText bonusInitiative = basicInfo.findViewById(R.id.bonus_initiative);
        bonusInitiative.setText(String.valueOf(monster.getBonusInitiative()));
        bonusInitiative.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String bonusString = text.toString();
                int newBonus = bonusString.isBlank() ? 0 : Integer.parseInt(bonusString);
                if (bonusInitiative.hasFocus() && monster.getBonusInitiative() != newBonus) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setBonusInitiative(newBonus);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        Button archive = basicInfo.findViewById(R.id.archive_monster);
        archive.setOnClickListener(view -> new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Archive Monster")
                .setMessage("Archive " + monster.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    MonsterInterface.archiveMonster(monster.getMonsterId());
                    monsterListView(true);
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
            renameMonsterDialog.setTitle("Rename " + monster.getName());
            renameMonsterDialog.setPositiveButton("OK", null);
            renameMonsterDialog.setNegativeButton("Cancel", null);

            AlertDialog renameDialog = renameMonsterDialog.create();
            renameDialog.setOnShowListener(dialogInterface -> {
                Button ok = renameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    monster.setName(newName.getText().toString());
                    name.setText(monster.getName());
                    updateMonster.run();
                    monsterListView(false);
                    renameDialog.dismiss();
                });
            });

            renameDialog.show();
        });

        Button copy = basicInfo.findViewById(R.id.copy_monster);
        copy.setOnClickListener(view -> {
            View renameView = inflater.inflate(R.layout.rename_add_monster_dialog, null);
            TextView text = renameView.findViewById(R.id.name_textview);
            TextView errorText = renameView.findViewById(R.id.add_rename_error_text);
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
                    final String newNameString = newName.getText().toString();

                    Monster newMonster = MonsterInterface.copyMonster(monster.getMonsterId(), newNameString);

                    if (newMonster == null) {
                        errorText.setText("A monster with the name " + newNameString + " already exists on this campaign");
                        errorText.setVisibility(View.VISIBLE);
                    } else {
                        monster = newMonster;
                        monsterListView(false);
                        builderView();
                        copyDialog.dismiss();
                    }
                });
            });

            copyDialog.show();
        });
    }

    private void initMonsterStats() {
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
            if (buttonView.isPressed()) {
                monster.getStrength().setProficient(isChecked);
                updateMonster.run();
                updateSavingThrowText(strSave, monster.getStrength().getScoreModifier());
            }
        });

        CheckBox athleticsExpertise = stats.findViewById(R.id.athletics_exp);
        athleticsExpertise.setChecked(monster.getStrength().getAthletics() == 2);
        athleticsProficiency = stats.findViewById(R.id.athletics_prof);
        athleticsProficiency.setChecked(monster.getStrength().getAthletics() > 0);

        athleticsExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
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

                updateMonster.run();
                updateSkillProficiencyText(athleticsProficiency, monster.getStrength().getAthletics(),
                        monster.getStrength().getScoreModifier(), getString(R.string.athletics));
                expertise = false;
            }
        });

        athleticsProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
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

                updateMonster.run();
                updateSkillProficiencyText(athleticsProficiency, monster.getStrength().getAthletics(),
                        monster.getStrength().getScoreModifier(), getString(R.string.athletics));
                proficiency = false;
            }
        });

        strengthSpinner.setSelection(monster.getStrength().getScore(), false);
        strengthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getStrength().getScore() != position) {
                    monster.getStrength().setScore(position);
                    updateMonster.run();
                    updateStrengthProficiencies();
                    updateAbilityModifier(strengthModifier, monster.getStrength().getScoreModifier());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //handle initial load
        updateStrengthProficiencies();
        updateAbilityModifier(strengthModifier, monster.getStrength().getScoreModifier());
    }

    private void monsterDEX(GridLayout stats) {
        Spinner dexteritySpinner = stats.findViewById(R.id.dexterity);
        TextView dexterityModifier = stats.findViewById(R.id.dex_mod);

        dexSave = stats.findViewById(R.id.dex_save);
        dexSave.setChecked(monster.getDexterity().isProficient());
        dexSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                monster.getDexterity().setProficient(isChecked);
                updateMonster.run();
                updateSavingThrowText(dexSave, monster.getDexterity().getScoreModifier());
            }
        });

        CheckBox acrobaticsExpertise = stats.findViewById(R.id.acrobatics_exp);
        acrobaticsExpertise.setChecked(monster.getDexterity().getAcrobatics() == 2);
        acrobaticsProficiency = stats.findViewById(R.id.acrobatics_prof);
        acrobaticsProficiency.setChecked(monster.getDexterity().getAcrobatics() > 0);

        acrobaticsExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
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

                updateMonster.run();
                updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getAcrobatics(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.acrobatics));
                expertise = false;
            }
        });

        acrobaticsProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
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

                updateMonster.run();
                updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getAcrobatics(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.acrobatics));
                proficiency = false;
            }
        });

        updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getAcrobatics(),
                monster.getDexterity().getScoreModifier(), getString(R.string.acrobatics));

        CheckBox sleightOfHandExpertise = stats.findViewById(R.id.sleight_exp);
        sleightOfHandExpertise.setChecked(monster.getDexterity().getSleightOfHand() == 2);
        sleightOfHandProficiency = stats.findViewById(R.id.sleight_prof);
        sleightOfHandProficiency.setChecked(monster.getDexterity().getSleightOfHand() > 0);

        sleightOfHandExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    sleightOfHandProficiency.setChecked(true);
                    monster.getDexterity().setSleightOfHand(2);
                } else {
                    monster.getDexterity().setSleightOfHand(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(sleightOfHandProficiency, monster.getDexterity().getSleightOfHand(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.sleight_of_hand));
                expertise = false;
            }
        });
        sleightOfHandProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
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

                updateMonster.run();
                updateSkillProficiencyText(sleightOfHandProficiency, monster.getDexterity().getSleightOfHand(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.sleight_of_hand));
                proficiency = false;
            }
        });

        CheckBox stealthExpertise = stats.findViewById(R.id.stealth_exp);
        stealthExpertise.setChecked(monster.getDexterity().getStealth() == 2);
        stealthProficiency = stats.findViewById(R.id.stealth_prof);
        stealthProficiency.setChecked(monster.getDexterity().getStealth() > 0);

        stealthExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    stealthProficiency.setChecked(true);
                    monster.getDexterity().setStealth(2);
                } else {
                    monster.getDexterity().setStealth(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(stealthProficiency, monster.getDexterity().getStealth(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.stealth));
                expertise = false;
            }
        });
        stealthProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
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

                updateMonster.run();
                updateSkillProficiencyText(stealthProficiency, monster.getDexterity().getStealth(),
                        monster.getDexterity().getScoreModifier(), getString(R.string.stealth));
                proficiency = false;
            }
        });

        dexteritySpinner.setSelection(monster.getDexterity().getScore(), false);
        dexteritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getDexterity().getScore() != position) {
                    monster.getDexterity().setScore(position);
                    updateMonster.run();
                    updateDexterityProficiencies();
                    updateAbilityModifier(dexterityModifier, monster.getDexterity().getScoreModifier());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //handle initial load
        updateDexterityProficiencies();
        updateAbilityModifier(dexterityModifier, monster.getDexterity().getScoreModifier());
    }

    private void monsterCON(GridLayout stats) {
        Spinner constitution = stats.findViewById(R.id.constitution);
        TextView constitutionModifier = stats.findViewById(R.id.con_mod);

        conSave = stats.findViewById(R.id.con_save);
        conSave.setChecked(monster.getConstitution().isProficient());
        conSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                monster.getConstitution().setProficient(isChecked);
                updateMonster.run();
                updateSavingThrowText(conSave, monster.getConstitution().getScoreModifier());
            }
        });

        constitution.setSelection(monster.getConstitution().getScore(), false);
        constitution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getConstitution().getScore() != position) {
                    monster.getConstitution().setScore(position);
                    updateMonster.run();
                    updateConstitutionProficiencies();
                    updateAbilityModifier(constitutionModifier, monster.getConstitution().getScoreModifier());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //handle initial load
        updateConstitutionProficiencies();
        updateAbilityModifier(constitutionModifier, monster.getConstitution().getScoreModifier());
    }

    private void monsterINT(GridLayout stats) {
        Spinner intelligence = stats.findViewById(R.id.intelligence);
        TextView intelligenceModifier = stats.findViewById(R.id.int_mod);

        intSave = stats.findViewById(R.id.int_save);
        intSave.setChecked(monster.getIntelligence().isProficient());
        intSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                monster.getIntelligence().setProficient(isChecked);
                updateMonster.run();
                updateSavingThrowText(intSave, monster.getIntelligence().getScoreModifier());
            }
        });

        CheckBox arcanaExpertise = stats.findViewById(R.id.arcana_exp);
        arcanaExpertise.setChecked(monster.getIntelligence().getArcana() == 2);
        arcanaProficiency = stats.findViewById(R.id.arcana_prof);
        arcanaProficiency.setChecked(monster.getIntelligence().getArcana() > 0);

        arcanaExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    arcanaProficiency.setChecked(true);
                    monster.getIntelligence().setArcana(2);
                } else {
                    monster.getIntelligence().setArcana(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(arcanaProficiency, monster.getIntelligence().getArcana(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.arcana));
                expertise = false;
            }
        });
        arcanaProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    arcanaExpertise.setChecked(false);
                    monster.getIntelligence().setArcana(0);
                } else {
                    monster.getIntelligence().setArcana(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(arcanaProficiency, monster.getIntelligence().getArcana(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.arcana));
                proficiency = false;
            }
        });

        CheckBox historyExpertise = stats.findViewById(R.id.history_exp);
        historyExpertise.setChecked(monster.getIntelligence().getHistory() == 2);
        historyProficiency = stats.findViewById(R.id.history_prof);
        historyProficiency.setChecked(monster.getIntelligence().getHistory() > 0);

        historyExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    historyProficiency.setChecked(true);
                    monster.getIntelligence().setHistory(2);
                } else {
                    monster.getIntelligence().setHistory(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(historyProficiency, monster.getIntelligence().getHistory(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.history));
                expertise = false;
            }
        });
        historyProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    historyExpertise.setChecked(false);
                    monster.getIntelligence().setHistory(0);
                } else {
                    monster.getIntelligence().setHistory(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(historyProficiency, monster.getIntelligence().getHistory(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.history));
                proficiency = false;
            }
        });

        CheckBox investigationExpertise = stats.findViewById(R.id.investigation_exp);
        investigationExpertise.setChecked(monster.getIntelligence().getInvestigation() == 2);
        investigationProficiency = stats.findViewById(R.id.investigation_prof);
        investigationProficiency.setChecked(monster.getIntelligence().getInvestigation() > 0);

        investigationExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    investigationProficiency.setChecked(true);
                    monster.getIntelligence().setInvestigation(2);
                } else {
                    monster.getIntelligence().setInvestigation(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(investigationProficiency, monster.getIntelligence().getInvestigation(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.investigation));
                expertise = false;
            }
        });
        investigationProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    investigationExpertise.setChecked(false);
                    monster.getIntelligence().setInvestigation(0);
                } else {
                    monster.getIntelligence().setInvestigation(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(investigationProficiency, monster.getIntelligence().getInvestigation(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.investigation));
                proficiency = false;
            }
        });

        CheckBox natureExpertise = stats.findViewById(R.id.nature_exp);
        natureExpertise.setChecked(monster.getIntelligence().getNature() == 2);
        natureProficiency = stats.findViewById(R.id.nature_prof);
        natureProficiency.setChecked(monster.getIntelligence().getNature() > 0);

        natureExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    natureProficiency.setChecked(true);
                    monster.getIntelligence().setNature(2);
                } else {
                    monster.getIntelligence().setNature(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(natureProficiency, monster.getIntelligence().getNature(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.nature));
                expertise = false;
            }
        });
        natureProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    natureExpertise.setChecked(false);
                    monster.getIntelligence().setNature(0);
                } else {
                    monster.getIntelligence().setNature(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(natureProficiency, monster.getIntelligence().getNature(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.nature));
                proficiency = false;
            }
        });

        CheckBox religionExpertise = stats.findViewById(R.id.religion_exp);
        religionExpertise.setChecked(monster.getIntelligence().getReligion() == 2);
        religionProficiency = stats.findViewById(R.id.religion_prof);
        religionProficiency.setChecked(monster.getIntelligence().getReligion() > 0);

        religionExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    religionProficiency.setChecked(true);
                    monster.getIntelligence().setReligion(2);
                } else {
                    monster.getIntelligence().setReligion(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(religionProficiency, monster.getIntelligence().getReligion(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.religion));
                expertise = false;
            }
        });
        religionProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    religionExpertise.setChecked(false);
                    monster.getIntelligence().setReligion(0);
                } else {
                    monster.getIntelligence().setReligion(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(religionProficiency, monster.getIntelligence().getReligion(),
                        monster.getIntelligence().getScoreModifier(), getString(R.string.religion));
                proficiency = false;
            }
        });

        intelligence.setSelection(monster.getIntelligence().getScore(), false);
        intelligence.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getIntelligence().getScore() != position) {
                    monster.getIntelligence().setScore(position);
                    updateMonster.run();
                    updateIntelligenceProficiencies();
                    updateAbilityModifier(intelligenceModifier, monster.getIntelligence().getScoreModifier());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //handle initial load
        updateIntelligenceProficiencies();
        updateAbilityModifier(intelligenceModifier, monster.getIntelligence().getScoreModifier());
    }

    private void monsterWIS(GridLayout stats) {
        Spinner wisdom = stats.findViewById(R.id.wisdom);
        TextView wisdomModifier = stats.findViewById(R.id.wis_mod);

        wisSave = stats.findViewById(R.id.wis_save);
        wisSave.setChecked(monster.getWisdom().isProficient());
        wisSave.setOnCheckedChangeListener((buttonView, isChecked) ->{
            if (buttonView.isPressed()) {
                monster.getWisdom().setProficient(isChecked);
                updateMonster.run();
                updateSavingThrowText(wisSave, monster.getWisdom().getScoreModifier());
            }
        });

        CheckBox animalHandlingExpertise = stats.findViewById(R.id.animal_exp);
        animalHandlingExpertise.setChecked(monster.getWisdom().getAnimalHandling() == 2);
        animalHandlingProficiency = stats.findViewById(R.id.animal_prof);
        animalHandlingProficiency.setChecked(monster.getWisdom().getAnimalHandling() > 0);

        animalHandlingExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    animalHandlingProficiency.setChecked(true);
                    monster.getWisdom().setAnimalHandling(2);
                } else {
                    monster.getWisdom().setAnimalHandling(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(animalHandlingProficiency, monster.getWisdom().getAnimalHandling(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.animal_handling));
                expertise = false;
            }
        });
        animalHandlingProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    animalHandlingExpertise.setChecked(false);
                    monster.getWisdom().setAnimalHandling(0);
                } else {
                    monster.getWisdom().setAnimalHandling(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(animalHandlingProficiency, monster.getWisdom().getAnimalHandling(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.animal_handling));
                proficiency = false;
            }
        });

        CheckBox insightExpertise = stats.findViewById(R.id.insight_exp);
        insightExpertise.setChecked(monster.getWisdom().getInsight() == 2);
        insightProficiency = stats.findViewById(R.id.insight_prof);
        insightProficiency.setChecked(monster.getWisdom().getInsight() > 0);

        insightExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    insightProficiency.setChecked(true);
                    monster.getWisdom().setInsight(2);
                } else {
                    monster.getWisdom().setInsight(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(insightProficiency, monster.getWisdom().getInsight(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.insight));
                expertise = false;
            }
        });
        insightProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    insightExpertise.setChecked(false);
                    monster.getWisdom().setInsight(0);
                } else {
                    monster.getWisdom().setInsight(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(insightProficiency, monster.getWisdom().getInsight(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.insight));
                proficiency = false;
            }
        });

        CheckBox medicineExpertise = stats.findViewById(R.id.medicine_exp);
        medicineExpertise.setChecked(monster.getWisdom().getMedicine() == 2);
        medicineProficiency = stats.findViewById(R.id.medicine_prof);
        medicineProficiency.setChecked(monster.getWisdom().getMedicine() > 0);

        medicineExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    medicineProficiency.setChecked(true);
                    monster.getWisdom().setMedicine(2);
                } else {
                    monster.getWisdom().setMedicine(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(medicineProficiency, monster.getWisdom().getMedicine(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.medicine));
                expertise = false;
            }
        });
        medicineProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    medicineExpertise.setChecked(false);
                    monster.getWisdom().setMedicine(0);
                } else {
                    monster.getWisdom().setMedicine(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(medicineProficiency, monster.getWisdom().getMedicine(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.medicine));
                proficiency = false;
            }
        });

        CheckBox perceptionExpertise = stats.findViewById(R.id.perception_exp);
        perceptionExpertise.setChecked(monster.getWisdom().getPerception() == 2);
        perceptionProficiency = stats.findViewById(R.id.perception_prof);
        perceptionProficiency.setChecked(monster.getWisdom().getPerception() > 0);

        perceptionExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    perceptionProficiency.setChecked(true);
                    monster.getWisdom().setPerception(2);
                } else {
                    monster.getWisdom().setPerception(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(perceptionProficiency, monster.getWisdom().getPerception(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.perception));
                expertise = false;
            }
        });
        perceptionProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    perceptionExpertise.setChecked(false);
                    monster.getWisdom().setPerception(0);
                } else {
                    monster.getWisdom().setPerception(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(perceptionProficiency, monster.getWisdom().getPerception(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.perception));
                proficiency = false;
            }
        });

        CheckBox survivalExpertise = stats.findViewById(R.id.survival_exp);
        survivalExpertise.setChecked(monster.getWisdom().getSurvival() == 2);
        survivalProficiency = stats.findViewById(R.id.survival_prof);
        survivalProficiency.setChecked(monster.getWisdom().getSurvival() > 0);

        survivalExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    survivalProficiency.setChecked(true);
                    monster.getWisdom().setSurvival(2);
                } else {
                    monster.getWisdom().setSurvival(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(survivalProficiency, monster.getWisdom().getSurvival(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.survival));
                expertise = false;
            }
        });
        survivalProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    survivalExpertise.setChecked(false);
                    monster.getWisdom().setSurvival(0);
                } else {
                    monster.getWisdom().setSurvival(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(survivalProficiency, monster.getWisdom().getSurvival(),
                        monster.getWisdom().getScoreModifier(), getString(R.string.survival));
                proficiency = false;
            }
        });

        wisdom.setSelection(monster.getWisdom().getScore(), false);
        wisdom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getWisdom().getScore() != position) {
                    monster.getWisdom().setScore(position);
                    updateMonster.run();
                    updateWisdomProficiencies();
                    updateAbilityModifier(wisdomModifier, monster.getWisdom().getScoreModifier());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //handle initial load
        updateWisdomProficiencies();
        updateAbilityModifier(wisdomModifier, monster.getWisdom().getScoreModifier());
    }

    private void monsterCHA(GridLayout stats) {
        Spinner charisma = stats.findViewById(R.id.charisma);
        TextView charismaModifier = stats.findViewById(R.id.cha_mod);

        chaSave = stats.findViewById(R.id.cha_save);
        chaSave.setChecked(monster.getCharisma().isProficient());
        chaSave.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                monster.getCharisma().setProficient(isChecked);
                updateMonster.run();
                updateSavingThrowText(chaSave, monster.getCharisma().getScoreModifier());
            }
        });

        CheckBox deceptionExpertise = stats.findViewById(R.id.deception_exp);
        deceptionExpertise.setChecked(monster.getCharisma().getDeception() == 2);
        deceptionProficiency = stats.findViewById(R.id.deception_prof);
        deceptionProficiency.setChecked(monster.getCharisma().getDeception() > 0);

        deceptionExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    deceptionProficiency.setChecked(true);
                    monster.getCharisma().setDeception(2);
                } else {
                    monster.getCharisma().setDeception(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(deceptionProficiency, monster.getCharisma().getDeception(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.deception));
                expertise = false;
            }

        });
        deceptionProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    deceptionExpertise.setChecked(false);
                    monster.getCharisma().setDeception(0);
                } else {
                    monster.getCharisma().setDeception(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(deceptionProficiency, monster.getCharisma().getDeception(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.deception));
                proficiency = false;
            }
        });

        CheckBox intimidationExpertise = stats.findViewById(R.id.intimidation_exp);
        intimidationExpertise.setChecked(monster.getCharisma().getIntimidation() == 2);
        intimidationProficiency = stats.findViewById(R.id.intimidation_prof);
        intimidationProficiency.setChecked(monster.getCharisma().getIntimidation() > 0);

        intimidationExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    intimidationProficiency.setChecked(true);
                    monster.getCharisma().setIntimidation(2);
                } else {
                    monster.getCharisma().setIntimidation(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(intimidationProficiency, monster.getCharisma().getIntimidation(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.intimidation));
                expertise = false;
            }
        });
        intimidationProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    intimidationExpertise.setChecked(false);
                    monster.getCharisma().setIntimidation(0);
                } else {
                    monster.getCharisma().setIntimidation(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(intimidationProficiency, monster.getCharisma().getIntimidation(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.intimidation));
                proficiency = false;
            }
        });

        CheckBox performanceExpertise = stats.findViewById(R.id.performance_exp);
        performanceExpertise.setChecked(monster.getCharisma().getPerformance() == 2);
        performanceProficiency = stats.findViewById(R.id.performance_prof);
        performanceProficiency.setChecked(monster.getCharisma().getPerformance() > 0);

        performanceExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    performanceProficiency.setChecked(true);
                    monster.getCharisma().setPerformance(2);
                } else {
                    monster.getCharisma().setPerformance(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(performanceProficiency, monster.getCharisma().getPerformance(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.performance));
                expertise = false;
            }
        });
        performanceProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    performanceExpertise.setChecked(false);
                    monster.getCharisma().setPerformance(0);
                } else {
                    monster.getCharisma().setPerformance(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(performanceProficiency, monster.getCharisma().getPerformance(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.performance));
                proficiency = false;
            }
        });

        CheckBox persuasionExpertise = stats.findViewById(R.id.persuasion_exp);
        persuasionExpertise.setChecked(monster.getCharisma().getPersuasion() == 2);
        persuasionProficiency = stats.findViewById(R.id.persuasion_prof);
        persuasionProficiency.setChecked(monster.getCharisma().getDeception() > 0);

        persuasionExpertise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (proficiency) {
                    return;
                }

                expertise = true;

                if (isChecked) {
                    persuasionProficiency.setChecked(true);
                    monster.getCharisma().setPersuasion(2);
                } else {
                    monster.getCharisma().setPersuasion(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(persuasionProficiency, monster.getCharisma().getPersuasion(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.persuasion));
                expertise = false;
            }
        });
        persuasionProficiency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                if (expertise) {
                    return;
                }

                proficiency = true;

                if (!isChecked) {
                    persuasionExpertise.setChecked(false);
                    monster.getCharisma().setPersuasion(0);
                } else {
                    monster.getCharisma().setPersuasion(1);
                }

                updateMonster.run();
                updateSkillProficiencyText(persuasionProficiency, monster.getCharisma().getPersuasion(),
                        monster.getCharisma().getScoreModifier(), getString(R.string.persuasion));
                proficiency = false;
            }
        });

        charisma.setSelection(monster.getCharisma().getScore(), false);
        charisma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getCharisma().getScore() != position) {
                    monster.getCharisma().setScore(position);
                    updateMonster.run();
                    updateCharismaProficiencies();
                    updateAbilityModifier(charismaModifier, monster.getCharisma().getScoreModifier());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //handle initial load
        updateCharismaProficiencies();
        updateAbilityModifier(charismaModifier, monster.getCharisma().getScoreModifier());
    }

    private void monsterSensesLanguagesCR() {
        View senseLanguageCR = view.findViewById(R.id.monster_senses_languages_cr);

        EditText senses = senseLanguageCR.findViewById(R.id.senses);
        senses.setText(monster.getSenses());
        senses.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (senses.hasFocus() && !Objects.equals(monster.getSenses(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setSenses(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        EditText languages = senseLanguageCR.findViewById(R.id.languages);
        languages.setText(monster.getLanguages());
        languages.addTextChangedListener(new TextWatcher() {
            final Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable text) {
                String newText = text.toString();
                if (languages.hasFocus() && !Objects.equals(monster.getLanguages(), newText)) {
                    handler.removeCallbacksAndMessages(null);
                    monster.setLanguages(newText);
                    handler.postDelayed(updateMonster, DELAY);
                }
            }
        });

        TextView xp = senseLanguageCR.findViewById(R.id.xp);
        Spinner crSpinner = senseLanguageCR.findViewById(R.id.challenge);
        ArrayAdapter<String> crAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, Util.getChallengeRatingCrList());
        crSpinner.setAdapter(crAdapter);
        crSpinner.setSelection(monster.getChallengeRating().getId() - 1, false);
        xp.setText(MessageFormat.format("({0} XP)", monster.getChallengeRating().getXp()));

        crSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (monster.getChallengeRating().getId() != position + 1) {
                    int oldProficiency = monster.getChallengeRating().getProficiencyBonus();
                    monster.setChallengeRating(Util.getChallengeRating(position));
                    xp.setText(MessageFormat.format("({0} XP)", monster.getChallengeRating().getXp()));
                    int newProficiency = monster.getChallengeRating().getProficiencyBonus();

                    if (oldProficiency != newProficiency) {
                        updateProficiencies();
                    }

                    updateMonster.run();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    public void monsterAbilities() {
        if (abilityLayout.getChildCount() > 1) {
            abilityLayout.removeViewsInLayout(1, abilityLayout.getChildCount() - 1);
        }

        //required to complete clear view after deleting last action
        abilityLayout.requestLayout();

        List<Ability> abilityList = monster.getAbilities();

        for (int i = 0; i < monster.getAbilities().size(); i++) {
            final int index = i;
            View abilityView = inflater.inflate(R.layout.monster_action_ability, abilityLayout);

            EditText name = abilityView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(abilityList.get(index).getName());
            name.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    String newText = text.toString();
                    if (name.hasFocus() && !Objects.equals(monster.getAbilities().get(index).getName(), newText)) {
                        handler.removeCallbacksAndMessages(null);
                        monster.getAbilities().get(index).setName(newText);
                        handler.postDelayed(updateMonster, DELAY);
                    }
                }
            });

            EditText desc = abilityView.findViewById(R.id.description);
            desc.setId(index);
            desc.setTag(index);
            desc.setText(abilityList.get(index).getDescription());
            desc.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    String newText = text.toString();
                    if (desc.hasFocus() && !Objects.equals(monster.getAbilities().get(index).getDescription(), newText)) {
                        handler.removeCallbacksAndMessages(null);
                        monster.getAbilities().get(index).setDescription(newText);
                        handler.postDelayed(updateMonster, DELAY);
                    }
                }
            });

            Button delete = abilityView.findViewById(R.id.delete_ability_action);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(view -> {
                monster.getAbilities().remove(index);
                monsterAbilities();
            });
        }

        Button add = view.findViewById(R.id.add_ability);
        add.setOnClickListener(view -> {
            monster.getAbilities().add(new Ability());
            monsterAbilities();
        });
    }

    public void monsterActions() {
        if (actionLayout.getChildCount() > 1) {
            actionLayout.removeViewsInLayout(1, actionLayout.getChildCount() - 1);
        }

        actionLayout.requestLayout();

        List<Action> actionList = monster.getActions();

        for (int i = 0; i < actionList.size(); i++) {
            final int index = i;
            View actionView = inflater.inflate(R.layout.monster_action_ability, actionLayout);

            EditText name = actionView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(actionList.get(index).getName());
            name.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    String newText = text.toString();
                    if (name.hasFocus() && !Objects.equals(monster.getActions().get(index).getName(), newText)) {
                        handler.removeCallbacksAndMessages(null);
                        monster.getActions().get(index).setName(newText);
                        handler.postDelayed(updateMonster, DELAY);
                    }
                }
            });

            EditText desc = actionView.findViewById(R.id.description);
            desc.setId(index);
            desc.setTag(index);
            desc.setText(actionList.get(index).getDescription());
            desc.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    String newText = text.toString();
                    if (desc.hasFocus() && !Objects.equals(monster.getActions().get(index).getDescription(), newText)) {
                        handler.removeCallbacksAndMessages(null);
                        monster.getActions().get(index).setDescription(newText);
                        handler.postDelayed(updateMonster, DELAY);
                    }
                }
            });

            Button delete = actionView.findViewById(R.id.delete_ability_action);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(view -> {
                monster.getActions().remove(index);
                monsterActions();
            });
        }

        Button add = view.findViewById(R.id.add_action);
        add.setOnClickListener(view -> {
            monster.getActions().add(new Action());
            monsterActions();
        });
    }

    public void monsterLegendaryActions() {
        LinearLayout countLayout = legendaryActionLayout.findViewById(R.id.legendary_count_layout);

        if (legendaryActionLayout.getChildCount() > 2) {
            legendaryActionLayout.removeViewsInLayout(2, legendaryActionLayout.getChildCount() - 2);
        }

        List<LegendaryAction> legendaryList = monster.getLegendaryActions();

        if (legendaryList.size() > 0) {
            countLayout.setVisibility(View.VISIBLE);

            Spinner actionCount = countLayout.findViewById(R.id.legendary_count_spinner);
            actionCount.setSelection(monster.getLegendaryActionCount(), false);
            actionCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (monster.getLegendaryActionCount() != position) {
                        monster.setLegendaryActionCount(position);
                        updateMonster.run();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        } else {
            countLayout.setVisibility(View.GONE);
        }

        for (int i = 0; i < legendaryList.size(); i++) {
            final int index = i;
            View legView = inflater.inflate(R.layout.monster_legendary_action, legendaryActionLayout);

            EditText name = legView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(legendaryList.get(index).getName());
            name.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    String newText = text.toString();
                    if (name.hasFocus() && !Objects.equals(monster.getLegendaryActions().get(index).getName(), newText)) {
                        handler.removeCallbacksAndMessages(null);
                        monster.getLegendaryActions().get(index).setName(newText);
                        handler.postDelayed(updateMonster, DELAY);
                    }
                }
            });

            EditText desc = legView.findViewById(R.id.description);
            desc.setId(index);
            desc.setTag(index);
            desc.setText(legendaryList.get(index).getDescription());
            desc.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    String newText = text.toString();
                    if (desc.hasFocus() && !Objects.equals(monster.getLegendaryActions().get(index).getDescription(), newText)) {
                        handler.removeCallbacksAndMessages(null);
                        monster.getLegendaryActions().get(index).setDescription(newText);
                        handler.postDelayed(updateMonster, DELAY);
                    }
                }
            });

            Spinner cost = legView.findViewById(R.id.cost);
            cost.setId(index);
            cost.setTag(index);
            cost.setSelection(legendaryList.get(index).getCost(), false);
            cost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (monster.getLegendaryActions().get(index).getCost() != position) {
                        monster.getLegendaryActions().get(index).setCost(position);
                        updateMonster.run();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            Button delete = legView.findViewById(R.id.delete_ability_action);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(v -> {
                monster.getLegendaryActions().remove(index);
                monsterLegendaryActions();
            });
        }

        Button add = view.findViewById(R.id.add_legendary_action);
        add.setOnClickListener(v -> {
            monster.getLegendaryActions().add(new LegendaryAction());
            monsterLegendaryActions();
        });
    }

    private void updateStrengthProficiencies() {
        updateSkillProficiencyText(athleticsProficiency, monster.getStrength().getAthletics(),
                monster.getStrength().getScoreModifier(), getString(R.string.athletics));
        updateSavingThrowText(strSave, monster.getStrength().getScoreModifier());
    }

    private void updateDexterityProficiencies() {
        updateSkillProficiencyText(acrobaticsProficiency, monster.getDexterity().getAcrobatics(),
                monster.getDexterity().getScoreModifier(), getString(R.string.acrobatics));
        updateSkillProficiencyText(sleightOfHandProficiency, monster.getDexterity().getSleightOfHand(),
                monster.getDexterity().getScoreModifier(), getString(R.string.sleight_of_hand));
        updateSkillProficiencyText(stealthProficiency, monster.getDexterity().getStealth(),
                monster.getDexterity().getScoreModifier(), getString(R.string.stealth));
        updateSavingThrowText(dexSave, monster.getDexterity().getScoreModifier());
    }

    private void updateConstitutionProficiencies() {
        updateSavingThrowText(conSave, monster.getConstitution().getScoreModifier());
    }

    private void updateIntelligenceProficiencies() {
        updateSkillProficiencyText(arcanaProficiency, monster.getIntelligence().getArcana(),
                monster.getIntelligence().getScoreModifier(), getString(R.string.arcana));
        updateSkillProficiencyText(historyProficiency, monster.getIntelligence().getHistory(),
                monster.getIntelligence().getScoreModifier(), getString(R.string.history));
        updateSkillProficiencyText(investigationProficiency, monster.getIntelligence().getInvestigation(),
                monster.getIntelligence().getScoreModifier(), getString(R.string.investigation));
        updateSkillProficiencyText(natureProficiency, monster.getIntelligence().getNature(),
                monster.getIntelligence().getScoreModifier(), getString(R.string.nature));
        updateSkillProficiencyText(religionProficiency, monster.getIntelligence().getReligion(),
                monster.getIntelligence().getScoreModifier(), getString(R.string.religion));
        updateSavingThrowText(intSave, monster.getIntelligence().getScoreModifier());
    }

    private void updateWisdomProficiencies() {
        updateSkillProficiencyText(animalHandlingProficiency, monster.getWisdom().getAnimalHandling(),
                monster.getWisdom().getScoreModifier(), getString(R.string.animal_handling));
        updateSkillProficiencyText(insightProficiency, monster.getWisdom().getInsight(),
                monster.getWisdom().getScoreModifier(), getString(R.string.insight));
        updateSkillProficiencyText(medicineProficiency, monster.getWisdom().getMedicine(),
                monster.getWisdom().getScoreModifier(), getString(R.string.medicine));
        updateSkillProficiencyText(perceptionProficiency, monster.getWisdom().getPerception(),
                monster.getWisdom().getScoreModifier(), getString(R.string.perception));
        updateSkillProficiencyText(survivalProficiency, monster.getWisdom().getSurvival(),
                monster.getWisdom().getScoreModifier(), getString(R.string.survival));
        updateSavingThrowText(wisSave, monster.getWisdom().getScoreModifier());
    }

    private void updateCharismaProficiencies() {
        updateSkillProficiencyText(deceptionProficiency, monster.getCharisma().getDeception(),
                monster.getCharisma().getScoreModifier(), getString(R.string.deception));
        updateSkillProficiencyText(intimidationProficiency, monster.getCharisma().getIntimidation(),
                monster.getCharisma().getScoreModifier(), getString(R.string.intimidation));
        updateSkillProficiencyText(performanceProficiency, monster.getCharisma().getPerformance(),
                monster.getCharisma().getScoreModifier(), getString(R.string.performance));
        updateSkillProficiencyText(persuasionProficiency, monster.getCharisma().getPersuasion(),
                monster.getCharisma().getScoreModifier(), getString(R.string.persuasion));
        updateSavingThrowText(chaSave, monster.getCharisma().getScoreModifier());
    }

    /**
     * A monster's CR determines it's proficiency bonus. Therefore, when a monster's CR is changed, it's proficiencies must all be recalculated
     */
    private void updateProficiencies() {
        updateStrengthProficiencies();
        updateDexterityProficiencies();
        updateConstitutionProficiencies();
        updateIntelligenceProficiencies();
        updateWisdomProficiencies();
        updateCharismaProficiencies();
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
     * Updates the text on the proficiency text box
     *
     * @param proficiencyBox the proficiency checkbox
     * @param proficiencyLevel the level of proficiency: 0 == none, 1 == proficient, 2 == expertise
     * @param scoreModifier the ability score's unsigned modifier
     * @param skillName the skill string
     */
    private void updateSkillProficiencyText(CheckBox proficiencyBox, int proficiencyLevel, int scoreModifier,
                                            String skillName) {
        int skillBonus;

        if (proficiencyLevel == 1) {
            skillBonus = scoreModifier + monster.getChallengeRating().getProficiencyBonus();
        } else if (proficiencyLevel == 2) {
            skillBonus = scoreModifier + monster.getChallengeRating().getProficiencyBonus() * 2;
        } else {
            skillBonus = scoreModifier;
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
}