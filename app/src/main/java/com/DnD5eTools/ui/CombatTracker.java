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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DnD5eTools.R;
import com.DnD5eTools.entities.Music;
import com.DnD5eTools.entities.PlayerCharacter;
import com.DnD5eTools.entities.encounter.EncounterMonster;
import com.DnD5eTools.entities.monster.Monster;
import com.DnD5eTools.interfaces.CombatInterface;
import com.DnD5eTools.interfaces.EncounterInterface;
import com.DnD5eTools.interfaces.MonsterInterface;
import com.DnD5eTools.interfaces.MusicInterface;
import com.DnD5eTools.interfaces.PlayerInterface;
import com.DnD5eTools.models.combatants.Combatant;
import com.DnD5eTools.models.combatants.LairActionCombatant;
import com.DnD5eTools.models.combatants.MonsterCombatant;
import com.DnD5eTools.models.combatants.PlayerCombatant;
import com.DnD5eTools.models.projections.NameIdProjection;
import com.DnD5eTools.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Tab for tracking player data, combat, and housing the soundboard.
 *
 * When first loaded, the user will see a list of all Player Characters
 * on the left of the screen, and the sound board on the right of the
 * screen. When an encounter is loaded, the Player list wil be replaced
 * with a list of all combatants currently involved in the encounter
 * sorted by initiative order. Ending the combat will return the user
 * to the list of players.
 */
public class CombatTracker extends Fragment {
    private List<PlayerCharacter> playerList;
    private List<Combatant> combatantList = new ArrayList<>();
    private List<Music> musicList;
    private View view;
    private LayoutInflater inflater;
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

        view = inflater.inflate(R.layout.combat_tracker_layout, container, false);
        view.setTag("CombatTracker");
        view.setId(View.generateViewId());

        leftView = view.findViewById(R.id.combat_left_side);

