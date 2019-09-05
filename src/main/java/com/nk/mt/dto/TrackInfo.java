package com.nk.mt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "artist", "album", "song", "downloads", "year", "downloadUrl" })
public class TrackInfo
{
    @JsonProperty(value = "albumn")
    String album;

    @JsonProperty(value = "song")
    String song;

    @JsonProperty(value = "artist")
    String artist;

    @JsonProperty(value = "downloads")
    long downloads;

    @JsonProperty(value = "downloadUrl")
    String downloadUrl;

    @JsonProperty(value = "year")
    String year;

    public String getAlbum()
    {
        return album;
    }

    public void setAlbum(String album)
    {
        album = replaceSpace(album);
        this.album = album;
    }

    public String getSong()
    {
        return song;
    }

    public void setSong(String song)
    {
        song = replaceSpace(song);
        this.song = song;
    }

    public String getArtist()
    {
        return artist;
    }

    public void setArtist(String artist)
    {
        artist = replaceSpace(artist);
        this.artist = artist;
    }

    public long getDownloads()
    {
        return downloads;
    }

    public void setDownloads(long downloads)
    {
        this.downloads = downloads;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    private String replaceSpace(String str)
    {
        if(str != null)
            return str.replaceAll(" ", "-");
        return null;

    }

    @Override
    public String toString()
    {
        return "com.nk.mt.dto.TrackInfo{" + "album='" + album + '\'' + ", song='" + song + '\'' + ", artist='" + artist + '\''
                + ", downloads=" + downloads + ", downloadUrl='" + downloadUrl + '\'' + ", year='" + year + '\'' + '}';
    }
}
