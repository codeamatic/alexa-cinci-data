package com.codeamatic;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.codeamatic.exceptions.DateRangeException;
import com.codeamatic.exceptions.DateStringNotSupportedException;
import com.codeamatic.exceptions.NeighborhoodNotSupportedException;
import com.codeamatic.socrata.CrimeReport;
import com.codeamatic.socrata.support.SocrataClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amazon.speech.speechlet.SpeechletResponse.newAskResponse;

/**
 * Cinci Data Speechlet for handling Alexa Speechlet requests.
 */
public class CincyDataSpeechlet implements Speechlet {
  private static final Logger log = LoggerFactory.getLogger(CincyDataSpeechlet.class);

  private static final String SOCRATA_TOKEN = System.getenv("SOCRATA_CINCY_TOKEN");
  private static final String SOCRATA_CRIME_API = System.getenv("SOCRATA_CINCY_CRIME_API");
  private static final String SKILL_NAME = "Cincy Data";

  private static final String TIME_START = "T00:00:00.000";
  private static final String TIME_END = "T23:59:59.999";

  private static final String SLOT_NEIGHBORHOOD = "neighborhood";
  private static final String SLOT_DATE = "date";
  private static final String SLOT_DATE_STRING = "date_string";

  private static final String NEIGHBORHOOD_PROMPT = "Which neighborhood would you like a crime report for?";

