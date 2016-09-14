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

package org.kafsemo.mivvi.app;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * @author joe
 */
public class RdfMiscVocabulary
{
    private static final ValueFactory VF = SimpleValueFactory.getInstance();

    public static final IRI smIcon = VF.createIRI("http://purl.org/net/rdf/papers/sitemap#icon");
    public static final IRI foafName = VF.createIRI("http://xmlns.com/foaf/0.1/name");
    public static final IRI kafsemoMivviCategory = VF.createIRI("tag:kafsemo.org,2004:mivvi#category");
}
