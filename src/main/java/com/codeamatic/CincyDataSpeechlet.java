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
import com.codeamatic.socrata.CrimeReport;
import com.codeamatic.socrata.support.SocrataClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
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

  private static final String SLOT_NEIGHBORHOOD = "neighborhood";
  private static final String SLOT_DATE = "date";
  private static final String SLOT_DATE_STRING = "date_string";

  private static final String NEIGHBORHOOD_PROMPT = "Which neighborhood and date would you like a crime report for?";

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
    String neighborhood = intent.getSlot(SLOT_NEIGHBORHOOD).getValue();
    String alexaDate = intent.getSlot(SLOT_DATE).getValue();
    String alexaDateString = intent.getSlot(SLOT_DATE_STRING).getValue();
    String[] dates;

    if(neighborhood != null) {
      // Get the Neighborhood requested
      boolean validNeighborhood = validateNeighborHoodFromSlot(neighborhood);

      if(! validNeighborhood) {
        log.error("Neighborhood not supported.", neighborhood);

        String speechOutput = "Sorry, crime and incident data is not supported for that neighborhood. "
                              + NEIGHBORHOOD_PROMPT;

        return buildAskResponse(speechOutput, null, null);
      }
    }

    // Get the date requested using the Alexa date intent
    if(alexaDate != null) {
      try {
        dates = getDatesFromSlot(alexaDate);
      } catch(DateRangeException dex) {
        log.error("Date requested is not supported.", dex);

        String speechOutput = "Sorry, crime and incident data is not supported for that date. "
                              + NEIGHBORHOOD_PROMPT;

        return buildAskResponse(speechOutput, null,null);
      }
    }
    // Custom data slot intent
    else if(alexaDateString != null) {
      try {
        dates = getDatesFromDateString(alexaDateString);
      } catch (Exception ex) {
        log.error("Date string request error.", ex);

        String speechOutput = "Sorry, crime and incident data is not supported for that date. "
                              + NEIGHBORHOOD_PROMPT;

        return buildAskResponse(speechOutput, null, null);
      }
    }
    else {
      // No date was queried, default to yesterday
      LocalDate yesterday = LocalDate.now().minus(Period.ofDays(1));
      dates = new String[2];
      dates[0] = yesterday.toString();
      dates[1] = dates[0];
    }

    SocrataClient socrataClient = new SocrataClient(SOCRATA_TOKEN, SOCRATA_CRIME_API);
    List<CrimeReport> crimeReports = socrataClient.getCrimeReports(neighborhood, dates);
    String outputVerbiage = this.generateSpeechOutput(crimeReports, neighborhood, dates);

    return buildAskResponse(outputVerbiage, null, null);
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

  /**
   * Retrieve a date or dates requested by the user, but mapped to a custom  slot.
   * This differs from the default date slot by allowing for the use of date ranges.
   *
   *  Example: "Since last tuesday" => last tuesday through now
   *
   * @param dateString String that holds the current mapped custom slot.
   * @return String date range
   * @throws DateStringNotSupportedException When a date is not supported
   * @throws DateRangeException When a date is in the future
   */
  private String[] getDatesFromDateString(String dateString) throws DateStringNotSupportedException, DateRangeException {
    return DateStringUtil.getFormattedDates(dateString);
  }

  /**
   * Retrieves the requested neighborhood slot value if it is supported, otherwises throws an exception.
   *
   * @param neighborhood String requested neighborhood
   * @return requested neighborhood if exists, null otherwise
   */
  private boolean validateNeighborHoodFromSlot(String  neighborhood) {
    List<String> neighborhoods = Neighborhoods.getNeighborhoods();
    return neighborhoods.stream().anyMatch(s -> s.equalsIgnoreCase(neighborhood.replace(" +", " ")));
  }

  /**
   * Retrieves the requested date, if applicable.  Returns date in the format
   * YYYY-MM-DD::YYYY-MM-DD where the second date represents the end date (or today).
   *
   * @param alexaDate String that holds the current Alexa default date slot
   * @return String date in string format
   * @throws DateRangeException thrown if the date provided is in the future
   */
  private String[] getDatesFromSlot(String alexaDate) throws DateRangeException {
    String socrataStringDate = AlexaDateUtil.getFormattedDate(alexaDate);

    if(socrataStringDate == null) {
      throw new DateRangeException("Unsupported date: " + alexaDate);
    }

    // Convert socrataDate to a real date to compare against todays date
    LocalDate socrataDate = LocalDate.parse(socrataStringDate, DateTimeFormatter.ISO_DATE);

    if(socrataDate.isAfter(LocalDate.now())) {
      throw new DateRangeException("Future date: " + socrataDate.toString());
    }

    String[] dates = new String[2];
    dates[0] = socrataStringDate + DateStringUtil.TIME_START;

    return dates;
  }

  /**
   *
   * @param crimeReportsList
   * @return
   */
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
   * Helper method for building a {@code SpeechletResponse} for "Ask".
   *
   * @param speechOutput String plaintext output
   * @param repromptText String reprompt text.  If set to null, then the speechOutput will be used for reprompt
   * @param card Simplecard card
   * @return SpeechletResponse with a card (if applicable)
   */
  private SpeechletResponse buildAskResponse(String speechOutput, String repromptText, SimpleCard card) {
    Reprompt reprompt = new Reprompt();
    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechOutput);

    // Set reprompt
    if(repromptText != null) {
      PlainTextOutputSpeech repromptSpeechOutput = new PlainTextOutputSpeech();
      repromptSpeechOutput.setText(repromptText);

      reprompt.setOutputSpeech(repromptSpeechOutput);
    } else {
      reprompt.setOutputSpeech(speech);
    }

    if (card == null) {
      return newAskResponse(speech, reprompt);
    } else {
      return newAskResponse(speech, reprompt, card);
    }
  }

  /**
   * Generates the verbiage output for a successful crime report query.
   *
   * @param crimeReports List of crime reports
   * @param neighborhood String the neighborhood queried
   * @param dates Array of Strings queried
   * @return String
   */
  private String generateSpeechOutput(List<CrimeReport> crimeReports, String neighborhood, String[] dates) {
      int numReports = crimeReports.size();
      String location = (neighborhood != null) ? neighborhood : "Cincinnati";
      String dateSubtext = (dates[1] != null) ? dates[0] + " to " + dates[1] : dates[0];

      String output = "There were " + numReports + " crimes reported in " + location;
      output += (dates[1] != null) ? " from " : " on ";
      output += dateSubtext;

      return output;
  }

  private String generateSpeechCard(List<CrimeReport> crimeReports) {
    return "";
  }
}