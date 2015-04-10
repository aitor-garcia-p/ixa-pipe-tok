/*
 *Copyright 2015 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package eus.ixa.ixa.pipe.seg;

import java.util.Properties;
import java.util.regex.Pattern;

import eus.ixa.ixa.pipe.tok.NonBreaker;

public class RuleBasedSegmenter implements SentenceSegmenter {
 
  /**
   * The constant representing every line break in the original input text.
   */
  public static final String LINE_BREAK = "<JAR>";
  /**
   * Constant representing a paragraph (a doubleLine) in the original input text.
   */
  public static final String PARAGRAPH = "<P>";
  /**
   * Line break pattern.
   */
  public static Pattern lineBreak = Pattern.compile("<JAR>");
  /**
   * Two lines.
   */
  public static Pattern doubleLine = Pattern.compile("<JAR><JAR>");
  /**
   * Paragraph pattern.
   */
  public static Pattern paragraph = Pattern.compile("(<P>+)");
  /**
   * If space paragraph mark and lowercase then it is a spurious paragraph.
   */
  //TODO extend to other expressions different from lower?
  public static Pattern spuriousParagraph = Pattern.compile("<P>(\\s*\\p{Lower})", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Non-period end of sentence markers (?!) followed by sentence starters.
   */
  public static Pattern noPeriodEnd = Pattern
      .compile("([?!])([<P>\\ ]+)([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Multi-dots followed by sentence starters.
   */
  public static Pattern multiDotsStarters = Pattern
      .compile("(\\.[\\.]+)([<P>\\ ]+)([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * Wrongly introduced periods; Centraal.There.
   */
  public static Pattern wrongPeriods = Pattern.
      compile("(\\w+[\\.]+)([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  
  /**
   * Some sort of punctuation inside a quote or parenthetical followed by a possible
   * sentence starter punctuation and upper case.
   */
  public static Pattern endInsideQuotes = Pattern
      .compile("([?!\\.][\\ ]*[\'\"\\)\\]\\%\u00BB\u003D\u0092\u0094\u201D\u203A\u2019]+)([<P> ]+)([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]*[\\ ]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);

  /**
   *  End with some sort of punctuation and followed by a sentence starter punctuation
   *  and upper case.
   */
  public static Pattern punctUpper = Pattern
      .compile("([?!\\.])([<P>\\ ]+)([\'\"\\(\\[\\¿\\¡\u00AB\u003C\u0091\u0093\u201B\u201C\u201F\u2018\u2039]+[\\ ]*[\\p{Lu}])", Pattern.UNICODE_CHARACTER_CLASS);
  /**
   * End of sentence punctuation, spaces and link.
   */
  public static Pattern endPunctLink = Pattern.compile("([?!\\.])([<P>\\s]+)(http.+|www+)", Pattern.UNICODE_CHARACTER_CLASS);

  /**
   * The nonbreaker decides when to split strings followed by periods.
   */
  private NonBreaker nonBreaker;

  public RuleBasedSegmenter(Properties properties) {
    if (nonBreaker == null) {
      nonBreaker = new NonBreaker(properties);
    }
  }
  
  public String[] segmentSentence(String text) {
    text = buildText(text);
    String[] sentences = sentenceSplitter(text);
    return sentences;
  }

  private String[] sentenceSplitter(String text) {
    
    // non-period end of sentence markers (?!) followed by sentence starters.
    text = noPeriodEnd.matcher(text).replaceAll("$1$2\n$3");
    // multi-dots followed by sentence starters
    text = multiDotsStarters.matcher(text).replaceAll("$1$2\n$3");
    text = wrongPeriods.matcher(text).replaceAll("$1\n$2");
    // end of sentence inside quotes or brackets
    text = endInsideQuotes.matcher(text).replaceAll("$1$2\n$3");
    // add breaks for sentences that end with some sort of punctuation are
    // followed by a sentence starter punctuation and upper case
    text = punctUpper.matcher(text).replaceAll("$1$2\n$3");
    text = endPunctLink.matcher(text).replaceAll("$1$2\n$3");
    
    // non prefix breaker detects exceptions to sentence breaks
    text = nonBreaker.SegmenterNonBreaker(text);
    
    //remove spurious paragraphs
    text = spuriousParagraph.matcher(text).replaceAll("  $1");
    //add break if <P>
    text = paragraph.matcher(text).replaceAll("$1\n");
    String[] sentences = text.split("\n");
    return sentences;
  }
  
  public String buildText(String text) {
    //<JAR><JAR> to <P>
    text = doubleLine.matcher(text).replaceAll(RuleBasedSegmenter.PARAGRAPH);
    //<JAR> to " "
    text = lineBreak.matcher(text).replaceAll(" ");
    return text;
  }

}
