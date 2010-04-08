/*
 * Mivvi - Metadata, organisation and identification for television programs
 * Copyright (C) 2004, 2005, 2006, 2010  Joseph Walton
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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.cal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/*
BEGIN:VCALENDAR
VERSION
 :2.0
BEGIN:VEVENT
UID
 :random-guid
SUMMARY
 :Manually created entry
DTSTART
 :20050603T000000Z
DTEND
 :20050603T010000Z
END:VEVENT
END:VCALENDAR
 */
public class ICalFile
{
    private final Writer w;
    private final DateFormat df;

    public ICalFile(File f) throws IOException
    {
        w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
        writeHeader();
        df = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    private void writeHeader() throws IOException
    {
        w.write("BEGIN:VCALENDAR\nVERSION\n :2.0\n");
    }

    private long uid = 0;

    public void writeEvent(Date start, Date end, String description, String location, String desc, String url) throws IOException
    {
        if (start == null)
            return;

        w.write("BEGIN:VEVENT\n");
        writeField("UID", Long.toString(uid++));
        writeField("SUMMARY", description);
        writeField("DTSTART", df.format(start));
        if (end != null)
            writeField("DTEND", df.format(end));
        writeField("LOCATION", location);
        writeField("DESCRIPTION", desc);
        writeField("URL", url);
        w.write("END:VEVENT\n");
    }
    
    private void writeField(String name, String value) throws IOException
    {
        w.write(name + ":\n");
        w.write(" " + value + "\n");
    }

    public void close() throws IOException
    {
        w.write("END:VCALENDAR\n");
        w.close();
    }
}
