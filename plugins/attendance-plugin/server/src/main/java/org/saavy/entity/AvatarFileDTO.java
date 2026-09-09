package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.component.UiField;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvatarFileDTO implements Serializable {

    @UiField(label = "ID", type = "number", hidden = true, editable = false, order = 1)
    private Long id;

    @UiField(label = "File Name", type = "text", hidden = true, order = 2)
    private String fileName;

    @UiField(label = "Content Type", type = "text", hidden = true, order = 3)
    private String contentType;

    @UiField(
            label = "Avatar Image",
            type = "image",
            order = 4,
            fileNameField = "fileName",
            contentTypeField = "contentType"
    )
    private String content;
}