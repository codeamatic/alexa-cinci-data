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
import java.util.List;

import static com.amazon.speech.speechlet.SpeechletResponse.newAskResponse;

/**
 * Cinci Data Speechlet for handling Alexa Speechlet requests.
 */
public class CincyDataSpeechlet implements Speechlet {
  private static final Logger log = LoggerFactory.getLogger(CincyDataSpeechlet.class);

  private static final String SOCRATA_TOKEN = System.getenv("SOCRATA_CINCY_TOKEN");
  private static final String SOCRATA_CRIME_API = System.getenv("SOCRATA_CINCY_CRIME_API");
  private static final String SKILL_NAME = "Cincy Data";

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
   * @return
   */
  private SpeechletResponse getCrimeReportResponse(final Intent intent) {
    String neighborhood = null;
    List<String> dates = null;

    // Get the Neighborhood requested
    try {
      neighborhood = getNeighborHoodFromIntent(intent);
    } catch (NeighborhoodNotSupportedException nex) {
      log.error("Neighborhood not supported.", nex);

      String speechOutput = "Sorry, crime and incident data is not supported for that neighborhood. "
              + NEIGHBORHOOD_PROMPT;

      PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
      speech.setText(speechOutput);

      Reprompt reprompt = new Reprompt();
      reprompt.setOutputSpeech(speech);

      return newAskResponse(speech, reprompt);
    }

    // Get the date requested
    try {
      dates.add(getDateFromIntent(intent));
    } catch(DateRangeException dex) {
      log.error("Date requested is in the future or not supported.", dex);

      String speechOutput = "Sorry, crime and incident data is not supported for that date. "
              + NEIGHBORHOOD_PROMPT;

      PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
      speech.setText(speechOutput);

      Reprompt reprompt = new Reprompt();
      reprompt.setOutputSpeech(speech);

      return newAskResponse(speech, reprompt);
    }

    // Get the date string request
    try {
      dates = getDateFromDateStringIntent(intent);
    } catch(Exception ex) {
      log.error("Date string request error.", ex);

      String speechOutput = "Sorry, crime and incident data is not supported for that date. "
              + NEIGHBORHOOD_PROMPT;

      PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
      speech.setText(speechOutput);

      Reprompt reprompt = new Reprompt();
      reprompt.setOutputSpeech(speech);

      return newAskResponse(speech, reprompt);
    }


    SocrataClient socrataClient = new SocrataClient(SOCRATA_TOKEN, SOCRATA_CRIME_API);
    List<CrimeReport> crimeReports = socrataClient.getCrimeReports(neighborhood, dates);

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
    StringBuilder neighborhoodsList = new StringBuilder();

    for(String neighborhood : Neighborhoods.getNeighborhoods()) {
      neighborhoodsList.append(neighborhood);
      neighborhoodsList.append(", ");
    }

    return neighborhoodsList.toString();
  }


  private List<String> getDateFromDateStringIntent(final Intent intent) throws DateStringNotSupportedException {
    String dateString = intent.getSlot(SLOT_DATE_STRING).getValue();

    // Date String wasn't requested, return immediately
    if(dateString == null) {
      return null;
    }

   // List<String> dateStringDates = DateStringUtil.getFormattedDate(dateString);

    return null;
  }

  /**
   * Retrieves the requested neighborhood slot value if it is supported, otherwises throws an exception.
   *
   * @param intent Intent that holds slots
   * @return requested neighborhood if exists, null otherwise
   * @throws NeighborhoodNotSupportedException throws Exception if neighborhood is not supported
   */
  private String getNeighborHoodFromIntent(final Intent intent) throws NeighborhoodNotSupportedException {
    String neighborhood = intent.getSlot(SLOT_NEIGHBORHOOD).getValue();

    // neighborhood wasn't requested, return immediately
    if(neighborhood == null) {
      return null;
    }

    List<String> neighborhoods = Neighborhoods.getNeighborhoods();
    boolean neighborhoodExists = neighborhoods.stream().anyMatch(s -> s.equalsIgnoreCase(neighborhood));

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
  private String getDateFromIntent(final Intent intent) throws DateRangeException {
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

    return socrataStringDate;
  }

}
