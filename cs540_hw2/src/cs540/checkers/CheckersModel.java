package cs540.checkers;
import static cs540.checkers.CheckersConsts.*;

import java.io.*;

import java.awt.event.*;
import javax.swing.event.*;

/**
 * This class models a Checkers game. It maintains the state of the checkers 
 * game and provides methods for examining and modifying the state. It also 
 * allows interested objects to be notified of changes in state. 
 * <p>
 * Specifically, this class maintains the tuple <code>(state, bs, side)</code>, 
 * where <code>state</code> is a variable among <code>READY</code>, 
 * <code>WAITING</code>, <code>FINISHED</code>, and <code>INVALID</code>, 
 * <code>bs</code> is an array containing the locations of all pieces on the 
 * checkers board, and <code>side</code> is the side of the active player.
 * In addition, <code>CheckersModel</code> contains an instance of 
 * {@link GameClock GameClock}, which maintains how much time each side has 
 * remaining. 
 * <p>
 * The default value for <code>state</code> is <code>READY</code>. In this 
 * state, neither player may see the board or use CPU cycles.  This state is 
 * used when the game is paused, before the game has started, or in between 
 * moves.  The player to make the next move is the active player.  The game 
 * clock is paused in this state. 
 * <p>
 * In the <code>WAITING</code> state, the active player is thinking, and uses 
 * all available CPU cycles to calculate its next move. In this state, the 
 * game clock ticks against the active player.
 * <p>
 * The state is <code>FINISHED</code> when a player wins or when the players 
 * tie.  When a player wins, the variable <code>winner</code> is the side of 
 * the winning player. When the players tie, <code>winner</code> is 
 * <code>NEITHER</code>. The active player is undefined in this state. The 
 * game clock is paused in this state. 
 * <p>
 * The <code>INVALID</code> state is used when the game cannot proceed, without 
 * a winner and without a tie. This may occur when the program quits before
 * a game is finished, or when an user purposefully invalidates a game.
 * The game clock is paused in this state.
 * <p>
 * <code>CheckersModel</code> provides several methods to transition among
 * its states.  The most frequently invoked methods are the pair 
 * {@link #startWaiting startWaiting} and {@link #makeMove makeMove}, which 
 * together allow looping back and forth between <code>READY</code> and 
 * <code>WAITING</code>.  The methods {@link #forfeit forfeit} and 
 * {@link #crashGame crashGame} transition into end states <code>FINISHED</code> 
 * and <code>INVALID</code>, respectively. 
 * <p>
 * <code>makeMove</code> may transition directly to <code>FINISHED</code> if 
 * the active player's move results in a win or a tie. Similarly, the 
 * constructor may directly enter <code>FINISHED</code> if the active player
 * cannot make any moves at the outset. 
 * <p>
 * <code>CheckersModel</code> maintains references to the two 
 * {@link CheckersPlayer CheckersPlayer}s for export to other classes, but does 
 * not otherwise interact with the players.
 * <p>
 * The design of <code>CheckersModel</code> follows thread-safety guidelines. 
 * 
 * @see CheckersController CheckersController
 * @author David He
 */
public class CheckersModel 
{
    /** The integer representing the active side */
    protected int side;

    /** An <code>int[]</code> array representing the board state */
    protected int[] bs;

    /** The state variable of the checkers game */
    protected int state;

    /** The <code>GameClock</code> used for time accounting */
    protected GameClock clock;

    /** The winner of the game, if <code>state</code> is <code>FINISHED</code> */
    protected int winner;

    /** The value of the state variable when the game is <code>READY</code> */
    public static final int READY     = 0;
    /** The value of the state variable when the game is <code>WAITING</code> */
    public static final int WAITING   = 1;
    /** The value of the state variable when the game is <code>FINISHED</code> */
    public static final int FINISHED  = 2;
    /** The value of the state variable when the game is <code>INVALID</code> */
    public static final int INVALID   = 3;

    /** 
     * The two checkers players in the checkers game. This class maintains
     * references to the players for export to other classes, and does not
     * otherwise interact with the players.
     */
    protected CheckersPlayer[] cp;

