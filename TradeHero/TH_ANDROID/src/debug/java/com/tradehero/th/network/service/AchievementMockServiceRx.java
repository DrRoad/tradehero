package com.tradehero.th.network.service;

import com.tradehero.th.api.BaseResponseDTO;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface AchievementMockServiceRx
{
    //<editor-fold desc="Get Mock Quest Bonus">
    @GET("/achievements/mockdaily/{contiguousCount}")
    Observable<BaseResponseDTO> getMockQuestBonus(
            @Path("contiguousCount") int contiguousCount,
            @Query("xpEarned") int xpEarned,
            @Query("xpTotal") int xpTotal);
    //</editor-fold>
}
