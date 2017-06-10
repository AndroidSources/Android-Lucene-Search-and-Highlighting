package com.portalme.lucenetest.lucene;


import com.portalme.lucenetest.model.Document;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;

import java.util.List;

public class SearchResult {
  public final int totalHits;
  public final List<Document> documents;
  final ScoreDoc lastScoreDoc;
  final Query query;
  final Sort sort;
  final HighlightingHelper highlightingHelper;

  SearchResult(int totalHits, List<Document> documents, ScoreDoc lastScoreDoc, Query query, Sort sort,
               HighlightingHelper highlightingHelper) {
    this.totalHits = totalHits;
    this.documents = documents;
    this.lastScoreDoc = lastScoreDoc;
    this.query = query;
    this.sort = sort;
    this.highlightingHelper = highlightingHelper;
  }

  public boolean hasMore() {
    return lastScoreDoc != null;
  }

  public String getHighlightedTitle(Document doc) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.TITLE_FIELD_NAME, doc.title);
  }

  public String getHighlightedReview(Document doc) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.REVIEW_FIELD_NAME, doc.review);
  }

  public String getFullHighlightedReview(Document doc) {
    highlightingHelper.setFragmentLength(Integer.MAX_VALUE);
    return highlightingHelper.highlightOrOriginal(Indexer.REVIEW_FIELD_NAME, doc.review);
  }
}
