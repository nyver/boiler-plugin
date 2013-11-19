package com.nyver.idea.plugin.boiler;

/**
 * Boiler exception class
 *
 * @author Yuri Novitsky
 */
public class BoilerException extends Exception
{
    public BoilerException(String message)
    {
        super(message);
    }

    public BoilerException(Throwable cause)
    {
        super(cause);
    }
}
