package hangman;

import java.io.*;
import java.util.*;

public class EvilHangmanGame implements IEvilHangmanGame {
    private Set<String> word_bank;
    private String display_word;
    private SortedSet<Character> guessed_letters;
    private int new_letters;

    public EvilHangmanGame() {
        this.word_bank = new HashSet<>();
        this.display_word = " ";
        this.guessed_letters = new TreeSet<>();
        this.new_letters = 0;
    }



    @Override
    public void startGame(File dictionary, int wordLength) throws IOException, EmptyDictionaryException {
        this.word_bank.clear();
        try (Scanner in = new Scanner(dictionary)) {
            while(in.hasNext()) {
                String word = in.next();
                if (word.length() == wordLength) {
                    word = word.toLowerCase();
                    this.word_bank.add(word);
                }
            }
            if (this.word_bank.size() == 0) { throw new EmptyDictionaryException("Empty Dictionary"); }
            this.display_word = "-".repeat(wordLength);
        }
    }

    public void startGameLoop(int num_guesses) {
        while (num_guesses > 0) {
            char guess = prompt(num_guesses);
            try {
                String orig_display_word = this.display_word;
                this.word_bank = makeGuess(guess);

                if (orig_display_word.equals(this.display_word)) {
                    num_guesses--;
                    System.out.printf("Sorry there are no %c's\n", guess);
                } else {
                    boolean letters_remain = false;
                    for (char c : this.display_word.toCharArray()) {
                        if (c == '-') { letters_remain = true; break; }
                    }
                    if (!letters_remain) { break; }
                    System.out.printf("Yes, there is %d %c's\n", this.new_letters, guess);
                }
            } catch (GuessAlreadyMadeException ex) { System.out.println("You already guessed that letter"); }
        }
        if (num_guesses > 0) { System.out.println("\nYou Win!!"); }
        else { System.out.println("\nYou Lose!!"); }
        for (String word : this.word_bank) {
            System.out.printf("The word was: %s", word);
            break;
        }
    }
    public char prompt(int g) {
        boolean vaild_input = false;
        char guess = ' ';
        do {
            Scanner in = new Scanner(System.in);

            if (g > 0) { System.out.printf("\nYou have %d guesses left", g); }
            else { System.out.print("\nyou have 1 guess left"); }
            System.out.print("\nUsed letters: ");
            for (Character c : guessed_letters) { System.out.printf("%c ", c); }
            System.out.printf("\nWord %s", this.display_word);
            System.out.print("\nEnter guess: ");

            try {
                // Validate input
                String input;
                do {
                    input = in.nextLine();
                } while (input.length() == 0);
                if (input.length() > 1) { throw new IOException(); }
                input = input.toLowerCase();
                guess = input.charAt(0);
                if (guess < 'a' || guess > 'z') { throw new IOException(); }
                vaild_input = true;
            } catch (IOException ex) { System.out.println("Invalid input"); }
        } while (!vaild_input);
        return guess;
    }
    @Override
    public Set<String> makeGuess(char guess) throws GuessAlreadyMadeException {
        for (char c : this.guessed_letters) {
            if (guess == c || guess == (char)(c - 32)) { throw new GuessAlreadyMadeException(); }
        }
        this.guessed_letters.add(guess);

        Map<String, Set<String>> partitions = getPartitions(guess);
        this.word_bank = getNewWordBank(partitions);
        return this.word_bank;
    }
    private Map<String, Set<String>> getPartitions(char guess) {
        HashMap<String, Set<String>> partitions = new HashMap<>();

        // Create Partitions
        for (String word : this.word_bank) {
            String key = getKey(word, guess);
            Set<String> curSet = partitions.get(key);
            if (curSet == null) { curSet = new HashSet<>(); }
            curSet.add(word);
            partitions.put(key, curSet);
        }
        for (Map.Entry<String, Set<String>> pair : partitions.entrySet()) {
            System.out.printf("\n\t%s %d", pair.getKey(), pair.getValue().size());
        }
        return partitions;
    }
    private String getKey(String word, char letter) {
        StringBuilder key = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (c == letter) { key.append(letter); }
            else { key.append('-'); }
        }
        return key.toString();
    }
    private Set<String> getNewWordBank(Map<String, Set<String>> partitions) {
        String newKey = " ";
        Set<String> newWordBank = new HashSet<>();

        for(Map.Entry<String, Set<String>> pair : partitions.entrySet()) {
            Set<String> curSet = pair.getValue();
            String curKey = pair.getKey();

            if (curSet.size() > newWordBank.size()) {
                newWordBank = curSet;
                newKey = curKey;
            }
            if (curSet.size() == newWordBank.size()) {
                if (breakTie(curKey, newKey)) {
                    newWordBank = curSet;
                    newKey = curKey;
                }
            }
        }
        updateDisplayWord(newKey);
        System.out.printf("\n\n\t%s, %d\n", newKey, newWordBank.size());
        return newWordBank;
    }
    private void updateDisplayWord(String newKey) {
        this.new_letters = 0;
        StringBuilder newDW = new StringBuilder();
        for (int i = 0; i < display_word.length(); i++) {
            if (newKey.charAt(i) != '-') {
                newDW.append(newKey.charAt(i));
                this.new_letters++;
            } else { newDW.append(this.display_word.charAt(i)); }
        }
        this.display_word = newDW.toString();
    }
    private boolean breakTie(String k1, String k2) {
        int k1_has = 0;
        int k2_has = 0;

        for (int i = 0; i < k1.length(); i++) {
            if (k1.charAt(i) != '-') { k1_has++; }
            if (k2.charAt(i) != '-') { k2_has++; }
        }
        if (k1_has != k2_has) { return k1_has < k2_has; }
        // Partitions have Equal Occurrences of letter
        // Determine Rightmost occurrence
        for (int i = 0; i < k1.length(); i++) {
            if (k1.charAt(i) == '-' && k2.charAt(i) != '-') { return true; }
            if (k2.charAt(i) == '-' && k1.charAt(i) != '-') { return false; }
        }
        // Keys are equal
        return false;
    }
    @Override
    public SortedSet<Character> getGuessedLetters() { return guessed_letters; }
}
