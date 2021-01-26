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
import java.util.Iterator;

public class WheelOfFortune {
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
    private Console console;
    public static final int CONSOLE_WIDTH=1024;
    public static final int CONSOLE_HEIGHT=768;
    public static final int FONT_SIZE=20;

    public static final int LETTER_GRID_X = 10;
    public static final int LETTER_GRID_Y = 10;
    public static final int LETTER_GRID_CELL_SIZE = 30;

    public static final int INTERACTION_AREA_Y=CONSOLE_HEIGHT*3/4;

    public static final int CHAT_BOX_WIDTH=CONSOLE_WIDTH/2;
    public static final int CHAT_BOX_HEIGHT=CONSOLE_WIDTH/5;
    public static final int CHAT_BOX_X=10;
    public static final int CHAT_BOX_Y=INTERACTION_AREA_Y-CHAT_BOX_HEIGHT-10;
    public static final int CHAT_BOX_MAX_LINES=5;

    public static final int SIDEBAR_X=CONSOLE_WIDTH*3/4;

    public final int INTERACTION_AREA_ROW;
    public final int INTERACTION_AREA_COL;

    public static final int PLAYER_NAME_MAX_LEN=10;

    public static final String PHRASE_FILE_NAME="phrases.wof_data";
    public static final String SCORE_FILE_NAME="scores.wof";

    private static final int LOSE_TURN=-1;
    private static final int BANKRUPT=-2;

    private static final int[] WHEEL_VALUES={
        LOSE_TURN,2500,700,600,550,
        BANKRUPT,600,550,500,800,
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

    private void drawWheel(int x,int y,int radius,double angle)
    {
        final Color[] WHEEL_COLORS={
            Color.LIGHT_GRAY,Color.ORANGE,Color.GREEN,Color.YELLOW,Color.PINK,
            Color.LIGHT_GRAY,Color.CYAN,Color.RED,Color.YELLOW,Color.ORANGE,
            Color.BLACK,Color.CYAN,Color.GREEN,Color.RED,Color.PINK
        };
        
        final int SLICES=WHEEL_VALUES.length;

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
                if (grid[j][i] != 0) console.setColor(filledColor);
                else console.setColor(emptyColor);
                console.fillRect(x + squareSize*i, y + squareSize*j, squareSize-1, squareSize-1);
                console.setColor(Color.black);
                console.drawString("" + grid[j][i], (int)(x+squareSize*(i+0.3)), (int)(y + squareSize*(j+0.65)));
            }
        }
    }

    private void drawButton(int x, int y, String action, char key, boolean activated) {
        console.setColor(Color.black);
        console.drawRect(x, y, 200, 50);
        console.drawRect(x+150, y, 50, 50);
        if (activated) console.setColor(new Color(204, 255, 204));
        else console.setColor(Color.white);
        console.fillRect(x+151, y+1, 49, 49);
        console.setColor(Color.black);
        console.setFont(new Font("Arial", Font.PLAIN, 20));
        console.drawString(action, (int)(x+16), (int)(y+33));
        console.drawString("" + key, (int)(x+168), (int)(y+33));
    }

    private void drawInteractionArea()
    {
        console.setColor(Color.LIGHT_GRAY);
        console.fillRect(0,INTERACTION_AREA_Y,SIDEBAR_X,CONSOLE_HEIGHT-INTERACTION_AREA_Y);
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

    private void drawPlayerInfo(int x, int y, String name, int balance, boolean current_turn, boolean is_winner) {
        console.setColor(Color.black);
        console.drawRect(x, y, 200, 50);
        if (current_turn) console.setColor(new Color(255, 102, 102));
        else if (is_winner) console.setColor(new Color(204, 255, 204));
        else console.setColor(new Color(255, 204, 204));
        console.fillRect(x+1, y+1, 199, 49);
        console.setColor(Color.black);
        console.setFont(new Font("Arial", Font.PLAIN, 20));
        console.drawString(name + " - " + balance, (int)(x+16), (int)(y+33));
    }

    private void drawChatBox(List<String> lines) {
        console.setColor(Color.black);
        console.drawRect(CHAT_BOX_X, CHAT_BOX_Y, CHAT_BOX_WIDTH, CHAT_BOX_HEIGHT);
        console.setColor(new Color(255, 255, 204));
        console.fillRect(CHAT_BOX_X+1, CHAT_BOX_Y+1, CHAT_BOX_WIDTH-1, CHAT_BOX_HEIGHT-1);
        console.setColor(Color.black);
        console.setFont(new Font("Arial", Font.PLAIN, 16));
        for (int i = Math.max(0, lines.size()-CHAT_BOX_MAX_LINES); i<lines.size(); i++) {
            console.drawString(lines.get(i), CHAT_BOX_X+16, CHAT_BOX_Y+20 + i*20);
        }
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

    public String acceptString(String prompt,int lengthLimit)
    {
        while(true)
        {
            drawInteractionArea();
            console.setCursor(INTERACTION_AREA_ROW+1,INTERACTION_AREA_COL+1);
            console.print(prompt);
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

    public boolean newRound()
    {
        Set<String> phraseCategoriesSet = phrases.keySet();
        String[] phraseCategories = new String[phraseCategoriesSet.size()];
        phraseCategoriesSet.toArray(phraseCategories);

        int categoryIdx = acceptMenuChoice("Please choose a category of phrases to play with.", phraseCategories);
        String category = phraseCategories[categoryIdx];
        console.clear();

        char[][] grid = new char[8][25];
        drawLetterGrid(grid);

        drawHostPlatform(CHAT_BOX_X+CHAT_BOX_WIDTH*6/5, CHAT_BOX_Y+20);
        drawHost(CHAT_BOX_X+CHAT_BOX_WIDTH*6/5, CHAT_BOX_Y+20);

        List chatBoxLines=new ArrayList<String>();
        chatBoxLines.add(formatDialog("Host","Hey what's your name?"));
        drawChatBox(chatBoxLines);
        String player1Name=acceptString("What is the name of player 1? ",PLAYER_NAME_MAX_LEN);
        chatBoxLines.add(formatDialog(player1Name,"I'm "+player1Name+" and I am excited to win some money!"));
        drawChatBox(chatBoxLines);

        chatBoxLines.add(formatDialog("Host","Hey what's your name?"));
        drawChatBox(chatBoxLines);
        String player2Name=acceptString("What is the name of player 2? ",PLAYER_NAME_MAX_LEN);
        chatBoxLines.add(formatDialog(player2Name,"I'm "+player2Name+" and I know I will win!"));
        chatBoxLines.add(formatDialog("Host","You surely sound confident!"));
        drawChatBox(chatBoxLines);
        return false;
    }

    public static void main(String[] args) {
        WheelOfFortune game = new WheelOfFortune();
        game.newRound();
        // while(game.newRound());
        // game.goodbye();
        /*
        char[][] chardata = new char[6][24];
        chardata[2][2] = 'A';
        chardata[2][3] = 'B';
        chardata[2][4] = 'C';
        game.drawLetterGrid(10, 10, 30, chardata);
        game.drawButton(100, 500, "Button", 'X', false);
        game.drawButton(400, 500, "Action", 'Y', true);
        game.drawHost(600, 600);
        */
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

