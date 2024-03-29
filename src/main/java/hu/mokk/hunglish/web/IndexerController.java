package hu.mokk.hunglish.web;

import hu.mokk.hunglish.lucene.Indexer;
import hu.mokk.hunglish.lucene.Searcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//@RooWebScaffold(path = "indexer", automaticallyMaintainView = true, formBackingObject=null)
@RequestMapping("/indexer/**")
@Controller
public class IndexerController {

	
	//@Autowired
	//private Indexer indexer;
	
	//@Autowired
	//private Searcher searcher;
	

	@RequestMapping(value = "/indexer", method = RequestMethod.GET)
    public String indexer(
    		@RequestParam(value = "magic", required = false) String magic,
    		ModelMap modelMap) {

		/*
		if ("666".equals(magic)){
			indexer.indexAll();
			searcher.reInitSearcher();
		}*/
		
		/*
		if ("777".equals(magic)){
			boolean create = false; //true=temp that is index will be created in hunglishIndexTmp, false=main
			indexer.indexAll(create);
			searcher.reInitSearcher();
		}
		*/
		
        return "indexer";
    }
}
