package de.mesirii.mapper;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.applet.*;
import java.io.*;
import java.net.*;
import java.util.*;

class Util {
    static Hashtable fontCache=new HashMap();
    public static Font getCachedFont(int size) {
	Font res=(Font)fontCache.get(new Integer(size));
	if (res==null) {
	    res=new Font("SansSerif",Font.PLAIN,size);
	    fontCache.put(new Integer(size),res);
	}
	return res;
    }
    public static String allTokens(StringTokenizer st) {
	return st.nextToken("").trim();
    }
}

public class Mappanel extends Panel implements Runnable {
    
  class NodeWorker {
    public static final int LEVEL=1, MAP=2, ALL=3, REGION=4, AKT=5;
    boolean modifyNode(int node, int map, int krit) {return false;};
    boolean modifyNode(int node, int krit) {return false;};
  }

    class MapButton extends IconButton {

	public Vector findListeners(MouseEvent e) {
	    Vector res=new Vector();
	    Dimension d=getSize();
	    int x=e.getX(),y=e.getY();
	    Region r;
	    for (Enumeration en=areas.keys();en.hasMoreElements();) {
		r=(Region)en.nextElement();
		if (r.inside(x,y,d.width,d.height))
		    res.addElement(areas.get(r));
	    }
	    return res;
	}
	public void mouseEntered(MouseEvent e) {
	    Vector list=findListeners(e);
	    CommandListener cl;
	    if (list.size()>0) {
		cl=(CommandListener)list.firstElement();
		statusHelp(cl.command);
	    }
	}

	public void mouseClicked(MouseEvent e) { 
	    Vector list=findListeners(e);
	    for (int i=0;i<list.size();i++) 
		((CommandListener)list.get(i)).performAction(MapButton.this);
	}

	public MapButton(String file) {
	    super(file);
	}
	Hashtable areas=new HashMap();
	class CommandListener {
	    public ActionListener al;
	    public String command;
	    public CommandListener(ActionListener al, String cmd) {
	        this.al=al;
		command=cmd;
	    }
	    public void performAction(Component source) {
		ActionEvent ae=new ActionEvent(source,ActionEvent.ACTION_PERFORMED,command);
		al.actionPerformed(ae);
	    }
	}
	public void setActionListener(Region r,ActionListener al, String command) {
	    //	    System.out.println(r+" "+command);
	    areas.put(r,new CommandListener(al,command));
	}

    }

class IconButton extends Canvas implements ImageObserver, MouseListener {
    Image i=null;
    boolean pressed=false;

    public void mouseClicked(MouseEvent evt) { 
	String acmd="";
	if ((evt.getModifiers() & evt.BUTTON1_MASK) != 0) acmd=cmd;
	if ((evt.getModifiers() & evt.BUTTON3_MASK) != 0) acmd=cmd2;
	ActionEvent ae=new ActionEvent(this,ActionEvent.ACTION_PERFORMED,acmd);
	for (int i=0;i<listeners.size();i++) {
	    ((ActionListener)listeners.get(i)).actionPerformed(ae);
	}
	repaint();
    }
    String tooltip;
    public void setTooltip(String text) {
	tooltip=text;
    }
    public void mouseEntered(MouseEvent e) {
	if (tooltip!=null) statusHelp(tooltip);
	else
	if (cmd2==null)
	    statusHelp(I18N("links")+" "+cmd);
	else
	    statusHelp(I18N("links")+" "+cmd+", "+I18N("rechts")+" "+cmd2);
    }
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}

    public void setImage(String file) {
	w=-1;h=-1;
	try {
	    URL u;
	    try {
		u=new URL(file);
	    } catch(Exception e) {
		u=getResourceURL(null,file);
	    }
	    i=Toolkit.getDefaultToolkit().getImage(u);
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }
    public IconButton(String file) {
	setImage(file);
	addMouseListener(this);
    }

    public IconButton(String file, String cmd) {
	setImage(file);
	setActionCommand(cmd);
	addMouseListener(this);
    }
    public IconButton(String file, String cmd,String cmd2) {
	setImage(file);
	setActionCommand(cmd);
	setActionCommand2(cmd2);
	addMouseListener(this);
    }

    String cmd,cmd2;
    public void setActionCommand(String cmd) {
	this.cmd=cmd;
    }
    public void setActionCommand2(String cmd) {
	this.cmd2=cmd;
    }
    Vector listeners=new Vector();
    public void addActionListener(ActionListener al) {
	if (!listeners.contains(al))
	    listeners.addElement(al);
    }

    public void removeActionListener(ActionListener al) {
	if (listeners.contains(al))
	    listeners.remove(al);
    }
    int w=-1,h=-1;
    public void paint(Graphics g) {
	super.paint(g);
	paintImage(g);
    }
    public void update(Graphics g) {
	super.update(g);
	paintImage(g);
    }
    protected void paintImage(Graphics g) {
	if (i==null) return;
	Dimension d=getSize();
	if (w==-1 || h==-1) {
	    w=i.getWidth(this);
	    h=i.getHeight(this);
	}
	if (w>-1 && h>-1) {
	    //	    System.out.print(".");
	    if (d.height > w && d.width > h)
		g.drawImage(i,(d.width-w)/2,(d.height-h)/2,this);
	    else {
		int sx,sy;
	        if ((float)(d.width/w) < (float)(d.height/h)) {
		    sx=d.width;
		    sy=h*d.width/w;
		} else {
		    sx=w*d.height/h;
		    sy=d.height;
		}
		g.drawImage(i,(d.width-sx)/2,(d.height-sy)/2,sx,sy,this);
	    }
	}
    }

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	    if ((infoflags & ( ALLBITS | FRAMEBITS )) > 0) {
		repaint();
		return ((infoflags & FRAMEBITS ) > 0);
	    }
	    return true;
	}
    
