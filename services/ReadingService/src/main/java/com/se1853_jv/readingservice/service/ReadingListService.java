package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.request.ReadingListCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdatePapersRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdateUsersRequest;
import com.se1853_jv.readingservice.dto.response.ReadingListResponse;

import java.util.List;

public interface ReadingListService {

    ReadingListResponse createReadingList(ReadingListCreateRequest request);

    ReadingListResponse getReadingListById(String listId);

    List<ReadingListResponse> getReadingListsByUser(String userId);

    ReadingListResponse updatePapers(String listId, ReadingListUpdatePapersRequest request);

    ReadingListResponse updateUsers(String listId, ReadingListUpdateUsersRequest request);

    void deleteReadingList(String listId);
}

