/set mapper_tf_version=mapper.tf vom Die, 12.12.2000

/file_contains mapper.tf Makros fuer das Zusammenspiel mit dem Mapper

/file_requires !basics.tf !lists.tf !util.tf
;
; mapper.tf 
; (c) by Mesirii
; version 1.00 15.3.99
;
/set file_version=1.01

/addh info \
initialisiert eine Verbindung zur Mapper Application. Es wird eine World namens mapper angelegt und verbunden. Ausserdem wird die Variable mapper auf 1 gesetzt.
/addh var mapper, automapper
/addh func addworld, connect, fg
/addh req customize.tf
/addh init_mapper comm
/defh init_mapper = \
     /addworld -Ttelnet mapper localhost %{1-%{mapper_port}}%;\
     /connect mapper%;\
     /fg -q -<%;\
     /def -q -t"*" -mglob -E(mapper) -wmapper t_grab_mapper = ${body_grab_mapper}%;\
     /set mapper=1%;\

/def body_grab_mapper = \
;	/echo ${world_name}%;\
	/if ({1}=~"0") \
	  /if (substr({-1},0,1)!~"/") \
	    /send -w%main_world %{-1}%; \
	  /else /eval %{-1}%;\
	  /endif%;\
	/endif%;\
	/echo From Mapper: %*%;


/addh info \
extrahiert aus den Anzeige der Ausgaenge die einzelnen Ausgaenge und erzeugt die Kommandos um im Mapper am aktuellen Punkt die Ausgaenge hinzuzufuegen. Es ist sinnvoll dieses Kommando an eine Tastenkombination zu binden.
/addh var mud_show_exits_command
/addh mak getroom2
/addh req customize.tf
/defh mapexits = \
       /set fulldetail=%;\
       /def_psave_trig %;\
       /def_psave3_trig%;\
       /set nextmakro=/getroom2 %*%;\
       %mud_show_exits_command %;\

/defh findroom = \
       /set fulldetail=%;\
       /def_psave_trig %;\
       /def_psave3_trig%;\
       /set nextmakro=/getexits2 %*%;\
       %mud_show_exits_command %;\

/def getexits2 = \
    /restricttoexit %;\
    /let getexits=findroom%;\
    /if (regmatch(mud_exits_output,fulldetail)==1) \
         /let p_long=$(/sreplace @{N} \\space %P2)%;\
;	/let p_long%;\
	 /while (regmatch(mud_exits_regexp,p_long)==1) \
	    /let getexits=%getexits %P1%;\
	    /let p_long=%PR%;\
	 /done%;\
    /endif%;\
    /let getexits%;\
    /send -wmapper %getexits%;
	
/addh info \
Hilfsmakro fuer mapexits. Hier werden die einzelen Ausgaenge extrahiert und an den Mapper geschickt. Zur Ueberpruefung, ob es ein normaler Ausgang ist, wird die Liste ?comm_abbr benutzt.
/addh var mapper, fulldetail
/addh getroom2 mak


/def getroom2 = \
    /restricttoexit %;\
    /if (regmatch(mud_exits_output,fulldetail)==1) \
         /let p_long=$(/sreplace @{N} \\space %P2)%;\
;	/let p_long%;\
	 /while (regmatch(mud_exits_regexp,p_long)==1) \
	    /if (mapper) \
;	/echo %P1%;\
	      /getkeyofvalue comm_abbr %P1%;\
	      /if (value=~error) \
	         /getvalueof xtramoves %P1%;\
	         /if (value!~error) \
		   /if (substr(value,0,1)=~"0")) \
		    /set value=%P1%;\
		   /else \
;		     /echo %P1%;\
		     /send -wmapper ar %P1%;\
		     /set value=sent%;\
	           /endif%;\
		 /else \
		   /set value=%error%;\
	         /endif%;\
	      /endif%;\
	      /if (value!~error & value!~"sent") \
	          /echo %value%;\
	          /send -wmapper ae %value%;\
	      /else \
	          /input /send -wmapper asp -d0 -m? -p? %P1%;\
	      /endif%;\
	    /endif%;\
	    /let p_long=%PR%;\
	 /done%;\
    /endif%;\
    /if (mi_list!~"") /mi%; /endif%;

