package org.example.repository;

import org.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query(nativeQuery = true, value = """
merge into users u
using (values
(:id, :userName, :firstName, :lastName, cast(:upsertAt as timestamp))
) v(id, user_name, first_name, last_name, upsert_at)
on u.id = v.id
when matched then
update set
user_name = v.user_name,
first_name = v.first_name,
number_actions = number_actions + 1,
updated_at = v.upsert_at
when not matched then
insert (id, user_name, first_name, last_name, number_actions, created_at, updated_at)
values (v.id, v.user_name, v.first_name, v.last_name, 1, v.upsert_at, v.upsert_at);
""")
    int updateAndIncrement(Long id, String userName, String firstName, String lastName, String upsertAt);
}
