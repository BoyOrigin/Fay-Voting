package fayvoting.model;

import fayvoting.service.CandidateService;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blockchain implements Serializable {
    private transient final CandidateService canServ;

    private static final String FILE_NAME = "blockchain.dat";
    private List<Block> chain;
    private Map<String, Integer> results;

    public Blockchain(final CandidateService canServ) {
        this.canServ = canServ;
        this.chain = new ArrayList<>();
        this.results = new HashMap<>();
        loadBlockchain();
    }

    public synchronized void addBlock(String voterId, String candidate) {
        if (results.containsKey(voterId)) {
            System.out.println("Voter has already voted.");
            return;
        }
        Block newBlock = new Block("Voter: " + voterId + " voted for " + candidate, chain.isEmpty() ? "0" : chain.get(chain.size() - 1).hash);
        chain.add(newBlock);
        results.put(candidate, results.getOrDefault(candidate, 0) + 1);
        recalculateResults();
        saveBlockchain();
    }

    public synchronized List<Block> getChain() {
        return chain;
    }

    public synchronized boolean validateBlockchain(List<Block> newChain) {
        for (int i = 1; i < newChain.size(); i++) {
            Block current = newChain.get(i);
            Block previous = newChain.get(i - 1);
            if (!current.previousHash.equals(previous.hash) || !current.calculateHash().equals(current.hash)) {
                return false;
            }
        }
        return true;
    }

    public synchronized void synchronizeBlockchain(List<Block> newChain) {
        if (newChain.size() > chain.size() && validateBlockchain(newChain)) {
            chain = newChain;
            recalculateResults();
            saveBlockchain();
            System.out.println("Blockchain successfully synchronized.");
        } else {
            // System.out.println("Received blockchain is invalid. (" + newChain.size() + " > " + chain.size() + ")");
        }
    }

    private synchronized void recalculateResults() {
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

    private synchronized void saveBlockchain() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(chain);
        } catch (IOException e) {
            System.err.println("Error saving blockchain: " + e.getMessage());
        }
    }

    private synchronized void loadBlockchain() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            chain = (List<Block>) in.readObject();
            recalculateResults();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing blockchain found. Starting a new one.");
        }
    }
}