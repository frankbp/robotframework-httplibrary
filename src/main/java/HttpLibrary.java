import com.netease.common.HttpConnection;

import org.robotframework.javalib.annotation.Autowired;
import org.robotframework.javalib.library.AnnotationLibrary;

public class HttpLibrary extends AnnotationLibrary {

	public static final String KEYWORD_PATTERN = "com/netease/common/**/*.class";
	public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";
	public static final String ROBOT_LIBRARY_VERSION = "0.0.1";
	public static final String ROBOT_LIBRARY_DOC_FORMAT = "ROBOT";
	public static final String LIBRARY_DOCUMENTATION = "HttpLibrary is a library to implement HTTP methods, including GET and POST";
	public static final String LIBRARY_INITIALIZATION_DOCUMENTATION = "HttpLibrary can be imported directly without any arguments.\n\r"
			+ "\n"
			+ "Examples:\n"
			+ "| Library | HttpLibrary |\n";

	public HttpLibrary() {
		super("com/acme/**/keyword/**/*.class");
        addKeywordPattern(KEYWORD_PATTERN);
        createKeywordFactory(); // => init annotations
	}
	
	@Autowired
	protected HttpConnection httpConnection;
	
	@Override
    public Object runKeyword(String keywordName, Object[] args) {
        return super.runKeyword(keywordName, toStrings(args));
    }
    
    @Override
    public String getKeywordDocumentation(String keywordName) {
    	if (keywordName.equals("__intro__"))
            return LIBRARY_DOCUMENTATION;
    	if (keywordName.equals("__init__"))
            return LIBRARY_INITIALIZATION_DOCUMENTATION;
    	try {
    		return super.getKeywordDocumentation(keywordName);
    	} catch (NullPointerException e) {
    		return "";
    	}
    }
    
    private Object[] toStrings(Object[] args) {
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < newArgs.length; i++) {
            if (args[i].getClass().isArray()) {
                newArgs[i] = args[i];
            } else {
                newArgs[i] = args[i].toString();
            }
        }
        return newArgs;
    }
}