    /**
     * The print stream to log to.
     * @see #setLog setLog
     */
    protected PrintStream log;

    /**
     * Constructs a <code>CheckersModel</code> with the specified checkers players, 
     * the initial checkers placement, and a standard game clock. Events are logged
     * to <code>stdout</code>.
     * @param cp        an array of the two checkers players
     */
    public CheckersModel(CheckersPlayer[] cp)
    {
        this(cp, Utils.INITIAL_BOARDSTATE, Utils.INITIAL_SIDE, new DefaultGameClock(), System.out);
    }

    /**
     * Constructs a <code>CheckersModel</code> with the specified checkers players,
     * a custom board state, a custom clock, and the specified log file.
     * <p>
     * The initial state of the CheckerModel is READY, unless the active player
     * cannot make any moves, in which case <code>state</code> is FINISHED.
     * <p>
     * The output log file is set to stdout.
     *
     * @param cp        an array of the two checkers players
     * @param bs        an array represeting the initial board state
     * @param side      the side to make the first move
     * @param clock     an game clock to provide time accounting
     * @param logOut    the output stream to log to
     */
    public CheckersModel(CheckersPlayer[] cp, int[] bs, int side, 
            GameClock clock, OutputStream logOut)
    {
        this.cp = cp.clone();
        this.bs = bs.clone();
        this.side = side;
        winner = NEITHER;
        this.clock = clock;

        setLog(logOut);

        log.printf("START %s %s\n", cp[RED].getName(), cp[BLK].getName());

        if (Utils.isLoser(bs, side))
            declareWinner(Utils.otherSide(side));
        else
            state = READY;
    }

    /**
     * Executes a move for the active player and swaps turns. If the active 
     * player wins or ties by making this move, this method sets the state to 
     * <code>FINISHED</code>, and updates the winner accordingly.  Otherwise, 
     * the method transitions to <code>READY</code>. In both cases, the method 
     * updates the board state, pauses the game clock, and sets the opponent 
     * as the active player. 
     * <p>
     * An <code>InvalidMoveException</code> is thrown if <code>move</code> is not legal.
     * <p>
     * This method may only called when <code>state</code> is 
     * <code>WAITING</code>. An <code>IllegalStateException</code> is thrown 
     * when called under any other state.
     *
     * @param move      the move to be made by the active player
     * @throws InvalidMoveException         if the move is not valid
     * @throws IllegalStateException        if the state is not <code>WAITING</code>
     */
    public synchronized void makeMove(Move move) throws InvalidMoveException
    {
        if (state != WAITING)
            throw new IllegalStateException();

        if (!Utils.isValidMove(bs, side, move))
            throw new InvalidMoveException();

        log.printf("MOVE %s %s (%d ms)\n", Utils.reprSide(side), move, clock.getTurnTime(side));

        /* Update state */
        Utils.execute(bs, move);
        side = Utils.otherSide(side);
        state = READY;

        /* Update clock */
        clock.press();
        clock.pause();

        if (Utils.isLoser(bs, side))
            declareWinner(Utils.otherSide(side));
        fireStateChanged();
    }

    /**
     * Sets the state to reflect waiting for a move selection from the active 
     * player. This method sets <code>state</code> to <code>WAITING</code>, 
     * and starts the game clock. The game clock will tick against the active 
     * player.
     * <p>
     * This method may only called when <code>state</code> is 
     * <code>READY</code>. An IllegalStateException is thrown when called 
     * under any other state.
     *
     * @throws IllegalStateException        if the state is not <code>READY</code>
     */
    public synchronized void startWaiting()
    {
        if (state != READY)
            throw new IllegalStateException();

        log.printf("WAIT %s\n", Utils.reprSide(side));

        state = WAITING;
        clock.resume();
        fireStateChanged();
    }