    public Dimension getPreferredSize() {
	    return new Dimension(20,20);
    }
}

    URL codebase=null;
    boolean isApplet=false;
    Properties props=new Properties();
  public static Hashtable colors=null;
  final Object dummy=new Object();

  final static int MARK_MASK=1, PRE_MARK_MASK=2, UNUSED=4, OVERLAP_MASK=8, SHOW_ATT_MASK=16, VISITED_MASK=32, HOUSE_MASK=64, HOUSE2_MASK=128, PARA_MASK=256, NOPARA_MASK=512;

  final static int PARA=1, NOPARA=0;
  Hashtable flags=null;
  Hashtable name_dir_lookup;
  int[] mask={0,1,2,4,8,0,16,32,64,128,256,512};
  boolean oneway=false, forcenode=false;
  float[] ipoint=new float[7];

  /*
    0     1     2     3     4     5     6     7     8     9   
    sp    sw    ssw   s     sso   so    oso   o     ono   no  
    10    11    12    13    14    15    16    17    18    19 
    nno   n     nnw   nw    wnw   w     wsw   ob    u     swu
    20    21    22    23    24    25    26    27    28    29
    sswu  su    ssou  sou   osou  ou    onou  nou   nnou  nu
    30    31    32    33    34    35    36    37    38    39
    nnwu  nwu   wnwu  wu    wswu  swob  sswob sob   ssoob soob
    40    41    42    43    44    45    46    47    48    49
    osoob oob   onoob noob  nnoob nob   nnwob nwob  wnwob wob
    50
    wswob
  */

  int[] dir_lookup={0,1,3,5,15,0,7,13,11,9,17,18};

  static String[] dir_name_lookup;

  int[][] directions =
  {{0,0,0}, // special 
   {-2,-2,0},{-1,-2,0}, // sw, ssw
   {0,-2,0},{1,-2,0},{2,-2,0},{2,-1,0}, // s..oso
   {2,0,0},{2,1,0},{2,2,0},{1,2,0}, // o..nno
   {0,2,0},{-1,2,0},{-2,2,0},{-2,1,0}, // n..wnw
   {-2,0,0},{-2,-1,0},  // w wsw
   {0,0,1},{0,0,-1}, // ob u
   {-2,-2,-1},{-1,-2,-1}, // alles mit unten
   {0,-2,-1},{1,-2,-1},{2,-2,-1},{2,-1,-1},
   {2,0,-1},{2,1,-1},{2,2,-1},{1,2,-1},
   {0,2,-1},{-1,2,-1},{-2,2,-1},{-2,1,-1},
   {-2,0,-1},{-2,-1,-1},
   {-2,-2,1},{-1,-2,1}, // alles mit oben
   {0,-2,1},{1,-2,1},{2,-2,1},{2,-1,1},
   {2,0,1},{2,1,1},{2,2,1},{1,2,1},
   {0,2,1},{-1,2,1},{-2,2,1},{-2,1,1},
   {-2,0,1},{-2,-1,1}};

    String[] icon_attribs = {
	"type","ways","tank","tport","house","pub","port","shop","post","npc"
    };


    final static String[] dir_name_lookup_de={"sp", "sw", "ssw", "s", "sso", "so", "oso", "o", "ono", "no", 
			    "nno", "n", "nnw", "nw", "wnw", "w", "wsw", "ob", "u", "swu",
			    "sswu", "su", "ssou", "sou", "osou", "ou", "onou", "nou", "nnou", "nu",
			    "nnwu", "nwu", "wnwu", "wu", "wswu", "swob", "sswob", "sob", "ssoob", "soob",
			    "osoob", "oob", "onoob", "noob", "nnoob", "nob", "nnwob", "nwob", "wnwob", "wob",
			    "wswob"};

    final static String[] dir_name_lookup_en={"sp", "sw", "ssw", "s", "sse", "se", "ese", "e", "ene", "ne", 
			    "nne", "n", "nnw", "nw", "wnw", "w", "wsw", "u", "d", "swd",
			    "sswd", "sd", "ssed", "sed", "esed", "ed", "ened", "ned", "nned", "nd",
			    "nnwd", "nwd", "wnwd", "wd", "wswd", "swu", "sswu", "su", "sseu", "seu",
			    "eseu", "eu", "eneu", "neu", "nneu", "nu", "nnwu", "nwu", "wnwu", "wu",
			    "wswu"};

    static String[] dir_long_lookup;

    final static String[] dir_long_lookup_de={"special", "suedwesten", "suedsuedwesten", "sueden", "suedsuedosten", "suedosten", "ostsuedosten", "osten", "ostnordosten", "nordosten", "nordnordosten", "norden", "nordnordwesten", "nordwesten", "westnordwesten", "westen", "westsuedwesten", "oben", "unten", "suedwestunten", "suedsuedwestunten", "suedunten", "suedsuedostunten", "suedostunten", "ostsuedostunten", "ostunten", "ostnordostunten", "nordostunten", "nordnordostunten", "nordunten", "nordnordwestunten", "nordwestunten", "westnordwestunten", "westunten", "westsuedwestunten", "suedwestoben", "suedsuedwestoben", "suedoben", "suedsuedostoben", "suedostoben", "ostsuedostoben", "ostoben", "ostnordostoben", "nordostoben", "nordnordostoben", "nordoben", "nordnordwestoben", "nordwestoben", "westnordwestoben", "westoben", "westsuedwestoben"};


    final static String[] dir_long_lookup_en={"special", "southwest", "southsouthwest", "south", "southsoutheast", "southeast", "eastsoutheast", "east", "eastnortheast", "northeast", "northnortheast", "north", "northnorthwest", "northwest", "westnorthwest", "west", "westsouthwest", "up", "down", "southwestdown", "southsouthwestdown", "southdown", "southsoutheastdown", "southeastdown", "eastsoutheastdown", "eastdown", "eastnortheastdown", "northeastdown", "northnortheastdown", "northdown", "northnorthwestdown", "northwestdown", "westnorthwestdown", "westdown", "westsouthwestdown", "southwestup", "southsouthwestup", "southup", "southsoutheastup", "southeastup", "eastsoutheastup", "eastup", "eastnortheastup", "northeastup", "northnortheastup", "northup", "northnorthwestup", "northwestup", "westnorthwestup", "westup", "westsouthwestup"};


  int[] dir_anti_lookup={0,9,10,11,12,13,14,15,16,1,2,3,4,5,6,7,8,18,17,43,44,45,46,47,48,49,50,35,36,37,38,39,40,41,42,27,28,29,30,31,32,33,34,19,20,21,22,23,24,25,26};

  int[][] calcDir_directions={{0,0,0},{-1,-1,0},{0,-1,0},{1,-1,0},{-1,0,0},{0,0,0},{1,0,0},{-1,1,0},{0,1,0},{1,1,0},{0,0,1},{0,0,-1}};  

  int level=0;
  Map[] maps;
  Hashtable info,named;
    Hashtable icons=new HashMap();

  class Map {
    String name;
    int id;
    Hashtable info, special, named;
    int numPoints, numExits, level, aktpoint;
    CheckboxMenuItem menu;
    int[][] points;
    int[][] exits;
    float[] min;
    float maxborder[][];
    public String toString() {
      return name+" ("+id+")";
    }
    Vector polygons;
  }

    class MPolygon {
	float[][] coords;
	int[] levels;
	Color color,textcolor;
	String text;
	float[][] center;
	float[][] border;
	int map;
	Primitive image;
	String imagefile;
	Vector outerPolys;
	Info info;

	public boolean inside(float x, float y, float z) {
	    if (levels[0]>z || levels[1]<z) return false;
	    System.out.println("x,y     "+x+", "+y+", "+z );
	    System.out.println("x,y min "+border[0][0]+", "+border[0][1]);
	    System.out.println("x,y max "+border[1][0]+", "+border[1][1]);
	    if (x<border[0][0] || x>border[1][0] ||
		y<border[0][1] || y>border[1][1]) return false;

	    System.out.println("inside true");
            int hits = 0;

            float ySave = 0;
	    int npoints=coords.length;

            // Find a vertex that's not on the halfline
            int i = 0;
            while (i < npoints && coords[i][1] == y) {
                i++;
	    }

            // Walk the edges of the polygon
	    float dx,dy, rx, ry, xi, yi, xj, yj;

	    for (int n = 0; n < npoints; n++) {
                int j = (i + 1) % npoints;
		xi=coords[i][0];
		yi=coords[i][1];
		xj=coords[j][0];
		yj=coords[j][1];

                dx = xj - xi;
                dy = yj - yi;

                // Ignore horizontal edges completely
                if (dy != 0) {
                    // Check to see if the edge intersects
                    // the horizontal halfline through (x, y)
                    rx = x - xi;
                    ry = y - yi;

                    // Deal with edges starting or ending on the halfline
                    if (yj == y && xj >= x) {
                        ySave = yi;
		    }
                    if (yi == y && xi >= x) {
                        if ((ySave > y) != (yj > y)) {
			    hits--;
			}
		    }

                    // Tally intersections with halfline
                    float s = ry / dy;
                    if (s >= 0.0 && s <= 1.0 && (s * dx) >= rx) {
                        hits++;
		    }
                }
                i = j;
            }

            // Inside if number of intersections odd
            return (hits % 2) != 0;
	}
	public MPolygon parse(Info poly) {
	    try {
		System.out.println("parsePolygon "+poly);
		MPolygon p=new MPolygon();
		p.info=poly;
		p.map = Integer.parseInt(poly.getAttribute("map"));
		p.levels=new int[2];
		p.levels[0]=new Integer(poly.getAttribute("level_from")).intValue();
		p.levels[1]=new Integer(poly.getAttribute("level_to")).intValue();
		p.coords=parseCoords(poly.getAttribute("coords"));
		p.color=parseColor(poly.getAttribute("color"));
		p.text=poly.getAttribute("text");
		float[] cres=modifyColor(p.color,-1,1.5f);
		p.textcolor=new Color(cres[0],cres[1],cres[2]);
		p.center=parseCoords(poly.getAttribute("center"));

		p.border=new float[2][2];
		p.border[0][0]=p.coords[0][0];
		p.border[1][0]=p.coords[0][0];
		p.border[0][1]=p.coords[0][1];
		p.border[1][1]=p.coords[0][1];
		for (int i=0;i<2;i++) {
		    for (int j=0;j<p.coords.length;j++) {
			//		      System.out.print(p.coords[j][i]+", ");
		      if (p.coords[j][i]<p.border[0][i]) p.border[0][i]=p.coords[j][i];
		      if (p.coords[j][i]>p.border[1][i]) p.border[1][i]=p.coords[j][i];
		    }
		    //		    System.out.println();
		}
		/*
		printArray(p.coords,true);
		System.out.println();
		printArray(p.border,true);
		*/
		p.imagefile = poly.getAttribute("image");
		System.out.println("Imagefile "+p.imagefile);
		if (p.imagefile!=null && p.imagefile.length()>0) {
		    p.image=mapcanvas.typeFactory("TiledPicture "+p.imagefile,null);
		    System.out.println("ImagePolygon: TiledPicture "+p.imagefile);

		    p.outerPolys=getInversePolygons(p.coords,p.border);

		} else p.outerPolys=null;
		return p;

	    } catch(Exception e) {
		e.printStackTrace();
		return null;
	    }
	}

	protected int borderTouched(float x,float y,float[] border) {
	    if (y == border[0]) return 0;
	    if (x == border[1]) return 1;
	    if (y == border[2]) return 2;
	    if (x == border[3]) return 3;
	    return -1;
	}

	protected int touchedBorders(float x,float y,float[] border, int[] touched) {
	    int count=0;
	    if (y == border[0]) touched[count++]=0;
	    if (x == border[1]) touched[count++]=1;
	    if (y == border[2]) touched[count++]=2;
	    if (x == border[3]) touched[count++]=3;
	    return count;
	}

	protected Vector getInversePolygons(float[][] xy, float[][] b) {
	    float[] border = { b[1][1], b[0][0], b[0][1], b[1][0] };
	    int i=0;
	    System.out.println("BOrder");
	    for (i=0;i<border.length;i++) {
		System.out.println(""+i+" "+border[i]);
	    }

	    int num=xy.length;
	    int lastborder;
	    int[] touched=new int[4];
	    int numtouched=0;
	    float[] x=new float[num];
	    float[] y=new float[num];
	    for (i=0;i<num;i++) {
		x[i]=xy[i][0];
		y[i]=xy[i][1];
		System.out.println(""+i+" "+x[i]+","+y[i]);
	    }
	    i=0;
	    while ((lastborder=borderTouched(x[i],y[i],border))==-1) {
		i++;
		if (i==num) return null;
	    }
	    Vector result=new Vector();
	    int lastindex=i;
	    int aktindex=i;
	    System.out.println("index "+lastindex+" border "+lastborder);
	    int edgecount=1;
	    int aktborder=-1;
	    float[][] pxy, pi;
	    for (int k=0;k<num;k++) {
		aktindex = (aktindex+1) % num ;
		edgecount++;
		aktborder=borderTouched(x[aktindex],y[aktindex],border);
		if (aktborder>-1) {
		    if (aktborder!=lastborder) edgecount++;
		    System.out.println("index "+aktindex+" border "+aktborder+" knoten "+edgecount);
		    pxy=new float[edgecount][2];
		    for (i=0;i<edgecount;i++) {
			pxy[i][0]=x[(i+lastindex) % num];
			pxy[i][1]=y[(i+lastindex) % num];
			//			System.out.println(i+" ri "+((i+lastindex)%num)+" "+pxy[0][i]+","+pxy[1][i]);
		    }
		    if (aktborder!=lastborder) {
			int diff=Math.abs(aktborder-lastborder);
			if (diff==2) {
			    if ((lastborder+diff)%4==aktborder) aktborder=(lastborder+1) % 4;
			    else aktborder=(lastborder-1) % 4;
			}
			pxy[edgecount-1][0]=border[((lastborder % 2) == 1)?lastborder:aktborder];
			pxy[edgecount-1][1]=border[((lastborder % 2) == 0)?lastborder:aktborder];
			//			System.out.println(edgecount-1+" bds "+(((aktborder % 2) == 1)?aktborder:lastborder)+"/"+(((aktborder % 2) == 0)?aktborder:lastborder)+" "+pxy[0][edgecount-1]+","+pxy[1][edgecount-1]);
		    }
//		    Polygon p=new Polygon(px,py,edgecount);
		    result.addElement(pxy);
		    numtouched=touchedBorders(x[aktindex],y[aktindex],border,touched);
		    if (numtouched>1) {
			lastborder=touched[1];
		    } else {
			lastborder=aktborder;
		    }
		    aktborder=-1;
		    lastindex=aktindex;
		    edgecount=1;
		}
	    }
	    return result;
	}
	public void printArray(float[][] a, boolean rowxy) {
	    System.out.println(" x, y");
	    if (rowxy)
		for (int i=0;i<a.length;i++)
		    System.out.println(a[i][0]+", "+a[i][1]);
	    else
		for (int i=0;i<a[0].length;i++)
		    System.out.println(a[0][i]+", "+a[1][i]);
	}
	public long dtime(long time, String text) {
	    long res=System.currentTimeMillis();
	    System.out.println(text+" : "+(res-time)+" ms");
	    return res;
	}
	public void draw(Graphics g,int level) {
	    if (levels[0]>level || levels[1]<level) return;
	    int[][] polycoords=rToVi(coords);
	    int[][] bd=rToVi(border);
	    g.setColor(color);
	    Polygon p=new Polygon(polycoords[0],polycoords[1],polycoords[0].length);
	    java.awt.Rectangle b=new java.awt.Rectangle(bd[0][0],bd[1][1],bd[0][1]-bd[0][0],bd[1][0]-bd[1][1]);
	    if (image==null) {
		g.fillPolygon(p);
	    } else {
		Shape clip=g.getClip();
		g.setClip(b);
		image.draw(b.x+b.width/2,b.y+b.height/2,b.width,b.height,color,g);
		if (outerPolys!=null) {
		    float[][] pf;
		    int[][] pi;
		    g.setColor(mapcanvas.background);
		    for (Enumeration en=outerPolys.elements();en.hasMoreElements();) {
			pf=(float[][])en.nextElement();
			pi=rToVi(pf);
			g.fillPolygon(pi[0],pi[1],pi[0].length);
		    }
		}
		Dimension s=mapcanvas.getSize();
		g.setClip(0,0,s.width,s.height);
	    }
	    if (text==null || text.length()==0) return;
	    g.setColor(textcolor);
	    int[][] polycenter=rToVi(center);
	    float[][] polyborder=rToV(border);
	    Font oldfont=g.getFont();
	    int fontsize=oldfont.getSize();
	    FontMetrics fm=g.getFontMetrics();

	    if (b.width>0 && b.height>0) {
	    float dx=(float)fm.stringWidth(text)/b.width;
	    float dy=(float)fontsize/b.height;

	    fontsize=Math.round((int)Math.sqrt(fontsize/Math.max(dx,dy)));
	    fontsize*=fontsize;
	    } else fontsize=dp[mapcanvas.TEXT_SIZE];
	    if (fontsize>5) {
		if (fontsize>100) fontsize=100;
		g.setFont(Util.getCachedFont(fontsize));
		fm=g.getFontMetrics();
		polycenter[0][0]-=fm.stringWidth(text)/2;
		polycenter[1][0]+=fontsize/2;
		g.drawString(text,polycenter[0][0],polycenter[1][0]);
		g.setFont(oldfont);
	    }
	    
	}
    }

    Vector polygons;

    protected void parsePolygons(int map) {
	Vector result=new Vector();
	Vector polys=Info.getType("polygon");
	if (polys==null) return;
	Info poly=null;
	MPolygon p=null;
	MPolygon instance=new MPolygon();
	String sMap=""+map;
	for (Enumeration en=polys.elements();en.hasMoreElements();) {
	    poly=(Info)en.nextElement();
	    if (!sMap.equals(poly.getAttribute("map"))) continue;
	    p=instance.parse(poly);
	    if (p!=null) result.addElement(p);
	}
	if (result.size()==0) return;
	if (map == aktmap )
	    polygons=result;
	if (maps[map]!=null)
	    maps[map].polygons=result;
    }

    class CommandActionListener implements ActionListener, ItemListener {

	public void actionPerformed(ActionEvent e) {
	    evalParamCommand(e.getActionCommand());
	}
	public void itemStateChanged(ItemEvent e) {
	    evalParamCommand(((MenuItem)e.getSource()).getActionCommand());	    
	}
    }
    CommandActionListener commandAL=new CommandActionListener();

    protected MenuItem createMenuItem(String label, String command) {
	MenuItem mi=new MenuItem(label);
	if (command!=null) {
	    mi.setActionCommand(command);
	    mi.addActionListener(commandAL);
	}
	return mi;
    }

    Hashtable cachedMenues=new HashMap();

    protected MenuItem createOptionMenuItem(String label, String option) {
	CheckboxMenuItem mi=new CheckboxMenuItem(label);
	System.out.println("createOptionMenuItem"+option);
	if (option!=null) {
	    mi.setActionCommand("opt "+option);
	    mi.addItemListener(commandAL);
	}
	cachedMenues.put(option,mi);
	return mi;
    }

    protected void setOptionMenuState(String option, boolean value) {
	Object menu=cachedMenues.get(option);
	System.out.println("setOptionMenuState "+option+" "+value+" "+menu);
	if (menu!=null)
	    ((CheckboxMenuItem)menu).setState(value);
    }
    protected void setOptionMenuStates() {
      setOptionMenuState("para",para);
      setOptionMenuState("grid",showgrid);
      setOptionMenuState("vgrid",showvirtual);
      setOptionMenuState("numbers",shownumbers);
    }

    public String I18N(String text) {
	return i18n.trans(text);
    }

    protected void addZoomNodeMenu(Menu m, int number) {
	m.add(createMenuItem(""+number+" "+I18N("Knoten"),"zoom nodes "+number));
    }
    Menu map_menu=null;

    protected void buildAiMenues(Menu ai) {
	int numAttr=Node.attr_names.length;
	for (int i=0;i<numAttr;i++)
	    ai.add(createMenuItem(I18N(Node.attr_names[i][1]),
				  "ai "+Node.attr_names[i][0]+" <"+
				  Node.attr_names[i][0]+">"));
    }
    public MenuBar createMenus() {
	MenuBar menu=new MenuBar();

	Menu allg=new Menu(I18N("Allgemeines"));
	allg.add(createMenuItem(I18N("Optionen anzeigen"),"opt"));
	Menu options=new Menu(I18N("Optionen"));
	options.add(createOptionMenuItem(I18N("Parallelwelt"),"para"));
	options.add(createOptionMenuItem(I18N("Knotennummern anzeigen"),"numbers"));
	options.add(createOptionMenuItem(I18N("Koordinatennetz"),"grid"));
	options.add(createOptionMenuItem(I18N("Bildschirmkoordinatennetz"),"vgrid"));
	setOptionMenuStates();
	allg.add(options);
	if (!isApplet)
	    allg.add(createMenuItem(I18N("Beenden"),"Q"));
	menu.add(allg);

	map_menu=new Menu(I18N("Karten"));
	map_menu.add(createMenuItem(I18N("Karten wechseln"),"sm <mapname>"));
	map_menu.add(createMenuItem(I18N("Karten anzeigen"),"lm"));
	map_menu.add(createMenuItem(I18N("Karten erzeugen"),"am <mapname>"));
	map_menu.add(createMenuItem(I18N("Karte löschen"),"dm <mapname>"));
	map_menu.add(createMenuItem(I18N("Karten umbenennen"),"rename <mapname> <newname>"));
	map_menu.addSeparator();
	if (!isApplet) {
	map_menu.add(createMenuItem(I18N("aktuelle Karte speichern"),"save"));
	map_menu.add(createMenuItem(I18N("alle Karten speichern"),"save all"));
	}
	if (map_menues.size()>0)
	    for (Enumeration en=map_menues.elements();en.hasMoreElements();)
		map_menu.add((MenuItem)en.nextElement());
	menu.add(map_menu);


	Menu node=new Menu(I18N("Knoten"));
	node.add(createMenuItem(I18N("Infos anzeigen"),"si"));

	Menu ai=new Menu(I18N("Infos setzen"));

	node.add(ai);
	ai.add(createMenuItem(I18N("allgemein"),"ai <key> <value>"));
	buildAiMenues(ai);

	node.add(createMenuItem(I18N("Infos löschen"),"ai <key>"));
	node.add(createMenuItem(I18N("Knoten erzeugen (Maus)"),"an <coord>"));
	node.add(createMenuItem(I18N("aktuellen Knoten löschen"),"DP"));
	node.add(createMenuItem(I18N("markierte Knoten löschen"),"DP marked"));
	node.add(createMenuItem(I18N("Flags anzeigen"),"sf"));
	node.add(createMenuItem(I18N("Flags setzen"),"sf <flag>"));

	node.addSeparator();
	node.add(createMenuItem(I18N("Markieren"),"mark <selection>"));
	node.add(createMenuItem(I18N("Demarkieren"),"unmark <selection>"));
	node.addSeparator();
	node.add(createMenuItem(I18N("Suche"),"lna <key> <selection> [<value>] [-m]"));
	menu.add(node);

	Menu exit=new Menu(I18N("Ausgänge"));
	exit.add(createMenuItem(I18N("Ausgang erzeugen"),"ae <direction>"));
	exit.add(createMenuItem(I18N("Rueckweg erzeugen"),"ar <command>"));
	exit.add(createMenuItem(I18N("Ausgang parametrisiert erzeugen"),"asp -d<direction> -p<pnode> -m<map> move command"));
	exit.add(createMenuItem(I18N("Ausgang löschen"),"DE <direction>"));
	exit.add(createMenuItem(I18N("Ausgang parametrisiert löschen"),"DE -d<direction> -p<pnode> -m<map> move command"));
	exit.addSeparator();
	exit.add(createMenuItem(I18N("Ausgang färben"),"aec <direction> <color>"));
	exit.add(createMenuItem(I18N("Tür einfügen"),"aed <direction> <doorname>"));
	exit.add(createMenuItem(I18N("zusätzlicher Ausgangsbefehl"),"aecmd <direction> <command>"));
	menu.add(exit);

	Menu zoom=new Menu(I18N("Zoom"));
	zoom.add(createMenuItem(I18N("Zentrieren"),"center"));
	Menu zoom_nodes=new Menu(I18N("sichtbare Knoten"));
	addZoomNodeMenu(zoom_nodes,5);
	addZoomNodeMenu(zoom_nodes,10);
	addZoomNodeMenu(zoom_nodes,15);
	addZoomNodeMenu(zoom_nodes,20);
	addZoomNodeMenu(zoom_nodes,50);
	addZoomNodeMenu(zoom_nodes,75);
	addZoomNodeMenu(zoom_nodes,100);
	addZoomNodeMenu(zoom_nodes,150);
	addZoomNodeMenu(zoom_nodes,200);

	zoom.add(zoom_nodes);
	zoom.add(createMenuItem(I18N("vergrößern"),"zoom"));
	zoom.add(createMenuItem(I18N("verkleinern"),"unzoom"));
	zoom.add(createMenuItem(I18N("ganze Karte"),"zoom map"));
	zoom.add(createMenuItem(I18N("aktuelles Level"),"zoom level"));
	menu.add(zoom);

	Menu objects=new Menu(I18N("Objekte"));
	objects.add(createMenuItem(I18N("Objekte auflisten"),"oi"));
	objects.add(createMenuItem(I18N("Polygone"),"oi polygon"));
	objects.add(createMenuItem(I18N("NPCs"),"oi NPC"));
	objects.add(createMenuItem(I18N("Knoten"),"oi Knoten"));
	objects.add(createMenuItem(I18N("Objekt bearbeiten"),"ei <nummer>"));
	objects.add(createMenuItem(I18N("Objekt löschen"),"di <nummer>"));
	objects.addSeparator();
	objects.add(createMenuItem(I18N("alle Objekte speichern"),"saveObjects"));
	objects.add(createMenuItem(I18N("alle Objekt neuladen"),"loadObjects"));
	objects.addSeparator();
	objects.add(createMenuItem(I18N("Polygon zeichnen (Maus)"),"poly - <color> <coords.> -t <text>"));
	objects.add(createMenuItem(I18N("Polygon editieren"),"ep <coord>"));

	menu.add(objects);
	Menu help_menu=new Menu(I18N("Hilfe"));
	help_menu.add(createMenuItem(I18N("Hilfeübersicht"),"?"));
	help_menu.add(createMenuItem(I18N("Hilfe zu Schlüsselwort"),"? <keyword>"));
	help_menu.add(createMenuItem(I18N("Farben"),"? farben"));
	menu.setHelpMenu(help_menu);
	Menu allg_help=new Menu(I18N("Allgemeines"));
	help_menu.add(allg_help);
	Menu object_help=new Menu(I18N("Hilfe zu Objekten"));
	help_menu.add(allg_help);
	Menu node_help=new Menu(I18N("Hilfe zu Knoten"));
	help_menu.add(node_help);
	Menu exits_help=new Menu(I18N("Hilfe zu Ausgängen"));
	help_menu.add(exits_help);
	Menu command_help=new Menu(I18N("Hilfe zu Kommandos"));
	help_menu.add(command_help);
	checkHelp();
	String helptext=(String)help.get("help");
	StringTokenizer st=new StringTokenizer(helptext,":\n");
	String command=null;
	int off=-1;
	try {
	while (st.hasMoreTokens()) {
	    command="? "+st.nextToken();
	    helptext=st.nextToken();
	    off=helptext.indexOf(")");
	    if (off>-1 && off<15) helptext=helptext.substring(off+1);
	    if (command.length()>6)
		allg_help.add(createMenuItem(I18N(helptext),command));
	    else 
		if (helptext.indexOf(I18N("noten"))>-1)
		node_help.add(createMenuItem(I18N(helptext),command));
	    else 
		if (helptext.indexOf(I18N("usg"))>-1)
		exits_help.add(createMenuItem(I18N(helptext),command));
	    else 
		if (helptext.indexOf(I18N("bjek"))>-1)
		object_help.add(createMenuItem(I18N(helptext),command));
	    else 
		command_help.add(createMenuItem(I18N(helptext),command));

	}
	} catch(Exception e) {
	}
	return menu;
    }

  MapCanvas mapcanvas;
  int MAXLEVEL=50;

    class Primitive {
	public Primitive() {}
	Color color=null;
	public void setColor(Color c) {
	    color=c;
	}
	public void draw(float x, float y, float dx, float dy, Color color,Graphics g) {
	    if (this.dx>-1) dx=dp[this.dx];
	    if (this.dy>-1) dy=dp[this.dy];
	    if (this.color!=null && color==null) color=this.color;
	    if (color!=null) g.setColor(color);
	    drawIt(x,y,dx,dy,color,g);
	}

	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    if (color!=null) g.setColor(color);
	    g.drawRect((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy);
	}
	public void setParams(String p) {}
	public void setDrawParams(String p) {}
	int dx=-1;
	int dy=-1;
	public void setSize(int dx) {
	    this.dx=dx;
	    this.dy=dx;
	}
	public void setSize(int dx,int dy) {
	    this.dx=dx;
	    this.dy=dy;
	}
    }
    class RandomPicture extends Picture {
	Vector images;
	
	public void setParams(String files) {
	    StringTokenizer st=new StringTokenizer(files);
	    String file;
	    String prefix="";
	    while (st.hasMoreTokens()) {
		file=st.nextToken();
		if (file.endsWith("/")) {
		    prefix=file;
		} else {
		    try {
			URL u;
			try {
			    u=new URL(prefix+file);
			} catch(Exception e) {
			    u=getResourceURL(null,prefix+file);
			}
			i=Toolkit.getDefaultToolkit().getImage(u);
			if (images==null) images=new Vector();
			images.addElement(i);
		    } catch(Exception e) {
		    }
		}
	    }
	}
	String param;
	Random r=new Random();
	int index;
	public void setDrawParams(String p) {
	    param=p;
	}
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    if (images==null) i=null;
	    else {
		if (param!=null) {
		    index=param.hashCode();
		    index*=index*index;
		    r.setSeed((long)index);
		}
		index=(int)(r.nextDouble()*images.size());
		System.out.println("index "+index+" size "+images.size()+""+param+" "+param.hashCode());
		i=(Image)images.get(index);
	    }
	    super.drawIt(x,y,dx,dy,color,g);
	    param=null;
	}

    }

    class Picture extends Primitive implements ImageObserver {
	Image i;
	int w=-1,h=-1;
	public void setParams(String file) {
	    try {
		URL u;
		try {
		    u=new URL(file);
		    System.out.println("Picture1: "+u);
		} catch(Exception e) {
		    u=getResourceURL(null,file);
		    System.out.println("Picture2: "+u);
		}
		i=Toolkit.getDefaultToolkit().getImage(u);
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    if (i==null) super.drawIt(x,y,dx,dy,color,g);
	    else {
		if (w==-1 || h==-1) {
		    w=i.getWidth(this);
		    h=i.getHeight(this);
		}
		//		System.out.println(dx+" "+dx+" > w>>2"+(w>>2)+" (w "+w);
		if (w>-1 && h>-1 && dx > w/4 && dy > h/4)
		    g.drawImage(i,(int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy,this);
		else super.drawIt(x,y,dx,dy,color,g);
	    }
	}
	
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	    if ((infoflags & ( ALLBITS | FRAMEBITS )) > 0) {
		needRepaint(); 
		return false;
	    }
	    return true;
	}

    }
    class TiledPicture extends Picture {
	int height, width;
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    h=i.getHeight(null);
	    w=i.getWidth(null);
	    System.out.println(width+" < "+dx+" : "+height+" < "+dy);
	    if (i==null || h==-1 || w==-1 || (w>=dx && h>=dy)) super.drawIt(x,y,dx,dy,color,g);
	    else {
		Dimension d=mapcanvas.getSize();
		int icx=(int)(x-dx/2), icy=(int)(y-dy/2);
		int idx=(int)dx, idy=(int)dy;

		int minx=Math.max(0,icx);
		int miny=Math.max(0,icy);

		int maxx=Math.min(d.width,(int)(icx+dx));
		int maxy=Math.min(d.height,(int)(icy+dy));

		for (int ix=minx; ix<maxx; ix+=w)
		    for (int iy=miny; iy<maxy; iy+=h)
			g.drawImage(i,ix,iy,this);			
	    }
	}
    }

    class Rectangle extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.drawRect((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy);
	}
    }

    class FilledRectangle extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.fillRect((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy);
	}
    }

    class Rectangle3D extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.draw3DRect((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy,true);
	}
    }
    class FilledRectangle3D extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.fill3DRect((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy,true);
	}
    }

    class Circle extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.drawArc((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy,0,360);
	}
    }

    class FilledCircle extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.fillArc((int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy,0,360);
	}
    }

    class FilledCircle3D extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    fill3DCircle(g,(int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy);

	}
	public void fill3DCircle(Graphics g,int x, int y,int w, int h) {
	       Color c=g.getColor();
	       Color brighter=c.brighter();
	       Color darker=c.darker();
	       g.fillArc(x,y,w,h,0,360);
	       g.setColor(brighter);
	       g.drawArc(x,y,w,h,45,135);
	       g.setColor(darker);
	       g.drawArc(x,y,w,h,225,135);
	   }

	}

    class Circle3D extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    draw3DCircle(g,(int)(x-dx/2),(int)(y-dy/2),(int)dx,(int)dy);

	}
	public void draw3DCircle(Graphics g,int x, int y,int w, int h) {
	       Color c=g.getColor();
	       Color brighter=c.brighter();
	       Color darker=c.darker();
	       g.drawArc(x,y,w,h,0,360);
	       g.setColor(brighter);
	       g.drawArc(x,y,w,h,45,135);
	       g.setColor(darker);
	       g.drawArc(x,y,w,h,225,135);
	   }
	}

    class FilledHouse extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    g.setColor(color);
	    g.fillRect((int)(x-dx/2),(int)(y),(int)dx,(int)(dx/2));
	    g.drawLine((int)(x-dx/2),(int)(y),(int)x,(int)(y-dy/2));
	    g.drawLine((int)x,(int)(y-dy/2),(int)(x+dx/2),(int)y);
	}
    }

    class Plus extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    gh.drawLine(g,x-dx/2,y,x+dx/2,y,dp[1],color);
	    gh.drawLine(g,x,y-dy/2,x,y+dy/2,dp[1],color);
	}
    }

    class Minus extends Primitive {
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    gh.drawLine(g,x-dx/2,y,x+dx/2,y,dp[1],color);
	}
    }

    class NPCSketch extends Primitive {
	String value;
	public void setDrawParams(String s) {
	    value=s;
	}
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    float[] col=new float[3];
	    // rot wenn auto, blau wenn nich
	    if (value.indexOf("#A")!=-1) col[0]=1;
	    else col[2]=1;
	    int off=(value.indexOf("#L"));
	    double strength=0.2;
	    int lv=1;
	    StringBuffer sb=new StringBuffer();
	    try {
		if (off!=-1) {
		    off+=2;
		    while ((off<value.length()) && Character.getType(value.charAt(off))==Character.DECIMAL_DIGIT_NUMBER) {
			sb.append(value.charAt(off++));
		    }
		    lv=Integer.parseInt(sb.toString());
		    strength=(float)lv/MAXLEVEL;
		}
	    } catch(NumberFormatException ex) {
	    }
	    if (strength>1) strength=1;
	    for (int i=col.length-1;i>=0;i--) {
		col[i]*=strength;
	    }
	    g.setColor(new Color(col[0],col[1],col[2]));
	    g.fillRect((int)x,(int)y,dp[1],dp[1]);
	    g.drawLine((int)(x-dp[2]),(int)(y+dp[3]),(int)(x+dp[2]),(int)(y+dp[3]));
	    if (value.indexOf("#B")!=-1) 
		g.drawLine((int)(x+dp[2]),(int)(y+dp[1]),(int)(x+dp[2]),(int)(y+dp[5]));
	    g.drawLine((int)x,(int)(y+dp[3]),(int)(x-dp[2]),(int)(y+dp[6]));
	    g.drawLine((int)x,(int)(y+dp[3]),(int)(x+dp[2]),(int)(y+dp[6]));
	}
    }

    class Text extends Primitive {
	String text;
	public void setParams(String s) {
	    text=s;
	}
	protected void drawIt(float x, float y, float dx, float dy, Color color,Graphics g) {
	    if (text==null || text.length()==0) return;
	    if (color!=null) g.setColor(color);
	    if (dx!=0 && dy!=0) {
		Font oldfont=g.getFont();
		int fontsize=oldfont.getSize();
		FontMetrics fm=g.getFontMetrics();
		
		dx=fm.stringWidth(text)/(dx);
		dy=fontsize/(dy);
		
		fontsize=Math.round(fontsize/Math.max(dx,dy));
		g.setFont(Util.getCachedFont(fontsize));
		fm=g.getFontMetrics();
		x-=fm.stringWidth(text)/2;
		y+=fontsize/2;
		g.drawString(text,(int)x,(int)y);
		g.setFont(oldfont);
	    } else
		g.drawString(text,(int)x,(int)y);
	}
    }

    class GraphicsHelper {
	public GraphicsHelper() {
	}

	public void drawLine(Graphics g, float x0, float y0, float x1, float y1, int d, Color color) {
	    //	    counter++;
	    if (d==0) d=1;
	    if (color!=null) g.setColor(color);
	    if (d==1) g.drawLine((int)x0,(int)y0,(int)x1,(int)y1);
	    else {
		float m;
		if (y0>y1 || x1<x0) {
		    m=y1;y1=y0;y0=m;
		    m=x1;x1=x0;x0=m;
		}
		float dx=x1-x0,dy=y1-y0;
		g.setColor(color);
		if (dx==0 && dy==0) return;
		int dorg=d;
		d--;
		int d0=d/2;
		d-=d0;
		if (dx==0f) {
		    g.fill3DRect((int)x0-d0,(int)y0,(int)dx+dorg,(int)dy,true);
		} else
		if (dy==0f) {
		    g.fill3DRect((int)x0,(int)y0-d0,(int)dx,(int)dy+dorg,true);
		} else {
		    m=-(dx/dy);
		    int[] x,y;
		    if (Math.abs(m)<1) {
			x=new int[] { (int)(x0-d0),(int)(x0+d),(int)(x1+d), (int)(x1-d0) };
			y=new int[] { (int)(y0-m*d0), (int)(y0+m*d),(int)(y1+m*d), (int)(y1-m*d0) };
		    } else {
			m=1/m;
			x=new int[] { (int)(x0-m*d0), (int)(x0+m*d),(int)(x1+m*d), (int)(x1-m*d0) };
			y=new int[] { (int)(y0-d0), (int)(y0+d),(int)(y1+d), (int)(y1-d0) };
		    }
		    g.fillPolygon(x,y,4);
		    if (color==null) 
			color=g.getColor();
		    Color darker=color.darker(), brighter=color.brighter();

		    g.setColor(darker);
		    g.drawLine(x[0],y[0],x[1],y[1]);
		    g.drawLine(x[1],y[1],x[2],y[2]);
		    g.setColor(brighter);
		    g.drawLine(x[2],y[2],x[3],y[3]);
		    g.drawLine(x[3],y[3],x[0],y[0]);
		}
	    }
	}

    }
    class MapCanvas extends Canvas {

	Color downcolor;
	Color invalidcolor;
	Color exitStringcolor;
	Color upcolor;
	Color specialcolor;
	Color levelcolor;
	Color mapcolor;
	Color selectedColor;
	Color aktcolor;
	Color overlapcolor;
	Color paracolor;
	Color housecolor;
	Color nodecolor;
	Color nodetextcolor;
	Color infotextcolor;
	Color visitedcolor;
	Color cgrid, ctext, ccenter;
	Color background;
	Color markerColor;
	boolean colors=false;

	int LINE_WIDTH;
	int VISITED_SIZE;
	int ICON_SIZE;
	int UNVISITED_SIZE;
	int SELECTED_SIZE;
	int AKT_SIZE;
	int INFO_SIZE;
	int TEXT_SIZE;
	int EXITTEXT_SIZE;
	int MAPSWITCH_SIZE;
	int UP_SIZE;
	int DOWN_SIZE;
	int MARKER_SIZE;

	protected void initColors() {
	    if (colors) return;
	    colors=true;
	    markerColor=getPropColor("marker");
	    downcolor=getPropColor("down");
	    invalidcolor=getPropColor("invalid");
	    exitStringcolor=getPropColor("exitString");
	    upcolor=getPropColor("up");
	    specialcolor=getPropColor("special");
	    levelcolor=getPropColor("level");
	    mapcolor=getPropColor("lines");
	    selectedColor=getPropColor("selected");
	    aktcolor=getPropColor("akt");
	    overlapcolor=getPropColor("overlap");
	    paracolor=getPropColor("para");
	    housecolor=getPropColor("house");
	    nodecolor=getPropColor("node");
	    nodetextcolor=getPropColor("nodetext");
	    infotextcolor=getPropColor("infotext");
	    cgrid=getPropColor("grid");
	    ctext=getPropColor("gridtext");
	    ccenter=getPropColor("gridcenter");
	    background=getPropColor("background");
	    visitedcolor=getPropColor("visited");

	    LINE_WIDTH=getPropSize("line",2);
	    ICON_SIZE=getPropSize("icon",16);
	    VISITED_SIZE=getPropSize("visited",13);
	    UNVISITED_SIZE=getPropSize("unvisited",5);
	    SELECTED_SIZE=getPropSize("selected",6);
	    MARKER_SIZE=getPropSize("marker",6);
	    AKT_SIZE=getPropSize("akt",18);
	    INFO_SIZE=getPropSize("info",6);
	    TEXT_SIZE=getPropSize("text",8);
	    EXITTEXT_SIZE=getPropSize("exittext",8);
	    MAPSWITCH_SIZE=getPropSize("mapswitch",3);
	    UP_SIZE=getPropSize("up",4);
	    DOWN_SIZE=getPropSize("down",4);
	}
	Hashtable types=new HashMap();

	protected void initTypes() {
	    for (Enumeration en=props.keys();en.hasMoreElements();) {
		String name=(String)en.nextElement();
		if (name.endsWith("type")) {
		    iniTypes.put(name.substring(0,name.length()-4),props.get(name));
		}
	    }
	    getType("node","FilledRectangle3D");
	    getType("background","FilledRectangle");
	    getType("akt","FilledRectangle3D");
	    getType("up","FilledRect");
	    getType("down","FilledRect");
	    getType("marker","FilledRect");
	    getType("selected","Rectangle3D");
	    getType("visited","FilledRectangle3D");
	    getType("unvisited","FilledRectangle3D");
	    getType("mapswitch","FilledRectangle3D");
	    getType("pub","Text K");
	    getType("port","Text H");
	    getType("shop","Text S");
	    getType("post","Text M");
	    getType("tank","Text T");
	    getType("ways","Text W");
	    getType("tport","Circle3D");
	    getType("npc","NPCSketch");
	    getAreaTypes();
	}

	Primitive defaultType=new Primitive();
	Hashtable iniTypes=new HashMap();
	protected void getType(String name, String defaultValue) {
	    String s=(String)iniTypes.get(name);
	    iniTypes.remove(name);
	    if (s==null) s=defaultValue;
	    System.out.println(name);
	    types.put(name,typeFactory(s,defaultValue));
	}

	protected void getAreaTypes() {
	    for (Enumeration en=iniTypes.keys();en.hasMoreElements();) {
		String name=(String)en.nextElement();
		types.put(name,typeFactory((String)iniTypes.get(name),"Text "+name));
	    }
	    iniTypes=new HashMap();
	}


	Primitive defaultPrimitive=new Primitive();
	public Primitive typeFactory(String atype, String defaultValue) {
	    if (atype==null && defaultValue==null) return defaultPrimitive;
	    if (atype==null) return typeFactory(defaultValue,null);
	    StringTokenizer st=new StringTokenizer(atype);
	    if (!st.hasMoreTokens()) return typeFactory(defaultValue,null);
	    String type=st.nextToken();
	    int dx=-1, dy=-1;
	    try {
	       dx=new Integer(type).intValue();
	       type=st.nextToken();
	       dy=new Integer(type).intValue();
	       type=st.nextToken();
	    } catch(Exception e) {
	    }
	    Color c=null;
	    if (type.startsWith("(")) {
		while (!type.endsWith(")")) 
		    type+=" "+st.nextToken();
		type=type.substring(1,type.length()-1);
		System.out.println("ColorType: "+type);
		c=parseColor(type);
		type=st.nextToken(" ");
		System.out.println("Type: #"+type+"#");
	    }
	    Primitive p=null;
	    if (type.equals("Primitive")) p=new Primitive();
	    if (type.equals("Picture")) p=new Picture();
	    if (type.equals("RandomPicture")) p=new RandomPicture();
	    if (type.equals("TiledPicture")) p=new TiledPicture();
	    if (type.equals("Rectangle")) p=new Rectangle();
	    if (type.equals("FilledRectangle")) p=new FilledRectangle();
	    if (type.equals("FilledHouse")) p=new FilledHouse();
	    if (type.equals("Rectangle3D")) p=new Rectangle3D();
	    if (type.equals("FilledRectangle3D")) p=new FilledRectangle3D();
	    if (type.equals("Circle")) p=new Circle();
	    if (type.equals("FilledCircle")) p=new FilledCircle();
	    if (type.equals("Circle3D")) p=new Circle3D();
	    if (type.equals("FilledCircle3D")) p=new FilledCircle3D();
	    if (type.equals("Text")) p=new Text();
	    if (type.equals("Plus")) p=new Plus();
	    if (type.equals("Minus")) p=new Minus();
	    if (type.equals("NPCSketch")) p=new NPCSketch();
	    if (p==null) p=typeFactory(defaultValue,null);
	    if (st.hasMoreTokens()) {
		p.setParams(Util.allTokens(st));
	    }
	    if (dx>-1) {
		if (dy>-1) p.setSize(dx,dy);
		else p.setSize(dx);
	    }
	    if (c!=null) p.setColor(c);
	    System.out.println(type+" "+p);
	    return p;
	}

    public MapCanvas() {

      addComponentListener(new ComponentAdapter() {
	public void componentShown(ComponentEvent e) {
	  System.out.println("shown");
	  updateSize();
	}
	public void componentResized(ComponentEvent e) {
	  System.out.println("resized");
	  updateSize();
	}
      });
      addMouseListener(mouser);
      addMouseMotionListener(mouser);
      enableEvents(AWTEvent.KEY_EVENT_MASK);
      enableEvents(AWTEvent.FOCUS_EVENT_MASK);
      addKeyListener(typer);
    }
    Hashtable lines=null;

	protected void drawPrimitive(String name, float x, float y, float dx, float dy, Color color,Graphics g) {
	  Primitive p=((Primitive)types.get(name));
	  if (p==null) p=defaultType;
	  //	  if (p instanceof Picture) color=background;
	  p.draw(x,y,dx,dy,color,g);
	}

	protected void drawPrimitive(String name, float x, float y, float dx, float dy, Color color,Graphics g, String sparams) {
	  Primitive p=((Primitive)types.get(name));
	  if (p==null) p=defaultType;
	  //if (p instanceof Picture) color=background;
	  p.setDrawParams(sparams);
	  p.draw(x,y,dx,dy,color,g);
	}

	protected void drawGrid(Graphics g) {
	    int px, py;
	    int pmaxx=(int)rToV(border[0][1],0);
	    int pmaxy=(int)rToV(border[1][1],1);
	    int pminx=(int)rToV(border[0][0],0);
	    int pminy=(int)rToV(border[1][0],1);
	    int pmaxy2=(int)rToV(border[1][1]-1,1);
	    if (pminy<0) pminy=(int)rToV(border[1][0]+1,1);
	    if (pminx<0) pminx=(int)rToV(border[0][0]+1,0);
	    //	    System.out.println(pminx+" "+pmaxx+" "+pminy+" "+pmaxy);
	    for (int x=(int)border[0][0];x<=(int)border[0][1];x+=1) {
	      px=(int)rToV(x,0);
	      //	      System.out.print(" px "+px);
	      g.setColor((x==0)?ccenter:cgrid);
	      g.drawLine(px,pminy,px,pmaxy);
	      g.setColor(ctext);
	      g.drawString(""+((showvirtual)?px:x),px,pmaxy2);
	    }
	    for (int y=(int)border[1][0];y<=(int)border[1][1];y+=1) {
	      py=(int)rToV(y,1);
	      //	      System.out.print(" py "+py);
	      g.setColor((y==0)?ccenter:cgrid);
	      g.drawLine(pminx,py,pmaxx,py);
	      g.setColor(ctext);
	      g.drawString(""+((showvirtual)?py:y),pminx,py);
	    }
	}


    public synchronized void paint(Graphics g) {
	boolean wasRepaintTrue=needrepaint;
      if (isVisible() && updateSize()) {
	Graphics old=g;
	g=memgc;
	int fontheight=getFont().getSize();
	if (needrecalc) {
	  calcfPoints();
	  needRepaint();
	}
	if (needrepaint) {
	  if (!colors) {
	      initColors();
	      initTypes();
	  }
	  long atime=System.currentTimeMillis();
	  g.setFont(Util.getCachedFont((dp[TEXT_SIZE]>0)?dp[TEXT_SIZE]:1));
	  g.setPaintMode();
	  needrepaint=false;
	  g.setColor(background);
	  g.fillRect(0,0,getSize().width,getSize().height);
	  Dimension d=getSize();
	  drawPrimitive("background",d.width/2,d.height/2,d.width,d.height,background,g);
	  int from, to; //, ffrom, fto;
	  float[] ffrom=null;
	  float[] fto=null;
	  float x,y;
	  Hashtable h=null;
	  Exit e=null;
	  Color c;
	  String value=null;
	  Node n;
	  int halo_dist=INFO_SIZE/2, halo_idx;
	  int[] halo_info={1,2,5,7};
	  Font exitFont=Util.getCachedFont((dp[EXITTEXT_SIZE]>0)?dp[EXITTEXT_SIZE]:1);
	  Font defaultFont=Util.getCachedFont((dp[TEXT_SIZE]>0)?dp[TEXT_SIZE]:1);

	  if (showgrid) {
	      drawGrid(g);
	  }


	  System.out.println("gridtime "+(System.currentTimeMillis()-atime));

	  atime=System.currentTimeMillis();
	  if (polygons!=null)
	      for (Enumeration en=polygons.elements();en.hasMoreElements();)
		  ((MPolygon)en.nextElement()).draw(g,level);
	  System.out.println("polytime "+(System.currentTimeMillis()-atime));

	  // alle sichtbaren punkte
	  for (int fc=0;fc<fcount;fc++) {
	    from=(int)fpoints[fc][3];
	    x=fpoints[fc][0];
	    y=fpoints[fc][1];
	    if (checkDimension(from,aktmap)) {
	    h=(Hashtable)special.get(new Integer(from));
	    if (h!=null) {
	      ffrom=fpoints[fc];
	      // alle ausgaenge
	      String exitString=null;
	      for (Enumeration en=h.keys();en.hasMoreElements();) {
		c=levelcolor;
		e=getExit((Exit)h.get(en.nextElement()));
		exitString=e.getExitString();
		// nicht auf ebene
		if (!(points[from][2]!=level && (e.map!=aktmap || points[e.to][2]!=level))) { // von ausserhalb des Levels liegenden Punkten nur die Wege in dieses Level malen
		  fto=null;
		  if (e.dir>0) {
		    to=e.to;
		    //		    if (checkDimension(to,e.map)) {
		    if (e.map==aktmap) {
		      if (points[to][4]>-1) {
			fto=fpoints[points[to][4]];
			if (points[to][2]>level||points[from][2]>level) c=upcolor;
			if (points[to][2]<level||points[from][2]<level) c=downcolor;
		      } 
		    } else {
		      // map switch
		      int[] changedir=directions[e.dir];
		      if (changedir[2]>0||points[from][2]>level) c=upcolor;
		      if (changedir[2]<0||points[from][2]<level) c=downcolor;
		      fto=new float[2];
		      for (int xy=0;xy<2;xy++)
			fto[xy]=rToV(points[from][xy]+changedir[xy],xy);
		      drawPrimitive("mapswitch",((ffrom[0]+fto[0])/2f),((ffrom[1]+fto[1])/2f),dp[MAPSWITCH_SIZE],dp[MAPSWITCH_SIZE],mapcolor,g);
		    }
		    if (fto!=null) {
		      if (e.dirname!=null) c=specialcolor;
		      if (e.color!=null) {
			  //			System.out.println(e+" Farbe "+e.color);
			  if (e.rcolor!=null)
			      e.rcolor=parseColor(e.color);
			  c=e.rcolor;
		      }
		      g.setColor(c);
		      if (!e.angle) {
			// nur entsprechend der dir malen
			g.setColor(invalidcolor);
			if (isOneWay(e)!=null)
			  gh.drawLine(g,ffrom[0]+(directions[e.dir][0]*dpt/2f),ffrom[1]-(directions[e.dir][1]*dpt/2f),fto[0]+(-directions[e.dir][0]*dpt/2f),fto[1]-(-directions[e.dir][1]*dpt/2f),dp[LINE_WIDTH/2],invalidcolor);
			else
			  gh.drawLine(g,ffrom[0]+(directions[e.dir][0]*dpt/2f),ffrom[1]-(directions[e.dir][1]*dpt/2f),fto[0],fto[1],dp[LINE_WIDTH/2],invalidcolor);
			g.setFont(exitFont);
			g.setColor(exitStringcolor);
			g.drawString(exitString+e.to,(int)(ffrom[0]+(directions[e.dir][0]*dpt/2f)),(int)(ffrom[1]-(directions[e.dir][1]*dpt/2f)));

			g.setColor(c);
			g.setFont(defaultFont);
			gh.drawLine(g,ffrom[0],ffrom[1],(ffrom[0]+(directions[e.dir][0]*dpt/2f)),(ffrom[1]-(directions[e.dir][1]*dpt/2f)),dp[LINE_WIDTH],c);

		      }
		      else {
			  gh.drawLine(g,ffrom[0],ffrom[1],(ffrom[0]+fto[0])/2f,(ffrom[1]+fto[1])/2f,dp[LINE_WIDTH],null);
			  if (exitString.length()>0) {
			    g.setColor(exitStringcolor);
			    g.setFont(exitFont);
			    g.drawString(exitString,(int)((ffrom[0]+fto[0])/2f),(int)((ffrom[1]+fto[1])/2f));
			    g.setColor(c);
			    g.setFont(defaultFont);
			  }
		      }
		    }
		  } else if (e.dir==0) {
		    // special exit
		    c=levelcolor;
		    if (e.dirname!=null) c=specialcolor;
		    g.setColor(c);
		    // ono
		    gh.drawLine(g,ffrom[0],ffrom[1],ffrom[0]-dp[7],ffrom[1]-dp[6]*2,dp[LINE_WIDTH],c);
		    drawPrimitive("mapswitch",(ffrom[0]-dp[1]-dp[7]),(ffrom[1]-dp[1]-dp[6]*2),dp[MAPSWITCH_SIZE],dp[MAPSWITCH_SIZE],(e.map!=aktmap)?mapcolor:levelcolor,g);
		  }
		}
	      }
	    }
	    }}
	  System.out.println("exitstime "+(System.currentTimeMillis()-atime));

	  int j=-1;
	  for (int fc=0;fc<fcount;fc++) {
	    int d_halo=halo_dist;

	    j=(int)fpoints[fc][3];
	    x=fpoints[fc][0];
	    y=fpoints[fc][1];
	    if ((points[j][3]&HOUSE_MASK)!=0) d_halo=halo_dist/2;	      	  
	    int[][] halo={
		  {-dp[d_halo]  , dp[3*d_halo]},  //ssw 0 (u)
		  {-dp[3*d_halo], dp[d_halo]},  //wsw 1 (house)
		  {-dp[3*d_halo],-dp[d_halo]}, //wnw 2 (portal/tport)
		  {-dp[d_halo]  ,-dp[3*d_halo]}, //nnw 3 (special exit)
		  
		  {dp[d_halo]  ,-dp[3*d_halo]},  //nno 4 (ob)
		  {dp[3*d_halo],-dp[d_halo]},  //ono 5 (tank,shop,pub)
		  {dp[3*d_halo], dp[d_halo]},   //oso 6 (nummer/name)
		  {dp[d_halo]  , dp[3*d_halo]}    //sso 7 (npc)
	      };

	      for (int i=0;i<halo.length;i++) {
		  halo[i][0]+=x;
		  halo[i][1]+=y;
	      }

	    int attrib_index=0;
	    String attrib=null;
	    if (checkDimension(j,aktmap)) {
	    c=((points[j][3]&VISITED_MASK)!=0)?visitedcolor:levelcolor;
	    g.setColor(c);
	    c=null;
	    if (j==aktpoint) c=aktcolor;
	    if (points[j][2]>level) c=upcolor;
	    if (points[j][2]<level) c=downcolor;
	    //	    if ((points[j][3]&HOUSE_MASK)!=0) c=housecolor;
	    if ((points[j][3]&OVERLAP_MASK)!=0) c=overlapcolor;
	    if ((points[j][3]&PARA_MASK)!=0) c=paracolor;
	    n=Mappanel.this.getNode(j);
	    if (n!=null) {
	      value=n.getAttribute("color");
	      if (value.length()>0) {
		Color cc=parseColor(value);
		if (cc!=null) c=cc;
	      }
	    }
	    if (points[j][2]>level) {
		drawPrimitive("up",x,y,dp[UP_SIZE],dp[UP_SIZE],c,g);
	    } else if (points[j][2]<level) {
		drawPrimitive("down",x,y,dp[DOWN_SIZE],dp[DOWN_SIZE],c,g);
	    }
	    else
	      {
		g.setColor(c);
		if (j==aktpoint)
		    drawPrimitive("akt",x,y,dp[AKT_SIZE],dp[AKT_SIZE],c,g);
		else {
		    value="visited";
		    if (n!=null) {
			value=n.getAttribute("icon");
			if (value.length()>0) {
			    Primitive p=(Primitive)icons.get(n);
			    if (p==null) {
				p=typeFactory(value,"Text ?");
				icons.put(n,p);
			    }
			    p.draw(x,y,dp[ICON_SIZE],dp[ICON_SIZE],c,g);
			    value=null;
			}
			else {
			    attrib=icon_attribs[attrib_index];
			    value="visited";
			    while (!n.hasAttrib(attrib)) {
				if (++attrib_index<icon_attribs.length)
				    attrib=icon_attribs[attrib_index];
				else break;
			    }
			    if (n.hasAttrib(attrib)) {
				if (attrib.equals("type"))
				    value=n.getValue(attrib);
				else
				value=attrib;
			    }
			}
			}
		    if (value!=null) {
			if ((points[j][3]&VISITED_MASK)!=0) 
			    drawPrimitive(value,x,y,dp[VISITED_SIZE],dp[VISITED_SIZE],c,g,""+j);
			else {
			    if (value.equals("visited")) value="unvisited";
			    drawPrimitive(value,x,y,dp[UNVISITED_SIZE],dp[UNVISITED_SIZE],c,g,""+j);
			}
		    }
		}
	      }
	    // oso 
	    if (points[j][2]==level) {
		g.setColor(nodecolor);		
		if (n==null) {
		  if (shownumbers) g.drawString(String.valueOf(j),(int)(x+dp[6]),(int)(y)+dp[8]);
		}
	        else {
		value=n.getAttribute("name");
		g.setColor(nodetextcolor);
		g.drawString(((value.length()>0)?(((shownumbers)?"("+j+") ":"")+value):((shownumbers)?""+j:"")),(int)(x+dp[6]),(int)(y)+dp[8]);

		// ono 5, sso 7, 1, 2
		g.setColor(infotextcolor);
		for (int i=attrib_index+1;i<icon_attribs.length;i++) {
		    attrib=icon_attribs[i];
		    halo_idx=halo_info[i%4];
		    if (n.hasAttrib(attrib)) {
			halo[halo_idx][0]=(int)drawInfo(n,attrib,g,halo[halo_idx][0],halo[halo_idx][1],dp[INFO_SIZE],dp[INFO_SIZE],c,halo_idx>3);
		    }
		}
	      }
	    }
	    if ((points[j][3]& (MARK_MASK | PRE_MARK_MASK))!=0) {
	      drawPrimitive("selected",x,y,dp[SELECTED_SIZE],dp[SELECTED_SIZE],selectedColor,g);
	    }
	    } else System.out.println(fpoints[fc][3]+" check failed");
	  }

	  if (numGetPoints>0 && get!=-1) {
	      int[][] sGetPoints=rToVi(getPoints);
	      for (int i=0;i<numGetPoints;i++) {
		  drawPrimitive("marker",sGetPoints[0][i],sGetPoints[1][i],dp[MARKER_SIZE],dp[MARKER_SIZE],markerColor,g);		  
	      }
	  }
	  System.out.println("PaintTime: "+(System.currentTimeMillis()-atime)+" ms");
	}
	if (!wasRepaintTrue) mouser.drawMarkRect(g);
	old.drawImage(mem,0,0,null);
	}
    }

	protected float drawInfo(Node n,String info,Graphics g,float x,float y,float dx,float dy,Color color,boolean addX) {
		if (n.getAttribute(info).length()>0) {
		    drawPrimitive(info,x,y,dx,dy,color,g);
		    if (addX) x+=dx; else x-=dx;
		}
		return x;
	}
    public void update(Graphics g) {
      paint(g);
    }
    public Dimension preferredSize() {
      return new Dimension(200,200);
    }
    protected boolean updateSize() {
      Dimension aktsize=getSize();
      if (aktsize.width==0 || aktsize.height==0) return false;
      if (dim==null || dim.width!=aktsize.width || dim.height!=aktsize.height) {
	dim=aktsize;
	if (memgc!=null) memgc.dispose();
	mem=createImage(aktsize.width,aktsize.height);
	memgc=mem.getGraphics();
	needRecalc(true);
	//	  repaint();
      }
      return true;
    }
  }
  Choice cb_maps, cb_hist;
  Button[] dbuttons=new Button[11];
  Scrollbar sb_vert, sb_hor;
  ActionTextArea smallinfo;
  TextArea status;

  Label statusLine;
  MTextField commandline;
    //  Label headline;
  GraphicsHelper gh=new GraphicsHelper();
  Image mem;
  Graphics memgc;
  Dimension dim;
  int numPoints, numExits, numMaps, aktmap;
  int loadmap=-1;
  boolean needrecalc=false,needrepaint=false;
    protected void wake() {
	if ((needrepaint || needrecalc) && paintThread!=null) {
	    //	    new Throwable().printStackTrace();
	    System.out.println("##### WAKE");
	    paintThread.wake(); 
	}
    }
    protected void needRepaint() {
	needRepaint(false);
    }
    protected void needRepaint(boolean wake) {
	needrepaint=true;
	System.out.print("|P");
	if (wake) wake();
    }

    protected void needRecalc() {
	needRecalc(false);
    }
    protected void needRecalc(boolean wake) {
	needrecalc=true;
	System.out.print("|R");
	if (wake) wake();
    }

  Hashtable special;
  String room=null;
  int aktpoint=0;
  int showpoint=0;
  int[][] points;  // [numPoints][xyz masks][fpoint reference]
  float[][] fpoints;
  int[][] exits;
  float border[][], maxborder[][]; // dimension, min/max
  int[] dp=new int[11], dp2=new int[11];
  float dpt=0;
  float[] zoom=new float[2];
  float delta=10;
  float[] dsize=new float[2];
  float[] painttrans=new float[2];
  float csize[]=new float[2];
  float min[];
  Mouser mouser=new Mouser();
  Typer typer=new Typer();
  boolean clearSequence=false;
  boolean dontadd=false, killlast=false;
  class Exit {
    int dir;
    int from;
    int to;
    int to2=-1;
    String command;
    String color;
    Color rcolor;
    String door;
    int flags;
    String dirname;
    int exitspos;
    int map;
    //    Exit other=null;
    boolean angle;
    public boolean updateAngle() {
      try {
	int uamap=(loadmap!=-1)?loadmap:aktmap;
      if (map!=uamap || dir==0 || dir==17 || dir==18) return true;
      int[][] uapoints=maps[uamap].points;
      int dx=uapoints[to][0]-uapoints[from][0];
      int dy=uapoints[to][1]-uapoints[from][1];
      if (dx==0 && dy==0) return true;
      else return checkAngle(dx,dy,dir);
      } catch(Exception e) {
	System.out.println("NullPointer  "+toString());
	return true;
      }
    }

    public String getExitString() {
      String res=(command==null)?"":"+";
      res+=((door==null)?"":door.substring(0,1));
      res+=((map!=aktmap)?maps[map].name+"-"+to:"");
      return res;
    }
    public String getLongExitString() {
      String res=((door==null)?"":door);
      res+=(command==null)?"":"["+command+"] ";
      res+=((map!=aktmap)?"m:"+map+",p:"+to:"");
      if (res.length()>0) res="("+res+")";
      return res;
    }
    public Exit getExit() {
      if (to2!=-1 && checkDimension(to2,map)) {
	int h=to; to=to2; to2=h;
	angle=updateAngle();
      }
      return this;
    }
    public void addSecond(int node) {
      if (to2==-1) to2=node;
    }
    public int getSecond() { return to2; }
    public String getDirName() {
      return (dirname==null)?getDirNameDir(dir):dirname;
    }
    public Exit(int exitspos, int dir, int afrom, int ato, int map, String dirname) {
      this.exitspos=exitspos;
      this.dir=dir;
      from=afrom; to=ato; this.dirname=dirname;
      this.map=map;
      angle=updateAngle();
    }
    
    public Exit(int exitspos, int dir, int afrom, int ato, int map) {
      this.exitspos=exitspos;
      this.dir=dir;
      from=afrom; to=ato; dirname=null;
      this.map=map;
      angle=updateAngle();
    }
    public Exit(int exitspos, String dir, int afrom, int ato, int map,String dirname) {
      this.exitspos=exitspos;
      this.dir=getDirectionId(dir);
      if (this.dir==-1) {this.dir=0;}
      this.dirname=dirname; from=afrom; to=ato;
      this.map=map;
      angle=updateAngle();
    }
    public Exit(int exitspos, String dir, int afrom, int ato, int map) {
      this.exitspos=exitspos;
      this.dir=getDirectionId(dir);
      if (this.dir==-1) {this.dir=0;this.dirname=dir;}
      else this.dirname=null; 
      from=afrom; to=ato; this.map=map;
	angle=updateAngle();
    }
    public String toString() {
	return i18n.trans("von Knoten $0 zu Knoten $1 in Map $2 Richtungsid $3 Richtungsname $4",new Object[]{new Integer(from),new Integer(to),maps[map],new Integer(dir), dirname});
    }
  }
  int fcount;
  String writepath;
  String path=null;
  String mapsdir=null;
  class PaintThread extends Thread {
      Component comp;
      boolean do_run=false;
      public PaintThread(Component c) {
	this.comp=c;
	do_run=true;
	setPriority(MIN_PRIORITY);
	this.start();
      }
      public void stopRunning() {
	  do_run=false;
      }
      public void startRunning() {
	  do_run=true;
	  this.start();
      }
    public void run() {
      try {
	while (do_run) {
	  if (needrepaint||needrecalc) {
	    comp.repaint();
	    System.out.print("+");
	  } else System.out.print(".");
	  synchronized(semaphore) {
	      semaphore.wait();
	  }
	  //	  sleep(100);
	}}
      catch (InterruptedException e) {}
    }
      public void wake() {
	  synchronized(semaphore) {
	      semaphore.notify();
	  }
      }
      Object semaphore=new Object();
  }
  int port=2000;
  String mapname=null;
  ColorChooser colorChooser=null;
  IconChooser iconChooser=null;
  public static I18N i18n=null;
    protected String getPropIfNull(String value, String name) {
	if (value!=null) return value;
	return props.getProperty(name);
    }

    public void setTitle(String title) {    
	Container c=getParent();
	while (c!=null && ! (c instanceof Frame)) {
	    c=c.getParent();
	}
	if (c!=null) 
	    ((Frame)c).setTitle("DER MAPPER: "+mapname+": "+title);
    }

    public Mappanel(URL codebase) {
	super();
	isApplet=true;
	this.codebase=codebase;
    }

    public Mappanel() {
	super();
    }

    protected void base_init(String base, String mapname, String lang, String port) throws Exception {
	loadDefaultOptions(base,"mapper.def");
	loadDefaultOptions(base,"mapper.ini");

	base=getPropIfNull(base,"url");
	mapname=getPropIfNull(mapname,"map");
	lang=getPropIfNull(lang,"lang");
	port=getPropIfNull(port,"port");

	System.out.println("base "+base);
	System.out.println("map "+mapname);
	System.out.println("lang "+lang);
	System.out.println("port "+port);

	if (base!=null) {
	    path=base;
	    if (!path.endsWith("/")) path+="/";
	    mapsdir=path;
	    if (mapname!=null) path+=mapname;
	    if (!path.endsWith("/")) path+="/";
	}

	try {
	    this.port=(new Integer(port)).intValue();
	} catch(Exception ex) {}

	this.mapname=mapname;

	URL url=new URL(path);
	writepath=url.getFile();
	if (lang!=null) 
	    if (isApplet) 
		i18n=new I18N(lang,codebase);
	    else
		i18n=new I18N(lang);

	if (lang.equals("en")) this.lang=EN;
	switch (this.lang) {
	case DE: {
	    dir_name_lookup=dir_name_lookup_de;
	    dir_long_lookup=dir_long_lookup_de;
	    break;
	}
	case EN: {
	    dir_name_lookup=dir_name_lookup_en;
	    dir_long_lookup=dir_long_lookup_en;
	}
	}
    }

    protected void initUI() {
	setLayout(new BorderLayout());

	add("North",getButtonBar());

	mapcanvas=new MapCanvas();
	mapcanvas.requestFocus();

	add("Center",mapcanvas);

	smallinfo=new ActionTextArea("",20,20,TextArea.SCROLLBARS_VERTICAL_ONLY);
	smallinfo.addActionListener(commandAL);
    
	add("East",smallinfo);

	Panel interactivePanel=new Panel();
	interactivePanel.setLayout(new BorderLayout());

    Panel modeCompass=new Panel();
    modeCompass.setLayout(new BorderLayout());

    Panel compass=new Panel();
    modeCompass.add("Center",compass);
    if (props.getProperty("gui_mode","button").equals("button")) {

    modeButton=new Button(I18N(buttonModeNames[buttonMode]));
    modeButton.addActionListener(modeButtonListener);

    modeCompass.add("North",modeButton);

    interactivePanel.add("West",modeCompass);
    compass.setLayout(new GridLayout(3,3));
    int count=7;
    for (int i=8;i>=0;i--) {
	if (i!=4) {
	    dbuttons[i]=new Button(dir_name_lookup[dir_lookup[count]]);
	    dbuttons[i].addActionListener(directionButtonListener);
	} else {
	    dbuttons[i]=new Button("c");
	    dbuttons[i].addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    evalCommand("center");
		}
	    });
	}
	compass.add(dbuttons[i]);
	if (count % 3 == 0 ) count-=5; else ++count;
    }
    } else {
    iconModeButton=new IconButton(props.getProperty("mode_icon_"+buttonModes[buttonMode]));
    
    iconModeButton.addActionListener(modeIconButtonListener);
    iconModeButton.setTooltip(I18N(buttonModeNames[buttonMode]));

    modeCompass.add("North",iconModeButton);
    interactivePanel.add("West",modeCompass);
    compass.setLayout(new BorderLayout());
    MapButton mb=new MapButton(props.getProperty("compass_image","compass.gif")) {
	public Dimension getPreferredSize() {
	    return new Dimension(60,60);
	}
    };

    compass.add("Center",mb);
    /*
    int count=7;
    Square rect;
    int x1=0,y1=0;
    for (int i=8;i>=0;i--) {
	x1=(2-(i%3))*33;
	y1=(2-i/3)*33;
	rect=new Square(x1,y1,x1+33,y1+33);
	if (i!=4) {
	    mb.setActionListener(rect,directionButtonListener,dir_name_lookup[dir_lookup[count]]);
	} else {
	    mb.setActionListener(rect,
				 new ActionListener() {
				     public void actionPerformed(ActionEvent e) {
					 evalCommand("center");
				     }
				 },
				 dir_name_lookup[dir_lookup[count]]);
	}
	if (count % 3 == 0 ) count-=5; else ++count;
    }
    */
    Pie pie;
    float angle=225f; //sw
    float delta;
    for (int i=1;i<=16;i++) {
	System.out.print(dir_name_lookup[i]+": ");
	delta=(i%2==1)?15f:7.5f;
	pie=new Pie(20,100,(int)(angle-delta),(int)(angle+delta));
	mb.setActionListener(pie,directionButtonListener,dir_name_lookup[i]);

	angle+=22.5f;
    }
	    mb.setActionListener(new Pie(0,20,0,359),
				 new ActionListener() {
				     public void actionPerformed(ActionEvent e) {
					 evalCommand("center");
				     }
				 },"c");

    }
    Panel commandPanel=new Panel();
    commandPanel.setLayout(new BorderLayout());

    commandline=new MTextField();
    commandline.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	  evalCommand(commandline.getText());
      }
    });
    commandline.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
	if (e.getKeyCode()==KeyEvent.VK_UP) getHistory(true);
	else if (e.getKeyCode()==KeyEvent.VK_DOWN) getHistory(false);
      }
    });

    Panel pCommand=new Panel() {
	public Insets insets() {
	    return new Insets(0,0,0,0);
	}
    };
    pCommand.setLayout(new BorderLayout());
    pCommand.add("Center",commandline);
    Panel pCommandButtons=new Panel();	
    pCommandButtons.setLayout(new FlowLayout(FlowLayout.LEFT));

    IconButton colorButton=new IconButton(props.getProperty("color_chooser","paint2.gif"));
    colorButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    chooseColor(commandline);
	}
    });
    colorButton.setTooltip(I18N("Farbauswahl"));
    pCommandButtons.add(colorButton);
    IconButton iconButton=new IconButton(props.getProperty("icon_chooser","painting.gif"));
    iconButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    chooseIcon(commandline);
	}
    });
    iconButton.setTooltip(I18N("Iconauswahl"));
    pCommandButtons.add(iconButton);

    cb_hist=new Choice() {
	public Dimension getPreferredSize() {
	    return new Dimension(50,20);
	}
    };
    cb_hist.addItemListener(new ItemListener() {
	public void itemStateChanged(ItemEvent e) {
	    if (e.getStateChange()==e.SELECTED) {
		histoffset=cb_hist.getSelectedIndex();
		commandline.setText((String)history.get(histoffset));
		commandline.requestFocus();
	    }
	}
    });
    saveHistory("sm 0");
    pCommandButtons.add(cb_hist);

    pCommand.add("East",pCommandButtons);
    commandPanel.add("North",pCommand);

    status=new TextArea("",3,30,TextArea.SCROLLBARS_VERTICAL_ONLY);
    status.setEditable(false);
    commandPanel.add("Center",status);
    
    interactivePanel.add("Center",commandPanel);

    statusLine=new Label();
    statusHelp(I18N("Willkommen"));
    interactivePanel.add("South",statusLine);


    add("South",interactivePanel);

    addKeyListener(typer);
  }
  
  public Panel getButtonBar() {
      Panel bb=new Panel()  {
	public Insets insets() {
	    return new Insets(0, 0, 0, 0);
	}
      };
      FlowLayout fl=new FlowLayout(FlowLayout.LEFT);
      fl.setVgap(0);
      bb.setLayout(fl);
      String name;
      int max=-1;
      int akt;
      for (Enumeration en=props.keys();en.hasMoreElements();) {
	  name=(String)en.nextElement();
	  if (name.startsWith("button_bar_")) {
	      akt=new Integer(name.substring("button_bar_".length())).intValue();
	      if (akt>max) max=akt;
	  }
      }
      if (max==-1) return bb;
      String value;
      StringTokenizer st;
      IconButton ib;
      for (akt=0;akt<=max;akt++) {
	  value=props.getProperty("button_bar_"+akt);
	  if (value!=null) {
	      st=new StringTokenizer(value);
	      ib=new IconButton(st.nextToken());
	      String cmd=Util.allTokens(st);
	      int off=cmd.indexOf('?');
	      if (off>-1) {
		  ib.setTooltip(cmd.substring(off+1));
		  cmd=cmd.substring(0,off);
	      }
	      off=cmd.indexOf(':');
	      if (off>-1) {
		  ib.setActionCommand(cmd.substring(0,off));
		  ib.setActionCommand2(cmd.substring(off+1));
	      } else ib.setActionCommand(cmd);
	      ib.addActionListener(commandAL);
	      bb.add(ib);
	  }
      }
      return bb;
  }

  protected void performButtonAction(String dir) {
      String aktMode=buttonModes[buttonMode];
      if (aktMode.equals("walk")) {
	  evalCommand(dir);
      } else
      if (aktMode.equals("pan")) {
	  evalCommand("mv "+dir);
      } else
      if (aktMode.equals("draw")) {
	  evalCommand("ae "+dir);
      } else
      if (aktMode.equals("drawwalk")) {
	  evalCommand("ae "+dir);
	  evalCommand(dir);
      } else
      if (aktMode.equals("delete")) {
	  evalCommand("DE "+dir);
      }
  }
  int buttonMode=0;

  ActionListener directionButtonListener=new ActionListener() {
    public void actionPerformed(ActionEvent e) {
	//	performButtonAction(((Button)e.getSource()).getLabel());
	performButtonAction(e.getActionCommand());
    }
  };
  
  ActionListener modeButtonListener=new ActionListener() {
    public void actionPerformed(ActionEvent e) {
	System.out.println("modeButtonListener "+e.paramString());
	buttonMode=++buttonMode % buttonModes.length;
	modeButton.setLabel(I18N(buttonModeNames[buttonMode]));
    }
  };

  ActionListener modeIconButtonListener=new ActionListener() {
    public void actionPerformed(ActionEvent e) {
	buttonMode=++buttonMode % buttonModes.length;
	iconModeButton.setImage(props.getProperty("mode_icon_"+buttonModes[buttonMode]));
	iconModeButton.setTooltip(I18N(buttonModeNames[buttonMode]));
	statusHelp(I18N(buttonModeNames[buttonMode]));
    }
  };

    String[] buttonModes={"walk","pan","draw","drawwalk","delete"};
    String[] buttonModeNames={"bewegen","verschieben","zeichnen","zeichnen&bewegen","löschen"};

  Button modeButton=null;
  IconButton iconModeButton=null;
  final static int DE=0, EN=1;
  boolean shownumbers=true;;
  boolean showgrid=false;
  boolean showvirtual=false;
  int lang=DE;

    public void chooseColor(TextComponent tf) {
	if (colorChooser==null) {
	    colorChooser=new ColorChooser();
	    colorChooser.pack();
	}
	    colorChooser.setTextComponent(tf);
	    colorChooser.show();
    }

    public void chooseIcon(TextComponent tf) {
	if (iconChooser==null) {
	    iconChooser=new IconChooser();
	    iconChooser.pack();
	}
	iconChooser.setTextComponent(tf);
	iconChooser.show();
    }
    public void initIconAttribs() {
	String s=props.getProperty("icon_attribs");
	if (s!=null) {
	    StringTokenizer st=new StringTokenizer(s);
	    icon_attribs=new String[st.countTokens()];
	    for (int i=0;i<icon_attribs.length;i++) {
		icon_attribs[i]=st.nextToken();
	    }
	}
    }
  public void init(String base, String mapname, String lang, String port) throws Exception {
    base_init(base,mapname,lang,port);
    showStatus(i18n.trans("Lesepräfix $0\nSchreibpräfix $1",new Object[]{path,writepath}));
    initDirections();
    initInteractive();
    initIconAttribs();
    loadOptions();
    loadInfo();
    loadColors();
    loadObjects();

    initUI();

    readMaps();
    switchMap(0);
    aktpoint=0;
    showMovedPoint();
    if (!isApplet) start();
  }

    Thread my_thread=null;
    PaintThread paintThread=null;
    boolean run_thread=false;
    public void start() {
	if (my_thread==null) {
	    my_thread=new Thread(this);
	    run_thread=true;
	    my_thread.start();
	}
	if (paintThread==null) {
	    paintThread=new PaintThread(mapcanvas);
	} else paintThread.startRunning();
    }
    public void stop() {
	run_thread=false;
	my_thread=null;
	if (paintThread!=null) {
	    paintThread.stopRunning();
	    paintThread=null;
	}
    }
    public void destroy() {
	run_thread=false;
	my_thread=null;
	if (paintThread!=null) {
	    paintThread.stopRunning();
	    paintThread=null;
	}
	removeAll();
    }

    Vector history=new Vector();
    int histoffset=-1;
    public void getHistory(boolean up) {
      if (up) histoffset--;
      else histoffset++;
      if (histoffset>=history.size()) histoffset=history.size()-1;
      if (histoffset<0) histoffset=0;
      cb_hist.select(histoffset);
      commandline.setText((String)history.get(histoffset));
    }

    public void saveHistory(String s) {
      if (history.contains(s)) cb_hist.remove(s);
      if (!history.remove(s) && history.size()>50) {
	  history.removeElementAt(0);
	  cb_hist.remove(0);
      }
      history.addElement(s);
      cb_hist.add(s);
      histoffset=history.size();
      cb_hist.select(history.size()-1);
    }

  public String getDirNameDir(int dir) {
    if (dir>0 && dir<dir_name_lookup.length) 
      return dir_name_lookup[dir];
    else return null;
  }
  Vector undo = new Vector();
  boolean noundo=false;
  public void undo() {
    if (!undo.isEmpty()) {
      noundo=false;
      Vector cmds=(Vector)undo.lastElement();
      undo.removeElementAt(undo.size()-1);
      showStatus(i18n.trans("Rücknahme von Befehl")+": "+cmds.get(0));
      noundo=true;
      for (int i=cmds.size()-1;i>0;i--)
	evalCommand((String)cmds.get(i));
      noundo=false;
    }
  }
  public void addUndo(Vector undo, String s) {
    //    if (!noundo) undo.addElement(s);
  }
  public void saveUndo(Vector cmds) {
    if (!noundo && cmds.size()>1) {
      if (undo.size()>50) undo.removeElementAt(0);
      undo.addElement(cmds);
      for (int i=0;i<cmds.size();i++)
	System.out.println("saveUndo "+i+": "+cmds.get(i));
    }
  }
  DataOutputStream socket_out=null;
  public void send_socket(String s) {
    try {
      if (socket_out!=null) socket_out.writeBytes(s+"\n");
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  boolean from_socket=false;
  public void run()  {
    ServerSocket server=null;
    Socket client=null;
    String s=null;
    DataInputStream r=null;
    try {
    server=new ServerSocket(port);
      while (run_thread) {
	try {
	client=server.accept();
	System.out.println("Client accepted"+client);
	System.out.flush();
	r=new DataInputStream(client.getInputStream());
	s=null;
	socket_out=new DataOutputStream(client.getOutputStream());
	while ((s=r.readLine())!=null) {
	  System.out.println("Client: "+s);
	  if (s!=null) {
	    from_socket=true;
	    evalCommand(s);
	    from_socket=false;
	  }
	}
	client.close();
	} catch(Exception e) {e.printStackTrace();
	}
      }
    } catch(IOException e) {
	e.printStackTrace();
    }
    finally {
      try {
      r.close();
      client.close();
      server.close();
      } catch(Exception e) {
	e.printStackTrace();
      }
    }
  }
  protected void initDirections() {
    name_dir_lookup=new HashMap();
    Integer in=null;
    for (int i=0;i<dir_name_lookup.length;i++) {
      in=new Integer(i);
      name_dir_lookup.put(dir_name_lookup[i],in);
      name_dir_lookup.put(dir_long_lookup[i],in);
    }
    flags=new HashMap();
    flags.put("mark",new Integer(1));
    flags.put("overlap",new Integer(8));
    flags.put("visited",new Integer(32));
    flags.put("building",new Integer(64));
    flags.put("para",new Integer(256));
    flags.put("nopara",new Integer(512));
    calcDefaultAngles();
  }

  class Typer extends KeyAdapter {
    KeyEvent lastpressed, lasttyped;
    public void keyPressed(KeyEvent e) {
      System.out.println(e.getKeyChar() +" "+KeyEvent.getKeyText(e.getKeyCode()));
      lastpressed=e;
      if ((e.getKeyCode()==e.VK_DELETE) || (e.getKeyCode()==e.VK_BACK_SPACE) ) {
	dontadd=true;
	String s=commandline.getText();
	if (s.length()>0)
	    commandline.setText(s.substring(0,s.length()-1));
      }
      if (e.getKeyCode()==e.VK_ENTER) {
	evalSequence();
	dontadd=true;
      }
    }
    public void keyTyped(KeyEvent e) {
      lasttyped=e;
      testDirection(e.getKeyChar());
      evalTyped(lastpressed.getModifiers(),e.getKeyChar(),lastpressed.getKeyCode());
      if (!dontadd && (lastpressed.getModifiers()==0 || (lastpressed.getModifiers() == e.SHIFT_MASK))) {
	if (e.getKeyChar()>=' ') {
	    commandline.setText(commandline.getText()+e.getKeyChar());
	    //	    needRepaint();
	}
      }
      dontadd=false;
      ((Component)e.getSource()).requestFocus();
    }
    public void evalSequence() {
      evalCommand(commandline.getText());
    }
    public void evalTyped(int mod, char key,int code) {
      Vector undo=null;
      if (!noundo) undo=new Vector();
      System.out.println(":typed "+KeyEvent.getKeyModifiersText(mod)+" code: "+KeyEvent.getKeyText(code)+" char "+key);
      if (mod == KeyEvent.SHIFT_MASK) {
	if (key=='U') {
	  addUndo(undo,"unmark all");
	  StringBuffer sb=new StringBuffer();
	  for (int i=numPoints-1;i>=0;i--) {
	    if ((points[i][3]&MARK_MASK)!=0) sb.append(i+" ");
	    points[i][3]&=~MARK_MASK;
	  }
	  if (sb.length()>0) addUndo(undo,"mark nodes "+sb.toString());
	  showStatus(I18N("Markierung: alle Markierungen entfernt"));
	  needRepaint();
	}
      } else
      if ((mod & KeyEvent.CTRL_MASK) !=0) {
	switch (code) {
	case KeyEvent.VK_ADD: 
	  addUndo(undo,"moveview "+getDirNameDir(17));
	  moveView("17",undo); // oben
	  break;
	case KeyEvent.VK_SUBTRACT:
	  addUndo(undo,"moveview "+getDirNameDir(18));
	  moveView("18",undo); // unten
	  break;
	case KeyEvent.VK_Z:
	  addUndo(undo,"zoom 1");
	  zoom(ZOOM_COUNT,1,undo); break;
	case KeyEvent.VK_U:
	  addUndo(undo,"unzoom 1");
	  zoom(UNZOOM_COUNT,1,undo);
	  break;
	case KeyEvent.VK_C: 
	case KeyEvent.VK_M:
	  {
	    addUndo(undo,"center");
	    center(null,undo);
	  break;
	  }}}
      else {
	if (mod == (KeyEvent.ALT_MASK | KeyEvent.META_MASK) ||
	    mod == KeyEvent.ALT_MASK) {
	  if (key=='#') {
	    oneway=true;
	    showStatus(I18N("Erzeugen: naechstes Erzeugen nur Hinrichtung"));
	    System.out.println("oneway true");
	  } else
	  if (key=='$') {
	    forcenode=true;
	    showStatus(I18N("Erzeugen: naechstes Erzeugen eines Knoten erzeugt bei schon vorhandenen Knoten einen zusätzlichen"));
	    System.out.println("forcenode true");
	  }
	} else
	  if (needrecalc) { 
	      dontadd=true;
	  } 
      }
      if (!noundo) saveUndo(undo);
      wake();
    }

    public void testDirection(char d) {
      int dir=0;
      Vector undo=null;
      if (!noundo) undo=new Vector();
      dir=Character.getNumericValue(d);
      if (dir<10 && dir>0|| d=='+' || d=='-') {
	if (d=='+') dir=10;
	if (d=='-') dir=11;
	dir=dir_lookup[dir];
	if (lastpressed.getModifiers() == KeyEvent.CTRL_MASK) {
	  if (dir>-1 && dir<directions.length) {
	    addUndo(undo,getDirNameDir(dir));
	    moveView(getDirNameDir(dir),undo);
	    dontadd=true;
	  }}
	else
	  if (lastpressed.getModifiers() == (KeyEvent.ALT_MASK | KeyEvent.META_MASK) || lastpressed.getModifiers() == KeyEvent.ALT_MASK) {
	    if (dir>-1 && dir<dir_name_lookup.length) {      
	      undo.addElement("add exit and move "+getDirNameDir(dir));
	      addPoint(aktpoint,-1,aktmap,dir,null,false,forcenode,1,false,undo);
	      movePoint(getDirNameDir(dir),undo);
	      System.out.println("xxdir "+dir+" "+getDirNameDir(dir));
	      dontadd=true;
	    }}
	  else
	    if (dir>-1 && dir<dir_name_lookup.length) {
	      addUndo(undo,getDirNameDir(dir));
	      movePoint(getDirNameDir(dir),undo);
	      dontadd=true;
	    }
      }
      saveUndo(undo);
    }
  }
    final static int ZOOM_COUNT=1, UNZOOM_COUNT=2, ZOOM_MAP=3, ZOOM_LEVEL=4, ZOOM_NODES=5;

    public void zoom(int mode, int count, Vector undo) {
      //      addUndo(undo,((!zoom)?"zoom ":"unzoom ")+count);
      int ldelta=-1;
      if (count<0) {
	mode=UNZOOM_COUNT;
	count=-count;
      }
      switch(mode) {
	case ZOOM_COUNT: {
	  for (int i=0; i<count ;i++) {
	    delta=delta/1.5f;
	  }
	  needRecalc(); 
	  showStatus("View: Zoom");
	  break;
	}
      case UNZOOM_COUNT: {
	  for (int i=0; i<count ;i++) {
	    delta=delta*1.5f;
	  }
	needRecalc();
	showStatus("View: UnZoom");
	break;
      }
      case ZOOM_LEVEL: {
	int[][] lborder=new int[2][2];
	for (int i=0;i<numPoints;i++) {
	  if (points[i][2]==level && points[i][4]!=-2) {
	    System.out.print(i+", ");
	    for (int xy=0;xy<2;xy++) {
		lborder[xy][0]=Math.min(points[i][xy],lborder[xy][0]);
		lborder[xy][1]=Math.max(points[i][xy],lborder[xy][1]);
	    }
	  }
	}
	ldelta=Math.max(lborder[0][1]-lborder[0][0],lborder[1][1]-lborder[1][0]);
	min[0]=(lborder[0][1]+lborder[0][0])/2;
	min[1]=(lborder[1][1]+lborder[1][0])/2;
	System.out.println("ZoomLBorder X:"+lborder[0][0]+" < "+lborder[0][1]+" und Y:"+lborder[1][0]+" < "+lborder[1][1]+" maxdelta "+ldelta);
	showStatus("View: Zoom: Level");
	break;
      }
      case ZOOM_MAP: {
	ldelta=(int)Math.max(maxborder[0][1]-maxborder[0][0],maxborder[1][1]-maxborder[1][0]);
	min[0]=(maxborder[0][1]+maxborder[0][0])/2;
	min[1]=(maxborder[1][1]+maxborder[1][0])/2;
	showStatus("View: Zoom: Map");
	break;
      }
      case ZOOM_NODES: {
	ldelta=count*2;
	min[0]=points[aktpoint][0];
	min[1]=points[aktpoint][1];
	showStatus("View: Zoom: "+count+" "+I18N("Knoten sichtbar"));
	break;
      }
      }      
      if (ldelta!=-1) {
	delta=ldelta+1;
	needRecalc();
      } 
    }
  public double sgn(float x) {
    return (x>0)?1:-1;
  }
  int[] angles=null;

  public void calcDefaultAngles() {
    angles=new int[directions.length];
    for (int i=0;i<directions.length;i++) 
      angles[i]=(int)Math.rint(calcAngle(directions[i][0], directions[i][1]));
  }

  public int calcAngle(float dx, float dy) {
    if (dx==0 && dy==0) return -1;
    double angle=0;
    // -PI/2 bis +PI/2
    if (dx!=0) {
      angle=Math.atan(dy/dx);
      //      angle*=-sgn(dx);
    } else
      angle=Math.PI/2*sgn(dy);
    if (dx<0) angle+=Math.PI;
    if (angle<0) angle=2*Math.PI+angle;
    angle=(angle/Math.PI)*180;
    return (int)Math.rint(angle);
  }
  public boolean checkAngle(float dx, float dy, int dir) {
    boolean res=false;
    int angle=-1;
    if (dir==0 || dir==17 || dir==18 || dx==0 && dy==0) res=true;
    else {
      angle=calcAngle(dx,dy);
      int cmp=20;
      if (angles[dir] % 90 ==0) cmp/=4;
      if (Math.abs(angle-angles[dir])<cmp) res=true;
    }
    //    System.out.println("checkAngle "+dy+"/"+dx+" winkel "+angle+" richtung "+getDirNameDir(dir)+" check "+res);
    return res;
  }
  public float parseFloat(String s,float error) {
    try {
      return Float.valueOf(s).floatValue();
    } catch(Exception e) {}
    return error;
  }
    public void center(String param, Vector undo) {
      addUndo(undo,"center view "+min[0]+" "+min[1]+" "+level);
      if (param==null || param.length()==0) {
	min[0]=points[aktpoint][0];
	min[1]=points[aktpoint][1];
	level=points[aktpoint][2];
	  param=I18N("aktuellen Punkt");
      } else {
	StringTokenizer st=new StringTokenizer(param);
	String dev=st.nextToken();
	if ("screen".startsWith(dev)) {
	  if (st.hasMoreTokens()) min[0]=vToR(parseFloat(st.nextToken(),rToV(min[0],0)),0);
	  if (st.hasMoreTokens()) min[1]=vToR(parseFloat(st.nextToken(),rToV(min[1],1)),1);
	  if (st.hasMoreTokens()) level=Integer.parseInt(st.nextToken());
	} else
	if ("view".startsWith(dev)) {
	  if (st.hasMoreTokens()) min[0]=parseFloat(st.nextToken(),min[0]);
	  if (st.hasMoreTokens()) min[1]=parseFloat(st.nextToken(),min[1]);
	  if (st.hasMoreTokens()) level=Integer.parseInt(st.nextToken());
	} else {
	  if ("node".startsWith(dev)) {
	    if (st.hasMoreTokens())
	      dev=st.nextToken();
	  }
	  int[] pt=getAllPointId(aktmap,dev);
	  if (pt[0]!=-1) {
	    if (pt[1]!=aktmap) switchMap(pt[1]);
	    min[0]=points[pt[0]][0];
	    min[1]=points[pt[0]][1];
	    level=points[pt[0]][2];
	  }
	}
      }
      needRecalc();
      showStatus(I18N("View: Zentrieren um")+" "+param);
    }
  public void moveView(String dir, Vector undo) {
    int off=dir.indexOf('*');
    int count=1;
    if (off!=-1) {
      count=Integer.parseInt(dir.substring(0,off));
      dir=dir.substring(off+1);
    }
    int idir=getDirectionId(dir);
    System.out.println("moveView "+dir+">"+idir);
    if (idir>0) {
      int[] move=directions[idir];
      addUndo(undo,"center view "+min[0]+" "+min[1]+" "+level);
      level+=move[2]*count;
      min[0]+=move[0]*dsize[0]*0.4f*count;
      min[1]+=move[1]*dsize[1]*0.4f*count;
      //      if (!noundo) {
	String status="View : ";
	status+=I18N("Mittelpunkt geändert Richtung ");
	if (count>1) status+=" "+count+"*";
	status+=getDirNameDir(idir);
	status+=" "+I18N("neuer Mittelpunkt");
	status+=" ("+min[0]+", "+min[1]+")";
	if (move[2]!=0) {
	  if (level>maxborder[2][1]||level<maxborder[2][0]) {
	    status+="\n";
	      status+=" "+I18N("keine Knoten auf neuer Ebene")+" ";
	  }}
      showStatus(status);
      //    }
      needRecalc();
    }
  }
  boolean para=false;
  public String getPointString(int map, int node) {
    StringBuffer sb=new StringBuffer();
    Node n=null;
    if (map!=aktmap) {
      checkMap(map);
      sb.append(maps[map].toString()+" ");
      n=(Node)maps[map].info.get(new Integer(node));
    } else n=(Node)info.get(new Integer(node));
    if (n!=null &&!n.getAttribute("name").equals("")) sb.append(n.getAttribute("name")+"("+node+")"); else sb.append(""+node);
    return sb.toString();
  }

  public boolean checkDimension(int node, int map) {
    if (map==aktmap) 
	return checkDimension(((points[node][3] & PARA_MASK)!=0), ((points[node][3] & NOPARA_MASK)!=0));    
    else return checkDimension(((maps[map].points[node][3] & PARA_MASK)!=0), ((maps[map].points[node][3] & NOPARA_MASK)!=0));    
  }

  public boolean checkDimension(boolean p, boolean np) {
    // beide nicht gesetzt oder beide gesetzt - ueberall sichtbar
    // wenn para gesetzt in para sichtbar
    
    if (para && (p || !np)) return true;
    if (!para && (np || !p)) return true;
    return false;
  }
  public void saveOptions() {
      if (isApplet) return;
    try {
      Writer fw=new BufferedWriter(new FileWriter(writepath+"map.opts"));
      Object key;
      for (Enumeration e=opt.keys();e.hasMoreElements();) {
	key=e.nextElement();	fw.write(key+" "+opt.get(key)+"\n");
      }
      fw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
    public URL getResourceURL(String baseURL, String resfile) {
	resfile=resfile.trim();
	URL u=null;
	try {
	    if (baseURL==null) {
		if (isApplet) {
		    u=new URL(codebase,resfile);
		} else {
		    u=getClass().getClassLoader().getSystemResource(resfile);
		    //		    System.out.println("getResourceURL1: "+u+" file #"+resfile+"# class "+getClass().toString());
		    if (u==null) {
			u=ClassLoader.getSystemClassLoader().getResource(resfile);
			//			System.out.println("getResourceURL2: "+u+" file "+resfile);
		    }
		}
	    } else {
		if (!baseURL.endsWith("/")) baseURL+="/";
		u=new URL(baseURL);
		u=new URL(u,resfile);
	    }
	} catch(Exception e) {
	    System.out.println("Resource URL for "+resfile+" not found: "+e.getMessage());
	    //	    e.printStackTrace();
	}
	return u;
    }

    protected void loadDefaultOptions(String base,String file) {
	try {
	    InputStream is=null;
	    URL u=null;
	    System.out.println("loadDefaultOptions "+base);
	    if (base==null) {
		if (isApplet) {
		    u=new URL(codebase,file);
		} else
		    u=getClass().getClassLoader().getSystemResource(file);
	    } else {
		if (!base.endsWith("/")) base+="/";
		u=new URL(base);
		u=new URL(u,file);
	    }
		is=new BufferedInputStream(u.openStream());
		props.load(is);
		System.out.println(props);
	} catch(Exception e) {
	    System.out.println("Property File: "+file+" not found: "+e.getMessage());
	}
    }

    public void listOptions() {
	  String option=null;
	  Object value;
	  smallinfo.clear();
	  smallinfo.append("Options:\n");
	  for (Enumeration en=opt.keys();en.hasMoreElements();) {
	    option=(String)en.nextElement();
	    value=opt.get(option);
	    smallinfo.append(option+": "+value+"\n","opt "+option+" <"+value+">");
	  }
	  smallinfo.setText();
    }

    public void loadOptions() {
	try {
      BufferedReader fr=new BufferedReader(new InputStreamReader(new URL(path+"map.opts").openStream()));
      String res=null;
      StringTokenizer st=null;
      while ((res=fr.readLine())!=null) {
	st=new StringTokenizer(res);
	try {
	    opt.put(st.nextToken(),Util.allTokens(st));
	} catch(Exception e) {}
      }
      fr.close();
      System.out.println(opt);
      shownumbers=getOptFlag("numbers");
      showgrid=getOptFlag("grid");
      showvirtual=getOptFlag("vgrid");
      para=getOptFlag("para");

      setOptionMenuStates();

      MAXLEVEL=Integer.parseInt((String)opt.get("level"));
    } catch(Exception e) {
	//      e.printStackTrace();
    }
  }
  boolean getOptFlag(String optname) {
    Object o=opt.get(optname);
    if (o!=null) return o.equals("1");
    opt.put(optname,"0");
    return false;
  }
  public void  redirectConnection(int omap, int opoint,int nmap,int npoint, Hashtable dirs) {
    boolean all=(dirs==null || dirs.size()==0);
    readAllMaps();
    System.out.println("redirectConnection dirs"+dirs);
    // Integer fuer Punkt in alter Map und Punkt in neuer Map
    Integer P=new Integer(opoint);
    Integer DP=new Integer(npoint);
    // Hashtable fuer Rueckrichtungen
    Hashtable adirs=new HashMap();
    String dirname;
    int dir, adir;
    // Abkuerzung fuer Referenzen
    Map NMap=maps[nmap], OMap=maps[omap];
    if (!all)
      // gegenrichtungen suchen & sammeln
    for (Enumeration en=dirs.keys();en.hasMoreElements();) {
      dirname=(String)en.nextElement();
      dir=getDirectionId(dirname);
      if (dir>0) {
	adirs.put(getDirNameDir(dir_anti_lookup[dir]),dummy);
      }
    }
    System.out.println(adirs+"\n"+adirs); 
    Hashtable h;
    Exit e;
    // in allen Maps suchen
    for (int i=numMaps-1;i>=0;i--)
      // fuer alle Knoten die Richtungen abtesten
      for (Enumeration en2=maps[i].special.keys();en2.hasMoreElements();) {
	h=(Hashtable)maps[i].special.get(en2.nextElement());
	// fuer alle Richtungen pro Knoten
	for (Enumeration en3=h.keys();en3.hasMoreElements();) {
	  dirname=(String)en3.nextElement();
	  // Wenn entweder alle Richtungen oder Richtung==notwendige Gegenrichtung
	  if (all || adirs.get(dirname)!=null) {
	    // Exit holen
	    e=getExit((Exit)h.get(dirname));
	    // Wenn das ein Ausgang zum alten Punkt ist, wird er aktualisert auf den neuen Punkt
	    if (e.map==omap && e.to==opoint) {
	      e.map=nmap;e.to=npoint;
	      e.updateAngle();
	      maps[i].exits[e.exitspos][1]=npoint;
	    }
	  }
	}
      }

    Hashtable src, dest;
    if (!all) {
      // Ausgaenge des alten Punktes
      src=(Hashtable)OMap.special.get(P);
      // neue Ausgaenge des neuen Punktes
      dest=(Hashtable)NMap.special.get(DP);
      // wenn noch nicht vorhanden, erzeugen
      if (dest==null) {
	dest=new HashMap();
      }
    } else {
      // wenn eh alle uebertragen werden, wird die alte geloescht
      src=(Hashtable)OMap.special.remove(P);
      // und als neue gesetzt
      dest=src;
    }
    // neuer Ausgang
    int ne;
    System.out.println(src+"\n"+dest);
    // wenn Quellausgaenge vorhanden
    if (src!=null) {
      // in neuer Map Hashtable mit Ausgaengen speichern
      NMap.special.put(DP,dest);
      // fuer alle Quellausgaenge
      for (Enumeration en2=src.keys();en2.hasMoreElements();) {
	dirname=(String)en2.nextElement();
	// wenn alle uebertragen werden, oder richtung enthalten ist
	if (all || dirs.containsKey(dirname)) {
	  System.out.println(dirname);
	  // ausgang holen
	  e=getExit((Exit) src.get(dirname));
	  if (!all) {
	    // von quelle nach ziel kopieren
	    src.remove(dirname);
	    dest.put(dirname,e);
	  }
	  // exits in alter map loeschen
	  OMap.exits[e.exitspos][0]=-1;
	  // in neuer Map freien exit finden
	  ne=findFreeExit(nmap);
	  // wenn Maximum, dann Maximum erhoehen
	  if (ne>=NMap.numExits) NMap.numExits=ne+1;
	  // Quelle, Ziel setzen
	  e.from=npoint;
	  NMap.exits[ne][0]=e.from;
	  NMap.exits[ne][1]=e.to;
	  NMap.exits[ne][2]=e.dir;
	  e.exitspos=ne;
	}
      }
    System.out.println(src+"\n"+dest);
    }
  }
    Hashtable opt=new HashMap();
    final static String COMMAND_SEPARATOR=";";
    public void evalParamCommand(String cmd) {
	int off;
	String command;
	while (cmd.length()>0) {
	   off=cmd.indexOf(COMMAND_SEPARATOR);
	   if (off>-1) {
	       command=cmd.substring(0,off);
	       cmd=cmd.substring(off+COMMAND_SEPARATOR.length());
	   } else {
	       command=cmd; cmd="";
	   }
	   off=command.indexOf('<');
	   if (off>-1) {
	       commandline.requestFocus();
	       commandline.setText(command);
	       int off2=command.indexOf('>',off);
	       commandline.select(off,off2+1);
	   }
	   else
	       evalCommand(command);
	}
    }
    
  public void evalCommand(String s) {
    commandline.setText("");
    get=-1;
    s=s.trim();
    String key=null;
    for (Enumeration en=getPattern2.keys();en.hasMoreElements();) {
	key=(String)en.nextElement();
	if (s.indexOf(key)>-1) {
	    showStatus(i18n.trans("Parameter $0 fehlt!",new String[]{key}));
	    evalParamCommand(s);
	    return;
	}
    }
    s=s.replace('<',' ');
    s=s.replace('>',' ');
    showStatus(I18N("Befehl")+": "+s);
    String[] options=new String[1];
    int dir=-1, pt=-1, map=-1;
    options[0]=s;
    int[] opts=null;
    String desc=null;
    StringTokenizer st=new StringTokenizer(s);
    String param=null;
    String command=null;
    String params="";
    boolean history=true;
    Vector undo=null;
    if (!noundo) {
      undo=new Vector();
      undo.addElement(s);
    }
    if (st.hasMoreTokens()) {
      command=st.nextToken();
      try {
	params=s.substring(command.length()+1);
      } catch(Exception e) {}
    }
    else
      if (s.length()>0) command=Util.allTokens(st);
    System.out.println("evalCommand "+command+" saveundo "+!noundo);
    if (command!=null) {
      command=command.toLowerCase();
      if (movePoint(s,undo)) {
	  history=false;
    } else
    if (command.equals("opt")) {
      if (st.hasMoreTokens()) {
	 param=st.nextToken();
	if ("para".startsWith(param)) {
	  if (st.hasMoreTokens()) {
	    para=(st.nextToken().equals("1"));
	  } else 
	    para=!para;
	  opt.put("para",(para)?"1":"0");
	  setOptionMenuState("para",para);
	  showStatus(I18N("Dimension geändert")+": "+((para)?I18N("Parawelt"):I18N("Normalwelt")));
	  needRepaint();
	} else
	if ("numbers".startsWith(param)) {
	  shownumbers=!shownumbers;needRepaint();
	  setOptionMenuState("numbers",shownumbers);
	  opt.put("numbers",(shownumbers)?"1":"0");
	    addUndo(undo,"opt numbers");
	}
	else
	  if ("grid".startsWith(param)) {
	      showgrid=!showgrid; needRepaint();
	      opt.put("grid",(showgrid)?"1":"0");
	      setOptionMenuState("grid",showgrid);

	      addUndo(undo,"opt grid");
	      showStatus(I18N("Anzeige Gitter (Map Koordinaten)"));
	  }
	else
	  if ("vgrid".startsWith(param)) {
	    if (showgrid==false) {
	      addUndo(undo,"opt grid");
	    }
	    addUndo(undo,"opt vgrid");
	    showgrid=true; showvirtual=!showvirtual; needRepaint();
	    setOptionMenuState("vgrid",showvirtual);
	    setOptionMenuState("grid",showgrid);
	    opt.put("vgrid",(showvirtual)?"1":"0");
	    showStatus(I18N("Anzeige Gitter (Bildschirmkoordinaten)"));
	  }
	else
	  if ("level".startsWith(param)) {
	    if (st.hasMoreTokens()) {
	      try {
		int tmaxlevel=MAXLEVEL;
		MAXLEVEL=Integer.parseInt(st.nextToken());
		opt.put("level",""+MAXLEVEL);
		needRepaint();
		addUndo(undo,"opt level "+tmaxlevel);
		showStatus(i18n.trans("Maximalen Level fuer NPCs auf $0 gesetzt",""+MAXLEVEL));
	      } catch(Exception e) {
		  showStatus(I18N("Fehler in NPC-Level"));
	      }
	    } else
		  showStatus(I18N("Kein Level angegeben"));
	  }
	  else {
	  if (!noundo) {
	    String tvalue=(String)opt.get(param);
	    if (tvalue==null) {
	      addUndo(undo,"opt "+param);
	    } else
	      addUndo(undo,"opt "+param+" "+tvalue);
	    if (st.hasMoreTokens()) {
		showStatus(i18n.trans("Option hinzugefuegt: Schluessel $0 Wert $1",param,(String)opt.get(param)));
	      opt.put(param,Util.allTokens(st)); 
	      saveOptions();
	    }
	    else {
	      Object o=opt.remove(param);
	      if (o==null) 
		    showStatus(i18n.trans("Option: $0 war nicht vorhanden",param));
	      else {
		    showStatus(i18n.trans("Option entfernt: Schluessel $0 alter Wert $1"));
		  saveOptions();
	      }
	    }
	  }
	  }
      } else {
	  listOptions();
      }
    } else
      if (command.equals("lpc")) {
	if (st.hasMoreTokens()) {
	  param=st.nextToken();
	  if (param.equals("map")) {
	    for (int i=0;i<numPoints;i++) {
	      if (points[i][4]!=-2) lpc(i);
	    }
	  }
	  else 
	  if (param.equals("marked")) {
	    for (int i=numPoints-1;i>=0; i--) 
	      if ((points[i][3]& MARK_MASK)!=0)
	      lpc(i);
	  }
	} else lpc(aktpoint);
      }
      else
    if (command.equals("sn")) {
      // pending undo
      Hashtable dirs=new HashMap();
      Hashtable h=(Hashtable)special.get(new Integer(aktpoint));
      int np;
      String dirname;
      while (st.hasMoreTokens()) {
	param=st.nextToken();
	dir=getDirectionId(param);
	if (dir>0) param=getDirNameDir(dir);
	if (h.get(param)!=null) {
	  dirs.put(param,dummy);
	  System.out.print(param);
	}
      }
      if (dirs.size()>0) {
	np=addNode(points[aktpoint][0],points[aktpoint][1],points[aktpoint][2],true,null);
	redirectConnection(aktmap, aktpoint, aktmap, np,dirs);
      }
    } else
      if (command.equals("lo")) {
	  listOptions();
      } else
	if (command.equals("unmark")|| command.equals("um")) {
	  final Vector ux=new Vector();
	  workReferenced(NodeWorker.MAP,st,new NodeWorker() {
	    boolean modifyNode(int node, int krit) {
	      if ((points[node][3]&MARK_MASK)!=0) {
	      if (!noundo) {
		ux.addElement(new Integer(node));
	      }
		points[node][3]&=~MARK_MASK;
		return true;
	      }
	    return true;
	    }
	  });
	  if (!noundo && ux.size()>0) {
	    StringBuffer sb=new StringBuffer("unmark nodes ");
	    for (int i=0;i<ux.size();i++)
	      sb.append(ux.get(i).toString()+" ");
	    addUndo(undo,sb.toString());
	  }
	  
	  int count=0;
	  for (int i=0;i<numPoints;i++) {
	    if (points[i][4]!=-2 && (points[i][3] & MARK_MASK)!=0)
	       count++;
	  }
	  showStatus(I18N("Knoten demarkieren: markierte Knoten")+": "+count);

	  needRepaint();
	} else
	if (command.equals("moveview")||command.equals("mv")) {
	  if (st.hasMoreTokens())
	    moveView(st.nextToken(),undo);
	} else
	  if ("center".startsWith(command)) {
	    center(params,undo);
	} else
	  if (command.equals("rp")||command.equals("repaint")) {
	      needRecalc();
	} else
	  if ("zoom".equals(command)||"zm".equals(command)) {
	    int zmode=ZOOM_COUNT, zcount=1;
	    if (params.startsWith("m")) zmode=ZOOM_MAP;
	    else
	    if (params.startsWith("l")) zmode=ZOOM_LEVEL;
	    else
	    if (params.startsWith("n")) zmode=ZOOM_NODES;
	    if (zmode!=ZOOM_COUNT) {
	      int off=params.indexOf(' ');
	      if (off>-1)
		params=params.substring(off+1);
	    }
	    try {
	      zcount=Integer.parseInt(params);
	    } catch(Exception e) {
	    }
	    System.out.println("zoom("+zmode+","+zcount+","+undo+")");
	    zoom(zmode,zcount,undo);
	} else
	  if ("unzoom".equals(command)||"uz".equals(command)) {
	    try {
	      zoom(UNZOOM_COUNT,Integer.parseInt(params),undo);
	    } catch(Exception e) {
	      zoom(UNZOOM_COUNT,1,undo);
	    }
	} else
	if ("mark".startsWith(command)) {
	  final Vector ux=new Vector();
	  workReferenced(NodeWorker.AKT,st,new NodeWorker() {
	    boolean modifyNode(int node, int krit) {
	      if ((points[node][3]&MARK_MASK)==0) {
	      if (!noundo) {
		ux.addElement(new Integer(node));
	      }
		points[node][3]|=MARK_MASK;
		return true;
	      }
	    return true;
	    }
	  });
	  if (!noundo && ux.size()>0) {
	    StringBuffer sb=new StringBuffer("mark nodes ");
	    for (int i=0;i<ux.size();i++)
	      sb.append(ux.get(i).toString()+" ");
	    addUndo(undo,sb.toString());
	  }
	  int count=0;
	  for (int i=0;i<numPoints;i++) {
	    if (points[i][4]!=-2 && (points[i][3] & MARK_MASK)!=0)
	       count++;
	  }
	  showStatus(I18N("markierte Knoten")+": "+count);

	  needRepaint();
	} else
      if (command.equals("mh")) {
	// pending undo
	if (st.hasMoreTokens()) {
	  param=st.nextToken();
	  pt=getPointId(aktmap,param);
	  if (pt==-1) pt=aktpoint; 
	  int[] base=points[pt];
	  int flags=0, calc;
	    for (int i=numPoints-1;i>=0; i--) {
	      flags=points[i][3];
	      if ((flags & MARK_MASK)!=0) {
		if ((flags & HOUSE_MASK)==0) { // was HOUSE2_MASK
		  if ((flags & HOUSE_MASK)!=0); // flags |=HOUSE2_MASK;
		  else flags |= HOUSE_MASK;
		  for (int j=0;j<3;j++) {
		    calc=points[i][j];
		    calc-=base[j];
		    calc/=2;
		    calc+=base[j];
		    points[i][j]=calc;
		  }
		  points[i][3]=flags;
		}
	      } 
	    }
	    needRecalc();
	}
      } else
	  /*
      if (command.equals("undo")) {
	// no undo for undo
	System.out.println(params);
	if (params!=null && params.length()>0) {
	  try {
	    int count=Integer.parseInt(params);
	    for (int i=0;i<count;i++) undo();
	  } catch(Exception e) {
	    //	    e.printStackTrace();
	  }
	} else undo();
      } else
	  */
      if (command.equals("lna")||command.equals("search")) {
	// no undo
	if (st.hasMoreTokens()) {
	  boolean mark=false;
	  boolean unmark=false;
	  String att=st.nextToken();
	  if (att.equals("-m")) {
	    mark=true;
	    att=st.nextToken();
	  } else
	  if (att.equals("-u")) {
	    unmark=true;
	    att=st.nextToken();
	  }
	  int LEVEL=1, MAP=2, ALL=3, REGION=4;
	  int which=LEVEL;
	  int[] reg=null;
	  param=null;
	  String task;
	  boolean isTask=false;
	  if (st.hasMoreTokens()) {
	    param=st.nextToken().toLowerCase();
	    if ("level".startsWith(param)) which=LEVEL;
	    else if ("map".startsWith(param)) which=MAP;
	    else if ("all".startsWith(param)) {
	      which=ALL;
	    }
	    else if ("region".startsWith(param)) {
	      which=REGION;
	      reg=new int[3];
	      int count=0;
	      while (count<3 && st.hasMoreTokens()) 
		try {
		  reg[count++]=Integer.parseInt(st.nextToken())*2;
		} catch(NumberFormatException e) {
		}
	    }
	  }
	  String search=null;
	  boolean dosearch=false, negsearch=false;
	  String value=null;
	  int idx=-1;
	  if (st.hasMoreTokens()) {
	    search=st.nextToken();
	    if ("task".startsWith(search)) {
	      isTask=true;
	      search=null;
	    } else {
	      if (search.startsWith("!")) {
		negsearch=true;
		search=search.substring(1);
	      }
	      try {
	      search+=Util.allTokens(st);
	      } catch(Exception e) {}
	      search=search.toLowerCase();
	      dosearch=true;
	    }
	  }
	  smallinfo.clear();
	  smallinfo.append(att+":\n");
	  StringBuffer sb;
	  String taskCmd;
	  Hashtable tasks=null, taskCmds=null, temp=null;
	  if (isTask) {
	      tasks=new HashMap();
	      taskCmds=new HashMap();
	  }
	  Integer tasknr;
	  int x=points[aktpoint][0],y=points[aktpoint][1];
	  Map m=null; int mcount=0;
	  pushMap(aktmap);
	  String cmdPrefix;
	  if (which==ALL) mcount=0; else mcount=aktmap;
	  do {
	    cmdPrefix="";
	    checkMap(mcount);
	    m=maps[mcount++];
	    for (Enumeration en=m.info.keys();en.hasMoreElements();) {
	      Integer N=(Integer)en.nextElement();
	      int n=N.intValue();
	      if (which==LEVEL && points[n][2]==level
		  || which==REGION && Math.abs(points[n][0]-x)<=reg[0]
		  && Math.abs(points[n][1]-y)<=reg[1]
		  && Math.abs(points[n][2]-level)<=reg[2]
		  || which==MAP || which==ALL) {
		Node node=(Node)m.info.get(N);
		if (isTask) {
		  temp=node.getTask(att);
		  if (temp!=null) {
		    if (mark) {
		     if (mcount-1!=aktmap) 
		       m.points[n][3]|=MARK_MASK;
		     else points[n][3]|=MARK_MASK;
		    }
		    if (unmark)
		     if (mcount-1!=aktmap) 
		       m.points[n][3]&=~MARK_MASK;
		     else points[n][3]&=~MARK_MASK;

		    if (node.hasAttrib("name")) desc=node.getAttribute("name")+"("+N+")"; else desc=N.toString();
		    if (mcount-1!=aktmap) {
			desc=m.toString()+":"+desc;
			taskCmd="sm "+(mcount-1)+COMMAND_SEPARATOR+"g "+n;
		    } else taskCmd="g "+n;
		    desc=" ["+desc+"]\n";
		    for (Enumeration en2=temp.keys();en2.hasMoreElements();) {
		      tasknr=(Integer)en2.nextElement();
		      tasks.put(tasknr,(String)temp.get(tasknr)+desc);
		      taskCmds.put(tasknr,taskCmd);
		    }
		  }
		}
		else {
		  value=node.getAttribute(att);
		  if (dosearch) idx=value.toLowerCase().indexOf(search);
		  if (!value.equals("") & (!dosearch | dosearch & (negsearch & idx==-1 | !negsearch & idx != -1))) {
		    m.points[n][3]|=SHOW_ATT_MASK;
		    if (mark) m.points[n][3]|=MARK_MASK;
		    if (unmark) m.points[n][3]&=~MARK_MASK;
		  }
		}
	      }
	    }
	    if (!isTask) {
	    boolean mapdisp=false;
	    for (int i=0;i<m.numPoints;i++)
	      if ((m.points[i][3]&SHOW_ATT_MASK)!=0) {
		if (!mapdisp && which==ALL) {
		  cmdPrefix="sm "+(mcount-1);
		  smallinfo.append("Map "+m.toString()+":\n",cmdPrefix);
		  mapdisp=true;
		  cmdPrefix+=COMMAND_SEPARATOR;
		}
		Node n=(Node)m.info.get(new Integer(i));
		sb=new StringBuffer();
		if (n.hasAttrib("name"))
		    sb.append(n.getAttribute("name")+"("+i+")"); 
		else sb.append(""+i);
		sb.append(": "+n.getAttribute(att)+"\n");
		smallinfo.append(sb.toString(),cmdPrefix+"g "+i);
		m.points[i][3]&=~SHOW_ATT_MASK;
	      }
	    }
	  } while (which==ALL && mcount<numMaps);
	  if (isTask) {
	    int count=0;
	    Integer Count=null;
	    if (opt.get(att)!=null) task=(String)opt.get(att); else task=att;
	    String res=null;
	    smallinfo.clear();
	    smallinfo.append("Task: "+task+"\n");
	    do {
	      Count=new Integer(count++);
	      if ((res=(String)tasks.get(Count))!=null) {
		  smallinfo.append(Count+": "+res,(String)taskCmds.get(Count));
		  tasks.remove(Count);
	      }
	    } while(tasks.size()>0);
	  }
	  smallinfo.setText();
	}	    
      }
    if (command.equals("an")) {
      if (st.hasMoreTokens()) {
	int off=-1;
	if ((off=s.indexOf("$"))!=-1) {
	    forcenode=true;
	    s=s.substring(0,off)+s.substring(off+1);
	    st=new StringTokenizer(s); st.nextToken();
	  }
	int [] dest=new int[3];
	dest[2]=Integer.MIN_VALUE;
	dest[1]=Integer.MIN_VALUE;
	int counter=0;
	int base=aktpoint;
	boolean marked=false;
	param=st.nextToken();
	boolean relative=false;
      try {
	if (param.startsWith("-r")) {
	  relative=true;
	  System.out.println("relative");
	  if (param.length()==2) base=aktpoint;
	  else base=getPointId(aktmap,param.substring(2));
	} else {
	  dest[counter]=Integer.parseInt(param); counter++;}
	for (;counter<dest.length;counter++)
	  dest[counter]=Integer.parseInt(param=st.nextToken());
	param=null;
	param=st.nextToken();
      } catch(Exception e) {
	//	e.printStackTrace();
      }
	if (dest[2]==Integer.MIN_VALUE) dest[2]=(relative)?0:level;
	if (dest[1]==Integer.MIN_VALUE) dest[1]=(relative)?0:points[aktpoint][1];
	if (relative) {
	  for (int i=0;i<dest.length;i++)
	    dest[i]+=points[base][i];
	}
	pt=addNode(dest[0],dest[1],dest[2],forcenode,undo);
	recalcBorder(pt);
	needRecalc();
      }
    }
    else
    if (command.equals("mp")) {
      if (st.hasMoreTokens()) {
	boolean relative=false;
	map=-1;
	int [] dest=new int[3];
	dest[2]=Integer.MIN_VALUE;
	dest[1]=Integer.MIN_VALUE;
	int counter=0;
	int base=aktpoint;
	boolean marked=false;
	dir=-1;
	int length=1;
	param=st.nextToken();
	Hashtable mnodes=new HashMap();
	String undostring="";
	int undobase=-1;
      try {
	if (param.startsWith("-r")) {
	  relative=true;
	  if (param.length()>2) {
	    String token=param.substring(2);
	    int off=-1;
	    if ((off=token.indexOf("*"))!=-1) {
	      try {
		length=Integer.parseInt(token.substring(0,off));
	      } catch (Exception e) { }
	      token=token.substring(off+1);
	    }

	    dir=getDirectionId(token);
	  }
	} else if (param.startsWith("-a")) {
	  relative=false;
	  if (param.length()==2) base=aktpoint;
	  else base=getPointId(aktmap,param.substring(2));
	}
	else { dest[counter]=Integer.parseInt(param); counter++; }
	if (dir<1)
	for (;counter<dest.length;counter++)
	  dest[counter]=Integer.parseInt(param=st.nextToken());
	param=st.nextToken();
      } catch(Exception e) {
	e.printStackTrace();
      }
      undostring+=points[base][0]+" "+points[base][1]+" "+points[base][2];
	if (param!=null) {
	if (param.startsWith("-m")) {
	  map=getMapId(param.substring(2));
	  if (map==aktmap) map=-1; else relative=false;
	  if (map!=-1) undostring+=" -m"+aktmap;
	  if (st.hasMoreTokens()) param=st.nextToken(); else param=null;
	}
	if (param!=null) {
	  if ("marked".startsWith(param)) {
	    marked=true;
	    undostring+=" marked";
	    for (int i=numPoints-1;i>=0; i--) 
	      if ((points[i][3]& MARK_MASK)!=0)
		mnodes.put(new Integer(i),dummy);
	  } 
	} 
	}
	if (mnodes.size()==0) mnodes.put(new Integer(aktpoint),dummy);
	if (dir<1) {
	  if (dest[2]==Integer.MIN_VALUE) dest[2]=(relative)?0:level;
	  if (dest[1]==Integer.MIN_VALUE) dest[1]=(relative)?0:points[aktpoint][1];
	}
        System.out.println("moveNodes "+dest[0]+" "+dest[1]+" "+dest[2]);
	System.out.println("Map "+map);
	if (lang==EN)
	    showStatus("Moving "+((marked)?"marked ":"")+"node(s) "+((map!=-1)?"to map "+map+" ":"")+((relative)?"relative by delta":"absolute to coordinates")+" ("+dest[0]+","+dest[1]+","+dest[2]+")");
	  else
	    showStatus("Verschieben "+((marked)?"markierter ":"")+"Knoten "+((map!=-1)?"nach Map "+map+" ":"")+((relative)?"relativ um Delta":"absolut zu Koordinaten")+" ("+dest[0]+","+dest[1]+","+dest[2]+")");
	if (!relative) {
	  for (int i=0;i<dest.length;i++)
	    dest[i]-=points[base][i];
	} 
	if (dir>0) {
	  int[] r=directions[dir];
	  for (int i=0;i<dest.length;i++)
	    dest[i]=r[i]*length;
	}
	int p;
	undobase=base;
	int oldmap=aktmap;
	if (map==-1) {
	  for (Enumeration en=mnodes.keys();en.hasMoreElements();) {
	    p=((Number)en.nextElement()).intValue();
	    for (int i=0;i<dest.length;i++)
	      points[p][i]+=dest[i];
	  }
	} else {
	  Hashtable refnodes=new HashMap();
	  readAllMaps();
	  switchMap(map);
	  int dp;
	  Object o;
	  Map omap=maps[oldmap];
	  Integer P, DP;
	  Hashtable refexits=new HashMap(), h;
	  Object rpt;
	  int plen=points[0].length;
	  Exit e;
	  for (Enumeration en=mnodes.keys();en.hasMoreElements();) {
	    P=(Integer)en.nextElement();
	    p=P.intValue();
	    dp=findFreePoint();
	    if (p==base) undobase=dp;
	    DP=new Integer(dp);
	    mnodes.put(P,DP);
	    System.out.print(p+">"+dp+" ");
	    System.arraycopy(omap.points[p],0,points[dp],0,plen);
	    for (int i=2;i>=0;i--) 
	      points[dp][i]+=dest[i];
	    omap.points[p][4]=-2;
	    o=omap.info.remove(P);
	    if (o!=null) info.put(DP,o);
	    pushMap(aktmap);
	    redirectConnection(oldmap,p,aktmap,dp,null);
	    popMap(aktmap);
	    recalcBorder(dp);
	  }
	}
 	if (!noundo) {
	  undostring="MP -a"+undobase+" "+undo;
	  if (map!=-1) {
	    addUndo(undo,"sm "+oldmap);
	  }
	  addUndo(undo,undostring);
	  addUndo(undo,"g "+undobase);
	}
	needRecalc();
    }
    } else
    if (command.equals("ar")) {
      if (st.hasMoreTokens()) {
	String dirname=Util.allTokens(st);
	Hashtable h=(Hashtable)special.get(new Integer(aktpoint));
	if (h!=null && h.size()==1) {
	  Exit e=getExit((Exit)h.get(h.keys().nextElement()));
	  if (e!=null) {
	    addUndo(undo,"asp -d"+e.dir+" -m"+e.map+" -p"+e.to+((e.dirname!=null)?e.dirname:""));
	    addUndo(undo,"DE #"+dirname);
	    h.remove(e.getDirName());
	    e.dirname=dirname;
	    h.put(e.getDirName(),e);
	    needRepaint();
	  }
	} else {
	  int from=-1;
	  for (int i=numExits-1;i>=0;i--) {
	    if (exits[i][1]==aktpoint && exits[i][0]>-1)
	      if (from==-1) from=exits[i][0];
	      else {from=-1;break;}
	  }
	  if (from==-1) {
	      showStatus(I18N("Ersetzen: Keinen bzw. mehr als einen Knoten gefunden, die mit diesem Knoten verbunden sind."));
	  }
	  else {
	    addUndo(undo,"DE #"+dirname);
	    h=(Hashtable)special.get(new Integer(from));
	    Exit e=null;
	    for (Enumeration en=h.keys();en.hasMoreElements();) {
	      e=getExit((Exit)h.get(en.nextElement()));
	      if (e.to==aktpoint) break;
	    }
	    if (e.to==aktpoint) {
	      addExit(aktpoint,e.from,e.map,dir_anti_lookup[e.dir],dirname,true,null);
	      needRepaint();
	    }
	  }
	}
      }
    } else
    if (command.equals("alb")) { // AddLab
      try {
	String adir=st.nextToken();
	String np=st.nextToken();
	int npt=getPointId(aktmap,np);
	if (npt==-1) {
	  int apt=-1;
	  try { apt=Integer.parseInt(np); } catch(Exception e) {}
	  if (apt>-1) {
	    numPoints=apt+1;
	    renewPoints();
	    npt=getPointId(aktmap,np);
	    
	  }
	}
      if (npt!=-1) evalCommand("asp -d#2*"+adir+" -p"+npt);
      } catch(Exception e) {
	e.printStackTrace();
      }
    } else
    if (command.equals("ae")) { //ae (#?(dirnr|dirname) )*  // #==oneway
      String dirname;
      while (st.hasMoreTokens()) {
	dirname=st.nextToken();
	if (new String("marked").startsWith(dirname)) {
	    showStatus(I18N("Erzeugen: Erzeuge Verbindungen zwischen markierten Knoten"));
	  for (int i=numPoints-1;i>=1; i--) 
	    if ((points[i][3]& MARK_MASK)!=0)
	      for (int j=i-1;j>=0; j--) 
		if (i!=j && (points[j][3]& MARK_MASK)!=0) {
		  System.out.println("calcdir "+dir_name_lookup[calcDir(i,j)]);
		  int cdir=calcDir(i,j);
		  if (isExit(i,-1,aktmap,cdir,null)==null && isExit(j,-1,aktmap,dir_anti_lookup[cdir],null) == null)
		  addBothExits(i,j,aktmap,cdir,null,false,undo);
		}
	}
	else {
	  int length=1;
	  boolean force=false;
	  forcenode=false;
	  int off=-1;
	  String token=dirname;
	  if ((off=token.indexOf("#"))!=-1) {
	    oneway=true;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("!"))!=-1) {
	    force=true;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("$"))!=-1) {
	    forcenode=true;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  boolean addsecond=false;
	  if ((off=token.indexOf("&"))!=-1) {
	    addsecond=true;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("*"))!=-1) {
	    try {
	      length=Integer.parseInt(token.substring(0,off));
	    } catch (Exception e) { }
	    token=token.substring(off+1);
	  }
	  dir=getDirectionId(token);
	  if (dir>0) {
	    addPoint(aktpoint,-1,aktmap,dir,null,force,forcenode,length, addsecond, undo);
	  }
	}
      }
    } else
      if (command.equals("asp")) { //addSpecial -d#(dirnr|dirname|0) [-p(pointname|id) [-m(mapname|id)]] description 
	opts=parseOptions(options); if (opts!=null) {dir=opts[OPT_DIR];map=opts[OPT_MAP];pt=opts[OPT_DEST]; desc=options[0];}
	if (dir!=-1) {
	  System.out.println("map "+map+" point "+pt+" desc "+desc);
	  if (desc!=null) oneway=true;
	  if (dir==0 && pt!=-1 || dir>0) 
	    pt=addPoint(aktpoint,pt,map,dir,desc,opts[OPT_FORCE]==1,opts[OPT_FORCE_NODE]==1,opts[OPT_LENGTH],opts[OPT_SECOND]==1,undo);
	  // movePoint(desc);
	}
      } else
	if (command.equals("lm")) {
	  listMaps();
	} else
    if (command.equals("save")) {
      int m=-2;
      param="";
      if (st.hasMoreTokens()) {
	param=st.nextToken();
      } else {
	  saveMap(aktmap);
	  saveMaps();
      }
      if (param.equals("all")) {
	for (int i=0;i<numMaps;i++) {
	  saveMap(i);
	}
	saveMaps();
      } else {
	do {
	  m=getMapId(param);
	  if (m>-1) {
	    saveMap(m);
	    saveMaps();
	  }
	  if (st.hasMoreTokens())
	    param=st.nextToken();
	  else param=null;
	} while (param!=null);
      }
    }
    else
    if (command.equals("load")) {
      int m=-2;
      param="";
      if (st.hasMoreTokens()) {
	param=st.nextToken();
      } // else readMap(aktmap);
      if (param.equals("all")) {
	for (int i=0;i<numMaps;i++) {
	  readMap(i);
	  if (aktmap==i) {
	      popMap(i);
	      needRepaint();
	  }
	}
      } else {
	do {
	  m=getMapId(param);
	  if (m>-1) {
	    readMap(m);
	  if (aktmap==m) {
	      popMap(m);
	      needRepaint();
	  }

	  }
	  if (st.hasMoreTokens())
	    param=st.nextToken();
	  else param=null;
	} while (param!=null);
      }
    }

    else
      if (command.equals("q")) {
	saveOptions();
	if (!isApplet) System.exit(0);
      }
    else
      if (command.equals("sf")) {
	if (st.hasMoreTokens()) {
	  String att=st.nextToken();
	  boolean doset=false; boolean set=true;
	  if (att.startsWith("+")) {
	    doset=true;
	    att=att.substring(1);
	  } else if (att.startsWith("-")) {
	    doset=true;
	    set=false;
	    att=att.substring(1);
	  }
	  Integer fl=(Integer)flags.get(att);
	  if (fl!=null) {
	    final int ifl=fl.intValue();
	    final boolean doset2=doset, set2=set;
	    final String att2=att;
	    workReferenced(NodeWorker.AKT,st,new NodeWorker() {
	      boolean modifyNode(int node, int krit) {
		boolean set3=false;
		if (!doset2) set3=!((points[node][3]&ifl)!=0);
		else set3=set2;
		if  (!set3) {
		  points[node][3]&=~ifl;
		  showStatus(i18n.trans("SetFlag: $0 Flag $1 gelöscht",getPointString(aktmap,node),att2));
		}
		else {
		  points[node][3]|=ifl;
		  showStatus(i18n.trans("SetFlag: $0 Flag $1 gesetzt",getPointString(aktmap,node),att2));
		}
		return true;
	      }});
	  }
	  needRepaint();
      } else {
	  smallinfo.clear();
	  smallinfo.append("Flags:\n");
	  int fl=-1;
	  String flagString=null;
	  int pflags=points[aktpoint][3];
	  for (Enumeration en=flags.keys();en.hasMoreElements();) {
	    flagString=(String)en.nextElement();
	    fl=((Integer)flags.get(flagString)).intValue();
	    if ((pflags & fl)!=0) {
	      smallinfo.append(flagString+": "+I18N("gesetzt")+"\n",
			       "sf "+flagString+" <1>");
	    } 
	    else smallinfo.append(flagString+": "+I18N("nicht gesetzt")+"\n",
				  "sf "+flagString+" <0>");
	  }
	  smallinfo.setText();
	}
      } 
    else
      if (command.equals("aecmd")) {
	if (st.hasMoreTokens()) {
	  String ename=st.nextToken();
	  boolean oneway=false;
	  if (ename.startsWith("#")) {
	    ename=ename.substring(1);
	    oneway=true;
	  }
	  Exit e=getExit((Exit)((Hashtable)special.get(new Integer(aktpoint))).get(ename));
	  Exit ex=null;
	  if (e!=null) {
	    if (!oneway) ex=isOneWay(e);
	    if (st.hasMoreTokens()) {
	      e.command=Util.allTokens(st);
	      if (ex!=null) ex.command=e.command;
	    }
	    else {
	      e.command=null;
	      if (ex!=null) ex.command=null;
	    }
	  }
	}
      } else
      if (command.equals("aed")) {
	if (st.hasMoreTokens()) {
	  String ename=st.nextToken();
	  Exit e=getExit(aktpoint, ename);
	  //(Exit)((Hashtable)special.get(new Integer(aktpoint))).get(ename));
	  if (e!=null)
	  if (st.hasMoreTokens())
	    e.door=Util.allTokens(st);
	  else e.door=null;
	}	
      } else
      if (command.equals("aec")) {
	if (st.hasMoreTokens()) {
	  String ename=st.nextToken();
	  boolean oneway=false;
	  if (ename.startsWith("#")) {
	    ename=ename.substring(1);
	    oneway=true;
	  }
	  Exit e=getExit((Exit)((Hashtable)special.get(new Integer(aktpoint))).get(ename));
	  Exit ex=null;
	  if (e!=null) {
	    if (st.hasMoreTokens()) {
	      if (!oneway) ex=isOneWay(e);
	      String cname=Util.allTokens(st);
	      System.out.println("ace "+cname);
	      if (parseColor(cname)!=null) {
		e.color=cname;
		needRepaint();
		if (ex!=null) {
		  ex.color=cname;
		}
	      }
	    } 
	    else if (e.color!=null || ex!=null && ex.color!=null) {
	      needRepaint();
	      e.color=null;
	      if (ex!=null) ex.color=null;
	    }
	  }}
      } else
      if (command.equals("ai")) {
	if (st.hasMoreTokens()) {
	    final String att=st.nextToken();
	    String select="";
	    String att_value=null;
	    if (st.hasMoreTokens()) {
	      att_value=Util.allTokens(st);
System.out.println(att_value);
	      int off=att_value.indexOf("-r");
	      if (off>-1) {
		select=att_value.substring(off+2).trim();
		att_value=att_value.substring(0,off).trim();
System.out.println(select);
System.out.println(att_value);
	      }
	    }
	    st=new StringTokenizer(select);
	    final String f_att_value=att_value;
	    workReferenced(NodeWorker.AKT,st,new NodeWorker() {
	    boolean modifyNode(int anode, int krit) {
	      Node node=(Node)info.get(new Integer(anode));
	      if (f_att_value!=null && f_att_value.length()>0) {
		if (node==null) {
		  node=new Node(); 
		  info.put(new Integer(anode),node);
		}
		node.setAttribute(att,f_att_value);
		boolean nameadded=false;
		if (att.equals("ways") && (node.getAttribute("name").length()==0)) {
		  node.setAttribute("name",node.getAttribute("ways"));
		  nameadded=true;
		}
		if (att.equals("name") || nameadded) {
		  named.put(node.getAttribute("name"),new Integer(anode));
		}
		if (att.equals("icon")) {
		    icons.remove(node);
		}
		  showStatus(i18n.trans("Fuer Knoten $0 neues Attribut $1 Wert $2",""+anode,att,node.getAttribute(att)));
		needRepaint();
	      } else if (node!=null) {
		  showStatus(i18n.trans("Attribut $1 aus Knoten $0 gelöscht, vorheriger Wert $2",""+anode,att,node.getAttribute(att)));
		node.setAttribute(att,null); 
	      }
	      return true;
	    }
	    });
	    pointInfo(aktpoint);
	}
      }
      else if (command.equals("saveobjects")) { // saveObjects
	saveObjects();
      }
      else if (command.equals("loadobjects")) { // saveObjects
	loadObjects();
      }
      else if (command.equals("oi")) { // ObjektInfo
	if (params!=null) {
	  smallinfo.setText(Info.getInfo(params));
	}
      }
      else if (command.equals("si")) { // ShowInfo (node)
	pointInfo(st.hasMoreElements()?getPointId(aktmap,st.nextToken()):aktpoint);
      }
      else if (command.equals("ci")) {
	if (params!=null && params.length()>0)
	  new Info(params);
	  saveObjects();
      }
      else if (command.equals("ei")||command.equals("eo")) { // EditInfo oder EditObject
	if (params!=null) {
	    Info info=Info.get(params);
	    editInfo(info);
	}
	else
	pointInfo(st.hasMoreElements()?getPointId(aktmap,st.nextToken()):aktpoint);
      }
      else if (command.equals("di")) { 
	if (params!=null) {
	  Info i=Info.get(params);
	  smallinfo.setText(i.toString());
	  int pmap=-1;
	  try {
	      if (i.getType().equals("polygon")) {
		  pmap=new Integer(i.getAttribute("map")).intValue();
	      }
	  } catch(Exception e) {
	      e.printStackTrace();
	      showStatus(i18n.trans("Fehler beim Aktualisieren von")+" "+i.getTypeName());
	  }
	  Info.deleteInfo(i);
	  if (pmap>-1) {
	      parsePolygons(pmap);
	      needRepaint();
	  }
	  saveObjects();
	}
      }
      else if (command.equals("ep")||command.equals("editPoly")) {
	  param=st.nextToken();
	  float[][] pcoords=parseCoordsSpaced(params);
	  System.out.println(pcoords.length+" "+pcoords[0][0]);
	  MPolygon poly;
	  for (Enumeration en=polygons.elements();en.hasMoreElements();) {
	      poly=(MPolygon)en.nextElement();
	      if (poly.inside(pcoords[0][0],pcoords[0][1],pcoords[0][2])) {
		  editInfo(poly.info);
		  break;
	      } else System.out.println(poly.info.toString()+" false");
	  }
      }
      else if (command.equals("poly")) {
	// Polygon als Object kreiieren
	// in Anzeige einbauen
	try {
	if (st.hasMoreTokens()) {
	  Info poly=new Info("polygon","Poly "+maps[aktmap].name);
	  poly.setAttribute("map",new Integer(aktmap));
	  param=st.nextToken();
	  int off=param.indexOf("-");
	  Integer level_from=new Integer(Integer.MIN_VALUE);
	  Integer level_to=new Integer(Integer.MAX_VALUE);
	  if (off!=-1 && off<3) {
	    try {
	      level_from=new Integer(param.substring(0,off));
	    }catch(Exception e) {}
	    try {
	      level_to=new Integer(param.substring(off+1));
	    }catch(Exception e) {}
	  }
	  poly.setAttribute("level_from",level_from);
	  poly.setAttribute("level_to",level_to);
	  if (st.hasMoreTokens()) {
	    param=st.nextToken();
	    if (param.startsWith("(")) {
		param+=" "+st.nextToken();
	    }
	    if (parseColor(param)==null) {
	      param=props.getProperty("default_poly");
	    }
	    poly.setAttribute("color",param);
	  }
	  param=Util.allTokens(st);
	  off=param.indexOf("-t");
	  if (off>-1) {
	    String ptext=param.substring(off+2).trim();
	    param=param.substring(0,off).trim();
	    poly.setAttribute("text",ptext);
	    poly.setName("Poly "+maps[aktmap].name+" "+ptext);
	  }
	  float[][] pcoords=null;
	  if ((pcoords=parseCoords(param))!=null) {
	    poly.setAttribute("coords",param);
	    float[] bd=new float[2];
	    for (int i=pcoords.length-1;i>=0;i--) {
	      bd[0]+=pcoords[i][0];
	      bd[1]+=pcoords[i][1];
	    }
	    bd[0]/=pcoords.length;
	    bd[1]/=pcoords.length;
	    /*
	    float[][] bd=getBorders(pcoords);
	    bd[0][0]=(bd[0][0]+bd[0][1])/2;
	    bd[1][0]=(bd[1][0]+bd[1][1])/2;
	    */
	    poly.setAttribute("center",""+bd[0]+","+bd[1]);
	  }
	}
	saveObjects();
	parsePolygons(aktmap);
	} catch(Exception e) {
	  e.printStackTrace();
	}
	needRepaint();
	System.out.println(Info.getType("polygon"));
      }
      else
	if (command.equals("am")) {
	  if (st.hasMoreTokens()) {
	    createMap(Util.allTokens(st));
	  }
	}
      else
	if (command.equals("rename")) {
	    if (st.hasMoreTokens()) {
		int id=getMapId(st.nextToken());
		if (st.hasMoreTokens())
		    renameMap(id,Util.allTokens(st));
	    }
	}
    else
	if (command.equals("dm")) {
	  if (st.hasMoreTokens()) {
	    clearMap(getMapId(Util.allTokens(st)));
	  }
	}
	else if (command.equals("dp")) {
	  readAllMaps();
	  pt=aktpoint;
	  String ptname=null;
	  if (st.hasMoreTokens()) 
	    while (st.hasMoreTokens()) {
	      ptname=st.nextToken();
	      if (new String("marked").startsWith(ptname)) {
		showStatus(I18N("Löschen: Entferne markierte Knoten"));
		for (int i=numPoints-1;i>=0; i--) 
		  if ((points[i][3]& MARK_MASK)!=0)
		    deletePoint(i);
	      }
	      else {
		pt=getPointId(aktmap,ptname);
		if (pt>-1) deletePoint(pt);
	      }
	    }
	  else
	    deletePoint(pt);
	}
	else if (command.equals("cgrid")) {
	  int px=Integer.parseInt(st.nextToken());
	  int py=Integer.parseInt(st.nextToken());
	  int sx=-1, sy=-1;
	  boolean di=false;
	  String opt;
	  while (st.hasMoreTokens()) {
	    opt=st.nextToken();
	    if (opt.equals("-sx")) sx=Integer.parseInt(st.nextToken());
	    if (opt.equals("-sy")) sy=Integer.parseInt(st.nextToken());
	    if (opt.equals("-di")) di=true;
	  }
	  System.out.println(px+" "+py+" "+sx+" "+sy);
	  if (sx!=-1) {
	  if (sy==-1) sy=sx;
	  if (sy>0 && sx>0) {
	   if (lang==EN)
	     showStatus("Create: Grid width "+sx+" height "+sy+" Position of actual node within grid "+px+", "+py+((di)?"with diagonals":""));
	   else
	     showStatus("Erzeugen: Netzstruktur: Breite "+sx+" Höhe "+sy+" Position des aktuellen Knotens im Netz "+px+", "+py +((di)?"mit Diagonalen":""));
	   showStatus=false;
	  int bx=points[aktpoint][0]-px*2,by=points[aktpoint][1]-py*2;
	  int[][] grid=new int[sx][sy];
	  int cx=0,cy=0;
	  for (int x=bx;x<bx+sx*2;x+=2) {
	    for (int y=by;y<by+sy*2;y+=2) {
	      grid[cx][cy++]=addNode(x,y,level,false,null);
	      System.out.println("adding node at"+x+","+y+","+level+" cx "+cx+" cy "+cy);
	    }
	    cx++;
	    cy=0;
	  }
	  for (int x=0;x<sx;x++) for (int y=0;y<sy;y++)
	    for (int d=calcDir_directions.length-1;d>=0;d--) {
	      cx=x+calcDir_directions[d][0];
	      cy=y+calcDir_directions[d][1];
	      System.out.print(" cx "+cx+" cy "+cy);
	      if ((x!=cx || y!=cy) && cx>=0 && cx<sx && cy>=0 && cy <sy)
		if (!(x!=cx && y!=cy && !di))
		addExit(grid[x][y],grid[cx][cy],aktmap,dir_lookup[d],null,false,null);
	    }
	   showStatus=true;
	  }
	  }
	}
    	else if (command.equals("de")) {
	  opts=parseOptions(options); if (opts!=null) {dir=opts[0];map=opts[1];pt=opts[2]; desc=options[0];}
	  if (dir>-1) {
	    Exit e=deleteExit(aktpoint,pt,map,dir,desc);
	    if (e!=null && !oneway) {
	      deleteExit(e.to,e.from,aktmap,dir_anti_lookup[e.dir],null);
	    }
	    oneway=false;
	  }
	  else {
	    String dirname;
	    while (st.hasMoreTokens()) {
	      dirname=st.nextToken();
	      if (new String("marked").startsWith(dirname)) {
		  showStatus(I18N("Löschen: Entferne Verbindungen zwischen markierten Knoten"));
	  for (int i=numPoints-1;i>=1; i--) 
	    if ((points[i][3]& MARK_MASK)!=0)
	      for (int j=i-1;j>=0; j--) 
		if (i!=j && (points[j][3]& MARK_MASK)!=0) {
		  deleteExit(i,j,aktmap,calcDir(i,j),null);
		  deleteExit(j,i,aktmap,calcDir(j,i),null);
		}
	      }
	      else {
	      if (dirname.startsWith("#")) {
		oneway=true;
		System.out.println("oneway true");
		dirname=dirname.substring(1);
	      }
	      dir=getDirectionId(dirname);
	      //	      if (dir>-1) {
		Exit e=deleteExit(aktpoint,-1,aktmap,dir,(dir==-1)?dirname:null);
		if (dir>-1 &&e!=null && !oneway) {
		  deleteExit(e.to,e.from,aktmap,dir_anti_lookup[e.dir],null);
		}
		oneway=false;
		//	      }
	      }
	    }
	  }}
	else if (command.equals("sm")) {
	  if (st.hasMoreTokens()) {
	    int m=getMapId(Util.allTokens(st));
	    if (m>-1 && m!=aktmap) {
	      addUndo(undo,"sm "+aktmap);
	      switchMap(m);
	    }
	  }
	}
	else if (command.equals("ps")) {
	  System.out.println(special.toString());
	}
	else if (command.equals("pfc")) {
	  System.out.println("fcount "+fcount);
	  for (int i=0;i<fcount;i++) {
	    System.out.println(i+". fpoints>point"+(int)fpoints[i][3]+" point>fpoint "+(((int)fpoints[i][3]>-1)?points[(int)fpoints[i][3]][4]:-1)+ " fpoint "+fpoints[i][4]);
	  }
	}
	else if (command.equals("?")) {
	  checkHelp();
	  showHelp(params);
	}
	else if (command.equals("g")) {
	  pt=getPointId(aktmap,s.substring(2));
	  if (pt>-1) {
	    addUndo(undo,"g "+aktpoint);
	    showStatus(I18N("Bewegung: gehe zu Knoten")+" "+s.substring(2));
	    aktpoint=pt;
	    showMovedPoint();
	  }
	}
	else if (command.equals("go")) {
	  if (st.hasMoreTokens()) {
	  String dest=Util.allTokens(st);
	  int[] pts=getAllPointId(aktmap,dest);
	  if (pts[0]>-1) {
	    addUndo(undo,"g "+aktpoint);
	    addUndo(undo,"sm "+aktmap);
	    showStatus(I18N("Bewegung: gehe zu Knoten")+" "+dest+((pts[1]!=aktmap)?maps[pts[1]].toString():""));
	  if (pts[1]!=aktmap) switchMap(pts[1]);
	  aktpoint=pts[0];
	  showMovedPoint();
	  }
	  }
	}
    //    if (history && !noundo) saveHistory(s);
    if (history) saveHistory(s);
    //    if (!noundo && undo.size()>1) saveUndo(undo);
    wake();
  }}

  float[][] getBorders(float[][] arg) {
    float[][] bd=new float[2][2];
    for (int x=0;x<2;x++) 
      for (int mm=0;mm<2;mm++) 
	bd[x][mm]=arg[x][0];
    for (int i=arg.length-1;i>=1;i--)
      for (int xy=0;xy<2;xy++) {
	bd[xy][0]=Math.min(arg[i][xy],bd[xy][0]);
	bd[xy][1]=Math.max(arg[i][xy],bd[xy][1]);
      }
    return bd;
  }

Frame helpwindow=null;
TextArea helptext=null;
Hashtable help=null;
  public void checkHelp() {
    if (helpwindow==null) {
      helpwindow=new Frame("Help");
      helptext=new TextArea(20,60);
      helpwindow.setLayout(new BorderLayout());
      helpwindow.add("Center",helptext);
      Button close=new Button(I18N("Schliessen"));
      close.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  ((Component)e.getSource()).getParent().hide();
	}
      });
      helpwindow.add("South",close);
      helpwindow.pack();
   }
    if (help==null) {
      try {
	DataInputStream dis=new DataInputStream(new BufferedInputStream((new URL(mapsdir+"mapper_"+I18N("de")+".hlp")).openStream())); 
	String l=null;
	String ref=null, text="";
	help=new HashMap();
	while ((l=dis.readLine())!=null) {
	  if (l.indexOf("§")!=-1) {
	    if (ref!=null) {
	      help.put(ref,text);
	      //	      System.out.println(ref+": "+text.substring(0,20));
	    }
	    ref=l.substring(0,l.indexOf("§"));
	    text="";
	  } else {
	    text+=l+'\n';
	  }
	}
	if (ref!=null) help.put(ref,text);
	dis.close();
      }
      catch(Exception e) {
	e.printStackTrace();
      }
    }
  }
    public void renameMap(int id, String newname) {
	Map m=maps[id];
	showStatus(i18n.trans("Umbenennen von Karte $0 zu $1",new String[] {m.toString(),newname}));
	m.name=newname;
	m.menu.setLabel(m.name);
	if (id == aktmap) setTitle(maps[id].toString());
    }
    public void clearMap(int id) {
	showStatus(I18N("Löschen von Karte")+": "+maps[id]);
	switchMap(id);
	readAllMaps();
	int i=0;
	try {
	for (i=numPoints-1;i>=0;i--) {
	  deletePoint(i); 
	}
	} catch(Exception e) {
	    showStatus(I18N("Fehler beim Löschen von Knoten")+": "+i);
	}
	aktpoint=0;
	Map m=maps[id];
	m.name+="_cleared";

	m.menu.setLabel(m.name);
	numPoints=1;
	points=new int[5][5];
	for (i=1;i<5;i++)
	  points[i][4]=-2;

        points[0][0]=0;
	points[0][1]=0;
        points[0][2]=0;
        points[0][4]=-1;
	info=new HashMap();
	named=new HashMap();
	special=new HashMap();

	min=new float[2];
	min[0]=0;
	min[0]=0;
	
	level=0;
	numExits=0;
	exits=new int[5][3];
	for (i=0;i<exits.length;i++) exits[i][0]=-1;
	maxborder=new float[3][2];
	for (int j=0;j<3;j++) {
	    maxborder[j][0]=-0.3f;
	    maxborder[j][1]=0.3f;
	}
	pushMap(id);
	pointInfo(0);
	if (id == aktmap) setTitle(maps[id].toString());
	needRecalc();
    }
