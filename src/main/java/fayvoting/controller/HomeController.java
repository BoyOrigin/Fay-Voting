package fayvoting.controller;


import fayvoting.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

	@Autowired
	private CandidateService canServ;
	
	@GetMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title","HOME");

		m.addAttribute("nodename", System.getProperty("node.name", "Node-A"));

		int c1 = canServ.getNumOfVotes("candidate1");
		int c2 = canServ.getNumOfVotes("candidate2");
		int c3 = canServ.getNumOfVotes("candidate3");
		int c4 = canServ.getNumOfVotes("candidate4");


		m.addAttribute("c1", c1);
		m.addAttribute("c2", c2);
		m.addAttribute("c3", c3);
		m.addAttribute("c4", c4);
		return "home";
	}
	
	@GetMapping("/signin")
	public String login(Model m)
	{
		m.addAttribute("title", "SIGNIN");
		return "signin";
	}
	
	
	@GetMapping("/register")
	public String register(Model m)
	{
		m.addAttribute("title","REGISTER");
		return "register";
	}
	
	/*
	@GetMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title","ABOUT");
		return "about";
	}*/

}
