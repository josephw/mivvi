/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright © 2004-2016 Joseph Walton
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.tv;

import java.util.Date;

public class Programme
{
    String title;
    String subtitle;
    Date start;
    Date end;
    String channel;
    Integer seasonNumber;
    Integer seasonLength;
    Integer episodeNumber;

    public String getTitle()
    {
        return title;
    }

    public Date getStart()
    {
        return start;
    }

    public String getSubTitle()
    {
        return subtitle;
    }

    public String getChannel()
    {
        return channel;
    }

    public Date getEnd()
    {
        return end;
    }

    public Integer getEpisodeNumber()
    {
        return episodeNumber;
    }

    public Integer getSeasonLength()
    {
        return seasonLength;
    }

    public Integer getSeasonNumber()
    {
        return seasonNumber;
    }
}
