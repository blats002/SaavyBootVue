package org.saavy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic base entity class for file attachments and document storage.
 * Provides standard columns for filename, MIME content type, and base64/binary payload content.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseFile {

    @Column(name = "file_name")
    protected String fileName;

    @Column(name = "content_type")
    protected String contentType;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    protected String content;
}
