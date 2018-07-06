package it.meridian.spellbook35;


import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class SearchSuggestions extends ContentProvider
{
	static private final String CREATE_TABLE = "CREATE VIRTUAL TABLE _fts_spell USING fts3(" +
	                                           "    id   INTEGER PRIMARY KEY NOT NULL, " +
	                                           "    name TEXT    UNIQUE      NOT NULL, " +
	                                           ")";
	
	static private final String QUERY = "SELECT id   AS _id,                                                 " +
	                                    "       name AS " + SearchManager.SUGGEST_COLUMN_TEXT_1 +         ", " +
	                                    "       name AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID + "  " +
	                                    "FROM  _fts_spell                                                    " +
	                                    "WHERE name MATCH ?                                                  " +
	                                    "COLLATE nocase";
	
	
	public @Override
	boolean
	onCreate()
	{
		return true;
	}
	
	
	/**
	 * Implement this to handle query requests from clients.
	 * This method can be called from multiple threads, as described in
	 * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html#Threads">Processes
	 * and Threads</a>.
	 *
	 * @param uri           The URI to query. This will be the full URI sent by the client;
	 *                      if the client is requesting a specific record, the URI will end in a record number
	 *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
	 *                      that _id value.
	 * @param projection    The list of columns to put into the cursor. If
	 *                      {@code null} all columns are included.
	 * @param selection     A selection criteria to apply when filtering rows.
	 *                      If {@code null} then all rows are included.
	 * @param selectionArgs You may include ?s in selection, which will be replaced by
	 *                      the values from selectionArgs, in order that they appear in the selection.
	 *                      The values will be bound as Strings.
	 * @param sortOrder     How the rows in the cursor should be sorted.
	 *                      If {@code null} then the provider is free to define the sort order.
	 * @return a Cursor or {@code null}.
	 */
	public @Override @Nullable
	Cursor
	query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
	{
		// Add asterisks around the search expression to enable prefix/suffix matching
		for(int i = 0; i < selectionArgs.length; i++)
			selectionArgs[i] = "*" + selectionArgs[i] + "*";
		
		Cursor cursor = Application.query(QUERY, selectionArgs);
		return cursor;
	}
	
	
	public @Override @Nullable
	String
	getType(@NonNull Uri uri)
	{
		return null;
	}
	
	
	public @Override @Nullable
	Uri
	insert(@NonNull Uri uri, @Nullable ContentValues values)
	{
		return null;
	}
	
	
	public @Override
	int
	delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
	{
		return 0;
	}
	
	
	@Override
	public
	int
	update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs)
	{
		return 0;
	}
}
