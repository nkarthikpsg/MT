package com.nk.mt.helper;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.nk.mt.dto.TrackInfo;

public class MassTamilanDownloadHelper
{
    public static MassTamilanDownloadHelper instance()
    {
        return new MassTamilanDownloadHelper();
    }

    public static void main(String[] args) throws Exception
    {
        instance().download();
    }

    private void download() throws Exception
    {
        Properties properties = new Properties();

        properties.load(this.getClass().getResourceAsStream("download_preferences.properties"));

        String csv = FileUtils.readFileToString(new File(properties.getProperty("in")), Charset.defaultCharset());

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = csvMapper.schemaFor(TrackInfo.class).withColumnSeparator('\t').withHeader();
        List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(csv).readAll();

        ObjectMapper mapper = new ObjectMapper();

        List<TrackInfo> trackInfoList = readAll.stream().map(o -> mapper.convertValue(o, TrackInfo.class))
                .collect(Collectors.toList());

        long d_lower = NumberUtils.toLong(properties.getProperty("d_lower"), -1);
        long d_upper = NumberUtils.toLong(properties.getProperty("d_upper"), Long.MAX_VALUE);
        trackInfoList = trackInfoList.stream()
                .filter(track -> track.getDownloads() > d_lower && track.getDownloads() <= d_upper)
                .collect(Collectors.toList());

        if (BooleanUtils.toBoolean(properties.getProperty("count_only")))
        {
            System.out.println("Total Selected Count: " + trackInfoList.size());
            System.out.println(trackInfoList);
            return;
        }

        long limit = getLimit(properties);
        for (TrackInfo trackInfo : trackInfoList)
        {
            if (limit-- == 0)
            {
                System.out.println(
                        MessageFormat.format("Reached configured limit: {0} of total: {1}", trackInfoList.size()));
                break;
            }

            URL obj = new URL("https://www.masstamilan.org" + trackInfo.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla");

            boolean redirect = false;

            // normally, 3xx is redirect
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
            {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            if (redirect)
            {

                String newUrl = conn.getHeaderField("Location");
                conn.disconnect();

                obj = new URL(newUrl);
                conn = (HttpURLConnection) obj.openConnection();

                try
                {
                    InputStream is = conn.getInputStream();
                    FileUtils.copyInputStreamToFile(is,
                            new File(properties.getProperty("out") + "/" +trackInfo.getSong() + ".mp3"));

                    System.out.println("SUCESS: " + newUrl);
                }
                catch (Exception e)
                {
                    System.out.println("Failed:" + newUrl);
                }
            }
        }
    }

    private static long getLimit(Properties properties)
    {
        return NumberUtils.toLong(properties.getProperty("limit"));
    }

}
