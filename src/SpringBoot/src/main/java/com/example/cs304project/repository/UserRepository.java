package com.example.cs304project.repository;

import com.example.cs304project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //自定义查询方法
    /*
    *
保存/更新

S save(S entity)：保存或更新一个实体。

List<S> saveAll(Iterable<S> entities)：保存或更新多个实体。

查询

Optional<T> findById(ID id)：根据主键查找实体。

boolean existsById(ID id)：判断某个实体是否存在。

List<T> findAll()：查询所有实体。

Iterable<T> findAllById(Iterable<ID> ids)：根据多个主键查找实体。

long count()：统计实体总数。

分页与排序 findAll(Pageable pageable)、findAll(Sort sort)。

删除

void deleteById(ID id)：根据主键删除实体。

void delete(T entity)：删除给定的实体。

void deleteAll(Iterable<? extends T> entities)：删除多个实体。

void deleteAll()：删除所有实体。

    * */

    //根据用户名查找用户
    Optional<User> findByUserName(String userName);
    //根据邮箱查找用户
    Optional<User> findByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
}
