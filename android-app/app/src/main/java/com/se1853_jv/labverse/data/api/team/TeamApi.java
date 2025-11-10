package com.se1853_jv.labverse.data.api.team;

import com.se1853_jv.labverse.data.dto.request.CreateTeamRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateTeamRequest;
import com.se1853_jv.labverse.data.dto.request.AddTeamMemberRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateMemberRoleRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.TeamResponse;
import com.se1853_jv.labverse.data.dto.response.TeamMemberResponse;
import com.se1853_jv.labverse.data.dto.response.TeamsPageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TeamApi {
    
    @GET("teams")
    Call<BaseJsonResponse<TeamsPageResponse>> getTeams(
            @Query("search") String search,
            @Query("researchField") String researchField,
            @Query("privacy") String privacy,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    @GET("teams/{id}")
    Call<BaseJsonResponse<TeamResponse>> getTeamById(@Path("id") String id);

    @POST("teams")
    Call<BaseJsonResponse<TeamResponse>> createTeam(@Body CreateTeamRequest request);

    @PUT("teams/{id}")
    Call<BaseJsonResponse<TeamResponse>> updateTeam(
            @Path("id") String id,
            @Body UpdateTeamRequest request
    );

    @DELETE("teams/{id}")
    Call<BaseJsonResponse<Object>> deleteTeam(@Path("id") String id);

    @GET("teams/{id}/members")
    Call<BaseJsonResponse<List<TeamMemberResponse>>> getTeamMembers(@Path("id") String id);

    @POST("teams/{id}/members")
    Call<BaseJsonResponse<TeamMemberResponse>> addTeamMember(
            @Path("id") String teamId,
            @Body AddTeamMemberRequest request
    );

    @DELETE("teams/{id}/members/{memberId}")
    Call<BaseJsonResponse<Object>> removeTeamMember(
            @Path("id") String teamId,
            @Path("memberId") String memberId
    );

    @PUT("teams/{id}/members/{memberId}/role")
    Call<BaseJsonResponse<TeamMemberResponse>> updateMemberRole(
            @Path("id") String teamId,
            @Path("memberId") String memberId,
            @Body UpdateMemberRoleRequest request
    );
}

