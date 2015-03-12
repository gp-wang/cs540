package cs540.checkers.ui;
import cs540.checkers.*;

import java.awt.event.*;
import javax.swing.event.*;

/**
 * This controller extends {@link CheckersController CheckersController} by 
 * allowing a mouse click to override stalls in {@link #loop loop}.
 * @author David He
 */
public class CheckersUIController extends CheckersController implements ActionListener
{
    public CheckersUIController(CheckersModel model)
    {
        super(model);
    }

    public CheckersUIController(CheckersModel model, long[] turnLimit, 
            boolean[] moveOnClick)
    {
        super(model, turnLimit, moveOnClick);
    }

    public void actionPerformed(ActionEvent e)
    {
        /* Mouse clocked */
        if (e.getSource() instanceof CheckersBoardModel)
            loop(true);
    }
}
