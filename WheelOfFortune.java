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

    // Class Constructor
    public WheelOfFortune() {
        console = new Console();
    }

    private void drawLetterGrid(int x,int y, int squareSize, char[][] grid) {
    }

    public static void main(String[] args) {
        WheelOfFortune game = new WheelOfFortune();
        try {
            while (true) {    
                game.newRound();
            }
        }
        catch(UserExitException e) { // UserExitException is thrown when user wants to exit
            game.goodbye();
        }
    }
}
