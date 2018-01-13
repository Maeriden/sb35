package it.meridian.spellbook35.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.meridian.spellbook35.R;


public class FragmentCharacter extends it.meridian.spellbook35.Fragment
{
	static public final String ARG_CHARACTER_NAME = "character_name";
	
	
	static public
	Fragment
	newInstance(String character_name)
	{
		Fragment fragment = new FragmentCharacter();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
		}
		return fragment;
	}
	
	
	private AdapterPager adapter;
	private String       character_name;
	
	
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.character_name = this.getArguments().getString(ARG_CHARACTER_NAME);
		this.adapter = new AdapterPager(this.getChildFragmentManager(),
		                                FragmentCharacterSpellSlots.newInstance(this.character_name),
		                                FragmentCharacterSpellKnown.newInstance(this.character_name));
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewPager pager = (ViewPager) inflater.inflate(R.layout.fragment_character, container, false);
		pager.setAdapter(this.adapter);
		return pager;
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.character_name);
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
					return getContext().getResources().getString(R.string.page_spell_slots);
				case 1:
					return getContext().getResources().getString(R.string.page_spell_known);
			}
			
			return null;
		}
	}
}
