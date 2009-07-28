package de.mesirii.mapper;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Mapper extends Applet {

  Mappanel mapper=null;
  Frame frame=null;

    public Mapper() {

    }

    public void start() {
	if (mapper!=null) mapper.start();
    }
    public void stop() {
	if (mapper!=null) mapper.stop();
    }
    public void destroy() {
	if (mapper!=null) mapper.destroy();
    }

  public static void main(String argv[]) throws Exception {
    String url=null, map=null, lang=null,port=null;
    try {
      if (argv.length==1) map=argv[0];
      else 
      if (argv.length==2) {map=argv[0]; port=argv[1];}
      else {
	  url=argv[0];
	  map=argv[1];
	  lang=argv[2];
	  port=argv[3];
      }
    } catch(Exception e) {
      System.out.println("Usage: java Mapper maps-url mapname language port");
      System.out.println("Usage: e.g. java Mapper file://localhost/home/mh14/tf/maps mg.mud.de de 2000");
      System.out.println("Usage: or   java Mapper file://localhost/home/mh14/tf/maps 3k.org en 2000");
    }
    Mappanel mapper=new Mappanel();
    Frame frame=new Frame();
    frame.setSize(500,400);
    frame.add("Center",mapper);
    frame.setTitle("DER MAPPER");
    frame.show();
    mapper.init(url,map,lang,port);
    MenuBar mb=mapper.createMenus();
    frame.setMenuBar(mb);
    frame.validate();
  }

  MenuBar mb;
  LabelMenu bm;
  Button showInWindow;

  public void init () {
    try {
      setLayout(new BorderLayout());
      String url=getParameter("path");
      String map=getParameter("map");
      String lang=getParameter("lang");
      String port=getParameter("port");
      System.out.println(url +'\n'+ map+" "+lang);
      mapper=new Mappanel(getCodeBase());
      add("Center",mapper);
      mapper.init(url,map,lang,port);

      mb=mapper.createMenus();
      bm=new LabelMenu();
      bm.grabMenu(mb);

      add("North",bm);

      System.out.println("isApplet!!!");
      showInWindow=new Button(mapper.I18N("Im Fenster anzeigen"));
      add("South",showInWindow);
  
      showInWindow.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  remove(mapper);
	  bm.setVisible(false);
	  if (frame==null) {
	      showInWindow.setVisible(false);
	      frame=new Frame();
	      frame.setSize(500,400);
	      remove(mapper);
	      frame.add("Center",mapper);
	      frame.setTitle("DER MAPPER");
	      frame.setMenuBar(mb);
	      frame.show();
	      frame.validate();
	      frame.addWindowListener(new WindowAdapter() {
		  public void windowClosing(WindowEvent we) {
		      frame.remove(mapper);
		      frame.hide();
		      bm.setVisible(true);
		      bm.grabMenu(mb);
		      add("Center",mapper);
		      showInWindow.setVisible(true);
		      validate();
		  }
	      });
	  } else {
	      remove(mapper);
	      frame.add("Center",mapper);
	      frame.show();
	  }
	  bm.fillMenu(mb);
	}
      });
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}


