package it.meridian.spellbook35.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import it.meridian.spellbook35.Application;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSlotAssign;
import it.meridian.spellbook35.views.ViewCharacterSpellKnownGroup;


public class FragmentSlotAssign extends it.meridian.spellbook35.Fragment implements AdapterExpandableList.ISupplier
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
	newInstance(String character_name, ISpellChoiceListener callback)
	{
		FragmentSlotAssign fragment = new FragmentSlotAssign();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
			fragment.callback = callback;
		}
		return fragment;
	}
	
	
	protected AdapterExpandableList adapter;
	protected String character_name;
	protected ISpellChoiceListener callback;
	
	
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
		String title = this.getString(R.string.assign_spell);
		this.getActivity().setTitle(title);
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
	onClickButtonAssign(ViewSlotAssign view, int groupPosition, int childPosition)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		try
		{
			ISpellChoiceListener listener = (ISpellChoiceListener)this.callback;
			listener.onSpellChoiceResult(spell_name);
		}
		catch(ClassCastException ignored)
		{
			Toast.makeText(this.getContext(), "ERROR: Target fragment does not implement ISpellChoiceListener", Toast.LENGTH_SHORT).show();
		}
		
		this.activity_main().pop_fragment();
	}
	
	
	// Hack to call onCancelFragmentAssignSpell when the user presses BACK
	private
	boolean
	onKeyListener(View view, int keyCode, KeyEvent event)
	{
		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			if(keyCode == KeyEvent.KEYCODE_BACK)
			{
				try
				{
					ISpellChoiceListener listener = (ISpellChoiceListener)this.getTargetFragment();
					listener.onSpellChoiceCancel();
				}
				catch(ClassCastException ignored)
				{
					Toast.makeText(this.getContext(),
					               "ERROR: Target fragment does not implement IResultListener",
					               Toast.LENGTH_SHORT).show();
				}
			}
		}
		return false;
	}
	
	
	private
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
//		int source_id = Utils.CursorGetInt(group_cursor, "source_id");
//		int spell_level = Utils.CursorGetInt(group_cursor, "level");
//		source_id = source_id & 0xFFFF0000;
//		spell_level = spell_level & 0xFFFF0000;
//		return (source_id << 16) | spell_level;
		
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
	
	
	@Override
	public
	View
	getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewCharacterSpellKnownGroup view = (ViewCharacterSpellKnownGroup) convertView;
		if(view == null)
			view = new ViewCharacterSpellKnownGroup(this.getContext());
		
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level    = Utils.CursorGetInt(group_cursor, "spell_level");
		int known_amount   = Utils.CursorGetInt(group_cursor, "spell_count");
		
		view.setTextSource(source_name);
		view.setTextLevel(Integer.toString(spell_level));
		view.setTextAmount(Integer.toString(known_amount));
		
		return view;
	}
	
	
	@Override
	public
	View
	getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSlotAssign view = (ViewSlotAssign) convertView;
		if(view == null)
		{
			view = new ViewSlotAssign(this.getContext());
			view.setButtonOnClickListener(this::onClickButtonAssign);
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		
		view.setTextName(spell_name);
		view.setTextDesc(spell_desc);
		view.setPosition(groupPosition, childPosition);
		
		return view;
	}
	
	
	
	
	public interface ISpellChoiceListener
	{
		void onSpellChoiceResult(String spell_name);
		void onSpellChoiceCancel();
	}
}
