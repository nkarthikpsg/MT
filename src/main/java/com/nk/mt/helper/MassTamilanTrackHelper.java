package com.nk.mt.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.nk.mt.dto.AlbumnInfo;
import com.nk.mt.dto.TrackInfo;

public class MassTamilanTrackHelper
{
    Properties properties = null;

    public static MassTamilanTrackHelper instance()
    {
        MassTamilanTrackHelper massTamilanTrackHelper = new MassTamilanTrackHelper();

        massTamilanTrackHelper.loadProperties();

        return massTamilanTrackHelper;
    }

    private void loadProperties()
    {
        properties = new Properties();
        try
        {
            properties.load(this.getClass().getResourceAsStream("/search_preferences.properties"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        instance().getTracksAndWrite();
    }

    private void writeToFile(List<TrackInfo> tracks) throws IOException
    {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = csvMapper.schemaFor(TrackInfo.class).withColumnSeparator('\t').withHeader();
        String s = csvMapper.writer(schema).writeValueAsString(tracks);
        //        System.out.println(s);
        FileUtils.writeStringToFile(new File(properties.getProperty("songs_file_name")), s, "UTF-8");
    }

    public List<TrackInfo> getTracksAndWrite() throws IOException
    {
        String homeUrl = properties.getProperty("url");

        List<AlbumnInfo> albumnInfoList = getAlbums(homeUrl);

        List<TrackInfo> tracks = getTracks(albumnInfoList);

        writeToFile(tracks);

        return tracks;

    }

    public List<TrackInfo> getTracks(List<AlbumnInfo> albumnInfoList)
    {
        List<TrackInfo> trackInfoList = new ArrayList<>();
        albumnInfoList.stream().forEach(albumnInfo -> trackInfoList.addAll(getTracks(albumnInfo)));

        return trackInfoList;

    }

    public List<TrackInfo> getTracks(AlbumnInfo albumnInfo)
    {
        Document document = getDocument("https://www.masstamilan.org" + albumnInfo.getUrl());
        Element table = document.selectFirst("table[id=tlist]");
        //table.child = body, children()[0] = Name, Download, Play label row
        Elements children = table.child(0).children();
        children.remove(0);

        Element movieElement = document.selectFirst("fieldset[id=movie-handle]");
        Element musicElement = getFirstElement(movieElement.getElementsByAttributeValueContaining("href", "/music/"));
        Element yearElement = getFirstElement(
                movieElement.getElementsByAttributeValueContaining("href", "/browse-by-year/"));

        String music = getValue(musicElement);
        String year = getValue(yearElement);

        List<TrackInfo> trackInfoList = new ArrayList<>();

        for (Element child : children)
        {
            TrackInfo trackInfo = new TrackInfo();
            Elements downloadElements = child.select("a[class=dlink anim]");
            Iterator<Element> iterator = downloadElements.iterator();
            String downloadUrl = null;

            Element countElement = child.selectFirst("span[class=dl-count]");
            long count = NumberUtils.toLong(getValue(countElement), 0);
            trackInfo.setDownloads(count);

            long min_download = NumberUtils.toLong(properties.getProperty("min_download"), -1);

            if(count < min_download)
            {
                break;
            }

            while (iterator.hasNext())
            {
                Element downloadElement = iterator.next();
                String value = getValue(downloadElement);
                if (StringUtils.contains(value, "320kbps"))
                {
                    downloadUrl = downloadElement.attributes().get("href");
                    break;
                }
                else if (StringUtils.contains(value, "128kbps"))
                {
                    downloadUrl = downloadElement.attributes().get("href");
                }
            }
            trackInfo.setDownloadUrl(downloadUrl);

            Element nameElement = child.selectFirst("span[itemprop=name]").selectFirst("a");
            String name = getValue(nameElement);
            trackInfo.setAlbum(albumnInfo.getAlbum());
            trackInfo.setSong(name);
            trackInfo.setArtist(music);
            trackInfo.setYear(year);

            System.out.println(trackInfo);

            if (StringUtils.isNoneEmpty(trackInfo.getSong()))
            {
                trackInfoList.add(trackInfo);
            }
        }

        return trackInfoList;
    }

    private Element getFirstElement(Elements elements)
    {
        if (elements.size() > 0)
            return elements.get(0);

        return null;
    }

    public List<AlbumnInfo> getAlbums( String albumnsHomeUrl)
    {
        return getAlbums(1, albumnsHomeUrl);
    }

    public List<AlbumnInfo> getAlbums(int pageNumber, String albumnsHomeUrl)
    {
        List<AlbumnInfo> albumnInfoList = new ArrayList<>();

        long totalPages = NumberUtils.toLong(properties.getProperty("no_of_pages"), Integer.MAX_VALUE);

        if(pageNumber++ > totalPages)
        {
            return albumnInfoList;
        }

        Document document = getDocument(albumnsHomeUrl);
        Element albumnsElement = document.select("div[class=botlist]").get(0);

        Elements albumnElements = albumnsElement.select("div[class=botitem]");

        Iterator<Element> iterator = albumnElements.iterator();
        while (iterator.hasNext())
        {
            Element albumnElement = iterator.next();
            Element element = albumnElement.selectFirst("a");
            String url = element.attributes().get("href");
            String albumn = element.attributes().get("title");

            AlbumnInfo albumnInfo = new AlbumnInfo();
            albumnInfo.setAlbum(StringUtils.removeEnd(albumn, " Songs Download"));
            albumnInfo.setUrl(url);

            //            System.out.println(albumnInfo);
            albumnInfoList.add(albumnInfo);
        }

        Element nextPageElement = document.selectFirst("a[rel=next]");
        if (nextPageElement != null)
        {
            String url = nextPageElement.attributes().get("href");
            if (StringUtils.isNoneEmpty(url))
            {
                url = "https://www.masstamilan.org" + url;
                albumnInfoList.addAll(getAlbums(pageNumber, url));
            }
        }

        return albumnInfoList;
    }

    private Document getDocument(String saavnPlayList)
    {
        Document doc;
        try
        {
            doc = Jsoup.connect(saavnPlayList).get();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return doc;
    }

    private String getValue(Element element)
    {
        if (element != null && element.childNodeSize() > 0)
        {
            return element.childNode(0).toString();
        }

        return null;

    }
}
