package it.meridian.spellbook35.utils;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	
	static public
	int
	copy_file(InputStream reader, OutputStream writer)
	{
		byte[] buffer = new byte[4 * 1024];
		while(true)
		{
			int read_count = 0;
			try
			{
				read_count = reader.read(buffer, 0, buffer.length);
				if(read_count == -1)
				{
					return 0;
				}
			}
			catch(IOException e)
			{
				System.err.println(e.getMessage());
				return 1;
			}
			
			try
			{
				writer.write(buffer, 0, read_count);
			}
			catch(IOException e)
			{
				System.err.println(e.getMessage());
				return 2;
			}
		}
	}
}
