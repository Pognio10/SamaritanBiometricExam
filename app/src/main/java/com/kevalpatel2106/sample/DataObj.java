package com.kevalpatel2106.sample;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Marco on 30/01/17.
 */

public class DataObj
{
	private static final String SUBSCRIPTION_KEY = "de383960738d49c19cd41cbd4f399a56";

	public static String getSubscriptionKey()
	{
		return SUBSCRIPTION_KEY;
	}

	public static void saveMainPath(SharedPreferences sharedPreferences, String path)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if(!sharedPreferences.contains(path))
		{
			editor.putString("mainPath", path);
			editor.commit();
		}
	}

	public static String getMainPath(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getString("mainPath", null);
	}

	public static void addPersonWhiteList(SharedPreferences sharedPreferences, String personId, String personName)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if(!sharedPreferences.contains(personId))
		{
			editor.putString(personId, personName);
			editor.commit();
		}
	}

	public static void removePerson(SharedPreferences sharedPreferences, String uuidPerson)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.remove(uuidPerson);
		editor.commit();
	}

	public static void listPersonWhiteListAction(SharedPreferences sharedPreferences, String uuidPerson, Action action)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if(action.name().equals(Action.SAVE.name()))
		{
			Set<String> oldData = sharedPreferences.getStringSet("listPerson", Collections.<String>emptySet());
			Set<String> set = new HashSet<String>();
			set.addAll(oldData);
			set.add(uuidPerson);
			editor.putStringSet("listPerson", set);
			editor.commit();
		}
		else if(action.name().equals(Action.DELETE.name()))
		{
			Set<String> oldSet = sharedPreferences.getStringSet("listPerson", Collections.<String>emptySet());
			ArrayList oldData = new ArrayList();
			oldData.addAll(oldSet);
			oldData.remove(uuidPerson);
			Set<String> set = new HashSet<String>();
			set.addAll(oldData);
			editor.putStringSet("listPerson", set);
			editor.commit();
		}
	}

	public static Set<String> getListPersonWhitelist(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getStringSet("listPerson", Collections.<String>emptySet());
	}

	public static boolean existPersonWhiteList(SharedPreferences sharedPreferences, String personId)
	{
		return sharedPreferences.contains(personId);
	}

	public static String getPersonWhiteList(SharedPreferences sharedPreferences, String personId)
	{
		return sharedPreferences.getString(personId, "");
	}

	public static void clearDataObj(SharedPreferences sharedPreferences)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		editor.commit();
	}


	public static void saveGroupId(SharedPreferences sharedPreferences, String groupId)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("groupId", groupId);
		editor.commit();
	}

	public static String getGroupId(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getString("groupId", "NOT SET");
	}

	public enum Action
	{
		DELETE,
		SAVE
	}



	/*public static void saveTurni(SharedPreferences sharedPreferences, String month, Turni turni)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		Set set;
		if(existMonth(sharedPreferences, month))
			set = getMonthSet(sharedPreferences, month);
		else
			set = new HashSet();
		set.add(turni.toString());
		editor.putStringSet(month, set);
		editor.commit();
	}


	public static void addMonth(SharedPreferences sharedPreferences, String month)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		Set s = new HashSet();
		if(existMonth(sharedPreferences, month))
			s = getMonthSet(sharedPreferences, month);
		editor.putStringSet(month, s);
		editor.commit();
	}

	public static void update_set_month(SharedPreferences sharedPreferences, List<Turni> list_turni, String month)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		Set set = new HashSet();
		for(Turni turno : list_turni)
			set.add(turno.toString());
		editor.putStringSet(month, set);
		editor.commit();

	}

	public static int getNumberForMonth(SharedPreferences sharedPreferences, String month)
	{
		if(existMonth(sharedPreferences, month))
			return getMonthSet(sharedPreferences, month).size();
		return 0;
	}

	public static boolean existMonth(SharedPreferences sharedPreferences, String month)
	{
		return sharedPreferences.contains(month);
	}

	public static Set getMonthSet(SharedPreferences sharedPreferences, String month)
	{
		return sharedPreferences.getStringSet(month, null);
	}

	public static Map getAll(SharedPreferences sharedPreferences)
	{
		return sharedPreferences.getAll();
	}

	public static ArrayList<String> getAllKey(SharedPreferences sharedPreferences)
	{
		ArrayList<String> key_list =  new ArrayList<>();
		for(String key : sharedPreferences.getAll().keySet())
		{
			key_list.add(key);
		}

		return key_list;
	}

	public static void removeDataMonth(SharedPreferences sharedPreferences, String month)
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		if(existMonth(sharedPreferences, month))
		{
			editor.remove(month);
			editor.apply();
			editor.commit();
		}
	}*/
}
