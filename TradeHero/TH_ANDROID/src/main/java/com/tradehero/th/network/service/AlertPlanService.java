package com.tradehero.th.network.service;

import com.tradehero.th.api.alert.AlertPlanDTO;
import com.tradehero.th.api.billing.PurchaseReportDTO;
import com.tradehero.th.api.users.RestorePurchaseForm;
import com.tradehero.th.api.users.UserProfileDTO;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

public interface AlertPlanService
{
    //<editor-fold desc="Get Alert Plans">
    @GET("/users/{userId}/alertPlans")
    List<AlertPlanDTO> getAlertPlans(
            @Path("userId") int userId);
    //</editor-fold>

    //<editor-fold desc="Subscribe To Alert Plan">
    @POST("/users/{userId}/alertPlans")
    UserProfileDTO subscribeToAlertPlan(
            @Path("userId") int userId,
            @Body PurchaseReportDTO purchaseReportDTO);
    //</editor-fold>

    //<editor-fold desc="Check Alert Plan Subscription">
    @POST("/users/{userId}/alertPlans/checkAlertPlanSubscription")
    UserProfileDTO checkAlertPlanSubscription(
            @Path("userId") int userId);
    //</editor-fold>

    //<editor-fold desc="Restore Purchases">
    @POST("/users/{userId}/alertPlans/restore")
    UserProfileDTO restorePurchases(
            @Path("userId") int userId,
            @Body RestorePurchaseForm restorePurchaseForm);
    //</editor-fold>
}