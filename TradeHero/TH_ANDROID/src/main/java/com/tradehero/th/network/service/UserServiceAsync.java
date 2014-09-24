package com.tradehero.th.network.service;

import com.tradehero.common.billing.googleplay.GooglePlayPurchaseDTO;
import com.tradehero.th.api.form.UserFormDTO;
import com.tradehero.th.api.social.HeroDTOList;
import com.tradehero.th.api.social.InviteFormDTO;
import com.tradehero.th.api.social.SocialNetworkEnum;
import com.tradehero.th.api.social.UserFriendsDTOList;
import com.tradehero.th.api.users.PaginatedAllowableRecipientDTO;
import com.tradehero.th.api.users.UpdateCountryCodeDTO;
import com.tradehero.th.api.users.UpdateCountryCodeFormDTO;
import com.tradehero.th.api.users.UpdateReferralCodeDTO;
import com.tradehero.th.api.users.UserAvailabilityDTO;
import com.tradehero.th.api.users.UserProfileDTO;
import com.tradehero.th.api.users.UserSearchResultDTOList;
import com.tradehero.th.api.users.UserTransactionHistoryDTOList;
import com.tradehero.th.api.users.WebSignInFormDTO;
import com.tradehero.th.api.users.password.ForgotPasswordDTO;
import com.tradehero.th.api.users.password.ForgotPasswordFormDTO;
import com.tradehero.th.api.users.payment.UpdateAlipayAccountDTO;
import com.tradehero.th.api.users.payment.UpdateAlipayAccountFormDTO;
import com.tradehero.th.api.users.payment.UpdatePayPalEmailDTO;
import com.tradehero.th.api.users.payment.UpdatePayPalEmailFormDTO;
import com.tradehero.th.fragments.social.friend.FollowFriendsForm;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedOutput;

interface UserServiceAsync
{
    //<editor-fold desc="Sign-Up With Email">
    @FormUrlEncoded @POST("/SignupWithEmail")
    void signUpWithEmail(@Header("Authorization") String authorization,
            @Field("biography") String biography,
            @Field("deviceToken") String deviceToken,
            @Field("displayName") String displayName,
            @Field("inviteCode") String inviteCode,
            @Field("email") String email,
            @Field("emailNotificationsEnabled") Boolean emailNotificationsEnabled,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("location") String location,
            @Field("password") String password,
            @Field("passwordConfirmation") String passwordConfirmation,
            @Field("pushNotificationsEnabled") Boolean pushNotificationsEnabled,
            @Field("username") String username,
            @Field("website") String website,
            @Field("phone_number") String phoneNumber,
            @Field("verify_code") String verifyCode,
            @Field("device_access_token") String deviceAccessToken,
            Callback<UserProfileDTO> cb);

    @Multipart @POST("/SignupWithEmail")
    void signUpWithEmail(
            @Header("Authorization") String authorization,
            @Part("biography") String biography,
            @Part("deviceToken") String deviceToken,
            @Part("displayName") String displayName,
            @Part("inviteCode") String inviteCode,
            @Part("email") String email,
            @Part("emailNotificationsEnabled") Boolean emailNotificationsEnabled,
            @Part("firstName") String firstName,
            @Part("lastName") String lastName,
            @Part("location") String location,
            @Part("password") String password,
            @Part("passwordConfirmation") String passwordConfirmation,
            @Part("pushNotificationsEnabled") Boolean pushNotificationsEnabled,
            @Part("username") String username,
            @Part("website") String website,
            @Part("phone_number") String phoneNumber,
            @Part("verify_code") String verifyCode,
            @Part("device_access_token") String deviceAccessToken,
            @Part("profilePicture") TypedOutput profilePicture,
            Callback<UserProfileDTO> cb);
    //</editor-fold>

