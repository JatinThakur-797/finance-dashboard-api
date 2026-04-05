package com.auth.service;

import com.auth.dto.CreateFinancialRecordRequest;
import com.auth.dto.FinancialRecordResponse;
import com.auth.entities.FinancialRecord;
import com.auth.entities.RecordType;
import com.auth.entities.Role;
import com.auth.entities.User;
import com.auth.exeptions.AuthException;
import com.auth.exeptions.NotFoundException;
import com.auth.repository.FinancialRecordRepo;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepo repository;
    private final UserRepository userRepository;
    // GET ALL
    public List<FinancialRecordResponse> getAllRecords() {
        return repository.findAll()
                .stream()
                .map(FinancialRecordResponse::new)
                .toList();
    }

    // CREATE
    public FinancialRecordResponse createRecord(CreateFinancialRecordRequest request, UUID userId) {
        User createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory().trim().toLowerCase());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());
        record.setCreatedBy(createdBy);

        return new FinancialRecordResponse(repository.save(record));
    }

    // FILTER
    public List<FinancialRecordResponse> filterRecords(RecordType type, String category,
                                                       LocalDate from, LocalDate to) {
        List<FinancialRecord> results;

        if (type != null && category != null) {
            results = repository.findByTypeAndCategory(type, category.trim().toLowerCase());
        } else if (type != null) {
            results = repository.findByType(type);
        } else if (category != null) {
            results = repository.findByCategory(category.trim().toLowerCase());
        } else if (from != null && to != null) {
            if (from.isAfter(to)) throw new AuthException("'from' date must be before 'to' date");
            results = repository.findByDateBetween(from, to);
        } else {
            results = repository.findAll();
        }

        return results.stream().map(FinancialRecordResponse::new).toList();
    }
    // DELETE — Admin can delete any record, Analyst can only delete what they created
    public void deleteRecord(UUID recordId, UUID requestingUserId) {

        FinancialRecord record = repository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("Record not found"));

        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isAdmin = requestingUser.getRole() == Role.ADMIN;

        // Guard against records with no createdBy (e.g. seeded data)
        boolean isOwner = record.getCreatedBy() != null
                && record.getCreatedBy().getId().equals(requestingUserId);

        if (!isAdmin && !isOwner) {
            throw new AuthException("You are not authorized to delete this record");
        }

        repository.delete(record);
    }
}