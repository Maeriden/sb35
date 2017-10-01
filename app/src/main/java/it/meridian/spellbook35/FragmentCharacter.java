package it.meridian.spellbook35;

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


public class FragmentCharacter extends android.support.v4.app.Fragment
{
	static public final String ARG_KEY_CHARACTER = "character";
	
	private AdapterPager adapter;
	private String character;
	private Fragment frag_character_slots;
	private Fragment frag_character_known;
	
	
	
	public FragmentCharacter()
	{
		this.frag_character_slots = new FragmentCharacterSpellSlots();
		this.frag_character_known = new FragmentCharacterSpellKnown();
	}
	
	/**
	 * Called to do initial creation of a fragment.  This is called after
	 * {@link #onAttach(Activity)} and before
	 * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 * <p>
	 * <p>Note that this can be called while the fragment's activity is
	 * still in the process of being created.  As such, you can not rely
	 * on things like the activity's content view hierarchy being initialized
	 * at this point.  If you want to do work once the activity itself is
	 * created, see {@link #onActivityCreated(Bundle)}.
	 *
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@CallSuper
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		this.adapter = new AdapterPager(this.getChildFragmentManager(),
		                                this.frag_character_slots,
		                                this.frag_character_known);
		
		Bundle args = this.getArguments();
		this.character = args.getString(ARG_KEY_CHARACTER);
		this.frag_character_slots.setArguments(args);
		this.frag_character_known.setArguments(args);
	}
	
	
	
	/**
	 * Called to have the fragment instantiate its user interface view.
	 * This is optional, and non-graphical fragments can return null (which
	 * is the default implementation).  This will be called between
	 * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
	 * <p>
	 * <p>If you return a View from here, you will later be called in
	 * {@link #onDestroyView} when the view is being released.
	 *
	 * @param inflater           The LayoutInflater object that can be used to inflate
	 *                           any views in the fragment,
	 * @param container          If non-null, this is the parent view that the fragment's
	 *                           UI should be attached to.  The fragment should not add the view itself,
	 *                           but this can be used to generate the LayoutParams of the view.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 *                           from a previous saved state as given here.
	 * @return Return the View for the fragment's UI, or null.
	 */
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
		this.getActivity().setTitle(this.character);
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
