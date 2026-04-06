package com.mindgarden.customer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Drei Korrekturen gegenüber der ersten Version:
 *
 * 1. @Id kommt aus jakarta.persistence (JPA), NICHT aus spring.data
 *    → spring.data.annotation.Id ist für MongoDB/NoSQL
 *
 * 2. @Data ersetzt durch @Getter + @Setter
 *    → @Data generiert equals()/hashCode() über alle Felder
 *    → Hibernate-Proxies (Lazy Loading) brechen damit
 *    → Auf JPA Entities: niemals @Data verwenden
 *
 * 3. @Entity + @Table hinzugefügt
 *    → Ohne @Entity weiß Hibernate nicht dass das eine DB-Tabelle ist
 *    → @Table(name=...) macht den Tabellennamen explizit
 *
 * 4. int number → String phone
 *    → +49 151 12345678 passt nicht in einen int
 *
 * 5. Address als @Embedded (nicht eigene Tabelle)
 *    → Adresse existiert nicht ohne Kunde → kein eigener Lifecycle
 *    → @Embeddable speichert Adressfelder in derselben Zeile wie Customer
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
