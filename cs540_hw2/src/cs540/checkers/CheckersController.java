package cs540.checkers;

import java.util.*;

/**
 * This controller progresses a checkers game by acting on an instance of 
 * {@link CheckersModel CheckerModel}. The crux of this controller's functionality 
 * resides in {@link #step step}, which traverses the state transition table of 
 * <code>CheckersModel</code> one step at a time. <code>step</code> is repeatedly
 * called within {@link #loop loop} until the game can not progress without 
 * external stimuli. Examples of external stimuli include a mouse click, 
 * a timer event, or a move selection. Certain types of external stimuli have 
 * predictable timing - <code>step</code> will indicate in its return code when 
 * these stimuli are expected to occur. <code>loop</code> will then use this 
 * return value to resume looping at the indicated time. 
 * <p>
 * We briefly describe the functionality of <code>step</code>, as well as how
 * this functionality depends on several configurable parameters.
 * <ul>
 * <li> By default, the transition from <code>READY</code> to 
 * <code>WAITING</code> occurs after a delay of <code>turnDelayTime</code> 
 * milliseconds. This <i>turn delay</i> serves to improve visual clarity for 
 * CheckersUI, since otherwise the transition occurs too rapidly to be noticed.
 * However, if the <code>moveOnClick</code> option is enabled for the active 
 * side, this transition will wait for a mouse click before proceeding. 
 * <li> By default, the transition between <code>WAITING</code> to 
 * <code>READY</code> occurs after the active player selects a move. However, 
 * if the <i>turn limit</i> option <code>turnLimit</code> is non-negative, the 
 * transition from WAITING to <code>READY</code> will occur after 
 * <code>turnLimit</code> milliseconds, regardless of whether the active player 
 * has finished selecting its move. 
 * <li> If the active player is an interactive player (as determined by 
 * {@link CheckersPlayer#isHuman CheckersPlayer.isHuman}), the transition from 
 * <code>WAITING</code> to <code>READY</code> will ignore the turn limit. <code>step</code> will never 
 * force a partially selected move from an interactive player.
 * </ul>
 * <p>
 * The design of <code>CheckersController</code> follows thread-safety 
 * guidelines. 
 * @see CheckersModel CheckersModel
 * @author David He
 */
public class CheckersController
{
    /** The <code>CheckersModel</code> controlled by this class. */
    protected CheckersModel model;

    /** 
     * The <code>TurnAgent</code> used for controlling a player. This is 
     * <code>null</code> except when <code>model.getState()</code> is WAITING.
     * A new <code>TurnAgent</code> is constructed for each turn.
     */
    protected TurnAgent turnAgent;

    /**
     * The timer used to wake up <code>loop</code> to process events in the future.
     */
    protected Timer timer;

    /**
     * The {@link CountdownClock CountdownClock} used to enforce turn limits. 
     * Each side has its own timer.
     */
    protected CountdownClock[] turnClock;

    /**
     * The {@link CountdownClock CountdownClock} that implements turnDelay, 
     * which inserts a small pause between the transition from <code>READY</code> 
     * to <code>WAITING</code>.
     */
    protected CountdownClock turnDelayClock;

    /**
     * How long, in milliseconds, to pause before the <code>READY</code> to 
     * <code>WAITING</code> transition. Set by 
     * {@link #calcTurnDelay calcTurnDelay}.
     */
    protected long turnDelayTime;

    /**
     * Whether turn delay is enabled.
     */
    protected boolean turnDelay;

    /**
     * Whether the <code>READY</code> to <code>WAITING</code> transition 
     * requires a mouse click.
     */
    protected boolean[] moveOnClick;

    protected static final int BREAK_LOOP = -1;
    protected static final int CONTINUE_LOOP = 0;
    /**
     * Creates a <code>CheckersController</code> for the given model with 
     * default settings. By default, turn limits are disabled, and 
     * <code>moveOnClick</code> is false for both players.
     * @param model         the model to control
     */
    public CheckersController(CheckersModel model)
    {
        this(model, new long[] {-1, -1}, new boolean[] {false, false});
    }

