package cs540.checkers;
import static cs540.checkers.CheckersConsts.*;

import cs540.checkers.ui.*;
import cs540.checkers.cli.*;

import java.io.*;
import java.lang.reflect.*;

/**
 * This class performs tasks necessary for starting the Checkers program, 
 * including parsing command line arguments, initializing a checkers game, and 
 * calling GUI creation routines. 
 * The start of execution, <code>main</code>, resides in this class.
 * @author Justin Tritz
 * @author David He
 */
public class Checkers 
{
    public CheckersPlayer[] cp;
    public boolean[] moveOnClick;
    public long[] turnLimit;
    public boolean verbose;
    public boolean nogui;
    public boolean turnDelay;
    public OutputStream logFile;
    public int[] depthLimit;

    public int[] bs;
    public int side;

    /**
     * Constructs a new Checkers with default values for all options.
     */
    public Checkers()
    {
        cp = new CheckersPlayer[] { null, null };
        moveOnClick = new boolean[] { false, false };
        verbose = true;
        turnLimit = new long[] {3000, 3000};
        nogui = false;
        turnDelay = true;
        logFile = System.out;
        depthLimit = new int[] {-1, -1};

        bs = Utils.INITIAL_BOARDSTATE;
        side = Utils.INITIAL_SIDE;
    }

    public static CheckersPlayer createCheckersPlayer(String fqClassName, String playerName, int side)
    {
        CheckersPlayer player;
        Class<?> cpClass;
        Constructor<?> cpConst;

        try {
            cpClass = Class.forName(fqClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

        try {
            cpConst = cpClass.getConstructor(String.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

        try {
            player = (CheckersPlayer)cpConst.newInstance(playerName, side);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

        return player;
    }

    /**
     * Sets the option values of this class by parsing command line arguments.
     * @param args     the list of command line arguments
     * @throws IllegalArgumentExpection if <code>args</code> includes unknown options
     */
    public void parseOptions(String[] args) throws IllegalArgumentException
    {
        if (args.length < 2)
            throw new IllegalArgumentException();

        for (int i = 0; i < 2; i++)
        {
            String fqClassName, playerName;

            /* Use fqcn if prepend '.' */
            if (args[i].charAt(0) == '.')
            {
                fqClassName = args[i].substring(1);
                playerName = fqClassName
                    .replace("cs540\\.checkers\\.", "")
                    .replace("Player\\z", "");
            }
            else
            {
                fqClassName = "cs540.checkers." + args[i] + "Player";
                playerName = args[i];
            }

            cp[i] = createCheckersPlayer(fqClassName, playerName, i);
        }

        for (int i = 2; i < args.length; i++)
        {
            if (args[i].equals("--turntime"))
            {
                long _turnLimit = Integer.parseInt(args[i + 1]);
                turnLimit[RED] = _turnLimit;
                turnLimit[BLK] = _turnLimit;
                i += 1;
            }
            else if (args[i].equals("--step"))
            {
                moveOnClick[RED] = true;
                moveOnClick[BLK] = true;
            }
            else if (args[i].equals("--verbose"))
                verbose = true;
            else if (args[i].equals("--nogui"))
                nogui = true;
            else if (args[i].equals("--disable-turn-delay"))
                turnDelay = false;
            else if (args[i].equals("--initbs"))
            {
                try {
                    bs = Utils.loadBoardState(args[i+1]);
                } catch (IOException e) {
                    System.out.println(e);
                    throw new IllegalArgumentException();
                } catch (FormatException e) {
                    System.out.println(e);
                    throw new IllegalArgumentException();
                }

                i += 1;
            }
            else if (args[i].equals("--initside"))
            {
                side = Integer.parseInt(args[i + 1]);
                i += 1;
            }
            else if (args[i].equals("--depthlimit"))
            {
                depthLimit[RED] = Integer.parseInt(args[i + 1]);
                depthLimit[BLK] = Integer.parseInt(args[i + 2]);
                i += 2;
            }
            else if (args[i].equals("--logfile"))
            {
                try{
                    logFile = new FileOutputStream(args[i + 1], true); //append
                } catch (FileNotFoundException e) {
                    System.out.println(e);
                    throw new IllegalArgumentException();
                }

                i += 1;
            }
            else
                throw new IllegalArgumentException();
        }
    }

    private static String help_str = 
            "Usage: java -jar Checkers.jar <red_player> <blk_player> [OPTION]...\n" +
            "Starts a checker game with <red_player> and <blk_player> specifying the\n" +
            "abbreviated class names of the red and black checkers players, respectively.\n"+

            "Optional parameters include:\n" +
            "--turntime <turnLimit>      Sets how long computer players are allowed to think. (milliseconds)\n" + 
            "--step                      Require a mouse click before the start of each turn.\n" +
            "--verbose                   Print output loquaciously.\n" +
            "--disable-turn-delay        Disable the READY -> WAITING turn delay.\n" +
            "--initbs <filename>         Read the initial board state from <filename>.\n" +
            "--initside <side>           Sets side to be the first to move.\n" +
            "--depthlimit <redDepthLimit> <blkDephLimit>\n" +
            "                            Sets the maximum iterative depth of iterative deepening for each player\n" +
            "--nogui                     Do not launch a GUI\n" +
            "--logfile                   Append to the specified log file\n" +
            //"--help: Show this message.\n" +
            "";

    /**
     * This is the main method where the program will begin execution.
     */
    public static void main(String[] args)
    {
        /* Get default options */
        Checkers checkers = new Checkers();

        /* Override default options by parsing command line arguments */
        try {
            checkers.parseOptions(args);
        } catch (IllegalArgumentException e) {
            System.out.println(help_str);
            System.exit(1);
        }

        /* Go! */
        checkers.init();
    }

    /**
     * Constructs objects necessary for a checkers game to function, and
     * launches a GUI to interact with the game.
     */
    public void init()
    {
        /* Set global verbosity */
        Utils.verbose = verbose;

        /* Set depthLimit for players */
        for (int i : new int[] {RED, BLK} )
            if (depthLimit[i] != -1)
                cp[i].setDepthLimit(depthLimit[i]);

        /* Create clock object */
        GameClock clock = new DefaultGameClock(
                new long[] {1800 * 1000, 1800 * 1000}, side);

        /* Create game model */
        CheckersModel cm = new CheckersModel(cp, bs, side, clock, logFile);

        CheckersController ctl;

        if (nogui)
        {
            /* Launch CLI */
            ctl = new CheckersController(cm);
            CheckersCLI ui = CheckersCLI.launch(cm, ctl);
        }
        else
        {
            /* Create the Swing GUI */
            ctl = new CheckersUIController(cm);
            CheckersUI ui = CheckersUI.launch(cm, ctl);

            /* If any players are HumanPlayer, pass a reference to gui's
             * CheckerBoard widget. This is necessary for HumanPlayer's 
             * calculateMove().                                            */
            for (int i : new int[] {RED, BLK})
                if (cp[i] instanceof HumanPlayer)
                    ((HumanPlayer)cp[i]).setCheckersBoardWidget(ui.getCheckersBoardWidget());
        }

        /* Pass moveOnClick to the controller */
        for (int i : new int[] {RED, BLK} )
            ctl.setMoveOnClick(i, moveOnClick[i]);

        /* Pass turnLimit to the controller */
        for (int i : new int[] {RED, BLK} )
            ctl.setTurnLimit(i, turnLimit[i]);

        /* Pass turnDelay to the controller */
        ctl.setTurnDelay(turnDelay);

        /* Automatically start controller loop (after short delay) */
        ctl.loopLater(500);
    }
}
