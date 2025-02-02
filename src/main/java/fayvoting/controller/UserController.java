package fayvoting.controller;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import fayvoting.model.User;
import fayvoting.service.UserService;

@Controller
public class UserController {
	
	@Autowired
	private UserService userServ;
	
	@PostMapping("/createuser")
	public String createUser(@ModelAttribute User user, HttpSession session)
	{
		String username = user.getUsername();
		
		if(userServ.getUserByUsername(username) != null)
		{
			session.setAttribute("fail", "Registration Failed, Please try different Username");
			
			return "redirect:/register";
		}
		else{
			userServ.addUser(user);
			session.setAttribute("msg", "Registration successful");
			return "redirect:/register";
		}
		
	}
	
	@GetMapping("/user")
	public String dashboard(Model m, Principal p)
	{
		String username = p.getName(); //
		
		User user  = userServ.getUserByUsername(username);
		
		m.addAttribute("user",user);
		m.addAttribute("title","DASHBOARD");
		
		return "user/dashboard";
		
		
	}
	
		

}
