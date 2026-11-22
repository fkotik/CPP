package ru.cpp.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String name;

    private String patronymic;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Связь "многие пользователи -> одна роль" (аналог belongsTo в Eloquent).
    // @JoinColumn: в какой колонке этой таблицы лежит внешний ключ.
    // EAGER: роль загружается сразу вместе с пользователем (справочник маленький,
    // а роль нужна почти всегда). Альтернатива LAZY разберём, когда дойдём до партий.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // В Java булево поле принято называть без "is": геттер будет isActive()
    @Column(name = "is_active", nullable = false)
    private boolean active;

    // Заполняется самой БД (DEFAULT now()), поэтому Hibernate его не вставляет и не обновляет
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AppUser() {
    }

    public AppUser(String surname, String name, String patronymic,
                   String login, String passwordHash, Role role, boolean active) {
        this.surname = surname;
        this.name = name;
        this.patronymic = patronymic;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public Integer getId() { return id; }
    public String getSurname() { return surname; }
    public String getName() { return name; }
    public String getPatronymic() { return patronymic; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // "Соколов Игорь Петрович" (отчество, если есть)
    public String getFullName() {
        StringBuilder sb = new StringBuilder(surname).append(' ').append(name);
        if (patronymic != null && !patronymic.isBlank()) {
            sb.append(' ').append(patronymic);
        }
        return sb.toString();
    }
}