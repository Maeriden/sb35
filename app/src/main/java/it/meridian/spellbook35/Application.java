package it.meridian.spellbook35;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Application extends android.app.Application
{
	static private SQLiteDatabase database;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		if(database != null)
			database.close();
		
		String database_path = this.getDatabasePath("spellbook35.sqlite").toString();
		database = SQLiteDatabase.openDatabase(database_path, null, 0);
	}
	
	static public Cursor query(String sql, String... selectionArgs)
	{
		Cursor cursor = database.rawQuery(sql, selectionArgs);
		return cursor;
	}
	
	static public long insert(String table, ContentValues values)
	{
		long rowid = database.insert(table, null, values);
		return rowid;
	}
	
	static public int update(String table, ContentValues values, String where, String... args)
	{
		int affected = database.update(table, values, where, args);
		return affected;
	}
	
	static public int delete(String table, String where, String... args)
	{
		int affected = database.delete(table, where, args);
		return affected;
	}
}
