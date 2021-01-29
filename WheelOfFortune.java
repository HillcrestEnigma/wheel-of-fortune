/*
 * Name: Paul Lee and Peter Ye
 * Teacher: Ms Krasteva
 * Date: 2021 Jan 19 14:17
 * Assignment: Wheel of Fortune ISP
 */

/* Program Purpose: Recreate the Wheel of Fortune game in Ready to Program
 * Variables:
 * - Console console: reference to console
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import hsa.Console;
import hsa.Message;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

class PlayerScore implements Comparable
{
    public final String playerName;
    public final int score;
    public PlayerScore(String pn,int sc)
    {
        score=sc;
        playerName=pn;
    }
    public int compareTo(Object o)
    {
        PlayerScore other=(PlayerScore)o;
        if(score==other.score)
        {
            return playerName.compareTo(other.playerName);
        }
        else
        {
            return new Integer(score).compareTo(new Integer(other.score));
        }
    }
    public String toString() // only here for TESTING
    {
        return playerName+" -> "+score;
    } 
}

public class WheelOfFortune {
    private Console console;
    private Random rng;

    public static final int NUMBER_OF_SPINS=10;

    public static final int CONSOLE_WIDTH=1024;
    public static final int CONSOLE_HEIGHT=768;
    public static final int FONT_SIZE=20;

    public static final int LETTER_GRID_X = 10;
    public static final int LETTER_GRID_Y = 10;
    public static final int LETTER_GRID_CELL_SIZE = 30;
    public static final int LETTER_GRID_ROWS = 8;
    public static final int LETTER_GRID_COLS = 25;

    public static final int INTERACTION_AREA_Y=CONSOLE_HEIGHT*3/4;

    public static final int CHAT_BOX_WIDTH=CONSOLE_WIDTH/2;
    public static final int CHAT_BOX_HEIGHT=CONSOLE_HEIGHT/4;
    public static final int CHAT_BOX_X=10;
    public static final int CHAT_BOX_Y=INTERACTION_AREA_Y-CHAT_BOX_HEIGHT-10;
    public static final int CHAT_BOX_MAX_LINES=9;

    public static final int SIDEBAR_X=CONSOLE_WIDTH*3/4;
    public static final int SIDEBAR_WIDTH=CONSOLE_WIDTH/4;
    public static final int SIDEBAR_ITEM_HEIGHT = 50;

    public final int INTERACTION_AREA_ROW;
    public final int INTERACTION_AREA_COL;

    public static final int WHEEL_X=SIDEBAR_X+SIDEBAR_WIDTH;
    public static final int WHEEL_Y=0;
    public static final int WHEEL_RADIUS=SIDEBAR_WIDTH;
    public static final double WHEEL_INIT_VEL_LOWER_BOUND=Math.PI*3;
    public static final double WHEEL_INIT_VEL_UPPER_BOUND=Math.PI*7;
    public static final double WHEEL_ACCEL=-Math.PI*5/3;

    public static final int PLAYER_LIST_X = SIDEBAR_X;
    public static final int PLAYER_LIST_Y = CONSOLE_HEIGHT-SIDEBAR_WIDTH-(int)(SIDEBAR_ITEM_HEIGHT*4.5);

    public static final int CATEGORY_INDICATOR_X = SIDEBAR_X;
    public static final int CATEGORY_INDICATOR_Y = PLAYER_LIST_Y+(int)(SIDEBAR_ITEM_HEIGHT*3.5);

    public static final int PLAYER_NAME_MAX_LEN=10;

    public static final String PHRASE_FILE_NAME="phrases.wof_data";
    public static final String SCORE_FILE_NAME="scores.wof";

    public static final String HOST_NAME="Host";

    private static final int LOSE_TURN=-1;
    private static final int BANKRUPT=-2;
    private static final int COMMUNISM=-3;

    private static final int[] WHEEL_VALUES={
        LOSE_TURN,2500,700,600,550,
        BANKRUPT,600,500,COMMUNISM,800,
        LOSE_TURN,800,500,900,500};

    private Map phrases;
    private List playerScores;

    // Class Constructor
    public WheelOfFortune() {
        // create a test console to calculate number of pixels per row and column
        int pxPerRow,pxPerCol;
        Console tmpConsole = new Console(1,1,FONT_SIZE);
        pxPerRow=tmpConsole.getHeight();
        pxPerCol=tmpConsole.getWidth();
        tmpConsole.close();

        int consoleRows=CONSOLE_HEIGHT/pxPerRow;
        int consoleCols=CONSOLE_WIDTH/pxPerCol;

        INTERACTION_AREA_ROW=INTERACTION_AREA_Y/pxPerRow+1;
        INTERACTION_AREA_COL=1;

        console=new Console(consoleRows,consoleCols,FONT_SIZE);

        phrases=new HashMap();
        playerScores=new ArrayList();

        rng=new Random();
        
        readPhrasesFromFile();
        readScoresFromFile();
    }

    private void drawWheelBaseToGraphics(Graphics2D graphics,Color[] WHEEL_COLORS,int radius, double angle)
    {
        final int SLICES=WHEEL_VALUES.length;
        int linesToDraw=(int)(2*Math.PI*radius);
        int linesPerSlice=linesToDraw/SLICES+1;

        int xPrev=(int)(Math.cos(angle)*radius)+radius;
        int yPrev=(int)(Math.sin(angle)*radius)+radius;

        for(int i=0;i<linesToDraw;++i)
        {
            double lineAngle=angle+2*Math.PI*i/linesToDraw;
            graphics.setColor(WHEEL_COLORS[i/linesPerSlice]);
            if(i%linesPerSlice<3)
            {
                graphics.setColor(Color.BLACK);
            }
            int xNew=(int)(Math.cos(lineAngle)*radius)+radius;
            int yNew=(int)(Math.sin(lineAngle)*radius)+radius;
            graphics.fillPolygon(new int[]{radius,xPrev,xNew},new int[]{radius,yPrev,yNew},3);
            xPrev=xNew;
            yPrev=yNew;
        }
        graphics.setColor(Color.BLACK);
        for(int i=0;i<linesToDraw;++i)
        {
            double lineAngle=angle+2*Math.PI*i/linesToDraw;
            int xBorder=(int)(Math.cos(lineAngle)*(radius-1))+radius;
            int yBorder=(int)(Math.sin(lineAngle)*(radius-1))+radius;
            graphics.fillOval(xBorder-2,yBorder-2,4,4);
        }

    }

    private void drawWheelTextToGraphics(Graphics2D graphics,int radius, double angle)
    {
        graphics.rotate(angle-Math.PI*0.3,radius,radius);
        graphics.setFont(new Font("Arial",Font.BOLD,radius/12));
        final int SLICES=WHEEL_VALUES.length;
        for(int i=0;i<SLICES;++i)
        {
            if(WHEEL_VALUES[i]==BANKRUPT)
            {
                graphics.setColor(Color.WHITE);
            }
            else
            {
                graphics.setColor(Color.BLACK);
            }
            String sliceStr;
            if(WHEEL_VALUES[i]==BANKRUPT)
            {
                sliceStr="BANKRUPT";
            }
            else if(WHEEL_VALUES[i]==LOSE_TURN)
            {
                sliceStr="LOSE A TURN";
            }
            else
            {
                sliceStr="$"+WHEEL_VALUES[i];
            }
            graphics.drawString(sliceStr,radius/8,radius);
            graphics.rotate(2*Math.PI/SLICES,radius,radius);
        }

    }

    private void drawWheel(double angle)
    {
        int x = WHEEL_X;
        int y = WHEEL_Y;
        int radius = WHEEL_RADIUS;

        

        final Color[] WHEEL_COLORS={
            Color.LIGHT_GRAY,Color.ORANGE,Color.GREEN,Color.YELLOW,Color.PINK,
            Color.LIGHT_GRAY,Color.CYAN,Color.RED,Color.YELLOW,Color.ORANGE,
            Color.BLACK,Color.CYAN,Color.GREEN,Color.RED,Color.PINK
        };
        
        BufferedImage bufferedImage=new BufferedImage(radius*2,radius*2,BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics=(Graphics2D)bufferedImage.getGraphics();
        drawWheelBaseToGraphics(graphics,WHEEL_COLORS, radius, angle);
        drawWheelTextToGraphics(graphics,radius,angle);

        console.drawImage(bufferedImage,x-radius,y-radius,null);
    }

    private void drawLetterGrid(char[][] grid) {
        int x = LETTER_GRID_X;
        int y = LETTER_GRID_Y;
        int squareSize = LETTER_GRID_CELL_SIZE;
        console.setFont(new Font("Arial", Font.PLAIN, 15));
        Color emptyColor = new Color(153, 204, 255);
        Color filledColor = new Color(242, 242, 242);
        for (int i=0; i<grid[0].length; i++) {
            for (int j=0; j<grid.length; j++) {
                if (grid[j][i] == 0) console.setColor(emptyColor);
                else console.setColor(filledColor);
                if (grid[j][i] == '_') grid[j][i] = ' ';
                console.fillRect(x + squareSize*i, y + squareSize*j, squareSize-1, squareSize-1);
                if(grid[j][i]!=0)
                {
                    console.setColor(Color.black);
                    console.drawString("" + grid[j][i], (int)(x+squareSize*(i+0.3)), (int)(y + squareSize*(j+0.65)));
                }
            }
        }
    }

    private char[][] stringToCharacterGrid(String str,int rows,int cols)
    {
        char[][] grid=new char[rows][cols];
        String[] words=str.split("\0");
        int currentRow=0,currentCol=0;
        for(int i=0;i<words.length;++i)
        {
            String currentWord=words[i].toUpperCase();
            while(currentRow<rows)
            {
                if(cols-currentCol >= currentWord.length())
                {
                    for(int j=0;j<currentWord.length();++j)
                    {
                        grid[currentRow][currentCol+j]=currentWord.charAt(j);
                    }
                    currentCol+=currentWord.length()+1;
                    break;
                }
                else
                {
                    ++currentRow;
                    currentCol=0;
                }
            }
        }
        return grid;
    }

    

    private void drawButton(int x, int y, String action, char key, boolean activated) {
        console.setColor(Color.black);
        console.drawRect(x, y, 200, 50);
        console.drawRect(x+150, y, 50, 50);
        if (activated) console.setColor(new Color(204, 255, 204));
        else console.setColor(Color.white);
        console.fillRect(x+1, y+1, 149, 49);
        console.fillRect(x+151, y+1, 49, 49);
        console.setColor(Color.black);
        console.setFont(new Font("Arial", Font.PLAIN, 20));
        console.drawString(action, (int)(x+16), (int)(y+33));
        console.drawString("" + key, (int)(x+168), (int)(y+33));
    }

    private void drawHost(int x, int y) {
        console.setColor(Color.black);
        console.fillOval(x, y, 50, 50);
        console.fillRect(x+5, y+45, 40, 100);
    }

    private void drawHostPlatform(int x, int y) {
        console.setColor(Color.gray);
        console.fillOval(x-25, y+120, 100, 50);
    }

    

    private void drawGenericSidebarWidget(int x, int y, String text, Color color) {
        console.setColor(Color.black);
        console.drawRect(x, y, SIDEBAR_WIDTH, SIDEBAR_ITEM_HEIGHT);
        console.setColor(color);
        console.fillRect(x+1, y+1, SIDEBAR_WIDTH-1, SIDEBAR_ITEM_HEIGHT-1);
        console.setColor(Color.black);
        console.setFont(new Font("Arial", Font.PLAIN, 20));
        console.drawString(text, (int)(x+16), (int)(y+SIDEBAR_ITEM_HEIGHT*33/50.0));
    }

    private void drawGenericSidebarWidget(int x, int y, String text) {
        drawGenericSidebarWidget(x, y, text, new Color(255, 204, 204));
    }

    private void drawSidebarHeading(int x, int y, String text) {
        drawGenericSidebarWidget(x, y, text, new Color(255, 153, 102));
    }

    private void drawPlayerInfo(int playerID, String name, int balance, boolean currentTurn, boolean isWinner) {
        Color color;
        if (currentTurn) color = new Color(255, 204, 102);
        else if (isWinner) color = new Color(204, 255, 204);
        else color = new Color(255, 204, 204);
        drawGenericSidebarWidget(PLAYER_LIST_X, PLAYER_LIST_Y + SIDEBAR_ITEM_HEIGHT*playerID, name + " - $" + balance, color);
    }

    private void drawPlayerInfo(int playerID, String name, int balance, boolean currentTurn) {
        drawPlayerInfo(playerID, name, balance, currentTurn, false);
    }

    private void drawChatBox(List lines) {
        console.setColor(Color.black);
        console.drawRect(CHAT_BOX_X, CHAT_BOX_Y, CHAT_BOX_WIDTH, CHAT_BOX_HEIGHT);
        console.setColor(new Color(255, 255, 204));
        console.fillRect(CHAT_BOX_X+1, CHAT_BOX_Y+1, CHAT_BOX_WIDTH-1, CHAT_BOX_HEIGHT-1);
        console.setColor(Color.black);
        console.setFont(new Font("Arial", Font.PLAIN, 16));
        int topMessage = Math.max(0, lines.size()-CHAT_BOX_MAX_LINES);
        for (int i = 0; i<Math.min(CHAT_BOX_MAX_LINES, lines.size()); i++) {
            console.drawString((String)lines.get(i + topMessage), CHAT_BOX_X+16, CHAT_BOX_Y+20 + (i)*20);
        }
    }

    private void drawSidebar() {
        console.setColor(new Color(255, 102, 102));
        console.fillRect(SIDEBAR_X, 0, SIDEBAR_WIDTH, INTERACTION_AREA_Y);

        drawSidebarHeading(PLAYER_LIST_X, PLAYER_LIST_Y, "Players");
        drawPlayerInfo(1, "???", 0, false, false);
        drawPlayerInfo(2, "???", 0, false, false);

        drawSidebarHeading(CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y, "Category");
        drawGenericSidebarWidget(CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y+SIDEBAR_ITEM_HEIGHT, "???");

        drawWheel(0);
        console.setColor(new Color(100, 100, 100));
        console.fillOval(WHEEL_X - WHEEL_RADIUS*13/16, WHEEL_Y + WHEEL_RADIUS*13/16, 10, 10);
    }

    private String parseCategoryMarker(String line)
    {   // returns the category name if line is a category marker
        // returns an empty string otherwise
        if(line.length()==0)
        {
            return "";
        }
        else if(line.charAt(line.length()-1)==':')
        {
            return line.substring(0,line.length()-1);
        }
        else
        {
            return "";
        }
    }
    private String parsePhrase(String line)
    {   // returns the phrase if line is a phrase
        // returns an empty string otherwise
        if(line.length()==0)
        {
            return "";
        }
        else if(line.charAt(0)=='-')
        {
            return line.substring(1,line.length()).trim();

        }
        else
        {
            return "";
        }
    }
    private void addPhraseToMap(String phrase,String category)
    {
        if(!phrases.containsKey(category))
        {
            phrases.put(category,new ArrayList());
        }
        List categoryPhrases=(List)phrases.get(category);
        categoryPhrases.add(phrase);
    }
    private void readPhrasesFromFile()
    {
        try
        {
            BufferedReader input=new BufferedReader(new FileReader(PHRASE_FILE_NAME));
            String line;
            String currentCategory="DEFAULT";
            while((line=input.readLine())!=null)
            {
                line=line.trim();
                String categoryMarker=parseCategoryMarker(line);
                if(categoryMarker.length()>0)
                {
                    currentCategory=categoryMarker;
                }
                else
                {
                    String phrase=parsePhrase(line);
                    if(phrase.length()>0)
                    {
                        addPhraseToMap(phrase,currentCategory);
                    }
                }
            }
            input.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private PlayerScore parseScore(String line)
    {
        line=line.trim();
        String[] parts=line.split("\\s*,\\s*");
        if(parts.length!=2)
        {
            return null;
        }
        else
        {
            return new PlayerScore(parts[0],Integer.parseInt(parts[1]));
        }
    }

    private void readScoresFromFile()
    {
        try
        {
            BufferedReader input=new BufferedReader(new FileReader(SCORE_FILE_NAME));
            String line;
            String currentCategory="DEFAULT";
            while((line=input.readLine())!=null)
            {
                PlayerScore score=parseScore(line);
                if(score!=null)
                {
                    playerScores.add(score);
                }
            }
            Collections.sort(playerScores,Collections.reverseOrder());
            input.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void writeScoresToFile()
    {
        try
        {
            PrintWriter output=new PrintWriter(new FileWriter(SCORE_FILE_NAME));
            for(int i=0;i<playerScores.size();++i)
            {
                PlayerScore playerScore=(PlayerScore)playerScores.get(i);
                output.println(playerScore.playerName+","+playerScore.score);
            }
            output.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void displayHighScores() // only here for TESTING
    {
        for(int i=0;i<playerScores.size();++i)
        {
            console.println(playerScores.get(i).toString());
        }
    }

    private void displayPhrases() // only here for TESTING
    {
        Set phraseCategories=phrases.keySet();
        Iterator it=phraseCategories.iterator();
        while(it.hasNext())
        {
            String categoryName=(String)it.next();

            console.println("CURRENT CATEGORY: "+categoryName);
            List phrasesInCategory=(List)phrases.get(categoryName);
            for(int i=0;i<phrasesInCategory.size() && i<3;++i)
            {
                console.println((String)phrasesInCategory.get(i));
            }
            console.println();
        }
    }

    public String formatDialog(String speaker,String message)
    {
        return speaker+": "+message;
    }

    public void drawInteractionArea()
    {
        console.setColor(Color.LIGHT_GRAY);
        console.fillRect(0,INTERACTION_AREA_Y,CONSOLE_WIDTH,CONSOLE_HEIGHT-INTERACTION_AREA_Y);
    }
    public void drawToInteractionArea(String str)
    {
        drawInteractionArea();
        console.setCursor(INTERACTION_AREA_ROW+1,INTERACTION_AREA_COL);
        console.print(str);
    }
    public String acceptString(String prompt,int lengthLimit)
    {
        while(true)
        {
            drawToInteractionArea(prompt);
            String answer=console.readLine();
            if(answer.length()>lengthLimit)
            {
                console.println("The string which you entered was too long! Please try again.\nPress any key to continue ...");
                console.getChar();
                continue;
            }
            else
            {
                return answer;
            }
        }
    }

    public int acceptChoice(String prompt, String[] choices) {
        console.setFont(new Font("Arial", Font.PLAIN, 15));
        drawToInteractionArea(prompt);
        for (int i=0; i<choices.length; i++) {
            drawButton(100 + (i%3)*250, INTERACTION_AREA_Y + (CONSOLE_HEIGHT-INTERACTION_AREA_Y)/2 + (i/3)*100, choices[i], (char)(i+'1'), false);
        }
        char[] availableLetters = new char[choices.length];
        for (int i=0; i<choices.length; i++) availableLetters[i] = (char)(i+'1');
        return (int)(acceptChar(availableLetters)-'1');
    }

    public char acceptChar(char[] available) {
        char input = console.getChar();
        for (int i=0; i<available.length; i++) if (input == available[i]) return input;
        new Message("Please enter a valid option.");
        return acceptChar(available);
    }

    public int acceptMenuChoice(String prompt, String[] choices) {
        console.clear();
        console.setFont(new Font("Arial", Font.PLAIN, 15));
        console.println(prompt);
        for (int i=0; i<choices.length; i++) {
            drawButton(50 + (i%3)*250, 300 + (i/3)*100, choices[i], (char)(i+'1'), false);
        }
        char[] availableLetters = new char[choices.length];
        for (int i=0; i<choices.length; i++) availableLetters[i] = (char)(i+'1');
        return (int)(acceptChar(availableLetters)-'1');
    }

    public void pauseProgram() {
        drawToInteractionArea("Press any key to continue.");
        console.getChar();
    }

    public boolean newRound()
    {
        Set phraseCategoriesSet = phrases.keySet();
        String[] phraseCategories = new String[phraseCategoriesSet.size()];
        phraseCategoriesSet.toArray(phraseCategories);

        int categoryIdx = acceptMenuChoice("Please choose a category of phrases to play with.", phraseCategories);
        String category = phraseCategories[categoryIdx];
        console.clear();



        drawLetterGrid(new char[LETTER_GRID_ROWS][LETTER_GRID_COLS]);

        drawHostPlatform(CHAT_BOX_X+CHAT_BOX_WIDTH*6/5, CHAT_BOX_Y+20);
        drawHost(CHAT_BOX_X+CHAT_BOX_WIDTH*6/5, CHAT_BOX_Y+20);

        drawSidebar();
        drawGenericSidebarWidget(CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y+SIDEBAR_ITEM_HEIGHT, category);

        List chatBoxLines=new ArrayList();
        chatBoxLines.add(formatDialog(HOST_NAME,"Hey what's your name?"));
        drawChatBox(chatBoxLines);
        String player1Name=acceptString("What is the name of player 1? ",PLAYER_NAME_MAX_LEN);
        int player1Balance = 0;
        drawPlayerInfo(1, player1Name, player1Balance, false);
        chatBoxLines.add(formatDialog(player1Name,"I'm "+player1Name+" and I am excited to win some money!"));
        drawChatBox(chatBoxLines);


        chatBoxLines.add(formatDialog(HOST_NAME,"Hey what's your name?"));
        drawChatBox(chatBoxLines);
        String player2Name=acceptString("What is the name of player 2? ",PLAYER_NAME_MAX_LEN);
        int player2Balance = 0;
        chatBoxLines.add(formatDialog(player2Name,"I'm "+player2Name+" and I know I will win!"));
        chatBoxLines.add(formatDialog(HOST_NAME,"You surely sound confident!"));
	drawChatBox(chatBoxLines);

        List phrasesInCategory=(List)phrases.get(category);
        int phraseIndex = -1;
        String phrase = "";
        char[] currentlyGuessed = new char[0];
        Set availableLetters=new HashSet();
        boolean newPhrase = true;

        boolean player1Turn=true;
        double angle = 0;
        double vel;
        int letterProfit;
        String currentPlayerName,otherPlayerName;

        int playerAction;
        String[] playerTurnChoices = {"Guess letter", "Solve puzzle"};

        for(int loop=0;loop<NUMBER_OF_SPINS;++loop)
        {
            if (newPhrase) {
                phraseIndex=rng.nextInt(phrasesInCategory.size());
                phrase=((String)phrasesInCategory.get(phraseIndex)).toUpperCase();
                System.out.println(phrase);
                currentlyGuessed=new char[phrase.length()];
                for(int i=0;i<phrase.length();++i)
                {
                    char c=phrase.charAt(i);
                    if(Character.isLetter(c))
                    {
                        currentlyGuessed[i]='_';
                    }
                    else if(Character.isSpaceChar(c))
                    {
                        currentlyGuessed[i]=0;
                    }
                    else
                    {
                        currentlyGuessed[i]=c;
                    }
                }

                availableLetters=new HashSet();
                for(char c='A';c<='Z';++c)
                {
                    availableLetters.add(new Character(c));
                }

                drawLetterGrid(
                    stringToCharacterGrid(
                        new String(currentlyGuessed),
                        LETTER_GRID_ROWS,
                        LETTER_GRID_COLS
                    )
                );

                newPhrase = false;
                
                chatBoxLines.add(formatDialog(HOST_NAME, "New phrase!"));
                drawChatBox(chatBoxLines);
            }

            drawPlayerInfo(1, player1Name, player1Balance, player1Turn);
            drawPlayerInfo(2, player2Name, player2Balance, !player1Turn);

            if(player1Turn)
            {
                currentPlayerName=player1Name;
                otherPlayerName=player2Name;
            }
            else
            {
                currentPlayerName=player2Name;
                otherPlayerName=player1Name;
            }

            chatBoxLines.add(formatDialog(HOST_NAME,"It's now your turn "+currentPlayerName+"."));
            drawChatBox(chatBoxLines);
            PLAYER_TURN: while (true) {
                playerAction = acceptChoice("What would you like to do?", playerTurnChoices);
                if (playerAction == 0) {
                    chatBoxLines.add(formatDialog(HOST_NAME,"Go spin the wheel!"));
                    chatBoxLines.add(formatDialog(currentPlayerName,"Alright!"));
                    drawChatBox(chatBoxLines);
                    drawToInteractionArea("The wheel is spinning.");

                    for (int i=0; i<20; i++) {
                        angle += Math.PI/180.0;
                        drawWheel(angle);
                        try {
                            Thread.sleep(1);
                        } catch (Exception e) {}
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {}
                    vel = Math.random()*(WHEEL_INIT_VEL_UPPER_BOUND-WHEEL_INIT_VEL_LOWER_BOUND)+WHEEL_INIT_VEL_LOWER_BOUND;
                    while (vel > 0) {
                        drawWheel(angle);
                        angle -= vel/100.0;
                        vel += WHEEL_ACCEL/100.0;
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {}
                    }

                    int letterValue = WHEEL_VALUES[(int)((Math.PI/15-angle)%(Math.PI*2)/(Math.PI*2)*WHEEL_VALUES.length)];

                    if (letterValue == LOSE_TURN) {
                        chatBoxLines.add(formatDialog(HOST_NAME, "Sorry, " + currentPlayerName + ". Looks like you lose a turn this time."));
                        chatBoxLines.add(formatDialog(currentPlayerName, "Whoops!"));
                        drawChatBox(chatBoxLines);
                        break;
                    } else if (letterValue == BANKRUPT) {
                        chatBoxLines.add(formatDialog(HOST_NAME, "Oh NO! " + currentPlayerName + ", you have just bankrupted."));
                        chatBoxLines.add(formatDialog(currentPlayerName, "NOOOOOOOOO!"));
                        drawChatBox(chatBoxLines);
                        if (player1Turn) {
                            player1Balance = 0;
                            drawPlayerInfo(1, player1Name, player1Balance, true);
                        } else {
                            player2Balance = 0;
                            drawPlayerInfo(2, player2Name, player2Balance, true);
                        }
                        break;
                    }

                    chatBoxLines.add(formatDialog(HOST_NAME, "$" + letterValue));
                    chatBoxLines.add(formatDialog(HOST_NAME, "Now guess a letter, " + currentPlayerName + "!"));
                    drawToInteractionArea("Please enter the letter you think is in the word/phrase.");
                    drawChatBox(chatBoxLines);

                    char guess;
                    while(true)
                    {
                        guess=Character.toUpperCase(console.getChar());

                        chatBoxLines.add(formatDialog(currentPlayerName, guess + "!"));
                        drawChatBox(chatBoxLines);

                        if(availableLetters.contains(new Character(guess)))
                        {
                            break;
                        }
                        else
                        {
                            chatBoxLines.add(formatDialog(HOST_NAME, "That has been guessed already bruhhh"));
                            chatBoxLines.add(formatDialog(HOST_NAME, "Since I'm a nice person, I'll let you try again."));
                            drawChatBox(chatBoxLines);
                            guess=console.getChar();
                        }
                    }
                    availableLetters.remove(new Character(guess));

                    int occurences=0;
                    for(int i=0;i<currentlyGuessed.length;++i)
                    {
                        if(phrase.charAt(i)==guess)
                        {
                            ++occurences;
                            currentlyGuessed[i]=guess;
                        }
                    }

                    String guessResult;
                    if(occurences==0) guessResult="are no";
                    else if(occurences==1) guessResult="is 1";
                    else guessResult="are "+occurences;
                    chatBoxLines.add(formatDialog(HOST_NAME, "There "+guessResult+" "+guess+"'s!"));
                    drawChatBox(chatBoxLines);

                    drawLetterGrid(
                        stringToCharacterGrid(
                            new String(currentlyGuessed),
                            LETTER_GRID_ROWS,
                            LETTER_GRID_COLS
                        )
                    );

                    if (occurences == 0) break;

                    letterProfit = occurences * letterValue;
                    if (player1Turn) {
                        player1Balance += letterProfit;
                        drawPlayerInfo(1, player1Name, player1Balance, true);
                    } else {
                        player2Balance += letterProfit;
                        drawPlayerInfo(2, player2Name, player2Balance, true);
                    }

                    for (int i=0; i<currentlyGuessed.length; ++i) {
                        if (currentlyGuessed[i] != phrase.charAt(i)) {
                            pauseProgram();
                            continue PLAYER_TURN;
                        }
                    }

                    chatBoxLines.add(formatDialog(HOST_NAME, "We got all the letters. Congratulations!"));
                    drawChatBox(chatBoxLines);
                    newPhrase = true;
                    break;
                } else {
                    chatBoxLines.add(formatDialog(currentPlayerName, "I would like to solve the puzzle!"));
                    chatBoxLines.add(formatDialog(HOST_NAME, "Yes?"));
                    drawChatBox(chatBoxLines);
                    String guess = acceptString("What is the answer to the puzzle? ", 36);

                    chatBoxLines.add(formatDialog(currentPlayerName, guess + "?"));
                    drawChatBox(chatBoxLines);

                    guess=guess.toUpperCase();

                    if(guess.length()!=currentlyGuessed.length)
                    {
                        chatBoxLines.add(formatDialog(HOST_NAME, "That's not even the same length as the phrase!"));
                        chatBoxLines.add(formatDialog(HOST_NAME, "What were you thinking?"));
                        chatBoxLines.add(formatDialog(currentPlayerName, "noooooooooo"));
                        drawChatBox(chatBoxLines);
                        break;
                    }
                    boolean knownPartsMatch=true;
                    for(int i=0;i<currentlyGuessed.length;++i)
                    {
                        if(currentlyGuessed[i]=='_')
                            continue;
                        else if(currentlyGuessed[i]=='\0')

                        {
                            if(guess.charAt(i)!=' ')
                            {
                                knownPartsMatch=false;
                                break;
                            }
                        }
                        else
                        {
                            if(currentlyGuessed[i]!=guess.charAt(i))
                            {
                                knownPartsMatch=false;
                                break;
                            }
                        }
                    }
                    if(!knownPartsMatch)
                    {
                        chatBoxLines.add(formatDialog(HOST_NAME, "You got the KNOWN parts of the phrase wrong!"));
                        chatBoxLines.add(formatDialog(HOST_NAME, "What were you thinking?"));
                        chatBoxLines.add(formatDialog(currentPlayerName, "noooooooooo"));
                        drawChatBox(chatBoxLines);
                        break;
                    }

                    if(guess.equals(phrase))
                    {
                        chatBoxLines.add(formatDialog(HOST_NAME, "That's right!"));
                        chatBoxLines.add(formatDialog(currentPlayerName, "Oh my gosh!"));
                        drawChatBox(chatBoxLines);
                        drawToInteractionArea("CORRECT.");
                        newPhrase = true;

                        for (int i=0; i<currentlyGuessed.length; ++i) {
                            if (Character.isSpaceChar(phrase.charAt(i))) currentlyGuessed[i] = '\0';
                            else currentlyGuessed[i] = phrase.charAt(i);
                        }

                        drawLetterGrid(
                            stringToCharacterGrid(
                                new String(currentlyGuessed),
                                LETTER_GRID_ROWS,
                                LETTER_GRID_COLS
                            )
                        );

                        break;
                    } else {
                        chatBoxLines.add(formatDialog(HOST_NAME, "Sorry " + currentPlayerName + ", that's not it."));
                        chatBoxLines.add(formatDialog(currentPlayerName, "Oh..."));
                        drawChatBox(chatBoxLines);
                        drawToInteractionArea("WRONG.");
                        break;
                    }
                }
            }

            pauseProgram();

            player1Turn = !player1Turn;
        }
        return false;
    }

    public static void main(String[] args) {
        WheelOfFortune game = new WheelOfFortune();
        game.newRound();

        /*
        game.displayHighScores();
        game.displayPhrases();

        game.writeScoresToFile();
        */
        /*
            while (game.newRound()) {    
                game.newRound();
            }
        }
        catch(UserExitException e) { // UserExitException is thrown when user wants to exit
            game.goodbye();
        }
        */
    }
}
