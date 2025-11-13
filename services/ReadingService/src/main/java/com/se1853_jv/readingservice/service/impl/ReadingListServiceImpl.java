package com.se1853_jv.readingservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se1853_jv.readingservice.dto.request.ReadingListCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdatePapersRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdateUsersRequest;
import com.se1853_jv.readingservice.dto.response.ReadingListResponse;
import com.se1853_jv.readingservice.exception.ResourceNotFoundException;
import com.se1853_jv.readingservice.model.ReadingList;
import com.se1853_jv.readingservice.repository.ReadingListRepository;
import com.se1853_jv.readingservice.service.ReadingListService;
import com.se1853_jv.readingservice.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReadingListServiceImpl implements ReadingListService {

    private final ReadingListRepository readingListRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ReadingListResponse createReadingList(ReadingListCreateRequest request) {
        String listId = java.util.UUID.randomUUID().toString();
        ReadingList readingList = ReadingList.builder()
                .id(listId)
                .name(request.getName())
                .userIdsList(listToJsonString(request.getUserIdsList()))
                .paperIdsList(listToJsonString(request.getPaperIdsList()))
                .build();

        ReadingList saved = readingListRepository.save(readingList);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingListResponse getReadingListById(String listId) {
        ReadingList readingList = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Reading list not found"));
        return toResponse(readingList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingListResponse> getReadingListsByUser(String userId) {
        List<ReadingList> lists = readingListRepository.findByUserId(userId);
        return lists.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReadingListResponse updatePapers(String listId, ReadingListUpdatePapersRequest request) {
        ReadingList readingList = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Reading list not found"));

        List<String> currentPapers = jsonStringToList(readingList.getPaperIdsList());
        
        if ("add".equalsIgnoreCase(request.getAction())) {
            // Merge new papers
            for (String paperId : request.getPaperIds()) {
                if (!currentPapers.contains(paperId)) {
                    currentPapers.add(paperId);
                }
            }
        } else if ("remove".equalsIgnoreCase(request.getAction())) {
            // Remove papers
            currentPapers.removeAll(request.getPaperIds());
        } else {
            throw new IllegalArgumentException("Invalid action. Must be 'add' or 'remove'");
        }

        readingList.setPaperIdsList(listToJsonString(currentPapers));
        ReadingList updated = readingListRepository.save(readingList);
        return toResponse(updated);
    }

    @Override
    public ReadingListResponse updateUsers(String listId, ReadingListUpdateUsersRequest request) {
        ReadingList readingList = readingListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Reading list not found"));

        List<String> currentUsers = jsonStringToList(readingList.getUserIdsList());
        
        if ("add".equalsIgnoreCase(request.getAction())) {
            // Merge new users
            for (String userId : request.getUserIds()) {
                if (!currentUsers.contains(userId)) {
                    currentUsers.add(userId);
                }
            }
        } else if ("remove".equalsIgnoreCase(request.getAction())) {
            // Remove users
            currentUsers.removeAll(request.getUserIds());
        } else {
            throw new IllegalArgumentException("Invalid action. Must be 'add' or 'remove'");
        }

        readingList.setUserIdsList(listToJsonString(currentUsers));
        ReadingList updated = readingListRepository.save(readingList);
        return toResponse(updated);
    }

    @Override
    public void deleteReadingList(String listId) {
        if (!readingListRepository.existsById(listId)) {
            throw new ResourceNotFoundException("Reading list not found");
        }
        readingListRepository.deleteById(listId);
    }

    private ReadingListResponse toResponse(ReadingList readingList) {
        List<String> decodedUserIds = jsonStringToList(readingList.getUserIdsList());
        List<String> decodedPaperIds = jsonStringToList(readingList.getPaperIdsList());
        
        // Encode all IDs in the lists
        List<String> encodedUserIds = decodedUserIds != null ? 
                decodedUserIds.stream()
                    .map(IdEncoder::encode)
                    .collect(Collectors.toList()) : new ArrayList<>();
        
        List<String> encodedPaperIds = decodedPaperIds != null ? 
                decodedPaperIds.stream()
                    .map(IdEncoder::encode)
                    .collect(Collectors.toList()) : new ArrayList<>();
        
        return ReadingListResponse.builder()
                .id(IdEncoder.encode(readingList.getId()))
                .name(readingList.getName())
                .userIdsList(encodedUserIds)
                .paperIdsList(encodedPaperIds)
                .createdAt(readingList.getCreatedAt())
                .updatedAt(readingList.getUpdatedAt())
                .build();
    }

    private String listToJsonString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("Error converting list to JSON", e);
            return "[]";
        }
    }

    private List<String> jsonStringToList(String json) {
        if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error parsing JSON string to list", e);
            return new ArrayList<>();
        }
    }
}

