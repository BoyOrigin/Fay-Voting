package fayvoting.model;

public class Vote {
    private String voterId;
    private String candidateId;

    public Vote(String voterId, String candidateId) {
        this.voterId = voterId;
        this.candidateId = candidateId;
    }
}