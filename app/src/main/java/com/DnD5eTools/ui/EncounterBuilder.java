package com.DnD5eTools.ui;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DnD5eTools.R;
import com.DnD5eTools.entities.Music;
import com.DnD5eTools.entities.encounter.Encounter;
import com.DnD5eTools.entities.encounter.EncounterMonster;
import com.DnD5eTools.entities.encounter.XpThresholds;
import com.DnD5eTools.entities.monster.Monster;
import com.DnD5eTools.interfaces.EncounterInterface;
import com.DnD5eTools.interfaces.MonsterInterface;
import com.DnD5eTools.interfaces.MusicInterface;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.DnD5eTools.util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tabs for creating and editing Encounters.
 */
public class EncounterBuilder extends Fragment {
    private Encounter encounter;
    private List<String> encounterNameList;
    private List<NameIdProjection> encounterList;
    private List<String> musicNameList = new ArrayList<>();
    private List<Music> musicList;
    private LayoutInflater inflater;
    private View view;
    private LinearLayout playerLevelsContainer;
    private LinearLayout monstersContainer;
    private List<Integer> playerCountList;
    private List<Integer> playerLevelList;
    private List<XpThresholds> xpThresholdsList;
    private int easyThreshold;
    private int mediumThreshold;
    private int hardThreshold;
    private int deadlyThreshold;
    private int dailyXpBudget;
    final int UPDATE_DELAY = 3000;

    private final Handler updateHandler = new Handler();

