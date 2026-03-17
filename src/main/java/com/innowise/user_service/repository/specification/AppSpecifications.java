package com.innowise.user_service.repository.specification;

import com.innowise.user_service.entity.PaymentCard;
import com.innowise.user_service.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AppSpecifications {
    public static Specification<User> filterUsers(String name, String surname) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) predicates.add(cb.equal(root.get("name"), name));
            if (surname != null && !surname.isBlank()) predicates.add(cb.equal(root.get("surname"), surname));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PaymentCard> filterCardsByUser(String userName, String userSurname) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<PaymentCard, User> userJoin = root.join("user");

            if (userName != null && !userName.isBlank()) predicates.add(cb.equal(userJoin.get("name"), userName));
            if (userSurname != null && !userSurname.isBlank())
                predicates.add(cb.equal(userJoin.get("surname"), userSurname));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}