  @Override
  public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
    log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
  }

  @Override
  public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
    log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

    return getWelcomeResponse();
  }

  @Override
  public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
    log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

    Intent intent = request.getIntent();
    String intentName = (intent != null) ? intent.getName() : "";

    switch(intentName) {
      case "CrimeReportIntent":
        return getCrimeReportResponse(intent);
      case "SupportedNeighborhoodsIntent":
        return getSupportedNeighborhoodsResponse();
      case "AMAZON.HelpIntent":
        return getHelpResponse();
      case "AMAZON.StopIntent":
        return getStopResponse();
      case "AMAZON.CancelIntent":
        return null;
      default:
        throw new SpeechletException("Invalid Intent");
    }
  }

  @Override
  public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
    log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
  }

  /**
   * Creates and returns a {@code SpeechletResponse} with a welcome message and card.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getWelcomeResponse() {
    String neighborhoodPrompt = NEIGHBORHOOD_PROMPT;
    String speechText = "Welcome to the " + SKILL_NAME + " skill. "
            + "You can get crime and incident data for neighborhoods in the city of Cincinnati. "
            + "For example, you can say, give me yesterday's crime report for Avondale. "
            + "For a list of supported neighborhoods, ask what neighborhoods are supported. "
            + "For additional instructions on what you can say, say help me. "
            + neighborhoodPrompt;

    // Create the Simple card content
    SimpleCard card = new SimpleCard();
    card.setTitle(SKILL_NAME);
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(neighborhoodPrompt);

    // Create reprompt
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);

    return newAskResponse(speech, reprompt, card);
  }

  /**
   * Creates and returns a {@code SpeechletResponse} for when a user would like the skill to stop.
   *
   * @return SpeechletResponse spoken response for the given intent
   */
  private SpeechletResponse getStopResponse() {
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText("Goodbye!");

    return SpeechletResponse.newTellResponse(speech);
  }

  /**
   * Creates and returns a {@code SpeechletResponse} with a welcome message and card.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getHelpResponse() {
    String speechText = "Welcome to the " + SKILL_NAME + " skill."
            + " You can get crime and incident data for the city of Cincinnati."
            + " For example, you could say, give me a crime report."
            + " For additional instructions on what you can say, please say help me.";

    // Create the Simple card content
    SimpleCard card = new SimpleCard();
    card.setTitle(SKILL_NAME);
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    // Create reprompt
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);

    return newAskResponse(speech, reprompt, card);
  }

  /**
   *  Creates and returns a {@code SpeechletResponse} with a message and card.
   *
   * @param intent current intent
   * @return SpeechletResponse  spoken and visual response for crime report intent
   */
  private SpeechletResponse getCrimeReportResponse(final Intent intent) {
    String neighborhoodSlot = intent.getSlot(SLOT_NEIGHBORHOOD).getValue();
    String alexaDateSlot = intent.getSlot(SLOT_DATE).getValue();
    String alexaDateStringSlot = intent.getSlot(SLOT_DATE_STRING).getValue();
    List<String> dates = null;

  if(neighborhoodSlot != null) {
    // Get the Neighborhood requested
    try {
      String neighborhood = getNeighborHoodFromSlot(neighborhoodSlot);
    } catch (NeighborhoodNotSupportedException nex) {
      log.error("Neighborhood not supported.", nex);

      String speechOutput = "Sorry, crime and incident data is not supported for that neighborhood. "
                            + NEIGHBORHOOD_PROMPT;

      return buildAskResponse(speechOutput, null);
    }
  }

    // Get the date requested
    try {
      dates = getDatesFromIntent(intent);
    } catch(DateRangeException dex) {
      log.error("Date requested is in the future or not supported.", dex);

      String speechOutput = "Sorry, crime and incident data is not supported for that date. "
              + NEIGHBORHOOD_PROMPT;

      return buildAskResponse(speechOutput, null);
    }

    // Get the date string request
    try {
      dates = getDatesFromDateStringIntent(intent);
    } catch(Exception ex) {
      log.error("Date string request error.", ex);

      String speechOutput = "Sorry, crime and incident data is not supported for that date. "
              + NEIGHBORHOOD_PROMPT;

      return buildAskResponse(speechOutput, null);
    }


    SocrataClient socrataClient = new SocrataClient(SOCRATA_TOKEN, SOCRATA_CRIME_API);
    //List<CrimeReport> crimeReports = socrataClient.getCrimeReports(neighborhood, dates);
    //Map<String, List> crimeReportsMap = filterCrimeReports(crimeReports);

    return null;
  }

  /**
   * Creates and returns a {@code SpeechletResponse} with a list of neighborhoods and card.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getSupportedNeighborhoodsResponse() {
    String repromptText = "Which neighborhood would you like a crime report for?";
    String speechOutputText = "Currently, I have crime and incident data for the following Cincinnati neighborhoods: "
            + getAllNeighborhoods() + repromptText;

    // Card
    SimpleCard card = new SimpleCard();
    card.setTitle(SKILL_NAME + " - Neighborhoods");
    card.setContent(speechOutputText);

    // Plaintext Original Speech
    PlainTextOutputSpeech speechOutput = new PlainTextOutputSpeech();
    speechOutput.setText(speechOutputText);

    // Plaintext Reprompt Speech
    PlainTextOutputSpeech repromptSpeechOutput = new PlainTextOutputSpeech();
    repromptSpeechOutput.setText(repromptText);

    Reprompt repromptOutput = new Reprompt();
    repromptOutput.setOutputSpeech(repromptSpeechOutput);

    return newAskResponse(speechOutput, repromptOutput, card);
  }

  /**
   * Retrieve a listing of all of the supported neighborhoods, in a speech friendly format.
   *
   * @return String list of all supported neighborhoods
   */
  private String getAllNeighborhoods() {
    StringBuilder stringBuilder = new StringBuilder();

    for(String neighborhood : Neighborhoods.getNeighborhoods()) {
      // Replace additional spaces in between words
      String neighborhoodFormatted = neighborhood.trim().replaceAll(" +", " ");
      stringBuilder.append(neighborhoodFormatted);
      stringBuilder.append(", ");
    }

    stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.length());

    return stringBuilder.toString();
  }


  private List<String> getDatesFromDateStringIntent(final Intent intent) throws DateStringNotSupportedException, DateRangeException {
    String dateString = intent.getSlot(SLOT_DATE_STRING).getValue();

    // Date String wasn't requested, return immediately
    if(dateString == null) {
      return null;
    }

   List<String> dateStringDates = DateStringUtil.getFormattedDates(dateString);

    return null;
  }

  /**
   * Retrieves the requested neighborhood slot value if it is supported, otherwises throws an exception.
   *
   * @param neighborhood String requested neighborhood
   * @return requested neighborhood if exists, null otherwise
   * @throws NeighborhoodNotSupportedException throws Exception if neighborhood is not supported
   */
  private String getNeighborHoodFromSlot(String  neighborhood) throws NeighborhoodNotSupportedException {
    List<String> neighborhoods = Neighborhoods.getNeighborhoods();
    boolean neighborhoodExists = neighborhoods.stream().anyMatch(s -> s.equalsIgnoreCase(neighborhood.replace(" +", " ")));

    if(neighborhoodExists) {
      return neighborhood;
    } else {
      // requested neighborhood isn't available
      throw new NeighborhoodNotSupportedException(neighborhood);
    }
  }

  /**
   * Retrieves the requested date, if applicable.
   *
   * @param intent Intent that holds slots
   * @return String date in string format
   * @throws DateRangeException thrown if the date provided is in the future
   */
  private List<String> getDatesFromIntent(final Intent intent) throws DateRangeException {
    String alexaDate = intent.getSlot(SLOT_DATE).getValue();

    if(alexaDate == null) {
      return null;
    }

    String socrataStringDate = AlexaDateUtil.getFormattedDate(alexaDate);

    if(socrataStringDate == null) {
      throw new DateRangeException("Unsupported date: " + alexaDate);
    }

    // Convert socrataDate to a real date to compare against todays date
    LocalDate socrataDate = LocalDate.parse(socrataStringDate, DateTimeFormatter.ISO_DATE);

    if(socrataDate.isAfter(LocalDate.now())) {
      throw new DateRangeException("Future date: " + socrataDate.toString());
    }

    List<String> dateList = new ArrayList<>();
    dateList.add(socrataStringDate + TIME_START);
    dateList.add(socrataStringDate + TIME_END);

    return dateList;
  }

  private Map<String, List<CrimeReport>> filterCrimeReports(List<CrimeReport> crimeReportsList) {
    Map<String, List<CrimeReport>> crimeReportsMap = new HashMap<>();

    for(CrimeReport crimeReport : crimeReportsList) {
      String offense = crimeReport.getOffense();

      if(crimeReportsMap.containsKey(offense)) {
        crimeReportsMap.get(offense).add(crimeReport);
      } else {
        // Add record to
        crimeReportsMap.put(offense, Arrays.asList(crimeReport));
      }
    }

    return crimeReportsMap;
  }

  /**
   * Helper method for building a {@code SpeechletResponse}.
   *
   * @param speechOutput String plaintext output
   * @param card Simplecard card
   * @return SpeechletResponse with a card (if applicable)
   */
  private SpeechletResponse buildAskResponse(String speechOutput, SimpleCard card) {
    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechOutput);

    // Create reprompt
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);

    if (card == null) {
      return newAskResponse(speech, reprompt);
    } else {
      return newAskResponse(speech, reprompt, card);
    }
  }
}