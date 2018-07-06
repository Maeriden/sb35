package it.meridian.spellbook35.hierarchy.characters.character;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.utils.AdapterPager;


public class FragmentCharacter extends it.meridian.spellbook35.Fragment
{
	static public final String ARG_CHARACTER_NAME = "character_name";
	
	
	static public
	Fragment
	newInstance(String character_name)
	{
		FragmentCharacter fragment = new FragmentCharacter();
		{
			Bundle args = new Bundle(1);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
		}
		return fragment;
	}
	
	
	private PagerAdapter adapter;
	private String       character_name;
	
	
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.character_name = this.getArguments().getString(ARG_CHARACTER_NAME);
		
		Fragment[] fragments = new Fragment[] {FragmentCharacterScrolls.newInstance(this.character_name),
		                                       FragmentCharacterSpellSlots.newInstance(this.character_name),
		                                       FragmentCharacterSpellKnown.newInstance(this.character_name)};
		String[]   titles    = new String[]   {this.getString(R.string.scrolls),
		                                       this.getString(R.string.slots),
		                                       this.getString(R.string.known)};
		this.adapter = new AdapterPager(this.getChildFragmentManager(), fragments, titles);
	}
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewPager pager = (ViewPager) inflater.inflate(R.layout.fragment_pager, container, false);
		pager.setAdapter(this.adapter);
		pager.setCurrentItem(1);
		return pager;
	}
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		this.getActivity().setTitle(this.character_name);
	}
}
