package it.meridian.spellbook35;

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

import java.util.ArrayList;
import java.util.Objects;

import it.meridian.spellbook35.utils.AdapterExpandableList;
import it.meridian.spellbook35.utils.Utils;
import it.meridian.spellbook35.views.ViewSpellLearnable;
import it.meridian.spellbook35.views.ViewSpellSlotGroup;


public class FragmentSpellSource extends android.support.v4.app.Fragment implements AdapterExpandableList.ISupplier
{
	static public final String ARG_KEY_SOURCE = "source";
	static public final String ARG_KEY_CHARACTER = "character";
	
	static protected final String QUERY_GROUPS =
			"  SELECT spells.source AS source, " +
			"         spells.level  AS level,  " +
			"         COUNT(*)      AS count   " +
			"    FROM source_spells spells     " +
			"   WHERE spells.source = ?        " +
			"   AND NOT EXISTS (SELECT 1                               " +
			"                     FROM character_spell_known known     " +
			"                    WHERE known.character = ?             " +
			"                      AND known.spell     = spells.spell  " +
			"                      AND known.source    = spells.source " +
			"                      AND known.level     = spells.level) " +
			"GROUP BY spells.level";
	
	static protected final String QUERY_CHILDREN =
			"SELECT spell.rowid   AS spell_id,   " +
			"       spell.name    AS spell_name, " +
			"       spell.summary AS spell_desc  " +
	        "  FROM source_spells spells,        " +
			"       spell         spell          " +
	        " WHERE spells.spell = spell.name    " +
	        "   AND spells.source = ?            " +
	        "   AND spells.level = ?             " +
			"   AND spell.disabled = 0";
	
	static protected final String QUERY_CHILDREN_2 =
			"SELECT spell.id      AS spell_id,   " +
			"       spell.name    AS spell_name, " +
			"       spell.summary AS spell_desc  " +
			"  FROM source_spells spells,        " +
			"       spell         spell          " +
			" WHERE spells.spell   = spell.name  " +
			"   AND spells.source  = ?           " +
			"   AND spells.level   = ?           " +
			"   AND spell.disabled = 0           " +
			"   AND NOT EXISTS (SELECT 1                               " +
			"                     FROM character_spell_known known     " +
			"                    WHERE known.character = ?             " +
			"                      AND known.spell     = spells.spell  " +
			"                      AND known.source    = spells.source " +
			"                      AND known.level     = spells.level) ";
	
	
	private String source_name;
	private String character_name;
	private AdapterExpandableList adapter;
	private FragmentSpellInfo frag_spell_info;
	private ArrayList<LearnedSpell> choices;
	
	
	
	public FragmentSpellSource()
	{
		this.frag_spell_info = new FragmentSpellInfo();
		this.adapter = new AdapterExpandableList(this, null);
		this.choices = new ArrayList<>();
	}
	
	
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.choices.clear();
		boolean do_refresh = false;
		
		String source_name = (String) this.getArguments().get(ARG_KEY_SOURCE);
		if(!Objects.equals(this.source_name, source_name))
		{
			this.source_name = source_name;
			do_refresh = true;
		}
		
		String character_name = (String) this.getArguments().get(ARG_KEY_CHARACTER);
		if(!Objects.equals(this.character_name, character_name))
		{
			this.character_name = character_name;
			do_refresh = true;
		}
		
		// FIXME: This is a hack. Properly set up parent views to pass down the current character
		if(this.character_name == null)
		{
			this.character_name = Application.current_character;
		}
		
