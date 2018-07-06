package it.meridian.spellbook35.utils;


import android.support.v4.app.FragmentManager;

import it.meridian.spellbook35.Fragment;


public class AdapterPager extends android.support.v4.app.FragmentPagerAdapter
{
	protected final Fragment[] fragments;
	protected final String[]   titles;
	
	
	public
	AdapterPager(FragmentManager fragmentManager, Fragment[] fragments, String[] titles)
	{
		super(fragmentManager);
		this.fragments = fragments;
		this.titles    = titles;
	}
	
	
	@Override
	public
	int
	getCount()
	{
		return this.fragments.length;
	}
	
	
	@Override
	public
	Fragment
	getItem(int i)
	{
		if(0 <= i && i < this.fragments.length)
			return this.fragments[i];
		return null;
	}
	
	
	@Override
	public
	CharSequence
	getPageTitle(int position)
	{
		if(0 <= position && position < this.titles.length)
			return this.titles[position];
		return null;
	}
}
