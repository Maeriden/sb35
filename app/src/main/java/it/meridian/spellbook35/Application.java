package it.meridian.spellbook35;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.meridian.spellbook35.utils.Utils;


public class Application extends android.app.Application
{
	static public final String DATABASE_NAME = "spellbook35.sqlite";
	static private Application instance;
	
	
	private SQLiteDatabase database;
	private String         current_character;
	
	
	static public
	Application
	get_instance()
	{
		return instance;
	}
	
	
	public @Override
	void
	onCreate()
	{
		super.onCreate();
		Application.instance = this;
		
		File database_dir = this.getDatabasePath().getParentFile();
		if(!database_dir.exists())
		{
			if(!database_dir.mkdir())
			{
				Toast.makeText(this, "Could not create databases directory", Toast.LENGTH_SHORT).show();
			}
		}
		
		this.reloadDatabase();
	}
	
	
	public
	File
	getDatabasePath()
	{
//		return this.getExternalFilesDir(null);
		return this.getDatabasePath(Application.DATABASE_NAME);
	}
	
	
	public
	boolean
	reloadDatabase()
	{
		if(this.database != null)
			this.database.close();
		
		File database_file = this.getDatabasePath();
		if(database_file.exists())
		{
			try
			{
				this.database = SQLiteDatabase.openDatabase(database_file.toString(), null, 0);
				return true;
			}
			catch(SQLiteCantOpenDatabaseException ignored)
			{
			}
		}
		
		return false;
	}
	
	
	static public
	Cursor
	query(String sql, String... selectionArgs)
	{
		Cursor cursor = null;
		if(instance.database != null)
			cursor = instance.database.rawQuery(sql, selectionArgs);
		return cursor;
	}
	
	
	static public
	long
	insert(String table, ContentValues values)
	{
		long rowid = -1;
		if(instance.database != null)
			rowid = instance.database.insert(table, null, values);
		return rowid;
	}
	
	
	static public
	int
	update(String table, ContentValues values, String where, String... args)
	{
		int affected = 0;
		if(instance.database != null)
			affected = instance.database.update(table, values, where, args);
		return affected;
	}
	
	
	static public
	int
	delete(String table, String where, String... args)
	{
		int affected = 0;
		if(instance.database != null)
			affected = instance.database.delete(table, where, args);
		return affected;
	}
	
	
	static public
	boolean
	create_character(String name)
	{
		if(name != null && name.length() > 0)
		{
			ContentValues values = new ContentValues(1);
			values.put("name", name);
			long rowid = Application.insert("character", values);
			return rowid != -1;
		}
		return false;
	}
	
	
	static public
	boolean
	rename_character(String old_name, String new_name)
	{
		if(old_name != null && old_name.length() > 0)
		{
			if(new_name != null && new_name.length() > 0)
			{
				ContentValues values = new ContentValues(1);
				values.put("name", new_name);
				final String WHERE = "name = ?";
				int affected = Application.update("character", values, WHERE, old_name);
				return affected > 0;
			}
		}
		return false;
	}
	
	
	static public
	boolean
	delete_character(String name)
	{
		if(name != null && name.length() > 0)
		{
			final String WHERE = "name = ?";
			long affected = Application.delete("character", WHERE, name);
			return affected > 0;
		}
		return false;
	}
	
	
	static public
	Character[]
	backup_characters()
	{
		Map<String, Character> characters = new HashMap<>();
		
		if(instance.database != null)
		{
			Cursor cursor = Application.query("SELECT * FROM character");
			if(cursor != null && cursor.getCount() > 0)
			{
				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToNext();
					int    id   = Utils.CursorGetInt(cursor, "id");
					String name = Utils.CursorGetString(cursor, "name");
					
					Character character = new Character();
					character.id   = id;
					character.name = name;
					characters.put(name, character);
				}
				cursor.close();
				
				
				cursor = Application.query("SELECT * FROM character_spell_slot");
				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToNext();
					String  character = Utils.CursorGetString(cursor, "character");
					int     id        = Utils.CursorGetInt(cursor, "id");
					int     level     = Utils.CursorGetInt(cursor, "level");
					String  spell     = Utils.CursorGetString(cursor, "spell");
					boolean expended  = Utils.CursorGetInt(cursor, "expended")  != 0;
					boolean is_domain = Utils.CursorGetInt(cursor, "is_domain") != 0;
					
					SpellSlot slot = new SpellSlot();
					slot.id        = id;
					slot.level     = level;
					slot.spell     = spell;
					slot.expended  = expended;
					slot.is_domain = is_domain;
					characters.get(character).slots.add(slot);
				}
				cursor.close();
				
				
				cursor = Application.query("SELECT * FROM character_spell_known");
				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToNext();
					String character    = Utils.CursorGetString(cursor, "character");
					String source       = Utils.CursorGetString(cursor, "source");
					String spell        = Utils.CursorGetString(cursor, "spell");
					int    level        = Utils.CursorGetInt(cursor, "level");
					int    learn_remain = Utils.CursorGetInt(cursor, "learn_remain");
					int    copy_remain  = Utils.CursorGetInt(cursor, "copy_remain");
					
					SpellKnown known = new SpellKnown();
					known.source       = source;
					known.spell        = spell;
					known.level        = level;
					known.learn_remain = learn_remain;
					known.copy_remain  = copy_remain;
					characters.get(character).known.add(known);
				}
				cursor.close();
			}
		}

		return characters.values().toArray(new Character[0]);
	}
	
	
	static public
	void
	restore_characters(Character[] characters)
	{
		for(Character character : characters)
		{
			ContentValues values_character = new ContentValues(2);
			values_character.put("id",   character.id);
			values_character.put("name", character.name);
			long rowid = Application.insert("character", values_character);
			if(rowid != -1)
			{
				for(SpellSlot slot : character.slots)
				{
					ContentValues values = new ContentValues(6);
					values.put("character", character.name);
					values.put("id",        slot.id);
					values.put("level",     slot.level);
					values.put("spell",     slot.spell);
					values.put("expended",  slot.expended);
					values.put("is_domain", slot.is_domain);
					Application.insert("character_spell_slot", values);
				}
				
				for(SpellKnown known : character.known)
				{
					ContentValues values = new ContentValues(6);
					values.put("character",    character.name);
					values.put("source",       known.source);
					values.put("spell",        known.spell);
					values.put("level",        known.level);
					values.put("learn_remain", known.learn_remain);
					values.put("copy_remain",  known.copy_remain);
					Application.insert("character_spell_known", values);
				}
			}
		}
	}
	
	
	
	
	public static class Character
	{
		int    id;
		String name;
		
		List<SpellSlot>  slots = new ArrayList<>();
		List<SpellKnown> known = new ArrayList<>();
	}
	
	
	public static class SpellSlot
	{
		int       id;
		int       level;
		String    spell;
		boolean   expended;
		boolean   is_domain;
	}
	
	
	public static class SpellKnown
	{
		String    source;
		String    spell;
		int       level;
		int       learn_remain;
		int       copy_remain;
	}
}