/addh info Die Liste mapinfo enthaelt die mudspezifischen kommandos, fuer das Extrahieren der gewuenschten Informatioenen
/addh mapinfo list

/createlist mapinfo
/addtolist mapinfo short kschau
/addtolist mapinfo long schaue
/addtolist mapinfo author rohrhoere
/addtolist mapinfo npc knuddel alle

/addh info \
entsprechend des uebergebenen Parameters, holt sich /mapinfo die mudspezifischen Kommandos aus der liste %mapinfo und laesst diese vom Mud ausfuehren. Der Ergebnistext wird von spezifischen Makros geparst, die dann die Werte an den Mapper senden
/addh var mapper, fulldetail, mapinfo
/addh req customize.tf, way.tf
/addh mapinfo comm
/defh mapinfo = \
    /if (mapper) \
       /if ({#}>0)\
         /getvalueof mapinfo %1%;\
         /if (value!~error) \
;/echo mapinfo %1 %value%;\
         /set fulldetail=%;\
         /def_psave_trig %;\
         /def_psave3_trig %{2-1}%;\
         /set nextmakro=/map%1%;\
;/set nextmakro%;\
         %value %;\
         /endif%;\
       /elseif (mi_list!~"") \
	  /test tokenize(" ",mi_list)%;\
	  /let i=0%;\
	  /set value=%;\
	  /while (++i<=T0) \
	     /if (i>1) \
	     /test value:=strcat(value,"/add_catchup_action /mapinfo ",\{T%{i}\}," 2",strrep("\\\\",2*i-3),"\%;",strrep("\\\\",2*i-3),"\%; ")%;\
	     /else \
	     /test value:=strcat("/clear_actions\%;/mapinfo ",\{T%{i}\},"\%;\%; ")%;\
	     /endif%;\
	  /done%;\
/echo %value%;\
	  /eval %value%;\
       /endif%;\
    /endif%;

/addh info \
das Makro parst die Short, die als Ergebnis von /mapinfo erhalten wird
/addh mak restricttoexit,
/addh var mapper
/addh req way.tf
/addh mapshort mak
/def mapshort = \
;    /set fulldetail%;\
    /let off=$[strstr(fulldetail,"@{N}")]%;\
    /if (off==0) \
	/let map_short=$[substr(fulldetail,off+4)]%;\
	/let off=$[strstr(map_short,"@{N}")]%;\
    /endif%;\
    /if (off>-1) \
	/let map_short=$[substr(map_short,0,off)]%;\
    /endif%;\
    /test regmatch(mud_extract_short_regexp,map_short)%;\
    /let map_short=%P1%;\
    /send -wmapper ai short %map_short%;\

/addh info \
das Makro parst die Long, die als Ergebnis von /mapinfo erhalten wird
/addh mak restricttoexit,
/addh var mapper
/addh req way.tf
/addh maplong mak

/def maplong = \
;/echo MapLong %fulldetail%;\
   /restricttoexit%;\
;   /getkeyofvalue lastpoints %fulldetail%;\
   /getpoint2%;\
   /if (value!~error) \
     /send -wmapper ai ways %value%;\
   /endif%;\
   /while (substr(fulldetail,0,4)=~"@{N}") \
     /set fulldetail=$[substr(fulldetail,4)]%;\
   /done%;\
;   /set fulldetail%;\
   /let off=$[strrchr(fulldetail,"@")]%;\
   /if (off>-1) /set fulldetail=$[substr(fulldetail,0,off)]%; /endif%;\
   /send -wmapper ai long $(/sreplace @{N} \\space %fulldetail)%;\
;   /set fulldetail%;

/addh info \
das Makro parst die NPCS, die als Ergebnis von /mapinfo erhalten wird
/addh mak restricttoexit,
/addh var mapper
/addh req way.tf
/addh mapnpc mak

/def cmapnpc = \
	/if (mapper==1) \
	/getlistvalueof npcs %*%;\
	/if (value!~error) \
	  /set temp_list_npc=%value%;\
	  /set cmapnpc=ci §§ NPC%;\
	  /getvalueof temp_list_npc name%;\
	  /if (value!~error) \
	    /set cmapnpc=$[strcat(cmapnpc," ",value)]%;\
	    /forEach temp_list_npc kv /cmapnpc2%;\
	    /send -wmapper %cmapnpc%;\
	  /endif%;\
	/endif%;\
	/endif%;

/def cmapnpc2 = /set cmapnpc=%{cmapnpc}§%{*}%;

/def mapnpc =\
     /if (regmatch("Du knuddelst (.+)\\.",fulldetail)) \
	/test tokenize(", ",replace(" und ",", ",replace("@{N}"," ",{P1})))%;\
	/let count=0%;\
	/let res=%;\
	/while (++count<=T0) \
	   /test value:=artikel_entfernen(\{T%count\})%;\
/echo %value%;\
	   /if (strstr(res,value)==-1) \
	      /let res=%res %value%;\
	   /endif%;\
	/done%;\
     	/input /send -wmapper ai npc%res%;\
     /endif%;

/set mi_list=
/addsave mi_list

/addh info \
das Makro parst den Autor, die als Ergebnis von /mapinfo erhalten wird
/addh mak restricttoexit,
/addh var mapper
/addh req way.tf
/addh mapauthor mak

/def mapauthor =\
;     /set fulldetail%;\
     /test regmatch("Eine Stimme fluestert: Dieser Raum wurde von (.+) geschrieben",fulldetail)%;\
     /send -wmapper ai author $(/sreplace @{N} \\space %P1)%;

/addh info \
Abkuerzungsmakro fuer /mapinfo
/addh mi comm
/defh mi = /mapinfo %*%;

/addh info wenn auf 1 werden alle Bewegungen im Mud zum Erzeugen neuer Ausgaenge im Mapper benutzt. Da das aber leicht zu Fehlern fuehrt ist es standardmaessig ausgeschaltet.
/addh see walk
/addh automapper var
/set automapper=0
/addsave automapper

/addh info wenn auf 1 wird der mapper mit Kommandos (Ausgaenge erstellen, Infos hinzufuegen, bewegen) versorgt
/addh mapper var

/addh info schickt den uebergebenen Text als Kommando an den Mapper
/addh var mapper
/addh map comm
/defh map = \
     /if (mapper) \
       /send -wmapper - %*%;\
     /endif%;

/addh info Makro das das uebergebene Attribut mit den zugehoerigen Informationen an das Kommando ai des Mappers schickt
/addh ai comm
/defh ai = /map ai %*%;

; aus way.tf

/addh info Begrenzt die Raumbeschreibung, die in %fulldetail steht auf bis einschliesslich der Ausgaenge. Die Variable room_npcs wird auf den Rest gesetzt.
/addh var mud_exits_output
/addh restricttoexit mak
/def restricttoexit = \
     /let reg=$[strcat(mud_exits_output)] %;\
     /set room_npcs=%;\
     /if (regmatch(reg,fulldetail)==1) \
        /set fulldetail=%PL%P0%;\
	/set room_npcs=%PR%;\
	/if (strstr(room_npcs,"@{N}")==0) \
	  /set room_npcs=$[substr(room_npcs,4)]%;\
	/endif%;\
     /endif %;

; aus customize.tf

/addh info \
Die Art, wie Ausgaenge der Raeume angegeben werden (als regexp), so dass in P2 alle Ausgaenge stehen
/addh mud_exits_output var
/set mud_exits_output=(keine sichtbaren Ausgaenge|sichtbare Ausgaenge: |sichtbaren Ausgang: |sicheren Ausgang: )([^\.]*\.)

/addh info eine Regexp um die Himmelsrichtungen aus %P2 zu holen
/addh mud_exits_regexp var
/set mud_exits_regexp=([a-z_]+)(, ?| und |\.?$)


/addh info \
hier sind Befehle enthalten, die die Fernsteuerung des Mappers vom TF aus unterstuetzen. (<a href="map/Mapper.html">siehe auch dort</a>)@{N}\
Ausserdem sind auch in ?way.tf Anpassungen bei den Send Hooks vorgenommen worden, so dass Bewegung im Mud auch Bewegung auf dem Mapper hervorruft.
/addh tut Starten mit /init_mapper und dann bei bedarf /mapexits bzw. /mapinfo aufrufen.
/addh see mapper, automapper, mapinfo, mi, ai
/addh req customize.tf, way.tf
/addh version 1.01
/addh_fileinfo


/file_done mapper.tf 