public float[][] parseCoords(String scoord) {
  StringTokenizer st=new StringTokenizer(scoord," ");
  int count=st.countTokens();
  float[][] res=null;
  if (count>0) {
    res=new float[count][3];
    while (st.hasMoreTokens()) {
      count--;
      int count2=0;
      StringTokenizer st2=new StringTokenizer(st.nextToken(),",");
      while (st2.hasMoreTokens()) {
	res[count][count2++]=(new Float(st2.nextToken())).floatValue();
      }
    }
  }
  return res;
}

public float[][] parseCoordsSpaced(String scoord) {
  StringTokenizer st=new StringTokenizer(scoord," ");
  int count=st.countTokens();
  float[][] res=null;
  if (count>0) {
    res=new float[count/3+1][3];
    count=0;
    while (st.hasMoreTokens()) {
	res[count/3][count%3]=(new Float(st.nextToken())).floatValue();
	count++;
    }
  }
  return res;
}

    public float[] modifyColor(Color c, int def, float factor) {
      if (factor==0) factor=2f;
      float[] cc=new float[3];
      cc[0]=c.getRed()/255f;
      cc[1]=c.getGreen()/255f;
      cc[2]=c.getBlue()/255f;
      float[][] cres=new float[2][3];
      int darker=0, brighter=0; // per default heller
      for (int i=0;i<cc.length;i++) {
	cres[0][i]=Math.max(0f,Math.min(cc[i]*factor,1f)); // heller
	if (factor!=0)
	  cres[1][i]=Math.min(1f,Math.max(cc[i]/factor,0f)); // dunkler
	else cres[1][i]=1f;
	if (cres[0][i]!=cc[i]) brighter++;
	if (cres[1][i]!=cc[i]) darker++;

	System.out.println(((i==0)?"Rot":(i==1)?"Gruen":"Blau")+cc[i]+"-> "+cres[0][i]+" > "+cres[1][i]);
      }
      return cres[(def==-1)?((darker>brighter)?1:0):def];
    }

    public int getPropSize(String propname, int def) {
	try {
	    return new Integer(props.getProperty(propname+"_size")).intValue();
	} catch(Exception e) {}
	return def;
    }
    public Color getPropColor(String propname) {
	return parseColor(props.getProperty(propname));
    }
