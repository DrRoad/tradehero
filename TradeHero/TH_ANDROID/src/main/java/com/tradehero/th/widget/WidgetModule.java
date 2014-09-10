package com.tradehero.th.widget;

import dagger.Module;

/**
 * Created by tho on 9/9/2014.
 */
@Module(
        injects = {
                ServerValidatedUsernameText.class,
                MarkdownTextView.class,
                VotePair.class,
        },
        library = true,
        complete = false
)
public class WidgetModule
{
}
