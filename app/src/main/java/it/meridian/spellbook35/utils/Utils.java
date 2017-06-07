package it.meridian.spellbook35.utils;


import android.database.Cursor;

import it.meridian.spellbook35.BuildConfig;

public final class Utils
{
	private Utils() {}

	static public void Assert(boolean condition)
	{
		if(BuildConfig.DEBUG)
			if(condition)
				throw new AssertionError();
	}

	static public void Assert(boolean condition, String message)
	{
		if(BuildConfig.DEBUG)
			if(condition)
				throw new AssertionError(message);
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	static public int CursorGetInt(Cursor cursor, String column)
	{
		int index = cursor.getColumnIndex(column);
		int result = cursor.getInt(index);
		return result;
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	static public long CursorGetLong(Cursor cursor, String column)
	{
		int index = cursor.getColumnIndex(column);
		long result = cursor.getLong(index);
		return result;
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	static public String CursorGetString(Cursor cursor, String column)
	{
		int index = cursor.getColumnIndex(column);
		String result = cursor.getString(index);
		return result;
	}
}
