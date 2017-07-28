package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import java.util.Iterator;
import it.unibo.alchemist.boundary.gpsload.api.GPSTimeNormalizer;
import it.unibo.alchemist.boundary.gpsload.api.NormalizeTimeWithFirstOfAll;
import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader;
import it.unibo.alchemist.model.interfaces.GPSTrace;

/**
 * 
 */
public class TestGPSLoader {

    private static final String BASE_DIR = "/trace/";
    private static final String BASE_OK_TEST = BASE_DIR + "ok/";
    private static final String BASE_NOT_OK_TEST = BASE_DIR + "no_ok/";
    
    private static final String DIRECTORY_WITH_FILES = BASE_OK_TEST + "sub1/";
    private static final String DIRECTORY_WITH_SUBDIRECTORIES = BASE_OK_TEST;
    
    private static final String WRONG_EXTENSION = BASE_NOT_OK_TEST + "/wrong_extension/";
    private static final String UNRECOGNIZED_EXTENSION = BASE_NOT_OK_TEST + "unrecognized_extension/";
    
    private static final String TIME_NORMALIZER_CLASS = "NormalizeTimeWithFirstOfAll";
    private static final GPSTimeNormalizer NORMALIZER = new NormalizeTimeWithFirstOfAll();
    
    private TraceLoader loaderGpx;
    
    private static final int TOTAL_POINTS = 12196; 


    /**
     * 
     */
    @Test
    public void testOk() {
        
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_FILES, TIME_NORMALIZER_CLASS);
            final Iterator<GPSTrace> trace = this.loaderGpx.iterator();
            for (int i = 0; i < 3; i++) {
                if(trace.hasNext()) {
                    trace.next();
                } else {
                    fail("not loading all trace");
                }
            }
            assertFalse("Load more traces of the due", trace.hasNext());
        } catch (Exception e) {
            fail(e.getMessage());
        } 
        
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_SUBDIRECTORIES, true,  NORMALIZER);
            final Iterator<GPSTrace> trace = this.loaderGpx.iterator();
            int points = 0;
            for (int i = 0; i < 6; i++) {
                if(trace.hasNext()) {
                    points += trace.next().size();                 
                } else {
                    fail("not loading all trace");
                }
            }
            assertTrue("the iterator is not cycle", trace.hasNext());
            assertEquals(points, TOTAL_POINTS);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
    }

    @Test
    public void testError() {
        
        try {
            this.loaderGpx = new TraceLoader(WRONG_EXTENSION, TIME_NORMALIZER_CLASS);
            fail("not exception for wrong extension");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        } 
        
        try {
            this.loaderGpx = new TraceLoader(UNRECOGNIZED_EXTENSION, NORMALIZER);
            fail("not exception for unrecognized extension");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
    }
}
