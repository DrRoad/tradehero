package com.tradehero.th.models.push.baidu.baidu;

import dagger.Module;

@Module(
        injects = {
                BaiduPushMessageDTOTest.class,
                BaiduPushMessageReceiverTest.class,
                BaiduIntentReceiverTest.class
        },
        complete = false,
        library = true
)
public class BaiduTestModule
{
}
