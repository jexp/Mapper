// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Mappanel.java

package de.mesirii.mapper;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class MTextField extends TextField
    implements FocusListener
{

    public void focusGained(FocusEvent focusevent)
    {
        focused = (MTextField)focusevent.getSource();
    }

    public void focusLost(FocusEvent focusevent)
    {
    }

    public static MTextField queryFocus()
    {
        return focused;
    }

    public void replaceSelection(String s)
    {
        String s1 = getText();
        setText(s1.substring(0, getSelectionStart()) + s + s1.substring(getSelectionEnd()));
    }

    public MTextField()
    {
        addFocusListener(this);
    }

    static MTextField focused = null;

}
