package info.ephyra;

public class WebTriviaQuestion {
    private String question;
    private String[] answers;

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String q) {
        question = q;
    }

    public String[] getAnswers() {
        return answers;
    }
    public void setAnswers(String[] a) {
        answers = a;
    }

    @Override
    public String toString() {
        return "Q: "+question+" -- "+answers.toString();
    }
}