Hashtable parsedColors=new HashMap();

public Color parseColor(String scolor) {
  if (scolor==null) {
      showStatus(i18n.trans("Farbe $0 nicht gefunden!",new Object[] {scolor}));
      return Color.red;
  }
  scolor=scolor.trim();
  if (scolor.startsWith("(") && scolor.endsWith(")"))
      scolor=scolor.substring(1,scolor.length()-1);
  Color cc=(Color)parsedColors.get(scolor);
  if (cc!=null) return cc;

  if (colors!=null)
      cc=(Color)colors.get(scolor.toLowerCase());
  if (cc==null) 
      try {
	  if (scolor.indexOf(',')>-1) {
	      float[] values=new float[3];
	      int val_counter=0;
	      StringTokenizer st=new StringTokenizer(scolor,",");
	      while (st.hasMoreTokens()) {
		  values[val_counter++]=new Float(st.nextToken()).floatValue();
	      }
	      cc=new Color(values[0],values[1],values[2]);
	  } 
	  else cc=Color.decode(scolor);
      } catch(Exception ex) {
      }
  if (cc!=null) parsedColors.put(scolor,cc);
  if (cc==null) {
      showStatus(i18n.trans("Farbe $0 nicht gefunden!",new Object[] {scolor}));
      cc=Color.red;
  }
  return cc;
}

  public void loadColors() {
    if (colors==null) {
      colors=new HashMap();
      try {
	DataInputStream dis=new DataInputStream(new BufferedInputStream((new URL(mapsdir+"map_colors_"+I18N("de"))).openStream())); 
	String l=null;
	String ref=null, text="";
	int off=-1;
	while ((l=dis.readLine())!=null) {
	  off=l.indexOf("§");
	    if (off!=-1)
	      colors.put(l.substring(0,off).toLowerCase(),Color.decode(l.substring(off+1)));
	}
	dis.close();
      }
      catch(Exception e) {
	e.printStackTrace();
      }
      System.out.println("Colors: "+colors);
    }
  }
  public void showHelp(String ref) {
    String text=null;
    //    System.out.println("ref"+ref);
    if (ref!=null) text=(String)help.get(ref.trim());
    if (text==null) text=(String)help.get("help");
    helptext.setText(text);
    helpwindow.show();
  }
  class EditWindow extends Frame {
    TextArea edittext;
    Choice attribs;
    MTextField tattrib;
    Panel pattrib;
    MyItemListener itemListener=new MyItemListener();
    class MyItemListener implements ItemListener {
	public void itemStateChanged(ItemEvent e) {
	   String s=attribs.getSelectedItem();
	   System.out.println("itemStateChanged "+lastSelection+"->"+s);
           if (!s.equals(lastSelection)) {
	     if (lastSelection.length()>0) addEntry(lastSelection);
	     showEntry(s);
	     updateCombo(s);
	   }
	   lastSelection=s;
	}
    }
    public EditWindow() {
      super("Edit");
      edittext=new TextArea(20,60);
      setLayout(new BorderLayout());
      add("Center",edittext);
      pattrib=new Panel();
      add("North",pattrib);
      pattrib.setLayout(new FlowLayout(FlowLayout.LEFT));
      attribs=new Choice();
      attribs.addItemListener(itemListener);
      pattrib.add(attribs);
      tattrib=new MTextField();
      tattrib.setColumns(30);
      pattrib.add(tattrib);
      Button battrib=new Button(I18N("Hinzufügen"));
      battrib.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  String s=tattrib.getText();
	  if (s.length()>0) {
	    String v=edittext.getText();
	    if (v.length()==0) {
	      v="value?";
	      edittext.setText(v);
	      edittext.selectAll();
	      tattrib.setText("");
	    }
	    i.setAttribute(s,v);
	    updateCombo(s+" *");
	  }
	}
      });
      pattrib.add(battrib);
      pattrib.invalidate();
      pattrib.validate();

      Panel controls=new Panel();
      controls.setLayout(new FlowLayout(FlowLayout.LEFT));
      add("South",controls);
      Button close=new Button(I18N("Schliessen"));
      close.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  addEntry(lastSelection);
	  doneEditInfo(i);
	  setVisible(false);
	}
      });

      controls.add(close);
      Button delete=new Button(I18N("Löschen"));
      delete.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  String s=attribs.getSelectedItem();
	  System.out.println("delete "+s);
	  if (s.endsWith(" *")) {
	    s=s.substring(0,s.length()-2);
	    edittext.setText("");
	    System.out.println("setattribute "+s+",null");
	    i.setAttribute(s,null);
	    updateCombo(null);
	  }
	}
      });
      controls.add(delete);

    IconButton colorButton=new IconButton(props.getProperty("color_chooser","paint2.gif"));
    colorButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    chooseColor(edittext);
	}
    });

    controls.add(colorButton);
    IconButton iconButton=new IconButton(props.getProperty("icon_chooser","painting.gif"));
    iconButton.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    chooseIcon(edittext);
	}
    });
    controls.add(iconButton);

      pack();
    }
  void addEntry(String s) {
    String value=edittext.getText();
    if (value.length()==0) value=null;
    if (s!=null)
      if (s.endsWith(" *")) {
	i.setAttribute(s.substring(0,s.length()-2),value);
      } else
	i.setAttribute(s,value);
  }
  void showEntry(String s) {
    if (s.endsWith(" *")) {
      edittext.setText(i.getAttribute(s.substring(0,s.length()-2)));
      edittext.selectAll();
    } else {
      edittext.setText("");
    }
  }
    String lastSelection;
    public void updateCombo(String s) {
      /*
      if (s==null)
	s=attribs.getSelectedItem();
      */
      attribs.setVisible(false);
      attribs.removeAll();
      String[] keys=i.getAllKeys();
      for (int j=0;j<keys.length;j++)
	attribs.add(keys[j]);
      attribs.setVisible(true);
      pattrib.invalidate();
      pattrib.validate();
      if (s==null) {
	lastSelection="";
	attribs.select(0);
	itemListener.itemStateChanged(null);
      }
      else {
	lastSelection=attribs.getSelectedItem();
	//	lastSelection=s;
	attribs.select(s);
      }
    }
    Info i;
    public void show(Info ai) {
      if (ai!=i) {
	i=ai;
	this.setTitle("Edit "+i.getTypeName());	
	edittext.setText("");
	tattrib.setText("");
	updateCombo(null);
      }
      this.show();
    }
  }

  EditWindow editwindow;

  public void editInfo(Info i) {
    if (i==null) return;
    if (editwindow==null) {
      editwindow=new EditWindow();
    }
    editwindow.show(i);
  }
  String template=null;

    public void doneEditInfo(Info i) {
	    saveObjects();
	    try {
		if (i.getType().equals("polygon")) {
		    parsePolygons(new Integer(i.getAttribute("map")).intValue());
		}
		needRepaint(true);
	    } catch(Exception e) {
		e.printStackTrace();
		showStatus(i18n.trans("Fehler beim Aktualisieren von")+" "+i.getTypeName());
	    }

    }
  public void lpc(int node) {
    if (isApplet) return;
    String res=null;
    boolean update=false;
    char[] dest=new char[4096];
    StringBuffer sb=new StringBuffer();
    int read_res=0;
    try {
      Reader r=new BufferedReader(new InputStreamReader(new URL(path+makeLpcFilename(aktmap,node)+".c").openStream()));
      while ((read_res=r.read(dest,0,4096))!=-1) {
	sb.append(dest,0,read_res);
      }
      System.out.println(sb);
      res=sb.toString();
      r.close();
      update=true;
    } catch(Exception except) {
      except.printStackTrace();
    if (template==null) {
      try {
	sb=new StringBuffer();
	Reader r=null;
	try {
	  r=new BufferedReader(new FileReader((new URL(mapsdir+maps[aktmap].name+"/map"+aktmap+"_template.c")).getFile()));
	} catch(Exception ex2) {
	  ex2.printStackTrace();
	  try {
	    r=new BufferedReader(new FileReader((new URL(mapsdir+mapname+"/template.c")).getFile()));	
	  } catch(Exception ex3) {
	    ex3.printStackTrace();
	    r=new BufferedReader(new FileReader(new URL(mapsdir+"template.c").getFile()));	
	  }
	}
      while ((read_res=r.read(dest,0,4096))!=-1) {
	sb.append(dest,0,read_res);
      }
      System.out.println(sb);
      template=sb.toString();
      r.close();
      } catch(IOException e) {e.printStackTrace();}
    }
      res=new String(template);
      update=false;
    }
    System.out.println("pre change "+res);

    Node n=null;
    //    int off=-1, off2;

    StringBuffer temp=new StringBuffer();
    StringBuffer cmd=new StringBuffer();
    if ((n=(Node)info.get(new Integer(node)))!=null) {
      String[] keys=n.getKeys();
      for (int i=keys.length-1;i>=0;i--) {
	if (keys[i].startsWith("f_")) {
	  String abs_path=(String)opt.get(keys[i]);
	  if (abs_path==null) abs_path="";
	  else if (!abs_path.endsWith("/")) abs_path+="/";
	  res=replaceLpc(res,keys[i],abs_path+n.getAttribute(keys[i]),update);
	} 
	else
	  res=replaceLpc(res,keys[i],n.getAttribute(keys[i]),update);
      }
    }
    res=replaceLpc(res,"filename",makeLpcFilename(aktmap,node),update);
    Hashtable h=(Hashtable)special.get(new Integer(node));
    if (h!=null) {
      StringBuffer ex=new StringBuffer();
      Exit exit;
      for (Enumeration e=h.keys();e.hasMoreElements();) {
	exit=getExit((Exit)h.get(e.nextElement()));
	if (exit.dirname==null) {
	  ex.append("\tAddExit(\"");
	  ex.append(dir_long_lookup[exit.dir]);
	  ex.append("\",\"");
	  ex.append("../"+makeLpcFilename(exit.map,exit.to));
	  ex.append("\");\n");
	} else {
	  int off2=exit.dirname.indexOf(' ');
	  ex.append("\tAddCmd(\"");
	  ex.append((off2>-1)?exit.dirname.substring(0,off2):exit.dirname);
	  ex.append("\",\"");
	  ex.append(exit.dirname.replace(' ','_'));
	  //	  ex.append("../"+makeLpcFilename(exit.map,exit.to));
	  ex.append("\");\n");
	  cmd.append(exit.dirname.replace(' ','_')+"(string str) {\n");
	  cmd.append("\tif ("+((off2>-1)?"str==\""+exit.dirname.substring(off2+1)+"\"":"!str")+") {\n\t this_player()->move(_MakePath(\""+"../"+makeLpcFilename(exit.map,exit.to)+"\"), M_GO, \"\");\n\treturn 1;\n\t}\n\treturn 0;\n");
	  cmd.append("}\n");
	}
      }
      res=replaceLpc(res,"exits",ex.toString(),update);
    }
    res=replaceLpc(res,"commands",cmd.toString(),update);
    System.out.println(res);
    try {
      System.out.println(writepath+" / "+maps[aktmap].name);
      File f=new File(writepath+maps[aktmap].name);
      if (!f.exists()) {
	System.out.println("creating directory");
	if (!f.mkdir()) System.out.println("Error when creating directory");
      }
      FileWriter fw=new FileWriter(writepath+makeLpcFilename(aktmap,node)+".c");
      fw.write(res);
      fw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public String makeLpcFilename(int map, int node) {
    checkMap(map);
    Node n=(Node)maps[map].info.get(new Integer(node));
    String id=null;
    if (n!=null && (id=n.getAttribute("name")).length()>0) {
      id=id.replace('/','_');
      return maps[map].name+"/"+maps[map].name+"_"+id;
    }
    else return maps[map].name+"/"+maps[map].name+"_"+node;
  }
  protected String replaceLpc(String res,String key, String replacement, boolean update) {
    System.out.println("key "+key+ "update "+update);
    StringBuffer temp=new StringBuffer();
    if (replacement==null || replacement.equals("")) return res;
    int off, off2, anf, end;
    System.out.println(update);
    if (update) {
      off=res.indexOf("// $"+key+"$");
      off2=res.indexOf("/*"+key+"*",off);
      if (off!=-1 && off2!=-1) {
	temp.append(res.substring(0,off));
	temp.append(res.substring(off2));
	res=temp.toString();
      } else System.out.println(res);
      temp=new StringBuffer();
    }
    off=res.indexOf("/*"+key+"*");
    if (off!=-1) {
      anf=off;
      temp.append(res.substring(0,off));
      off+=key.length()+3;
      off2=res.indexOf("#"+key+"#",off);
      if (off2!=-1) {
	temp.append("// $"+key+"$ der Bereich bis vor *"+key+"* wird beim update geloescht!!!\n");
	temp.append(res.substring(off,off2));
	off2+=key.length()+2;
	temp.append(replacement);
	off=res.indexOf("*/",off2);
	temp.append(res.substring(off2,off));
	//	temp.append("// $"+key+"$");
	end=off+2;
	temp.append("\n"+res.substring(anf,end));
	temp.append(res.substring(off+2));
	res=temp.toString();
      }
    }
    return res;
  }
  protected int calcDir(int p1, int p2) {
    int[] d=new int[3];
    for (int i=0;i<3;i++) {
      d[i]=points[p2][i]-points[p1][i];
      if (d[i]!=0) d[i]/=Math.abs(d[i]);
      System.out.print(d[i]+" ");
    }
    boolean found=true;
    for (int i=calcDir_directions.length-1;i>=0;i--) {
      found=true;
      for (int j=0;j<3;j++)
	if (calcDir_directions[i][j]!=d[j]) found=false;
      if (found) return dir_lookup[i];
    }
    return 0;
  }
  public Node getNode(int id) {
    return (Node)info.get(new Integer(id));
  }
  public void pointInfo(int id) {
    if (points[id][4]>-2) {
	Node node=(Node)info.get(new Integer(id));
    String cmdPrefix="g "+id+COMMAND_SEPARATOR;
    smallinfo.clear();
    smallinfo.append(I18N("Knoten")+": ");
    if (node!=null) 
      smallinfo.append(node.getAttribute("name"),"g "+id);
    smallinfo.append(" (m:"+aktmap+",p:"+id+",c:"+points[id][0]+","+points[id][1]+","+points[id][2]+")"+'\n',"g "+id);
    Hashtable h=(Hashtable)special.get(new Integer(id));
    Exit e=null;
    if (h!=null) {
        smallinfo.append(I18N("Ausgaenge")+": ");
      for (Enumeration en=h.keys();en.hasMoreElements();) {
	e=getExit((Exit)h.get(en.nextElement()));
	
	smallinfo.append(e.getDirName()+e.getLongExitString()+(en.hasMoreElements()?", ":"\n"),cmdPrefix+e.getDirName());
      }
    }
    if ((points[id][3]&MARK_MASK)!=0) smallinfo.append(I18N("Markiert")+" ",cmdPrefix+"sf marked <1>");
    if ((points[id][3]&HOUSE_MASK)!=0) smallinfo.append(","+I18N("halber Masstab")+" ",cmdPrefix+"sf building <1>");
    if ((points[id][3]&VISITED_MASK)!=0) smallinfo.append(","+I18N("betreten")+" ",cmdPrefix+"sf visited <1>");
    if ((points[id][3]&PARA_MASK)!=0) smallinfo.append(","+I18N("Parawelt"),cmdPrefix+"sf para <1>");
    if ((points[id][3]&(HOUSE_MASK|VISITED_MASK|MARK_MASK|PARA_MASK))!=0) smallinfo.append("\n");
    if (node!=null) {
	appendInfo(node,"short",cmdPrefix);
	appendInfo(node,"ways",cmdPrefix);
	appendInfo(node,"npc",cmdPrefix);
	appendInfo(node,"teleport",cmdPrefix);
	appendInfo(node,"tank",cmdPrefix);
	appendInfo(node,"pub",cmdPrefix);
	appendInfo(node,"exa",cmdPrefix);
	appendInfo(node,"shop",cmdPrefix);
	appendInfo(node,"house",cmdPrefix);
	appendInfo(node,"port",cmdPrefix);
	appendInfo(node,"color",cmdPrefix);
	appendInfo(node,"type",cmdPrefix);
	appendInfo(node,"icon",cmdPrefix);
	Hashtable misc=node.misc;
	if (misc!=null)
	    for (Enumeration en=misc.keys();en.hasMoreElements();) 
		appendInfo(node,(String)en.nextElement(),cmdPrefix);
	smallinfo.append(node.showAttribute("long",i18n));     
    }
    smallinfo.setText();
    }
  }
    protected void appendInfo(Node node,String attrib,String cmdPrefix) {
	smallinfo.append(node.showAttribute(attrib,i18n),cmdPrefix+"ai "+attrib+" <"+node.getAttribute(attrib)+">");
    }
    /*
    public void CBMaps() {
      cb_maps.removeAll();
      String mapstring="";
      for (int i=0;i<numMaps;i++) {
	mapstring=maps[i].name+" ("+maps[i].id+")";
	cb_maps.add(mapstring+((maps[i].points==null)?"":" *"));
      }
      cb_maps.select(aktmap);

    }
    */
  public void listMaps() {
     smallinfo.clear();
     smallinfo.append("Maps:\n");
    String mapstring="";
    for (int i=0;i<numMaps;i++) {
      smallinfo.append(maps[i].name+" ("+maps[i].id+")\n","sm "+i);
    }
    smallinfo.setText();
    showStatus(I18N("Anzeige aller Karten"));
  }
  public void createMap(String name) {
    if (getMapId(name)<0) {
      Map[] tmp=new Map[numMaps+1];
      System.arraycopy(maps,0,tmp,0,numMaps);
      maps=tmp;
      Map m=new Map();
      maps[numMaps]=m;
      m.aktpoint=0;
      m.id=numMaps;
      m.name=name;
      m.menu=new CheckboxMenuItem(name,true);
      m.numPoints=1;
      m.points=new int[5][5];
      for (int i=1;i<5;i++)
	m.points[i][4]=-2;

      m.points[0][0]=0;
      m.points[0][1]=0;
      m.points[0][2]=0;
      m.points[0][4]=-1;
      m.info=new HashMap();
      m.named=new HashMap();
      m.special=new HashMap();

      m.min=new float[2];
      m.min[0]=0;
      m.min[0]=0;

      m.level=0;
      m.numExits=0;
      m.exits=new int[5][3];
      for (int i=0;i<m.exits.length;i++) m.exits[i][0]=-1;
      m.maxborder=new float[3][2];
      for (int j=0;j<3;j++) {
	m.maxborder[j][0]=-0.3f;
	m.maxborder[j][1]=0.3f;
      }
      numMaps++;
      showStatus(I18N("Erzeugen: Karte")+" "+m);

      saveMap(numMaps-1);
      saveMaps();
      //      CBMaps();
    }
  }
  public void deletePoint(int pt) {
    points[pt][4]=-2;
    Integer ipos=new Integer(pt);
    Hashtable h=(Hashtable)special.get(ipos);
    special.remove(ipos);
    info.remove(ipos);
    if (h!=null)
      for (Enumeration en=h.keys();en.hasMoreElements();) 
	exits[((Exit)h.get(en.nextElement())).exitspos][0]=-1;
    showStatus(i18n.trans("Löschen: Knoten mit Wegen: $0 aus Karte: $1",new String[] {""+pt,maps[aktmap].toString()}));
    Hashtable w=null;
    Exit e=null;
    Object key=null;
    for (int i=0;i<numMaps;i++) {
      for (Enumeration en=maps[i].special.keys();en.hasMoreElements();) {
	w=(Hashtable)maps[i].special.get(en.nextElement());
	for (Enumeration en2=w.keys();en2.hasMoreElements();) {
	  key=en2.nextElement();
	  e=getExit((Exit)w.get(key));
	  if (e.map==aktmap && e.to==pt) {
	    w.remove(key);
	    exits[e.exitspos][0]=-1;
	  }
	}
      }
    }
    pt=0;
    if (h!=null)
    for (Enumeration en=h.keys();en.hasMoreElements();) {
      e=getExit((Exit)h.get(en.nextElement()));
      if (e.map==aktmap) {
	pt=e.to;
	break;
      }
    }
    aktpoint=pt;
    showMovedPoint();
    needRecalc();
  }
  public boolean workReferenced(int def, StringTokenizer st, NodeWorker worker) {
    boolean res=false;
    int LEVEL=1, MAP=2, ALL=3, REGION=4, AKT=5, NAMED=6, MARKED=7;
    int which=def;
    int[] reg=null;
    String param=null;
    if (st.hasMoreTokens()) {
      param=st.nextToken().toLowerCase();
      if ("marked".startsWith(param)) which=MARKED;
      if ("level".startsWith(param)) which=LEVEL;
      else if ("named".startsWith(param)||"nodes".startsWith(param)) which=NAMED; 
      else if ("map".startsWith(param)) which=MAP;
      else if ("all".startsWith(param)) {
	which=ALL;
      }
      else if ("region".startsWith(param)) {
	which=REGION;
	reg=new int[3];
	int count=0;
	while (count<3 && st.hasMoreTokens()) 
	  try {
	    reg[count++]=Integer.parseInt(st.nextToken())*2;
	  } catch(NumberFormatException e) {
	  }
      }
    }
    if (which==NAMED) {
      int pt=-1, pt2=-1;
      while(st.hasMoreTokens()) {
	String named=st.nextToken();
	int off=named.indexOf('-');
	if (off>-1) {
	  pt=getPointId(aktmap,named.substring(0,off));
	  pt2=getPointId(aktmap,named.substring(off+1));	  
	  System.out.println("NAMED: "+named+" from "+pt+" to "+pt2);
	  if (pt!=-1 && pt2!=-1) 
	      for (int i=pt;i<=pt2;i++)
		  worker.modifyNode(i, NAMED);
	}
	pt=getPointId(aktmap,named);
	if (pt!=-1)
	    worker.modifyNode(pt, NAMED);
      }
      return true;
    }
    if (which==AKT) {
      	  worker.modifyNode(aktpoint, AKT);
      return true;
    }
    if (which==ALL) {
      Map m=null; int mcount=0;
      pushMap(aktmap);
      mcount=0; 
      res=true;
      do {
	checkMap(mcount);
	m=maps[mcount++];
	for (int n=0;n<m.numPoints;n++) {
	  worker.modifyNode(n, mcount-1,ALL);
	}
      } while (mcount<numMaps);
    }
    else {
      int x=points[aktpoint][0],y=points[aktpoint][1];
      for (int n=0;n<numPoints;n++) {
	if (which==LEVEL && points[n][2]==level
	    || which==MARKED && (points[n][3] & MARK_MASK)!=0
	    || which==REGION && Math.abs(points[n][0]-x)<=reg[0]
	    && Math.abs(points[n][1]-y)<=reg[1]
	    && Math.abs(points[n][2]-level)<=reg[2]
	    || which==MAP) {
	  res=true;
	  worker.modifyNode(n, which);
	}}}	  

    return res;
  }
  public int[] parseOptions(String[] s) {
    StringTokenizer st=new StringTokenizer(s[0]);
    st.nextToken();
    String token="";
    String pointstring=null;
    int dir=-1, pt=-1, map=-1, length=1, force=0, force_node=0, add_second=0;
    boolean optionparsed=true;
    while (optionparsed) {
      optionparsed=false;
      token=null;
      if (st.hasMoreTokens()) {
	token=st.nextToken();
	if (token.startsWith("-d")) {
	  token=token.substring(2);
	  int off=-1;
	  if ((off=token.indexOf("#"))!=-1) {
	    oneway=true;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("!"))!=-1) {
	    force=1;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("&"))!=-1) {
	    add_second=1;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("$"))!=-1) {
	    force_node=1;
	    token=token.substring(0,off)+token.substring(off+1);
	  }
	  if ((off=token.indexOf("*"))!=-1) {
	    try {
	      length=Integer.parseInt(token.substring(0,off));
	    } catch (Exception e) { }
	    token=token.substring(off+1);
	  }
	  dir=getDirectionId(token);
	  //	  System.out.println("dir "+dir);
	  if (dir==0) oneway=true;
	  optionparsed=true;
	} else
	  if (token.startsWith("-p")) {
	    pointstring=token.substring(2);
	    System.out.println("pointstring " +pointstring);
	    optionparsed=true;
	  } else
	    if (token.startsWith("-m")) {
	      map=getMapId(token.substring(2));
	      if (map!=aktmap) oneway=true;
	      optionparsed=true;
	    }
      }
    }
    if (map==-1) map=aktmap;
    if (pt==-1 && pointstring!=null) {
      pt=getPointId(map,pointstring);
      if (map==aktmap && pt==-1) {
	try {
	  Integer.parseInt(pointstring);
	} catch(Exception e) {
	  for (map=0;map<numMaps;map++) {
	    if (map==aktmap) map++;
	    pt=getPointId(map,pointstring);
	    if (pt!=-1) break;
	  }
	  if (pt==-1) map=aktmap;
	  else
	    if (map!=aktmap) oneway=true;
	}
      }
      System.out.println("getPointId() "+pt);
    }
    if (st.hasMoreTokens()) token+=" "+Util.allTokens(st);
    if (pt==-1 && dir==-1) {
      return null;
    } 
    else {
      s[0]=token;
      int[] opt={dir,map,pt,length,force,force_node,add_second};
      return opt;
    }
  }
  int OPT_DIR=0, OPT_MAP=1,OPT_DEST=2, OPT_LENGTH=3, OPT_FORCE=4, OPT_FORCE_NODE=5, OPT_SECOND=6;
  
  public int getOldDirectionId(String dirname) {
    int dir=-1;
    try {
      dir=Integer.parseInt(dirname);
      if (dir>=dir_lookup.length || dir<0) dir=-1;
      else dir=dir_lookup[dir];
    } catch(NumberFormatException e) {
      Integer i=(Integer)name_dir_lookup.get(dirname);
      //      if (i!=null) dir=lookup_dir[i.intValue()];
      if (i!=null) dir=i.intValue();
    }
    //    System.out.println("dir "+dir);
    return dir;
  }
  public int getDirectionId(String dirname) {
    int dir=-1;
    try {
      dir=Integer.parseInt(dirname,10);
      if (dir>=dir_name_lookup.length || dir<0) dir=-1;
      //      else dir=dir_lookup[dir];
    } catch(NumberFormatException e) {
      Integer i=(Integer)name_dir_lookup.get(dirname);
      System.out.println("lookup "+dirname);
      //      if (i!=null) dir=lookup_dir[i.intValue()];
      if (i!=null) dir=i.intValue();
    }
    //    System.out.println("dir "+dir);
    return dir;
  }
  public int getMapId(String name) {
    int map=-1;
    try {
      map=Integer.parseInt(name);
      if (map>=numMaps || map<0) map=-1;
    } catch(NumberFormatException e) {
      for (int i=0;i<numMaps;i++) 
	if (maps[i]!=null && maps[i].name.equals(name)) {
	  map=i;
	  break;
	}
    }
    return map;
  }
  public int[] getAllPointId(int map, String dest) {
    int i=map;
    int pt=getPointId(i,dest);
    try {
      Integer.parseInt(dest);
    } catch(Exception e) {
      if (pt==-1) {
	for (i=0;i<numMaps;i++) {
	  if (i!=map) {
	    pt=getPointId(i,dest);
	    if (pt!=-1) break;
	  }
	}
      }
    }
    int[] res={pt,i};
    return res;
  }
  public int getPointId(int map,String point) {
    int pt=-1;
    checkMap(map);
    if (map==aktmap) pushMap(aktmap);
    try {
      pt=Integer.parseInt(point);
      if (pt>=maps[map].numPoints || pt<0) pt=-1;
    } catch(NumberFormatException e) {
      Integer i=(Integer)maps[map].named.get(point);
      if (i!=null) pt=i.intValue();
    }
    return pt;
  }
  public boolean movePoint(String d,Vector undo) {
    Exit e=getExit(aktpoint,d);
    //      System.out.println(e.toString());
    if (e==null) return false;

      clearSequence=true;
      int oldmap=aktmap;
      if (e.map==aktmap || (e.map!=aktmap && switchMap(e.map))) {
	if (!noundo && undo!=null) {
	  if (e.map!=oldmap) {
	    undo.addElement("sm "+aktmap);
	  }
	  undo.addElement("g "+aktpoint);
	}
	aktpoint=e.to;
	if (oldmap!=aktmap) center(null,undo);
	if (from_socket) points[aktpoint][3]|=VISITED_MASK;
	else {
	  String command=null;
	  if (e.command!=null) {
	      command=e.command;
	      int off=command.indexOf("#");
	      if (off>-1) {
		  send_socket(command.substring(0,off));
		  command=command.substring(off+1);
	      } else send_socket("0 "+e.command);
	  }
	  send_socket("0 "+e.getDirName());
	  if (command!=null) {
	      send_socket("0 "+command);
	  }
	  Node n=(Node)info.get(new Integer(aktpoint));
	  if (n!=null && n.getAttribute("ways").length()>0) {
	    send_socket("0 /lp "+n.getAttribute("ways"));
	  }
	}
	showStatus(I18N("Bewegung: aktueller Knoten")+": "+aktpoint);

	showMovedPoint();
      }
      return true;
  }
 
  public void showMovedPoint() {
    needRepaint();
    pointInfo(aktpoint);
    if (level!=points[aktpoint][2]) {
      level=points[aktpoint][2];
      showStatus(I18N("Bewegung: neue Z-Ebene")+" "+level);
      needRecalc();
    } 

    if (!insideBorder(aktpoint)) {
      min[0]=points[aktpoint][0];
      min[1]=points[aktpoint][1];
      needRecalc();
    } else {

      for (int j=0;j<2;j++) {
	if (points[aktpoint][j]-border[j][0]<2) {
	  min[j]-=dsize[j]*0.2f;
	  needRecalc();
	}
	if (border[j][1]-points[aktpoint][j]<2) {
	  needRecalc();
	  min[j]+=dsize[j]*0.2f;
	}
      }
    }
    //	repaint();
  }
  public Exit getExit(Exit e) {
      if (e!=null) e=e.getExit();
      return e;
  }
  public Exit getExit(int pt,String d) {
    if (d==null) return null;
    Hashtable h=(Hashtable)special.get(new Integer(pt));
    if (h!=null) {
      Exit e=getExit((Exit)h.get(d));
      if (e==null) {
	  int dir=getDirectionId(d);
	  if (dir==-1) {
	      System.out.println("wrong direction name"+d);
	      return null;
	  }
	  e=(Exit)h.get(dir_name_lookup[dir]);
	  e=getExit(e);
	  if (e==null) {
	      for (Enumeration en=h.keys(); en.hasMoreElements();) {
		  e=getExit((Exit)h.get(en.nextElement()));
		  //		  System.out.println(e+" dir "+dir+" dirname "+d);
		  if (e.dirname!=null && e.dir==dir) {
		      //		      System.out.println("Erfolg: "+e);
		      break;
		  } else e=null;
	      }
	  }
      }
      return getExit(e);
    } else System.out.println("hashtable null");
    return null;
  }
  public Exit getBackExit(Exit e) {
    String bkname=getDirNameDir(dir_anti_lookup[e.dir]);
    return getExit(e.to,bkname);
  }
  public Exit isOneWay(Exit e) {
    Exit ex=getBackExit(e);
    return (ex==null || ex.to!=e.from)?null:ex;
  }
  protected int findFreePoint() {
    int i;
    for (i=0;i<numPoints;i++)
      if (points[i][4]==-2) break;
    if (i>points.length-1) renewPoints();
    if (i==numPoints) numPoints++;
    System.out.println("numPoints "+numPoints+" new point "+i);
    return i;
  }
  protected int addNode(int x, int y, int z, boolean force,  Vector undo) {
    return addNode(x,y,z,force,-1,undo);
  }
  protected int addNode(int x, int y, int z, boolean force, int dest, Vector undo) {
    int np=-1;
    int res=-1;
    System.out.println(x+","+y+","+z+force+forcenode+findPoint(x,y,z));
	if (((res=findPoint(x,y,z))==-1) ||force || forcenode) {
	  if (dest==-1 || (res!=-1 && forcenode)) np=findFreePoint(); else np=dest;
	  forcenode=false;
	  if (!noundo && undo!=null && res==-1) {
	    undo.addElement("DP "+np);
	  }
	  showStatus(I18N("Erzeugen: neuer Knoten")+": "+np+"("+x+","+y+","+z+") "+I18N("Map")+": "+maps[aktmap]);
	  System.out.println("Adding point"+np+" : "+x+", "+y+", "+z);
	  points[np][0]=x;
	  points[np][1]=y;
	  points[np][2]=z;
	  points[np][3]=0;
	  points[np][4]=-1;
	  if (res!=-1) {
	    points[np][3]|=OVERLAP_MASK;
	  }
	  recalcBorder(np);
	  needRecalc(); //repaint();
	} else np=findPoint(x,y,z);
	return np;
  }
  protected int addPoint(int akt, int dest, int map,int dir, String dn, boolean forceexit, boolean forcenode, int length, boolean addsecond, Vector undo) {
    int np;
    String dirname=(dn==null)?getDirNameDir(dir):dn;
    Exit e=getExit(akt,dirname);
    System.out.println("addpoint: "+e+" forceexit "+forceexit+" forcenode "+forcenode);
    if (e!=null && !checkDimension(e.to,e.map)) addsecond=true;
    if (e==null || forceexit || forcenode || addsecond) {
      clearSequence=true;
      System.out.println("Adding point");
      np=numPoints;
      System.out.println(dir+" "+map+" "+dest+" "+length);
      boolean fpara=false, fnopara=false;
      int flags=0;
      flags=points[akt][3];
      fpara=(flags & PARA_MASK)!=0;
      fnopara=(flags & NOPARA_MASK)!=0;
      if (addsecond && e!=null && e.getSecond()==-1) {
	addsecond=false;
	if (fpara==fnopara) {
	  flags=points[e.to][3];
	  fpara=(flags & PARA_MASK)!=0;
	  fnopara=(flags & NOPARA_MASK)!=0;
	  if (fpara!=fnopara) {
	    forcenode=true;
	    addsecond=true;
	  } 
	} 
      } else addsecond=false;
      int house=1;
      if ((points[akt][3] & HOUSE_MASK)!=0) house=2;
      //      System.out.println("points[dest][4]==-2"+points[dest][4]);
      if (dir>0 && map==aktmap && (dest==-1 || points[dest][4]==-2)) {
	int x=points[akt][0]+directions[dir][0]*length/house;
	int y=points[akt][1]+directions[dir][1]*length/house;
	int z=points[akt][2]+directions[dir][2]*length;
        addUndo(undo,"g "+akt);
	np=addNode(x,y,z,forcenode,dest,undo);
	if (addsecond) {
	  if (!fpara) points[np][3]|=PARA_MASK;
	  if (!fnopara) points[np][3]|=NOPARA_MASK;
	  points[np][3]&=~OVERLAP_MASK;
	} else {
	  if (fpara) points[np][3]|=PARA_MASK;
	  if (fnopara) points[np][3]|=NOPARA_MASK;
	}
	if (house!=1) points[np][3]|=HOUSE_MASK;
	if (!insideBorder(np)) {
	  if (!noundo && undo!=null) {
	    undo.addElement("center view "+min[0]+" "+min[1]+" "+level);
	  }
	  min[0]+=directions[dir][0]*length/2f/house;
	  min[1]+=directions[dir][1]*length/2f/house;
	  needRecalc();
	}
      } else np=dest;
      if (addsecond && e!=null) {
	e.addSecond(np);
	if (!oneway && dir>0 && dir<dir_anti_lookup.length && map==aktmap) {
	  addExit(np,akt,map,dir_anti_lookup[dir],dn,forceexit,undo);
	}
	oneway=false;
      } else
	addBothExits(akt,np,map,dir,dn,forceexit,undo);
    } else np=e.to;
    return np;
  }
  public void addBothExits(int p1, int p2, int map,int dir, String dn, boolean force, Vector undo) {
    //    addUndo(undo,"g "+p1);
      if (addExit(p1,p2,map,dir,dn,force,undo) && !oneway && dir>0 && dir<dir_anti_lookup.length && map==aktmap) {
	addExit(p2,p1,map,dir_anti_lookup[dir],dn,force,undo);
      }
      oneway=false;
  }
  public int findFreeExit() {
    int i;
    for (i=0;i<exits.length;i++)
      if (exits[i][0]==-1) return i;
    if (i==exits.length) {
      renewExits();
    }
    System.out.println("findFreeExit "+i);
    return i;
  }
  public int findFreeExit(int map) {
    int i;
    for (i=0;i<maps[map].exits.length;i++)
      if (maps[map].exits[i][0]==-1) return i;
    if (i==maps[map].exits.length) {
      renewExits(map);
    }
    System.out.println("findFreeExit "+i);
    return i;
  }
  public boolean addExit(int p1, int p2, int map,int dir, String dn, boolean force, Vector undo) {
    Exit e=null;
    int ne=numExits;
    if ((e=isExit(p1,p2,map,dir,dn))==null || force ) {
      if (e==null) {
	ne=findFreeExit();
	System.out.println("addexit newExit"+ne+"numExits "+numExits);
	if (ne>=numExits) numExits=ne+1;
      } else ne=e.exitspos;
      if (!noundo && undo!=null) {
	if (e!=null && (exits[ne][1]!=p2 || exits[ne][2]!=dir)) {
	  undo.addElement("asp -d"+dir+" -p"+p2+" -m"+map+" "+((dn!=null)?dn:""));
	}
	if (dir==0 || map!=aktmap)
	  undo.addElement("DE -d"+dir+" -p"+p2+" -m"+map+((dn!=null)?" "+dn:""));
	else
	undo.addElement("DE #"+((dn!=null)?dn:getDirNameDir(dir)));
	undo.addElement("g "+p1);
      }
      exits[ne][0]=p1;
      exits[ne][1]=p2;
      exits[ne][2]=dir;
      Hashtable h=(Hashtable)special.get(new Integer(p1));
      if (h==null) h=new HashMap();
      e=new Exit(ne,dir,p1,p2,map,dn);
      h.put(e.getDirName(),e);
      special.put(new Integer(p1),h);
      showStatus(I18N("Erzeugen: Ausgang")+": "+e);
      needRepaint();
      return true;
    }
    return false;
  }


  public void replaceExit(int p1, int p2, int map, int dir, String dirname,Vector undo) {
    Exit e=isExit(p1,p2,map,dir,dirname);
    if (e==null) {
      addExit(p1,p2,map, dir,dirname,false,undo);
    } else {
	showStatus(I18N("Ueberschreiben: Ausgang")+": "+e);
	if (!noundo && undo!=null) {
	undo.addElement("g "+p1);
	undo.addElement("DE #"+((dirname!=null)?dirname:getDirNameDir(dir)));
	undo.addElement("asp -d"+e.dir+" -p"+e.to+" -m"+e.map+" "+((e.dirname!=null)?dirname:""));
      }
      exits[e.exitspos][0]=p1;
      exits[e.exitspos][1]=p2;
      exits[e.exitspos][2]=dir;
      e.from=p1;
      e.to=p2;
      e.map=map;
      e.dir=dir;
      e.dirname=dirname;
      e.updateAngle();
	showStatus(I18N("Ueberschreiben: mit")+": "+e);
    }
  }

  public Exit deleteExit(int p1, int p2, int map, int dir, String dirname) {
    Exit e=isExit(p1,p2,map,dir,dirname);
    if (e!=null) {
      exits[e.exitspos][0]=-1;
      Hashtable h=(Hashtable)special.get(new Integer(p1));
      h.remove(e.getDirName());
      needRepaint();
      if (e.exitspos==numExits-1) numExits--;
      showStatus(I18N("Loeschen: Ausgang")+": "+e);
    }
    return e;
  }

  protected Exit isExit(int p1, int p2, int map,int dir, String dirname) {
    Hashtable h=(Hashtable)special.get(new Integer(p1));
    String d=dirname;
    if (h==null) return null;
    if (dirname==null) {
      d=getDirNameDir(dir);
    }
    Exit e=null;
    if (d!=null) e=getExit((Exit)h.get(d));
    if (e==null || (p2>-1 && e.to!=p2) || (map>-1 && e.map!=map)) return null;
    return e;
  }
  protected void recalcBorder(int p) {
    boolean changed=false;
    for (int j=0;j<3;j++) {
      if (maxborder[j][0]>points[p][j]) {
	maxborder[j][0]=points[p][j];
	changed=true;
      }
      if (maxborder[j][1]<points[p][j]) {
	maxborder[j][1]=points[p][j];
	changed=true;
      }
    }
    if (changed) {
	//      delta=Math.max(Math.abs(maxborder[0][0]-maxborder[0][1]),Math.abs(maxborder[1][0]-maxborder[1][1]));
	//      if (delta==0) delta=0.6f;
      for (int j=0; j<2;j++) {
	  border[j][0]=min[j]-dsize[j]/2;
	  border[j][1]=min[j]+dsize[j]/2;
      }
      System.out.println("Border: x "+maxborder[0][0] +", "+ maxborder[0][1] +" ,y "+ maxborder[1][0] +", "+ maxborder[1][1] + " delta "+delta);
    }
  }
  protected void renewExits() {
    if (numExits>=exits.length-1) {
      System.out.println("renewing Exits"+exits.length+" "+numExits);
      int [][]tmp=new int[exits.length+10][3];
      System.arraycopy(exits,0,tmp,0,exits.length);
      for (int i=exits.length;i<exits.length+10;i++)
	tmp[i][0]=-1;
      exits=tmp;
      System.out.println("renewing Exits len"+exits.length+" num "+numExits);
    }
  }
  protected void renewPoints() {
    if (numPoints>points.length-10) {
      System.out.println("renewing Points"+points.length);
      int [][]tmp=new int[numPoints+10][5];
      System.arraycopy(points,0,tmp,0,points.length);
      for (int i=points.length;i<tmp.length;i++)
	tmp[i][4]=-2;
      points=tmp;
    }
  }

  protected void renewExits(int map) {
    Map amap=maps[map];
    if (amap.numExits>=amap.exits.length-1) {
      System.out.println("renewing Exits"+amap.exits.length+" "+amap.numExits);
      int [][]tmp=new int[amap.exits.length+10][3];
      System.arraycopy(amap.exits,0,tmp,0,amap.exits.length);
      for (int i=amap.exits.length;i<amap.exits.length+10;i++)
	tmp[i][0]=-1;
      amap.exits=tmp;
      System.out.println("renewing Exits"+amap.exits.length);
    }
  }
  protected void calcModifiersFromV(Point p1, Point p2) {
    float x0=vToR(p1.x,0);
    float x1=vToR(p2.x,0);
    float y0=vToR(p1.y,1);
    float y1=vToR(p2.y,1);
    float d=Math.max(Math.abs(x0-x1),Math.abs(y0-y1));
    float mx=(x0+x1)/2;
    float my=(y0+y1)/2;
    min[0]=mx;
    min[1]=my;
    delta=d;
  }

    public void statusHelp(String s) {
	if (s==null) statusLine.setText("");
	else statusLine.setText(s);
    }

  protected boolean showStatus=true;
  StringBuffer tempStatus=new StringBuffer();
  int statusSize=0;
  public void showStatus(String s) {
    System.out.println(s);
    if (statusSize>40*160) {
	String temp=status.getText();
	temp=temp.substring(temp.length()/2);
	statusSize=temp.length();
	status.setText(temp);
    }
    statusSize+=s.length()+1;
    if (status==null) tempStatus.append(s+"\n");
    else {
	if (tempStatus!=null) {
	    status.append(tempStatus.toString());
	    tempStatus=null;
	}
	if (showStatus && !noundo) {
	    status.append(s+'\n');
	    try {
		status.setCaretPosition(Integer.MAX_VALUE);
	    } catch(Exception e) {}
	}
    }
  }
  public void calcfPoints() {
      try {
    if (needrecalc) {
      long time=System.currentTimeMillis();
      needrecalc=false;
      // reset der bisherigen Werte, Referenzen loeschen

      if (fpoints!=null)
	for (int i=fcount-1;i>=0;i--) {
	  if (points[(int)fpoints[i][3]][4]>-1)
	    points[(int)fpoints[i][3]][4]=-1;
	  fpoints[i][4]=-1;
	  fpoints[i][3]=-1;
	}
      if ((fpoints==null) || (fpoints.length!=numPoints)) {
	fpoints=new float[numPoints][5];
      }
      fcount=0;

      csize[0]=dim.width-5;
      csize[1]=dim.height-5;

      for (int j=0;j<2;j++) {
	zoom[j]=csize[j]/delta;
      }
      zoom[0]=Math.min(zoom[0],zoom[1]);
      zoom[1]=-zoom[0];
      dsize=new float[2];
      if (csize[0]<csize[1]) {
	dsize[0]=delta;
	dsize[1]=delta*(csize[1]/csize[0]);
      } else {
	dsize[0]=delta*(csize[0]/csize[1]);
	dsize[1]=delta;
      }
      for (int j=0; j<2;j++) {
	border[j][0]=min[j]-dsize[j]/2;
	border[j][1]=min[j]+dsize[j]/2;
      }
      System.out.println("Border: x "+border[0][0] +", "+ border[0][1] +" ,y "+ border[1][0] +", "+ border[1][1]);

      dpt=rToV(1,0)-rToV(0,0);
System.out.println("dpt "+dpt);	  
      dp=new int[40];
      for (int i=0;i<dp.length;i++) {
	dp[i]=(int)(i/35.714287f*2.0*dpt);
	//	idp[i]=(int)(i/35.714287f*dpt);
      }
      fcount=0 ;
      int pt=-1;
      for (int i=numPoints-1;i>=0;i--) {
	if (insideBorder(i)) {
	  pt=i;
	  if ((pt>-1)&&(points[pt][4]==-1)) {
	    fpoints[fcount][3]=pt; // gegenseitige Referenzen
	    fpoints[fcount][4]=1;  // zentrale Knotene
	    points[pt][4]=fcount;
	    fcount++;
	  }}}
      painttrans[0]=dim.width/2; painttrans[1]=dim.height/2;
      pt=-1;
      int ldiff=0;
      for (int i=0;i<fcount;i++) {
	pt=(int)fpoints[i][3];
	ldiff=points[pt][2]-level;
	float rnd=0;
	for (int j=0;j<2;j++) {
	  rnd=0;
	  if ((points[pt][3]&OVERLAP_MASK)!=0 && ldiff==0) rnd=(float)Math.random()*dp[10];
	  fpoints[i][j]=(points[pt][j]-min[j])*zoom[j]+painttrans[j]+(ldiff*dp[5]*((j==0)?1:-2))+rnd;
	}
	calcExits(pt);
      }
    System.out.println("Neuberechnung: "+(System.currentTimeMillis()-time)+" ms");
    }
      }catch(Exception e) {}
  }
  protected int findPoint(int x, int y, int z) {
    for (int i=numPoints-1;i>=0;i--)
      if (points[i][4]>-2 && checkDimension(i,aktmap) && points[i][0]==x && points[i][1]==y && points[i][2]==z) return i;
    return -1;
  }
  protected boolean insideBorder(int pt) {
    int x=points[pt][0],y=points[pt][1];
    //    System.out.println("Border: x "+border[0][0] +", "+ border[0][1] +" ,y "+ border[1][0] +", "+ border[1][1]+" x "+x+" y "+y);     
    if (points[pt][2]!=level) return false;
    if (  x<border[0][0]
	  ||x>border[0][1]
	  ||y<border[1][0]
	  ||y>border[1][1])
      return false;
    //     System.out.println(x+", "+y);
    return true;
  }
  protected void calcExits(int p) {
    int pt=-1;
    Hashtable h=null;
    Exit e=null;
    int bpt=points[p][4];
    if (fpoints[bpt][4]==1) {
      h=(Hashtable)special.get(new Integer(p));
      if (h!=null) {
	for (Enumeration en=h.keys();en.hasMoreElements();) {
	  e=getExit((Exit)h.get(en.nextElement()));
	  if (e.dir>0 && e.map==aktmap) {
	    pt=e.to;
	    if (points[pt][4]==-1) {
	      fpoints[fcount][3]=pt; // gegenseitige Referenzen
	      fpoints[fcount][4]=(points[pt][2]!=level)?3:2; // nur fuer Wege notwendig
	      points[pt][4]=fcount;
	      fcount++;
	    } 		
	    }
	  }
      }}}
  public void saveMap(int id) {
    if (maps[id]==null || maps[id].points==null || isApplet ) return;
    if (id==aktmap) pushMap(aktmap);
    try {
      Map m=maps[id];
      FileWriter fw=new FileWriter(writepath+"map"+id+".info");
      Integer key=null;
      Node n=null;
      for (Enumeration en=m.info.keys();en.hasMoreElements();) {
	key=(Integer)en.nextElement();
	fw.write(""+key.intValue()+"\n");
	fw.write(((Node)m.info.get(key)).toString());
      }
      fw.close();

      fw=new FileWriter(writepath+"map"+id+".coord");
      fw.write("Version 0.4\n");
      fw.write(""+m.numPoints+'\n'+"3"+'\n');
      for (int i=0;i<m.numPoints;i++) {
	fw.write("pt "+((m.points[i][4]==-2)?-i-1:i)+" "+m.points[i][3]);
	fw.write(""+'\n'+m.points[i][0]+'\n'+m.points[i][1]+'\n'+m.points[i][2]+'\n');
      }
      fw.close();
      Exit e=null;
      Hashtable h;
      fw=new FileWriter(writepath+"map"+id+".exits");
      int ex=0;
      System.out.println("numExits "+m.numExits+" exits.length "+m.exits.length);
      for (int i=0;i<m.numExits;i++) 
	if (m.exits[i][0]!=-1) ex++;
      fw.write(""+(ex)+'\n');
      System.out.println("exits "+ex);
      //      System.out.println(m.special.toString());
      for (int i=0;i<m.numPoints;i++) {
	h=(Hashtable)m.special.get(new Integer(i));
	if (h!=null) 
	  for (Enumeration en=h.keys();en.hasMoreElements();) {
	    e=getExit((Exit)h.get(en.nextElement()));
	    if (e!=null){
	      fw.write(""+e.from+" ");
	      fw.write(""+((e.map==id)?e.to:-e.to-1));
	      if (e.to2!=-1) fw.write("/"+e.to2);
	      if (e.map!=id) fw.write(" "+e.map);
	      fw.write(" "+e.dir+((e.dirname!=null)?" "+e.dirname:"")+'\n');
	      if (e.door!=null) fw.write("§door:"+e.door+"\n");
	      if (e.command!=null) fw.write("§command:"+e.command+"\n");
	      if (e.color!=null) fw.write("§color:"+e.color+"\n");
	    }
	    else System.out.println("e==null:"+i);
	  } else System.out.println("h==null:"+i);
      }
      showStatus(I18N("Speichern : Map")+" "+m);
      fw.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  public void readMaps() {
    try {
System.out.println(path);
      DataInputStream dis=new DataInputStream(new BufferedInputStream((new URL(path+"map")).openStream()));
      String l,token;
      numMaps=Integer.parseInt(dis.readLine());
System.out.println("readMaps() numMaps "+numMaps);
      maps=new Map[numMaps];
      StringTokenizer st;
      int id;
      String name;
      Menu menu=null;
      int bis_id=0;
      for (int i=0;i<numMaps;i++) {
	l=dis.readLine();
	try {
	  st=new StringTokenizer(l);
	  id=Integer.parseInt(st.nextToken());
	  name=st.nextToken();
	  if (id % 20 ==0) {
	      if (id+19>=numMaps) bis_id=numMaps-1; else bis_id=id+19;
	      menu=new Menu(i18n.trans("Karten von $0 bis $1",new Object[] {new Integer(id), new Integer(bis_id) }));
	      map_menues.addElement(menu);
	      if (map_menu!=null) 
		  map_menu.add(menu);
	  }
	  maps[id]=new Map();
	  maps[id].id=id;
	  maps[id].name=name;
	  maps[id].menu=new CheckboxMenuItem(maps[id].toString());
	  maps[id].menu.setActionCommand("sm "+id);
	  maps[id].menu.addItemListener(commandAL);
	  menu.add(maps[id].menu);
	} catch(Exception e) {
	    e.printStackTrace();
	}
      }
	dis.close();
    } catch(Exception e) {e.printStackTrace();}
    listMaps();
  }
  Vector map_menues=new Vector();

  void loadInfo() {
      DataInputStream dis=null;
      try {
	try {
	dis=new DataInputStream(new BufferedInputStream((new URL(path+"default_"+I18N("de")+".info")).openStream())); 
	System.out.println(path+"default_"+I18N("de")+".info");
	} catch (Exception e) {
	  dis=new DataInputStream(new BufferedInputStream((new URL(mapsdir+"default_"+I18N("de")+".info")).openStream())); 
	System.out.println(mapsdir+"default_"+I18N("de")+".info");
	}
	String l=null;
	String ref=null;
	Hashtable h_types=new HashMap();
	Hashtable h_type=new HashMap();
	int off;
	while ((l=dis.readLine())!=null) {
	  if (l.indexOf("§")!=-1) {
	    if (ref!=null) {
	      h_types.put(ref,h_type);
	      h_type=new HashMap();
	    }
	    ref=l.substring(0,l.indexOf("§"));
	  } else {
	    off=l.indexOf(' ');
	    if (off>-1) {
	      h_type.put(l.substring(0,off),l.substring(off+1));
	    }
	  }
	}
	if (ref!=null) h_types.put(ref,h_type);
      Info.setDefaults(h_types);

      // debug
      System.out.println(h_types);

      }
      catch(Exception e) {
	e.printStackTrace();
      }
  }
  void loadObjects() {
    try {
      DataInputStream dis=new DataInputStream(new BufferedInputStream((new URL(path+"map.info")).openStream()));
      Info.loadAll(dis);
      dis.close();

      // debug
      /*
      Info i=new Info("Knoten","Testknoten");
      i.setAttribute("exa","ja");
      i.setAttribute("short","Eine Short");
      i.setAttribute("testatt","23415345");
      System.out.println(i.getShortList());
      System.out.println(i.toString());

      */
      System.out.println(Info.all);
    } catch(Exception e) {
	System.out.println("Error when loading Objects : "+e.getMessage());
	//      e.printStackTrace();
    }
  }
  public void saveObjects() {
    if (isApplet) return;
    try {
      FileWriter fw=new FileWriter(writepath+"map.info");
      Info.saveAll(fw);
      fw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public void saveMaps() {
    if (isApplet) return;
    try {
      FileWriter fw=new FileWriter(writepath+"map");
      fw.write(""+numMaps+"\n");
      for (int i=0;i<numMaps;i++) {
	fw.write(""+maps[i].id+" "+maps[i].name+'\n');
      }
      fw.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public void readMap(int id) {
    try {
      DataInputStream dis=new DataInputStream(new BufferedInputStream((new URL(path+"map"+id+".coord")).openStream()));
      String l;
      Map m=maps[id];
      if (maps[id]==null) {
	m=new Map();
	maps[id]=m;
      }
      float version=0;
      m.aktpoint=0;
      l=dis.readLine();
      if (l.equals("Version alpha1")) {version=0.1f;l=dis.readLine();}
      if (l.equals("Version 0.2")) {version=0.2f;l=dis.readLine();}
      if (l.equals("Version 0.3")) {version=0.3f;l=dis.readLine();}
      if (l.equals("Version 0.4")) {version=0.4f;l=dis.readLine();}
      m.numPoints=Integer.parseInt(l);
      System.out.println("numPoints "+m.numPoints);
      int dimensions=Integer.parseInt(dis.readLine());
      m.points=new int[m.numPoints+5][5];
      for (int i=0;i<m.numPoints+5;i++)
	m.points[i][4]=-2;
      m.info=new HashMap();
      m.named=new HashMap();
      m.special=new HashMap();
      border=new float[3][2];
      m.min=new float[2];
      int pos=0;
      int d;
      for (int i=0;i<m.numPoints;i++) {
	l=dis.readLine();
	StringTokenizer st=new StringTokenizer(l);
	st.nextToken(); // pt
	pos=Integer.parseInt(st.nextToken()); // pos
	if (pos<0) {
	  pos=-pos-1;
	  m.points[pos][4]=-2;
	} else m.points[pos][4]=-1;
	if (version>0.1 && st.hasMoreTokens()) { // flags
	  m.points[pos][3]=Integer.parseInt(st.nextToken());
	}
	for (int j=0;j<dimensions;j++) {
	  d=Integer.parseInt(dis.readLine());
	  // v alpha1
	  if (version<0.1) d*=2;
	  m.points[pos][j]=d;
	  if ((border[j][0]>d)||(pos==0)) border[j][0]=d;
	  if ((border[j][1]<d)||(pos==0)) border[j][1]=d;
	}
	//      System.out.println(pos+": ("+ points[pos][0] + ", " + points[pos][1] + ")");
	if (m.points[pos][4]!=-2) m.points[pos][4]=-1;
	pos++;
      }
      m.maxborder=new float[3][2];
      for (int j=0;j<3;j++) {
	m.maxborder[j][0]=border[j][0]-0.3f;
	m.maxborder[j][1]=border[j][1]+0.3f;
      }
      for (int j=0;j<2;j++) {
	m.min[j]=m.points[0][j];
      }
      //            m.delta[0]=Math.max(Math.abs(m.maxborder[0][0]-m.maxborder[0][1]),Math.abs(m.maxborder[1][0]-m.maxborder[1][1]));

      dis.close();
      dis=new DataInputStream(new BufferedInputStream((new URL(path+"map"+id+".exits")).openStream()));
      m.numExits=Integer.parseInt(dis.readLine());
      m.exits=new int[m.numExits+5][3];
      for (int i=0;i<5;i++) m.exits[m.numExits+i][0]=-1;
      StringTokenizer st;
      int newmap=-1;
      loadmap=id;
      Exit e=null;
      for (pos=0;pos<m.numExits;pos++) {
	try {
	  l=dis.readLine();
	  if (version>=0.4f)
	  while (l.startsWith("§")) {
	    st=new StringTokenizer(l.substring(1),":");
	    String att=st.nextToken();
	    if (e!=null) {
	    if (att.equals("door")) e.door=Util.allTokens(st).substring(1);
	    if (att.equals("command")) e.command=Util.allTokens(st).substring(1);
	    if (att.equals("color")) e.color=Util.allTokens(st).substring(1);	    
	    }
	    l=dis.readLine();
	  }
	  int second=-1;
	  newmap=id;
	  st=new StringTokenizer(l);
	  String token=null;
	  token=st.nextToken();
	  m.exits[pos][0]=Integer.parseInt(token);

	  token=st.nextToken();
	  int off=token.indexOf("/");
	  if (off!=-1) {
	    second=Integer.parseInt(token.substring(off+1));
	    token=token.substring(0,off);
	  }
	  m.exits[pos][1]=Integer.parseInt(token);

	  if (m.exits[pos][1]<0) {
	    m.exits[pos][1]=-m.exits[pos][1]-1;
	    token=st.nextToken();
	    newmap=getMapId(token);
	    System.out.println(pos+". "+token+" mapid "+newmap);
	  }

	  token=st.nextToken();
	  //	  System.out.print(token+" "+dir_names[Integer.parseInt(token)]+" ");
	  m.exits[pos][2]=(version>0)?getDirectionId(token):getOldDirectionId(token);
	  //	  System.out.println(m.exits[pos][2]+" "+dir_name_lookup[m.exits[pos][2]]);
	  int pt=m.exits[pos][0];	
	  Integer ipos=new Integer(pt);
	  Hashtable h=(Hashtable)m.special.get(ipos);
	  if (h==null) h=new HashMap();
	  e=new Exit(pos,m.exits[pos][2],m.exits[pos][0],m.exits[pos][1],newmap,(st.hasMoreTokens())?Util.allTokens(st):null);
	  if (second!=-1) e.addSecond(second);
	  try { 
	    h.put(e.getDirName(),e);
	  } catch(NullPointerException ex) {
	    System.out.println(e);

	  }
	  m.special.put(ipos,h);
	} catch(Exception ex) {
	  ex.printStackTrace();
	}    
      }
      loadmap=-1;
      dis.close();
      try {
      dis=new DataInputStream(new BufferedInputStream((new URL(path+"map"+id+".info")).openStream()));
      l=null;
      Node n=null;
      Integer ipos=null;
      String att=null;
      while ((l=dis.readLine())!=null) {
	try {
	  ipos=new Integer(l);
	  //	  System.out.println();
	  //	  System.out.print(ipos+" ");
	  n=new Node();
	  m.info.put(ipos,n);
	  m.points[ipos.intValue()][3]|=VISITED_MASK;
	} catch (NumberFormatException ex) {
	  st=new StringTokenizer(l);
	  att=st.nextToken();
	  String value=(st.hasMoreTokens()?Util.allTokens(st):null);
	  n.setAttribute(att,value);
	  if (value!=null) if (att.equals("name")) m.named.put(value,ipos);
	  //	  System.out.print(n.getAttribute(att)+" ");
	}
      }
      dis.close();
      } catch(Exception ex) {ex.printStackTrace();}
      showStatus(I18N("Laden")+" : "+I18N("Karte")+" "+m);
      parsePolygons(id);

      //      for (int i=0;i<m.numPoints;i++) System.out.println(m.points[i][3]);
      //      for (int i=0;i<m.numExits;i++) System.out.println(m.exits[i][2]);

      //      System.out.println(m.info.toString());
      //    System.out.println(m.switch_map_points.toString());
      needRecalc(true);
      //    repaint();
      //      CBMaps();
      m.menu.setState(true);
    } catch(Exception e) {e.printStackTrace();}
  }
  public boolean checkMap(int id) {
    if (maps[id]==null || maps[id].points==null) readMap(id);
    if (maps[id]!=null && maps[id].points!=null) {
      return true;
    }
    return false;
  }
  public void pushMap(int id) {
    if (id<numMaps) {
      if (maps[id]!=null) {
	needRecalc();
	maps[id].aktpoint=aktpoint;
	maps[id].level=level;
	maps[id].numPoints=numPoints;
	maps[id].numExits=numExits;
        maps[id].points=points;
        maps[id].polygons=polygons;
	if (fpoints!=null)
	  for (int i=fcount-1;i>=0;i--) 
	    if (maps[id].points[(int)fpoints[i][3]][4]>-2)
	      maps[id].points[(int)fpoints[i][3]][4]=-1;
	maps[id].exits=exits;
	maps[id].special=special;
	maps[id].maxborder=maxborder;
	maps[id].min=min;
	maps[id].info=info;
	maps[id].named=named;
      }
    }}
  public void popMap(int id) {
    if (id<numMaps) {
      if (maps[id]!=null) {
	aktpoint=maps[id].aktpoint;
	points=maps[id].points;
	exits=maps[id].exits;
	special=maps[id].special;
	maxborder=maps[id].maxborder;
	min=maps[id].min;
	info=maps[id].info;
	named=maps[id].named;
	level=maps[id].level;
	numPoints=maps[id].numPoints;
	numExits=maps[id].numExits;
	polygons=maps[id].polygons;
      }}
  }
  public void readAllMaps() {
    for (int i=0;i<numMaps;i++)
      checkMap(i);
  }
  public boolean switchMap(int id) {
    if (id<numMaps) {
      pushMap(aktmap);
      if (checkMap(id)) {
	  showStatus(I18N("Bewegung: Umschalten von Map")+" :"+maps[aktmap]);
	popMap(id);
	aktmap=id;
	fpoints=null; fcount=0 ;
	needRecalc();
	showStatus(I18N("Bewegung: zu Map")+" :"+maps[id]);
	setTitle(maps[id].toString());
	return true;
      }
    }
    return false;
  }

    public void replaceSelection(TextComponent tf, String text) {
	String s=tf.getText();
	tf.setText(s.substring(0,tf.getSelectionStart())+text+
		s.substring(tf.getSelectionEnd()));
    }

  public void drawRect(Graphics g,Point lo,Point ru) {
    Point x,y;
    //    System.out.println("Knoten "+lo.toString()+" "+ru.toString());
    x=new Point(Math.min(lo.x,ru.x),Math.min(lo.y,ru.y));
    y=new Point(Math.abs(lo.x-ru.x),Math.abs(lo.y-ru.y));
    g.drawRect(x.x,x.y,y.x,y.y);
  }
  public float vToR(float arg, int xy) {
    return (arg-painttrans[xy])/zoom[xy]+min[xy];
  }
  public float vToR(int arg, int xy) {
    return ((float)arg-painttrans[xy])/zoom[xy]+min[xy];
  }
  public float rToV(float arg, int xy) {
    return (arg-min[xy])*zoom[xy]+painttrans[xy];
  }

  public int[][] rToVi(float[][] arg) {
    int[][] res=new int[arg[0].length][arg.length];
    for (int i=0;i<arg.length;i++)
      for (int xy=0;xy<2;xy++)
	res[xy][i]=(int)((arg[i][xy]-min[xy])*zoom[xy]+painttrans[xy]);
    return res;
  }

  public float[][] rToV(float[][] arg) {
    float[][] res=new float[arg[0].length][arg.length];
    for (int i=0;i<arg.length;i++)
      for (int xy=0;xy<2;xy++)
	res[xy][i]=(arg[i][xy]-min[xy])*zoom[xy]+painttrans[xy];
    return res;
  }
  public int rToVi(float arg, int xy) {
    int res=(int)((arg-min[xy])*zoom[xy]+painttrans[xy]);
    System.out.print(res+" ");
    return res;
  }
  Hashtable getPattern,getPattern2;

  int get=-1;
  int getpos=0;

  final static int GET_NODE=1, GET_NODE_MAP=2, GET_COORD=3, GET_SCREEN=4, GET_NODE_SIMPLE=5, GET_PREC_DEC=8, GET_MANY=16, GET_DELIM_KOMMA=32;

  void initInteractive() {
    getPattern=new HashMap();
    getPattern.put("poly",new Integer(GET_PREC_DEC | GET_MANY | GET_COORD | GET_DELIM_KOMMA));
    getPattern.put("an",new Integer(GET_COORD));
    getPattern.put("mp",new Integer(GET_COORD));
    getPattern.put("ep",new Integer(GET_COORD));
    getPattern.put("asp",new Integer(GET_NODE_MAP));
    getPattern.put("text",new Integer(GET_COORD | GET_PREC_DEC));
    getPattern.put("si",new Integer(GET_NODE_SIMPLE));
    getPattern.put("center node",new Integer(GET_NODE_SIMPLE));    
    getPattern.put("center screen",new Integer(GET_SCREEN));    
    getPattern.put("center view",new Integer(GET_COORD));    
    getPattern.put("center",new Integer(GET_NODE_SIMPLE));    
    getPattern.put("mark nodes",new Integer(GET_NODE_SIMPLE | GET_MANY));


    getPattern2=new HashMap();
    getPattern2.put("<coords.>",new Integer(GET_PREC_DEC | GET_MANY | GET_COORD | GET_DELIM_KOMMA));
    getPattern2.put("<coords>",new Integer(GET_MANY | GET_COORD | GET_DELIM_KOMMA));
    getPattern2.put("<coord>",new Integer(GET_COORD));
    getPattern2.put("-p<pnode> -m<map>",new Integer(GET_NODE_MAP));
    getPattern2.put("<coord.>",new Integer(GET_COORD | GET_PREC_DEC));
    getPattern2.put("<node>",new Integer(GET_NODE_SIMPLE));
    getPattern2.put("<nodes>",new Integer(GET_NODE_SIMPLE | GET_MANY));
  }

  float[][] getPoints=new float[30][3];
  int numGetPoints=0;
  int getOff=-1;
  void checkGet() {
    String text=null;
    if (get==-1) {
      text=commandline.getText();
      /*
      getpos=text.indexOf("<");
      if (getpos==-1) getpos=text.length()-1;
      */
      text=text.toLowerCase();
      String key=null;
      Integer i=null;
	for (Enumeration en=getPattern2.keys();en.hasMoreElements();) {
	  key=(String)en.nextElement();
	  if ((getOff=text.indexOf(key))>-1) {
	    i=(Integer)getPattern2.get(key);
	    getpos=getOff;
	    getOff=getpos+key.length();
	    break;
	  }
	}
      if (i==null) {
      for (Enumeration en=getPattern.keys();en.hasMoreElements();) {
	key=(String)en.nextElement();
	if ((getOff=text.indexOf(key))==0) {
	  i=(Integer)getPattern.get(key);
	  getpos=text.length()-1;
	  getOff=getpos;
	  break;
	}
      }
      }
      if (i!=null) {
	get=i.intValue();
	numGetPoints=0;
      } else get=-1;
    }
    if (get!=-1) {
      String delim=((get & GET_DELIM_KOMMA)!=0)?",":" ";
      //      text=commandline.getText();
      if (!((get & GET_MANY)!=0)) {
	  //	text=text.substring(0,getpos);
	numGetPoints=0;
      }
      float x,y,z;
      int node=(int)ipoint[6];
      switch (get & 7) {
      case GET_SCREEN:
	      text=Math.round(ipoint[0])+delim+Math.round(ipoint[1]);
	      getPoints(ipoint[3],ipoint[4],ipoint[5]);
	      break;
      case GET_COORD:
	  if ((get & GET_PREC_DEC)!=0) {
	      x=Math.round(ipoint[3]*10f)/10f;
	      y=Math.round(ipoint[4]*10f)/10f;
	      text=x+delim+y+delim+ipoint[5];
	  }
	  else {
	      x=Math.round(ipoint[3]);
	      y=Math.round(ipoint[4]);
	      text=(int)x+delim+(int)y+delim+(int)ipoint[5];
	  }
	  getPoints(x,y,ipoint[5]);
	break;
      case GET_NODE:
	  if (node==-1) return;
	  text="-p"+node;
	  getPoints(points[node][0],points[node][1],points[node][2]);
	  break;
      case GET_NODE_SIMPLE:
	  if (node==-1) return;
	  text=""+node;
	  getPoints(points[node][0],points[node][1],points[node][2]);
	  break;
      case GET_NODE_MAP:
	  if (node==-1) return;
	  int map=(int)ipoint[2];
	  text="-p"+node+" -m"+map;
	      getPoints(maps[map].points[node][0],
			maps[map].points[node][1],
			maps[map].points[node][2]);
	  break;
      }
      String s=commandline.getText();
      System.out.println(getpos);
      System.out.println(s.substring(0,getpos));
      System.out.println(getOff);
      System.out.println(s.substring(getOff));
      commandline.setText(s.substring(0,getpos)+" "+text+s.substring(getOff));
      getOff=getpos+text.length()+1;
      if ((get & GET_MANY)!=0) 
	  getpos+=(text.length()+1);
      System.out.println("getOffNew"+getOff+" texlen "+text.length());

    }
  }
  public void getPoints(float x, float y, float z) {
      getPoints[numGetPoints][0]=x;
	  getPoints[numGetPoints][1]=y;
	  getPoints[numGetPoints][2]=z;
      numGetPoints++;
      needRepaint();
}
  class Mouser extends MouseAdapter implements MouseMotionListener {
    int x,y,button;
    Point anf, akt, last;
    boolean drawRect=false;
    final int SHOW=1, ZOOM=2, MARK=3, MOVE=4, POPUP=5;
    int state=SHOW;
    boolean marked=false;
    public void drawMarkRect(Graphics g) {
      if (drawRect) {
	if (last.x!=akt.x || last.y!=akt.y) {
	g.setXORMode(Color.white);
	g.setColor(Color.black);
	drawRect(g,anf,last);
	last.x=akt.x;
	last.y=akt.y;
	drawRect(g,anf,last);
	g.setPaintMode();
	}
      }
    }
    int clickCount;
    boolean popup=false;
    private void evalEvent(MouseEvent evt) {
      x=evt.getX();
      y=evt.getY();
      if ((evt.getModifiers() & evt.BUTTON1_MASK) != 0) button=1;
      if ((evt.getModifiers() & evt.BUTTON2_MASK) != 0) button=2;
      if ((evt.getModifiers() & evt.BUTTON3_MASK) != 0) button=3;
      if (evt.getClickCount()==2) button=3;
      clickCount=evt.getClickCount();
      popup=evt.isPopupTrigger();
    }
    private void initState(MouseEvent evt) {
      state=SHOW;
      if ((evt.getModifiers() & (evt.CTRL_MASK|evt.SHIFT_MASK)) !=0 && popup) state=POPUP;
      else
      if ((evt.getModifiers() & evt.CTRL_MASK) !=0) state=ZOOM;
      else
	if ((evt.getModifiers() & evt.SHIFT_MASK) !=0) state=MARK;
      switch (state) {
      case POPUP: System.out.println("POPUP");
      case ZOOM: System.out.println("ZOOM");
      case MARK: System.out.println("MARK");
      case SHOW: System.out.println("SHOW");
      }
    }
    public void mousePressed(MouseEvent evt) {
      evalEvent(evt);
      initState(evt);
      ((Component)evt.getSource()).requestFocus();
      if (button==1 && (state==ZOOM || state==MARK)){
	if (lang==EN)	  
	  showStatus("Mouse: Start: "+((state==ZOOM)?"zoom":"mark"));
	else
	  showStatus("Mouse: Beginn: "+((state==ZOOM)?"Zoomen":"Markieren"));
	drawRect=true;
	marked=false;
      }
      anf=new Point(x,y);
      last=new Point(x,y);
      akt=new Point(x,y);
    }
      int xmax,xmin, ymax, ymin;
    public void mouseDragged(MouseEvent evt) {
      evalEvent(evt);
      if ((state==ZOOM||state==MARK)&&((akt.x!=x)||(akt.y!=y))) {
	akt.x=x;
	akt.y=y;
	if (state==MARK) {
	    xmin=Math.min(akt.x,anf.x);
	    ymin=Math.min(akt.y,anf.y);
	    xmax=Math.max(akt.x,anf.x);
	    ymax=Math.max(akt.y,anf.y);
	  needrepaint=false;
	  for (int i=0;i<fcount;i++) {
	    if (fpoints[i][0]>=xmin && fpoints[i][0]<=xmax &&
		fpoints[i][1]>=ymin && fpoints[i][1]<=ymax &&
		points[(int)fpoints[i][3]][2]==level)
		{
		    if ((points[(int)fpoints[i][3]][3]&PRE_MARK_MASK) == 0) {
			points[(int)fpoints[i][3]][3]|=PRE_MARK_MASK;
			marked=true;
			needRepaint();
		    }
		}
	    else {
		if ((points[(int)fpoints[i][3]][3]&PRE_MARK_MASK) != 0) {
		    points[(int)fpoints[i][3]][3]&=~PRE_MARK_MASK;
		    needRepaint();
		}
	    }
	  }
	} 
	mapcanvas.repaint();
      }
      if (state==SHOW && button==1 && ((akt.x!=x)||(akt.y!=y))) {
	  float dx=vToR(x,0)-vToR(akt.x,0);
	  float dy=vToR(y,1)-vToR(akt.y,1);
	  if (Math.abs(dx)>=0.5f || Math.abs(dy)>=0.5f) {
	      min[0]+=dx;
	      min[1]+=dy;
	      akt.x=x;
	      akt.y=y;
	      needRecalc();
	  }
      }
      wake();
    }
    public void mouseMoved(MouseEvent evt) {

      room=null;
      int sp=findPoint(evt.getX(),evt.getY());
      if (sp!=showpoint) {
	showpoint=sp;
      }

    }
    public void mouseReleased(MouseEvent evt) {
      evalEvent(evt);
      if (state==ZOOM) {
	if (button==3) {
	    zoom(UNZOOM_COUNT,1,null);
	    showStatus(I18N("Mouse: View Unzoom"));
	    needRecalc();
	  } else
	    if (button==1) {
		showStatus(I18N("Mouse: Ende Zoomen"));
	      if (Math.abs(akt.x-anf.x)>5 && Math.abs(akt.y-anf.y)>5) {
		calcModifiersFromV(akt,anf);
		needRecalc();
	      }
	      state=SHOW;
	    }} else
      if (state==SHOW) {
	if (button==1) {
	  ipoint[0]=x;
	  ipoint[1]=y;
	  ipoint[2]=aktmap;
	  ipoint[3]=vToR(x,0);
	  ipoint[4]=vToR(y,1);
	  ipoint[5]=level;
	  ipoint[6]=findPoint(x,y);
	  checkGet();
	  for (int i=0;i<ipoint.length;i++)
	    System.out.print(ipoint[i]+", ");
	  System.out.println();
	  showStatus(I18N("Info")+": "+I18N("Bildschirm")+" ("+x+","+y+") "+I18N("Knoten")+" "+ipoint[6]+" ("+ipoint[3]+","+ipoint[4]+","+ipoint[5]+") "+I18N("Karte")+":"+maps[aktmap]);
	  if (ipoint[6]>-1) pointInfo((int)ipoint[6]);
	}
	if (button==3 || button==1 && clickCount>1) {
	  int pt=findPoint(x,y);
	  if (pt>-1) {
	    aktpoint=pt;
	    showStatus(I18N("Bewegung: neuer Knoten")+": "+pt);
	    showMovedPoint();
	  }
	}
      }
      if (state==MARK) {
	if (button==3) for (int i=0;i<numPoints;i++) points[i][3]&=~MARK_MASK & ~ PRE_MARK_MASK;
	else {
	  if (!marked) {
	    int pt=findPoint(x,y);
	    if (pt!=-1) {
	      if ((points[pt][3] & MARK_MASK)!=0) points[pt][3]&=~ MARK_MASK; else points[pt][3]|=MARK_MASK;
	      needRepaint();
	      mapcanvas.repaint();
	    }
	  } else {
	    int anz=0;
	    for (int i=0;i<numPoints;i++) 
	      if ((points[i][3] & PRE_MARK_MASK)!=0) {
		points[i][3]|=MARK_MASK;
		points[i][3]&=~PRE_MARK_MASK;
	      }
	  }
	}
	int anz=0;
	for (int i=0;i<numPoints;i++)
	  if (points[i][4]>-2 && ((points[i][3] & MARK_MASK)!=0))
	    anz++;
	drawRect=false;
	showStatus(I18N("Markierte Knoten")+" "+anz);
      }
      wake();
    }
    public int findPoint(int x,int y) {
      float diff,min=1000;
      int sp=-1;
      for (int i=0;i<fcount;i++) {
	if (!checkDimension((int)fpoints[i][3],aktmap)) continue;
	diff=(x-fpoints[i][0])*(x-fpoints[i][0])+
	  (y-fpoints[i][1])*(y-fpoints[i][1]);
	if  (diff<min) {
	  min=diff;
	  sp=(int)fpoints[i][3];
	}}
      return sp;
    }
  }
}
class Info {
  String type;
  int id;
  String name;
  static int maxid=0;
  static Hashtable types; // type -> hashtable mit defaults fuer typ und Bezeichnern in der aktuellen sprache default->bezeichner
  Hashtable attributes;
  Hashtable defKeys;
  static Hashtable all=new HashMap(), alltyped=new HashMap();
  // alltyped Hashtable mit Types auf Vectoren mit den Objekten abgebildet
  public static void setDefaults(Hashtable h) {
    types=h;
  }
  
    public String getType() {
	return type;
    }
  public Info(String type, String name) throws Exception {
    defKeys=(Hashtable)types.get(type);
    if (defKeys==null) throw new Exception("no type "+type+" defined");
    this.name=name;
    this.id=maxid++;
    this.type=type;
    addInfo(this);
  }
  private Info(String type, int id, String name) throws Exception {
    defKeys=(Hashtable)types.get(type);
    if (defKeys==null) throw new Exception("no type "+type+" defined");
    this.name=name;
    this.id=id;
    if (id>=maxid) maxid=id+1;
    this.type=type;
    addInfo(this);
  }
  public Info(String textual) {
    String l=textual;
    StringTokenizer st=new StringTokenizer(l);
    try {
    if (l.startsWith("§§ ")) {
      st.nextToken();
      Info i=new Info(st.nextToken(" ").trim(),st.nextToken("§").trim());
      while(st.hasMoreTokens()) {
	System.out.println(i);
	i.setAttribute(st.nextToken(" ").trim(),st.nextToken("§"));
      }
      //      i.setAttribute(st.nextToken(" "),Util.allTokens(st).substring(1));
    }
    } catch(Exception e) {
      e.printStackTrace();
      /*
      if (lang==EN) 
	showStatus("Creation of object failed: "+l.substring(0,20));
      else
	showStatus("Erzeugung des Objekts fehlgeschlagen: "+l.substring(0,20));
      */
    }
  }
  static void addInfo(Info i) {
    Vector v=(Vector)alltyped.get(i.type);
    if (v==null) {
      v=new Vector();
      alltyped.put(i.type,v);
    }
    v.addElement(i);
    all.put(new Integer(i.id),i);
  }
  static void deleteInfo(Info i) {
    Vector v=(Vector)alltyped.get(i.type);
    if (v!=null) {
      v.remove(i);
    }
    all.remove(new Integer(i.id));
  }
  public static RangedStringBuffer getInfo(String sid) {
    try {
      Info i=(Info)all.get(Integer.valueOf(sid));
      return new RangedStringBuffer(i.toString(),"ei "+sid);
    } catch(Exception e) {
      // zwei Parameter?
        int off=sid.indexOf(' ');
	String l;
	RangedStringBuffer sb=new RangedStringBuffer();
	//  zwei Parameter, l=Typ
	if (off>-1) l=sid.substring(0,off);
	// ansonsten nur l=Typ als Parameter
	else l=sid;
	// Typ gefunden
	if (alltyped.containsKey(l)) {
	  // Hashtable dieses Typs holen
	  Vector v=(Vector)alltyped.get(l);
	  // l = Substring des Namens
	  if (off>-1) l=sid.substring(off+1);
	  // wenn nur ein Element enthalten wird es zurueckgegeben
	  if (v.size()==1) return getInfo(""+((Info)(v.get(0))).id);
	  else {
	    String value=null;
	    // ansonsten check aller enthaltenen elemente auf uebereinstimmung mit namenssubstring
	    for (Enumeration en=v.elements();en.hasMoreElements();) {
	      Info i=(Info)en.nextElement();
	      // wenn nur ein Parameter sammeln
	      if (off==-1)
		sb.append(i.getName()+", ","oi "+i.id);
	      // ansonsten Beginn des Namens ueberpruefen
	      else if (off>-1)
		if (i.getName().startsWith(l))
		  // Kurzinfo anhaengen
		  sb.append(i.getShortList()+"\n","ei "+i.id);
	      // bei Gleichheit Kurzinfo am Anfang einfuegen
		else if (i.getName().equals(l))
		  sb.insert(0,i.getShortList()+"\n","ei "+i.id);
	    }
	  }
	} else
	  // keine Parameter
	  // fuer alle Typen Anzahl der vorhandenen Objekte anzeigen
	  for (Enumeration en=types.keys();en.hasMoreElements();) {
	    l=(String)en.nextElement();
	    sb.append(l,"oi "+l);
	    if (alltyped.containsKey(l)) sb.append(": "+((Vector)alltyped.get(l)).size(),"oi "+l);
	    sb.append("\n");
	  }
	return sb;
    }
  }
  public static Vector getType(String cid) {
    return (Vector)alltyped.get(cid);
  }
  public static Info get(String sid) {
    if (sid.startsWith("$")) {
      sid=sid.substring(1);
      int off=sid.indexOf(' ');
      if (off>-1)
	try {
	return new Info(sid.substring(0,off),sid.substring(off+1));
	} catch(Exception e) {
	  e.printStackTrace();
	}
      return null;
    }
    try {
      Info i=(Info)all.get(Integer.valueOf(sid));
      return i;
    } catch(Exception e) {
        int off=sid.indexOf(' ');
	String l;
	StringBuffer sb=new StringBuffer();
	// zwei Parameter: Typ + Name
	if (off>-1) l=sid.substring(0,off);
	// ein Parameter nur Typ
	else l=sid;
	Vector v=null;
	Enumeration en=null;
	if (alltyped.containsKey(l)) {
	  // Vector des Typs
	  v=(Vector)alltyped.get(l);
	  // zwei Parameter: weiter mit Name
	  if (off>-1) {
	    l=sid.substring(off+1);
	    en=v.elements();
	  }
	  // nur ein Element - > Anzeigen
	  if (v.size()==1) return (Info)(v.get(0));
	  // Enumeration ueber Vector
	} else {
	  en=all.elements();
	  l=sid;
	} // ueber komplette eingabe suchen
	if (en!=null) { 
	    Info i=null, res=null;
	    //	    String value=null;
	    for (;en.hasMoreElements();) {
	      i=(Info)en.nextElement();
	      //	      value=((Info)en.nextElement()).name;
	      if (off>-1)
		if (i.name.startsWith(l))
		  if (res==null) res=i; // best match
		  else if (i.name.equals(l)) {
		    res=i;
		    return res;
		  }
	    }
	    return res;
	// unter allen Knoten den mit dem am besten passenden Namen suchen
	// am besten mit oben verbinden
	}
    }
    return null;
  }
    public void setName(String name) {
	this.name=name;
    }

  public void setAttribute(String name, Object value) {
    if (value!=null) {
	if (attributes==null) attributes=new HashMap();
	attributes.put(name,value);
    } else {
      if (attributes!=null) attributes.remove(name);
    }
  }

  public String getAttribute(String name) {
    Object res=null;
    if (attributes!=null) 
      res=attributes.get(name);
    return (res==null)?"":res.toString();
  }
  public String getAttributeString(String name) {
    if (attributes==null) return "";
    Object val=attributes.get(name);
    String value=null;
    if (val!=null) value=val.toString();
    if (value==null) return "";
    String label=(String)defKeys.get(name);
    if (label==null) return name+": "+value+"\n";
    else  return label+": "+value+"\n";
  }

  public String getName() {
    return name+"["+id+"]";
  }
  public String getTypeName() {
    return type+":"+name+"["+id+"]";
  }
  public String getShortList() {
    if (attributes==null) return getName();
    StringBuffer sb=new StringBuffer();
    String value=null;
    for (Enumeration en=defKeys.keys();en.hasMoreElements();)
      if ((value=(String)attributes.get(en.nextElement()))!=null) {
	if (value.length()>20) value=value.substring(0,20);
	sb.append(value+", ");
      }
    for (Enumeration en=attributes.keys();en.hasMoreElements();) {
      value=(String)en.nextElement();
      if (!defKeys.containsKey(value)) {
	value=(String)attributes.get(value);
	if (value.length()>20) value=value.substring(0,20);
	sb.append(value+", ");
      }

    }
    if (sb.length()>0) return name+"["+id+"]"+"("+(sb.toString()).substring(0,sb.length()-2)+")";
    else return getName();
  }
  public String toString() {
    if (attributes==null) return getName();
    StringBuffer sb=new StringBuffer();
    String value=null;
    for (Enumeration en=defKeys.keys();en.hasMoreElements();)
      sb.append(getAttributeString((String)en.nextElement()));
    for (Enumeration en=attributes.keys();en.hasMoreElements();) {
      value=(String)en.nextElement();
      if (!defKeys.containsKey(value)) sb.append(getAttributeString(value));
    }
    if (sb.length()>0) return name+"["+id+"]:\n"+(sb.toString()).substring(0,sb.length());
    else return getName();
  }
  public String[] getKeys() {
    if (attributes==null) return new String[0];
    String[] res=new String[attributes.size()];
    int counter=0;
    for (Enumeration e=attributes.keys();e.hasMoreElements();)
      res[counter++]=(String)e.nextElement();
    return res;
  }
  
  public String[] getAllKeys() {
    Vector res=new Vector();
    String key=null;
    for (Enumeration en=defKeys.keys();en.hasMoreElements();) {
      key=(String)en.nextElement();
      if (attributes!=null && attributes.containsKey(key)) key+=" *";
      res.addElement(key);
    }
    if (attributes!=null)
    for (Enumeration en=attributes.keys();en.hasMoreElements();) {
      key=(String)en.nextElement();
      if (!defKeys.containsKey(key)) {
	if (attributes.containsKey(key)) key+=" *";
	res.addElement(key);
      }}
    String[] r=new String[res.size()];
    res.copyInto(r);
    return r;
  }
  public static void loadAll (DataInputStream dis) throws Exception {
      String l=null;
      Info i=null;
      String att=null;
      String text="";
      while ((l=dis.readLine())!=null) {
	StringTokenizer st=new StringTokenizer(l);
	  if (l.startsWith("§§ ")) {
	    st.nextToken();
	    try {
	      // debug
	      if (att!=null && text.length()>0) {
		i.setAttribute(att,text);
		att=null;
		text="";
	      }
	      if (i!=null) System.out.println(i.toString());
	      i=new Info(st.nextToken(),new Integer(st.nextToken()).intValue(),Util.allTokens(st).substring(1));
	    } catch(Exception e) {
	      i=null;
	    }
	  } else {
	    if (i!=null && l.startsWith("§ ")) {
	      st.nextToken();
	      if (att!=null && text.length()>0) {
		i.setAttribute(att,text);
		att=null;
		text="";
	      }
	      att=st.nextToken();
	    } else text+="\n";
	    if (st.hasMoreTokens())
	      text+=Util.allTokens(st);
	  }
      }
      if (att!=null && text.length()>0) 
	  i.setAttribute(att,text.trim());
	  
      // debug
      System.out.println(all);
  }
  public static void saveAll(FileWriter fw) throws Exception {
      Info i;
    String key;
    for (Enumeration en=all.elements();en.hasMoreElements();) {
      i=(Info)en.nextElement();
      //      if (!type.equals("node"))
      fw.write("§§ "+i.type+" "+i.id+" "+i.name+"\n");
      Hashtable h=i.attributes;
      if (h!=null)
      for (Enumeration en2=h.keys();en2.hasMoreElements();) {
	key=(String)en2.nextElement();
	fw.write("§ "+key+" "+h.get(key)+"\n");
      }
      //      fw.write("\n");
    }
  }
}
class Node {
  //  int x,y,z;
  Hashtable attributes;
  public Hashtable misc;

  public static int hasAttribute(String name) {
    for (int i=0;i<attr_names.length;i++) {
      if (attr_names[i][0].equals(name)) {
	return i;
      }}
    return -1;
  }

  public void setAttribute(String name, String value) {
    if (value!=null) {
      if (hasAttribute(name)!=-1) {
	if (attributes==null) attributes=new HashMap();
	attributes.put(name,value);
      } else {
	if (misc==null) misc=new HashMap();
	misc.put(name,value);
      }
    } else {
      if (attributes!=null) attributes.remove(name);
      if (misc!=null) misc.remove(name);
    }
  }
  public String getAttribute(String name) {
    String result=getValue(name);
    return (result==null)?"":result;
  }
  public String getValue(String name) {
    String result=null;
    if (attributes!=null)
      result=(String)attributes.get(name);
    if (result==null && misc!=null)
      result=(String)misc.get(name);
    return result;
  }

  public boolean hasAttrib(String name) {
    return (attributes!=null && attributes.containsKey(name) || 
	    misc!=null && misc.containsKey(name));
  }
  public String showAttribute(String name, I18N i18n) {
    int id=hasAttribute(name);
    String value=null;
    if (id!=-1 && attributes!=null) {
      value=(String)attributes.get(name);
      if (value!=null)
	return i18n.trans(attr_names[id][1])+": "+value+"\n";
    } else if (misc!=null) {
      value=(String)misc.get(name);
      if (value!=null) return name+": "+value+"\n";
    }
    return "";
  }
public final static String[][] attr_names = 
{{"short","Kurzbeschreibung"},
 {"long","Langbeschreibung"},
 {"npc","NPCs"},
{"name","Name"},
{"ways","WegeKnoten"},
{"exa","komplett untersucht"},
{"tank","Tanke"},
{"house","Häuser"},
{"tport","Teleporter"},
{"port","Hafen"},
{"shop","Laden"},
{"pub","Kneipe"},
{"post","Post"},
{"color","Farbe"},
{"icon","Icon"},
{"type","Landschaft"}
};

  public String toString() {
    StringBuffer res=new StringBuffer();
    String key=null;
    if (attributes!=null)
    for (Enumeration en=attributes.keys();en.hasMoreElements();) {
      key=(String)en.nextElement();
      res.append(key+" "+attributes.get(key)+"\n");
    }
    if (misc!=null)
    for (Enumeration en=misc.keys();en.hasMoreElements();) {
      key=(String)en.nextElement();
      res.append(key+" "+misc.get(key)+"\n");
    }
    return res.toString();
  }
  public Hashtable getTask(String task) {
    if (misc!=null) {
      //      if (misc.get(task)==null) return null;
    Hashtable ret=null;
    String key=null, keynr;
    int tasklen=task.length();
    Integer tasknr=null;
    for (Enumeration en=misc.keys();en.hasMoreElements();) {
      key=(String)en.nextElement();
      if (key.startsWith(task)) {
	keynr=key.substring(tasklen).trim();
	System.out.println("tasknr"+keynr);
	try {
	  tasknr=Integer.valueOf(keynr);
	  if (ret==null) ret=new HashMap();
	  ret.put(tasknr, misc.get(key));
	} catch(NumberFormatException e) {}
      }
    }
    return ret;
    }
    return null;
  }
  public String getMisc() {
    StringBuffer res=new StringBuffer();
    String key=null;
    if (misc!=null)
    for (Enumeration en=misc.keys();en.hasMoreElements();) {
      key=(String)en.nextElement();
      res.append(key+": "+misc.get(key)+"\n");
    }
    return res.toString();
  }
  public String[] getKeys() {
    String[] res=new String[(((misc!=null)?misc.size():0)+((attributes!=null)?attributes.size():0))];
    int counter=0;
    if (attributes!=null)
    for (Enumeration e=attributes.keys();e.hasMoreElements();)
      res[counter++]=(String)e.nextElement();
    if (misc!=null)
    for (Enumeration e=misc.keys();e.hasMoreElements();)
      res[counter++]=(String)e.nextElement();
    return res;
  };
}
  
abstract class Chooser extends Frame {
    Panel buttons=new Panel();
    I18N i18n=Mappanel.i18n;
    public Chooser() {
	setLayout(new BorderLayout());
	buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
	Button choose=new Button(i18n.trans("Übernehmen"));
	choose.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		choose();
		hide();
		if (tf!=null) tf.requestFocus();
	    }
	});
	buttons.add(choose);

	Button clear=new Button(i18n.trans("Löschen"));
	clear.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		clear();
	    }
	});
	buttons.add(clear);

	Button close=new Button(i18n.trans("Schliessen"));
	close.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		hide();
	    }
	});
	buttons.add(close);
	add("South",buttons);
    }

    TextComponent tf=null;

    public void setTextComponent(TextComponent tf) {
	this.tf=tf;
    }

    protected abstract String getValue();
    protected abstract void clear();
    String pattern=null;
    public void choose() {
	if (tf==null) return;
	String s=tf.getText();
	int start=tf.getSelectionStart();
	int end=tf.getSelectionEnd();
	if (start!=end) {
	    tf.setText(s.substring(0,start)+getValue()+s.substring(end));
	    return;
	} 
	if (pattern!=null) {
	    int off=s.indexOf(pattern);
	    if (off>-1) {
		tf.setText(s.substring(0,off)+getValue()+s.substring(off+pattern.length()));		
		return;
	    }
	}
	tf.setText(s.substring(0,start)+getValue()+s.substring(end));

    }
}

