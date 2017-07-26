package it.unibo.alchemist.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openstreetmap.osmosis.osmbinary.file.FileFormatException;
import it.unibo.alchemist.boundary.gpsload.LoadGPSMappingJson;
import it.unibo.alchemist.boundary.gpsload.LoadGPSMappingStrategy;
import it.unibo.alchemist.boundary.gpsload.LoadGPSTraceGPX;
import it.unibo.alchemist.boundary.gpsload.LoadGPSTraceMappingStrategy;
import it.unibo.alchemist.boundary.gpsload.LoadGPSTraceStrategy;
import it.unibo.alchemist.boundary.gpsload.LoadGPSTraceMapping;
import it.unibo.alchemist.boundary.gpsload.NormalizeTimeStrategy;
import it.unibo.alchemist.boundary.gpsload.NormalizeTimeWithFirstOfAll;
import it.unibo.alchemist.boundary.gpsload.TraceLoader;

/**
 * 
 */
public class TestGPSLoader {

    private static final String BASE_DIR = "/trace/test_directory/";
    private static final String BASE_ONE_TRACE = "/trace/test_file/one_trace/";
    private static final String BASE_MULTI_TRACE = "/trace/test_file/multi_trace/";
    private static final String DIRECTORY_OK = BASE_DIR + "dir_ok";
    private static final String DIRECTORY_WITHOUT_CONFIG_FILE = BASE_DIR + "dir_without_config_file";
    private static final String INVALID_CONFIG_FILE = BASE_DIR + "dir_with_config_file_not_valid";
    private static final String NO_DIRECTORY = BASE_DIR + "no_directory";
    private static final String DIRECTORY_WITH_NODE_WITHOUT_TRACE_MAPPED = BASE_ONE_TRACE + "without_trace_mapped";
    private static final String DIRECTORY_WITH_NODE_WITHOUT_FILE_TRACE = BASE_ONE_TRACE + "without_file_trace";
    private static final String DIRECTORY_WITH_NODE_WITH_FILE_TRACE_NOT_VALID = BASE_ONE_TRACE + "file_trace_not_valid";
    private static final String DIRECTORY_WITH_NODE_WITH_TRACE_NULL = BASE_ONE_TRACE + "file_with_trace_null";
    private static final String DIRECTORY_WITH_NODE_WITH_FILE_TRACE_WITH_SEGMENT_NULL = BASE_ONE_TRACE + "file_with_segment_null";
    private static final String DIRECTORY_WITH_NODE_WITH_FILE_TRACE_WITH_POINT_WITHOUT_TIME = BASE_ONE_TRACE + "file_with_pointTime_null";
    private static final String DIRECTORY_WITH_FILE_WITH_SELECTED_TRACE = BASE_MULTI_TRACE + "with_selected_trace";
    private static final String DIRECTORY_WITH_FILE_WITHOUT_SELECTED_TRACE = BASE_MULTI_TRACE + "without_selected_trace";
    private final LoadGPSTraceStrategy strategyTrace = new LoadGPSTraceGPX();
    private final LoadGPSMappingStrategy strategyMapping = new LoadGPSMappingJson();
    private final LoadGPSTraceMappingStrategy strategyLoad = new LoadGPSTraceMapping(strategyMapping, strategyTrace);
    private final NormalizeTimeStrategy timeStrategy = new NormalizeTimeWithFirstOfAll();
    private TraceLoader loaderGpx;
    /*
     * number of GPSPoint relative GPSTrece of node with id = 3 (file 2454858.gpx)
     */
    private static final int NUMBER_GPSPOINT = 962;
    private static final int IDNODE = 3;

    /**
     * 
     */
    @Test
    public void testDirectory() {
        /* 
         * directory path isn't a directory 
         */
        try {
            this.loaderGpx = new TraceLoader(NO_DIRECTORY, this.strategyLoad, this.timeStrategy);
            fail("not exception for no directory");
        } catch (IllegalArgumentException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (IOException e) {
            fail("Unexpected exception type");
        } 
        /*
         * directory not contains file with mapping 
         */
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_WITHOUT_CONFIG_FILE, this.strategyLoad, this.timeStrategy);
            fail("not exception for directory without config file mapping");
        } catch (FileNotFoundException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        }
        /* 
         * directory contains file with mapping, but isn't correct 
         */
        try {
            this.loaderGpx = new TraceLoader(INVALID_CONFIG_FILE, this.strategyLoad, this.timeStrategy);
            fail("not exception for directory with config file mapping not valid");
        } catch (FileNotFoundException e) {
            fail("Unexpected exception type");
        } catch (IllegalArgumentException | IOException | NullPointerException e) {
            assertFalse(e.getMessage().isEmpty());
        }

