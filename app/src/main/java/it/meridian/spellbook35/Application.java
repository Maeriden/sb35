package it.meridian.spellbook35;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
	query(SQLiteDatabase database, String sql, String... selectionArgs)
	{
		Cursor cursor = null;
		if(database != null)
			cursor = database.rawQuery(sql, selectionArgs);
		return cursor;
	}
	
	
	static public
	long
	insert(SQLiteDatabase database, String table, ContentValues values)
	{
		long rowid = -1;
		if(database != null)
			rowid = database.insert(table, null, values);
		return rowid;
	}
	
	
	static public
	int
	update(SQLiteDatabase database, String table, ContentValues values, String where, String... args)
	{
		int affected = 0;
		if(database != null)
			affected = database.update(table, values, where, args);
		return affected;
	}
	
	
	static public
	int
	delete(SQLiteDatabase database, String table, String where, String... args)
	{
		int affected = 0;
		if(database != null)
			affected = database.delete(table, where, args);
		return affected;
	}
	
	
	static public
	Cursor
	query(String sql, String... selectionArgs)
	{
		return Application.query(Application.instance.database, sql, selectionArgs);
	}
	
	
	static public
	long
	insert(String table, ContentValues values)
	{
		return Application.insert(Application.instance.database, table, values);
	}
	
	
	static public
	int
	update(String table, ContentValues values, String where, String... args)
	{
		return Application.update(Application.instance.database, table, values, where, args);
	}
	
	
	static public
	int
	delete(String table, String where, String... args)
	{
		return Application.delete(Application.instance.database, table, where, args);
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
				int affected = Application.update("character", values, "name = ?", old_name);
				
				if(affected > 0)
				{
					values = new ContentValues(1);
					values.put("character", new_name);
					Application.update("character_spell_slot",  values, "character = ?", old_name);
					Application.update("character_spell_known", values, "character = ?", old_name);
				}
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
			long affected = Application.delete("character", "name = ?", name);
			
			if(affected > 0)
			{
				Application.delete("character_spell_slot",  "character = ?", name);
				Application.delete("character_spell_known", "character = ?", name);
			}
			
			return affected > 0;
		}
		return false;
	}
	
	
	static public
	Character[]
	deserialize_characters()
	{
		return deserialize_characters(Application.instance.database);
	}
	
	
	static public
	Character[]
	deserialize_characters(SQLiteDatabase database)
	{
		if(database == null)
			return null;
		
		Map<String, Character> characters = new HashMap<>();
		
		Cursor cursor = Application.query(database, "SELECT * FROM character");
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
			
			
			try
			{
				cursor = Application.query(database, "SELECT * FROM character_spell_slot");
				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToNext();
					
					SpellSlot slot = new SpellSlot();
					String character = Utils.CursorGetString(cursor, "character");
					slot.id          = Utils.CursorGetInt(cursor,    "id");
					slot.level       = Utils.CursorGetInt(cursor,    "level");
					slot.spell       = Utils.CursorGetString(cursor, "spell");
					slot.expended    = Utils.CursorGetInt(cursor,    "expended")  != 0;
					slot.is_domain   = Utils.CursorGetInt(cursor,    "is_domain") != 0;
					
					characters.get(character).slots.add(slot);
				}
				cursor.close();
			}
			catch(SQLiteException ignored)
			{
			
			}
			
			
			try
			{
				cursor = Application.query(database, "SELECT * FROM character_spell_known");
				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToNext();
					
					SpellKnown known = new SpellKnown();
					String character   = Utils.CursorGetString(cursor, "character");
					known.source       = Utils.CursorGetString(cursor, "source");
					known.spell        = Utils.CursorGetString(cursor, "spell");
					known.level        = Utils.CursorGetInt(cursor,    "level");
					known.learn_remain = Utils.CursorGetInt(cursor,    "study_remaining_time");
					known.copy_remain  = Utils.CursorGetInt(cursor,    "copy_remaining_time");
					
					characters.get(character).known.add(known);
				}
				cursor.close();
			}
			catch(SQLiteException ignored)
			{
			
			}
			
			
			try
			{
				cursor = Application.query(database, "SELECT * FROM character_scroll");
				for(int i = 0; i < cursor.getCount(); i++)
				{
					cursor.moveToNext();
					
					Scroll scroll = new Scroll();
					String character = Utils.CursorGetString(cursor, "character");
					scroll.id        = Utils.CursorGetInt(cursor,    "id");
					scroll.type      = Utils.CursorGetInt(cursor,    "type");
					scroll.level     = Utils.CursorGetInt(cursor,    "caster_level");
					scroll.spell     = Utils.CursorGetString(cursor, "spell");
					
					characters.get(character).scrolls.add(scroll);
				}
				cursor.close();
			}
			catch(SQLiteException ignored)
			{
			
			}
		}

		return characters.values().toArray(new Character[0]);
	}
	
	
	static public
	void
	serialize_characters(String database_name, Character[] characters)
	{
		if(characters == null || characters.length < 1)
			return;
		
//		DateFormat df  = DateFormat.getDateTimeInstance();
//		String     now = df.format(new Date());
//
		File           character_backup_file = new File(Application.instance.getExternalFilesDir("character_backups"), database_name);
		SQLiteDatabase database              = SQLiteDatabase.openDatabase(character_backup_file.toString(), null, 0);
		
		for(Character character : characters)
		{
			ContentValues values_character = new ContentValues(2);
			values_character.put("id",   character.id);
			values_character.put("name", character.name);
			long rowid = Application.insert(database, "character", values_character);
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
					Application.insert(database, "character_spell_slot", values);
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
					Application.insert(database, "character_spell_known", values);
				}
				
				for(Scroll scroll : character.scrolls)
				{
					ContentValues values = new ContentValues(5);
					values.put("character",    character.name);
					values.put("id",           scroll.id);
					values.put("type",         scroll.type);
					values.put("caster_level", scroll.level);
					values.put("spell",        scroll.spell);
					Application.insert(database, "character_scroll", values);
				}
			}
		}
	}
	
	
	static public
	void
	import_characters(Character[] characters)
	{
		Application.import_characters(Application.instance.database, characters);
	}
	
	
	static public
	void
	import_characters(SQLiteDatabase database, Character[] characters)
	{
		if(database == null)
			return;
		
		for(Character character : characters)
		{
			ContentValues values_character = new ContentValues(2);
			values_character.put("id",   character.id);
			values_character.put("name", character.name);
			long rowid = Application.insert(database, "character", values_character);
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
					Application.insert(database, "character_spell_slot", values);
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
					Application.insert(database, "character_spell_known", values);
				}
				
				for(Scroll scroll : character.scrolls)
				{
					ContentValues values = new ContentValues(5);
					values.put("character",    character.name);
					values.put("id",           scroll.id);
					values.put("type",         scroll.type);
					values.put("caster_level", scroll.level);
					values.put("spell",        scroll.spell);
					Application.insert(database, "character_scroll", values);
				}
			}
		}
	}
	
	
	
	
	public static class Character
	{
		int    id;
		String name;
		
		List<SpellSlot>  slots   = new ArrayList<>();
		List<SpellKnown> known   = new ArrayList<>();
		List<Scroll>     scrolls = new ArrayList<>();
	}
	
	
	static class SpellSlot
	{
		int     id;
		int     level;
		String  spell;
		boolean expended;
		boolean is_domain;
	}
	
	
	static class SpellKnown
	{
		String source;
		String spell;
		int    level;
		int    learn_remain;
		int    copy_remain;
	}
	
	
	static class Scroll
	{
		int    id;
		int    type;
		int    level;
		String spell;
	}
}
