package it.meridian.spellbook35;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.Objects;

import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellAssign;
import it.meridian.spellbook35.views.ViewSpellKnownGroup;


public class FragmentAssignSpell extends android.support.v4.app.Fragment implements AdapterExpandableList.ISupplier
{
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
	
	
	
	
	
	protected AdapterExpandableList adapter;
	protected String character;
	
	protected FragmentSpellInfo frag_spell_info;
	protected FragmentSpellSources frag_spell_sources;
	
	
	
	public FragmentAssignSpell()
	{
		this.frag_spell_info = new FragmentSpellInfo();
		this.frag_spell_sources = new FragmentSpellSources();
		this.adapter = new AdapterExpandableList(this, null);
	}
	
	
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		String character = (String) this.getArguments().get(FragmentCharacter.ARG_KEY_CHARACTER);
		if(!Objects.equals(this.character, character))
		{
			this.character = character;
			this.refresh();
		}
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setOnKeyListener(this::onKeyListener);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setAdapter(this.adapter);
		return list_view;
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.character);
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// Do not create a menu
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_add:
			{
				this.frag_spell_sources.setChooserTargetFragment(this);
				// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
				this.getParentFragment().getFragmentManager().beginTransaction()
						.replace(R.id.activity_main_content, this.frag_spell_sources)
						.addToBackStack(null)
						.commit();
			}
			break;
		}
		return true;
	}
	
	
	protected boolean onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		Bundle args = new Bundle(1);
		args.putString(FragmentSpellInfo.ARG_KEY_SPELL, spell_name);
		this.frag_spell_info.setArguments(args);
		
		// NOTE: Needs to use getParentFragment because this fragment's manager is the ViewPager's childManager
		this.getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_spell_info)
				.addToBackStack(null)
				.commit();
		return true;
	}
	
	
	private void onClickButtonAssign(ViewSpellAssign view, int groupPosition, int childPosition)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		try
		{
			ISpellChoiceListener listener = (ISpellChoiceListener)this.getTargetFragment();
			listener.onSpellChoiceResult(spell_name);
		}
		catch(ClassCastException ignored)
		{
			Toast.makeText(this.getContext(),
			               "ERROR: Target fragment does not implement IResultListener",
			               Toast.LENGTH_SHORT).show();
		}
		
		this.getFragmentManager().popBackStack();
	}
	
	
	
	// Hack to call onCancelFragmentAssignSpell when the user presses BACK
	private boolean onKeyListener(View view, int keyCode, KeyEvent event)
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
	
	
	private void refresh()
	{
		Cursor groups_cursor = Application.query(QUERY_GROUPS,
		                                         this.character);
		this.adapter.swapGroupsCursor(groups_cursor);
	}
	
	
	@Override
	public long getExpandableListGroupId(Cursor group_cursor, int groupPosition)
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
	public long getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		int known_id = Utils.CursorGetInt(child_cursor, "id");
		return known_id;
	}
	
	
	@Override
	public Cursor getExpandableListChildrenCursor(Cursor group_cursor)
	{
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		Cursor children_cursor = Application.query(QUERY_CHILDREN,
		                                           this.character,
		                                           source_name,
		                                           Integer.toString(spell_level));
		return children_cursor;
	}
	
	
	@Override
	public View getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewSpellKnownGroup view = (ViewSpellKnownGroup) convertView;
		if(view == null)
			view = new ViewSpellKnownGroup(this.getContext());
		
		String source_name = Utils.CursorGetString(group_cursor, "source_name");
		int spell_level = Utils.CursorGetInt(group_cursor, "spell_level");
		int known_amount = Utils.CursorGetInt(group_cursor, "spell_count");
		
		view.setTextSource(source_name);
		view.setTextLevel(Integer.toString(spell_level));
		view.setTextAmount(Integer.toString(known_amount));
		
		return view;
	}
	
	
	@Override
	public View getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellAssign view = (ViewSpellAssign) convertView;
		if(view == null)
		{
			view = new ViewSpellAssign(this.getContext());
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
