package it.meridian.spellbook35;


import it.meridian.spellbook35.activities.ActivityMain;


public abstract class Fragment extends android.support.v4.app.Fragment
{
	public
	ActivityMain
	activity_main()
	{
		return (ActivityMain)this.getActivity();
	}
}
