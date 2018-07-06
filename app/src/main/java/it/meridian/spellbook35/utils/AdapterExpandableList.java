package it.meridian.spellbook35.utils;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;

import java.util.HashSet;
import java.util.Set;


public class AdapterExpandableList implements android.widget.ExpandableListAdapter
{
	private ISupplier supplier;
	private Cursor groups_cursor;
	private final SparseArray<Cursor> children_cursors = new SparseArray<>();
	private final Set<DataSetObserver> observers = new HashSet<>();
	
	
	
	public AdapterExpandableList(ISupplier supplier, Cursor groups_cursor)
	{
		this.supplier = supplier;
		this.groups_cursor = groups_cursor;
	}
	
	
	public void swapGroupsCursor(Cursor new_cursor)
	{
		if(this.groups_cursor == new_cursor)
			return;
		
		if(this.groups_cursor != null)
		{
			synchronized(this.observers)
			{
				for(DataSetObserver obs : this.observers)
					this.groups_cursor.unregisterDataSetObserver(obs);
			}
			
			this.groups_cursor.close();
			for(int i = 0; i < this.children_cursors.size(); ++i)
				this.children_cursors.valueAt(i).close();
			this.children_cursors.clear();
		}
		
		this.groups_cursor = new_cursor;
		
		if(this.groups_cursor != null)
		{
			synchronized(this.observers)
			{
				for(DataSetObserver obs : this.observers)
					this.groups_cursor.registerDataSetObserver(obs);
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
	 * Gets the number of groups.
	 *
	 * @return the number of groups
	 */
	@Override
	public int getGroupCount()
	{
		int result = 0;
		if(this.groups_cursor != null)
			result = this.groups_cursor.getCount();
		return result;
	}
	
	/**
	 * Gets the number of children in a specified group.
	 *
	 * @param groupPosition the position of the group for which the children
	 *                      count should be returned
	 * @return the children count in the specified group
	 */
	@Override
	public int getChildrenCount(int groupPosition)
	{
		int result = 0;
		Cursor children_cursor = this.getChildrenCursor(groupPosition);
		if(children_cursor != null)
			result = children_cursor.getCount();
		return result;
	}
	
	/**
	 * Gets the data associated with the given group.
	 *
	 * @param groupPosition the position of the group
	 * @return the data child for the specified group
	 */
	@Override
	public Object getGroup(int groupPosition)
	{
		if(this.groups_cursor != null)
			this.groups_cursor.moveToPosition(groupPosition);
		return this.groups_cursor;
	}
	
	/**
	 * Gets the data associated with the given child within the given group.
	 *
	 * @param groupPosition the position of the group that the child resides in
	 * @param childPosition the position of the child with respect to other
	 *                      children in the group
	 * @return the data of the child
	 */
	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		Cursor result = this.getChildrenCursor(groupPosition);
		if(result != null)
			result.moveToPosition(childPosition);
		return result;
	}
	
	private Cursor getChildrenCursor(int group_position)
	{
		Cursor result = this.children_cursors.get(group_position);
		if(result == null)
		{
			if(this.groups_cursor != null)
			{
				int prev_position = this.groups_cursor.getPosition();
				this.groups_cursor.moveToPosition(group_position);
				result = this.supplier.getExpandableListChildrenCursor(this.groups_cursor);
				this.children_cursors.put(group_position, result);
				this.groups_cursor.moveToPosition(prev_position);
			}
		}
		return result;
	}
	
	
	
	
	/**
	 * Indicates whether the child and group IDs are stable across changes to the
	 * underlying data.
	 *
	 * @return whether or not the same ID always refers to the same object
	 * @see Adapter#hasStableIds()
	 */
	@Override
	public boolean hasStableIds()
	{
		return true;
	}
	
	/**
	 * Whether the child at the specified position is selectable.
	 *
	 * @param groupPosition the position of the group that contains the child
	 * @param childPosition the position of the child within the group
	 * @return whether the child is selectable.
	 */
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}
	
	/**
	 * @see ListAdapter#areAllItemsEnabled()
	 */
	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}
	
	/**
	 * @see ListAdapter#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		int count = this.getGroupCount();
		return count == 0;
	}
	
	
	
	
	/**
	 * Gets an ID for a child that is unique across any item (either group or
	 * child) that is in this list. Expandable lists require each item (group or
	 * child) to have a unique ID among all children and groups in the list.
	 * This method is responsible for returning that unique ID given a child's
	 * ID and its group's ID. Furthermore, if {@link #hasStableIds()} is true, the
	 * returned ID must be stable as well.
	 *
	 * @param groupId The ID of the group that contains this child.
	 * @param childId The ID of the child.
	 * @return The unique (and possibly stable) ID of the child across all
	 * groups and children in this list.
	 */
	@Override
	public long getCombinedChildId(long groupId, long childId)
	{
		long group_bits = groupId & 0x7FFFFFFFL;
		long child_bits = childId & 0xFFFFFFFFL;
		long result = (group_bits << 32) | child_bits;
		return result;
	}
	