        return view;
    }

    public void initViews() {
        soundBoard();
        preCombatView();
    }

    /**
     * Sets up the pre combat view (PC names, ac, bonus, delete button)
     */
    private void preCombatView() {
        playerList = PlayerInterface.getPlayerList();

        View labels = inflater.inflate(R.layout.pre_combat_labels, leftView);

        //loop and add layouts
        for (int i = 0; i < playerList.size(); i++) {
            final int index = i;

            View playerView = inflater.inflate(R.layout.player_layout, leftView);

            TextView name = playerView.findViewById(R.id.player_name);
            name.setId(index);
            name.setText(playerList.get(index).getName());
            name.setTag(index);

            CheckBox isCombatant = playerView.findViewById(R.id.combatant_checkbox);
            isCombatant.setId(index);
            isCombatant.setChecked(false);
            isCombatant.setTag(index);

            isCombatant.setOnCheckedChangeListener((buttonView, isChecked) -> {
                playerList.get(index).setCombatant(isChecked);
                PlayerInterface.updatePlayer(playerList.get(index));
            });

            //index of ac = ac - 1
            Spinner ac = playerView.findViewById(R.id.ac_spinner);
            ac.setId(index);
            ac.setSelection(playerList.get(index).getAc() - 1, false);
            ac.setTag(index);

            ac.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerList.get(index).setAc(Integer.parseInt(ac.getSelectedItem().toString()));
                    PlayerInterface.updatePlayer(playerList.get(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            //index of bonus = bonus + 5
            Spinner bonus = playerView.findViewById(R.id.bonus_spinner);
            bonus.setId(index);
            bonus.setSelection(playerList.get(index).getInitiativeBonus() + 5, false);
            bonus.setTag(index);

            bonus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerList.get(index).setInitiativeBonus(Integer.parseInt(bonus.getSelectedItem().toString()));
                    PlayerInterface.updatePlayer(playerList.get(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            //index of initiative = initiative - 1
            Spinner initiative = playerView.findViewById(R.id.init_roll_spinner);
            initiative.setId(index);
            initiative.setSelection(playerList.get(index).getRolledInitiative() - 1, false);
            initiative.setTag(index);

            initiative.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerList.get(index).setRolledInitiative(position + 1);
                    PlayerInterface.updatePlayer(playerList.get(index));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            Button kill = playerView.findViewById((R.id.kill_player));
            kill.setId(index);
            kill.setOnClickListener(view -> new AlertDialog.Builder(getContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Kill Player")
                    .setMessage("Kill " + playerList.get(index).getName() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        PlayerInterface.killPlayerCharacter(playerList.get(index).getId());
                        leftView.removeAllViewsInLayout();
                        preCombatView();
                    })
                    .setNegativeButton("No", null)
                    .show()
            );
        }

        View buttonLayout = inflater.inflate(R.layout.pre_combat_buttons, leftView);

        Button addPlayer = buttonLayout.findViewById((R.id.add_player));
        addPlayer.setOnClickListener(view -> {
            View addView = inflater.inflate(R.layout.add_player_dialog, null);
            EditText nameEntry = addView.findViewById((R.id.name_entry));

            final AlertDialog.Builder addPlayerDialog = new AlertDialog.Builder(getContext());
            addPlayerDialog.setView(addView);
            addPlayerDialog.setTitle("Add New PC");
            addPlayerDialog.setPositiveButton("OK", null);
            addPlayerDialog.setNegativeButton("Cancel", null);

            AlertDialog add = addPlayerDialog.create();
            add.setOnShowListener(dialogInterface -> {
                Button ok = add.getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    String entry = nameEntry.getText().toString();

                    if (!entry.trim().isEmpty()) {
                        PlayerInterface.addPlayerCharacter(entry);
                        add.dismiss();
                        leftView.removeAllViewsInLayout();
                        preCombatView();
                    }
                });
            });

            add.show();
        });

        Button loadButton = buttonLayout.findViewById((R.id.load_encounter));
        loadButton.setOnClickListener(view -> {
            List<NameIdProjection> encounterList = EncounterInterface.getEncounterList();
            List<String> encounterNameList = encounterList.stream()
                    .map(NameIdProjection::getName)
                    .collect(Collectors.toList());

            //Load encounter list into a drop down
            View loadView = inflater.inflate(R.layout.choose_encounter_dialog, null);
            AutoCompleteTextView encName = loadView.findViewById(R.id.encounter_text);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, encounterNameList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            encName.setAdapter(adapter);
            encName.setOnClickListener(v -> encName.showDropDown());

            //load drop down into dialog box
            final AlertDialog.Builder loadDialog = new AlertDialog.Builder(getContext());
            loadDialog.setTitle("Select an Encounter");
            loadDialog.setView(loadView);
            loadDialog.setPositiveButton("OK", (dialog, which) -> {
                int encounterId =
                        encounterList.get(encounterNameList.indexOf(encName.getText().toString())).getId();
                Util.loadEncounter(EncounterInterface.getEncounter(encounterId));
            });
            loadDialog.setNegativeButton("Cancel", null);

            AlertDialog load = loadDialog.create();
            load.show();
        });

        Button beginButton = buttonLayout.findViewById((R.id.begin_encounter));
        beginButton.setOnClickListener(view -> {
            if (!Util.isEncounterLoaded()) {
                new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Not Loaded")
                        .setMessage("No Encounter Loaded")
                        .setPositiveButton("Close", null)
                        .show();
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("Start Encounter - " + Util.getLoadedEncounter().getName())
                        .setMessage("Are the player combatants selected?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            initializeCombatantList();
                            combatView();
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    /**
     * Set up the initial list of combatants
     */
    private void initializeCombatantList() {
        combatantList.clear();

        //check for players marked as combatants
        for (PlayerCharacter pc : playerList) {
            if(pc.isCombatant()) {
                combatantList.add(new PlayerCombatant(pc));
            }
        }

        if (Util.getLoadedEncounter().isLairAction()) {
            combatantList.add(new LairActionCombatant());
        }

        List<EncounterMonster> encounterMonsterList = Util.getLoadedEncounter().getMonsterList();

        for(EncounterMonster monster : encounterMonsterList) {
            combatantList.add(new MonsterCombatant(monster));
        }

        sortInitialCombatantList();
    }

    /**
     * Expands any groups that contain more than one monster into separate combatants, if that group is not a
     * reinforcement and if it has not already been expanded
     */
    private void expandGroups() {
        for (int i = 0; i < combatantList.size(); i++) {
            if (combatantList.get(i).getQuantity() > 1 && !combatantList.get(i).isReinforcement() && !combatantList.get(i).isExpanded()) {
                combatantList.get(i).setExpanded(true);
                for (int j = 0; j < combatantList.get(i).getQuantity() - 1; j++) {
                    combatantList.add(i, new MonsterCombatant((MonsterCombatant) combatantList.get(i)));
                }

                //skip past added monsters
//                i += combatantList.get(i).getQuantity();
            }
        }
    }

    /**
     * Weighs combatants then sorts them by initiative. Absolute ties are broken via user input
     * Should only called once at the beginning of combat and before reinforcements are expanded
     */
    private void sortInitialCombatantList() {
        for (int leftCombatant = 0; leftCombatant < combatantList.size() - 1; leftCombatant++) {
            for (int rightCombatant = leftCombatant + 1; rightCombatant < combatantList.size(); rightCombatant++) {
                boolean tied = combatantList.get(leftCombatant).weighAndGetTied(combatantList.get(rightCombatant));

                //if tied, run tie breaker alert
                if (tied) {
                    resolveTie(combatantList.get(leftCombatant), combatantList.get(rightCombatant));
                }
            }
        }

        Collections.sort(combatantList);
        expandGroups();
    }

    private void sortNewReinforcement() {
        //new reinforcement is at end of list by default, weigh all others against it
        int newCombatant = combatantList.size() - 1;

        for (int i = 0; i < newCombatant; i++) {
            boolean tied = combatantList.get(i).weighAndGetTied(combatantList.get(newCombatant));

            if (tied) {
                resolveTie(combatantList.get(i), combatantList.get(newCombatant));
            }

            //combatant i should already be expanded, so if quantity > 1 then must update all of the
            //expanded monsters tie breaker values and weights
            for (int j = 1; j < combatantList.get(i).getQuantity(); j++) {
                combatantList.get(i + j).setTieBreaker(combatantList.get(i).getTieBreaker());
                combatantList.get(i + j).setWeight(combatantList.get(i).getWeight());
            }

            //after updating them, skip past the expanded monsters
            i += combatantList.get(i).getQuantity() - 1;
        }

        Collections.sort(combatantList);
    }

    /**
     * Resolves a tie between two combatants
     * @param leftCombatant The combatant to display on the left side of the dialog
     * @param rightCombatant The combatant to display on the right side of the dialog
     */
    private void resolveTie(Combatant leftCombatant, Combatant rightCombatant) {
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                throw new RuntimeException();
            }
        };

        View tieView = inflater.inflate(R.layout.tie_breaker_layout, null);
        TextView leftName = tieView.findViewById(R.id.left_name);
        leftName.setText(leftCombatant.getName());
        TextView rightName = tieView.findViewById(R.id.right_name);
        rightName.setText(rightCombatant.getName());
        Spinner leftSpinner = tieView.findViewById(R.id.left_spinner);
        leftSpinner.setSelection(leftCombatant.getTieBreaker());
        Spinner rightSpinner = tieView.findViewById(R.id.right_spinner);
        rightSpinner.setSelection(rightCombatant.getTieBreaker());

        final AlertDialog.Builder tieBreaker = new AlertDialog.Builder(getContext());
        tieBreaker.setTitle("Tie Breaker")
                .setView(tieView)
                .setCancelable(false)
                .setPositiveButton("OK", null);

        AlertDialog alert = tieBreaker.create();
        alert.setOnShowListener(dialog -> {
            Button button = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (!leftSpinner.getSelectedItem().equals(rightSpinner.getSelectedItem()) &&
                        !leftSpinner.getSelectedItem().equals("0") && !rightSpinner.getSelectedItem().equals("0")) {
                    leftCombatant.setTieBreaker(Integer.parseInt((String) leftSpinner.getSelectedItem()));
                    rightCombatant.setTieBreaker(Integer.parseInt((String) rightSpinner.getSelectedItem()));
                    leftCombatant.weighAndGetTied(rightCombatant);
                    handler.sendMessage(handler.obtainMessage());
                    alert.dismiss();
                }
            });
        });

        alert.show();

        try {
            Looper.loop();
        } catch(RuntimeException e) { }
    }

    /**
     * Thew view for the combat
     */
    private void combatView() {
        CombatInterface.updateCombatantList(combatantList);
        leftView.removeAllViewsInLayout();

        //used for ids and tags of view elements
        int tag_counter = 1;

        for (Combatant combatant : combatantList) {
            //do not display reinforcements or removed combatants
            if (combatant.isReinforcement() || combatant.isRemoved()) {
                continue;
            }

//            final int index = 1;
            int tag = tag_counter * 10;// + index;

            View combatantView = inflater.inflate(R.layout.combatant_layout, leftView);

            TextView initiative = combatantView.findViewById(R.id.initiative);
            initiative.setId(tag);
            initiative.setTag(tag);
            tag++;

            TextView name = combatantView.findViewById(R.id.name);
            name.setId(tag);
            name.setTag(tag);
            tag++;

            //todo: was it ever necessary to set tags on labels? is it for anything else now that things have been
            // updated?
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

            initiative.setText(Integer.toString(combatant.getInitiative()));
            name.setText(combatant.getName());
            ac_text.setText(Integer.toString(combatant.getAc()));
            ac_text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    combatant.setAc(Integer.parseInt(ac_text.getText().toString()));
                }

                @Override
                public void afterTextChanged(Editable s) {
                    combatant.setAc(Integer.parseInt(ac_text.getText().toString()));
                }
            });

            if (combatant.isLairAction()) {
                ac_label.setVisibility(View.INVISIBLE);
                ac_text.setVisibility(View.INVISIBLE);
                hp_label.setVisibility(View.INVISIBLE);
                hp_text.setVisibility(View.INVISIBLE);
                kill.setVisibility(View.INVISIBLE);
                name.setTextSize(22);
                name.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            } else if (!combatant.isMonster()) {
                hp_label.setVisibility(View.INVISIBLE);
                hp_text.setVisibility(View.INVISIBLE);
                kill.setVisibility(View.INVISIBLE);
            } else {
                remove.setVisibility(View.VISIBLE);
                hp_text.setText(Integer.toString(combatant.getHitPoints()));
                hp_text.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        combatant.setHitPoints(Integer.parseInt(hp_text.getText().toString()));
                    }
                });

                //set initial text of kill button
                if (combatant.isInvisible()) {
                    kill.setText("Visible");
                } else if (combatant.isAlive()) {
                    kill.setText("Kill");
                } else {
                    kill.setText("Revive");
                }

                name.setEnabled(combatant.isAlive());
                ac_text.setEnabled(combatant.isAlive());
                hp_text.setEnabled(combatant.isAlive());

                kill.setOnClickListener(view -> {
                    if (combatant.isInvisible()) {
                        combatant.setInvisible(false);
                        kill.setText("Kill");
                    } else if (combatant.isAlive()) {
                        combatant.kill();
                        kill.setText("Revive");

                        playSound(MONSTER_KILL, "OTHER");
                    } else {
                        combatant.revive();
                        kill.setText("Kill");
                    }

                    name.setEnabled(combatant.isAlive());
                    ac_text.setEnabled(combatant.isAlive());
                    hp_text.setEnabled(combatant.isAlive());

                    //update server combat screen
                    CombatInterface.updateCombatantList(combatantList);
                });

                remove.setOnClickListener(view -> {
                    combatant.setRemoved(true);
                    combatView();
                });
            }

            tag_counter++;
        }

        View combatButtons = inflater.inflate(R.layout.combat_buttons, leftView);
        Button reinforcementButton = combatButtons.findViewById(R.id.reinforcements_button);
        reinforcementButton.setOnClickListener(view -> {
            View reinLayout = inflater.inflate(R.layout.main_reinforcement_layout, null);

            for (Combatant combatant : combatantList) {
                if (combatant.isReinforcement()) {
                    View innerLayout = inflater.inflate(R.layout.reinforcement_layout, (ViewGroup) reinLayout);

                    TextView initiative = innerLayout.findViewById(R.id.initiative);
                    initiative.setText(Integer.toString(combatant.getInitiative()));
                    initiative.setId(View.generateViewId());

                    TextView monName = innerLayout.findViewById(R.id.reinforcement_name);
                    monName.setText(MessageFormat.format("{0} x{1}", combatant.getName(), combatant.getQuantity()));
                    monName.setId(View.generateViewId());

                    CheckBox check = innerLayout.findViewById(R.id.reinforcement_checkbox);
                    check.setId(View.generateViewId());
                    check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        //set to opposite of checked state (checked == true, but that would mean isReinforcement() == true)
                        combatant.setReinforcement(!isChecked);
                    });
                }
            }

            final AlertDialog.Builder reinforcementDialog = new AlertDialog.Builder(getContext());
            reinforcementDialog.setTitle("Add Reinforcements");
            reinforcementDialog.setView(reinLayout);
            reinforcementDialog.setCancelable(false);
            reinforcementDialog.setPositiveButton("Done", (dialog, which) -> {
                expandGroups();
                combatView();
            });

            AlertDialog alert = reinforcementDialog.create();
            alert.show();
        });

        Button newRein = combatButtons.findViewById(R.id.new_reinforcements_button);
        newRein.setOnClickListener(view -> {
            View outsideReinforcementLayout = inflater.inflate(R.layout.outside_reinforcement_layout, null);
            AutoCompleteTextView monName = outsideReinforcementLayout.findViewById(R.id.monster_text);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, Util.getMonsterNameList());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            monName.setAdapter(adapter);
            monName.setOnClickListener(v -> monName.showDropDown());
            TextView invalidName = outsideReinforcementLayout.findViewById(R.id.invalid_name);

            Spinner quantity = outsideReinforcementLayout.findViewById(R.id.quantity_spinner);
            Spinner initiative = outsideReinforcementLayout.findViewById(R.id.init_roll_spinner);

            final AlertDialog.Builder outsideReinforcementsDialog = new AlertDialog.Builder(getContext());
            outsideReinforcementsDialog.setTitle("Add Reinforcements");
            outsideReinforcementsDialog.setView(outsideReinforcementLayout);
            outsideReinforcementsDialog.setNegativeButton("Cancel", null);
            outsideReinforcementsDialog.setPositiveButton("OK", null);

            AlertDialog alert = outsideReinforcementsDialog.create();
            alert.setOnShowListener(dialogInterface -> {
                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setOnClickListener(v -> {
                    String name = monName.getText().toString();
                    if (!Util.getMonsterNameList().contains(name)) {
                        invalidName.setVisibility(View.VISIBLE);
                        return;
                    }

                    int index = Util.getMonsterNameList().indexOf(name);
                    Monster monster = MonsterInterface.getMonster(Util.getMonsterList().get(index).getId());
                    EncounterMonster encounterMonster = new EncounterMonster(monster,
                            quantity.getSelectedItemPosition() + 1, initiative.getSelectedItemPosition() + 1);
                    Combatant newCombatant = new MonsterCombatant(encounterMonster);
                    combatantList.add(newCombatant);

                    alert.dismiss();
                    sortNewReinforcement();
                    expandGroups();
                    combatView();
                });
            });
            alert.show();
        });

        //End the encounter and stop the music
        Button finishEndMusic = combatButtons.findViewById(R.id.finish_button_end_music);
        finishEndMusic.setOnClickListener(view -> {
            final AlertDialog.Builder finishEnc = new AlertDialog.Builder(getContext());
            finishEnc.setTitle("End the Encounter?");
            finishEnc.setPositiveButton("Finish", (dialog, which) -> {
                MusicInterface.stopMusic();
                CombatInterface.endCombat();
                leftView.removeAllViewsInLayout();
                preCombatView();
            });

            finishEnc.setNegativeButton("Cancel", null);

            AlertDialog alert = finishEnc.create();
            alert.show();
        });

        //end the encounter but don't stop the music
        Button finishPlayMusic = combatButtons.findViewById(R.id.finish_button_music);
        finishPlayMusic.setOnClickListener(view -> {
            final AlertDialog.Builder finishEnc = new AlertDialog.Builder(getContext());
            finishEnc.setTitle("End the Encounter?");
            finishEnc.setPositiveButton("Finish", (dialog, which) -> {
                CombatInterface.endCombat();

                leftView.removeAllViewsInLayout();
                preCombatView();
            });

            finishEnc.setNegativeButton("Cancel", null);

            AlertDialog alert = finishEnc.create();
            alert.show();
        });
    }

    private void soundBoard() {
        GridLayout clipGrid = view.findViewById(R.id.sound_clip_gird);

        Button enemyCrit = clipGrid.findViewById(R.id.enemy_crit);
        enemyCrit.setOnClickListener(view -> playSound(MONSTER_CRIT, "OTHER"));

        Button playerCrit = clipGrid.findViewById(R.id.player_crit);
        playerCrit.setOnClickListener(view -> playSound(PLAYER_CRIT, "OTHER"));

        Button enemyDeath = clipGrid.findViewById(R.id.enemy_death);
        enemyDeath.setOnClickListener(view -> playSound(MONSTER_KILL, "OTHER"));

        Button playerDeath = clipGrid.findViewById(R.id.player_death);
        playerDeath.setOnClickListener(view -> playSound(PLAYER_KILL, "OTHER"));

        Button trap = clipGrid.findViewById(R.id.trap);
        trap.setOnClickListener(view -> playSound(TRAP, "OTHER"));

        Button fear = clipGrid.findViewById(R.id.fear);
        fear.setOnClickListener(view -> playSound(FEAR, "OTHER"));

        Button irrational = clipGrid.findViewById(R.id.irrational);
        irrational.setOnClickListener(view -> playSound(IRRATIONAL, AFFLICTION));

        Button powerful = clipGrid.findViewById(R.id.powerful);
        powerful.setOnClickListener(view -> playSound(POWERFUL, VIRTUE));

        Button paranoid = clipGrid.findViewById(R.id.paranoid);
        paranoid.setOnClickListener(view -> playSound(PARANOID, AFFLICTION));

        Button courageous = clipGrid.findViewById(R.id.courageous);
        courageous.setOnClickListener(view -> playSound(COURAGEOUS, VIRTUE));

        Button selfish = clipGrid.findViewById(R.id.selfish);
        selfish.setOnClickListener(view -> playSound(SELFISH, AFFLICTION));

        Button stalwart = clipGrid.findViewById(R.id.stalwart);
        stalwart.setOnClickListener(view -> playSound(STALWART, VIRTUE));

        Button abusive = clipGrid.findViewById(R.id.abusive);
        abusive.setOnClickListener(view -> playSound(ABUSIVE, AFFLICTION));

        Button vigorous = clipGrid.findViewById(R.id.vigorous);
        vigorous.setOnClickListener(view -> playSound(VIGOROUS, VIRTUE));

        Button fearful = clipGrid.findViewById(R.id.fearful);
        fearful.setOnClickListener(view -> playSound(FEARFUL, AFFLICTION));

        Button focused = clipGrid.findViewById(R.id.focused);
        focused.setOnClickListener(view -> playSound(FOCUSED, VIRTUE));

        Button hopeless = clipGrid.findViewById(R.id.hopeless);
        hopeless.setOnClickListener(view -> playSound(HOPELESS, AFFLICTION));

        Button masochistic = clipGrid.findViewById(R.id.masochistic);
        masochistic.setOnClickListener(view -> playSound(MASOCHISTIC, AFFLICTION));

        musicList = MusicInterface.getMusicList();
        List<String> musicNameList = musicList.stream()
                .map(Music::getName)
                .collect(Collectors.toList());

        LinearLayout musicPlayer = view.findViewById(R.id.music_player);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                musicNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner title = musicPlayer.findViewById(R.id.music_title);
        title.setAdapter(adapter);
        title.setSelection(0,false);

        Button pause = musicPlayer.findViewById(R.id.pause);
        pause.setOnClickListener(view -> {
            MusicInterface.pauseMusic();
        });

        Button play = musicPlayer.findViewById(R.id.play);
        play.setOnClickListener(view -> {
            MusicInterface.playMusic(musicList.get(title.getSelectedItemPosition()).getId());
        });

        Button stop = musicPlayer.findViewById(R.id.stop);
        stop.setOnClickListener(view -> {
            MusicInterface.stopMusic();
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