class ColorChooser extends Chooser {
    int selection=5;
    String selected="";
    int mx, my;
    int baseIndex=0;
    CCanvas canvas=new CCanvas();

    class CCanvas extends Canvas {
	Vector colors=new Vector();
	class MColor {
	    public MColor(Color color, String name) {
		this.color=color;
		this.name=name;
	    }
	    Color color;
	    String name;
	}
	public CCanvas() {
	    Hashtable hcolors=Mappanel.colors;
	    String name;
	    for (Enumeration en=hcolors.keys();en.hasMoreElements();) {
		name=(String)en.nextElement();
		colors.addElement(new MColor((Color)hcolors.get(name),name));
	    }	    
	    for (float r=0;r<=1f;r+=0.2f)
		for (float g=0;g<=1f;g+=0.2f)
		    for (float b=0;b<=1f;b+=0.2f)
			colors.addElement(new MColor(new Color(r,g,b),""+r+","+g+","+b));
	}
	int cx=120;
	int cy=40;
	public void update(Graphics g) {
	    paint(g);
	}
	public void paint(Graphics g) {
	    Color c;
	    int index=0;
	    Dimension d=getSize();
	    int dx=d.width/(cx),dy=d.height/(cy);
	    int x,y;
	    MColor mcolor;
	    float fdx;
	    Font oldfont=g.getFont();
	    int fontsize=oldfont.getSize();
	    FontMetrics fm=g.getFontMetrics();

	    //	    System.out.println("base "+baseIndex+" sel "+selection+" "+selected+" indexCount "+indexSize());
	    for (int count=0;count<colors.size();count++) {
		if (count<baseIndex) {
		    continue;
		}
		mcolor=(MColor)colors.get(count);

		x=(index % dx)*cx;
		y=(index / dx)*cy;
		if (mx>-1 && mx>=x && mx<=x+cx && my>=y && my<=y+cy) {
		    selection=count;
		    mx=-1;
		    my=-1;
		}
		g.setColor((count==selection)?Color.blue:Color.white);
		g.fillRect(x,y,cx,cy);

		if (count==selection) selected=mcolor.name;
		g.setColor(mcolor.color);
		g.fill3DRect(x+3,y+3,cx-6,cy-6,true);

		g.setColor(Color.black);

		fdx=(float)fm.stringWidth(mcolor.name)/(cx-8);
		if (fdx>1) {
		    fontsize=Math.round(fontsize/fdx+0.5f);
		    g.setFont(Util.getCachedFont(fontsize));
		}
		x=x+(cx-g.getFontMetrics().stringWidth(mcolor.name))/2;
		g.drawString(mcolor.name,x,y+cy/2);
		g.setFont(oldfont);

		index++;
		if (index>=indexSize()) break;
	    }
	}
	public Dimension getPreferredSize() {
	    return new Dimension(200,200);
	}
	public int indexSize() {
	    Dimension d=getSize();
	    return (int)(d.width/cx+0.5)*(int)(d.height/cy+0.5);
	}
	public int maxIndex() {
	    int maxIndex=colors.size()-indexSize();
	    return (maxIndex>0)?maxIndex:0;
	}
    };

