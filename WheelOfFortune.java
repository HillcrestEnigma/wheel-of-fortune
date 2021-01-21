/*
 * Name: Paul Lee and Peter Ye
 * Teacher: Ms Krasteva
 * Date: 2021 Jan 19 14:17
 * Assignment: Wheel of Fortune ISP
 */

/*
 * Program Purpose: Recreate the Wheel of Fortune game in Ready to Program
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
        for (int i=0; i<grid[0].length; i++) {
            for (int j=0; j<grid.length; j++) {
                console.drawRect(x + squareSize*i, y + squareSize*j, squareSize, squareSize);
                console.drawString("" + grid[j][i], (int)(x+squareSize*(i+0.45)), (int)(y + squareSize*(j+0.55)));
            }
        }
    }

    public static void main(String[] args) {
        WheelOfFortune game = new WheelOfFortune();
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
