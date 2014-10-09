package com.tradehero.th.fragments.social.hero;

import android.content.Context;
import com.tradehero.th.api.social.HeroDTOExtWrapper;
import com.tradehero.th.persistence.social.HeroType;
import javax.inject.Inject;

public class AllHeroFragment extends HeroesTabContentFragment
{
    @SuppressWarnings("UnusedDeclaration") @Inject Context doNotRemoveOrItFails;

    @Override protected HeroType getHeroType()
    {
        return HeroType.ALL;
    }

    @Override protected void display(HeroDTOExtWrapper heroDTOExtWrapper)
    {
        display(heroDTOExtWrapper.allActiveHeroes);
    }
}