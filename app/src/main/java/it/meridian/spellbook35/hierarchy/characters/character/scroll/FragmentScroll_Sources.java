package it.meridian.spellbook35.hierarchy.characters.character.scroll;


import it.meridian.spellbook35.R;
import it.meridian.spellbook35.hierarchy.characters.character.known.FragmentLearn_Sources;


public class FragmentScroll_Sources extends FragmentLearn_Sources
{
	public @Override
	void
	onStart()
	{
		super.onStart();
		this.getActivity().setTitle(R.string.add_scroll);
	}
}
