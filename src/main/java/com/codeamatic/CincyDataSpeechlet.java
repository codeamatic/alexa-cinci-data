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
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.codeamatic.exceptions.DateRangeException;
import com.codeamatic.exceptions.DateStringNotSupportedException;
import com.codeamatic.socrata.CrimeReport;
import com.codeamatic.socrata.support.SocrataClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.amazon.speech.speechlet.SpeechletResponse.newAskResponse;
import static com.amazon.speech.speechlet.SpeechletResponse.newTellResponse;

/**
 * Cinci Data Speechlet for handling Alexa Speechlet requests.
 */
public class CincyDataSpeechlet implements Speechlet {
  private static final Logger log = LoggerFactory.getLogger(CincyDataSpeechlet.class);

  private static final String SOCRATA_TOKEN = System.getenv("SOCRATA_CINCY_TOKEN");
  private static final String SOCRATA_CRIME_API = System.getenv("SOCRATA_CINCY_CRIME_API");
  private static final String SKILL_NAME = "Cincy Data";

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
      case "AMAZON.HelpIntent":
        return getHelpResponse();
      case "AMAZON.StopIntent":
      case "AMAZON.CancelIntent":
        return buildTellResponse("Goodbye", null);
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
    String speechText = "<p>Welcome to the " + SKILL_NAME + " skill. "
            + "You can get crime and incident data for the city of Cincinnati.</p>"
            + "<p>Just ask for a crime report.</p>";

    return buildTellResponse(speechText, null);
  }

  /**
   * Creates and returns a {@code SpeechletResponse} with a welcome message and card.
   *
   * @return SpeechletResponse spoken and visual response for the given intent
   */
  private SpeechletResponse getHelpResponse() {
    String speechText = "<p>You can get crime and incident data for the city of Cincinnati.</p>"
            + "<p>Just ask for a crime report.</p>";

    return buildTellResponse(speechText, null);
  }

  /**
   *  Creates and returns a {@code SpeechletResponse} with a message and card.
   *
   * @param intent current intent
   * @return SpeechletResponse  spoken and visual response for crime report intent
   */
  private SpeechletResponse getCrimeReportResponse(final Intent intent) {
    String neighborhood = null;

    SocrataClient socrataClient = new SocrataClient(SOCRATA_TOKEN, SOCRATA_CRIME_API);
    List<CrimeReport> crimeReports = socrataClient.getCrimeReports(neighborhood, null);

    String outputVerbiage = this.generateSpeechOutput(crimeReports, neighborhood);
    SimpleCard card = this.generateSpeechCard(crimeReports, neighborhood);

    return buildTellResponse(outputVerbiage, card);
  }

  /**
   * Helper method for building a {@code SpeechletResponse} for "Tell".
   * @param speechOutput String plaintext output
   * @param card Simplecard card
   * @return SpeechletResponse with a card (if applicable)
   */
  private SpeechletResponse buildTellResponse(String speechOutput, SimpleCard card) {
    // Create the ssml text output.
    SsmlOutputSpeech speech = new SsmlOutputSpeech();
    speech.setSsml(speechOutput);

    if (card == null) {
      return newTellResponse(speech);
    } else {
      return newTellResponse(speech, card);
    }
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
    // Create the ssml text output.
    SsmlOutputSpeech speech = new SsmlOutputSpeech();
    speech.setSsml(speechOutput);

    // Set reprompt
    if(repromptText != null) {
      SsmlOutputSpeech repromptSpeechOutput = new SsmlOutputSpeech();
      repromptSpeechOutput.setSsml(repromptText);

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
   * Retrieve the total number of incidents across all offenses.
   *
   * @param crimeReports List of crime reports reported
   * @return a total count of incidents
   */
  private int getCrimeReportCount(List<CrimeReport> crimeReports) {
    int count = 0;

    for(CrimeReport crimeReport : crimeReports) {
      count += Integer.parseInt(crimeReport.getCount());
    }

    return count;
  }

  /**
   * Generates the verbiage output for a successful crime report query.
   *
   * @param crimeReports List of crime reports
   * @param neighborhood String the neighborhood queried
   * @return String
   */
  private String generateSpeechOutput(List<CrimeReport> crimeReports, String neighborhood) {
      int numReports = getCrimeReportCount(crimeReports);
      String reportCount = (numReports > 0) ? Integer.toString(numReports) : "no";
      String location = (neighborhood != null) ? neighborhood : "Cincinnati";

      return "There were " + reportCount + " crimes reported in " + location + " yesterday.";
  }

  /**
   * Generate a SimpleCard from a list of crime reports and a neighborhood.
   *
   * @param crimeReports List of crime reports
   * @param neighborhood String neighborhood being requested
   * @return a SimpleCard
   */
  private SimpleCard generateSpeechCard(List<CrimeReport> crimeReports, String neighborhood) {
    int numReports = getCrimeReportCount(crimeReports);
    String reportCount = (numReports > 0) ? Integer.toString(numReports) : "no";
    String location = (neighborhood != null) ? neighborhood : "Cincinnati";

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(reportCount);
    stringBuilder.append(" crimes were reported yesterday.");
    stringBuilder.append("\n\n");

    for(CrimeReport crimeReport : crimeReports) {
      stringBuilder.append(crimeReport.getCount());
      stringBuilder.append(" - ");
      stringBuilder.append(crimeReport.getOffense().replaceAll("-", ""));
      stringBuilder.append("\n");
    }

    // Create the Simple card content
    SimpleCard card = new SimpleCard();
    card.setTitle("Crime Report - " + location);
    card.setContent(stringBuilder.toString());

    return card;
  }
}