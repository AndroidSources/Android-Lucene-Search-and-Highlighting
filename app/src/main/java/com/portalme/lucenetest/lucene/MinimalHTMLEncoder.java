package com.portalme.lucenetest.lucene;



import org.apache.lucene.search.highlight.Encoder;


public class MinimalHTMLEncoder implements Encoder {
  public final static String htmlEncode(String plainText) {
    if (plainText == null || plainText.length() == 0) {
      return "";
    }

    StringBuilder result = new StringBuilder(plainText.length());
    for (int index = 0; index < plainText.length(); index++) {
      char ch = plainText.charAt(index);

      switch (ch) {
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        default:
          result.append(ch);
      }
    }

    return result.toString();
  }

  @Override
  public String encodeText(String originalText) {
    return htmlEncode(originalText);
  }
}
