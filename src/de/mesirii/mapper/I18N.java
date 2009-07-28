package de.mesirii.mapper;

import java.util.*;
import java.net.*;

public class I18N {

    Properties translations;
    boolean loaded;
    String NULL_NAME;

    public I18N(String lang) {
	this(lang,null);
    }

    public I18N(String lang, URL url)
    {
        translations = new Properties();
        loaded = false;
        NULL_NAME = "k.A.";
        System.out.println("lang " + lang);
        try
        {
            Properties properties = new Properties();
            if(url != null)
            {
                URL url1 = new URL(url, "ressource_" + lang);
                System.out.println(url1);
                properties.load(url1.openStream());
            } else
            {
                properties.load(getClass().getResourceAsStream("ressource_" + lang));
            }
            String key;
            for(Enumeration enumeration = properties.keys(); enumeration.hasMoreElements(); translations.put(key.replace('_', ' '), properties.get(key)))
                key = (String)enumeration.nextElement();

            System.out.println(translations);
            loaded = true;
            NULL_NAME = trans(NULL_NAME);
        }
        catch(Exception exception)
        {
            System.out.println("Resource File: ressource_" + lang + " not found: " + exception.getMessage());
        }
    }
    /*
    public I18N(String lang, URL codebase) {
	System.out.println("lang "+lang);
	try {
	    Properties temp=new Properties();

	    if (codebase!=null) {
		URL u=new URL(codebase,"ressource_"+lang);
		System.out.println(u);
		temp.load(u.openStream());
	    } else
	    temp.load(getClass().getResourceAsStream("ressource_"+lang));

	    String key=null;
	    for (Enumeration en=temp.keys();en.hasMoreElements();) {
		key=(String)en.nextElement();
		translations.put(key.replace('_',' '),temp.get(key));
	    }
	    System.out.println(translations);
	    loaded=true;
	    NULL_NAME=trans(NULL_NAME);
	} catch(Exception e) {
	    System.out.println("Resource File: ressource_"+lang+" not found: "+e.getMessage());
	    //	    e.printStackTrace();
	}
    }
*/

    public String trans(String s) {
	if (s==null) return null;
	if (!loaded) return s;
	String line=translations.getProperty(s);
	if (line==null) return s; else return line;
    }

    public String trans(String s, Object[] params) {
	if (s==null) return null;
	String line=trans(s);
	for (int i=0;i<params.length;i++) {
	    if (params[i]==null)
		line=replace(line,"$"+i,NULL_NAME);
	    else
		line=replace(line,"$"+i,params[i].toString());
	}
	return line;
    }
    public String trans(String s, String s1, String s2)
    {
        return trans(s, ((Object []) (new String[] {
            s1, s2
        })));
    }

    public String trans(String s, String s1, String s2, String s3)
    {
        return trans(s, ((Object []) (new String[] {
            s1, s2, s3
        })));
    }

    public String trans(String s, String s1, String s2, String s3, String s4)
    {
        return trans(s, ((Object []) (new String[] {
            s1, s2, s3, s4
        })));
    }

    public String trans(String s, String s1)
    {
        if(s == null)
            return null;
        String s2 = trans(s);
        if(s1 == null)
            s2 = replace(s2, "$0", NULL_NAME);
        else
            s2 = replace(s2, "$0", s1);
        return s2;
    }

    protected String replace(String base, String old, String neu) {
	int off=base.indexOf(old);
	int oldlen=old.length();
	StringBuffer sb=new StringBuffer();
	while (off!=-1) {
	    sb.append(base.substring(0,off));
	    sb.append(neu);
	    base=base.substring(off+oldlen);
	    off=base.indexOf(old);	    
	}
	sb.append(base);
	return sb.toString();
    }
}






