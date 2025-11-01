package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.request.ReadingListCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdatePapersRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdateUsersRequest;
import com.se1853_jv.readingservice.dto.response.ReadingListResponse;

import java.util.List;
import java.util.UUID;

public interface ReadingListService {

    ReadingListResponse createReadingList(ReadingListCreateRequest request);

    List<ReadingListResponse> getReadingListsByUser(String userId);

    ReadingListResponse updatePapers(UUID listId, ReadingListUpdatePapersRequest request);

    ReadingListResponse updateUsers(UUID listId, ReadingListUpdateUsersRequest request);

    void deleteReadingList(UUID listId);
}

