package hangman;

public class GuessAlreadyMadeException extends Exception {
    public GuessAlreadyMadeException() {}
    public GuessAlreadyMadeException(String s) { super(s); }
}
