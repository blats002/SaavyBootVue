package org.saavy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "saavy_test")
public class SaavyTest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "text_1")
  private String text1;

  public String getText1() {
    return text1;
  }

  public void setText1(String text1) {
    this.text1 = text1;
  }
}