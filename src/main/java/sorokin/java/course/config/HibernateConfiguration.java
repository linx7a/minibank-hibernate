package sorokin.java.course.config;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;

@Configuration
public class HibernateConfiguration {
    @Bean
    public SessionFactory sessionFactory(
            @Value("${db.driver}") String driver,
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.dialect}") String dialect,
            @Value("${hibernate.hbm2ddl.auto}") String hbm2ddl,
            @Value("${hibernate.show_sql}") String showSql,
            @Value("${hibernate.format_sql}") String formatSql
    ) {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        configuration
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Account.class)
                .setProperty("hibernate.connection.driver_class", driver)
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", username)
                .setProperty("hibernate.connection.password", password)
                .setProperty("hibernate.dialect", dialect)
                .setProperty("hibernate.hbm2ddl.auto", hbm2ddl)
                .setProperty("hibernate.show_sql", showSql)
                .setProperty("hibernate.format_sql", formatSql)
                .setProperty("hibernate.current_session_context_class", "thread");


        return configuration.buildSessionFactory();
    }
}
