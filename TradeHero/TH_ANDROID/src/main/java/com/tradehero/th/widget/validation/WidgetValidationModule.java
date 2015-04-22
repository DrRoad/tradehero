package com.tradehero.th.widget.validation;

import dagger.Module;

@Module(
        injects = {
                DisplayNameValidatedText.class,
                MatchingPasswordText.class,
                PasswordValidatedText.class,
        },
        library = true,
        complete = false
)
public class WidgetValidationModule
{
}
