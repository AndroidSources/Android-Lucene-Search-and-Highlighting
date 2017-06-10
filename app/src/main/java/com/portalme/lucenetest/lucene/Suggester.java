package com.portalme.lucenetest.lucene;

/**
 * Copyright (c) 2015 Lukhnos Liu
 *
 * Licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.suggest.DocumentDictionary;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import org.lukhnos.portmobile.file.Files;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Suggester implements AutoCloseable {
  static final int DEFAULT_SUGGESTION_COUNT = 10;
  static final String INDEX_NAME = "suggestion";

  final Path indexRootPath;
  final AnalyzingInfixSuggester suggester;
  int suggestionCount;


  public Suggester(String indexRoot) throws IOException {
    indexRootPath = Paths.get(indexRoot);
    Analyzer analyzer = Indexer.getAnalyzer();
    Directory suggestionDir = FSDirectory.open(getSuggestionIndexPath(indexRootPath));
    suggester = new AnalyzingInfixSuggester(suggestionDir, analyzer);
    suggestionCount = DEFAULT_SUGGESTION_COUNT;
  }

  static Path getSuggestionIndexPath(Path indexRoot) {
    return indexRoot.resolve(INDEX_NAME);
  }


  public static void rebuild(String indexRoot) throws IOException {
    Path indexRootPath = Paths.get(indexRoot);
    Path suggestionPath = getSuggestionIndexPath(indexRootPath);


    if (Files.exists(suggestionPath)) {
      Util.deletePath(suggestionPath);
    }


    Analyzer analyzer = Indexer.getAnalyzer();
    Directory suggestionDir = FSDirectory.open(getSuggestionIndexPath(indexRootPath));
    AnalyzingInfixSuggester suggester = new AnalyzingInfixSuggester(suggestionDir, analyzer);


    Directory indexDir = FSDirectory.open(Indexer.getMainIndexPath(indexRootPath));
    IndexReader reader = DirectoryReader.open(indexDir);


    DocumentDictionary docDict = new DocumentDictionary(reader, Indexer.TITLE_FIELD_NAME, null);
    InputIterator iterator = docDict.getEntryIterator();
    Set<BytesRef> titleSet = new HashSet<>();
    BytesRef next;
    while ((next = iterator.next()) != null) {
      if (titleSet.contains(next)) {
        continue;
      }

      titleSet.add(next);
      suggester.add(next, null, 0, null);
    }

    reader.close();

    suggester.commit();
    suggester.close();
  }

  public List<String> suggest(String query) throws IOException {
    List<Lookup.LookupResult> results = suggester.lookup(query, suggestionCount, false, true);

    List<String> suggestions = new ArrayList<String>();
    for (Lookup.LookupResult result : results) {
      if (result.highlightKey instanceof String) {
        suggestions.add((String) result.highlightKey);
      } else {
        suggestions.add(result.key.toString());
      }
    }

    return suggestions;
  }

  @Override
  public void close() throws Exception {
    suggester.close();
  }
}