		if(do_refresh)
		{
			Cursor groups_cursor = Application.query(QUERY_GROUPS, this.source_name, this.character_name);
			this.adapter.swapGroupsCursor(groups_cursor);
		}
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
	{
		ExpandableListView list_view = new ExpandableListView(this.getContext());
		list_view.setId(R.id.expandable_list_view);
		list_view.setOnChildClickListener(this::onClickExpandableListItem);
		list_view.setAdapter(this.adapter);
		list_view.setOnKeyListener(this::onKeyListener);
		return list_view;
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_spell_learn, menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.menu_action_done:
			{
				this.onClickDone();
			}
			break;
		}
		return true;
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.source_name);
	}
	
	
	private boolean onClickExpandableListItem(ExpandableListView list_view, View item, int groupPosition, int childPosition, long id)
	{
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		
		Bundle args = new Bundle(1);
		args.putString(FragmentSpellInfo.ARG_KEY_SPELL, spell_name);
		this.frag_spell_info.setArguments(args);
		
		this.getFragmentManager().beginTransaction()
				.replace(R.id.activity_main_content, this.frag_spell_info)
				.addToBackStack(null)
				.commit();
		return true;
	}
	
	
	private void onClickCheckbox(ViewSpellLearnable view, int groupPosition, int childPosition, boolean isChecked)
	{
		Cursor group_cursor = (Cursor) this.adapter.getGroup(groupPosition);
		Cursor child_cursor = (Cursor) this.adapter.getChild(groupPosition, childPosition);
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		int level = Utils.CursorGetInt(group_cursor, "level");
		
		LearnedSpell item = new LearnedSpell();
		item.source_name = this.source_name;
		item.spell_level = level;
		item.spell_name = spell_name;
		
		if(isChecked)
		{
			this.choices.add(item);
		}
		else
		{
			this.choices.remove(item);
		}
	}
	
	
	private void onClickDone()
	{
		if(this.choices.isEmpty())
			return;
		
		try
		{
			ILearnSpellChoiceListener listener = (ILearnSpellChoiceListener)this.getTargetFragment();
			listener.onLearnSpellChoiceResult(this.choices);
			
			this.getFragmentManager().popBackStack();
			this.getFragmentManager().popBackStack();
		}
		catch(ClassCastException ignored)
		{
			Toast.makeText(this.getContext(),
			               "ERROR: Target fragment does not implement IResultListener",
			               Toast.LENGTH_SHORT).show();
		}
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
					ILearnSpellChoiceListener listener = (ILearnSpellChoiceListener)this.getTargetFragment();
					listener.onLearnSpellChoiceCancel();
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
	
	
	@Override
	public long getExpandableListGroupId(Cursor group_cursor, int groupPosition)
	{
		int level = Utils.CursorGetInt(group_cursor, "level");
		return level;
	}
	
	@Override
	public long getExpandableListChildId(Cursor child_cursor, int groupPosition, int childPosition)
	{
		int spell_id = Utils.CursorGetInt(child_cursor, "spell_id");
		return spell_id;
	}
	
	@Override
	public Cursor getExpandableListChildrenCursor(Cursor group_cursor)
	{
		int level = Utils.CursorGetInt(group_cursor, "level");
		Cursor children_cursor = Application.query(QUERY_CHILDREN_2,
		                                           this.source_name,
		                                           Integer.toString(level),
		                                           this.character_name);
		return children_cursor;
	}
	
	@Override
	public View getExpandableListGroupView(Cursor group_cursor, int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewSpellSlotGroup view = (ViewSpellSlotGroup) convertView;
		if(view == null)
			view = new ViewSpellSlotGroup(this.getContext());
		
		int level = Utils.CursorGetInt(group_cursor, "level");
		int count = Utils.CursorGetInt(group_cursor, "count");
		
		String level_text = this.getContext().getResources().getString(R.string.level_N, level);
		String count_text = Integer.toString(count);
		
		view.setLevelText(level_text);
		view.setCountText(count_text);
		return view;
	}
	
	@Override
	public View getExpandableListChildView(Cursor child_cursor, int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		ViewSpellLearnable view = (ViewSpellLearnable) convertView;
		if(view == null)
		{
			view = new ViewSpellLearnable(this.getContext());
			view.setCheckboxStateChangeListener(this::onClickCheckbox);
		}
		
		String spell_name = Utils.CursorGetString(child_cursor, "spell_name");
		String spell_desc = Utils.CursorGetString(child_cursor, "spell_desc");
		
		view.setTextName(spell_name);
		view.setTextDesc(spell_desc);
		view.setPosition(groupPosition, childPosition);
		
		boolean isChecked = false;
		for(LearnedSpell choice : this.choices)
		{
			if(choice.spell_name.equals(spell_name))
			{
				isChecked = true;
				break;
			}
		}
		view.setCheckboxState(isChecked);
		
		return view;
	}
	
	
	public class LearnedSpell
	{
		public String source_name;
		public int spell_level;
		public String spell_name;
	}
	
	public interface ILearnSpellChoiceListener
	{
		void onLearnSpellChoiceResult(ArrayList<LearnedSpell> choices);
		void onLearnSpellChoiceCancel();
	}
}