    private final Runnable updateEncounter = new Runnable() {
        @Override
        public void run() {
            EncounterInterface.updateEncounter(encounter);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;

        view = inflater.inflate(R.layout.encounter_builder_layout, container, false);
        view.setId(View.generateViewId());
        view.setTag("EncounterBuilder");

        playerLevelsContainer = view.findViewById(R.id.encounter_player_levels_container);
        monstersContainer = view.findViewById(R.id.encounter_monsters_container);

        return view;
    }

    public void initViews() {
        //set music list
        musicList = MusicInterface.getMusicList();
        musicNameList = musicList.stream()
                .map(Music::getName)
                .collect(Collectors.toList());

        xpThresholdsList = EncounterInterface.getXpThresholds();

        encounterListView(true);
        builderView();
    }

    /**
     * Sets up the ListView that contains a list of all the encounters on the server
     *
     * @param loadNewEncounter true if the encounter should be set automatically, false if one is set elsewhere
     */
    private void encounterListView(boolean loadNewEncounter) {
        initEncounterList();

        //get default encounter to display
        if (encounterList.size() == 1) {
            //no encounters in campaign yet, make a new default encounter and add it to the list
            Encounter encounter = EncounterInterface.addEncounter("New Encounter");
            encounterList.add(new NameIdProjection(encounter.getEncounterId(), encounter.getName()));
            encounterNameList.add(encounterList.get(1).getName());
        }

        if (loadNewEncounter) {
            int encounterId = encounterList.get(1).getId();
            encounter = EncounterInterface.getEncounter(encounterId);
        }

        ListView encListView = view.findViewById(R.id.encounter_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.simple_list_view, encounterNameList);
        encListView.setAdapter(adapter);
        encListView.setOnItemClickListener((parent, view, position, id) -> {
            //check if add encounter was selected, should be first encounter in list
            if (position == 0) {
                View addView = inflater.inflate(R.layout.rename_add_encounter_dialog, null);
                TextView text = addView.findViewById(R.id.name_textview);
                TextView errorText = addView.findViewById(R.id.add_rename_error_text);
                text.setText("Enter the new encounter's name:");
                EditText newName = addView.findViewById(R.id.name_entry);

                final AlertDialog.Builder renameEncounterDialog = new AlertDialog.Builder(getContext());
                renameEncounterDialog.setView(addView);
                renameEncounterDialog.setTitle("Add Encounter");
                renameEncounterDialog.setPositiveButton("OK", null);
                renameEncounterDialog.setNegativeButton("Cancel", null);

                AlertDialog add = renameEncounterDialog.create();
                add.setOnShowListener(dialogInterface -> {
                    Button ok = add.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(v -> {
                        //create new encounter
                        String name = newName.getText().toString();
                        Encounter newEncounter = EncounterInterface.addEncounter(name);

                        if (newEncounter == null) {
                            errorText.setText("An encounter with the name " + name + " already exists on this campaign");
                            errorText.setVisibility(View.VISIBLE);
                        } else {
                            //push any pending updates before loading the new encounter
                            if (updateHandler.hasCallbacks(null)) {
                                prepareInstantUpdate();
                            }

                            //reload list then display new encounter in builder
                            encounter = newEncounter;
                            encounterListView(false);
                            builderView();
                            add.dismiss();
                        }
                    });
                });

                add.show();
            } else if (encounterList.get(position).getId() == encounter.getEncounterId()) {
                //if the selected encounter is the current one, do nothing and exit
                return;
            } else {
                //push any pending updates before loading the new encounter
                if (updateHandler.hasCallbacks(null)) {
                    prepareInstantUpdate();
                }
                //get new encounter, display in builder
                encounter = EncounterInterface.getEncounter(encounterList.get(position).getId());
                builderView();
            }
        });
    }

    /**
     * Initializes the encounter list including an entry at index 0 for adding new encounters
     */
    private void initEncounterList() {
        //setup add encounter
        NameIdProjection addEncounter = new NameIdProjection(0, "Add Encounter");

        encounterList = new ArrayList<>();
        encounterList.add(addEncounter);
        encounterList.addAll(EncounterInterface.getEncounterList());
        encounterNameList = encounterList.stream()
                .map(NameIdProjection::getName)
                .collect(Collectors.toList());
    }

    /**
     * Initializes the encounter builder view using
     * the first encounter in the list. If there are
     * no encounters in the list, it will be blank
     */
    private void builderView() {
        nameView();
        playerLevelsView();
        difficultyView();
        musicLairActionView();
        monsterListView();
    }

    private void nameView() {
        View nameView = view.findViewById(R.id.encounter_name_buttons_layout);
        TextView name = nameView.findViewById(R.id.name);
        name.setText(encounter.getName());

        Button archive = nameView.findViewById(R.id.archive);
        archive.setOnClickListener(view -> new AlertDialog.Builder(getContext())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Archive Encounter")
                .setMessage("Archive " + encounter.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    //push any pending updates before archiving the encounter
                    if (updateHandler.hasCallbacks(null)) {
                        prepareInstantUpdate();
                    }

                    EncounterInterface.archiveEncounter(encounter.getEncounterId());
                    encounterListView(true);
                    builderView();
                })
                .setNegativeButton("No", null)
                .show()
        );

        Button rename = nameView.findViewById(R.id.rename);
        rename.setOnClickListener(view -> {
            View renameView = inflater.inflate(R.layout.rename_add_encounter_dialog, null);
            TextView text = renameView.findViewById(R.id.name_textview);
            text.setText(MessageFormat.format("Enter a new name for {0}", encounter.getName()));
            EditText newName = renameView.findViewById(R.id.name_entry);

            final AlertDialog.Builder renameEncounterDialog = new AlertDialog.Builder(getContext());
            renameEncounterDialog.setView(renameView);
            renameEncounterDialog.setTitle("Rename " + encounter.getName());
            renameEncounterDialog.setPositiveButton("OK", null);
            renameEncounterDialog.setNegativeButton("Cancel", null);

            AlertDialog renameDialog = renameEncounterDialog.create();
            renameDialog.setOnShowListener(dialogInterface -> {
                Button ok = renameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    encounter.setName(newName.getText().toString());
                    name.setText(encounter.getName());
                    prepareInstantUpdate();
                    encounterListView(false);
                    renameDialog.dismiss();
                });
            });

            renameDialog.show();
        });

        Button load = nameView.findViewById(R.id.load);
        load.setOnClickListener(view -> Util.loadEncounter(encounter));
    }

    private void playerLevelsView() {
        //on first pass the lists will be null, make en entry for 4 players of level 1
        if (playerCountList == null) {
            playerCountList = new ArrayList<>();
            playerCountList.add(4);

            playerLevelList = new ArrayList<>();
            playerLevelList.add(1);
        }

        //unload any previous entries
        if (playerLevelsContainer.getChildCount() > 1) {
            playerLevelsContainer.removeViewsInLayout(1, playerLevelsContainer.getChildCount() - 1);
        }

        for (int i = 0; i < playerCountList.size(); i++) {
            final int index = i;
            View playerLevels = inflater.inflate(R.layout.encounter_player_levels_layout, playerLevelsContainer);
            Spinner players = playerLevels.findViewById(R.id.num_players);
            players.setId(index);
            players.setTag(index);
            players.setSelection(playerCountList.get(index) - 1, false);
            System.out.println("index: " + index);
            players.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerCountList.set(index, position + 1);
                    difficultyView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            Spinner level = playerLevels.findViewById(R.id.level);
            level.setId(index);
            level.setTag(index);
            level.setSelection(playerLevelList.get(index) - 1, false);
            level.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerLevelList.set(index, position + 1);
                    difficultyView();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            Button delete = playerLevels.findViewById(R.id.archive);
            delete.setId(index);
            delete.setTag(index);
            delete.setOnClickListener(view -> {
                playerCountList.remove(index);
                playerLevelList.remove(index);
                playerLevelsView();
                difficultyView();
            });
        }

        inflater.inflate(R.layout.add_player_level_button, playerLevelsContainer);
        Button add = view.findViewById(R.id.add_player_level);
        add.setOnClickListener(v -> {
            playerCountList.add(1);
            playerLevelList.add(1);
            playerLevelsView();
            difficultyView();
        });
    }

    private void difficultyView() {
        calculateXpThresholds();
        int encounterXpTotal = encounter.getXpTotal();

        View difficultyView = view.findViewById(R.id.encounter_difficulty_layout);
        TextView easy = difficultyView.findViewById(R.id.easy);
        easy.setText(MessageFormat.format("Easy: {0}", easyThreshold));
        TextView medium = difficultyView.findViewById(R.id.medium);
        medium.setText(MessageFormat.format("Medium: {0}", mediumThreshold));
        TextView hard = difficultyView.findViewById(R.id.hard);
        hard.setText(MessageFormat.format("Hard: {0}", hardThreshold));
        TextView deadly = difficultyView.findViewById(R.id.deadly);
        deadly.setText(MessageFormat.format("Deadly: {0}", deadlyThreshold));
        TextView budget = difficultyView.findViewById(R.id.budget);
        budget.setText(MessageFormat.format("Daily Budget: {0}", dailyXpBudget));
        TextView total = difficultyView.findViewById(R.id.total);
        total.setText(MessageFormat.format("Total XP: {0}", encounterXpTotal));

        easy.setTypeface(encounterXpTotal >= easyThreshold && encounterXpTotal < mediumThreshold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        medium.setTypeface(encounterXpTotal >= mediumThreshold && encounterXpTotal < hardThreshold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        hard.setTypeface(encounterXpTotal >= hardThreshold && encounterXpTotal < deadlyThreshold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        deadly.setTypeface(encounterXpTotal >= deadlyThreshold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    private void calculateXpThresholds() {
        //reset previous calculations
        easyThreshold = 0;
        mediumThreshold = 0;
        hardThreshold = 0;
        deadlyThreshold = 0;
        dailyXpBudget = 0;

        for (int i = 0; i < playerCountList.size(); i++) {
            int level = playerLevelList.get(i);
            int count = playerCountList.get(i);

            easyThreshold += xpThresholdsList.get(level - 1).getEasy() * count;
            mediumThreshold += xpThresholdsList.get(level - 1).getMedium() * count;
            hardThreshold += xpThresholdsList.get(level - 1).getHard() * count;
            deadlyThreshold += xpThresholdsList.get(level - 1).getDeadly() * count;
            dailyXpBudget += xpThresholdsList.get(level - 1).getBudget() * count;
        }
    }

    private void musicLairActionView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, musicNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        View musicLairView = view.findViewById(R.id.encounter_music_lair_layout);
        Spinner musicSpinner = musicLairView.findViewById(R.id.music);
        musicSpinner.setAdapter(adapter);
        var musicIndex = IntStream.range(0, musicList.size()).filter(x -> musicList.get(x).getId() == encounter.getMusicId()).findFirst().orElse(1);
        musicSpinner.setSelection(musicIndex, false);
        musicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int musicId = musicList.get(position).getId();
                if (encounter.getMusicId() != musicId) {
                    encounter.setMusicId(musicId);
                    prepareDelayedUpdate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        CheckBox lair = musicLairView.findViewById(R.id.lair_action);
        lair.setChecked(encounter.getHasLairAction());
        lair.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                encounter.setHasLairAction(isChecked);
                prepareDelayedUpdate();
            }
        });
    }

    private void monsterListView() {
        if (monstersContainer.getChildCount() > 1) {
            monstersContainer.removeViewsInLayout(1, monstersContainer.getChildCount() - 1);
        }

        List<EncounterMonster> monsterList = encounter.getEncounterMonsters();

        for (int i = 0; i < monsterList.size(); i++) {
            final int index = i;
            View monsterView = inflater.inflate(R.layout.encounter_monster_layout, monstersContainer);
            TextView name = monsterView.findViewById(R.id.name);
            name.setId(index);
            name.setTag(index);
            name.setText(monsterList.get(index).getName());

            EditText quantity = monsterView.findViewById(R.id.quantity);
            quantity.setId(index);
            quantity.setTag(index);
            quantity.setText(String.valueOf(monsterList.get(index).getQuantity()));
            quantity.addTextChangedListener(new TextWatcher() {
                final Handler handler = new Handler();

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable text) {
                    int monQuantity = Integer.parseInt(text.toString());
                    //list could be empty if minus is clicked twice in succession
                    if (encounter.getEncounterMonsters().get(index).getQuantity() != monQuantity && !encounter.getEncounterMonsters().isEmpty()) {
                        if (monQuantity <= 0) {
                            encounter.getEncounterMonsters().remove(index);
                            monsterListView();
                        } else {
                            encounter.getEncounterMonsters().get(index).setQuantity(monQuantity);
                        }

                        prepareDelayedUpdate();
                        difficultyView();
                    }
                }
            });

            Button plus = monsterView.findViewById(R.id.plus_quantity);
            plus.setId(index);
            plus.setTag(index);
            plus.setOnClickListener(view -> quantity.setText(String.valueOf(Integer.parseInt(quantity.getText().toString()) + 1)));

            Button minus = monsterView.findViewById(R.id.minus_quantity);
            minus.setId(index);
            minus.setTag(index);
            minus.setOnClickListener(view -> quantity.setText(String.valueOf(Integer.parseInt(quantity.getText().toString()) - 1)));

            Spinner initiative = monsterView.findViewById(R.id.initiative);
            initiative.setId(index);
            initiative.setTag(index);
            initiative.setSelection(monsterList.get(index).getInitiativeRoll() - 1, false);
            initiative.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (encounter.getEncounterMonsters().get(index).getInitiativeRoll() != position + 1) {
                        encounter.getEncounterMonsters().get(index).setInitiativeRoll(position + 1);
                        prepareDelayedUpdate();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            CheckBox reinforcement = monsterView.findViewById(R.id.reinforcement);
            reinforcement.setId(index);
            reinforcement.setTag(index);
            reinforcement.setChecked(monsterList.get(i).getIsReinforcement());
            reinforcement.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    encounter.getEncounterMonsters().get(index).setIsReinforcement(isChecked);
                    prepareDelayedUpdate();
                }
            });

            CheckBox minion = monsterView.findViewById(R.id.minion);
            minion.setId(index);
            minion.setTag(index);
            minion.setChecked(monsterList.get(i).getIsMinion());
            minion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    encounter.getEncounterMonsters().get(index).setIsMinion(isChecked);
                    prepareDelayedUpdate();
                    difficultyView();
                }
            });

            CheckBox invisible = monsterView.findViewById(R.id.invisible);
            invisible.setId(index);
            invisible.setTag(index);
            invisible.setChecked(monsterList.get(i).getIsInvisible());
            invisible.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    encounter.getEncounterMonsters().get(index).getIsInvisible(isChecked);
                    prepareDelayedUpdate();
                }
            });
        }

        inflater.inflate(R.layout.add_monster_in_encounter_button, monstersContainer);
        Button add = view.findViewById(R.id.add_monster_to_encounter);
        add.setOnClickListener(view -> {
            View addMonster = inflater.inflate(R.layout.add_monster_in_encounter_layout, null);
            AutoCompleteTextView monName = addMonster.findViewById(R.id.monster_text);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, Util.getMonsterNameList());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            monName.setAdapter(adapter);
            monName.setOnClickListener(v -> monName.showDropDown());
            TextView invalidName = addMonster.findViewById(R.id.invalid_name);

            final AlertDialog.Builder addMonDialog = new AlertDialog.Builder(getContext());
            addMonDialog.setTitle("Add Monster to Encounter");
            addMonDialog.setView(addMonster);
            addMonDialog.setNegativeButton("Cancel", null);
            addMonDialog.setPositiveButton("OK", null);

            AlertDialog alert = addMonDialog.create();
            alert.setOnShowListener(dialogInterface -> {
                Button ok = alert.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    String name = monName.getText().toString();

                    if (!Util.getMonsterNameList().contains(name)) {
                        invalidName.setVisibility(View.VISIBLE);
                        return;
                    }

                    int index = Util.getMonsterNameList().indexOf(name);
                    Monster newMonster = MonsterInterface.getMonster(Util.getMonsterList().get(index).getId());
                    encounter.getEncounterMonsters().add(new EncounterMonster(newMonster));
                    prepareInstantUpdate();

                    monsterListView();
                    difficultyView();
                    alert.dismiss();
                });
            });
            alert.show();
        });
    }

    /**
     * Stages a delayed Encounter update on the Update Handler. If there are any updates
     * already stages, they are first removed, effectively resetting the timer so that
     * only one update happens.
     */
    private void prepareDelayedUpdate() {
        updateHandler.removeCallbacksAndMessages(null);
        updateHandler.postDelayed(updateEncounter, UPDATE_DELAY);
    }

    /**
     * Immediately executes an update on the Encounter via the Update Handler. If there are
     * any updates, such as one set by prepareDelayedUpdate, they are removed so that only
     * one update occurs.
     */
    private void prepareInstantUpdate() {
        updateHandler.removeCallbacksAndMessages(null);
        updateHandler.post(updateEncounter);
    }
}
