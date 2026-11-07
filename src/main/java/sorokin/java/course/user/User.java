package sorokin.java.course.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import sorokin.java.course.account.Account;
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "login", nullable = false, unique = true)
    private String login;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Account> accountList = new ArrayList<>();

    public User() {
    }

    public User(String login) {
        this.login = login;
    }

    public Long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", accountList=" + accountList +
                '}';
    }
}