    /**
     * Forfeits the active player and ends the game.
     * This method sets <code>state</code> to <code>FINISHED</code> and 
     * declares the opponent of the active player as the winner. 
     * <p>
     * This method may only called when <code>state</code> is <code>WAITING</code>.
     * An <code>IllegalStateException</code> is thrown when called under any other state.
     *
     * @throws IllegalStateException        if the state is not <code>WAITING</code>
     */
    public synchronized void forfeit()
    {
        if (state != WAITING)
            throw new IllegalStateException();

        log.printf("FORFEIT %s\n", Utils.reprSide(side));

        clock.press();
        clock.pause();

        declareWinner(Utils.otherSide(side));
    }

    /**
     * Finishes the game with a win or a tie. 
     * This method sets <code>state</code> to <code>FINISHED</code> and sets 
     * <code>winner</code> to <code>side</code>.
     * @param side      the side to be declared the winner, or <code>NEITHER</code> for
     *                  a tie
     */
    protected synchronized void declareWinner(int side)
    {
        log.printf("WIN %s\n", Utils.reprSide(side));

        clock.pause();
        winner = side;
        state = FINISHED;
        fireStateChanged();
    }

    /**
     * Crashes the game and invalidate the results.
     * This method sets <code>state</code> to <code>INVALID</code> and 
     * <code>winner</code> to <code>NEITHER</code>.
     * <p>
     * This method may only called when <code>state</code> is <code>READY</code>
     * or <code>WAITING</code>. An <code>IllegalStateException</code> is thrown 
     * when called under any other state.
     *
     * @throws IllegalStateException        if the state is not <code>READY</code> 
     *                                      or <code>WAITING</code>
     */
    public synchronized void crashGame()
    {
        if (state != WAITING && state != READY)
            throw new IllegalStateException();

        log.printf("CRASH\n");

        clock.pause();
        winner = NEITHER;
        state = INVALID;
        fireStateChanged();
    }

    /**
     * Returns a string representation of the specified state. This method
     * returns <code>null</code> if <code>state</code> is not one of the four
     * defined states.
     * @param state         the state to represent as a string
     * @return              the string representation of <code>state</code>
     */
    public static String reprState(int state)
    {
        switch (state)
        {
            case READY:    return "READY";
            case WAITING:  return "WAITING";
            case FINISHED: return "FINISHED";
            case INVALID:  return "INVALID";
            default: return null;
        }
    }

    /**
     * Gets this model's board state.
     * @return          the int array representating this model's board state
     */
    public int[] getBoardState() { return (int[])bs.clone(); }

    /**
     * Gets this model's active side.
     * @return          this model's active side
     */
    public int getSide() { return side; }

    /**
     * Gets this model's state variable.
     * @return          this model's state variable
     */
    public int getState() { return state; }

    /**
     * Gets the winner of this game.
     * This method should only be called when <code>state</code> is FINISHED.
     * @return          the winner of this game
     */
    public int getWinner() { return winner; }

    /**
     * Gets the <code>CheckersPlayer</code> on the specified side.
     * @param side      the side of the requested player
     * @return          the <code>CheckersPlayer</code> on <code>side</code>
     */
    public CheckersPlayer getPlayer(int side) { return cp[side]; }

    /**
     * Gets the clock used by this model for time accounting.
     * @return          the game clock used for time accounting
     */
    public GameClock getClock() { return clock; }
    /**
     * Sets this model's clock for time accounting use.
     * @param clock     the game clock to be used for time accounting
     */
    public void setClock(GameClock clock) { this.clock = clock; }


    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Adds <code>listener</code> as a listener to changes in the model. 
     * @param listener  the ChangeListener to be added
     */
    public void addChangeListener(ChangeListener listener)
    { listenerList.add(ChangeListener.class, listener); }

    /**
     * Removes <code>listener</code> as a listener to changes in the model. 
     * @param listener  the ChangeListener to be removed 
     */
    public void removeChangeListener(ChangeListener listener)
    { listenerList.remove(ChangeListener.class, listener); }

    /**
     * Runs each ChangeListener's <code>stateChanged</code> method. 
     */
    protected void fireStateChanged()
    {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener listener : listenerList.getListeners(ChangeListener.class))
            listener.stateChanged(e);
    }

    /**
     * Sets the output stream to log to.
     * @param log           the output stream for logging
     */
    public void setLog(OutputStream log) { this.log = new PrintStream(log); }
}