    //<editor-fold desc="Signup">
    @POST("/users")
    void signUp(
            @Header("Authorization") String authorization,
            @Body UserFormDTO user,
            Callback<UserProfileDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Update Profile">
    @FormUrlEncoded @PUT("/users/{userId}/updateUser")
    void updateProfile(
            @Path("userId") int userId,
            @Field("deviceToken") String deviceToken,
            @Field("displayName") String displayName,
            @Field("email") String email,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("password") String password,
            @Field("passwordConfirmation") String passwordConfirmation,
            @Field("username") String username,
            @Field("emailNotificationsEnabled") Boolean emailNotificationsEnabled,
            @Field("pushNotificationsEnabled") Boolean pushNotificationsEnabled,
            @Field("biography") String biography,
            @Field("location") String location,
            @Field("website") String website,
            Callback<UserProfileDTO> cb);

    @Multipart @PUT("/users/{userId}/updateUser")
    void updateProfile(
            @Path("userId") int userId,
            @Part("deviceToken") String deviceToken,
            @Part("displayName") String displayName,
            @Part("email") String email,
            @Part("firstName") String firstName,
            @Part("lastName") String lastName,
            @Part("password") String password,
            @Part("passwordConfirmation") String passwordConfirmation,
            @Part("username") String username,
            @Part("emailNotificationsEnabled") Boolean emailNotificationsEnabled,
            @Part("pushNotificationsEnabled") Boolean pushNotificationsEnabled,
            @Part("biography") String biography,
            @Part("location") String location,
            @Part("website") String website,
            @Part("profilePicture") TypedOutput profilePicture,
            Callback<UserProfileDTO> cb);

    @Multipart @PUT("/users/{userId}/updateUser")
    void updatePhoto(
            @Path("userId") int userId,
            @Part("profilePicture") TypedOutput profilePicture,
            Callback<UserProfileDTO> cb);

    @Multipart @PUT("/users/{userId}/updateUser")
    void updateName(
            @Path("userId") int userId,
            @Part("displayName") String displayName,
            Callback<UserProfileDTO> cb);

    @Multipart @PUT("/users/{userId}/updateUser")
    void updateAccount(
            @Path("userId") int userId,
            @Part("email") String email,
            @Part("password") String password,
            @Part("passwordConfirmation") String passwordConfirmation,
            Callback<UserProfileDTO> cb);
    //</editor-fold>

    //<editor-fold desc="Signin">
    @POST("users/signin")
    void signIn(
            @Body WebSignInFormDTO webSignInFormDTO,
            Callback<Response> callback);
    //</editor-fold>

    //<editor-fold desc="Check Display Name Available">
    @GET("/checkDisplayNameAvailable")
    void checkDisplayNameAvailable(
            @Query("displayName") String username,
            Callback<UserAvailabilityDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Forgot Password">
    @POST("/forgotPassword")
    void forgotPassword(
            @Body ForgotPasswordFormDTO forgotPasswordFormDTO,
            Callback<ForgotPasswordDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Search Users">
    @GET("/users/search")
    void searchUsers(
            @Query("q") String searchString,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<UserSearchResultDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Search Allowable Recipients">
    @GET("/users/allowableRecipients")
    void searchAllowableRecipients(
            @Query("searchTerm") String searchString,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage,
            Callback<PaginatedAllowableRecipientDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Get User">
    @GET("/users/{userId}")
    void getUser(
            @Path("userId") int userId,
            Callback<UserProfileDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Get User Transactions History">
    @GET("/users/{userId}/transactionHistory")
    void getUserTransactions(
            @Path("userId") int userId,
            Callback<UserTransactionHistoryDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Update PayPal Email">
    @POST("/users/{userId}/updatePayPalEmail")
    void updatePayPalEmail(
            @Path("userId") int userId,
            @Body UpdatePayPalEmailFormDTO updatePayPalEmailFormDTO,
            Callback<UpdatePayPalEmailDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Update Alipay Account">
    @POST("/users/{userId}/updateAlipayAccount")
    void updateAlipayAccount(
            @Path("userId") int userId,
            @Body UpdateAlipayAccountFormDTO updateAlipayAccountFormDTO,
            Callback<UpdateAlipayAccountDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Delete User">
    @DELETE("/users/{userId}")
    void deleteUser(
            @Path("userId") int userId,
            Callback<Response> callback);
    //</editor-fold>

    //<editor-fold desc="Get Friends">
    @GET("/users/{userId}/getFriends")
    void getFriends(
            @Path("userId") int userId,
            Callback<UserFriendsDTOList> callback);

    @GET("/users/{userId}/GetNewFriends")
    void getSocialFriends(
            @Path("userId") int userId,
            @Query("socialNetwork") SocialNetworkEnum socialNetwork,
            Callback<UserFriendsDTOList> callback);

    @GET("/users/{userId}/SearchFriends")
    void searchSocialFriends(
            @Path("userId") int userId,
            @Query("socialNetwork") SocialNetworkEnum socialNetwork,
            @Query("q")String query,
            Callback<UserFriendsDTOList> callback);
    //</editor-fold>

    @POST("/users/BatchFollow/free")
    void followBatchFree(@Body FollowFriendsForm followFriendsForm, Callback<UserProfileDTO> callback);

    //<editor-fold desc="Invite Friends">
    @POST("/users/{userId}/inviteFriends")
    void inviteFriends(
            @Path("userId") int userId,
            @Body InviteFormDTO inviteFormDTO,
            Callback<Response> callback);
    //</editor-fold>

    //<editor-fold desc="Add Follow Credit">
    @POST("/users/{userId}/addCredit")
    void addCredit(
            @Path("userId") int userId,
            @Body GooglePlayPurchaseDTO purchaseDTO,
            Callback<UserProfileDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Follow Hero">
    @POST("/users/{userId}/follow")
    void follow(
            @Path("userId") int userId,
            Callback<UserProfileDTO> callback);

    @POST("/users/{userId}/follow/free")
    void freeFollow(
            @Path("userId") int userId,
            Callback<UserProfileDTO> callback);

    @POST("/users/{userId}/follow")
    void follow(
            @Path("userId") int userId,
            @Body GooglePlayPurchaseDTO purchaseDTO,
            Callback<UserProfileDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Unfollow Hero">
    @POST("/users/{userId}/unfollow")
    void unfollow(
            @Path("userId") int userId,
            Callback<UserProfileDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Get Heroes">
    @GET("/users/{userId}/heroes")
    void getHeroes(
            @Path("userId") int userId,
            Callback<HeroDTOList> callback);
    //</editor-fold>

    //<editor-fold desc="Update Country Code">
    @POST("/users/{userId}/updateCountryCode")
    void updateCountryCode(
            @Path("userId") int userId,
            @Body UpdateCountryCodeFormDTO updateCountryCodeFormDTO,
            Callback<UpdateCountryCodeDTO> callback);
    //</editor-fold>

    //<editor-fold desc="Update Referral Code">
    @POST("/users/{userId}/updateInviteCode")
    void updateReferralCode(
            @Path("userId") int userId,
            @Body UpdateReferralCodeDTO updateReferralCodeDTO,
            Callback<Response> callback);
    //</editor-fold>

    //<editor-fold desc="Send Verify code">
    @POST("/sendCode")
    void sendCode(
            @Query("phoneNumber") String phoneNumber,
            Callback<Response> cb);
    //</editor-fold>
}
