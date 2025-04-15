//package rt.model.note;
//
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.ru.RussianAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.StringField;
//import org.apache.lucene.document.TextField;
//import org.apache.lucene.index.*;
//import org.apache.lucene.queryparser.classic.ParseException;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.TermQuery;
//import org.apache.lucene.store.ByteBuffersDirectory;
//import org.apache.lucene.store.Directory;
//
//import java.io.IOException;
//
//public class LuceneTextMatchNoteFinder extends TextMatchNoteFinder implements AutoCloseable {
//
//    private final Analyzer analyzer;
//    private final Directory directory;
//
//    public LuceneTextMatchNoteFinder() {
//        this.analyzer = new RussianAnalyzer();
//        this.directory = new ByteBuffersDirectory();
//    }
//
//    @Override
//    protected boolean noteIsSuitable(Note note) {
//        if (note.getText().isEmpty() || note.getText().isBlank()) {
//            return false;
//        }
//        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
//            writer.deleteAll();
//
//            Document doc = new Document();
//            doc.add(new TextField("content", note.getText(), Field.Store.NO));
//            doc.add(new StringField("content_exact", note.getText(), Field.Store.NO));
//            writer.addDocument(doc);
//            writer.commit();
//
//            try (IndexReader reader = DirectoryReader.open(directory)) {
//                IndexSearcher searcher = new IndexSearcher(reader);
//                QueryParser parser = new QueryParser("content", analyzer);
//
//                for (String arg : args) {
//                    Query exactQuery = new TermQuery(new Term("content_exact", arg));
//                    if (searcher.search(exactQuery, 1).totalHits.value() > 0) {
//                        return true;
//                    }
//                    try {
//                        Query morphQuery = parser.parse(QueryParser.escape(arg));
//                        if (searcher.search(morphQuery, 1).totalHits.value() > 0) {
//                            return true;
//                        }
//                    } catch (ParseException _) {
//                    }
//                }
//            }
//        } catch (IOException _) {
//        }
//        return false;
//    }
//
//    @Override
//    public void close() {
//        try {
//            directory.close();
//        } catch (IOException _) {
//        }
//        analyzer.close();
//    }
//}