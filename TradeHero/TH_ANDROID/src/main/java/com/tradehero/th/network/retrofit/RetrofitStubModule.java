package com.tradehero.th.network.retrofit;

import com.tradehero.th.network.service.DiscussionService;
import com.tradehero.th.network.service.stub.DiscussionServiceStub;
import com.tradehero.th.network.service.MessageService;
import com.tradehero.th.network.service.stub.MessageServiceStub;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Created with IntelliJ IDEA. User: tho Date: 1/27/14 Time: 11:39 AM Copyright (c) TradeHero
 */

@Module(
        includes = {
        },
        injects = {
        },
        overrides = true,
        complete = false,
        library = true
)
public class RetrofitStubModule
{
    @Provides @Singleton MessageService provideMessageServiceStub(MessageServiceStub messageService)
    {
        return messageService;
    }

    @Provides @Singleton DiscussionService provideDiscussionServiceStub(DiscussionServiceStub discussionService)
    {
        return discussionService;
    }
}