    /**
     * Creates a <code>CheckersController</code> for the given model with 
     * the specified turn time controls and <code>moveOnClick</code> settings.
     * @param model         the model to control
     * @param turnLimit     time in milliseconds the sides have to make a move
     * @param moveOnClick   whether to wait for an interactive click before each move
     */
    public CheckersController(CheckersModel model, long[] turnLimit, boolean[] moveOnClick)
    {
        this.model = model;
        this.moveOnClick = moveOnClick.clone();
        
        /* Create the turn clock, which enforces for how long each player can
         * think per turn */
        turnClock = new CountdownClock[2];
        for (int i = 0; i < 2; i++)
            turnClock[i] = new DefaultCountdownClock(turnLimit[i]);

        turnAgent = null;
        timer = new Timer(true);

        /* Create the clock to enforce delays in between turns, which helps
         * bring clarity to the UI */
        calcTurnDelay();
        turnDelayClock = new DefaultCountdownClock(turnDelayTime);
        turnDelay = true;
    }

    /**
     * Calculates this controller's turn delay based on the turn limit. 
     * Turn delay is a pause between each turn, which helps bring clarity
     * to the CheckersUI. Turn delay is activated even when --step is not 
     * specified. It can, however, be overridden by a manual click. 
     * Here, we calculate turnDelayTime. We scale it linearly with the 
     * allowed turn time for each player, but clamp to 250ms and 1000ms.
     */
    public void calcTurnDelay()
    {
        turnDelayTime = 0;
        for (int i = 0; i < 2; i++)
            turnDelayTime += turnClock[i].getDelay();
        turnDelayTime /= 10;
        turnDelayTime = Math.max(turnDelayTime,  250);
        turnDelayTime = Math.min(turnDelayTime, 1000);
    }

    /**
     * Starts a timer which invokes <code>loop(false)</code> at
     * <code>delayTime</code> milliseconds in the future.
     * @param delayTime    the number of milliseconds to wait before calling run.
     * @see #loop loop
     */
    public void loopLater(long delayTime)
    {
        TimerTask task = new TimerTask()
        {
            public void run() { loop(false); }
        };
        timer.schedule(task, delayTime);
    }

    /**
     * Advances the checkers game by repeatedly calling <code>step</code> 
     * until the game cannot be progressed further from its present state. 
     * If the return value of <code>step</code> indicates that the game may
     * progress at a specific time in the future, this method schedules a 
     * timer which calls <code>loop(false)</code> at that time.
     * <p>
     * This method is idempotent.
     * @param isClick       whether this is called from an user click
     * @see #step step
     */
    public synchronized void loop(boolean isClick)
    {
        long sleepTime = 0;
        while (sleepTime == CONTINUE_LOOP)
        {
            try {
                sleepTime = step(isClick);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            isClick = false;
        }

        if (sleepTime > 0)
            loopLater(sleepTime);
    }

    /**
     * Attempts to progress the checkers game one step at a time. If some
     * progress was made, such as a state transition in the 
     * <code>CheckersModel</code> this controller acts on, this method returns
     * <code>CONTINUE_LOOP == 0</code>. Otherwise, if the game cannot progress 
     * from its current state without some external stimulus (such as a mouse 
     * click, move selection, or countdown timer completion), this method 
     * returns an integer specifying how many milliseconds in the future the 
     * caller can expect one of these stimuli to occur; the caller should 
     * re-invoke this method at the specified time in the future. However, if 
     * game progression depends on an external stimulus that occurs 
     * indefinitely in the future (such as acquiring a move from 
     * <code>ui.HumanPlayer</code>), this method returns 
     * <code>BREAK_LOOP == -1</code>.
     * <p>
     * This method is nilpotent, and is intended to be called from within a loop.
     * <p>
     * Please note that a return value of <code>0</code> does not necessarily 
     * imply that a state transition in <code>CheckerModel</code> was induced. 
     * @param isClick       whether this is called from an user click
     * @return              an integer specifying how many milliseconds in the
     *                      future the caller should re-invoke this method;
     *                      <code>CONTINUE_LOOP</code> if this method should be 
     *                      re-invoked right away; and <code>BREAK_LOOP</code> 
     *                      if unknown or indeterminate
     */
    protected long step(boolean isClick)
    {
        switch (model.getState())
        {
            case CheckersModel.READY:
                return stepReady(isClick);

            case CheckersModel.WAITING:
                return stepWaiting(isClick);

            case CheckersModel.FINISHED:
            case CheckersModel.INVALID:
            default:
                return BREAK_LOOP;
        }
    }

    /**
     * Helper method for {@link #step step}.
     * @param isClick       whether this is called from an user click
     */
    protected long stepWaiting(boolean isClick)
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        Move move;

        /* Ask the player nicely for a chosen move */
        move = turnAgent.getMove();
        if (move != null)
        {
            /* Execute the move, and continue loop */
            try {
                model.makeMove(move);
            } catch (InvalidMoveException e) {
                model.forfeit();
            }
            return CONTINUE_LOOP;
        }

        /* 
         * If the player has used up the allocalated per-turn time,
         * forcefully obtain a move from the player and execute it. 
         * Otherwise sleep for timeRemain milliseconds and and check 
         * this condition again after waking up. 
         */
        switch (turnClock[side].getState())
        {
            case CountdownClock.FINISHED:
                /* Don't force moves from interactive players. */
                if (player.isHuman())
                    return BREAK_LOOP;

                /* Don't force a move if no turnLimit */
                if (turnClock[side].getDelay() == -1)
                    return BREAK_LOOP;

                /* Stop calculation and forcefully obtain a move */
                turnAgent.stopCalculate();
                move = turnAgent.getForcedMove();
                turnAgent = null;

                /* Execute the move, and continue loop */
                try { 
                    model.makeMove(move);
                } catch (InvalidMoveException e) {
                    model.forfeit();
                }

                return CONTINUE_LOOP;
            case CountdownClock.RUNNING:
                return turnClock[side].getTimeRemain();
        }
        return BREAK_LOOP;
    }

