/*
 * Automatic Hangman is a program that functions both as a simple playable game of hangman and an AI that plays hangman.
 *     Copyright (C) 2019  Noah Stanford <noahstandingford@gmail.com>
 *
 *     Automatic Hangman is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Automatic Hangman is distributed in the hope that it will be fun, interesting, and educational,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.corruptedark.automatichangman;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.xml.utils.URI;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import javax.swing.text.TextAction;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainForm {

    private JPanel panel;
    private JPasswordField solutionField;
    private JButton generateButton;
    private JButton clearButton;
    private JButton confirmButton;
    private JTextField guessesField;
    private JTextField nextGuessField;
    private JButton nextGuessButton;
    private JRadioButton machineButton;
    private JRadioButton humanButton;
    private JSVGCanvas hangmanCanvas;
    private JPanel letterSpacesPanel;
    private JLabel winLossLabel;
    private JButton aboutButton;
    private PlainDocument solutionDoc;
    private PlainDocument guessDoc;
    private FieldCharacterLimit guessCharacterLimit;
    private FieldCharacterLimit solutionCharacterLimit;
    private List<JLabel> solutionLetterSpaces;
    private List<String> dictionary;
    private List<String> subDictionary;
    private Random rand;

    private int hangmanStep;

    private final int SOLUTION_CHARACTER_LIMIT = 45;
    private final int GUESS_CHARACTER_LIMIT = 1;

    private boolean playerIsHuman;
    private boolean gameStarted;
    private boolean gameOver;
    private boolean playerWins;
    private String guessList;
    private String correctGuesses;
    private String solution;
    private static final String[] alphabet = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private List<Double> probabilities;

    private final String FILLER = "  ";

    private final char NO_MASK = (char)0;
    private final char MASK = '*';

    private final String PLAYER_WINS = "Player wins!";
    private final String PLAYER_LOSES = "Player loses!";

    private ExecutorService executorService;

    private AboutForm aboutForm;

    public MainForm() {

        executorService = Executors.newCachedThreadPool();

        rand = new Random();
        dictionary = new ArrayList<>();
        subDictionary = new ArrayList<>();

        probabilities = new ArrayList<>();

        try {

            InputStream wordsFile = getDictionaryStream();
            Scanner scan = new Scanner(wordsFile);
            while(scan.hasNextLine())
            {
                dictionary.add(scan.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i = 0; i < alphabet.length; i++)
        {
            probabilities.add(0.0);
        }

        playerIsHuman = true;
        gameStarted = false;
        gameOver = false;
        playerWins = false;
        guessList = "";
        correctGuesses = "";

        solutionCharacterLimit = new FieldCharacterLimit(SOLUTION_CHARACTER_LIMIT);
        solutionDoc = new PlainDocument();
        solutionDoc.setDocumentFilter(solutionCharacterLimit);
        solutionField.setDocument(solutionDoc);

        guessCharacterLimit = new FieldCharacterLimit(GUESS_CHARACTER_LIMIT);
        guessDoc = new PlainDocument();
        guessDoc.setDocumentFilter(guessCharacterLimit);
        nextGuessField.setDocument(guessDoc);


        hangmanStep = 0;
        setHangmanImage(hangmanStep);


        solutionLetterSpaces = new ArrayList<>(SOLUTION_CHARACTER_LIMIT);
        JLabel temp;
        Font tempFont;
        Map attributes;
        for(int i = 0; i < SOLUTION_CHARACTER_LIMIT; i++)
        {
            temp = new JLabel();
            temp.setVisible(false);
            temp.setText(FILLER);

            tempFont = temp.getFont();

            attributes = tempFont.getAttributes();
            attributes.put(TextAttribute.UNDERLINE,TextAttribute.UNDERLINE_ON);
            attributes.put(TextAttribute.SIZE,30);
            temp.setFont(tempFont.deriveFont(attributes));

            solutionLetterSpaces.add(temp);
            letterSpacesPanel.add(temp);
        }

        humanButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(!playerIsHuman)
                {
                    guessList = "";
                    correctGuesses = "";
                    gameStarted = false;
                    gameOver = false;
                    nextGuessField.setText("");
                    guessesField.setText("");

                    playerIsHuman = true;
                    machineButton.setSelected(false);
                    solutionField.setEchoChar(MASK);
                    solutionField.setText("");
                    solutionField.setEditable(false);
                    nextGuessField.setEditable(true);
                    hideBody();
                    clear();
                }
            }
        });

        machineButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                for(int i = 0; i < solutionLetterSpaces.size(); i++)
                {
                    solutionLetterSpaces.get(i).setText(FILLER);
                    solutionLetterSpaces.get(i).setVisible(false);
                }

                if(playerIsHuman)
                {
                    guessList = "";
                    correctGuesses = "";
                    gameStarted = false;
                    gameOver = false;
                    guessesField.setText("");

                    playerIsHuman = false;
                    humanButton.setSelected(false);
                    solutionField.setEchoChar(NO_MASK);
                    solutionField.setEditable(true);
                    nextGuessField.setEditable(false);
                    nextGuessField.setText("");
                    hideBody();
                }
            }
        });

        generateButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(playerIsHuman)
                {
                    solutionField.setEchoChar(MASK);
                }

                gameStarted = false;
                solutionField.setText(dictionary.get(rand.nextInt(dictionary.size())));
            }
        });

        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                clear();
            }
        });
        confirmButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(!gameStarted && (!playerIsHuman || (playerIsHuman && solutionField.getEchoChar() != NO_MASK)))
                {

                    hideBody();

                    gameStarted = true;
                    gameOver = false;
                    playerWins = false;
                    winLossLabel.setVisible(false);
                    correctGuesses = "";
                    guessList = "";

                    solution = solutionField.getText();

                    guessesField.setText("");

                    for (int i = 0; i < solutionLetterSpaces.size(); i++) {
                        solutionLetterSpaces.get(i).setText(FILLER);
                        solutionLetterSpaces.get(i).setVisible(false);
                    }
                    for (int i = 0; i < solution.length(); i++) {
                        solutionLetterSpaces.get(i).setVisible(true);
                    }

                    if (!playerIsHuman) {
                        Runnable optimalMachineRunnable = new Runnable() {
                            @Override
                            public void run() {
                                playAsOptimalMachine();
                            }
                        };

                        if(executorService.isShutdown())
                        {
                            executorService = Executors.newSingleThreadExecutor();
                        }

                        executorService.execute(optimalMachineRunnable);
                    }
                }
            }
        });
        nextGuessButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(!guessList.contains(nextGuessField.getText()) && gameStarted && !gameOver && playerIsHuman)
                {
                    isGuessCorrect(findGuessIndex(nextGuessField.getText().charAt(0)));
                }

                nextGuessField.setText("");
            }
        });
        nextGuessField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);

                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if(!guessList.contains(nextGuessField.getText()) && gameStarted && !gameOver && playerIsHuman)
                    {
                        isGuessCorrect(findGuessIndex(nextGuessField.getText().charAt(0)));
                    }

                    nextGuessField.setText("");
                }
            }
        });
        aboutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(aboutForm == null)
                {
                   aboutForm = new AboutForm();
                }
                else if(aboutForm.isDisplayable())
                {
                    aboutForm.setVisible(true);
                }

            }
        });
    }

    void clear()
    {
        executorService.shutdownNow();

        gameStarted = false;
        gameOver = false;
        playerWins = false;

        winLossLabel.setVisible(false);

        solutionField.setText("");
        correctGuesses = "";
        guessList = "";
        guessesField.setText("");
        nextGuessField.setText("");

        hideBody();

        for(int i = 0; i < solutionLetterSpaces.size(); i++)
        {
            solutionLetterSpaces.get(i).setText(FILLER);
            solutionLetterSpaces.get(i).setVisible(false);
        }
    }

    private void setProbabilitiesFromDictionary()
    {
        double sum = 0;
        for (int i = 0; i < alphabet.length; i++)
        {
            probabilities.set(i,0.0);
        }

        for (int i = 0; i < alphabet.length; i++)
        {
            for(int j = 0; j < subDictionary.size(); j++)
            {
                if(subDictionary.get(j).contains(alphabet[i]) && !guessList.contains(alphabet[i]))
                {
                    probabilities.set(i, probabilities.get(i) + 1);
                }
                else if(guessList.contains(alphabet[i]))
                {
                    probabilities.set(i, 0.0);
                }
            }

            sum += probabilities.get(i);
        }

        for (int i = 0; i < alphabet.length; i++)
        {
            probabilities.set(i,probabilities.get(i)/sum);
        }
    }

    void playAsOptimalMachine()
    {
        subDictionary.clear();

        for (int i = 0; i < dictionary.size(); i++) {
            if (dictionary.get(i).length() == solution.length()) {
                subDictionary.add(dictionary.get(i));
            }
        }

        int guessIndex;
        while (!gameOver && !Thread.currentThread().isInterrupted()) {
            setProbabilitiesFromDictionary();
            guessIndex = guessFromProbabilities(probabilities);
            nextGuessField.setText(alphabet[guessIndex]);
            filterDictionary(alphabet[guessIndex], isGuessCorrect(guessIndex));
            panel.invalidate();

            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean solutionFound()
    {
        boolean assumedCorrect = true;

        for (int i = 0; i < solution.length() && assumedCorrect; i++)
        {
            assumedCorrect = correctGuesses.contains(solution.substring(i,i+1));
        }

        return assumedCorrect;
    }

    private void showBodyPart()
    {
        hangmanStep++;

        setHangmanImage(hangmanStep);

        if(hangmanStep == 6)
        {
            winLossLabel.setText(PLAYER_LOSES);
            winLossLabel.setVisible(true);
            solutionField.setEchoChar(NO_MASK);
            gameOver = true;
            gameStarted = false;
            playerWins = false;
        }
        else if(hangmanStep > 6 || hangmanStep < 1)
        {
            gameOver = true;
            gameStarted = false;
        }

    }

    private int findGuessIndex(char guess)
    {
        int foundIndex = -1;
        boolean matches = false;
        for(int i = 0; i < alphabet.length && !matches; i++)
        {
            matches = alphabet[i].charAt(0) == guess;
            if(matches)
            {
                foundIndex = i;
            }
        }

        return foundIndex;
    }

    private boolean isGuessCorrect(int guessIndex)
    {
        boolean correct = solution.contains(alphabet[guessIndex]);
        if (!gameOver)
        {
            if (!guessList.contains(alphabet[guessIndex]))
            {
                guessList += alphabet[guessIndex];
                guessesField.setText(guessesField.getText() + alphabet[guessIndex] + ", ");
                if (correct)
                {
                    correctGuesses += alphabet[guessIndex];
                    for (int i = 0; i < solution.length(); i++)
                    {
                        if (solution.charAt(i) == alphabet[guessIndex].charAt(0))
                        {
                            solutionLetterSpaces.get(i).setText(alphabet[guessIndex]);
                        }
                    }
                    playerWins = solutionFound();
                    gameOver = playerWins;
                    if (playerWins)
                    {
                        winLossLabel.setText(PLAYER_WINS);
                        winLossLabel.setVisible(true);
                        gameStarted = false;
                    }
                }
                else
                {
                    showBodyPart();
                }
            }
            else
            {
                showBodyPart();
            }
        }
        else
        {
            solutionField.setEchoChar(NO_MASK);
        }

        return correct;
    }

    private void filterDictionary(String letter, boolean include)
    {
        if(include)
        {
            for(int i = subDictionary.size() - 1; i >= 0 ; i--)
            {
                if(!subDictionary.get(i).contains(letter))
                {
                    subDictionary.remove(i);
                }
            }
        }
        else
        {
            for (int i = subDictionary.size() - 1; i >= 0; i--)
            {
                if (subDictionary.get(i).contains(letter))
                {
                    subDictionary.remove(i);
                }
            }
        }

        for(int i = subDictionary.size() - 1; i >= 0; i--)
        {
            boolean mismatch = false;
            for(int j = 0; j < solution.length() && !mismatch; j++)
            {
                if((!solutionLetterSpaces.get(j).getText().equals(FILLER)) && solutionLetterSpaces.get(j).getText().charAt(0) != subDictionary.get(i).charAt(j))
                {
                    mismatch = true;
                    subDictionary.remove(i);
                }
            }
        }
    }

    private int guessFromProbabilities(List<Double> probabilitiesArray)
    {
        List<Integer> maxPool = new ArrayList<>();
        int guessIndex = 0;

        double max = -99.0;

        for(int i = 0; i < probabilitiesArray.size(); i++)
        {
            if (probabilitiesArray.get(i) >= max)
            {
                max = probabilitiesArray.get(i);
                guessIndex = i;
            }
        }

        for(int i = 0; i < probabilitiesArray.size(); i++)
        {
            if(probabilitiesArray.get(i) >= max)
            {
                maxPool.add(i);
            }
        }

        if (maxPool.size() != 0)
        {
            guessIndex = maxPool.get(rand.nextInt(maxPool.size()));
        }


        return guessIndex;
    }

    void hideBody()
    {
        hangmanStep = 0;
        setHangmanImage(hangmanStep);
    }

    void setHangmanImage(int step)
    {
        if(step <= 6) {

            try {
                hangmanCanvas.setURI(getClass().getClassLoader().getResource("hangman_" + step + ".svg").toURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    InputStream getDictionaryStream()
    {
        InputStream file = null;


        try {
            file = getClass().getClassLoader().getResourceAsStream("words_alpha.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Automatic Hangman");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        frame.setContentPane(new MainForm().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);

    }

}
