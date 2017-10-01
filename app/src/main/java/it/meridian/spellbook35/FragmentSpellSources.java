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


public class FragmentSpellSources extends Fragment
{
	private AdapterPager adapter;
	private FragmentSpellSourcesClasses frag_sources_classes;
	private FragmentSpellSourcesDomains frag_sources_domains;
	private FragmentSpellSourcesFeats   frag_sources_feats;
	
	
	
	public FragmentSpellSources()
	{
		this.frag_sources_classes = new FragmentSpellSourcesClasses();
		this.frag_sources_domains = new FragmentSpellSourcesDomains();
		this.frag_sources_feats   = new FragmentSpellSourcesFeats();
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
		                                this.frag_sources_classes,
		                                this.frag_sources_domains,
		                                this.frag_sources_feats);
		
//		Bundle args = this.getArguments();
//		this.frag_sources_classes.setArguments(args);
//		this.frag_sources_domains.setArguments(args);
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
		this.getActivity().setTitle(R.string.learn_spell);
	}
	
	/**
	 * Set a fragment to pass on to the actual spell chooser fragment
	 * so that it can communicate back the selection
	 * @param target
	 */
	public void setChooserTargetFragment(Fragment target)
	{
		this.frag_sources_classes.setChooserTargetFragment(target);
		this.frag_sources_domains.setChooserTargetFragment(target);
		this.frag_sources_feats.setChooserTargetFragment(target);
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
