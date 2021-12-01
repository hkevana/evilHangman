package hangman;

import java.io.*;

public class EvilHangman {

    public static void main(String[] args) {
        if (args.length == 3) {
            File src = new File(args[0]);
            int wordLength = Integer.parseInt(args[1]);
            int numGuesses = Integer.parseInt(args[2]);
            EvilHangmanGame game = new EvilHangmanGame();
            try {
                game.startGame(src, wordLength);
                game.startGameLoop(numGuesses);
            }
            catch (EmptyDictionaryException ex) { System.out.println("Dictionary Empty"); }
            catch (IOException ex) { ex.printStackTrace(); }
        } else { System.out.println("Usage: Java <options> <src.Main> dictionaryFile wordLength numGuesses"); }
    }
}
