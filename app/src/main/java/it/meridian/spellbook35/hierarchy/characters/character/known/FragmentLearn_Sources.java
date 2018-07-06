package it.meridian.spellbook35.hierarchy.characters.character.known;


import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

import it.meridian.spellbook35.Fragment;
import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.ISpellSelectionListener;
import it.meridian.spellbook35.hierarchy.characters.character.known.learn.FragmentLearn_Source_Spells;
import it.meridian.spellbook35.hierarchy.ISourceSelectionListener;
import it.meridian.spellbook35.utils.AdapterPager;


public class FragmentLearn_Sources extends it.meridian.spellbook35.Fragment
		implements ISourceSelectionListener
{
	static private final String ARG_CHARACTER_NAME = "character_name";
	
	
	static public
	Fragment
	newInstance(String character_name, ISpellSelectionListener callback)
	{
		FragmentLearn_Sources fragment = new FragmentLearn_Sources();
		{
			Bundle args = new Bundle(2);
			args.putString(ARG_CHARACTER_NAME, character_name);
			fragment.setArguments(args);
			fragment.callback = callback;
		}
		return fragment;
	}
	
	
	private PagerAdapter            adapter;
	private String                  character_name;
	private ISpellSelectionListener callback;
	
	
	public @Override @CallSuper
	void
	onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(false);
		
		this.character_name = this.getArguments().getString(ARG_CHARACTER_NAME);
		
		Fragment[] fragments = new Fragment[] {
				FragmentLearn_SourcesClasses.newInstance(),
				FragmentLearn_SourcesDomains.newInstance(),
				FragmentLearn_SourcesFeats.newInstance()
		};
		String[] titles = new String[] {
				this.getString(R.string.classes),
				this.getString(R.string.domains),
				this.getString(R.string.feats)
		};
		this.adapter = new AdapterPager(this.getChildFragmentManager(), fragments, titles);
	}
	
	
	public @Override
	@Nullable View
	onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewPager pager = (ViewPager) inflater.inflate(R.layout.fragment_pager, container, false);
		pager.setAdapter(this.adapter);
		return pager;
	}
	
	
	public @Override
	void
	onStart()
	{
		super.onStart();
		this.getActivity().setTitle(R.string.learn_spells);
	}
	
	
	public @Override
	void
	on_source_selection_accept(Collection<String> selection)
	{
		String source_name = new ArrayList<>(selection).get(0);
		Fragment fragment = FragmentLearn_Source_Spells.newInstance(source_name, this.character_name, this.callback);
		this.activity_main().push_fragment(fragment);
	}
}
