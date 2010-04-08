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

package org.kafsemo.mivvi.app;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * @author joe
 */
public class RdfMiscVocabulary
{
    public static final URI smIcon = new URIImpl("http://purl.org/net/rdf/papers/sitemap#icon");
    public static final URI foafName = new URIImpl("http://xmlns.com/foaf/0.1/name");
    public static final URI kafsemoMivviCategory = new URIImpl("tag:kafsemo.org,2004:mivvi#category");
}
