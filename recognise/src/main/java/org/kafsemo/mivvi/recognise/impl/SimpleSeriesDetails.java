package org.kafsemo.mivvi.recognise.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kafsemo.mivvi.recognise.SeriesDetails;

public class SimpleSeriesDetails extends SeriesDetails<URI>
{
    public final URI id;
    private String title;
    private final List<String> descriptions = new ArrayList<String>();

    public SimpleSeriesDetails(URI id)
    {
        this.id = id;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return this.title;
    }

    public List<String> descriptions()
    {
        return Collections.unmodifiableList(descriptions);
    }

    public void addDescription(String desc)
    {
        descriptions.add(desc);
    }
}
