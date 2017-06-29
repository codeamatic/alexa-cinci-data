package com.codeamatic;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is the handler for the AWS Lambda function powering this skills kit.
 */
public final class CincyDataSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
  private static final Set<String> supportedApplicationIds = new HashSet<String>();

  static {
    supportedApplicationIds.add("amzn1.ask.skill.39f6ac17-40f7-48e6-aa59-4a38583b728c");
  }

  public CincyDataSpeechletRequestStreamHandler() {
    super(new CincyDataSpeechlet(), supportedApplicationIds);
  }
}