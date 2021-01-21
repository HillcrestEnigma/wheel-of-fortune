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
import hsa.Console;
import java.io.*;

public class WheelOfFortune {
    Console console;
    final int CONSOLE_WIDTH=1024;
    final int CONSOLE_HEIGHT=768;

    // Class Constructor
    public WheelOfFortune() {
        // create a test console to calculate number of pixels per row and column
        int pxPerRow,pxPerCol;
        Console tmpConsole = new Console(1,1);
        pxPerRow=tmpConsole.getHeight();
        pxPerCol=tmpConsole.getWidth();
        tmpConsole.close();

        int consoleRows=CONSOLE_HEIGHT/pxPerRow;
        int consoleCols=CONSOLE_WIDTH/pxPerCol;

        console=new Console(consoleRows,consoleCols);
        /*
        console.println(console.getWidth());
        console.println(console.getHeight());
        */
    }

    private void drawLetterGrid(int x, int y, int squareSize, char[][] grid) {
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

    private void drawHost(int x, int y) {
        console.setColor(Color.black);
        console.fillOval(x, y, 50, 50);
        console.fillRect(x+5, y+45, 40, 100);
    }

    public static void main(String[] args) {
        WheelOfFortune game = new WheelOfFortune();
        char[][] chardata = new char[6][24];
        chardata[2][2] = 'A';
        chardata[2][3] = 'B';
        chardata[2][4] = 'C';
        game.drawLetterGrid(10, 10, 30, chardata);
        game.drawButton(100, 500, "Button", 'X', false);
        game.drawButton(400, 500, "Action", 'Y', true);
        game.drawHost(600, 600);
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
