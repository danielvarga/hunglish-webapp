/**
 * 
 */
package hu.mokk.hunglish.lucene;

import hu.mokk.hunglish.domain.Bisen;
import hu.mokk.hunglish.jmorph.AnalyzerProvider;

import java.io.File;
import java.io.IOException;

import net.sf.jhunlang.jmorph.parser.ParseException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author bpgergo
 * 
 */
@Configurable
public class Indexer {

	/**
	 * create a new index in the tmp dir or append docs to an index already
	 * existing in the main index dir
	 * 
	 * @author bpgergo
	 * 
	 */
	public enum CreateOrAppend {
		Create, Append
	}

	@Autowired
	private AnalyzerProvider analyzerProvider;
	@Autowired
	private Integer mergeFactor = 100;
	@Autowired
	private Integer maxBufferedDocs = 1000;
	@Autowired
	private String indexDir;
	@Autowired
	private String tmpIndexDir;
	
	private IndexWriter indexWriter;

	private void initIndexWriter(CreateOrAppend createOrAppend) {
		if (analyzerProvider == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The analyzerProvider is null.");
		}
		boolean create = createOrAppend.compareTo(CreateOrAppend.Create) == 0;
		String dir = create ? tmpIndexDir : indexDir;
		if (dir == null) {
			throw new IllegalStateException(
					"Cannot create indexWriter. The directory is null.");
		}
		try {
			indexWriter = new IndexWriter(new SimpleFSDirectory(new File(dir)),
					analyzerProvider.getAnalyzer(), create,
					IndexWriter.MaxFieldLength.UNLIMITED);
		} catch (CorruptIndexException e) {
			throw new RuntimeException("Cannot open index directory.", e);
		} catch (LockObtainFailedException e) {
			throw new RuntimeException("Cannot open index directory.", e);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open index directory.", e);
		}
		if (mergeFactor != null){
			indexWriter.setMergeFactor(mergeFactor);
		}
		indexWriter.setMaxBufferedDocs(maxBufferedDocs);

	}

	synchronized public void indexAll(CreateOrAppend createOrAppend)
			throws CorruptIndexException, LockObtainFailedException,
			IOException, IllegalAccessException, InstantiationException,
			ParseException {
		initIndexWriter(createOrAppend);
		Bisen.indexAll(indexWriter);
	}

	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}

	public void setMergeFactor(Integer mergeFactor) {
		this.mergeFactor = mergeFactor;
	}

	public void setMaxBufferedDocs(Integer maxBufferedDocs) {
		this.maxBufferedDocs = maxBufferedDocs;
	}

	public void setIndexDir(String indexDir) {
		this.indexDir = indexDir;
	}

	public void setTmpIndexDir(String tmpIndexDir) {
		this.tmpIndexDir = tmpIndexDir;
	}

}
