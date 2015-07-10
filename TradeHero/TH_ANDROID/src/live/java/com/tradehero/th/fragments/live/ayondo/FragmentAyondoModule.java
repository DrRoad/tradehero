package com.tradehero.th.fragments.live.ayondo;

import dagger.Module;

@Module(
        injects = {
                LiveSignUpStep1AyondoFragment.class,
                LiveSignUpStep2AyondoFragment.class,
                LiveSignUpStep3AyondoFragment.class,
                LiveSignUpStep4AyondoFragment.class,
                LiveSignUpStep5AyondoFragment.class,
        },
        library = true,
        complete = false
)
public class FragmentAyondoModule
{
}