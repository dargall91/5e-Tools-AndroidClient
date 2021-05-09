package com.DnD5eTools.client;

import com.DnD5eTools.monster.Encounter;
import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.player.PlayerCharacter;

import org.json.JSONException;

import java.util.ArrayList;

/**
 * Library interface.
 */
public interface DNDLibrary {
	Monster getMonster(String name) throws JSONException;
	Encounter getEncounter(String gname) throws JSONException;
	PlayerCharacter getPlayerCharacter(String name) throws JSONException;
	boolean addMonster(String name) throws JSONException;
	boolean addEncounter(String name) throws JSONException;
	boolean addPlayerCharacter(String name) throws JSONException;
	boolean deleteMonster(String name) throws JSONException;
	boolean deleteEncounter(String name) throws JSONException;
	boolean deletePlayerCharacter(String name) throws JSONException;
	boolean renameMonster(String oldName, Monster monster) throws JSONException;
	boolean renameEncounter(String oldName, Encounter encounter) throws JSONException;
	boolean updateMonster(Monster monster) throws JSONException;
	boolean updateEncounter(Encounter encounter) throws JSONException;
	boolean updatePlayerCharacter(PlayerCharacter character) throws JSONException;
	boolean restoreMonster(String name) throws JSONException;
	boolean restoreEncounter(String name) throws JSONException;
	ArrayList<String> getMonsterList() throws JSONException;
	ArrayList<String> getEncounterList() throws JSONException;
	ArrayList<String> getPlayerCharacterList() throws JSONException;
	boolean saveMonster(String name) throws JSONException;
	boolean saveEncounter(String name) throws JSONException;
	boolean savePlayerCharacters() throws JSONException;
	boolean saveAll() throws JSONException;
}
