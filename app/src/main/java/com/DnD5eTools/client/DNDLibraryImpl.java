package com.DnD5eTools.client;

import com.DnD5eTools.monster.Encounter;
import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.player.PlayerCharacter;

import java.io.*;
import java.util.*;
//import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

/**
 * Implementation of Library interface.
 */
public class DNDLibraryImpl implements DNDLibrary {
	private final Hashtable<String, Monster> monLib;
	private final Hashtable<String, Encounter> encLib;
	private final Hashtable<String, PlayerCharacter> plrLib;
	
	public DNDLibraryImpl() {
		monLib = new Hashtable<String, Monster>();
		encLib = new Hashtable<String, Encounter>();
		plrLib = new Hashtable<String, PlayerCharacter>();
		initMonLib();
		initEncLib();
		initPlrLib();
	}
	
	private void initMonLib() {
		File file = new File("Monsters/");
		String[] fileNames = file.list();
		String name;
		
		for (int i = 0; i < fileNames.length; i++) {
			name = fileNames[i].substring(0, Math.min(fileNames[i].length(), fileNames[i].length() - 5));
			monLib.put(name, new Monster(name));
		}
		
	}
	
	private void initEncLib() {
		File file = new File("Encounters/");
		String[] fileNames = file.list();
		String name;
		
		for (int i = 0; i < fileNames.length; i++) {
			name = fileNames[i].substring(0, Math.min(fileNames[i].length(), fileNames[i].length() - 5));
			encLib.put(name, new Encounter(name));
		}
	}
	
