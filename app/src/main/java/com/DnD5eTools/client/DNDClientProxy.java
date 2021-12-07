package com.DnD5eTools.client;

import com.DnD5eTools.monster.Encounter;
import com.DnD5eTools.monster.Monster;
import com.DnD5eTools.player.PlayerCharacter;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Proxy for server communication.
 */
public class DNDClientProxy implements DNDLibrary {

    private static final int buffSize = 1024;
    private String host;
    private int port;

    public DNDClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void changeConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    private String callMethod(String library, String method, Object[] params) {
        JSONObject call = new JSONObject();
        String result = "";

        try {
            ArrayList<Object> list = new ArrayList();

            call.put("library", library);
            call.put("method", method);

            for (int i = 0; i <  params.length; i++)
                list.add(params[i]);

            JSONArray jsonParams = new JSONArray(list);
            call.put("params", jsonParams);

            Socket sock = new Socket(host, port);
            sock.setSoTimeout(15000);
            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();

            String request = call.toString();
            //byte[] bytesToSend = request.getBytes();
            //System.out.println("Request: " + request);
            PrintWriter writer = new PrintWriter(out, true);
            writer.println(request);

            //TODO: update read to use BufferedReader to maintain consistency with server
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[buffSize];

            for (int i; (i = in.read(buffer)) != -1; )
                baos.write(buffer, 0, i);

            result = baos.toString();

            out.close();
            in.close();
            sock.close();
        } catch (Exception e) {
            System.out.println("Exception in callMethod: " + e.getMessage());
            result = "{}";
        }

        return result;
    }

    /**
     * Checks if the client is connected to the server
     *
     * @return True if connected, false if not
     * @throws JSONException
     */
    public boolean isConnected() throws JSONException {
        String result = callMethod("server", "connection", new Object[0]);
        //System.out.println(result);
        JSONObject jObj = new JSONObject(result);
        return jObj.optBoolean("result", false);
    }

    public Monster getMonster(String name) throws JSONException {
        String result = callMethod("monster", "get", new Object[]{name});
        JSONObject jObj = new JSONObject(result);
        return new Monster(jObj.getJSONObject("result"));
    }

    public Encounter getEncounter(String name) throws JSONException {
        String result = callMethod("encounter", "get", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jObj = new JSONObject(result);
        return new Encounter(jObj.getJSONObject("result"));
    }

    public PlayerCharacter getPlayerCharacter(String name) throws JSONException {
        String result = callMethod("pc", "get", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jObj = new JSONObject(result);
        return new PlayerCharacter(jObj.getJSONObject("result"));
    }

    public boolean addMonster(String name) throws JSONException {
        String result = callMethod("monster", "add", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean addEncounter(String name) throws JSONException {
        String result = callMethod("encounter", "add", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean addPlayerCharacter(String name) throws JSONException {
        String result = callMethod("pc", "add", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean deleteMonster(String name) throws JSONException {
        String result = callMethod("monster", "delete", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean deleteEncounter(String name) throws JSONException {
        String result = callMethod("encounter", "delete", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean deletePlayerCharacter(String name) throws JSONException {
        String result = callMethod("pc", "delete", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean renameMonster(String oldName, Monster monster) throws JSONException {
        String result = callMethod("monster", "change", new Object[]{oldName, monster.toJson()});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }
    public boolean renameEncounter(String oldName, Encounter encounter) throws JSONException {
        String result = callMethod("encounter", "change", new Object[]{oldName, encounter.toJson()});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean updateMonster(Monster monster) throws JSONException {
        String result = callMethod("monster", "update", new Object[]{monster.toJson()});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean updateEncounter(Encounter encounter) throws JSONException {
        String result = callMethod("encounter", "update", new Object[]{encounter.toJson()});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean updatePlayerCharacter(PlayerCharacter pc) throws JSONException {
        String result = callMethod("pc", "update", new Object[]{pc.toJson()});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean restoreMonster(String name) throws JSONException {
        String result = callMethod("monster", "restore", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean restoreEncounter(String name) throws JSONException {
        String result = callMethod("encounter", "restore", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public ArrayList<String> getMonsterList() throws JSONException {
        String result = callMethod("monster", "list", new Object[0]);
        //System.out.println("Received " + result + " from server.");
        JSONObject jObj = new JSONObject(result);
        JSONArray jArr = jObj.getJSONArray("result");
        ArrayList<String> list = new ArrayList();

        for (int i = 0; i < jArr.length(); i++)
            list.add(jArr.getString(i));

        return list;
    }

    public ArrayList<String> getEncounterList() throws JSONException {
        String result = callMethod("encounter", "list", new Object[0]);
        //System.out.println("Received " + result + " from server.");
        JSONObject jObj = new JSONObject(result);
        JSONArray jArr = jObj.getJSONArray("result");
        ArrayList<String> list = new ArrayList();

        for (int i = 0; i < jArr.length(); i++)
            list.add(jArr.getString(i));

        return list;
    }

    public ArrayList<String> getPlayerCharacterList() throws JSONException {
        String result = callMethod("pc", "list", new Object[0]);
        //System.out.println("Received " + result + " from server.");
        JSONObject jObj = new JSONObject(result);
        JSONArray jArr = jObj.getJSONArray("result");
        ArrayList<String> list = new ArrayList();

        for (int i = 0; i < jArr.length(); i++)
            list.add(jArr.getString(i));

        return list;
    }

    public boolean saveMonster(String name) throws JSONException {
        String result = callMethod("monster", "save", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean saveEncounter(String name) throws JSONException {
        String result = callMethod("encounter", "save", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public ArrayList<String> getMusicList() throws JSONException {
        String result = callMethod("encounter", "music", new Object[0]);
        JSONObject jObj = new JSONObject(result);
        JSONArray jArr = jObj.getJSONArray("result");
        ArrayList<String> list = new ArrayList();

        for (int i = 0; i < jArr.length(); i++)
            list.add(jArr.getString(i));

        return list;
    }

    public boolean savePlayerCharacters() throws JSONException {
        String result = callMethod("pc", "save", new Object[0]);
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean saveAll() throws JSONException {
        String result = callMethod("all", "save", new Object[0]);
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean startCombat(String name) throws JSONException {
        String result = callMethod("combat", "begin", new Object[]{name});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean updateCombat(JSONArray array) throws JSONException {
        String result = callMethod("combat", "update", new Object[]{array});
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }

    public boolean endCombat() throws JSONException {
        String result = callMethod("combat", "end", new Object[0]);
        //System.out.println("Received " + result + " from server.");
        JSONObject jsonResult = new JSONObject(result);
        return jsonResult.optBoolean("result", false);
    }
}
