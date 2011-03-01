package hu.mokk.hunglish.jmorph;

import hu.mokk.hunglish.domain.Bisen;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
public class AnalyzerProviderTest {

	@Autowired
	private AnalyzerProvider analyzerProvider;

	@Test
	public void testLemmatizerMap() {
		List<String> list = analyzerProvider.getLemmatizerMap().get(
				Bisen.huSentenceFieldName).lemmatize("embereinket");
		for (String o : list){
			System.out.println(o);
		}
	}

}
