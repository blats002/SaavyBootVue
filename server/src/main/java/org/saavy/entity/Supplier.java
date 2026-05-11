package org.saavy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.saavy.component.UiField;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor      // Required for JPA
@AllArgsConstructor
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @Column(nullable = false)
    @UiField(label = "Name", type = "text", order = 2)
    private String name;

    @Column(name = "contact_email", nullable = false, unique = true)
    @UiField(label = "Contact Email", type = "text", order = 3)
    private String contactEmail;

}
