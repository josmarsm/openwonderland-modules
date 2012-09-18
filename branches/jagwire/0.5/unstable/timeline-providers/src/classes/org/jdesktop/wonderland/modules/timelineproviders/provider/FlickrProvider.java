/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timelineproviders.provider;

import com.aetrion.flickr.REST;
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDateRange;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProvider;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProviderContext;
import org.jdesktop.wonderland.modules.timelineproviders.common.FlickrConstants;

/**
 * A provider that reads data from flickr
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class FlickrProvider implements TimelineProvider {
    private static final Logger logger =
            Logger.getLogger(FlickrProvider.class.getName());
    
    // single executor shared among all flickr providers
    private static ExecutorService exec = Executors.newCachedThreadPool();

    // the timeline provider context with configuration
    private TimelineProviderContext context;

    // the flickr API key
    private String apiKey;

    // other config
    private TimelineDateRange range;
    private int units;
    private int increments;
    private String text;
    private boolean fullText = false;
    private boolean relevance = false;
    private int returnCount = 4;
    private boolean creativeCommons = false;

    public void initialize(TimelineProviderContext context) {
        this.context = context;

        // read properties from the context
        Properties props = context.getQuery().getProperties();

        apiKey = System.getProperty("flickr.api.key");
        if (apiKey == null) {
            apiKey = props.getProperty(FlickrConstants.API_KEY_PROP);
        }
        if (apiKey == null) {
            throw new IllegalStateException("Flickr API key is required");
        }

        Date startDate = new Date(Long.parseLong(props.getProperty(FlickrConstants.START_DATE_PROP)));
        increments = Integer.parseInt(props.getProperty(FlickrConstants.INCREMENTS_PROP));
        units = Integer.parseInt(props.getProperty(FlickrConstants.UNITS_PROP));
        range = new TimelineDateRange(startDate, increments, units);

        System.out.println("Initialize flickr provider: " +
                startDate + " " + increments + " " + units + " key= " + apiKey);

        text = props.getProperty(FlickrConstants.SEARCH_TEXT_PROP);

        if (props.containsKey(FlickrConstants.SEARCH_TYPE_PROP)) {
            fullText = props.getProperty(FlickrConstants.SEARCH_TYPE_PROP).equals(FlickrConstants.SEARCH_FULLTEXT_KEY);
        }

        if (props.containsKey(FlickrConstants.SORT_PROP)) {
            relevance = props.getProperty(FlickrConstants.SORT_PROP).equals(FlickrConstants.SORT_RELEVANCE_KEY);
        }

        if (props.containsKey(FlickrConstants.RETURN_COUNT_PROP)) {
            returnCount = Integer.parseInt(props.getProperty(FlickrConstants.RETURN_COUNT_PROP));
        }

        if (props.containsKey(FlickrConstants.CC_PROP)) {
            creativeCommons = Boolean.parseBoolean(props.getProperty(FlickrConstants.CC_PROP));
        }

        // perform the initial queries
        int increment = 0;
        for (TimelineDate date : range.getIncrements()) {
            // get a new search parameters object for the text
            SearchParameters sp = getSearchParameters(text);
            if (sp != null) {

                sp.setMinTakenDate(date.getMinimum());
                sp.setMaxTakenDate(date.getMaximum());

                System.out.println("Submitting query for " + text + " range " +
                                   date);

                // create a task
                exec.submit(new FlickrQuery(sp, date, false));
            }

            // find keywords
            String keywords = props.getProperty(FlickrConstants.KEYWORD_PROP + increment);
            increment++;
            sp = getSearchParameters(keywords);
            if (sp != null) {
                System.out.println("Submitting query for keywords " + keywords + 
                                   " for increment " + increment);

                exec.submit(new FlickrQuery(sp, date, true));
            }
        }
    }

    public void shutdown() {
    }

    private void processResults(PhotoList results, TimelineDate range,
                                boolean forceDate)
    {
        System.out.println("Processing " + results.size() + " results");

        Set<DatedObject> add = new LinkedHashSet<DatedObject>();

        for (int i = 0; i < results.size(); i++) {
            Photo p = (Photo) results.get(i);
      
            TimelineDate taken;
            Date takenDate = p.getDateTaken();
            if (!forceDate && takenDate != null) {
                taken = new TimelineDate(takenDate);
            } else {
                taken = range;
            }

            //System.out.println("Small: " + p.getSmallUrl());
            //System.out.println("Medium: " + p.getMediumUrl());
            //System.out.println("Large: " + p.getLargeUrl());
            //System.out.println("Square: " + p.getSmallSquareUrl());
            //System.out.println("Orig: " + p.getOriginalUrl());
            //System.out.println("" + p.);


            String photoURI = "wl" + p.getSmallUrl();

            DatedImage image = new DatedImage(taken, photoURI);            
            image.setWidth(240);
            image.setHeight(240);
            
            if (p.getDescription() != null) {
                image.setDescription(p.getDescription());
            }

            System.out.println("Adding image " + photoURI + " date " +
                               taken.getMinimum() + " - " + taken.getMaximum() +
                               " size: " + p.getSmallSize().getWidth() +
                               " x " + p.getSmallSize().getHeight());

            add.add(image);
        }

        context.addResults(add);
    }

    /**
     * Return the search parameters without the dates
     */
    private SearchParameters getSearchParameters(String text) {
        SearchParameters sp = new SearchParameters();

        Set<String> extras = new LinkedHashSet<String>();
        extras.add(Extras.DATE_TAKEN);
        extras.add(Extras.URL_L);
        extras.add(Extras.URL_M);
        extras.add(Extras.URL_S);
        extras.add(Extras.URL_O);
        extras.add(Extras.URL_SQ);
        extras.add(Extras.URL_T);
        sp.setExtras(extras);

        if (text == null || text.trim().length() == 0) {
            return null;
        }

        if (fullText) {
            sp.setText(text);
        } else {
            sp.setTagMode("all");
            String tagArray[] = text.split("[ ,]");
            sp.setTags(tagArray);
        }

        if (relevance) {
            sp.setSort(SearchParameters.RELEVANCE);
        } else {
            sp.setSort(SearchParameters.INTERESTINGNESS_DESC);
        }

        // ignore for now
        //if (creativeCommons) {
        //    sp.setLicense();
        //}

        return sp;
    }

    class FlickrQuery implements Runnable {
        private SearchParameters params;
        private TimelineDate range;
        private boolean forceDate;

        public FlickrQuery(SearchParameters params, TimelineDate range, 
                           boolean forceDate)
        {
            this.params = params;
            this.range = range;
            this.forceDate = forceDate;
        }

        public void run() {
            try {
                PhotosInterface photosI = new PhotosInterface(apiKey, null, new REST());
                PhotoList results = photosI.search(params, returnCount, 0);

                System.out.println("found " + results.size() + " results");

                processResults(results, range, forceDate);
            } catch (Throwable t) {
                // report any errors
                logger.log(Level.WARNING, "Error performing query", t);
            }
        }
    }
}
