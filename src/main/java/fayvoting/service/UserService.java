package fayvoting.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fayvoting.model.User;
import fayvoting.repository.CandidateRepository;
import fayvoting.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private CandidateRepository canRepo;
	
	public User addUser(User user)
	{
		user.setRole("ROLE_NORMAL");

		return this.userRepo.saveAndFlush(user);
	}
	
	public List<User> getAllUsers()
	{
		return this.userRepo.findAll();
	}
	
	public User getUserById(int id)
	{
		return this.userRepo.getById(id);
	}
	
	public void deleteUser(int id)
	{
		this.userRepo.deleteById(id);
	}
	
	public User getUserByUsername(String username)
	{
		return this.userRepo.getUserByUsername(username);
	}
	

	

}
