package com.portalme.lucenetest.lucene;


import com.portalme.lucenetest.model.Document;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.Paths;
import java.util.List;


public class Indexer implements AutoCloseable {
  static final String TITLE_FIELD_NAME = "title";
  static final String YEAR_FIELD_NAME = "year";
  static final String RATING_FIELD_NAME = "rating";
  static final String POSITIVE_FIELD_NAME = "positive";
  static final String REVIEW_FIELD_NAME = "review";
  static final String SOURCE_FIELD_NAME = "source";

  static final String INDEX_NAME = "main";

  final IndexWriter indexWriter;


  public Indexer(String indexRoot) throws IOException {
    this(indexRoot, false);
  }


  public Indexer(String indexRoot, boolean appendIfExists) throws IOException {
    Path indexRootPath = Paths.get(indexRoot);
    Analyzer analyzer = getAnalyzer();

    Directory mainIndexDir = FSDirectory.open(getMainIndexPath(indexRootPath));
    IndexWriterConfig mainIndexWriterConfig = new IndexWriterConfig(analyzer);

    if (appendIfExists) {
      mainIndexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    } else {
      mainIndexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    }

    indexWriter = new IndexWriter(mainIndexDir, mainIndexWriterConfig);
  }

  public static Analyzer getAnalyzer() {
    return new EnglishAnalyzer();
  }

  static Integer getInteger(org.apache.lucene.document.Document luceneDoc, String fieldName) {
    IndexableField field = luceneDoc.getField(fieldName);
    if (field != null) {
      Number number = field.numericValue();
      if (number != null) {
        return number.intValue();
      }
    }
    return null;
  }

  static Document fromLuceneDocument(org.apache.lucene.document.Document luceneDoc) {
    String title = luceneDoc.get(TITLE_FIELD_NAME);

    Integer number;
    int year;
    number = getInteger(luceneDoc, YEAR_FIELD_NAME);
    year = (number == null) ? 0 : number;

    int rating;
    number = getInteger(luceneDoc, RATING_FIELD_NAME);
    rating = (number == null) ? 0 : number;

    boolean positive;
    number = getInteger(luceneDoc, POSITIVE_FIELD_NAME);
    positive = (number == null) ? false : (number != 0);

    String review = luceneDoc.get(REVIEW_FIELD_NAME);
    String source = luceneDoc.get(SOURCE_FIELD_NAME);

    return new Document(title, year, rating, positive, review, source);
  }

  static Path getMainIndexPath(Path indexRoot) {
    return indexRoot.resolve(INDEX_NAME);
  }

  @Override
  public void close() throws Exception {
    indexWriter.close();
  }

  public void addDocuments(List<Document> docs) throws IOException {
    Field titleField = new TextField(TITLE_FIELD_NAME, "", Field.Store.YES);
    Field titleDocsValueField = new SortedDocValuesField(TITLE_FIELD_NAME, new BytesRef(0));
    Field yearField = new IntField(YEAR_FIELD_NAME, 0, Field.Store.YES);
    Field yearDocsValueField = new NumericDocValuesField(YEAR_FIELD_NAME, 0L);
    Field ratingField = new IntField(RATING_FIELD_NAME, 0, Field.Store.YES);
    Field ratingDocsValueField = new NumericDocValuesField(RATING_FIELD_NAME, 0L);
    Field positiveField = new IntField(POSITIVE_FIELD_NAME, 0, Field.Store.YES);
    Field reviewField = new TextField(REVIEW_FIELD_NAME, "", Field.Store.YES);
    Field sourceField = new StringField(SOURCE_FIELD_NAME, "", Field.Store.YES);

    for (Document doc : docs) {
      org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();

      if (doc.title != null && !doc.title.isEmpty()) {
        titleField.setStringValue(doc.title);
        luceneDoc.add(titleField);

        titleDocsValueField.setBytesValue(new BytesRef(doc.title));
        luceneDoc.add(titleDocsValueField);
      }

      yearField.setIntValue(doc.year);
      luceneDoc.add(yearField);

      yearDocsValueField.setLongValue((long) doc.year);
      luceneDoc.add(yearDocsValueField);

      ratingField.setIntValue(doc.rating);
      luceneDoc.add(ratingField);

      ratingDocsValueField.setLongValue((long) doc.rating);
      luceneDoc.add(ratingDocsValueField);

      positiveField.setIntValue(doc.positive ? 1 : 0);
      luceneDoc.add(positiveField);

      if (doc.review != null && !doc.review.isEmpty()) {
        reviewField.setStringValue(doc.review);
        luceneDoc.add(reviewField);
      }

      if (doc.source != null && !doc.source.isEmpty()) {
        sourceField.setStringValue(doc.source);
        luceneDoc.add(sourceField);
      }

      indexWriter.addDocument(luceneDoc);
    }

    indexWriter.commit();
  }
}
