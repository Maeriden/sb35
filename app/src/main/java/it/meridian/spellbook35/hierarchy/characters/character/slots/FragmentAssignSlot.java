package it.meridian.spellbook35.hierarchy.characters.character.slots;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Collection;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.FragmentSpellInfo;
import it.meridian.spellbook35.hierarchy.ISpellSelectionListener;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.SpellSelectionInfo;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewAssignSlotChild;
import it.meridian.spellbook35.views.ViewAssignSlotGroup;


public class FragmentAssignSlot extends it.meridian.spellbook35.Fragment implements AdapterExpandableList.ISupplier
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
			"SELECT known.rowid   AS id,         " +
			"       known.spell   AS spell_name, " +
			"       spell.summary AS spell_desc  " +
			"  FROM character_spell_known known, " +
			"       spell                 spell  " +
			" WHERE known.spell = spell.name     " +
			"   AND known.character = ?          " +
			"   AND known.source = ?             " +
			"   AND known.level = ?";
	
	
	static public
	Fragment
	newInstance(String character_name, ISpellSelectionListener callback)
	{
		FragmentAssignSlot fragment = new FragmentAssignSlot();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
			fragment.callback = callback;
		}
		return fragment;
	}
	
	
	protected AdapterExpandableList   adapter;
	protected String                  character_name;
	protected ISpellSelectionListener callback;
	
	
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
		list_view.setOnKeyListener(this::onKeyListener);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	@Override
	public
	void
	onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.getString(R.string.assign_spell));
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
	
	
	private
	void
	onClickButtonAssign(ViewAssignSlotChild view, int group_position, int child_position)
	{
		Cursor group_cursor = (Cursor) this.adapter.getGroup(group_position);
		Cursor child_cursor = (Cursor) this.adapter.getChild(group_position, child_position);
		
		Collection<SpellSelectionInfo> selection = new ArrayList<>(1);
		
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int    spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		String spell_name  = Utils.CursorGetString(child_cursor, "spell_name");
		
		selection.add(new SpellSelectionInfo(source_name, spell_level, spell_name));
		this.callback.on_spell_selection_accept(selection);
		this.activity_main().pop_fragment();
	}
	
	
	private
	boolean
	onKeyListener(View view, int keyCode, KeyEvent event)
	{
		// Hack to call on_assign_slot_cancel() when the user presses BACK
		if(event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK)
		{
			this.callback.on_spell_selection_cancel();
		}
		return false;
	}
	
	
	public
	void
	refresh()
	{
		Cursor groups_cursor = Application.query(QUERY_GROUPS, this.character_name);
		this.adapter.swapGroupsCursor(groups_cursor);
	}
	
	
	@Override
	public
	long
	getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		String source = Utils.CursorGetString(group_cursor, "source_name");
		int level = Utils.CursorGetInt(group_cursor, "spell_level");
		return (source + level).hashCode();
	}
	
	
	@Override
	public
	long
	getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		return Utils.CursorGetInt(child_cursor, "id");
	}
	
	
	@Override
	public
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
	@Override
	public
	View
	getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewAssignSlotGroup view = (ViewAssignSlotGroup) convertView;
		if(view == null)
		{
			view = new ViewAssignSlotGroup(this.getContext());
		}
		
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level    = Utils.CursorGetInt(group_cursor, "spell_level");
		int known_amount   = Utils.CursorGetInt(group_cursor, "spell_count");
		view.set_content(source_name, spell_level, known_amount);
		return view;
	}
	
	
	@Override
	public
	View
	getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewAssignSlotChild view = (ViewAssignSlotChild) convertView;
		if(view == null)
		{
			view = new ViewAssignSlotChild(this.getContext());
			view.set_button_click_listener(this::onClickButtonAssign);
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		view.set_content(spell_name, spell_desc);
		view.set_position(groupPosition, childPosition);
		return view;
	}
}
