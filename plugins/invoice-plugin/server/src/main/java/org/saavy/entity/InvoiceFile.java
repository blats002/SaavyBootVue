package org.saavy.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.saavy.component.UiField;

@Entity
@Table(name = "invoice_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceFile extends BaseFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    public Invoice invoice;

    public static String getEmptyRecord(){
        return "{\n" +
                "    \"fileName\": '',\n" +
                "    \"contentType\": '',\n" +
                "    \"content\": null,\n" +
                "    \"invoice\": null\n" +
                "}";
    }
}

