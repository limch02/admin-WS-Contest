package com.admincontest.waitlist.domain;

import com.admincontest.classroom.domain.Classroom;
import com.admincontest.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Wait_List")
public class Waitlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Classroom room;
    
    @Column(name = "reservation_started_at", nullable = false)
    private LocalDateTime reservationStartedAt;
    
    @Column(name = "reservation_ended_at", nullable = false)
    private LocalDateTime reservationEndedAt;
    
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;
    
    @Column(name = "queue_position")
    private Long queuePosition;
    
    @Column(name = "status", length = 20)
    private String status;
    
    @PrePersist
    protected void onCreate() {
        if (reservedAt == null) {
            reservedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "WAITING";
        }
        if (queuePosition == null) {
            queuePosition = 1L;
        }
    }
    
    @Builder
    private Waitlist(User user, Classroom room, LocalDateTime reservationStartedAt, 
                    LocalDateTime reservationEndedAt, Long queuePosition, String status) {
        this.user = user;
        this.room = room;
        this.reservationStartedAt = reservationStartedAt;
        this.reservationEndedAt = reservationEndedAt;
        this.queuePosition = queuePosition != null ? queuePosition : 1L;
        this.status = status != null ? status : "WAITING";
    }
    
    public static Waitlist of(User user, Classroom room, LocalDateTime reservationStartedAt, 
                             LocalDateTime reservationEndedAt) {
        return Waitlist.builder()
                .user(user)
                .room(room)
                .reservationStartedAt(reservationStartedAt)
                .reservationEndedAt(reservationEndedAt)
                .build();
    }
    
    public void updateQueuePosition(Long position) {
        this.queuePosition = position;
    }
    
    public void approve() {
        this.status = "APPROVED";
    }
    
    public void cancel() {
        this.status = "CANCELLED";
    }
}

