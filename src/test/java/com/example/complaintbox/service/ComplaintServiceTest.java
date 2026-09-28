package com.example.complaintbox.service;

import com.example.complaintbox.dto.ComplaintRequest;
import com.example.complaintbox.dto.ComplaintResponse;
import com.example.complaintbox.dto.DashboardResponse;
import com.example.complaintbox.entity.Complaint;
import com.example.complaintbox.entity.User;
import com.example.complaintbox.enums.Category;
import com.example.complaintbox.enums.Priority;
import com.example.complaintbox.enums.Status;
import com.example.complaintbox.exception.ComplaintNotFoundException;
import com.example.complaintbox.exception.InvalidStatusChangeException;
import com.example.complaintbox.repository.ComplaintRepository;
import com.example.complaintbox.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ComplaintService complaintService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Kumar");
        user.setEmail("kumar@gmail.com");

        // save() simply returns the object it receives
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Complaint makeComplaint(Status status, int daysOld) {
        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setUser(user);
        complaint.setCategory(Category.PLUMBING);
        complaint.setRoomNumber("205");
        complaint.setDescription("Water leakage in bathroom");
        complaint.setPriority(Priority.HIGH);
        complaint.setStatus(status);
        complaint.setCreatedAt(LocalDateTime.now().minusDays(daysOld));
        complaint.setUpdatedAt(LocalDateTime.now().minusDays(daysOld));
        return complaint;
    }

    // ---------- create complaint ----------

    @Test
    void createComplaint_setsStatusOpenAndKeepsPriority() {
        when(userRepository.findByEmail("kumar@gmail.com")).thenReturn(Optional.of(user));
        ComplaintRequest request = new ComplaintRequest(Category.PLUMBING, "Leaking sink", "Block A", "2",
            "205", "Near the window", "Water leakage in bathroom", Priority.HIGH);

        ComplaintResponse response = complaintService.createComplaint("kumar@gmail.com", request);

        assertEquals(Status.OPEN, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertEquals("Leaking sink", response.title());
        assertEquals("Block A", response.hostelBlock());
        assertEquals("2", response.floor());
        assertEquals("Near the window", response.landmark());
        assertFalse(response.overdue());
    }

    @Test
    void createComplaint_usesMediumPriorityWhenMissing() {
        when(userRepository.findByEmail("kumar@gmail.com")).thenReturn(Optional.of(user));
        ComplaintRequest request = new ComplaintRequest(Category.CLEANING, "Dirty corridor", "Block A", "2",
            "205", null, "Corridor is not clean", null);

        ComplaintResponse response = complaintService.createComplaint("kumar@gmail.com", request);

        assertEquals(Priority.MEDIUM, response.priority());
    }

    // ---------- status workflow ----------

    @Test
    void updateStatus_openToInProgress_isAllowed() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(makeComplaint(Status.OPEN, 0)));

        ComplaintResponse response = complaintService.updateStatus(1L, Status.IN_PROGRESS);

        assertEquals(Status.IN_PROGRESS, response.status());
    }

    @Test
    void updateStatus_inProgressToResolved_isAllowed() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(makeComplaint(Status.IN_PROGRESS, 0)));

        ComplaintResponse response = complaintService.updateStatus(1L, Status.RESOLVED);

        assertEquals(Status.RESOLVED, response.status());
    }

    @Test
    void updateStatus_openToResolved_isAllowed() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(makeComplaint(Status.OPEN, 0)));

        ComplaintResponse response = complaintService.updateStatus(1L, Status.RESOLVED);

        assertEquals(Status.RESOLVED, response.status());
    }

    @Test
    void updateStatus_resolvedToOpen_isRejected() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(makeComplaint(Status.RESOLVED, 0)));

        InvalidStatusChangeException ex = assertThrows(InvalidStatusChangeException.class,
                () -> complaintService.updateStatus(1L, Status.OPEN));

        assertEquals("Invalid status change", ex.getMessage());
    }

    @Test
    void updateStatus_resolvedToInProgress_isRejected() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(makeComplaint(Status.RESOLVED, 0)));

        assertThrows(InvalidStatusChangeException.class,
                () -> complaintService.updateStatus(1L, Status.IN_PROGRESS));
    }

    @Test
    void updateStatus_inProgressToOpen_isRejected() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(makeComplaint(Status.IN_PROGRESS, 0)));

        assertThrows(InvalidStatusChangeException.class,
                () -> complaintService.updateStatus(1L, Status.OPEN));
    }

    @Test
    void updateStatus_unknownComplaint_throwsNotFound() {
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ComplaintNotFoundException.class,
                () -> complaintService.updateStatus(99L, Status.IN_PROGRESS));
    }

    // ---------- overdue ----------

    @Test
    void overdueFlag_isTrueForUnresolvedComplaintOlderThanFiveDays() {
        when(userRepository.findByEmail("kumar@gmail.com")).thenReturn(Optional.of(user));
        when(complaintRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(makeComplaint(Status.OPEN, 6)));

        List<ComplaintResponse> result = complaintService.getMyComplaints("kumar@gmail.com");

        assertTrue(result.get(0).overdue());
    }

    @Test
    void overdueFlag_isFalseForResolvedOrNewComplaints() {
        when(userRepository.findByEmail("kumar@gmail.com")).thenReturn(Optional.of(user));
        when(complaintRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(makeComplaint(Status.RESOLVED, 10), makeComplaint(Status.OPEN, 2)));

        List<ComplaintResponse> result = complaintService.getMyComplaints("kumar@gmail.com");

        assertFalse(result.get(0).overdue());
        assertFalse(result.get(1).overdue());
    }

    // ---------- admin list ----------

    @Test
    void getAllComplaints_sortsPriorityFirstThenUnresolvedAndOldest() {
        Complaint resolvedOld = makeComplaint(Status.RESOLVED, 20);
        resolvedOld.setId(1L);
        resolvedOld.setPriority(Priority.HIGH);
        Complaint openNew = makeComplaint(Status.OPEN, 1);
        openNew.setId(2L);
        openNew.setPriority(Priority.HIGH);
        Complaint openOld = makeComplaint(Status.IN_PROGRESS, 8);
        openOld.setId(3L);
        openOld.setPriority(Priority.MEDIUM);
        Complaint low = makeComplaint(Status.OPEN, 3);
        low.setId(4L);
        low.setPriority(Priority.LOW);
        when(complaintRepository.findAll()).thenReturn(List.of(low, resolvedOld, openOld, openNew));

        List<ComplaintResponse> result = complaintService.getAllComplaints();

        assertEquals(2L, result.get(0).id());
        assertEquals(1L, result.get(1).id());
        assertEquals(3L, result.get(2).id());
        assertEquals(4L, result.get(3).id());
    }

    // ---------- dashboard ----------

    @Test
    void getDashboard_returnsCountsFromRepository() {
        when(complaintRepository.count()).thenReturn(10L);
        when(complaintRepository.countByStatus(Status.OPEN)).thenReturn(4L);
        when(complaintRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(3L);
        when(complaintRepository.countByStatus(Status.RESOLVED)).thenReturn(3L);
        when(complaintRepository.countByStatusNotAndCreatedAtBefore(eq(Status.RESOLVED), any(LocalDateTime.class)))
                .thenReturn(2L);

        DashboardResponse dashboard = complaintService.getDashboard();

        assertEquals(10L, dashboard.total());
        assertEquals(4L, dashboard.open());
        assertEquals(3L, dashboard.inProgress());
        assertEquals(3L, dashboard.resolved());
        assertEquals(2L, dashboard.overdue());
    }
}
