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

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.characters.character.known.FragmentLearn_Sources;
import it.meridian.spellbook35.hierarchy.FragmentSpellInfo;
import it.meridian.spellbook35.hierarchy.ISpellSelectionListener;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.SpellSelectionInfo;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellKnownChild;
import it.meridian.spellbook35.views.ViewSpellKnownGroup;
import it.meridian.spellbook35.views.ViewIntPicker;


public class FragmentCharacterSpellKnown extends it.meridian.spellbook35.Fragment
                                         implements AdapterExpandableList.ISupplier,
                                                    ISpellSelectionListener
{
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	static protected final String QUERY_GROUPS =
			"  SELECT known.rowid  AS id,          " + // TODO: Maybe remove rowid
			"         known.source AS source_name, " +
			"         known.level  AS spell_level, " +
			"         COUNT(*)     AS spell_count  " +
			"    FROM character_spell_known known  " +
			"   WHERE character = ?                " +
			"GROUP BY known.source, known.level    " +
			"ORDER BY known.source, known.level";
	
	static protected final String QUERY_CHILDREN =
			"SELECT known.rowid                AS id,                   " +
			"       known.spell                AS spell_name,           " +
			"       known.study_remaining_time AS study_remaining_time, " +
			"       known.copy_remaining_time  AS copy_remaining_time   " +
			"  FROM character_spell_known known                         " +
			" WHERE known.character = ?                                 " +
			"   AND known.source = ?                                    " +
			"   AND known.level = ?";
	
	static private final String DELETE_WHERE =
			"    character = ?   " +
			"AND source = ? " +
			"AND spell = ?";
	
	
	static public
	Fragment
	newInstance(String character_name)
	{
		FragmentCharacterSpellKnown fragment = new FragmentCharacterSpellKnown();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
		}
		return fragment;
	}
	
	
	
	
	protected AdapterExpandableList adapter;
	private String character_name;
	private String dialog_source_name;
	private String dialog_spell_name;
	private int    dialog_max_value;
	private String dialog_update_key;
	
	
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
		inflater.inflate(R.menu.options_character_known, menu);
	}
	
	
	public @Override
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_add:
			{
				Fragment fragment = FragmentLearn_Sources.newInstance(this.character_name, this);
				this.activity_main().push_fragment(fragment);
			}
			break;
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
			this.getActivity().getMenuInflater().inflate(R.menu.context_character_known, menu);
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
		
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		switch(item.getItemId())
		{
			case R.id.menu_action_delete:
			{
				long affected = Application.delete("character_spell_known",
				                                   DELETE_WHERE,
				                                   this.character_name,
				                                   source_name,
				                                   spell_name);
				if(affected > 0)
					this.refresh();
			} break;
			
			case R.id.menu_action_study_spell:
			{
				int current_value  = Utils.CursorGetInt(child_cursor, "study_remaining_time");
				this.show_picker_dialog("Set study time", 8 - current_value, 8, source_name, spell_name, "study_remaining_time");
			} break;
			
			case R.id.menu_action_copy_spell:
			{
				int current_value  = Utils.CursorGetInt(child_cursor, "copy_remaining_time");
				this.show_picker_dialog("Set copy time", 24 - current_value, 24, source_name, spell_name, "copy_remaining_time");
			} break;
		}
		return true;
	}
	
	
	private
	void
	show_picker_dialog(String title, int current_value, int max_value, String source_name, String spell_name, String update_key)
	{
		this.dialog_source_name = source_name;
		this.dialog_spell_name  = spell_name;
		this.dialog_max_value   = max_value;
		this.dialog_update_key  = update_key;
		
		ViewIntPicker picker = new ViewIntPicker(this.getContext());
		{
			picker.setMinValue(0);
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
			AlertDialog alert = (AlertDialog)dialog;
			ViewIntPicker picker = alert.findViewById(R.id.view_int_picker);
			
			ContentValues values = new ContentValues(1);
			values.put(this.dialog_update_key, this.dialog_max_value - picker.getValue());
			Application.update("character_spell_known",
			                   values,
			                   "character = ? AND source = ? AND spell = ?",
			                   this.character_name, this.dialog_source_name, this.dialog_spell_name);
			this.refresh();
		}
		
		this.dialog_source_name = null;
		this.dialog_spell_name  = null;
		this.dialog_max_value   = 0;
		this.dialog_update_key  = null;
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
		long affected = 0;
		for(SpellSelectionInfo choice : selection)
		{
			ContentValues values = new ContentValues(4);
			values.put("character", this.character_name);
			values.put("source",    choice.source_name);
			values.put("spell",     choice.spell_name);
			values.put("level",     choice.spell_level);
			
			affected += Application.insert("character_spell_known", values);
		}
		
		if(affected > 0)
		{
			this.refresh();
		}
		
		this.activity_main().pop_fragment();
		this.activity_main().pop_fragment();
	}
	
	
	public @Override
	void
	on_spell_selection_cancel()
	{
		// TODO: Test if necessary
		this.activity_main().pop_fragment();
		this.activity_main().pop_fragment();
	}
	
	
	public @Override
	long
	getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
//		int source_id = Utils.CursorGetInt(group_cursor, "source_id");
//		int spell_level = Utils.CursorGetInt(group_cursor, "level");
//		source_id = source_id & 0xFFFF0000;
//		spell_level = spell_level & 0xFFFF0000;
//		return (source_id << 16) | spell_level;
		
		String source = Utils.CursorGetString(group_cursor, "source_name");
		int level = Utils.CursorGetInt(group_cursor, "spell_level");
		return (source + level).hashCode();
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
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		return Application.query(QUERY_CHILDREN,
		                         this.character_name,
		                         source_name,
		                         Integer.toString(spell_level));
	}
	
	
	@SuppressLint("SetTextI18n")
	public @Override
	View
	getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewSpellKnownGroup view = (ViewSpellKnownGroup) convertView;
		if(view == null)
		{
			view = new ViewSpellKnownGroup(this.getContext());
		}
		
		String source_name  = Utils.CursorGetString(group_cursor, "source_name");
		int    spell_level  = Utils.CursorGetInt(group_cursor, "spell_level");
		int    known_amount = Utils.CursorGetInt(group_cursor, "spell_count");
		view.set_content(source_name, spell_level, known_amount);
		return view;
	}
	
	
	@SuppressLint("DefaultLocale")
	public @Override
	View
	getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellKnownChild view = (ViewSpellKnownChild) convertView;
		if(view == null)
		{
			view = new ViewSpellKnownChild(this.getContext());
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		int study_remaining_time = Utils.CursorGetInt(child_cursor, "study_remaining_time");
		int copy_remaining_time  = Utils.CursorGetInt(child_cursor, "copy_remaining_time");
		view.set_content(spell_name, study_remaining_time, copy_remaining_time);
		return view;
	}
}
