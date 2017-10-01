package it.meridian.spellbook35;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;

public class Application extends android.app.Application
{
	static public final String DATABASE_NAME = "spellbook35.sqlite";
	static public String current_character;
	static private SQLiteDatabase database;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		this.reloadDatabase();
	}
	
	public boolean reloadDatabase()
	{
		if(database != null)
			database.close();
		
		String database_path = this.getDatabasePath(DATABASE_NAME).toString();
		try
		{
			database = SQLiteDatabase.openDatabase(database_path, null, 0);
		}
		catch(SQLiteCantOpenDatabaseException e)
		{
			return false;
		}
		
		return true;
	}
	
	static public Cursor query(String sql, String... selectionArgs)
	{
		Cursor cursor = null;
		if(database != null)
			cursor = database.rawQuery(sql, selectionArgs);
		return cursor;
	}
	
	static public long insert(String table, ContentValues values)
	{
		long rowid = -1;
		if(database != null)
			rowid = database.insert(table, null, values);
		return rowid;
	}
	
	static public int update(String table, ContentValues values, String where, String... args)
	{
		int affected = 0;
		if(database != null)
			affected = database.update(table, values, where, args);
		return affected;
	}
	
	static public int delete(String table, String where, String... args)
	{
		int affected = 0;
		if(database != null)
			affected = database.delete(table, where, args);
		return affected;
	}
}
