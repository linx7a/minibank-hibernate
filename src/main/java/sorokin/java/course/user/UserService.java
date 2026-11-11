package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountService;
import sorokin.java.course.db.TransactionHelper;

import java.util.List;

@Component
public class UserService {
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;
    private final AccountService accountService;

    public UserService(SessionFactory sessionFactory, TransactionHelper transactionHelper, AccountService accountService) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
        this.accountService = accountService;
    }

    public User createUser(String login) {
        String normalizedLogin = validateLogin(login);
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            Long counter = session.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class)
                    .setParameter("login", normalizedLogin)
                    .getSingleResult();
            if (counter > 0) {
                throw new IllegalArgumentException("User already exists with login=%s".formatted(normalizedLogin));
            }
            User user = new User(normalizedLogin);
            session.persist(user);

            Account defaultAccount = accountService.createAccount(user);
            user.getAccountList().add(defaultAccount);

            return user;
        });
    }

    public User findUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            User user = session.find(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("No such user with id=%s".formatted(id));
            }
            return user;
        });
    }

    public List<User> findAll() {
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            List<User> allUsers = session.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
            return allUsers;
        });
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }
}
