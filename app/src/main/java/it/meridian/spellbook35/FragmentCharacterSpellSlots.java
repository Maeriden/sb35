package it.meridian.spellbook35;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.Locale;
import java.util.Objects;

import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewIntPicker;
import it.meridian.spellbook35.views.ViewSpellSlot;
import it.meridian.spellbook35.views.ViewSpellSlotGroup;


public class FragmentCharacterSpellSlots extends android.support.v4.app.Fragment implements AdapterExpandableList.ISupplier, FragmentAssignSpell.ISpellChoiceListener
{
	static public final String TAG = "it.meridian.spellbook35.FragmentCharacterSpellSlots";
	static public final int REQUEST_CODE_ASSIGN_SPELL = 1;
	
	static private final String QUERY_GROUPS =
			"  SELECT level, SUM(expended) AS expended_count, COUNT(*) AS slot_count " +
			"    FROM character_spell_slot " +
			"   WHERE character = ? " +
			"GROUP BY level " +
			"ORDER BY level";
	
	static private final String QUERY_CHILDREN =
			"   SELECT slot.id, slot.spell_name, spell.summary AS spell_desc, slot.expended " +
			"     FROM character_spell_slot slot " +
			"LEFT JOIN spell " +
			"       ON slot.spell_name = spell.name " +
			"    WHERE slot.character = ? " +
			"      AND slot.level = ? " +
			" ORDER BY slot.spell_name IS NULL, slot.spell_name";
	
	static private final String WHERE_CLAUSE =
			"character = ? AND id = ?";
	
	static private final String QUERY_NEXT_SLOT_ID =
			"SELECT MAX(id)+1 AS next_id " +
			"  FROM character_spell_slot " +
			" WHERE character = ?";
	
	
	
	private AdapterExpandableList adapter;
	private String character_name;
	
	private FragmentSpellInfo frag_spell_info;
	private FragmentAssignSpell frag_assign_spell;
	
	private int slot_id_to_assign = -1;
	
	
	
	public FragmentCharacterSpellSlots()
	{
		this.adapter = new AdapterExpandableList(this, null);
		this.frag_spell_info = new FragmentSpellInfo();
		this.frag_assign_spell = new FragmentAssignSpell();
	}
	
