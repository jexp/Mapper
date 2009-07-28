package de.mesirii.mapper;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class LabelMenu extends Panel {
    class MenuEntry extends MouseAdapter {
	Label menuLabel;
	PopupMenu menu;
	public Label addMenu(Menu m) {
	    if (menuLabel!=null) menuLabel.setText(m.getLabel());
	    else {
		menuLabel=new Label(m.getLabel());
		menuLabel.addMouseListener(this);
	    }
	    menu=makePopUp(m);
	    menuLabel.add(menu);
	    return menuLabel;
	}
	public void mouseReleased(MouseEvent e) {
	    this.show();
	}
	public void show() {
	    if (menu!=null && menuLabel!=null) 
		menu.show(menuLabel,0,menuLabel.getSize().height);
	}
	public PopupMenu makePopUp(Menu m) {
	    PopupMenu pm=new PopupMenu();
	    int count=m.countItems();
	    MenuItem mi=null;
	    for (int i=0;i<count;i++) {
		mi=m.getItem(0);
		pm.add(mi);
	    }
	    //	    pm.addNotify();
	    return pm;
	}
    }
    public void grabMenu(MenuBar mb) {
	menus=new Vector();
	menuNames=new Vector();
	int count=mb.getMenuCount();
	menuPanel.removeAll();
	Menu menu=null;
	Menu help=mb.getHelpMenu();
	for (int i=0;i<count;i++) {
	    menu=mb.getMenu(i);
	    if (menu!=help)
		addMenu(menu);
	    else
		setHelpMenu(menu);
	}
    }

    public void fillMenu(MenuBar mb) {
	int count=menus.size();
	Menu myMenu=null;
	Menu mbMenu=null;
	String menuName=null;
	Menu help=mb.getHelpMenu();
	for (int i=0;i<count;i++) {
	    mbMenu=mb.getMenu(i);
	    if (!((String)menuNames.get(i)).equals(mbMenu.getLabel())) {
		mbMenu=new Menu((String)menuNames.get(i));
		mb.add(mbMenu);
	    }
	    myMenu=((MenuEntry)menus.get(i)).menu;
	    fillMenuItems(myMenu,mbMenu);
	}
	myMenu=getHelpMenu();
	mbMenu=mb.getHelpMenu();
	if (mbMenu==null) {
	    mbMenu=new Menu(helpMenu.menuLabel.getText());
	    mb.setHelpMenu(mbMenu);
	}
	fillMenuItems(myMenu,mbMenu);
    }
    protected void fillMenuItems(Menu myMenu,Menu mbMenu) {
	    int count2=myMenu.getItemCount();
	    for (int j=0; j<count2;j++)
		mbMenu.add(myMenu.getItem(0));
    }

    Panel menuPanel;
    Vector menus=new Vector();
    Vector menuNames=new Vector();
    public LabelMenu() {
	setLayout(new BorderLayout());
	menuPanel=new Panel();
	menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT,1,0));
	add("West",menuPanel);
    }
    public int getMenuCount() {
	return menuNames.size();
    }
    public Menu addMenu(Menu m) {
	int off=menuNames.indexOf(m.getLabel());
	if (off==-1) {
	    menuNames.addElement(m.getLabel());
	    MenuEntry me=new MenuEntry();
	    menuPanel.add(me.addMenu(m));
	    menus.addElement(me);
	} else {
	    menuNames.setElementAt(m.getLabel(),off);
	    MenuEntry me=(MenuEntry)menus.get(off);
	    me.addMenu(m);
	    //	    menuPanel.add(m,off);
	}
	return m;
    }

    MenuEntry helpMenu=null;
    public void setHelpMenu(Menu m) {
	if (m==null) {
	    if (helpMenu!=null) 
		remove(helpMenu.menuLabel);
	    helpMenu=null;
	    return;
	}
	if (helpMenu==null) {
	    helpMenu=new MenuEntry();
	    helpMenu.addMenu(m);
	    add("East",helpMenu.menuLabel);
	} else helpMenu.addMenu(m);
    }
    public Menu getHelpMenu() {
	return helpMenu.menu;
    }
}