    public ColorChooser() {
	pattern="<color>";
	add("Center",canvas);
	Button back=new Button(i18n.trans("zurueck"));
	buttons.add(back);
	back.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		baseIndex-=canvas.indexSize();
		if (baseIndex<=0) baseIndex=0;
		//	    System.out.println("base "+baseIndex+" sel "+selection+" "+selected);
		canvas.repaint(100);
	    }
	});
	Button next=new Button(i18n.trans("weiter"));
	next.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		baseIndex+=canvas.indexSize();
		if (baseIndex>canvas.maxIndex()) baseIndex=canvas.maxIndex();
		//	    System.out.println("base "+baseIndex+" sel "+selection+" "+selected);
		canvas.repaint(100);
	    }
	});
	buttons.add(next);

	canvas.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
		clear();
		mx=e.getX();
		my=e.getY();
		canvas.repaint();
	    }
	});
    }
    protected String getValue() {
	if (selected.indexOf(" ")!=-1)
	    return "("+selected+")";
	return selected;
    }
    protected void clear() {
	selection=-1;
	selected="";
    }
}

class IconChooser extends Chooser {
    int selection=5;
    String selected="";
    int mx, my;
    int baseIndex=0;
    ICanvas canvas=new ICanvas();

    class ICanvas extends Canvas implements ImageObserver {
	int cx=120;
	int cy=40;

