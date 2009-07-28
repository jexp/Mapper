#include <properties.h>
#include <rooms.h>

inherit "std/room";

create() {
  ::create();
  SetProp(P_LIGHT, 1);
  SetProp(P_INDOORS, 0);

  /*short* SetProp(P_INT_SHORT,"#short#"); */
  /*long* SetProp(P_INT_LONG,break_string("#long#",78)); */
  /*f_bsx* SetProp(P_BSX_INT_DESCRIPTION,
	  read_file("#f_bsx#")); */
  /*exits* #exits# */
  /*
  AddDetail(({}),
	    wrap());
  */
}
