package com.app.entity;

import com.app.constant.TransactionStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "id", nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String txnId;

    @Column(nullable = false)
    private Long fromId;

    @Column(nullable = false)
    private Long toId;

    @Column(nullable = false)
    private Double amount;

    private Double remark;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatusEnum status;

    private String reason;

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date lastUpdatedAt;
}
