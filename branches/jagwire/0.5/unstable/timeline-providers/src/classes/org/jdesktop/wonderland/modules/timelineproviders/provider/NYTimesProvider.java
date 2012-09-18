/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timelineproviders.provider;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedImage;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedNews;
import org.jdesktop.wonderland.modules.timeline.common.provider.DatedObject;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineDate;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProvider;
import org.jdesktop.wonderland.modules.timeline.provider.spi.TimelineProviderContext;
import org.jdesktop.wonderland.modules.timelineproviders.common.NYTimesConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A provider that reads data from the new york times
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public class NYTimesProvider implements TimelineProvider {
    private static final Logger logger =
            Logger.getLogger(NYTimesProvider.class.getName());

    // base NYTimes url
    private static final String BASE_URL =
            "http://api.nytimes.com/svc/search/v1/article?query=";

    // single executor shared among all flickr providers
    private static ExecutorService exec = Executors.newSingleThreadExecutor();

    // the timeline provider context with configuration
    private TimelineProviderContext context;

    // the flickr API key
    private String apiKey;

    // other config
    private TimelineDate range;
    private int increments;
    private String text;
    private int returnCount = 4;

    public void initialize(TimelineProviderContext context) {
        this.context = context;

        // read properties from the context
        Properties props = context.getQuery().getProperties();

        apiKey = System.getProperty("nytimes.api.key");
        if (apiKey == null) {
            apiKey = props.getProperty(NYTimesConstants.API_KEY_PROP);
        }
        if (apiKey == null) {
            throw new IllegalStateException("New York Times API key is required");
        }

        Date startDate = new Date(Long.parseLong(props.getProperty(NYTimesConstants.START_DATE_PROP)));
        Date endDate = new Date(Long.parseLong(props.getProperty(NYTimesConstants.END_DATE_PROP)));
        range = new TimelineDate(startDate, endDate);
        increments = Integer.parseInt(props.getProperty(NYTimesConstants.INCREMENTS_PROP));

        text = props.getProperty(NYTimesConstants.SEARCH_TEXT_PROP);

        
        if (props.containsKey(NYTimesConstants.RETURN_COUNT_PROP)) {
            returnCount = Integer.parseInt(props.getProperty(NYTimesConstants.RETURN_COUNT_PROP));
        }

        // perform the initial queries
        long incrementSize = range.getRange() / increments;
        for (long i = range.getMinimum().getTime();
                i < range.getMaximum().getTime();
                i += incrementSize)
        {
            // create the URL
            String url;
            try {
                url = BASE_URL + URLEncoder.encode(text, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new IllegalStateException(uee);
            }

            // fill in the date
            Date start = new Date(i);
            Date end = new Date(i + incrementSize);

            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            url += "&begin_date=" + df.format(start);
            url += "&end_date=" + df.format(end);
            url += "&rank=closest";
            url += "&api-key=" + apiKey;

            System.out.println("Submitting query for search from " + start +
                               " to " + end + " url " + url);

            // create a task
            exec.submit(new NYTimesQuery(url, new TimelineDate(start, end)));
        }
    }

    public void shutdown() {
    }

    private void processResults(JSONObject obj, TimelineDate range)
        throws JSONException
    {
        // wait for a bit so we don't overuse our API key
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {}
        
        Set<DatedObject> add = new LinkedHashSet<DatedObject>();

        JSONArray results = obj.getJSONArray("results");
        System.out.println("Processing " + results.length());

        // enforce the request for number of results
        int count = Math.min(results.length(), returnCount);
        for (int i = 0; i < count; i++) {
            JSONObject result = results.getJSONObject(i);

            // make sure we have the results we want
            if (!result.has("date") || !result.has("title") ||
                !result.has("url") || !result.has("body"))
            {
                continue;
            }

            TimelineDate date;
            try {
                DateFormat df = new SimpleDateFormat("yyyyMMdd");
                Date d = df.parse(result.getString("date"));
                date = new TimelineDate(d);
            } catch (ParseException pe) {
                logger.log(Level.WARNING, "Error parsing date", pe);
                date = range;
            }

            System.out.println(result.getString("date"));
            System.out.println(result.getString("title"));
            System.out.println(result.getString("url"));
            System.out.println(result.getString("body"));
            System.out.println();

            DatedNews dn = new DatedNews(date, result.getString("title"),
                                         result.getString("url"),
                                         result.getString("body"));

            if (result.has("small_image")) {
                dn.setImageURI(result.getString("small_image_url"));
                dn.setWidth(Integer.parseInt(result.getString("small_image_width")));
                dn.setHeight(Integer.parseInt(result.getString("small_image_height")));
            }
            add.add(dn);
        }

        context.addResults(add);
    }

    class NYTimesQuery implements Runnable {
        private URL url;
        private TimelineDate range;

        public NYTimesQuery(String urlStr, TimelineDate range) {
            this.range = range;

            try {
                url = new URL(urlStr);
            } catch (MalformedURLException mue) {
                throw new IllegalStateException(mue);
            }
        }

        public void run() {
            try {
                Reader r = new InputStreamReader(url.openStream());
                JSONObject obj = new JSONObject(new JSONTokener(r));

                processResults(obj, range);
            } catch (Throwable t) {
                // report any errors
                logger.log(Level.WARNING, "Error performing query", t);
            }
        }
    }
}
