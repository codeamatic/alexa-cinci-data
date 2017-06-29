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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cinci Data Speechlet for handling Alexa Speechlet requests.
 */
public class CincyDataSpeechlet implements Speechlet {
  private static final Logger log = LoggerFactory.getLogger(CincyDataSpeechlet.class);

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
        return getWelcomeResponse();
      case "AMAZON.HelpIntent":
        return getWelcomeResponse();
      case "AMAZON.StopIntent":
        return getWelcomeResponse();
      case "AMAZON.CancelIntent":
        return getWelcomeResponse();
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
    String speechText = "Welcome to the " + SKILL_NAME + " skill."
            + " You can get crime and incident data for the city of Cincinnati."
            + " For example, you could say, give me a crime report."
            + " For additional instructions on what you can say, please say help me.";

    // Create the Simple card content
    SimpleCard card = new SimpleCard();
    card.setTitle("Cincy Data");
    card.setContent(speechText);

    // Create the plain text output.
    PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
    speech.setText(speechText);

    // Create reprompt
    Reprompt reprompt = new Reprompt();
    reprompt.setOutputSpeech(speech);

    return SpeechletResponse.newAskResponse(speech, reprompt, card);
  }
}