    /**
     * Helper method for {@link #step step}.
     * @param isClick       whether this is called from an user click
     */
    protected long stepReady(boolean isClick)
    {
        int side = model.getSide();
        CheckersPlayer player = model.getPlayer(side);

        /* Skip this entire next section if isClick == true */
        if (!isClick)
        {
            /* Do not continue if moveOnClick is on */
            if (getMoveOnClick(side))
                return BREAK_LOOP;

            /* Otherwise, continue after a short delay (computed previously) */
            switch (turnDelayClock.getState())
            {
                /* Turn delay not started: start it now (if enabled) */
                case CountdownClock.PAUSED:
                    if (!turnDelay)
                        break;
                    turnDelayClock.resume();
                    /* Fall through */

                    /* Turn delay not complete; return remaining delay time */
                case CountdownClock.RUNNING:
                    return turnDelayClock.getTimeRemain();

                    /* Turn delay finished; continue processing */
                case CountdownClock.FINISHED:
                    break;
            }
        }

        /* Clear turn delay clock */
        turnDelayClock.reset();

        /* Set model state to WAITING */
        model.startWaiting();

        /* Start the turn clock that enforces term limits */
        turnClock[side].reset();
        turnClock[side].resume();

        /* Begin calculations with a TurnAgent and register callback */
        turnAgent = new TurnAgent(player, model.getBoardState());
        turnAgent.setCallbackController(this);
        turnAgent.startCalculate();

        return CONTINUE_LOOP;
    }

    /**
     * Terminates the checkers game. This method crashes the game if it is ongoing. 
     * Otherwise, this method does nothing. This is called when the UI exits, 
     * among other situations.
     */
    public synchronized void terminateGame()
    {
        if (turnAgent != null) {
            turnAgent.stopCalculate();
            turnAgent = null;
        }

        if (model.getState() == CheckersModel.READY ||
            model.getState() == CheckersModel.WAITING )
            model.crashGame();
    }

    /**
     * Sets whether a click is required before each move by the specified side.
     * @param side          an integer representing the side
     * @param b             whether a move is required
     */
    public void setMoveOnClick(int side, boolean b) { moveOnClick[side] = b; }

    /**
     * Gets whether a click is required before each move by the specified side.
     * @param side          an integer representing the side
     * @return              whether a move is required
     */
    public boolean getMoveOnClick(int side) { return moveOnClick[side]; }

    /**
     * Sets how long the specified side has to select a move each turn, or 
     * <code>-1</code> for no limit. 
     * @param side          an integer representing the side
     * @param limit         if non-negative, time, in milliseconds 
     *                      <code>side</code> may use in selecting a move each 
     *                      turn; if <code>-1</code>, no limit
     */
    public void setTurnLimit(int side, long limit)
    { 
        turnClock[side].setDelay(limit);
        calcTurnDelay();
        turnDelayClock.setDelay(turnDelayTime);
    }

    /**
     * Sets whether turn delay is enabled.
     * @param turnDelay     whether turn delay is enabled
     * @see #calcTurnDelay calcTurnDelay
     */
    public void setTurnDelay(boolean turnDelay) { this.turnDelay = turnDelay; }

    /**
     * Gets how long the specified side has to select a move each turn.
     * @param side          an integer representing the side
     * @return              time, in milliseconds, <code>side</code> may use 
     *                      in selecting a move each turn; or <code>-1</code>
     *                      if no limit
     */
    public long getTurnLimit(int side) { return turnClock[side].getDelay(); }
}
