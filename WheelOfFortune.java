/*
 * Name: Paul Lee and Peter Ye
 * Teacher: Ms Krasteva
 * Date: 2021 Jan 19 14:17
 * Assignment: Wheel of Fortune ISP
 */

/* Program Purpose: Recreate the Wheel of Fortune game in Ready to Program
 * Variables:
 * - Console console: reference to console
 *
 * Citations:
 * https://stackoverflow.com/questions/22839618/how-to-get-pixel-color-from-graphics-g
 * https://stackoverflow.com/questions/14124593/how-to-rotate-graphics-in-java
 * https://stackoverflow.com/questions/18073590/sort-list-in-reverse-in-order
 *
 * https://docs.oracle.com/javase/9/docs/api/java/awt/List.html
 * https://docs.oracle.com/javase/9/docs/api/java/lang/Comparable.html
 * https://docs.oracle.com/javase/9/docs/api/java/util/Set.html
 * https://docs.oracle.com/javase/9/docs/api/java/util/Map.html
 * https://docs.oracle.com/javase/9/docs/api/java/util/Collections.html
 * https://docs.oracle.com/javase/9/docs/api/java/awt/image/BufferedImage.html
 * https://docs.oracle.com/javase/9/docs/api/java/awt/Graphics2D.html
 *
 * https://www.geeksforgeeks.org/collections-shuffle-java-examples/
 * https://www.geeksforgeeks.org/java-touppercase-examples/
 * https://stackoverflow.com/questions/886955/how-do-i-break-out-of-nested-loops-in-java
 * https://hsa.larry.science/
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
import java.util.Random;


public class WheelOfFortune
{
    private Console console; // console window
    private Random rng; // random number generator

    public static final String PROGRAM_NAME = "Wheel of Fortune 1.0"; // name of the program

    // CHANGED FOR DEMONSTRATION PURPOSES
    public static final int NUMBER_OF_PHRASES = 3; // number of phrases per round

    // dimensions of console
    public static final int CONSOLE_WIDTH = 1024;
    public static final int CONSOLE_HEIGHT = 768;

    // font size for console
    public static final int FONT_SIZE = 20;

    // dimensions and position of letter grid
    public static final int LETTER_GRID_X = 10;
    public static final int LETTER_GRID_Y = 10;
    public static final int LETTER_GRID_CELL_SIZE = 30;
    public static final int LETTER_GRID_ROWS = 8;
    public static final int LETTER_GRID_COLS = 25;

    // dimensions and position of fullscreen letter grid
    public static final int FS_LETTER_GRID_X = 10;
    public static final int FS_LETTER_GRID_Y = 10;
    public static final int FS_LETTER_GRID_CELL_SIZE = 50;
    public static final int FS_LETTER_GRID_ROWS = 15;
    public static final int FS_LETTER_GRID_COLS = 20;

    // y-coordinate of interaction area
    public static final int INTERACTION_AREA_Y = CONSOLE_HEIGHT * 3 / 4;

    // dimensions and position of chat box
    public static final int CHAT_BOX_WIDTH = CONSOLE_WIDTH / 2;
    public static final int CHAT_BOX_HEIGHT = CONSOLE_HEIGHT / 4;
    public static final int CHAT_BOX_X = 10;
    public static final int CHAT_BOX_Y = INTERACTION_AREA_Y - CHAT_BOX_HEIGHT - 10;
    public static final int CHAT_BOX_MAX_LINES = 9;

    // dimensions and positon of remaining letters bar
    public static final int REMAINING_LETTERS_BAR_HEIGHT = CONSOLE_HEIGHT / 20;
    public static final int REMAINING_LETTERS_BAR_X = CHAT_BOX_X;
    public static final int REMAINING_LETTERS_BAR_Y = CHAT_BOX_Y - CONSOLE_HEIGHT / 8;

    // dimensions and position of sidebar
    public static final int SIDEBAR_X = CONSOLE_WIDTH * 3 / 4;
    public static final int SIDEBAR_WIDTH = CONSOLE_WIDTH / 4;
    public static final int SIDEBAR_ITEM_HEIGHT = 50;

    // coordinates of interaction area (in rows and columns)
    public final int INTERACTION_AREA_ROW;
    public final int INTERACTION_AREA_COL;

    // coordinates and radius of wheel
    public static final int WHEEL_X = SIDEBAR_X + SIDEBAR_WIDTH;
    public static final int WHEEL_Y = 0;
    public static final int WHEEL_RADIUS = SIDEBAR_WIDTH;

    // speed and acceleration of wheel
    public static final double WHEEL_INIT_VEL_LOWER_BOUND = Math.PI * 3;
    public static final double WHEEL_INIT_VEL_UPPER_BOUND = Math.PI * 7;
    public static final double WHEEL_ACCEL = -Math.PI * 2;

    // coordinates of player list
    public static final int PLAYER_LIST_X = SIDEBAR_X;
    public static final int PLAYER_LIST_Y = CONSOLE_HEIGHT - SIDEBAR_WIDTH - (int) (SIDEBAR_ITEM_HEIGHT * 4.5);

    // coordinates of category indicator
    public static final int CATEGORY_INDICATOR_X = SIDEBAR_X;
    public static final int CATEGORY_INDICATOR_Y = PLAYER_LIST_Y + (int) (SIDEBAR_ITEM_HEIGHT * 3.5);

    // maximum length of player name
    public static final int PLAYER_NAME_MAX_LEN = 10;

    // name of file storing phrases
    public static final String PHRASE_FILE_NAME = "phrases.wof_data";

    // name of file storing scores
    public static final String SCORE_FILE_NAME = "scores.wof";

    // name of the host of the game
    public static final String HOST_NAME = "Host";

    // constant values for the special sections of the wheel
    private static final int LOSE_TURN = -1;
    private static final int BANKRUPT = -2;
    private static final int COMMUNISM = -3;

    // prices of the sections of the wheel
    private static final int[] WHEEL_VALUES = {
        LOSE_TURN, 2500, 700, 600, 550,
        BANKRUPT, 600, 500, COMMUNISM, 800,
        LOSE_TURN, 800, 500, 900, 500};

    private Map phrases; // stores the phrases by category (open source, ICS, pandemic, etc)
    // key = String (category name)
    // value = List of Strings (list of phrases within a category)

    private Map phraseTypes; // stores the type of each phrase (noun, verb, adjective, etc)
    // key = String (phrase name)
    // value = String (type name)

    private List playerScores; // list of player scores (PlayerScore objects)

    // Class Constructor
    public WheelOfFortune ()
    {
        // create a test console to calculate number of pixels per row and column
        Console tmpConsole = new Console (1, 1, FONT_SIZE);

        int pxPerRow = tmpConsole.getHeight (); // number of pixels per console row
        int pxPerCol = tmpConsole.getWidth (); // number of pixels per console column
        tmpConsole.close (); // close test console

        // number of rows and columns in main console
        int consoleRows = CONSOLE_HEIGHT / pxPerRow;
        int consoleCols = CONSOLE_WIDTH / pxPerCol;

        // calculate row number of interaction area
        INTERACTION_AREA_ROW = INTERACTION_AREA_Y / pxPerRow + 1;
        INTERACTION_AREA_COL = 1;

        // create main console with custom title
        console = new Console (consoleRows, consoleCols, FONT_SIZE, PROGRAM_NAME);

        // initialize random number generator
        rng = new Random (12360666); // seed rng for DEMONSTRATION PURPOSES

        // initialize data structures
        phrases = new HashMap ();
        playerScores = new ArrayList ();
        phraseTypes = new HashMap ();

        // read phrases and previous scores from file
        readPhrasesFromFile ();
        readScoresFromFile ();

        // draw the splash screen animation
        drawSplashScreen ();

        Collections.sort (playerScores, Collections.reverseOrder ());
    }


    /* Draws the splash screen animation
     *
     * +---------------+-------------------------------+
     * |   Variable    |          Description          |
     * +---------------+-------------------------------+
     * | char[][] grid | letter grid for splash screen |
     * | String text   | splash screen text            |
     * | int row       | current grid row              |
     * | int col       | current grid column           |
     * +---------------+-------------------------------+
     */

    private void drawSplashScreen ()
    {
        char[] [] grid = new char [FS_LETTER_GRID_ROWS] [FS_LETTER_GRID_COLS];
        String text = "AMERICA'S\nFAVOURITE\nGAME";
        final int START_ROW = 1;
        final int START_COL = 5;
        int row = START_ROW, col = START_COL;
        for (int i = 0 ; i < text.length () ; ++i) // loop over text characters
        {
            char c = text.charAt (i);
            if (c == '\n') // newline; go to next row
            {
                ++row;
                col = START_COL;
            }
            else
            {
                grid [row] [col] = c; // copy character to grid
                ++col;
            }
            drawFullScreenLetterGrid (grid); // display grid
            try
            {
                Thread.sleep (100); // pause for 0.1 seconds
            }
            catch (Exception e)
            {
                e.printStackTrace ();
            }
        }
        for (double angle = 0 ; angle < 2 ; angle += 0.02) // rotate wheel
        {
            drawWheel (angle, CONSOLE_WIDTH / 2, CONSOLE_HEIGHT * 3 / 5, CONSOLE_HEIGHT * 3 / 10); // draw wheel
            try
            {
                Thread.sleep (20);
            }
            catch (Exception e)
            {
                e.printStackTrace ();
            }
        }
    }


    /* Draws the base of the wheel to a Graphics2D object
     *
     * +-----------------------+------------------------------------------+
     * |       Variable        |               Description                |
     * +-----------------------+------------------------------------------+
     * | Graphics2D graphics   | object to be drawn onto                  |
     * | Color[] WHEEL_COLORS  | colour of each wheel section             |
     * | int radius            | radius of wheel                          |
     * | double angle          | current angle of wheel (in radians)      |
     * | int SLICES            | number of wheel slices                   |
     * | int linesToDraw       | number of radial lines to draw           |
     * | int linesPerSlice     | number of radial lines to draw per slice |
     * | int xPrev             | x-coordinate of radial line              |
     * | int yPrev             | y-coordinate of radial line              |
     * +-----------------------+------------------------------------------+
     */
    private void drawWheelBaseToGraphics (Graphics2D graphics, Color[] WHEEL_COLORS, int radius, double angle)
    {
        final int SLICES = WHEEL_VALUES.length;
        int linesToDraw = (int) (2 * Math.PI * radius);
        int linesPerSlice = linesToDraw / SLICES + 1;

        // previous radial line (used in loop)
        int xPrev = (int) (Math.cos (angle) * radius) + radius;
        int yPrev = (int) (Math.sin (angle) * radius) + radius;

        for (int i = 0 ; i < linesToDraw ; ++i) // draw wheel using thin radial triangles
        {
            double lineAngle = angle + 2 * Math.PI * i / linesToDraw; // angle of line
            graphics.setColor (WHEEL_COLORS [i / linesPerSlice]);
            if (i % linesPerSlice < 3) // separate sections of wheel with solid black line
            {
                graphics.setColor (Color.BLACK);
            }

            // new radial line
            int xNew = (int) (Math.cos (lineAngle) * radius) + radius;
            int yNew = (int) (Math.sin (lineAngle) * radius) + radius;

            // draw triangle
            graphics.fillPolygon (new int[]
            {
                radius, xPrev, xNew
            }
            , new int[]
            {
                radius, yPrev, yNew
            }
            , 3);

            xPrev = xNew;
            yPrev = yNew;
        }
        graphics.setColor (Color.BLACK);
        for (int i = 0 ; i < linesToDraw ; ++i) // draw border of wheel using small circles
        {
            double lineAngle = angle + 2 * Math.PI * i / linesToDraw;
            // calculate position of border
            int xBorder = (int) (Math.cos (lineAngle) * (radius - 1)) + radius;
            int yBorder = (int) (Math.sin (lineAngle) * (radius - 1)) + radius;
            graphics.fillOval (xBorder - 2, yBorder - 2, 4, 4); // draw circle
        }

    }


    /*
     * Draws the remaining letters which haven't been guessed yet
     *
     * +---------------+------------------------------------------------------+
     * |   Variable    |                     Description                      |
     * +---------------+------------------------------------------------------+
     * | Set available | Set containing the letters that can still be guessed |
     * | int letterX   | x-coordinate of letter                               |
     * | int letterY   | y-coordinate of letter                               |
     * +---------------+------------------------------------------------------+
     */
    private void drawAvailableLetters (Set available)
    {
        console.setFont (new Font ("Arial", Font.PLAIN, REMAINING_LETTERS_BAR_HEIGHT / 2));

        for (int i = 0 ; i < 26 ; ++i) // iterate over letters of alphabet
        {
            int letterX, letterY;
            if (i < 13) // first row of letters
            {
                letterX = REMAINING_LETTERS_BAR_X + i * REMAINING_LETTERS_BAR_HEIGHT;
                letterY = REMAINING_LETTERS_BAR_Y;
            }
            else // second row of letters
            {
                letterX = REMAINING_LETTERS_BAR_X + (i - 13) * REMAINING_LETTERS_BAR_HEIGHT;
                letterY = REMAINING_LETTERS_BAR_Y + REMAINING_LETTERS_BAR_HEIGHT;
            }

            // draw bubble for letter
            console.setColor (Color.LIGHT_GRAY);
            console.fillOval (letterX - REMAINING_LETTERS_BAR_HEIGHT / 3, letterY - REMAINING_LETTERS_BAR_HEIGHT * 2 / 3, REMAINING_LETTERS_BAR_HEIGHT, REMAINING_LETTERS_BAR_HEIGHT);

            char c = (char) ('A' + i);
            if (available.contains (new Character (c))) // only draw letter if it is available
            {
                console.setColor (Color.BLACK);
                console.drawString (Character.toString (c), letterX, letterY); // draw letter
            }
        }
    }


    /* Draws the text for the wheel to a Graphics2D object
     *
     *
     * +---------------------+--------------------------------+
     * |      Variable       |          Description           |
     * +---------------------+--------------------------------+
     * | Graphics2D graphics | graphics object to be drawn on |
     * | int radius          | radius of wheel                |
     * | double angle        | angle of rotation              |
     * | int SLICES          | number of wheel slices         |
     * | String sliceStr     | the text of a wheel slice      |
     * +---------------------+--------------------------------+
     */
    private void drawWheelTextToGraphics (Graphics2D graphics, int radius, double angle)
    {
        // rotate the graphics object to allow for rotated text
        graphics.rotate (angle - Math.PI * 0.3, radius, radius);

        graphics.setFont (new Font ("Arial", Font.BOLD, radius / 12));
        final int SLICES = WHEEL_VALUES.length;
        for (int i = 0 ; i < SLICES ; ++i) // iterates over the slices of the wheel
        {
            if (WHEEL_VALUES [i] == BANKRUPT) // BANKRUPT is special because it uses white text, instead of the usual black text
            {
                graphics.setColor (Color.WHITE);
            }
            else // any other slice uses black text
            {
                graphics.setColor (Color.BLACK);
            }
            String sliceStr;

            // check if wheel value is a special constant
            if (WHEEL_VALUES [i] == BANKRUPT)
            {
                sliceStr = "BANKRUPT";
            }
            else if (WHEEL_VALUES [i] == LOSE_TURN)
            {
                sliceStr = "LOSE A TURN";
            }
            else if (WHEEL_VALUES [i] == COMMUNISM)
            {
                sliceStr = "COMMUNISM";
            }
            else // wheel value is a regular price
            {
                sliceStr = "$" + WHEEL_VALUES [i]; // append dollar sign to the front of the number
            }

            // draw the text for the current slice
            graphics.drawString (sliceStr, radius / 8, radius);

            // rotate the graphics object before drawing next slice
            graphics.rotate (2 * Math.PI / SLICES, radius, radius);
        }

    }


    private void drawWheel (double angle)
    {
        drawWheel (angle, WHEEL_X, WHEEL_Y, WHEEL_RADIUS);
    }


    /* Draws the spinning wheel for the game
     *
     * +-----------------------------+-------------------------------+
     * |          Variable           |          Description          |
     * +-----------------------------+-------------------------------+
     * | double angle                | angle of rotation             |
     * | Color[] WHEEL_COLORS        | colours of the wheel sections |
     * | BufferedImage bufferedImage | image to draw wheel onto      |
     * | Graphics2D graphics         | the graphics of bufferedImage |
     * +-----------------------------+-------------------------------+
     */
    private void drawWheel (double angle, int x, int y, int radius)
    {
        final Color[] WHEEL_COLORS = {
            Color.LIGHT_GRAY, Color.ORANGE, Color.GREEN, Color.YELLOW, Color.PINK,
            Color.LIGHT_GRAY, Color.CYAN, Color.RED, Color.YELLOW, Color.ORANGE,
            Color.BLACK, Color.CYAN, Color.GREEN, Color.RED, Color.PINK
            };

        BufferedImage bufferedImage = new BufferedImage (radius * 2, radius * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics (); // get graphics from bufferedImage
        drawWheelBaseToGraphics (graphics, WHEEL_COLORS, radius, angle); // draw wheel base to graphics
        drawWheelTextToGraphics (graphics, radius, angle); // draw wheel text to graphics

        console.drawImage (bufferedImage, x - radius, y - radius, null); // draw the bufferedImage to the console window
    }


    // overload of drawLetterGrid that also draws the available letters
    private void drawLetterGrid (char[] [] grid, Set available)
    {
        drawLetterGrid (grid, LETTER_GRID_X, LETTER_GRID_Y, LETTER_GRID_CELL_SIZE);
        drawAvailableLetters (available);
    }


    // wrapper around drawLetterGrid to make it take up the entire screen
    private void drawFullScreenLetterGrid (char[] [] grid)
    {
        drawLetterGrid (grid,
                FS_LETTER_GRID_X,
                FS_LETTER_GRID_Y,
                FS_LETTER_GRID_CELL_SIZE);
    }


    /* Draws grid of letters for the game
     *
     * +---------------+----------------------+
     * |   Variable    |     Description      |
     * +---------------+----------------------+
     * | char[][] grid | grid of letters      |
     * | int x         | x-coordinate of grid |
     * | int y         | y-coordinate of grid |
     * +---------------+----------------------+
     */
    private void drawLetterGrid (char[] [] grid, int x, int y, int squareSize)
    {
        console.setFont (new Font ("Arial", Font.PLAIN, squareSize / 2));
        Color emptyColor = new Color (153, 204, 255);
        Color filledColor = new Color (242, 242, 242);
        for (int i = 0 ; i < grid [0].length ; i++)
        { // rows of grid
            for (int j = 0 ; j < grid.length ; j++)
            { // columns of grid
                if (grid [j] [i] == 0)
                    console.setColor (emptyColor);                     // grid cell is empty
                else
                    console.setColor (filledColor);     // grid cell not empty
                if (grid [j] [i] == '_')
                    grid [j] [i] = ' ';
                console.fillRect (x + squareSize * i, y + squareSize * j, squareSize - 1, squareSize - 1); // draw grid cell rectangle
                if (grid [j] [i] != 0) // if grid cell not empty
                {
                    console.setColor (Color.black);
                    console.drawString ("" + grid [j] [i], (int) (x + squareSize * (i + 0.3)), (int) (y + squareSize * (j + 0.65))); // draw letter
                }
            }
        }
    }


    /* Converts a string to a character grid
     *
     * +---------------+----------------------------------+
     * |   Variable    |           Description            |
     * +---------------+----------------------------------+
     * | String str    | String to be processed           |
     * | int rows      | number of rows                   |
     * | int cols      | number of columns                |
     * | char[][] grid | character grid                   |
     * | words         | String split into separate words |
     * +---------------+----------------------------------+
     */
    private char[] [] stringToCharacterGrid (String str, int rows, int cols)
    {
        char[] [] grid = new char [rows] [cols];
        String[] words = str.split ("\0"); // split str
        int currentRow = 0, currentCol = 0; // current position in grid
        for (int i = 0 ; i < words.length ; ++i) // iterate over the words
        {
            String currentWord = words [i].toUpperCase ();
            while (currentRow < rows)
            {
                if (cols - currentCol >= currentWord.length ()) // check if there is enough space in row
                {
                    for (int j = 0 ; j < currentWord.length () ; ++j) // copy word over to grid
                    {
                        grid [currentRow] [currentCol + j] = currentWord.charAt (j); // copy a character
                    }
                    currentCol += currentWord.length () + 1;
                    break;
                }
                else
                {
                    ++currentRow; // increment row
                    currentCol = 0; // move to first column
                }
            }
        }
        return grid; // return newly created grid
    }


    /*
     * Draws a button, identified by a key which the user can presson their keyboard
     * +-------------------+---------------------------------------+
     * |     Variable      |              Description              |
     * +-------------------+---------------------------------------+
     * | int x             | x-coordinate of button                |
     * | int y             | y-coordinate of button                |
     * | String action     | text on button                        |
     * | char key          | button identifier                     |
     * | boolean activated | whether the button has been activated |
     * +-------------------+---------------------------------------+
     */
    private void drawButton (int x, int y, String action, char key, boolean activated)
    {
        console.setColor (Color.black);
        console.drawRect (x, y, 200, 50); // button border
        console.drawRect (x + 150, y, 50, 50);
        if (activated)
            console.setColor (new Color (204, 255, 204));              // change button colour if activated
        else
            console.setColor (Color.white);
        console.fillRect (x + 1, y + 1, 149, 49);
        console.fillRect (x + 151, y + 1, 49, 49);
        console.setColor (Color.black);
        console.setFont (new Font ("Arial", Font.PLAIN, 20));
        console.drawString (action, (int) (x + 16), (int) (y + 33)); // draw button action name
        console.drawString ("" + key, (int) (x + 168), (int) (y + 33)); // draw button key
    }


    /* Draws the host of the game
     *
     * +----------+----------------------+
     * | Variable |     Description      |
     * +----------+----------------------+
     * | int x    | x-coordinate of host |
     * | int y    | y-coordinate of host |
     * +----------+----------------------+
     */
    private void drawHost (int x, int y)
    {
        console.setColor (Color.black);
        console.fillOval (x, y, 50, 50); // circular head
        console.fillRect (x + 5, y + 45, 40, 100); // rectangular body
    }


    /* Draws the platform which the host stands on
     *
     * +----------+--------------------------+
     * | Variable |       Description        |
     * +----------+--------------------------+
     * | int x    | x-coordinate of platform |
     * | int y    | y-coordinate of platform |
     * +----------+--------------------------+
     */
    private void drawHostPlatform (int x, int y)
    {
        console.setColor (Color.gray);
        console.fillOval (x - 25, y + 120, 100, 50);
    }


    /* Draws a generic sidebar widget
     *
     * +-------------+------------------------------+
     * |  Variable   |         Description          |
     * +-------------+------------------------------+
     * | int x       | x-coordinate of widget       |
     * | int y       | y-coordinate of widget       |
     * | String text | text displayed on widget     |
     * | Color color | background colour for widget |
     * +-------------+------------------------------+
     */
    private void drawGenericSidebarWidget (int x, int y, String text, Color color)
    {
        console.setColor (Color.black);
        console.drawRect (x, y, SIDEBAR_WIDTH, SIDEBAR_ITEM_HEIGHT);
        console.setColor (color);
        console.fillRect (x + 1, y + 1, SIDEBAR_WIDTH - 1, SIDEBAR_ITEM_HEIGHT - 1);
        console.setColor (Color.black);
        console.setFont (new Font ("Arial", Font.PLAIN, 20));
        console.drawString (text, (int) (x + 16), (int) (y + SIDEBAR_ITEM_HEIGHT * 33 / 50.0));
    }


    // overload of drawGenericSidebarWidget() using default colour
    private void drawGenericSidebarWidget (int x, int y, String text)
    {
        drawGenericSidebarWidget (x, y, text, new Color (255, 204, 204));
    }


    // wrapper around drawGenericSidebarWidget() to draw the heading for the sidebar
    private void drawSidebarHeading (int x, int y, String text)
    {
        drawGenericSidebarWidget (x, y, text, new Color (255, 153, 102));
    }


    /* Draws the info (balance, name, current turn, etc) of a player
     *
     * +---------------------+------------------------------+
     * |      Variable       |         Description          |
     * +---------------------+------------------------------+
     * | int playerID        | ID of player (either 1 or 2) |
     * | String name         | player's name                |
     * | int balance         | player's balance             |
     * | boolean currentTurn | is it the player's turn      |
     * | boolean isWinner    | did the player win           |
     * +---------------------+------------------------------+
     */
    private void drawPlayerInfo (int playerID, String name, int balance, boolean currentTurn, boolean isWinner)
    {
        Color color;
        // change color based on player status
        if (currentTurn)
            color = new Color (255, 204, 102);
        else if (isWinner)
            color = new Color (204, 255, 204);
        else
            color = new Color (255, 204, 204);
        drawGenericSidebarWidget (PLAYER_LIST_X, PLAYER_LIST_Y + SIDEBAR_ITEM_HEIGHT * playerID, name + " - $" + balance, color);
    }


    // overload of drawPlayerInfo() using default of false for isWinner
    private void drawPlayerInfo (int playerID, String name, int balance, boolean currentTurn)
    {
        drawPlayerInfo (playerID, name, balance, currentTurn, false);
    }


    /* Draws the scrolling chat box
     *
     * +----------------+--------------------------------------+
     * |    Variable    |             Description              |
     * +----------------+--------------------------------------+
     * | List lines     | List of lines                        |
     * | int topMessage | index of top message to be displayed |
     * +----------------+--------------------------------------+
     */
    private void drawChatBox (List lines)
    {
        console.setColor (Color.black);
        console.drawRect (CHAT_BOX_X, CHAT_BOX_Y, CHAT_BOX_WIDTH, CHAT_BOX_HEIGHT);
        console.setColor (new Color (255, 255, 204));
        console.fillRect (CHAT_BOX_X + 1, CHAT_BOX_Y + 1, CHAT_BOX_WIDTH - 1, CHAT_BOX_HEIGHT - 1);
        console.setColor (Color.black);
        console.setFont (new Font ("Arial", Font.PLAIN, 16));
        int topMessage = Math.max (0, lines.size () - CHAT_BOX_MAX_LINES); // find top message
        for (int i = 0 ; i < Math.min (CHAT_BOX_MAX_LINES, lines.size ()) ; i++)
        { // iterate over messages
            console.drawString ((String) lines.get (i + topMessage), CHAT_BOX_X + 16, CHAT_BOX_Y + 20 + (i) * 20); // draw message
        }
    }


    // Draws the sidebar
    private void drawSidebar ()
    {
        console.setColor (new Color (255, 102, 102));
        console.fillRect (SIDEBAR_X, 0, SIDEBAR_WIDTH, INTERACTION_AREA_Y);

        drawSidebarHeading (PLAYER_LIST_X, PLAYER_LIST_Y, "Players");
        drawPlayerInfo (1, "???", 0, false, false);
        drawPlayerInfo (2, "???", 0, false, false);

        drawSidebarHeading (CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y, "Category");
        drawGenericSidebarWidget (CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y + SIDEBAR_ITEM_HEIGHT, "???");

        drawWheel (0);
        console.setColor (new Color (100, 100, 100));
        console.fillOval (WHEEL_X - WHEEL_RADIUS * 13 / 16, WHEEL_Y + WHEEL_RADIUS * 13 / 16, 10, 10);
    }


    // Parses a category marker from a String line
    private String parseCategoryMarker (String line)
    { // returns the category name if line is a category marker
        // returns an empty string otherwise
        if (line.length () == 0)
        {
            return ""; // return empty string
        }
        else if (!line.startsWith ("- ") && line.charAt (line.length () - 1) == ':')
        {
            return line.substring (0, line.length () - 1); // remove trailing ':'
        }
        else
        {
            return ""; // return empty string
        }
    }


    // Parses a type marker from a String line
    private String parseTypeMarker (String line)
    { // returns the type marker if line is a type marker
        // returns an empty string otherwise
        if (line.length () == 0)
        {
            return ""; // return empty string
        }
        else if (line.startsWith ("- ") && line.charAt (line.length () - 1) == ':')
        {
            return line.substring (2, line.length () - 1).trim (); // remove leading '-' and trailing ':'

        }
        else
        {
            return ""; // return empty string
        }
    }


    // Parses a phrase from a String line
    private String parsePhrase (String line)
    { // returns the phrase if line is a phrase
        // returns an empty string otherwise
        if (line.length () == 0)
        {
            return ""; // return empty string
        }
        else if (line.startsWith ("- "))
        {
            return line.substring (2, line.length ()).trim (); // remove leading '-'
        }
        else
        {
            return ""; // return empty string
        }
    }


    /* Adds a phrase to the phrase database
     *
     * +----------------------+-------------------------------+
     * |       Variable       |          Description          |
     * +----------------------+-------------------------------+
     * | String phrase        | the phrase                    |
     * | String category      | category of phrase            |
     * | String type          | type of phrase                |
     * | List categoryPhrases | list of phrases in a category |
     * +----------------------+-------------------------------+
     */
    private void addPhraseToMap (String phrase, String category, String type)
    {
        if (!phrases.containsKey (category)) // initialize category if it doesn't exist yet
        {
            phrases.put (category, new ArrayList ()); // initialize empty ArrayList
        }
        List categoryPhrases = (List) phrases.get (category); // fetch list of phrases in category
        categoryPhrases.add (phrase); // add phrase to category
        phraseTypes.put (phrase, type); // add phrase to phraseTypes
    }


    /* Reads the phrases from phrase file
     *
     * +------------------------+----------------------------------------+
     * |        Variable        |              Description               |
     * +------------------------+----------------------------------------+
     * | BufferedReader input   | input stream                           |
     * | String line            | line in file                           |
     * | String currentCategory | the category currently being processed |
     * | String currentType     | the type currently being processed     |
     * +------------------------+----------------------------------------+
     */
    private void readPhrasesFromFile ()
    {
        try
        {
            BufferedReader input = new BufferedReader (new FileReader (PHRASE_FILE_NAME)); // open input stream
            String line;
            // initialize category and type
            String currentCategory = "DEFAULT";
            String currentType = "DEFAULT";
            while ((line = input.readLine ()) != null) // keep on reading until end of file
            {
                line = line.trim ();

                //attempt to parse a category marker and a type marker from line
                String categoryMarker = parseCategoryMarker (line);
                String typeMarker = parseTypeMarker (line);
                if (categoryMarker.length () > 0) // category marker found
                {
                    currentCategory = categoryMarker;
                }
                else if (typeMarker.length () > 0) // type marker found
                {
                    currentType = typeMarker;
                }
                else
                { // phrase found
                    String phrase = parsePhrase (line);
                    if (phrase.length () > 0)
                    {
                        addPhraseToMap (phrase, currentCategory, currentType); // add phrase to phrase database
                    }
                }
            }
            input.close (); // close input stream
        }
        catch (IOException e)  // exception occurred when reading from file
        {
            e.printStackTrace (); // display stack trace
        }
    }


    // Parse a PlayerScore from a line
    private PlayerScore parseScore (String line)
    {
        line = line.trim (); // trim the line
        String[] parts = line.split ("\\s*,\\s*"); // split line into parts
        if (parts.length != 2) // not a PlayerScore; return null
        {
            return null;
        }
        else // is a PlayerScore
        {
            return new PlayerScore (parts [0], Integer.parseInt (parts [1])); // construct new PlayerScore and return it
        }
    }


    /* Read previous player scores from a file
     *
     * +----------------------+-----------------------+
     * |       Variable       |      Description      |
     * +----------------------+-----------------------+
     * | BufferedReader input | input stream          |
     * | String line          | line in file          |
     * | PlayerScore score    | the score of a player |
     * +----------------------+-----------------------+
     */
    private void readScoresFromFile ()
    {
        try
        {
            BufferedReader input = new BufferedReader (new FileReader (SCORE_FILE_NAME)); // open input stream
            String line;
            while ((line = input.readLine ()) != null) // read until end of file
            {
                PlayerScore score = parseScore (line); // attempt to parse PlayerScore from line
                if (score != null)
                {
                    playerScores.add (score); // add score to list of player scores
                }
            }
            input.close (); // close input stream
        }
        catch (IOException e)  // exception occurred when reading from file
        {
            e.printStackTrace (); // display stack trace
        }
    }


    /* Writes player scores to file
     *
     * +-------------------------+-----------------------+
     * |        Variable         |      Description      |
     * +-------------------------+-----------------------+
     * | PrintWriter output      | output stream         |
     * | PlayerScore playerScore | the score of a player |
     * +-------------------------+-----------------------+
     */
    private void writeScoresToFile ()
    {
        try
        {
            PrintWriter output = new PrintWriter (new FileWriter (SCORE_FILE_NAME)); // open output stream
            for (int i = 0 ; i < playerScores.size () ; ++i) // iterate over player scores
            {
                PlayerScore playerScore = (PlayerScore) playerScores.get (i);
                output.println (playerScore.playerName + "," + playerScore.score); // output player score to file
            }
            output.close (); // close output stream
        }
        catch (IOException e)  // exception occurred when writing to file
        {
            e.printStackTrace (); // display stack trace
        }
    }


    /* formats a dialog message
     *
     * +----------------+-----------------+
     * |    Variable    |   Description   |
     * +----------------+-----------------+
     * | String speaker | name of speaker |
     * | String message | message text    |
     * +----------------+-----------------+
     */
    public String formatDialog (String speaker, String message)
    {
        return speaker + ": " + message;
    }


    // draws the interaction area rectangle
    public void drawInteractionArea ()
    {
        console.setColor (Color.LIGHT_GRAY);
        console.fillRect (0, INTERACTION_AREA_Y, CONSOLE_WIDTH, CONSOLE_HEIGHT - INTERACTION_AREA_Y);
    }


    // draws a String str to the interaction area
    public void drawToInteractionArea (String str)
    {
        drawInteractionArea (); // draw interaction area rectangle
        console.setCursor (INTERACTION_AREA_ROW + 1, INTERACTION_AREA_COL); // move cursor
        console.print (str); // print str
    }


    /* accepts a string from the interaction area
     *
     * +-----------------+--------------------------+
     * |    Variable     |       Description        |
     * +-----------------+--------------------------+
     * | String prompt   | prompt given to the user |
     * | int lengthLimit | maximum length of input  |
     * | String answer   | user input               |
     * +-----------------+--------------------------+
     */
    public String acceptString (String prompt, int lengthLimit)
    {
        while (true) // loop until user enters a valid string
        {
            drawToInteractionArea (prompt); // draw prompt to interaction area
            String answer = console.readLine (); // read string from console
            if (answer.length () > lengthLimit) // answer too long; try again
            {
                console.println ("The string which you entered was too long! Please try again.\nPress any key to continue ...");
                console.getChar (); // wait for user to press key
                continue;
            }
            else // answer is valid; return answer
            {
                return answer;
            }
        }
    }


    /* accept a choice from user (drawn over interaction area)
     * +------------------+--------------------------+
     * |     Variable     |       Description        |
     * +------------------+--------------------------+
     * | String prompt    | prompt given to the user |
     * | String[] choices | list of choices          |
     * +------------------+--------------------------+
     */
    public int acceptChoice (String prompt, String[] choices)
    {
        console.setFont (new Font ("Arial", Font.PLAIN, 15));
        drawToInteractionArea (prompt); // draw prompt to interaction area
        for (int i = 0 ; i < choices.length ; i++)
        {
            // indicate choice using a button
            drawButton (100 + (i % 3) * 250, INTERACTION_AREA_Y + (CONSOLE_HEIGHT - INTERACTION_AREA_Y) / 2 + (i / 3) * 100, choices [i], (char) (i + '1'), false);
        }
        // available keys for user to press
        char[] availableLetters = new char [choices.length];
        for (int i = 0 ; i < choices.length ; i++)
            availableLetters [i] = (char) (i + '1');
        return (int) (acceptChar (availableLetters) - '1'); // return index of choice
    }


    /* accept a character from user
     *
     * +------------------+-------------------------+
     * |     Variable     |       Description       |
     * +------------------+-------------------------+
     * | char[] available | available characters    |
     * | char input       | character input by user |
     * +------------------+-------------------------+
     */
    public char acceptChar (char[] available)
    {
        char input = console.getChar (); // get input
        for (int i = 0 ; i < available.length ; i++)
            if (input == available [i])
                return input;                                                                   // if input character is available, then return it

        // otherwise, ask the user to try again
        new Message ("Please enter a valid option.");
        return acceptChar (available);
    }


    /* accept a menu choice from user
     * +------------------+--------------------------+
     * |     Variable     |       Description        |
     * +------------------+--------------------------+
     * | String prompt    | prompt given to the user |
     * | String[] choices | list of choices          |
     * +------------------+--------------------------+
     */
    public int acceptMenuChoice (String prompt, String[] choices)
    {
        console.clear ();
        console.setFont (new Font ("Arial", Font.PLAIN, 15));
        console.println (prompt); // print prompt to console
        for (int i = 0 ; i < choices.length ; i++)
        {
            // indicate choice using a button
            drawButton (50 + (i % 3) * 250, 300 + (i / 3) * 100, choices [i], (char) (i + '1'), false);
        }
        // available keys for user to press
        char[] availableLetters = new char [choices.length];
        for (int i = 0 ; i < choices.length ; i++)
            availableLetters [i] = (char) (i + '1');
        return (int) (acceptChar (availableLetters) - '1'); // return index of choice
    }


    // wait for the user to press any key to continue
    public void pauseProgram ()
    {
        drawToInteractionArea ("Press any key to continue.");
        console.getChar ();
    }


    /* Main menu of program
     * Returns true if the user wants to exit the game, false otherwise
     *
     * +--------------------------+--------------------------+
     * |         Variable         |       Description        |
     * +--------------------------+--------------------------+
     * | String[] mainMenuChoices | list of choices for user |
     * | int menuChoice           | choice selected by user  |
     * +--------------------------+--------------------------+
     */
    public boolean mainMenu ()
    {
        String[] mainMenuChoices = {"New round", "Leaderboard", "Instructions", "Exit game"};
        int menuChoice = acceptMenuChoice ("Please select what you want to do.", mainMenuChoices);
        console.clear ();
        if (menuChoice == 0)
        {
            if (newRound ())
                return true;                 // user chose to exit within newRound(); return true
        }
        else if (menuChoice == 1)
        {
            leaderboard (); // display leaderboard
        }
        else if (menuChoice == 2)
        {
            instructions (); // display instructions
        }
        else
        {
            return true; // exit game
        }
        return false; // do not exit game
    }


    // +---------------------------------+---------------------------------------------------------------------------------+
    // |            Variable             |                                   Description                                   |
    // +---------------------------------+---------------------------------------------------------------------------------+
    // | Set phraseCategoriesSet         | Stores a set of categories available                                            |
    // | String[] phraseCategories       | Stores the same data a sphraseCategoriesSet but in a String array               |
    // | int categoryIdx                 | Index of the category chosen on phraseCategories                                |
    // | String category                 | The name of the category                                                        |
    // | List chatBoxLines               | A list to store the chat log of the round                                       |
    // | String player1Name              | Stores the name chosen by player 1                                              |
    // | int player1Balance              | Stores the balance of player 1                                                  |
    // | String player2Name              | Stores the name chosen by player 2                                              |
    // | int player2Balance              | Stores the balance of player 2                                                  |
    // | List phrasesInCategory          | List of phrases available in the category                                       |
    // | int phraseIndex                 | Index of the current phrase on the list of the shuffled terms from the category |
    // | double spd                      | Stores the current speed of the wheel
    // | String phraseType               | Stores the type of the current phrase                                           |
    // | Set availableLetters            | A set to store the set of available letters to the user to guess                |
    // | boolean newPhrase               | Used to indicate if a new phrase is needed                                      |
    // | boolean player1Turn             | Used to store which player's turn it is                                         |
    // | double angle                    | Used to store the angle of the wheel rendered                                   |
    // | double spd                      | Stores the current speed of the wheel                                           |
    // | int letterProfit                | Used to store the winnings for the current spin for the user                    |
    // | String currentPlayerName        | Used to store the current player's name                                         |
    // | String otherPlayerName          | Used to store the other player's name                                           |
    // | int playerAction                | Stores which action the user took for the current spin                          |
    // +---------------------------------+---------------------------------------------------------------------------------+
    public boolean newRound ()
    {
        // Get a list of categories for the user to choose from
        Set phraseCategoriesSet = phrases.keySet ();
        String[] phraseCategories = new String [phraseCategoriesSet.size ()];
        phraseCategoriesSet.toArray (phraseCategories);

        // Prompt the user to select a category of phrases
        int categoryIdx = acceptMenuChoice ("Please choose a category of phrases to play with.", phraseCategories);
        String category = phraseCategories [categoryIdx];
        console.clear ();



        // Display the letter grid
        drawLetterGrid (new char [LETTER_GRID_ROWS] [LETTER_GRID_COLS], new HashSet ());

        // Draw the Host
        drawHostPlatform (CHAT_BOX_X + CHAT_BOX_WIDTH * 6 / 5, CHAT_BOX_Y + 20);
        drawHost (CHAT_BOX_X + CHAT_BOX_WIDTH * 6 / 5, CHAT_BOX_Y + 20);

        // Draw the sidebar
        drawSidebar ();
        drawGenericSidebarWidget (CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y + SIDEBAR_ITEM_HEIGHT, category);

        // Ask for names from both player 1 and player 2
        List chatBoxLines = new ArrayList ();
        chatBoxLines.add (formatDialog (HOST_NAME, "Hey what's your name?"));
        drawChatBox (chatBoxLines);
        String player1Name = acceptString ("What is the name of player 1? ", PLAYER_NAME_MAX_LEN);
        int player1Balance = 0;
        drawPlayerInfo (1, player1Name, player1Balance, false);
        chatBoxLines.add (formatDialog (player1Name, "I'm " + player1Name + " and I am excited to win some money!"));
        drawChatBox (chatBoxLines);


        chatBoxLines.add (formatDialog (HOST_NAME, "Hey what's your name?"));
        drawChatBox (chatBoxLines);
        String player2Name = acceptString ("What is the name of player 2? ", PLAYER_NAME_MAX_LEN);
        int player2Balance = 0;
        chatBoxLines.add (formatDialog (player2Name, "I'm " + player2Name + " and I know I will win!"));
        chatBoxLines.add (formatDialog (HOST_NAME, "You surely sound confident!"));
        drawChatBox (chatBoxLines);

        // Get a list of phrases available
        List phrasesInCategory = (List) phrases.get (category);
        // Shuffle the list of phrases to ensure they are displayed in random order
        Collections.shuffle (phrasesInCategory, rng);
        // Variables to store each phrases
        int phraseIndex = -1;
        String phrase = "";
        String phraseType = "";
        // Character list to store a list of characters guessed
        char[] currentlyGuessed = new char [0];
        // A set to store the letters available for the user to guess
        Set availableLetters = new HashSet ();
        boolean newPhrase = true;

        // Variable to store if it is player1Turn yet
        boolean player1Turn = true;
        // Store the angle of the wheel
        double angle = 0;
        // Store the speed of the wheel
        double spd;
        int letterProfit;
        String currentPlayerName, otherPlayerName;

        int playerAction;
        String[] playerTurnChoices = {"Guess letter", "Solve puzzle", "Exit round"};

        int loop = 0;
        while (loop < NUMBER_OF_PHRASES)
        {
            if (newPhrase)
            {
                // Fetch a new phrase
                phraseIndex++;
                // Fetch a phrase (in order every time a new one is requested)
                phrase = ((String) (phrasesInCategory.get (phraseIndex)));
                // Fetch the type of the phrase
                phraseType = (String) (phraseTypes.get (phrase));
                phrase = phrase.toUpperCase ();
                currentlyGuessed = new char [phrase.length ()];
                // Format the phrase so that the letter grid can correctly display it on the screen
                for (int i = 0 ; i < phrase.length () ; ++i)
                {
                    char c = phrase.charAt (i);
                    if (Character.isLetter (c))
                    {
                        currentlyGuessed [i] = '_';
                    }
                    else if (Character.isSpaceChar (c))
                    {
                        currentlyGuessed [i] = 0;
                    }
                    else
                    {
                        currentlyGuessed [i] = c;
                    }
                }

                // Reset the letters available
                availableLetters = new HashSet ();
                for (char c = 'A' ; c <= 'Z' ; ++c)
                {
                    availableLetters.add (new Character (c));
                }

                // Render the letter grid on the screen
                drawLetterGrid (
                        stringToCharacterGrid (
                            new String (currentlyGuessed),
                            LETTER_GRID_ROWS,
                            LETTER_GRID_COLS
                            ),
                        availableLetters
                        );

                newPhrase = false;
                ++loop;

                // Alert the user of the new phrase
                drawGenericSidebarWidget (CATEGORY_INDICATOR_X, CATEGORY_INDICATOR_Y + SIDEBAR_ITEM_HEIGHT, category + " / " + phraseType);

                chatBoxLines.add (formatDialog (HOST_NAME, "New puzzle!"));
                chatBoxLines.add (formatDialog (HOST_NAME, "This one is a " + phraseType + "."));
                drawChatBox (chatBoxLines);
            }

            // Render user information
            drawPlayerInfo (1, player1Name, player1Balance, player1Turn);
            drawPlayerInfo (2, player2Name, player2Balance, !player1Turn);

            // Cache some informations for efficiency later
            if (player1Turn)
            {
                currentPlayerName = player1Name;
                otherPlayerName = player2Name;
            }
            else
            {
                currentPlayerName = player2Name;
                otherPlayerName = player1Name;
            }

            chatBoxLines.add (formatDialog (HOST_NAME, "It's now your turn " + currentPlayerName + "."));
            drawChatBox (chatBoxLines);
            PLAYER_TURN:
            while (loop < NUMBER_OF_PHRASES)
            {

                chatBoxLines.add (formatDialog (HOST_NAME, "Go spin the wheel!"));
                chatBoxLines.add (formatDialog (currentPlayerName, "Alright!"));
                drawChatBox (chatBoxLines);
                drawToInteractionArea ("The wheel is spinning.");

                // Rotate the wheel slightly backward to simulate a hand touching the wheel and rotating it
                for (int i = 0 ; i < 20 ; i++)
                {
                    angle += Math.PI / 180.0;
                    drawWheel (angle);
                    try
                    {
                        Thread.sleep (1);
                    }
                    catch (Exception e)
                    {
                    }
                }
                // Pause the wheel for a brief moment
                try
                {
                    Thread.sleep (100);
                }
                catch (Exception e)
                {
                }
                // Calculate the initial speed of the wheel
                spd = rng.nextDouble () * (WHEEL_INIT_VEL_UPPER_BOUND - WHEEL_INIT_VEL_LOWER_BOUND) + WHEEL_INIT_VEL_LOWER_BOUND;
                while (spd > 0)
                {
                    // Render the wheel
                    drawWheel (angle);
                    // Rotate the wheel
                    angle -= spd / 100.0;
                    // decrease the speed
                    spd += WHEEL_ACCEL / 100.0;
                    // Sleep for 1/100th of a second
                    try
                    {
                        Thread.sleep (10);
                    }
                    catch (Exception e)
                    {
                    }
                }

                // Determine which portion of the wheel has stopped at
                int letterValue = WHEEL_VALUES [(int) ((Math.PI / 15 - angle) % (Math.PI * 2) / (Math.PI * 2) * WHEEL_VALUES.length)];

                if (letterValue == LOSE_TURN)
                {
                    // The user loses their turn
                    chatBoxLines.add (formatDialog (HOST_NAME, "Sorry, " + currentPlayerName + ". Looks like you lose a turn this time."));
                    chatBoxLines.add (formatDialog (currentPlayerName, "Whoops!"));
                    drawChatBox (chatBoxLines);
                    break;
                }
                else if (letterValue == BANKRUPT)
                {
                    // Bankrupt the user
                    chatBoxLines.add (formatDialog (HOST_NAME, "Oh NO! " + currentPlayerName + ", you have just bankrupted."));
                    chatBoxLines.add (formatDialog (currentPlayerName, "NOOOOOOOOO!"));
                    drawChatBox (chatBoxLines);
                    if (player1Turn)
                    {
                        player1Balance = 0;
                        drawPlayerInfo (1, player1Name, player1Balance, true);
                    }
                    else
                    {
                        player2Balance = 0;
                        drawPlayerInfo (2, player2Name, player2Balance, true);
                    }
                    break;
                }
                else if (letterValue == COMMUNISM)
                {
                    // Redistribute the cash equally
                    chatBoxLines.add (formatDialog (HOST_NAME, "COMMUNISMM"));
                    chatBoxLines.add (formatDialog (HOST_NAME, "SPLIT THE CASH YOU GREEDY CAPITALISTS!"));
                    drawChatBox (chatBoxLines);
                    int equalBalance = (player1Balance + player2Balance) / 2;
                    player1Balance = equalBalance;
                    player2Balance = equalBalance;
                    drawPlayerInfo (1, player1Name, player1Balance, player1Turn);
                    drawPlayerInfo (2, player2Name, player2Balance, !player1Turn);
                    break;
                }

                chatBoxLines.add (formatDialog (HOST_NAME, "$" + letterValue));
                drawChatBox (chatBoxLines);

                // Ask the user for their action
                playerAction = acceptChoice ("What would you like to do?", playerTurnChoices);
                if (playerAction == 0)
                {

                    // Prompt the user to guess a letter
                    chatBoxLines.add (formatDialog (HOST_NAME, "Now guess a letter, " + currentPlayerName + "!"));
                    drawToInteractionArea ("Please enter the letter you think is in the " + phraseType + ".");
                    drawChatBox (chatBoxLines);

                    char guess;
                    while (true)
                    {
                        // Accept a letter guess from the user
                        guess = Character.toUpperCase (console.getChar ());

                        chatBoxLines.add (formatDialog (currentPlayerName, guess + "!"));
                        drawChatBox (chatBoxLines);

                        if (availableLetters.contains (new Character (guess)))
                        {
                            break;
                        }
                        else
                        {
                            // Check if the letter guessed is invalid or duplicate
                            if ('A' > guess || guess > 'Z')
                                chatBoxLines.add (formatDialog (HOST_NAME, "That is an invalid letter."));
                            else
                                chatBoxLines.add (formatDialog (HOST_NAME, "That has been guessed already bruhhh"));
                            chatBoxLines.add (formatDialog (HOST_NAME, "Since I'm a nice person, I'll let you try again."));
                            drawChatBox (chatBoxLines);
                        }
                    }
                    availableLetters.remove (new Character (guess));

                    // Determine the number of occurences of the letter that the user chose in the phrase
                    int occurences = 0;
                    for (int i = 0 ; i < currentlyGuessed.length ; ++i)
                    {
                        if (phrase.charAt (i) == guess)
                        {
                            ++occurences;
                            currentlyGuessed [i] = phrase.charAt (i);
                        }
                    }

                    String guessResult;
                    if (occurences == 0)
                        guessResult = "are no";
                    else if (occurences == 1)
                        guessResult = "is 1";
                    else
                        guessResult = "are " + occurences;
                    chatBoxLines.add (formatDialog (HOST_NAME, "There " + guessResult + " " + guess + "'s!"));
                    drawChatBox (chatBoxLines);

                    drawLetterGrid (
                            stringToCharacterGrid (
                                new String (currentlyGuessed),
                                LETTER_GRID_ROWS,
                                LETTER_GRID_COLS
                                ),
                            availableLetters
                            );

                    if (occurences == 0)
                        break;

                    // Determine the correct payout to the user
                    letterProfit = occurences * letterValue;
                    if (player1Turn)
                    {
                        player1Balance += letterProfit;
                        drawPlayerInfo (1, player1Name, player1Balance, true);
                    }
                    else
                    {
                        player2Balance += letterProfit;
                        drawPlayerInfo (2, player2Name, player2Balance, true);
                    }

                    // Check if all letters were guessed correctly
                    for (int i = 0 ; i < currentlyGuessed.length ; ++i)
                    {
                        if (currentlyGuessed [i] != phrase.charAt (i))
                        {
                            pauseProgram ();
                            continue PLAYER_TURN;
                        }
                    }

                    // Notify the user that all letters were guessed.
                    chatBoxLines.add (formatDialog (HOST_NAME, "We got all the letters. Congratulations!"));
                    drawChatBox (chatBoxLines);
                    newPhrase = true;
                    break;
                }
                else if (playerAction == 1)
                {
                    // Prompt the user to enter their guess
                    chatBoxLines.add (formatDialog (currentPlayerName, "I would like to solve the puzzle!"));
                    chatBoxLines.add (formatDialog (HOST_NAME, "Yes?"));
                    drawChatBox (chatBoxLines);
                    // Get the guess from the user
                    String guess = acceptString ("What is the answer to the puzzle? ", 36);

                    chatBoxLines.add (formatDialog (currentPlayerName, guess + "?"));
                    drawChatBox (chatBoxLines);

                    guess = guess.toUpperCase ();

                    // Shame the user for guessing a phrase that is incorrectly sized
                    if (guess.length () != currentlyGuessed.length)
                    {
                        chatBoxLines.add (formatDialog (HOST_NAME, "That's not even the same length as the phrase!"));
                        chatBoxLines.add (formatDialog (HOST_NAME, "What were you thinking?"));
                        chatBoxLines.add (formatDialog (currentPlayerName, "noooooooooo"));
                        drawChatBox (chatBoxLines);
                        break;
                    }
                    // Determine if the user got the known parts of the phrase wrong
                    boolean knownPartsMatch = true;
                    for (int i = 0 ; i < currentlyGuessed.length ; ++i)
                    {
                        if (currentlyGuessed [i] == '_')
                            continue;
                        else if (currentlyGuessed [i] == '\0')

                            {
                                if (guess.charAt (i) != ' ')
                                {
                                    knownPartsMatch = false;
                                    break;
                                }
                            }
                        else
                        {
                            if (currentlyGuessed [i] != guess.charAt (i))
                            {
                                knownPartsMatch = false;
                                break;
                            }
                        }
                    }
                    // Shame the user for gussing the knowns part of the phrase wrong
                    if (!knownPartsMatch)
                    {
                        chatBoxLines.add (formatDialog (HOST_NAME, "You got the KNOWN parts of the phrase wrong!"));
                        chatBoxLines.add (formatDialog (HOST_NAME, "What were you thinking?"));
                        chatBoxLines.add (formatDialog (currentPlayerName, "noooooooooo"));
                        drawChatBox (chatBoxLines);
                        break;
                    }

                    if (guess.equalsIgnoreCase (phrase))
                    {
                        // Notify the user that their guess was right
                        chatBoxLines.add (formatDialog (HOST_NAME, "That's right!"));
                        chatBoxLines.add (formatDialog (currentPlayerName, "Oh my gosh!"));
                        drawChatBox (chatBoxLines);
                        drawToInteractionArea ("CORRECT.");
                        newPhrase = true;

                        // Calculate the number of characters guessed from solving the puzzle
                        int numLettersMissing = 0;
                        for (int i = 0 ; i < currentlyGuessed.length ; ++i)
                        {
                            if (Character.isSpaceChar (phrase.charAt (i)))
                                currentlyGuessed [i] = '\0';
                            else
                            {
                                currentlyGuessed [i] = phrase.charAt (i);
                                numLettersMissing++;
                            }
                        }

                        // Award the prize for solving the puzzle to the solver
                        letterProfit = numLettersMissing * letterValue;
                        if (player1Turn)
                        {
                            player1Balance += letterProfit;
                            drawPlayerInfo (1, player1Name, player1Balance, true);
                        }
                        else
                        {
                            player2Balance += letterProfit;
                            drawPlayerInfo (2, player2Name, player2Balance, true);
                        }

                        drawLetterGrid (
                                stringToCharacterGrid (
                                    new String (currentlyGuessed),
                                    LETTER_GRID_ROWS,
                                    LETTER_GRID_COLS
                                    ),
                                availableLetters
                                );

                        break;
                    }
                    else
                    {
                        // Inform the user that their guess wasn't correct
                        chatBoxLines.add (formatDialog (HOST_NAME, "Sorry " + currentPlayerName + ", that's not it."));
                        chatBoxLines.add (formatDialog (currentPlayerName, "Oh..."));
                        drawChatBox (chatBoxLines);
                        drawToInteractionArea ("WRONG.");
                        break;
                    }
                }
                else
                {
                    return false;
                }
            }

            pauseProgram ();

            // The other player gets their turn
            player1Turn = !player1Turn;
        }

        // Determine the winner
        boolean player1Winner = true;
        boolean player2Winner = true;
        if (player1Balance > player2Balance)
            player2Winner = false;
        else if (player1Balance < player2Balance)
            player1Winner = false;

        // Announce the winner
        if (player1Winner && player2Winner)
        {
            // Tie
            chatBoxLines.add (formatDialog (HOST_NAME, "Oh my... We have a tie here!"));
            chatBoxLines.add (formatDialog (HOST_NAME, "Contestants, please show your good sportsmanship!"));
            chatBoxLines.add (formatDialog (player1Name, "It was a pleasure playing with you!"));
            chatBoxLines.add (formatDialog (player2Name, "We both did amazing!"));
            drawChatBox (chatBoxLines);
        }
        else if (player1Winner)
        {
            // Player 1 Won
            chatBoxLines.add (formatDialog (HOST_NAME, "Congratulations " + player1Name + ", you are the winner tonight!"));
            chatBoxLines.add (formatDialog (player1Name, "Yayyyy!"));
            chatBoxLines.add (formatDialog (player1Name, player2Name + ", you were an amazing player too!"));
            chatBoxLines.add (formatDialog (player2Name, "Noo, congraultulations, " + player1Name + "!"));
            chatBoxLines.add (formatDialog (player2Name, "You are the winner tonight!"));
            drawChatBox (chatBoxLines);
        }
        else
        {
            // Player 2 Won
            chatBoxLines.add (formatDialog (HOST_NAME, "Congratulations " + player2Name + ", you won tonight!"));
            chatBoxLines.add (formatDialog (player2Name, "Really?"));
            chatBoxLines.add (formatDialog (player1Name, "Congraultations, " + player2Name + ", you were amazing!"));
            chatBoxLines.add (formatDialog (player2Name, "Thanks! You were amazing too!"));
            drawChatBox (chatBoxLines);
        }
        // Display the results in the sidebar user list
        drawPlayerInfo (1, player1Name, player1Balance, false, player1Winner);
        drawPlayerInfo (2, player2Name, player2Balance, false, player2Winner);
        // Save the results in the leaderboard
        playerScores.add (new PlayerScore (player1Name, player1Balance));
        playerScores.add (new PlayerScore (player2Name, player2Balance));
        pauseProgram ();
        return false;
    }


    public void leaderboard ()
    {
        console.clear ();
        // Sort the players to display them in order
        Collections.sort (playerScores, Collections.reverseOrder ());
        console.println ("Leaderboard\n");
        PlayerScore playerScore;
        for (int i = 0 ; i < Math.min (playerScores.size (), 10) ; i++)
        {
            // Display each player and their score
            playerScore = (PlayerScore) (playerScores.get (i));
            console.println (playerScore.playerName + " - $" + playerScore.score);
        }
        console.println ("\nPlease press any key to continue.");
        console.getChar ();
    }


    public void instructions ()
    {
        // Display the instructions
        console.clear ();
        console.println ("Wheel of Fortune\n");
        console.println ("Welcome to Wheel of Fortune, America's Game!\n");
        console.println ("In this game, you will be presented with 10 puzzles that consist of a mystery phrase or a word that you must attempt to guess what it is.");
        console.println ("Initially, we only show you the structure of the puzzle.");
        console.println ("When it's your turn you must first spin a wheel to determine the payout for your guess.");
        console.println ("You may guess a letter, and we will reveal where the letter you selected are placed on the puzzle.");
        console.println ("If your letter of choice is not on the puzzle, the other player gets the turn.");
        console.println ("If there are letters on the puzzle, there is a cash payout as determined by our formula.");
        console.println ("You may choose to guess the puzzle if you know what it is.");
        console.println ("It is encouraged to guess the phrase immidiately when you know it.");
        console.println ("After 10 puzzles, players with the most cash will win the game!");
        console.println ("Good Luck!");
        console.println ("\nPlease press any key to continue.");
        console.getChar ();
    }


    public void goodbye ()
    {
        // Save the leaderboard data and display a goodbye message to the user
        writeScoresToFile ();
        console.clear ();
        console.println ("Thank you for spending some quality time with America's Game: Wheel of Fortune!\n");
        console.println (" - Game Programmers Paul Lee and Peter Ye.");
    }


    public static void main (String[] args)
    {
        WheelOfFortune game = new WheelOfFortune ();

        boolean exitGame = false;

        while (!exitGame)
        {
            // Call the mainMenu until it returns true, which signals that the user wants to exit.
            exitGame = game.mainMenu ();
        }

        game.goodbye ();
    }
}

class PlayerScore implements Comparable // class to store the score of a player and their name
{
    public final String playerName; // name of player
    public final int score; // player's score
    public PlayerScore (String pn, int sc)
    {
        score = sc;
        playerName = pn;
    }


    public int compareTo (Object o)  // compares two PlayerScores (used when sorting a list of PlayerScores)
    {
        PlayerScore other = (PlayerScore) o;
        if (score == other.score) // scores equal; compare names
        {
            return playerName.compareTo (other.playerName);
        }
        else // compare scores
        {
            return new Integer (score).compareTo (new Integer (other.score));
        }
    }
}