	/**
	 * Called to do initial creation of a fragment.  This is called after
	 * {@link #onAttach(Activity)} and before
	 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 * <p>
	 * <p>Note that this can be called while the fragment's activity is
	 * still in the process of being created.  As such, you can not rely
	 * on things like the activity's content view hierarchy being initialized
	 * at this point.  If you want to do work once the activity itself is
	 * created, see {@link #onActivityCreated(Bundle)}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		String character_name = (String) this.getArguments().get(FragmentCharacter.ARG_KEY_CHARACTER);
		if(!Objects.equals(this.character_name, character_name))
		{
			this.character_name = character_name;
			this.refresh();
		}
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setOnCreateContextMenuListener(this::onCreateExpandableListContextMenu);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	/**
	 * Initialize the contents of the Activity's standard options menu.  You
	 * should place your menu items in to <var>menu</var>.  For this method
	 * to be called, you must have first called {@link #setHasOptionsMenu}.  See
	 * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
	 * for more information.
	 *
	 * @param menu The options menu in which you place your items.
	 * @see #setHasOptionsMenu
	 * @see #onPrepareOptionsMenu
	 * @see #onOptionsItemSelected
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_character_slots, menu);
	}
	
	/**
	 * This hook is called whenever an item in your options menu is selected.
	 * The default implementation simply returns false to have the normal
	 * processing happen (calling the item's Runnable or sending a message to
	 * its Handler as appropriate).  You can use this method for any items
	 * for which you would like to do processing without those other
	 * facilities.
	 * <p>
	 * <p>Derived classes should call through to the base class for it to
	 * perform the default menu handling.
	 *
	 * @param item The menu item that was selected.
	 * @return boolean Return false to allow normal menu processing to
	 * proceed, true to consume it here.
	 * @see #onCreateOptionsMenu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_add:
			{
				View dialog_view = View.inflate(this.getContext(), R.layout.dialog_choose_level, null);
				AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this.getContext());
				{
					dialog_builder.setView(dialog_view);
					dialog_builder.setPositiveButton(R.string.ok, this::onClickButtonAddSlot);
					dialog_builder.setNegativeButton(R.string.cancel, this::onClickButtonAddSlot);
					dialog_builder.setTitle(R.string.choose_levels);
				}
				dialog_builder.create().show();
			}
			break;
			
			case R.id.menu_action_clear:
			{
				ContentValues values = new ContentValues(1);
				values.put("expended", false);
				long affected = Application.update("character_spell_slot", values,
				                                   "character = ?",
				                                   this.character_name);
				if(affected > 0)
					this.refresh();
			}
			break;
		}
		return true;
	}
	
	
	private void onClickButtonAddSlot(DialogInterface dialog, int which)
	{
		if(which == DialogInterface.BUTTON_POSITIVE)
		{
			final int[] PICKER_IDS = new int[]{
					R.id.picker_level_0, R.id.picker_level_1,
					R.id.picker_level_2, R.id.picker_level_3,
					R.id.picker_level_4, R.id.picker_level_5,
					R.id.picker_level_6, R.id.picker_level_7,
					R.id.picker_level_8, R.id.picker_level_9,
			};
			
			AlertDialog alert = (AlertDialog) dialog;
			Cursor cursor = Application.query(QUERY_NEXT_SLOT_ID, this.character_name);
			cursor.moveToFirst();
			int next_id = Utils.CursorGetInt(cursor, "next_id");
			cursor.close();
			
			boolean do_refresh = false;
			for(int slot_level = 0; slot_level < PICKER_IDS.length; ++slot_level)
			{
				int picker_id = PICKER_IDS[slot_level];
				int slot_count = ((ViewIntPicker) alert.findViewById(picker_id)).getValue();
				for(int n = 0; n < slot_count; ++n)
				{
					ContentValues values = new ContentValues(3);
					values.put("character", this.character_name);
					values.put("id", next_id++);
					values.put("level", slot_level);
					long rowid = Application.insert("character_spell_slot", values);
					if(rowid > 0)
						do_refresh = true;
				}
			}
			
			if(do_refresh)
				this.refresh();
		}
	}
	
	
	@Override
	public void onSpellChoiceResult(String spell_name)
	{
		// FIXME: This function is public, meaning it could be called by external code while
		//        slot_id_to_assign is not yet consumed
		ContentValues values = new ContentValues(1);
		values.put("spell_name", spell_name);
		int affected = Application.update("character_spell_slot",
		                                  values,
		                                  WHERE_CLAUSE,
		                                  this.character_name,
		                                  Integer.toString(this.slot_id_to_assign));
		if(affected > 0)
		{
			this.refresh();
		}
		this.slot_id_to_assign = -1;
	}
	
	@Override
	public void onSpellChoiceCancel()
	{
		this.slot_id_to_assign = -1;
	}
	
	
	
	private boolean onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		boolean is_empty = spell_name == null;
		
		if(is_empty)
		{
			int slot_id = Utils.CursorGetInt(child_cursor, "id");
			this.slot_id_to_assign = slot_id;
			
			Bundle args = new Bundle(1);
			args.putString(FragmentCharacter.ARG_KEY_CHARACTER, this.character_name);
			this.frag_assign_spell.setArguments(args);
			this.frag_assign_spell.setTargetFragment(this, REQUEST_CODE_ASSIGN_SPELL);

			// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
			this.getParentFragment().getFragmentManager().beginTransaction()
					.replace(R.id.activity_main_content, this.frag_assign_spell)
					.addToBackStack(null)
					.commit();
		}
		else
		{
			Bundle args = new Bundle(1);
			args.putString(FragmentSpellInfo.ARG_KEY_SPELL, spell_name);
			this.frag_spell_info.setArguments(args);
			
			// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
			this.getParentFragment().getFragmentManager().beginTransaction()
					.replace(R.id.activity_main_content, this.frag_spell_info)
					.addToBackStack(null)
					.commit();
		}
		return true;
	}
	
	
	private void onCreateExpandableListContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		// TODO
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		int view_type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(view_type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
			
			boolean is_empty = Utils.CursorGetString(child_cursor, "spell_name") == null;
			boolean expended = Utils.CursorGetInt(child_cursor, "expended") != 0;
			
			this.getActivity().getMenuInflater().inflate(R.menu.context_spell_slot, menu);
			menu.findItem(R.id.clear_slot).setVisible(!is_empty);
			menu.findItem(R.id.expend_slot).setVisible(!expended);
			menu.findItem(R.id.recover_slot).setVisible(expended);
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		
		int view_type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(view_type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
			int slot_id = Utils.CursorGetInt(child_cursor, "id");
			
			switch(item.getItemId())
			{
				case R.id.expend_slot:
				{
					ContentValues values = new ContentValues(1);
					values.put("expended", true);
					long affected = Application.update("character_spell_slot", values,
					                                   WHERE_CLAUSE,
					                                   this.character_name,
					                                   Integer.toString(slot_id));
					if(affected > 0)
						this.refresh();
					return true;
				}
				
				case R.id.recover_slot:
				{
					ContentValues values = new ContentValues(1);
					values.put("expended", false);
					long affected = Application.update("character_spell_slot", values,
					                                   WHERE_CLAUSE,
					                                   this.character_name,
					                                   Integer.toString(slot_id));
					if(affected > 0)
						this.refresh();
					return true;
				}
				
				case R.id.clear_slot:
				{
					ContentValues values = new ContentValues(1);
					values.put("spell_name", (String) null);
					long affected = Application.update("character_spell_slot", values,
					                                   WHERE_CLAUSE,
					                                   this.character_name,
					                                   Integer.toString(slot_id));
					if(affected > 0)
						this.refresh();
					return true;
				}
				
				case R.id.delete_slot:
				{
					long affected = Application.delete("character_spell_slot",
					                                   WHERE_CLAUSE,
					                                   this.character_name,
					                                   Integer.toString(slot_id));
					if(affected > 0)
						this.refresh();
					return true;
				}
			}
		}
		return false;
	}
	
	
	private void refresh()
	{
		Cursor groups_cursor = Application.query(QUERY_GROUPS,
		                                         this.character_name);
		this.adapter.swapGroupsCursor(groups_cursor);
	}
	
	
	
	@Override
	public long getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		int level = Utils.CursorGetInt(group_cursor, "level");
		return level;
	}
	
	@Override
	public long getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		int id = Utils.CursorGetInt(child_cursor, "id");
		return id;
	}
	
	@Override
	public Cursor getExpandableListChildrenCursor(Cursor group_cursor)
	{
		int level = Utils.CursorGetInt(group_cursor, "level");
		Cursor children_cursor = Application.query(QUERY_CHILDREN,
		                                           this.character_name,
		                                           Integer.toString(level));
		return children_cursor;
	}
	
	@Override
	public View getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewSpellSlotGroup view = (ViewSpellSlotGroup) convertView;
		if(view == null)
			view = new ViewSpellSlotGroup(this.getContext());
		
		int level = Utils.CursorGetInt(group_cursor, "level");
		int expended_count = Utils.CursorGetInt(group_cursor, "expended_count");
		int slot_count = Utils.CursorGetInt(group_cursor, "slot_count");
		
		String level_text = this.getContext().getResources().getString(R.string.level_N, level);
		String count_text = String.format(Locale.getDefault(),
		                                  ViewSpellSlotGroup.COUNT_FORMAT,
		                                  slot_count - expended_count,
		                                  slot_count);
		view.setLevelText(level_text);
		view.setCountText(count_text);
		return view;
	}
	
	@Override
	public View getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellSlot view = (ViewSpellSlot) convertView;
		if(view == null)
			view = new ViewSpellSlot(this.getContext());
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		boolean expended = Utils.CursorGetInt(child_cursor, "expended") != 0;
		
		view.setNameText(spell_name);
		view.setDescText(spell_desc);
		view.setBackgroundColor(expended ? 0x40800000 : Color.TRANSPARENT);
		return view;
//		return LayoutInflater.from(this.getContext()).inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
	}
}