	/**
	 * Gets an ID for a group that is unique across any item (either group or
	 * child) that is in this list. Expandable lists require each item (group or
	 * child) to have a unique ID among all children and groups in the list.
	 * This method is responsible for returning that unique ID given a group's
	 * ID. Furthermore, if {@link #hasStableIds()} is true, the returned ID must be
	 * stable as well.
	 *
	 * @param groupId The ID of the group
	 * @return The unique (and possibly stable) ID of the group across all
	 * groups and children in this list.
	 */
	@Override
	public long getCombinedGroupId(long groupId)
	{
		long group_bits = groupId & 0x7FFFFFFFL;
		long result = 0x8000000000000000L | group_bits;
		return result;
	}
	
	
	
	
	/**
	 * Gets the ID for the group at the given position. This group ID must be
	 * unique across groups. The combined ID (see
	 * {@link #getCombinedGroupId(long)}) must be unique across ALL items
	 * (groups and all children).
	 *
	 * @param groupPosition the position of the group for which the ID is wanted
	 * @return the ID associated with the group
	 */
	@Override
	public long getGroupId(int groupPosition)
	{
		if(this.groups_cursor == null)
			return 0;
		this.groups_cursor.moveToPosition(groupPosition);
		return this.supplier.getExpandableListGroupId(this.groups_cursor, groupPosition);
	}
	
	/**
	 * Gets the ID for the given child within the given group. This ID must be
	 * unique across all children within the group. The combined ID (see
	 * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
	 * (groups and all children).
	 *
	 * @param groupPosition the position of the group that contains the child
	 * @param childPosition the position of the child within the group for which
	 *                      the ID is wanted
	 * @return the ID associated with the child
	 */
	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		Cursor child_cursor = this.children_cursors.get(groupPosition);
		child_cursor.moveToPosition(childPosition);
		return this.supplier.getExpandableListChildId(child_cursor, groupPosition, childPosition);
	}
	
	
	/**
	 * Gets a View that displays the given group. This View is only for the
	 * group--the Views for the group's children will be fetched using
	 * {@link #getChildView(int, int, boolean, View, ViewGroup)}.
	 *
	 * @param groupPosition the position of the group for which the View is
	 *                      returned
	 * @param isExpanded    whether the group is expanded or collapsed
	 * @param convertView   the old view to reuse, if possible. You should check
	 *                      that this view is non-null and of an appropriate type before
	 *                      using. If it is not possible to convert this view to display
	 *                      the correct data, this method can create a new view. It is not
	 *                      guaranteed that the convertView will have been previously
	 *                      created by
	 *                      {@link #getGroupView(int, boolean, View, ViewGroup)}.
	 * @param parent        the parent that this view will eventually be attached to
	 * @return the View corresponding to the group at the specified position
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		if(this.groups_cursor != null)
			this.groups_cursor.moveToPosition(groupPosition);
		return this.supplier.getExpandableListGroupView(this.groups_cursor, groupPosition, isExpanded, convertView, parent);
	}
	
	/**
	 * Gets a View that displays the data for the given child within the given
	 * group.
	 *
	 * @param groupPosition the position of the group that contains the child
	 * @param childPosition the position of the child (for which the View is
	 *                      returned) within the group
	 * @param isLastChild   Whether the child is the last child within the group
	 * @param convertView   the old view to reuse, if possible. You should check
	 *                      that this view is non-null and of an appropriate type before
	 *                      using. If it is not possible to convert this view to display
	 *                      the correct data, this method can create a new view. It is not
	 *                      guaranteed that the convertView will have been previously
	 *                      created by
	 *                      {@link #getChildView(int, int, boolean, View, ViewGroup)}.
	 * @param parent        the parent that this view will eventually be attached to
	 * @return the View corresponding to the child at the specified position
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		Cursor child_cursor = this.children_cursors.get(groupPosition);
		if(child_cursor != null)
			child_cursor.moveToPosition(childPosition);
		return this.supplier.getExpandableListChildView(child_cursor, groupPosition, childPosition, isLastChild, convertView, parent);
	}
	
	
	
	
	/**
	 * Called when a group is expanded.
	 *
	 * @param groupPosition The group being expanded.
	 */
	@Override
	public void onGroupExpanded(int groupPosition)
	{
		if(this.groups_cursor != null)
			this.groups_cursor.moveToPosition(groupPosition);
		this.supplier.onExpandableListGroupExpanded(this.groups_cursor, groupPosition);
	}
	
	/**
	 * Called when a group is collapsed.
	 *
	 * @param groupPosition The group being collapsed.
	 */
	@Override
	public void onGroupCollapsed(int groupPosition)
	{
		if(this.groups_cursor != null)
			this.groups_cursor.moveToPosition(groupPosition);
		this.supplier.onExpandableListGroupCollapsed(this.groups_cursor, groupPosition);
	}
	
	
	
	/**
	 * @param observer
	 * @see Adapter#registerDataSetObserver(DataSetObserver)
	 */
	@Override
	public void registerDataSetObserver(DataSetObserver observer)
	{
//		throw new UnsupportedOperationException("AdapterExpandableList::registerDataSetObserver");
		if(observer != null)
		{
			synchronized(this.observers)
			{
				this.observers.add(observer);
				if(this.groups_cursor != null)
					this.groups_cursor.registerDataSetObserver(observer);
			}
		}
	}
	
	/**
	 * @param observer
	 * @see Adapter#unregisterDataSetObserver(DataSetObserver)
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer)
	{
//		throw new UnsupportedOperationException("AdapterExpandableList::unregisterDataSetObserver");
		if(observer != null)
		{
			synchronized(this.observers)
			{
				this.observers.remove(observer);
				if(this.groups_cursor != null)
					this.groups_cursor.unregisterDataSetObserver(observer);
			}
		}
	}
	
	
	
	
	public interface ISupplier
	{
		long getExpandableListGroupId(Cursor group_cursor, int groupPosition);
		
		long getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition);
		
		Cursor getExpandableListChildrenCursor(Cursor group_cursor);

		View getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent);

		View getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent);
		
		default void onExpandableListGroupExpanded(Cursor group_cursor, int groupPosition) {}
		
		default void onExpandableListGroupCollapsed(Cursor group_cursor, int groupPosition) {}
	}
}
