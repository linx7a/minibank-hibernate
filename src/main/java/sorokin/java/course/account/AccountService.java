package sorokin.java.course.account;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.db.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.List;

@Component
public class AccountService {
    private final TransactionHelper transactionHelper;
    private final SessionFactory sessionFactory;
    private final AccountProperties accountProperties;

    public AccountService(TransactionHelper transactionHelper, SessionFactory sessionFactory, AccountProperties accountProperties) {
        this.transactionHelper = transactionHelper;
        this.sessionFactory = sessionFactory;
        this.accountProperties = accountProperties;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            Account account = new Account(accountProperties.getDefaultAmount());
            account.setUser(user);
            session.persist(account);
            return account;
        });
    }

    public Account findAccountById(Long id) {
        validatePositiveId(id, "account id");
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            Account account = session.find(Account.class, id);
            if (account == null) {
                throw new IllegalArgumentException("No such account with id=%s".formatted(id));
            }
            return account;
        });
    }

    public List<Account> getUserAccounts(Long userId) {
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            List<Account> allUserAccounts = session.createQuery(
                            "SELECT a FROM Account a WHERE a.user.id = :userId", Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
            return allUserAccounts;
        });
    }

    public void withdraw(Long fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);
        transactionHelper.executeInTransactionOrJoin(() -> {
            Account account = findAccountById(fromAccountId);
            if (amount > account.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                                .formatted(account.getId(), account.getMoneyAmount(), amount)
                );
            }
            account.setMoneyAmount(account.getMoneyAmount() - amount);
            return null;
        });
    }

    public void deposit(Long toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);
        transactionHelper.executeInTransactionOrJoin(() -> {
            Account account = findAccountById(toAccountId);
            account.setMoneyAmount(account.getMoneyAmount() + amount);
            return null;
        });
    }

    public Account closeAccount(Long accountId) {
        validatePositiveId(accountId, "account id");
        return transactionHelper.executeInTransactionOrJoin(() -> {
            Session session = sessionFactory.getCurrentSession();
            Account accountToClose = findAccountById(accountId);
            Long userId = accountToClose.getUser().getId();
            List<Account> userAccounts = getUserAccounts(userId);
            if (userAccounts.size() == 1) {
                throw new IllegalStateException("Can't close the only one account");
            }
            var accountToTransferMoney = userAccounts.stream()
                    .filter(it -> !it.getId().equals(accountId))
                    .findFirst()
                    .orElseThrow();
            accountToTransferMoney.setMoneyAmount(
                    accountToTransferMoney.getMoneyAmount() + accountToClose.getMoneyAmount());

            session.remove(accountToClose);
            return accountToClose;
        });
    }

    public void transfer(Long fromAccountId, Long toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("source and target account id must be different");
        }
        transactionHelper.executeInTransactionOrJoin(() -> {
            Account accountFrom = findAccountById(fromAccountId);
            Account accountTo = findAccountById(toAccountId);
            if (amount > accountFrom.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
                                .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
                );
            }
            accountFrom.setMoneyAmount(accountFrom.getMoneyAmount() - amount);
            int amountToTransfer = accountTo.getUser().getId().equals(accountFrom.getUser().getId())
                    ? amount : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
            accountTo.setMoneyAmount(accountTo.getMoneyAmount() + amountToTransfer);
            return null;
        });
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