	public void update(Graphics g) {
	    paint(g);
	}
	int updatecounter=0;
	public void paint(Graphics g) {
	    if (files==null) return;
	    int index=0;
	    Dimension d=getSize();
	    int dx=d.width/(cx),dy=d.height/(cy);
	    int x,y;
	    float fdx;
	    Font oldfont=g.getFont();
	    int fontsize=oldfont.getSize();
	    FontMetrics fm=g.getFontMetrics();
	    String name;
	    int w=-1,h=-1;
	    if (images==null) images=new Image[files.size()];

	    g.setColor(Color.white);
	    g.fillRect(0,0,d.width,d.height);

	    for (int count=0;count<files.size();count++) {
		if (count<baseIndex) {
		    continue;
		}
		name=(String)files.get(count);
		if (images[count]==null) {
		    images[count]=tk.getImage(dir+name);
		    updatecounter++;
		}

		x=(index % dx)*cx;
		y=(index / dx)*cy;
		if (mx>-1 && mx>=x && mx<=x+cx && my>=y && my<=y+cy) {
		    selection=count;
		    mx=-1;
		    my=-1;
		}
		g.setColor((count==selection)?Color.blue:Color.white);
		g.fillRect(x,y,cx,cy);

		if (count==selection) selected=name;
		
		if (w==-1 || h==-1) {
		    w=images[count].getWidth(this);
		    h=images[count].getHeight(this);
		}
		//		System.out.println(dx+" "+dx+" > w>>2"+(w>>2)+" (w "+w);


		if (w>-1 && h>-1 && cx-10 > w/4 && cy-10 > h/4) {
		    int sx,sy;
		    if ((float)(cx/w) < (float)((cy-10)/h)) {
			sx=cx;
			sy=h*cx/w;
		    } else {
			sx=w*(cy-10)/h;
			sy=cy-10;
		    }
		    g.drawImage(images[count],x+(cx-sx)/2,y,sx,sy,this);
		    //		    g.drawImage(images[count],x+(cx-w)/2,y,cy-10,cy-10,this);
		}

		//cx-6,cy-15,null);

		g.setColor(Color.black);

		fdx=(float)fm.stringWidth(name)/(cx-8);
		if (fdx>1) {
		    fontsize=Math.round(fontsize/fdx+0.5f);
		    g.setFont(Util.getCachedFont(fontsize));
		}
		x=x+(cx-g.getFontMetrics().stringWidth(name))/2;
		g.drawString(name,x,y+cy);
		g.setFont(oldfont);

		index++;
		if (index>=indexSize()) break;
	    }
	}
	public Dimension getPreferredSize() {
	    return new Dimension(200,200);
	}
	public int indexSize() {
	    Dimension d=getSize();
	    return (int)(d.width/cx+0.5)*(int)(d.height/cy+0.5);
	}
	public int maxIndex() {
	    if (files.size()==0) return 0;
	    int maxIndex=files.size()-indexSize();
	    return (maxIndex>0)?maxIndex:0;
	}
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	    if ((infoflags & ( ALLBITS | FRAMEBITS )) > 0) {
		if (--updatecounter <=0) repaint();
		return false;
	    }
	    return true;
	}
    };
    FileDialog fd;
    Image[] images;
    Vector files=new Vector();
    String dir="";
    FilenameFilter filter=new FilenameFilter() {
			public boolean accept(File dir, String name) {
			    name=name.toLowerCase();
			    return (name.endsWith(".gif")|| name.endsWith(".jpg"));
			}
		    };
    Toolkit tk=Toolkit.getDefaultToolkit();

    Choice types;
    Button filesButton;
    protected void initTypeChoice() {
	types=new Choice();

	types.addItem("Circle");
	types.addItem("Circle3D");
	types.addItem("FilledCircle");
	types.addItem("FilledCircle3D");
	types.addItem("FilledHouse");
	types.addItem("FilledRectangle");
	types.addItem("FilledRectangle3D");
	types.addItem("Image");
	types.addItem("Minus");
	types.addItem("NPCSketch");
	types.addItem("Picture");
	types.addItem("Plus");
	types.addItem("RandomPicture");
	types.addItem("Rectangle");
	types.addItem("Rectangle3D");
	types.addItem("Text");
	types.addItem("TiledPicture");
	types.select("Image");

	types.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
		String s=types.getSelectedItem();
		if (!s.equals(type)) {
		    if (s.endsWith("Picture") || s.equals("Image")) {
			filesButton.setEnabled(true);
			urlButton.setEnabled(true);
		    }
		    else {
			filesButton.setEnabled(false);
			urlButton.setEnabled(false);
		    }
		    files=new Vector();
		    type=s;
		}
	    }
	});
    }
    String type="Image";
    boolean returnURL=false;
    Button urlButton;
    public IconChooser() {
	pattern="<icon>";
	Panel top=new Panel();
	top.setLayout(new FlowLayout(FlowLayout.LEFT));
	filesButton=new Button(i18n.trans("Verzeichnis"));
	filesButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (fd==null) {
		    fd=new FileDialog(IconChooser.this,i18n.trans("Verzeichnis auswählen"),FileDialog.LOAD);
		    //		    fd.setFilenameFilter(filter);
		}
		if (dir.length()>0) fd.setDirectory(dir);
		fd.setFile(".");
		fd.show();
		String sdir=fd.getDirectory();
		if (sdir!=null) {
		    dir=sdir;
		    System.out.println("Dir: "+dir);
		    String[] sfiles=new File(sdir).list();
		    files=new Vector();
		    for (int i=0;i<sfiles.length;i++)
			if (filter.accept(null,sfiles[i])) {
			    files.addElement(sfiles[i]);
			}
		    baseIndex=0;
		    clear();
		}
		images=null;
	    }
	});
	top.add(filesButton);
	initTypeChoice();
	top.add(types);
	add("North",top);
	add("Center",canvas);
	urlButton=new Button(i18n.trans("Bild URL"));
	buttons.add(urlButton);
	urlButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		returnURL=true;
		choose();
		returnURL=false;
	    }
	});

	Button back=new Button(i18n.trans("zurueck"));
	buttons.add(back);
	back.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		baseIndex-=canvas.indexSize();
		if (baseIndex<=0) baseIndex=0;
		//	    System.out.println("base "+baseIndex+" sel "+selection+" "+selected);
		canvas.repaint(100);
	    }
	});
	Button next=new Button(i18n.trans("weiter"));
	next.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		baseIndex+=canvas.indexSize();
		if (baseIndex>canvas.maxIndex()) baseIndex=canvas.maxIndex();
		//	    System.out.println("base "+baseIndex+" sel "+selection+" "+selected);
		canvas.repaint(100);
	    }
	});
	buttons.add(next);

	canvas.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
		mx=e.getX();
		my=e.getY();
		clear();
	    }
	});
	setSize(200,200);
    }
    protected String getURL() {
	if (returnURL) {
	try {
	    return new URL("file","localhost",new File(dir+selected).getAbsolutePath()).toString();
	} catch(Exception e) {
	    e.printStackTrace();
	    return "";
	}
	} else {
	    String s=dir;
	    int off=s.indexOf(ICONS_DIR);
	    if (off>-1)
		s=s.substring(off+File.separator.length());
	    if (s.startsWith("./")) return s.substring(2)+selected;
	    else return s+selected;
	}
    }
    static String ICONS_DIR=File.separator+"icons"+File.separator;
    protected String getValue() {
	if (type.equals("Image"))
	    return getURL();
	else 
	    if (type.endsWith("Picture"))
		return type+" "+getURL();
	    else return type;
    }
    protected void clear() {
	selection=-1;
	selected="";
	canvas.repaint(100);
    }
}

    class InfoRange {
	int from, len;
	String cmd;
	public InfoRange(int from, int len, String cmd) {
	    this.len=len;
	    this.cmd=cmd;
	    this.from=from;
	}
	public String isTouched(int index) {
	    if (from<=index && from+len>index) return cmd;
	    return null;
	}
    }
    class RangedStringBuffer {
	StringBuffer buffer;
	public Vector infoRanges;
	public RangedStringBuffer() {
	    buffer=new StringBuffer();
	    infoRanges=new Vector();
	}
	public RangedStringBuffer(String s) {
	    this();
	    append(s);
	}
	public RangedStringBuffer(String s, String cmd) {
	    this();
	    append(s,cmd);
	}
	public void append(String s) {
	    buffer.append(s);
	}
	public void append(String s, String cmd) {
	    infoRanges.addElement(new InfoRange(buffer.length(),s.length(),cmd));
	    buffer.append(s);
	}
	public void insert(int index,String s, String cmd) {
	    Vector res=new Vector();
	    InfoRange range;
	    boolean before=true;
	    int len=s.length();
	    for (Enumeration en=infoRanges.elements();en.hasMoreElements();) {
		range=(InfoRange)en.nextElement();
		if (before) {
		    if (range.from+range.len<index) res.addElement(range);
		    else {
			before=false;
			if (range.from>=index) {
			    res.addElement(new InfoRange(index,len,cmd));
			    range.from+=len;
			    res.addElement(range);
			} else {
			    res.addElement(range);
			    range.len=index-range.from;
			    res.addElement(new InfoRange(index,len,cmd));
			}
		    }
		} 
		else {
		    range.from+=len;
		    res.addElement(range);
		}
	    }
	    infoRanges=res;
	    buffer.insert(index,s);
	}
	public String toString() {
	    return buffer.toString();
	}
    }

    class ActionAdapter {
	private Vector listeners=new Vector();
	public void addActionListener(ActionListener al) {
	    synchronized (listeners) {
	    if (!listeners.contains(al))
		listeners.addElement(al);
	    }
	}

	public  void removeActionListener(ActionListener al) {
	    synchronized (listeners) {
	    if (listeners.contains(al))
		listeners.remove(al);
	    }
	}
	public void fireActionEvent(Component source, String command) {
	    ActionEvent ae=new ActionEvent(source,ActionEvent.ACTION_PERFORMED,command);
	    synchronized (listeners) {
		for (int i=listeners.size()-1;i>=0;i--) 
		    ((ActionListener)listeners.get(i)).actionPerformed(ae);
	    }
	}
    }
    
    class ActionTextArea extends TextArea {
	Vector infoRanges;
	ActionAdapter action=new ActionAdapter();
	public ActionTextArea(String s, int rows, int columns, int sb_mode) {
	    super(s,rows,columns,sb_mode);
	    setEditable(false);
	    clear();
	    addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent me) {
		    if (me.getClickCount()>1) {
			int size=infoRanges.size();
			String cmd;
			InfoRange hit;
			for (int i=0;i<size;i++) {
			    hit=(InfoRange)infoRanges.get(i);
			    cmd=hit.isTouched(getCaretPosition());
			    if (cmd!=null) {
				action.fireActionEvent(ActionTextArea.this,cmd);
				break;
			    }
			}
		    }
		}
	    });
	}
	
	public void addActionListener(ActionListener al) {
	    action.addActionListener(al);
	}

	public  void removeActionListener(ActionListener al) {
	    action.removeActionListener(al);
	}

	public void clear() {
	    infoRanges=new Vector();
	    buffer=new StringBuffer();
	    isShowing=false;
	}
	boolean isShowing=false;
	StringBuffer buffer=null;
	public void append(String str, String cmd) {
	    infoRanges.addElement(new InfoRange(buffer.length(),str.length(),cmd));
	    buffer.append(str);
	}
	public void append(String str) {
	    buffer.append(str);
	}
	public void setText() {
	    isShowing=true;
	    super.setText(buffer.toString());
	}
	public void setText(RangedStringBuffer rbuffer) {
	    isShowing=true;
	    infoRanges=rbuffer.infoRanges;
	    super.setText(rbuffer.toString());
	}

	public void setText(String s) {
	    clear();
	    super.setText(s);
	}
	
    }

