package com.siragu.CMex.network;

import com.siragu.CMex.model.Activity;
import com.siragu.CMex.model.Comment;
import com.siragu.CMex.model.Post;
import com.siragu.CMex.network.request.CreateCommentRequest;
import com.siragu.CMex.network.request.CreatePostRequest;
import com.siragu.CMex.network.request.FollowRequestReview;
import com.siragu.CMex.network.request.PaymentRequest;
import com.siragu.CMex.network.request.ReportUserRequest;
import com.siragu.CMex.network.request.UserUpdateRequest;
import com.siragu.CMex.network.response.BaseListModel;
import com.siragu.CMex.network.response.CreatePostResponse;
import com.siragu.CMex.network.response.FollowRequest;
import com.siragu.CMex.network.response.LikeDislikeResponse;
import com.siragu.CMex.network.response.ProfileFollowRequestResponse;
import com.siragu.CMex.network.response.ProfileFollowRequestReviewResponse;
import com.siragu.CMex.network.response.ProfileResponse;
import com.siragu.CMex.network.response.ReportUserResponse;
import com.siragu.CMex.network.response.UserResponse;
import com.siragu.CMex.network.response.PaymentResponse;
import com.siragu.CMex.network.response.ProfileFollowResponse;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by a_man on 05-12-2017.
 */

public interface DrService {
    @Headers("Accept: application/json")
    @POST("api/profile")
    Call<UserResponse> createUpdateUser(@Header("Authorization") String token, @Body UserUpdateRequest userRegisterResponse, @Query("update") int update);

    @Headers("Accept: application/json")
    @GET("api/profile/following/{id}")
    Call<BaseListModel<UserResponse>> getFollowings(@Header("Authorization") String token, @Path("id") String profileId, @Query("page") int page);

    @Headers("Accept: application/json")
    @GET("api/profile/followers/{id}")
    Call<BaseListModel<UserResponse>> getFollowers(@Header("Authorization") String token, @Path("id") String profileId, @Query("page") int page);

    @Headers("Accept: application/json")
    @GET("api/stories/users")
    Call<ArrayList<UserResponse>> getStoryUsers(@Header("Authorization") String token);

    @Headers("Accept: application/json")
    @GET("api/stories/users/{id}")
    Call<ArrayList<Post>> getStory(@Header("Authorization") String token, @Path("id") String profileId);

    @Headers("Accept: application/json")
    @GET("api/profile/{id}")
    Call<ProfileResponse> getProfile(@Header("Authorization") String token, @Path("id") String profileId);

    @Headers("Accept: application/json")
    @GET("api/activities")
    Call<BaseListModel<Activity>> getActivities(@Header("Authorization") String token, @Query("page") int page);

    @Headers("Accept: application/json")
    @POST("api/report/{id}")
    Call<ReportUserResponse> reportUser(@Header("Authorization") String token, @Path("id") String profileId, @Body ReportUserRequest reportUserRequest);

    @Headers("Accept: application/json")
    @POST("api/posts")
    Call<CreatePostResponse> createPost(@Header("Authorization") String token, @Body CreatePostRequest createPostRequest);

    @Headers("Accept: application/json")
    @GET("api/posts")
    Call<BaseListModel<Post>> getPosts(@Header("Authorization") String token, @Query("treding") int type, @Query("page") int page);

    @Headers("Accept: application/json")
    @GET("api/posts")
    Call<BaseListModel<Post>> getPostsByUserId(@Header("Authorization") String token, @Query("user_profile_id") String userProfileId, @Query("treding") int type, @Query("page") int page);

    @Headers("Accept: application/json")
    @GET("api/posts/me")
    Call<BaseListModel<Post>> getPostsMy(@Header("Authorization") String token, @Query("page") int page);

    @Headers("Accept: application/json")
    @GET("api/posts/{id}/show")
    Call<Post> getPostById(@Header("Authorization") String token, @Path("id") String postId);

    @Headers("Accept: application/json")
    @DELETE("api/posts/{id}/delete")
    Call<JsonObject> deletePost(@Header("Authorization") String token, @Path("id") String postId);

    @Headers("Accept: application/json")
    @GET("api/posts/{id}/report")
    Call<JsonObject> reportPost(@Header("Authorization") String token, @Path("id") String postId);

    @Headers("Accept: application/json")
    @POST("api/posts/{id}/share")
    Call<JsonObject> updateSharePost(@Header("Authorization") String token, @Path("id") String postId);

    @Headers("Accept: application/json")
    @POST("api/posts/{id}/like")
    Call<LikeDislikeResponse> updatePostLike(@Header("Authorization") String token, @Path("id") String postId);

    @Headers("Accept: application/json")
    @POST("api/posts/{id}/dislike")
    Call<LikeDislikeResponse> updatePostDislike(@Header("Authorization") String token, @Path("id") String postId);

    @Headers("Accept: application/json")
    @GET("api/posts/{id}/comments")
    Call<BaseListModel<Comment>> getComments(@Header("Authorization") String token, @Path("id") String postId, @Query("page") int page);

    @Headers("Accept: application/json")
    @POST("api/posts/{id}/comments")
    Call<Comment> createComment(@Header("Authorization") String token, @Path("id") String postId, @Body CreateCommentRequest comment);

    @Headers("Accept: application/json")
    @POST("api/comments/{id}/like")
    Call<LikeDislikeResponse> updateCommentLike(@Header("Authorization") String token, @Path("id") String commentId);

    @Headers("Accept: application/json")
    @POST("api/comments/{id}/dislike")
    Call<LikeDislikeResponse> updateCommentDislike(@Header("Authorization") String token, @Path("id") String commentId);

    @Headers("Accept: application/json")
    @POST("api/profile/search")
    Call<BaseListModel<UserResponse>> profileSearch(@Header("Authorization") String token, @Body HashMap<String, String> request, @Query("page") int page);

    @Headers("Accept: application/json")
    @POST("api/profile/follow/{id}")
    Call<ProfileFollowResponse> profileFollowAction(@Header("Authorization") String token, @Path("id") String profileId);

    @Headers("Accept: application/json")
    @GET("api/profile/follow-requests/follow/{id}")
    Call<ProfileFollowRequestResponse> profileFollowActionRequest(@Header("Authorization") String token, @Path("id") String profileId);

    @Headers("Accept: application/json")
    @GET("api/profile/follow-requests")
    Call<ArrayList<FollowRequest>> profileFollowRequests(@Header("Authorization") String token);

    @Headers("Accept: application/json")
    @POST("api/profile/follow-requests/{id}/review")
    Call<ProfileFollowRequestReviewResponse> profileFollowActionReview(@Header("Authorization") String token, @Path("id") String requestId, @Body FollowRequestReview request);

    @Headers("Accept: application/json")
    @POST("api/profile/payment")
    Call<PaymentResponse> payment(@Header("Authorization") String token, @Body PaymentRequest paymentRequest);
}
