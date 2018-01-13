package it.meridian.spellbook35.fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import it.meridian.spellbook35.R;


public class FragmentSpellSources extends it.meridian.spellbook35.Fragment
                                  implements ISpellSourceChoiceListener
{
	static public int LIST_ITEM_HEIGHT = 40;
	static public float LIST_ITEM_TEXT_HEIGHT = 14f;
	static private final String ARG_TITLE          = "title";
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	
	static public
	Fragment
	newInstance(String title, String character_name, FragmentSpellSource.ISpellChoiceListener callback, int request_code)
	{
		FragmentSpellSources fragment = new FragmentSpellSources();
		{
			Bundle args = new Bundle(2);
			args.putString(ARG_TITLE, title);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
			fragment.callback     = callback;
			fragment.request_code = request_code;
		}
		return fragment;
	}
	
	
	private AdapterPager                             adapter;
	private String                                   character_name;
	private FragmentSpellSource.ISpellChoiceListener callback;
	private int                                      request_code;
	private String                                   title;
	
	
	
	public @Override @CallSuper
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.character_name = this.getArguments().getString(ARG_CHARACTER_NAME);
		this.title          = this.getArguments().getString(ARG_TITLE);
		
		this.adapter = new AdapterPager(this.getChildFragmentManager(),
		                                FragmentSpellSourcesClasses.newInstance(),
		                                FragmentSpellSourcesDomains.newInstance(),
		                                FragmentSpellSourcesFeats  .newInstance());
		
	}
	
	
	public @Override
	@Nullable View
	onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewPager pager = (ViewPager) inflater.inflate(R.layout.fragment_character, container, false);
		pager.setAdapter(this.adapter);
		return pager;
	}
	
	
	public @Override
	void
	onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.options_spell_browser, menu);
	}
	
	
	@Override
	public
	boolean
	onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if(id == R.id.menu_action_search)
		{
			this.getActivity().onSearchRequested();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	
	public @Override
	void
	onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.title);
	}
	
	
	public @Override
	void
	on_source_selected(String source_name)
	{
//		Fragment target       = this.getTargetFragment();
//		int      request_code = this.getTargetRequestCode();
		
		Fragment fragment = FragmentSpellSource.newInstance(source_name, this.character_name, this.callback);
		this.activity_main().push_fragment(fragment);
	}
	
	
	
	
	private class AdapterPager extends android.support.v4.app.FragmentPagerAdapter
	{
		private final Fragment[] fragments;
		
		
		AdapterPager(FragmentManager fragmentManager, Fragment... fragments)
		{
			super(fragmentManager);
			this.fragments = fragments;
		}
		
		
		@Override
		public int getCount()
		{
			return this.fragments.length;
		}
		
		
		@Override
		public Fragment getItem(int i)
		{
			if(0 <= i && i < this.fragments.length)
				return this.fragments[i];
			return null;
		}
		
		
		@Override
		public CharSequence getPageTitle(int position)
		{
			switch(position)
			{
				case 0:
					return getContext().getResources().getString(R.string.classes);
				case 1:
					return getContext().getResources().getString(R.string.domains);
				case 2:
					return getContext().getResources().getString(R.string.feats);
			}
			
			return null;
		}
	}
}
