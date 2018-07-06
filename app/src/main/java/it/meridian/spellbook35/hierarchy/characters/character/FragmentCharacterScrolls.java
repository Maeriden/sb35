package it.meridian.spellbook35.hierarchy.characters.character;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.Collection;
import java.util.Objects;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.FragmentSpellInfo;
import it.meridian.spellbook35.hierarchy.ISpellSelectionListener;
import it.meridian.spellbook35.hierarchy.characters.character.known.FragmentLearn_Sources;
import it.meridian.spellbook35.hierarchy.characters.character.scroll.FragmentScroll_Sources;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.SpellSelectionInfo;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewIntPicker;
import it.meridian.spellbook35.views.ViewScrollGroup;
import it.meridian.spellbook35.views.ViewSpellSlotChild;


public class FragmentCharacterScrolls extends Fragment implements AdapterExpandableList.ISupplier,
                                                                  ISpellSelectionListener
{
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	static private final String QUERY_GROUPS =
			"  SELECT scroll.type         AS scroll_type,  " +
			"         scroll.caster_level AS caster_level, " +
			"         COUNT(*)            AS scroll_count  " +
			"    FROM character_scroll scroll              " +
			"   WHERE scroll.character = ?                 " +
			"GROUP BY scroll.type,                         " +
			"         scroll.caster_level                  " +
			"ORDER BY scroll.type,                         " +
			"         scroll.caster_level";
	
	static private final String QUERY_CHILDREN =
			"   SELECT scroll.id      AS id,         " +
			"          scroll.spell   AS spell_name, " +
			"          spell.summary  AS spell_desc  " +
			"     FROM character_scroll scroll       " +
			"LEFT JOIN spell                         " +
			"       ON scroll.spell = spell.name     " +
			"    WHERE scroll.character = ?          " +
			"      AND scroll.caster_level = ?       " +
			" ORDER BY scroll.spell";
	
	static private final String WHERE_CLAUSE =
			"    character = ? " +
			"AND id = ?";
	
	static private final String QUERY_NEXT_SCROLL_ID =
			"SELECT MAX(scroll.id)+1 AS next_id " +
			"  FROM character_scroll scroll     " +
			" WHERE scroll.character = ?";
	
	
	static public
	Fragment
	newInstance(String character_name)
	{
		FragmentCharacterScrolls fragment = new FragmentCharacterScrolls();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
		}
		return fragment;
	}
	
	
	
	
	protected AdapterExpandableList adapter;
	private String character_name;
	private int    picker_scroll_id;
	private String add_scroll_type;
	
	
	public @Override @CallSuper
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.character_name = this.getArguments().getString(FragmentCharacter.ARG_CHARACTER_NAME);
		
		Cursor groups_cursor = Application.query(QUERY_GROUPS, this.character_name);
		this.adapter = new AdapterExpandableList(this, groups_cursor);
	}
	
	
	public @Override
	@Nullable View
	onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setGroupIndicator(null);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setOnCreateContextMenuListener(this::onCreateExpandableListContextMenu);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	
	public @Override
	void
	onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_character_scroll, menu);
	}
	
	
	public @Override
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_add_arcane:
			{
				this.add_scroll_type = "arcane";
				Fragment fragment = FragmentScroll_Sources.newInstance(this.character_name, this);
				this.activity_main().push_fragment(fragment);
			} break;
			
			case R.id.menu_action_add_divine:
			{
				this.add_scroll_type = "divine";
				Fragment fragment = FragmentScroll_Sources.newInstance(this.character_name, this);
				this.activity_main().push_fragment(fragment);
			} break;
		}
		return true;
	}
	
	
	protected
	void
	onCreateExpandableListContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		
		int view_type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if(view_type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		{
			this.getActivity().getMenuInflater().inflate(R.menu.context_character_scroll, menu);
		}
	}
	
	
	public @Override
	boolean
	onContextItemSelected(MenuItem item)
	{
		// NOTE: For some reason this function is called even when another fragment is visible
		if(!this.getUserVisibleHint())
			return super.onContextItemSelected(item);
		
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		
		if(ExpandableListView.getPackedPositionType(info.packedPosition) != ExpandableListView.PACKED_POSITION_TYPE_CHILD)
			return false;
		
		int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
		Cursor group_cursor = (Cursor) this.adapter.getGroup(groupPosition);
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		
		int scroll_id = Utils.CursorGetInt(child_cursor, "id");
		
		switch(item.getItemId())
		{
			case R.id.menu_action_update:
			{
				int caster_level = Utils.CursorGetInt(group_cursor, "caster_level");
				this.show_picker_dialog("Set spell level", caster_level, 0, 20, scroll_id);
			} break;
			
			case R.id.menu_action_delete:
			{
				long affected = Application.delete("character_scroll", WHERE_CLAUSE,
				                                   this.character_name,
				                                   Integer.toString(scroll_id));
				if(affected > 0)
					this.refresh();
			} break;
		}
		return true;
	}
	
	
	private
	void
	show_picker_dialog(String title, int current_value, int min_value, int max_value, int scroll_id)
	{
		this.picker_scroll_id = scroll_id;
		
		ViewIntPicker picker = new ViewIntPicker(this.getContext());
		{
			picker.setId(R.id.view_int_picker);
			picker.setMinValue(min_value);
			picker.setMaxValue(max_value);
			picker.setValue(current_value);
			picker.setGravity(Gravity.CENTER_HORIZONTAL);
			
			// FIXME: This causes the view to expand, filling the screen
//			picker.setBackgroundResource(R.drawable.background);
		}
		
		AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this.getContext());
		{
			dialog_builder.setView(picker);
			dialog_builder.setPositiveButton(R.string.ok, this::on_picker_dialog_closed);
			dialog_builder.setNegativeButton(R.string.cancel, this::on_picker_dialog_closed);
			dialog_builder.setTitle(title);
		}
		dialog_builder.create().show();
	}
	
	
	private
	void
	on_picker_dialog_closed(DialogInterface dialog, int which)
	{
		if(which == DialogInterface.BUTTON_POSITIVE)
		{
			ViewIntPicker picker = ((AlertDialog)dialog).findViewById(R.id.view_int_picker);
			int caster_level = picker.getValue();
			
			ContentValues values = new ContentValues(1);
			values.put("caster_level", caster_level);
			long affected = Application.update("character_scroll", values, WHERE_CLAUSE,
			                                   this.character_name,
			                                   Integer.toString(this.picker_scroll_id));
			if(affected > 0)
				this.refresh();
		}
		
		this.picker_scroll_id = -1;
	}
	
	
	protected
	boolean
	onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		Fragment fragment = FragmentSpellInfo.newInstance(spell_name);
		this.activity_main().push_fragment(fragment);
		return true;
	}
	
	
	public
	void
	refresh()
	{
		Cursor groups_cursor = Application.query(QUERY_GROUPS, this.character_name);
		this.adapter.swapGroupsCursor(groups_cursor);
	}
	
	
	public @Override
	void
	on_spell_selection_accept(Collection<SpellSelectionInfo> selection)
	{
		if(selection.size() > 0)
		{
			Cursor cursor = Application.query(QUERY_NEXT_SCROLL_ID, this.character_name);
			cursor.moveToFirst();
			int next_id = Utils.CursorGetInt(cursor, "next_id");
			cursor.close();
			
			int scroll_type = Objects.equals(this.add_scroll_type, "arcane") ? 1 : 2;
			this.add_scroll_type = null;
			
			long affected = 0;
			for(SpellSelectionInfo choice : selection)
			{
				ContentValues values = new ContentValues(5);
				values.put("character",    this.character_name);
				values.put("id",           next_id++);
				values.put("type",         scroll_type);
				values.put("caster_level", 0);
				values.put("spell",        choice.spell_name);
				
				affected += Application.insert("character_scroll", values);
			}
			
			if(affected > 0)
				this.refresh();
		}
		
		this.activity_main().pop_fragment();
		this.activity_main().pop_fragment();
	}
	
	
	public @Override
	void
	on_spell_selection_cancel()
	{
		this.add_scroll_type = null;
		// TODO: Test if necessary
		this.activity_main().pop_fragment();
		this.activity_main().pop_fragment();
	}
	
	
	public @Override
	long
	getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		String type  = Utils.CursorGetString(group_cursor, "scroll_type");
		int    level = Utils.CursorGetInt(group_cursor,    "caster_level");
		return (type + level).hashCode();
	}
	
	
	public @Override
	long
	getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		return Utils.CursorGetInt(child_cursor, "id");
	}
	
	
	public @Override
	Cursor
	getExpandableListChildrenCursor(Cursor group_cursor)
	{
		int caster_level = Utils.CursorGetInt(group_cursor, "caster_level");
		return Application.query(QUERY_CHILDREN,
		                         this.character_name,
		                         Integer.toString(caster_level));
	}
	
	
	@SuppressLint("SetTextI18n")
	public @Override
	View
	getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewScrollGroup view = (ViewScrollGroup) convertView;
		if(view == null)
		{
			view = new ViewScrollGroup(this.getContext());
		}

		String scroll_type  = Utils.CursorGetInt(group_cursor, "scroll_type") == 1 ? "Arcane" : "Divine";
		int    caster_level = Utils.CursorGetInt(group_cursor, "caster_level");
		int    scroll_count = Utils.CursorGetInt(group_cursor, "scroll_count");
		view.set_content(scroll_type, caster_level, scroll_count);
		return view;
	}
	
	
	@SuppressLint("DefaultLocale")
	public @Override
	View
	getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellSlotChild view = (ViewSpellSlotChild) convertView;
		if(view == null)
		{
			view = new ViewSpellSlotChild(this.getContext());
		}
		
		String  spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String  spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		boolean is_domain  = false;
		boolean expended   = false;
		view.set_content(spell_name, spell_desc, is_domain, expended);
		
		return view;
	}
}
