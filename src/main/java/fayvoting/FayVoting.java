package fayvoting;

import fayvoting.model.Blockchain;
import fayvoting.model.PeerToPeerNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fayvoting.model.Candidate;
import fayvoting.model.User;
import fayvoting.repository.CandidateRepository;
import fayvoting.repository.UserRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@SpringBootApplication
public class FayVoting implements CommandLineRunner{

	public static final PeerToPeerNode NODE = new PeerToPeerNode(Integer.getInteger("netty.bind", 20001), new Blockchain());

	public static void main(String[] args) {
		SpringApplication.run(FayVoting.class, args);
	}
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private CandidateRepository canRepo;

	@Override
	public void run(String... args) throws Exception {
		NODE.connectToPeer(System.getProperty("netty.connect1", "localhost"), Integer.getInteger("netty.connect1Port", 20002));
		NODE.connectToPeer(System.getProperty("netty.connect2", "localhost"), Integer.getInteger("netty.connect2Port", 20003));
		/*SCHEDULER.schedule(() -> {
			while (true) {
				try {
					// Admin
					User admin = new User();
					admin.setId(1);
					admin.setUsername("admin");
					admin.setName("admin");
					admin.setPassword("admin");
					admin.setStudyprogram("Administrator");
					admin.setRole("ROLE_ADMIN");
					admin.setStatus("admin");
					userRepo.save(admin);
					break;
				} catch (Throwable e) {
					e.printStackTrace();
					LockSupport.parkNanos("waiting 5 seconds", TimeUnit.SECONDS.toNanos(5L));
				}
			}
			System.out.println("Successfully add user");
		}, 5L, TimeUnit.SECONDS);

		// Candidates
		Candidate candidate1 = new Candidate();
		candidate1.setId(1);
		candidate1.setCandidate("candidate1");
		canRepo.save(candidate1);
		
		Candidate candidate2 = new Candidate();
		candidate2.setId(2);
		candidate2.setCandidate("candidate2");
		canRepo.save(candidate2);
		
		Candidate candidate3 = new Candidate();
		candidate3.setId(3);
		candidate3.setCandidate("candidate3");
		canRepo.save(candidate3);
		
		Candidate candidate4 = new Candidate();
		candidate4.setId(4);
		candidate4.setCandidate("candidate4");
		canRepo.save(candidate4);
		*/
	}
	

}
