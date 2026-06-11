package com.project1.shriganeshaynamah.Dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.project1.shriganeshaynamah.user.User;


@Repository
public interface userDao extends CrudRepository<User, Integer> {
    User findByName(String name);

    boolean existsByName(String name);

    boolean existsByEmail(String email);
}
