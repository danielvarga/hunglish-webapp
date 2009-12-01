/*
 * Created on Jan 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mokk.nlp.irutil;



import org.apache.avalon.fortress.ContainerManager;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.DefaultContainerManager;
import org.apache.avalon.fortress.util.FortressConfig;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * @author hp
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConsoleContainer {


    /**
     * @param args a <code>String[]</code> array of command line arguments
     * @throws Exception
     * @exception java.lang.Exception if an error occurs
     */
    public static final void main( String[] args ) throws Exception
    
    {
    		if (args.length < 2) {
			System.err.println("usage: IndexerConsole conffile logconf [-f filename]");
			
			System.exit(-1);
		}
    	
    		String input = null;
    		for(int i = 0; i < args.length; i++ ) {
    		    if(args[i].equals("-f")) {
    		        if(i< args.length -1) {
    		            input = args[i+1];
    		        }
    		    }
    		}
    		
    		
        FortressConfig config = new FortressConfig();
        config.setContainerClass( DefaultContainer.class );
        config.setContainerConfiguration( args[0] );
        config.setLoggerManagerConfiguration( args[1] );
        config.setContextDirectory( "./" );
        config.setWorkDirectory( "./" );
        

        ContainerManager m_containerManager = new DefaultContainerManager( config.getContext() );
        try {
			// ContainerUtil.configure(m_containerManager, new DefaultConfiguration("vacak"));
			ContainerUtil.initialize( m_containerManager );
		
			DefaultContainer container = (DefaultContainer) m_containerManager.getContainer();
        
			ServiceManager manager = container.getServiceManager();
       
		
			
			Indexer indexer = (Indexer) container.getServiceManager().lookup(Indexer.ROLE);
         
			indexer.index();
        } catch (Exception e) {
			
			e.printStackTrace();
		} finally {
			ContainerUtil.dispose( m_containerManager );
		}
   }
}