        /*
         * directory contains file with correct mapping, but at least a strategy is null
         */
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_OK, new LoadGPSTraceMapping(this.strategyMapping, null), this.timeStrategy);
            fail("not exception for null strategy");
        } catch (NullPointerException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        }

        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_OK, new LoadGPSTraceMapping(null, this.strategyTrace), this.timeStrategy);
            fail("not exception for null strategy");
        } catch (NullPointerException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        }

        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_OK, this.strategyLoad, null);
            fail("not exception for null strategy");
        } catch (NullPointerException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        }

        /* 
         * directory contains file with correct mapping
         */
        try {
            this.loaderGpx = new TraceLoader(DIRECTORY_OK, this.strategyLoad, this.timeStrategy);
            assertEquals(NUMBER_GPSPOINT, this.loaderGpx.getGPSTrace(IDNODE).size());
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        } 
    }

    /**
     * 
     */
    @Test
    public void testGPXFile() {
        /* 
         * directory contains file with correct mapping, but... 
         */
        try {
            /*
             * a node hasn't a trace mapped
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_NODE_WITHOUT_TRACE_MAPPED, this.strategyLoad, this.timeStrategy);
            fail("not exception for directory with node without trace mapped");
        } catch (IllegalArgumentException | FileNotFoundException | FileFormatException e) {
            fail("Unexpected exception type");
        } catch (IOException e) {
            assertFalse(e.getMessage().isEmpty());
        }

        try {
            /* 
             * file with trace not found
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_NODE_WITHOUT_FILE_TRACE, this.strategyLoad, this.timeStrategy);
            fail("not exception for directory with file mapped not found");
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        } catch (NullPointerException e) {
            assertFalse(e.getMessage().isEmpty());
        }

        try {
            /* 
             * file trace not valid
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_NODE_WITH_FILE_TRACE_NOT_VALID, this.strategyLoad, this.timeStrategy);
            fail("not exception for file trace not valid");
        } catch (IOException e) {
            assertFalse(e.getMessage().isEmpty());
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception type");
        }

        try {
            /*
             * file with track null 
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_NODE_WITH_TRACE_NULL, this.strategyLoad, this.timeStrategy);
            fail("not exception for file with trace null");
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        }

        try {
            /*
             * track with segment null 
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_NODE_WITH_FILE_TRACE_WITH_SEGMENT_NULL, this.strategyLoad, this.timeStrategy);
            fail("not exception for file with segment null");
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        }

        try {
            /* 
             * track with at least a point without time
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_NODE_WITH_FILE_TRACE_WITH_POINT_WITHOUT_TIME, this.strategyLoad, this.timeStrategy);
            fail("not exception for file with point null");
        } catch (IOException e) {
            fail("Unexpected exception type");
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().isEmpty());
        }
    }

    /**
     * 
     */
    @Test
    public void testGPXFileMultipleTrace() {
        /* 
         * directory contains file with correct mapping, but... 
         */
        try {
            /*
             * track required not exists
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_FILE_WITHOUT_SELECTED_TRACE, this.strategyLoad, this.timeStrategy);
            fail("not exception for inexistent trace request");
        } catch (FileFormatException | FileNotFoundException e) { 
            fail("Unexpected exception type");
        } catch (IOException e) {
            fail("Unexpected exception type");
        } catch (NullPointerException e) {
            assertFalse(e.getMessage().isEmpty());
        }

        try {
            /*
             * track required exists 
             */
            this.loaderGpx = new TraceLoader(DIRECTORY_WITH_FILE_WITH_SELECTED_TRACE, this.strategyLoad, this.timeStrategy);
            assertEquals(NUMBER_GPSPOINT, this.loaderGpx.getGPSTrace(IDNODE).size());
        } catch (IllegalArgumentException | IOException e) {
            fail("Unexpected exception type");
        }
    }
}
