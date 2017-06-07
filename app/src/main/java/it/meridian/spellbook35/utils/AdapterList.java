package it.meridian.spellbook35.utils;


import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.HashSet;
import java.util.Set;

import it.meridian.spellbook35.utils.callbacks.*;

@SuppressWarnings("UnnecessaryLocalVariable")
public class AdapterList implements android.widget.ListAdapter
{
	private IFunction1<Long, Cursor> getItemIdCallback;
	private IFunction3<View, Cursor, View, ViewGroup> getItemViewCallback;
	private Cursor cursor;
	private final Set<DataSetObserver> observers = new HashSet<>();
	
	
	public AdapterList(IFunction1<Long, Cursor> getItemIdCallback,
	                   IFunction3<View, Cursor, View, ViewGroup> getItemViewCallback,
	                   Cursor cursor)
	{
		this.getItemIdCallback = getItemIdCallback;
		this.getItemViewCallback = getItemViewCallback;
		this.cursor = cursor;
	}
	
	
	public AdapterList(IFunction1<Long, Cursor> getItemIdCallback,
	                   IFunction3<View, Cursor, View, ViewGroup> getItemViewCallback)
	{
		this(getItemIdCallback, getItemViewCallback, null);
	}
	

	public void swapCursor(Cursor new_cursor)
	{
		if(this.cursor == new_cursor)
			return;

		if(this.cursor != null)
		{
			synchronized(this.observers)
			{
				for(DataSetObserver obs : this.observers)
					this.cursor.unregisterDataSetObserver(obs);
			}
			this.cursor.close();
		}

		this.cursor = new_cursor;

		if(this.cursor != null)
		{
			synchronized(this.observers)
			{
				for(DataSetObserver obs : this.observers)
					this.cursor.registerDataSetObserver(obs);
			}
			this.notifyDataSetChanged();
		}
		else
		{
			this.notifyDataSetInvalidated();
		}
	}
	

	/**
	 * Notifies the attached observers that the underlying data has been changed
	 * and any View reflecting the data set should refresh itself.
	 */
	private void notifyDataSetChanged()
	{
		synchronized(this.observers)
		{
			// Since onChanged() is implemented by the app, it could do anything, including
			// removing itself from {@link observers} - and that could cause problems if
			// an iterator is used on the Set {@link observers}.
			// To avoid such problems, iterate over a copy of the set.
			for(DataSetObserver obs : new HashSet<>(this.observers))
				obs.onChanged();
		}
	}
	

	/**
	 * Notifies the attached observers that the underlying data is no longer valid
	 * or available. Once invoked this adapter is no longer valid and should
	 * not report further data set changes.
	 */
	private void notifyDataSetInvalidated()
	{
		synchronized(this.observers)
		{
			for(DataSetObserver obs : new HashSet<>(this.observers))
				obs.onInvalidated();
		}
	}
	

