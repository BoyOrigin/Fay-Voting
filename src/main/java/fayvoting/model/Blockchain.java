package fayvoting.model;

import fayvoting.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class Blockchain implements Serializable {
    @Autowired
    private CandidateService canServ;

    private static final String FILE_NAME = "blockchain.dat";
    private List<Block> chain;
    private Map<String, Integer> results;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.results = new HashMap<>();
        loadBlockchain();
    }

    public void addBlock(String voterId, String candidate) {
        if (results.containsKey(voterId)) {
            System.out.println("Voter has already voted.");
            return;
        }
        Block newBlock = new Block("Voter: " + voterId + " voted for " + candidate, chain.isEmpty() ? "0" : chain.get(chain.size() - 1).hash);
        chain.add(newBlock);
        results.put(candidate, results.getOrDefault(candidate, 0) + 1);
        saveBlockchain();
    }

    public List<Block> getChain() {
        return chain;
    }

    public boolean validateBlockchain(List<Block> newChain) {
        for (int i = 1; i < newChain.size(); i++) {
            Block current = newChain.get(i);
            Block previous = newChain.get(i - 1);
            if (!current.previousHash.equals(previous.hash) || !current.calculateHash().equals(current.hash)) {
                return false;
            }
        }
        return true;
    }

    public void synchronizeBlockchain(List<Block> newChain) {
        if (newChain.size() > chain.size() && validateBlockchain(newChain)) {
            chain = newChain;
            recalculateResults();
            saveBlockchain();
            System.out.println("Blockchain successfully synchronized.");
        } else {
            // System.out.println("Received blockchain is invalid.");
        }
    }

    private void recalculateResults() {
        results.clear();
        for (Block block : chain) {
            if (block.getDecryptedData().startsWith("Voter:")) {
                String[] parts = block.getDecryptedData().split(" voted for ");
                if (parts.length == 2) {
                    String candidate = parts[1];
                    results.put(candidate, results.getOrDefault(candidate, 0) + 1);
                }
            }
        }
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " votes");
            Candidate selectedCan = canServ.getCandidateByCandidate(entry.getKey());
            selectedCan.setVotes(entry.getValue());
            canServ.addCandidate(selectedCan); // update candidate
        }
        canServ.flush();
    }

    public void showResults() {
        System.out.println("Election Results:");
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + " votes");
        }
    }

    private void saveBlockchain() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(chain);
        } catch (IOException e) {
            System.err.println("Error saving blockchain: " + e.getMessage());
        }
    }

    private void loadBlockchain() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            chain = (List<Block>) in.readObject();
            recalculateResults();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing blockchain found. Starting a new one.");
        }
    }
}