interface Region {
    boolean inside(int x, int y,int dx,int dy);
}

class Square implements Region {
    int x0,y0,x1,y1;
    public Square(int x0,int y0,int x1,int y1) {
	this.x0=x0;
	this.y0=y0;
	this.x1=x1;
	this.y1=y1;
    }
    public boolean inside(int ix,int iy,int dx,int dy) {
	int x=ix*100/dx,y=iy*100/dy;
	return (x>=x0 && x<x1 && 
		y>=y0 && y<=y1);
    }
}

class Pie implements Region {
    int r,r0=-1;
    int from,to,from180,to180;
    int angle;
    static final float rad=(float)(180/Math.PI);
    public Pie(int r,int from, int to) {
	this.r=r;
	this.from=(from%360);
	this.to=(to%360);
	from180=(this.from+180)%360;
	to180=(this.to+180)%360;
	System.out.println("from,to,r"+this.from+","+this.to+","+r);
    }
    public Pie(int r0, int r, int from, int to) {
	this(r,from,to);
	this.r0=r0;
    }
    public boolean inside(int ix,int iy,int dr0,int dr1) {
	dr0/=2;
	dr1/=2;
	int dx=ix-dr0, dy=dr1-iy;
	int dr=(int)Math.sqrt(dx*dx+dy*dy);
	if (dr0*r/100<dr || r0>-1 && dr<r0*dr0/100) return false;
	if (dx==0) angle=(dy>0)?90:270;
	else {
	    angle=(int)(Math.atan(Math.abs((float)dy/(float)dx))*rad);
	    if (dx<0)
		if (dy>0) angle=180-angle;
		else angle+=180;
	    else
		if (dy<0) angle=360-angle;
	}
	if (angle>from && angle<to) return true;
	else {
	    angle=(angle+180)%360;
	    return (angle>from180 && angle<to180);
	}
    }
}