	/**
	 * Indicates whether all the items in this adapter are enabled. If the
	 * value returned by this method changes over time, there is no guarantee
	 * it will take effect.  If true, it means all items are selectable and
	 * clickable (there is no separator.)
	 *
	 * @return True if all items are enabled, false otherwise.
	 * @see #isEnabled(int)
	 */
	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}
	

	/**
	 * Returns true if the item at the specified position is not a separator.
	 * (A separator is a non-selectable, non-clickable item).
	 * <p>
	 * The result is unspecified if position is invalid. An {@link ArrayIndexOutOfBoundsException}
	 * should be thrown in that case for fast failure.
	 *
	 * @param position Index of the item
	 * @return True if the item is not a separator
	 * @see #areAllItemsEnabled()
	 */
	@Override
	public boolean isEnabled(int position)
	{
		return true;
	}
	

	/**
	 * Register an observer that is called when changes happen to the data used by this adapter.
	 *
	 * @param observer the object that gets notified when the data set changes.
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer)
	{
		if(observer != null)
		{
			synchronized(this.observers)
			{
				this.observers.add(observer);
				if(this.cursor != null)
					this.cursor.registerDataSetObserver(observer);
			}
		}
	}
	

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver}.
	 *
	 * @param observer the object to unregister.
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer)
	{
		if(observer != null)
		{
			synchronized(this.observers)
			{
				this.observers.remove(observer);
				if(this.cursor != null)
					this.cursor.unregisterDataSetObserver(observer);
			}
		}
	}
	

	/**
	 * How many items are in the data set represented by this Adapter.
	 *
	 * @return Count of items.
	 */
	@Override
	public int getCount()
	{
		int result = 0;
		if(this.cursor != null)
			result = this.cursor.getCount();
		return result;
	}
	

	/**
	 * Get the data item associated with the specified position in the data set.
	 *
	 * @param position Position of the item whose data we want within the adapter's
	 *                 data set.
	 * @return The data at the specified position.
	 */
	@Override
	public Object getItem(int position)
	{
		if(this.cursor != null)
		{
			boolean success = this.cursor.moveToPosition(position);
			return this.cursor;
		}
		return null;
	}
	

	/**
	 * Get the row id associated with the specified position in the list.
	 *
	 * @param position The position of the item within the adapter's data set whose row id we want.
	 * @return The id of the item at the specified position.
	 */
	@Override
	public long getItemId(int position)
	{
		long result = 0;
		if(this.cursor != null)
		{
			this.cursor.moveToPosition(position);
			result = this.getItemIdCallback.call(this.cursor);
		}
		return result;
	}
	

	/**
	 * Indicates whether the item ids are stable across changes to the
	 * underlying data.
	 *
	 * @return True if the same id always refers to the same object.
	 */
	@Override
	public boolean hasStableIds()
	{
		return true;
	}
	

	/**
	 * Get a View that displays the data at the specified position in the data set. You can either
	 * create a View manually or inflate it from an XML layout file. When the View is inflated, the
	 * parent View (GridView, ListView...) will apply default layout parameters unless you use
	 * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 *
	 * @param position    The position of the item within the adapter's data set of the item whose view
	 *                    we want.
	 * @param convertView The old view to reuse, if possible. Note: You should check that this view
	 *                    is non-null and of an appropriate type before using. If it is not possible to convert
	 *                    this view to display the correct data, this method can create a new view.
	 *                    Heterogeneous lists can specify their number of view types, so that this View is
	 *                    always of the right type (see {@link #getViewTypeCount()} and
	 *                    {@link #getItemViewType(int)}).
	 * @param parent      The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View result = null;
		if(this.cursor != null)
		{
			this.cursor.moveToPosition(position);
			result = this.getItemViewCallback.call(this.cursor, convertView, parent);
		}
		return result;
	}
	

	/**
	 * Get the type of View that will be created by {@link #getView} for the specified item.
	 *
	 * @param position The position of the item within the adapter's data set whose view type we
	 *                 want.
	 * @return An integer representing the type of View. Two views should share the same type if one
	 * can be converted to the other in {@link #getView}. Note: Integers must be in the
	 * range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
	 * also be returned.
	 * @see #IGNORE_ITEM_VIEW_TYPE
	 */
	@Override
	public int getItemViewType(int position)
	{
		return 0;
	}
	

	/**
	 * <p>
	 * Returns the number of types of Views that will be created by
	 * {@link #getView}. Each type represents a set of views that can be
	 * converted in {@link #getView}. If the adapter always returns the same
	 * type of View for all items, this method should return 1.
	 * </p>
	 * <p>
	 * This method will only be called when the adapter is set on the {@link AdapterView}.
	 * </p>
	 *
	 * @return The number of types of Views that will be created by this adapter
	 */
	@Override
	public int getViewTypeCount()
	{
		return 1;
	}
	

	/**
	 * @return true if this adapter doesn't contain any data.  This is used to determine
	 * whether the empty view should be displayed.  A typical implementation will return
	 * getCount() == 0 but since getCount() includes the headers and footers, specialized
	 * adapters might want a different behavior.
	 */
	@Override
	public boolean isEmpty()
	{
		boolean empty = this.getCount() == 0;
		return empty;
	}
}
