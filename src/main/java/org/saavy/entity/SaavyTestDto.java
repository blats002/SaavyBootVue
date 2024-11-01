package org.saavy.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO for {@link SaavyTest}
 */
public class SaavyTestDto implements Serializable {
    private final Long id;
    private final String text1;

    public SaavyTestDto(Long id, String text1) {
        this.id = id;
        this.text1 = text1;
    }

    public Long getId() {
        return id;
    }

    public String getText1() {
        return text1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaavyTestDto entity = (SaavyTestDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.text1, entity.text1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text1);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "text1 = " + text1 + ")";
    }
}