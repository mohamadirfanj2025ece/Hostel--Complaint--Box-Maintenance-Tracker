package com.example.complaintbox.service;

import com.example.complaintbox.dto.ComplaintRequest;
import com.example.complaintbox.dto.ComplaintResponse;
import com.example.complaintbox.dto.DashboardResponse;
import com.example.complaintbox.entity.Complaint;
import com.example.complaintbox.entity.User;
import com.example.complaintbox.enums.Priority;
import com.example.complaintbox.enums.Status;
import com.example.complaintbox.exception.ComplaintNotFoundException;
import com.example.complaintbox.exception.InvalidStatusChangeException;
import com.example.complaintbox.exception.UserNotFoundException;
import com.example.complaintbox.repository.ComplaintRepository;
import com.example.complaintbox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

// All business rules live here (status workflow, overdue calculation, dashboard)
@Service
public class ComplaintService {

    // A complaint is overdue when it is unresolved for more than this many days
    private static final int OVERDUE_DAYS = 5;

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    public ComplaintService(ComplaintRepository complaintRepository, UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
    }

    // ---------- USER ----------

    @Transactional
    public ComplaintResponse createComplaint(String email, ComplaintRequest request) {
        User user = findUserByEmail(email);
        LocalDateTime now = LocalDateTime.now();

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setCategory(request.category());
        complaint.setTitle(request.title().trim());
        complaint.setHostelBlock(request.hostelBlock().trim());
        complaint.setFloor(request.floor().trim());
        complaint.setRoomNumber(request.roomNumber().trim());
        complaint.setLandmark(request.landmark() == null ? null : request.landmark().trim());
        complaint.setDescription(request.description().trim());
        complaint.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        complaint.setStatus(Status.OPEN);   // always OPEN when created
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);

        return toResponse(complaintRepository.save(complaint));
    }

    // Only the complaints of the logged-in user
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints(String email) {
        User user = findUserByEmail(email);
        return complaintRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- ADMIN ----------

        // Highest priority first; within a priority, unresolved and oldest complaints come first.
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
            .sorted(Comparator.comparing(Complaint::getPriority).reversed()
                .thenComparing((Complaint c) -> c.getStatus() == Status.RESOLVED)
                .thenComparing(Complaint::getCreatedAt))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getOverdueComplaints() {
        return complaintRepository
                .findByStatusNotAndCreatedAtBeforeOrderByCreatedAtAsc(Status.RESOLVED, overdueLimit())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ComplaintResponse updateStatus(Long id, Status newStatus) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ComplaintNotFoundException(id));

        if (!isValidStatusChange(complaint.getStatus(), newStatus)) {
            throw new InvalidStatusChangeException();
        }

        complaint.setStatus(newStatus);
        complaint.setUpdatedAt(LocalDateTime.now());
        return toResponse(complaintRepository.save(complaint));
    }

    // Counts are calculated from the database every time
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        long total = complaintRepository.count();
        long open = complaintRepository.countByStatus(Status.OPEN);
        long inProgress = complaintRepository.countByStatus(Status.IN_PROGRESS);
        long resolved = complaintRepository.countByStatus(Status.RESOLVED);
        long overdue = complaintRepository.countByStatusNotAndCreatedAtBefore(Status.RESOLVED, overdueLimit());
        return new DashboardResponse(total, open, inProgress, resolved, overdue);
    }

    // ---------- helper methods ----------

    // Allowed flow: OPEN -> IN_PROGRESS -> RESOLVED  (and OPEN -> RESOLVED).
    // Going backward, or staying in the same status, is NOT allowed.
    private boolean isValidStatusChange(Status current, Status next) {
        if (current == Status.OPEN) {
            return next == Status.IN_PROGRESS || next == Status.RESOLVED;
        }
        if (current == Status.IN_PROGRESS) {
            return next == Status.RESOLVED;
        }
        return false; // RESOLVED cannot change any more
    }

    // Complaints created before this time are older than 5 days
    private LocalDateTime overdueLimit() {
        return LocalDateTime.now().minusDays(OVERDUE_DAYS);
    }

    // Overdue is calculated, never stored in the database
    private boolean isOverdue(Complaint complaint) {
        return complaint.getStatus() != Status.RESOLVED
                && complaint.getCreatedAt().isBefore(overdueLimit());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    private ComplaintResponse toResponse(Complaint complaint) {
        return new ComplaintResponse(
                complaint.getId(),
                complaint.getUser().getName(),
                complaint.getCategory(),
                complaint.getTitle(),
                complaint.getHostelBlock(),
                complaint.getFloor(),
                complaint.getRoomNumber(),
                complaint.getLandmark(),
                complaint.getDescription(),
                complaint.getPriority(),
                complaint.getStatus(),
                complaint.getCreatedAt(),
                complaint.getUpdatedAt(),
                isOverdue(complaint));
    }
}
