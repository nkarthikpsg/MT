package com.nk.mt.dto;

public class AlbumnInfo
{
    String album;

    String music;

    String url;

    public String getAlbum()
    {
        return album;
    }

    public void setAlbum(String album)
    {
        this.album = album;
    }

    public String getMusic()
    {
        return music;
    }

    public void setMusic(String music)
    {
        this.music = music;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String toString()
    {
        return "com.nk.mt.dto.AlbumnInfo{" + "album='" + album + '\'' + ", music='" + music + '\'' + ", url='" + url + '\'' + '}';
    }
}
