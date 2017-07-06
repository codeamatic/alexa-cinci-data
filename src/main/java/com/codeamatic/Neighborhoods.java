package com.codeamatic;

import java.util.ArrayList;
import java.util.List;

public final class Neighborhoods {

  private static final List<String> neighborhoods = new ArrayList<String>();

  private Neighborhoods() {}

  static {
    neighborhoods.add("Avondale");
    neighborhoods.add("Bond Hill");
    neighborhoods.add("Riverfront");
    neighborhoods.add("CBD");
    neighborhoods.add("California");
    neighborhoods.add("Camp  Washington");
    neighborhoods.add("Carthage");
    neighborhoods.add("C. B. D. / Riverfront");
    neighborhoods.add("Clifton");
    neighborhoods.add("Clifton/University Heights");
    neighborhoods.add("University Heights");
    neighborhoods.add("College  Hill");
    neighborhoods.add("Columbia / Tusculum");
    neighborhoods.add("Corryville");
    neighborhoods.add("CUF");
    neighborhoods.add("Downtown");
    neighborhoods.add("East  End");
    neighborhoods.add("East Price Hill");
    neighborhoods.add("East Walnut Hills");
    neighborhoods.add("East  Westwood");
    neighborhoods.add("English  Woods");
    neighborhoods.add("Evanston");
    neighborhoods.add("Fairview");
    neighborhoods.add("Fay Apartments");
    neighborhoods.add("Hartwell");
    neighborhoods.add("Hyde Park");
    neighborhoods.add("Kennedy Heights");
    neighborhoods.add("Linwood");
    neighborhoods.add("Lower Price  Hill");
    neighborhoods.add("Madisonville");
    neighborhoods.add("Millvale");
    neighborhoods.add("Mount  Adams");
    neighborhoods.add("Mount Airy");
    neighborhoods.add("Mount  Auburn");
    neighborhoods.add("Mount  Lookout");
    neighborhoods.add("Mount  Washington");
    neighborhoods.add("North Avondale");
    neighborhoods.add("Paddock Hills");
    neighborhoods.add("North Fairmount");
    neighborhoods.add("Northside");
    neighborhoods.add("O'Bryonville");
    neighborhoods.add("Oakley");
    neighborhoods.add("Over-The-Rhine");
    neighborhoods.add("Paddock  Hills");
    neighborhoods.add("Pendleton");
    neighborhoods.add("Pleasant Ridge");
    neighborhoods.add("Queensgate");
    neighborhoods.add("Riverside");
    neighborhoods.add("Roselawn");
    neighborhoods.add("Sayler  Park");
    neighborhoods.add("Sedamsville");
    neighborhoods.add("South Cumminsville");
    neighborhoods.add("S.. Cumminsville");
    neighborhoods.add("South  Fairmount");
    neighborhoods.add("Spring Grove Village");
    neighborhoods.add("Villages at Roll Hill");
    neighborhoods.add("Walnut Hills");
    neighborhoods.add("West  End");
    neighborhoods.add("West Price Hill");
    neighborhoods.add("Westwood");
    neighborhoods.add("Winton Hills");
  }

  public static List<String> getNeighborhoods() {
    return neighborhoods;
  }
}