	private void initPlrLib() {
		/*File file = new File("PCs/");
		String[] fileNames = file.list();
		String name;
		
		for (int i = 0; i < fileNames.length; i++) {
			name = fileNames[i].substring(0, Math.min(fileNames[i].length(), fileNames[i].length() - 5));
			plrLib.put(name, new PlayerCharacter(name));
		}*/
		
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("PCs/PlayerCharacters.json");

			if (in == null)
            		in = new FileInputStream(new File("PCs/PlayerCharacters.json"));

			//convert to string
			Scanner sc = new Scanner(in);
			StringBuffer sb = new StringBuffer();
			while(sc.hasNext()){
				sb.append(sc.nextLine());
			}
            		
			JSONArray arr = new JSONArray(new JSONTokener(sb.toString()));
			int length = arr.length();
			
			for (int i = 0; i < length; i++) {
				JSONObject obj = arr.getJSONObject(i);
				plrLib.put(obj.getString("name"), new PlayerCharacter(obj));
			}
				
		} catch (Exception e) {
			System.out.println("Error in DNDLibrary.initPlrLib: " + e.getMessage());
		}
	}
	
	public Monster getMonster(String name) {
		return monLib.get(name);
	}
	
	public Encounter getEncounter(String name) {
		return encLib.get(name);
	}
	
	public PlayerCharacter getPlayerCharacter(String name) {
		return plrLib.get(name);
	}
	
	public boolean addMonster(String name) {
		if (monLib.containsKey(name)) {
			return false;
		}
		
		Monster monster = new Monster();//(name)
		monster.setName(name);//get rid of
		monster.saveJson();
		monLib.put(name, new Monster(name)); //(name, monster)
		
		return true;
	}
	
	public boolean addEncounter(String name) {
		if (encLib.containsKey(name)) {
			return false;
		}
		
		Encounter encounter = new Encounter();
		encounter.setName(name);
		encounter.saveJson();
		encLib.put(name, new Encounter(name));
		
		return true;
	}
	
	public boolean addPlayerCharacter(String name) {
		if (plrLib.containsKey(name)) {
			return false;
		}
		
		plrLib.put(name, new PlayerCharacter(name));
		savePlayerCharacters();
		
		return true;
	}
	
	public boolean deleteMonster(String name) {
		boolean result = false;
		
		System.out.println("Deleting " + name +  ".json...");
		
		if (monLib.containsKey(name)) {
			monLib.remove(name);
			File file = new File("Monsters/" + name + ".json");
			result = file.delete();
		}
		
		if (result)
			System.out.println("Successfully deleted " + name + ".json");
				
		else
			System.out.println("Failed to delete " + name + ".json");
		
		return result;
	}
	
	public boolean deleteEncounter(String name) {
		boolean result = false;
		
		System.out.println("Deleting " + name +  ".json...");
		
		if (encLib.containsKey(name)) {
			encLib.remove(name);
			File file = new File("Encounters/" + name + ".json");
			result = file.delete();
		}
		
		if (result)
			System.out.println("Successfully deleted " + name + ".json");
				
		else
			System.out.println("Failed to delete " + name + ".json");
		
		return result;
	}
	
	public boolean deletePlayerCharacter(String name) {
		boolean result = false;
		
		System.out.println("Deleting " + name +  "...");
		
		if (plrLib.containsKey(name)) {
			plrLib.remove(name);
			result = true;
		}
		
		if (result) {
			System.out.println("Successfully deleted " + name);
			savePlayerCharacters();
		}
				
		else
			System.out.println("Failed to delete " + name);
		
		return result;
	}
	
	public boolean renameMonster(String oldName, Monster monster) {
		boolean result = false;

		if (monLib.contains(monster.getName()))
			return result;
			
		if (monLib.put(monster.getName(), monster) == null) {
			result = deleteMonster(oldName);
			saveMonster(monster.getName());
			System.out.println("Added " + monster.getName() + " to Monster library.");
		}

		else {
			System.out.println("Failed to add " + monster.getName() + " to Monster Library.");
			System.out.println(monster.getName() + " already exists in library.");
		}
			
		return result;
	}
	
	public boolean renameEncounter(String oldName, Encounter encounter) {
		boolean result = deleteEncounter(oldName);
		
		if (result && encLib.put(encounter.getName(), encounter) == null) {
			result = true;
			saveMonster(encounter.getName());
			System.out.println("Added " + encounter.getName() + " to Encounter library.");
		}

		else {
			System.out.println("Failed to add " + encounter.getName() + " to Encounter Library.");
			System.out.println(encounter.getName() + " already exists in library.");
		}
			
		return result;
	}
	
	public boolean updateMonster(Monster monster) {
		boolean result = false;
		String name = monster.getName();
		
		if (monLib.containsKey(name)) {
			monLib.replace(name, monster);
			result = true;
		}
			
		if (result)
			System.out.println("Successfully updated " + name);
				
		else
			System.out.println("Failed to update " + name);
		
		return result;
	}
	
	public boolean updateEncounter(Encounter encounter) {
		boolean result = false;
		String name = encounter.getName();
		
		if (encLib.containsKey(name)) {
			encLib.replace(name, encounter);
			result = true;
		}
			
		if (result)
			System.out.println("Successfully updated " + name);
				
		else
			System.out.println("Failed to update " + name);
		
		return result;
	}
	
	public boolean updatePlayerCharacter(PlayerCharacter character) {
		boolean result = false;
		String name = character.getName();
		
		if (plrLib.containsKey(name)) {
			plrLib.replace(name, character);
			result = true;
		}
			
		if (result) {
			System.out.println("Successfully updated " + name);
			savePlayerCharacters();
		}
				
		else
			System.out.println("Failed to update " + name);
		
		return result;
	}
	
	//TODO: return a monster object? possibly makes it easier to reload info?
	public boolean restoreMonster(String name) {
		boolean result = false;
		
		if (monLib.containsKey(name)) {
			monLib.replace(name, new Monster(name));
			result = true;
		}
		
		if (result)
			System.out.println("Successfully restored " + name);
				
		else
			System.out.println("Failed to restore " + name);
		
		return result;
	}
	
	public boolean restoreEncounter(String name) {
		boolean result = false;
		
		if (encLib.containsKey(name)) {
			encLib.replace(name, new Encounter(name));
			result = true;
		}
		
		if (result)
			System.out.println("Successfully restored " + name);
				
		else
			System.out.println("Failed to restore " + name);
		
		return result;
	}
	
	public ArrayList<String> getMonsterList() {
		return new ArrayList<String>(monLib.keySet());
	}
	
	public ArrayList<String> getEncounterList() {
		return new ArrayList<String>(encLib.keySet());
	}
	
	public ArrayList<String> getPlayerCharacterList() {
		return new ArrayList<String>(plrLib.keySet());
	}
	
	public boolean saveMonster(String name) {
		return monLib.get(name).saveJson();
	}
	
	public boolean saveEncounter(String name) {
		return encLib.get(name).saveJson();
	}
	
	private String playerCharactersToJsonString() {
		String result = "[]";
		
		try {
			result = playerCharactersToJson().toString(4);
		} catch (Exception e) {
			System.out.println("Error in DNDLibrary.playerCharactersToJsonString: " + e.getMessage());
		}
		
		return result;
	}
	
	private JSONArray playerCharactersToJson() {
		JSONArray arr = new JSONArray();
		
		try {
			Set<String> keys = plrLib.keySet();
			Iterator<String> itr = keys.iterator();
			
			while (itr.hasNext()) {
				arr.put(plrLib.get(itr.next()).toJson());
			}
		} catch (Exception e) {
			System.out.println("Exception in DNDLibrary.playerCharactersToJson: " + e.getMessage());
		}
		
		return arr;
	}
	
	//TODO: potentially private method, if change to private do not forget to remove from DNDlibrary.java and DNDClientProxy.java
	public boolean savePlayerCharacters() {
		boolean result = false;
		
		try {
			File file = new File("PCs/PlayerCharacters.json");
			
			if (!file.exists())
				file.createNewFile();
				
			FileWriter out = new FileWriter(file);
			
			out.write(playerCharactersToJsonString());
			out.flush();
			out.close();
			result = true;
			
			System.out.println("PlayerCharacters save successful.");
		} catch (Exception e) {
			System.out.println("Error in DNDLibrary.savePlayerCharacters: " + e.getMessage());
		}
		
		return result;
	}

	public boolean saveAll() {
		Set<String> keys = monLib.keySet();
		Iterator<String> itr = keys.iterator();
		
		while (itr.hasNext())
			if (!saveMonster(itr.next()))
				return false;
				
		keys = encLib.keySet();
		itr = keys.iterator();
		
		while (itr.hasNext())
			if (!saveEncounter(itr.next()))
				return false;
		
		return true;
	}
}
