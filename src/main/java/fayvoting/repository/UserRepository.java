package fayvoting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fayvoting.model.User;

public interface UserRepository  extends JpaRepository<User, Integer>{
	
	@Query("select v from User v where v.username = :username")
	public User getUserByUsername(@Param("username") String username);
	
	

	
}
