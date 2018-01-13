package it.meridian.spellbook35.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.Locale;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewIntPicker;
import it.meridian.spellbook35.views.ViewSpellSlot;
import it.meridian.spellbook35.views.ViewCharacterSpellSlotGroup;


public class FragmentCharacterSpellSlots extends it.meridian.spellbook35.Fragment
                                         implements AdapterExpandableList.ISupplier,
                                                    FragmentSlotAssign.ISpellChoiceListener
{
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	static private final String QUERY_GROUPS =
			"  SELECT slot.level         AS level,          " +
			"         SUM(slot.expended) AS expended_count, " +
			"         COUNT(*)           AS slot_count      " +
			"    FROM character_spell_slot slot             " +
			"   WHERE slot.character = ?                    " +
			"GROUP BY slot.level                            " +
			"ORDER BY slot.level";
	
	static private final String QUERY_CHILDREN =
			"   SELECT slot.id        AS id,         " +
			"          slot.spell     AS spell_name, " +
			"          slot.expended  AS expended,   " +
			"          slot.is_domain AS is_domain,  " +
			"          spell.summary  AS spell_desc  " +
			"     FROM character_spell_slot slot     " +
			"LEFT JOIN spell                         " +
			"       ON slot.spell = spell.name       " +
			"    WHERE slot.character = ?            " +
			"      AND slot.level = ?                " +
			" ORDER BY slot.is_domain DESC,          " +
			"          slot.spell IS NULL,           " +
			"          slot.spell";
	
	static private final String WHERE_CLAUSE =
			"    character = ? " +
			"AND id = ?";
	
	static private final String QUERY_NEXT_SLOT_ID =
			"SELECT MAX(slot.id)+1 AS next_id " +
			"  FROM character_spell_slot slot " +
			" WHERE slot.character = ?";
	
	
	static public
	Fragment
	newInstance(String character_name)
	{
		Fragment fragment = new FragmentCharacterSpellSlots();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
		}
		return fragment;
	}
	
	
	
	private AdapterExpandableList adapter;
	private String character_name;
	private int slot_id_to_assign = -1;
	
	
	@CallSuper
	@Override
	public
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.character_name = this.getArguments().getString(ARG_CHARACTER_NAME);
		
		Cursor groups_cursor = Application.query(QUERY_GROUPS, this.character_name);
		this.adapter = new AdapterExpandableList(this, groups_cursor);
	}
	
	
	@Nullable
	@Override
	public
	View
	onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setGroupIndicator(null);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setOnCreateContextMenuListener(this::onCreateExpandableListItemContextMenu);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	@Override
	public
	void
	onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_character_slot, menu);
	}
	
	
	@Override
	public
	boolean
	onOptionsItemSelected(MenuItem item)
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
	
	
	private
	void
	onClickButtonAddSlot(DialogInterface dialog, int which)
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
	public
	void
	onSpellChoiceResult(String spell_name)
	{
		// FIXME: This function is public, meaning it could be called by external code while
		//        slot_id_to_assign is not yet consumed
		ContentValues values = new ContentValues(1);
		values.put("spell", spell_name);
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
	public
	void
	onSpellChoiceCancel()
	{
		this.slot_id_to_assign = -1;
	}
	
	
	private
	boolean
	onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		boolean slot_is_empty = spell_name == null;
		
		if(slot_is_empty)
		{
			this.slot_id_to_assign = Utils.CursorGetInt(child_cursor, "id");
			
			Fragment fragment = FragmentSlotAssign.newInstance(this.character_name, this);
			this.activity_main().push_fragment(fragment);
//			// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
//			this.getParentFragment().getFragmentManager().beginTransaction()
//					.replace(R.id.activity_main_content, this.frag_assign_spell)
//					.addToBackStack(null)
//					.commit();
		}
		else
		{
			Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
			this.activity_main().push_fragment(fragment);
			// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
//			this.getParentFragment().getFragmentManager().beginTransaction()
//					.replace(R.id.activity_main_content, fragment)
//					.addToBackStack(null)
//					.commit();
		}
		return true;
	}
	
	
	private void onCreateExpandableListItemContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		// TODO
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		int view_type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(view_type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
			Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
			
			boolean expended  = Utils.CursorGetInt(child_cursor,    "expended")   != 0;
			boolean is_empty  = Utils.CursorGetString(child_cursor, "spell_name") == null;
			boolean is_domain = Utils.CursorGetInt(child_cursor,    "is_domain")  != 0;
			
			this.getActivity().getMenuInflater().inflate(R.menu.context_character_slot, menu);
//			menu.findItem(R.id.expend_slot).setVisible(!expended);
//			menu.findItem(R.id.recover_slot).setVisible(expended);
			menu.findItem(R.id.toggle_expended).setChecked(expended);
			menu.findItem(R.id.clear_slot).setEnabled(!is_empty);
			menu.findItem(R.id.toggle_domain).setChecked(is_domain);
		}
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// NOTE: For some reason this function is called even when another fragment is visible
		
		if(!this.getUserVisibleHint())
			return super.onContextItemSelected(item);
		
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
//				case R.id.expend_slot:
//				{
//					ContentValues values = new ContentValues(1);
//					values.put("expended", true);
//					long affected = Application.update("character_spell_slot", values,
//					                                   WHERE_CLAUSE,
//					                                   this.character_name,
//					                                   Integer.toString(slot_id));
//					if(affected > 0)
//						this.refresh();
//					return true;
//				}
//
//				case R.id.recover_slot:
//				{
//					ContentValues values = new ContentValues(1);
//					values.put("expended", false);
//					long affected = Application.update("character_spell_slot", values,
//					                                   WHERE_CLAUSE,
//					                                   this.character_name,
//					                                   Integer.toString(slot_id));
//					if(affected > 0)
//						this.refresh();
//					return true;
//				}
				
				case R.id.toggle_expended:
				{
					boolean expended = Utils.CursorGetInt(child_cursor, "expended") != 0;
					ContentValues values = new ContentValues(1);
					values.put("expended", !expended);
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
					values.put("spell", (String) null);
					long affected = Application.update("character_spell_slot", values,
					                                   WHERE_CLAUSE,
					                                   this.character_name,
					                                   Integer.toString(slot_id));
					if(affected > 0)
						this.refresh();
					return true;
				}
				
				case R.id.toggle_domain:
				{
					boolean is_domain = Utils.CursorGetInt(child_cursor, "is_domain") != 0;
					ContentValues values = new ContentValues(1);
					values.put("is_domain", !is_domain);
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
		ViewCharacterSpellSlotGroup view = (ViewCharacterSpellSlotGroup) convertView;
		if(view == null)
			view = new ViewCharacterSpellSlotGroup(this.getContext());
		
		int level          = Utils.CursorGetInt(group_cursor, "level");
		int expended_count = Utils.CursorGetInt(group_cursor, "expended_count");
		int slot_count     = Utils.CursorGetInt(group_cursor, "slot_count");
		
		String level_text = this.getContext().getResources().getString(R.string.level_N, level);
		String count_text = String.format(Locale.getDefault(),
		                                  ViewCharacterSpellSlotGroup.COUNT_FORMAT,
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
		boolean is_domain = Utils.CursorGetInt(child_cursor, "is_domain") != 0;
		boolean expended = Utils.CursorGetInt(child_cursor, "expended") != 0;
		
		view.setNameText(spell_name, is_domain);
		view.setDescText(spell_desc);
		view.setBackgroundColor(expended ? 0x40800000 : Color.TRANSPARENT); // ARGB
		return view;
	}
}
