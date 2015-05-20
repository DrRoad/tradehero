package com.tradehero.th.fragments.trade;

import dagger.Module;

@Module(
        injects = {
                BuySellStockFragmentTest.class,
                TradeListFragmentTest.class,

                AbstractTransactionDialogFragmentTestBase.class,
                BuyDialogFragmentTest.class,
        },
        complete = false,
        library = true
)
public class FragmentTradeUITestModule
{
}