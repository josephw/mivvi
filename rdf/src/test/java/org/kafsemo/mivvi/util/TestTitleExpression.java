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

package org.kafsemo.mivvi.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.kafsemo.mivvi.util.TitleExpression;

public class TestTitleExpression extends TestCase
{
    public void testConstant()
    {
        TitleExpression te;
        
        te = new TitleExpression("");
        assertEquals("", te.evaluate(null));

        te = new TitleExpression("A constant string");
        assertEquals("A constant string", te.evaluate(null));
    }
    
    public void testVariables()
    {
        TitleExpression te;
        
        te = new TitleExpression("${test}");
        assertEquals("TEST", te.evaluate(new CaseSwitchVariableResolver()));

        te = new TitleExpression("${This} is ${a} test");
        assertEquals("tHIS is A test", te.evaluate(new CaseSwitchVariableResolver()));

        te = new TitleExpression("This ${is} a ${test}");
        assertEquals("This IS a TEST", te.evaluate(new CaseSwitchVariableResolver()));
    }
    
    public void testUnknownVariables()
    {
        TitleExpression te;
        
        te = new TitleExpression("${test}");
        assertEquals("${test}", te.evaluate(new EmptyVariables()));
    }
    
    public void testFullExample()
    {
        TitleExpression te;
        
        te = new TitleExpression("${series}/${season.title}/${season.number}${episode.number}- ${episode.title}.${file.extension}");
        
        final String[][] VARS = {
                {"series", "Example Show"},
                {"season.title", "Season 1"},
                {"season.number", "1"},
                {"episode.number", "01"},
                {"episode.title", "Named Episode"},
                {"file.extension", "mpeg"}
        };
        
        TitleExpression.Variables var = new TitleExpression.Variables() {
            Map<String, String> m;
            {
                m = new HashMap<String, String>();
                for (int i = 0; i < VARS.length; i++) {
                    m.put(VARS[i][0], VARS[i][1]);
                }
            }
            public String get(String n)
            {
                return m.get(n);
            }
        };
        // XXX Set up
        
        String expected = "Example Show/Season 1/101- Named Episode.mpeg";
        assertEquals(expected, te.evaluate(var));
    }
    
    static final class CaseSwitchVariableResolver
        implements TitleExpression.Variables
    {
        public String get(String n)
        {
            char[] ca = n.toCharArray();
            StringBuffer sb = new StringBuffer(ca.length);
            for (int i = 0; i < ca.length; i++) {
                if (Character.isLowerCase(ca[i])) {
                    sb.append(Character.toUpperCase(ca[i]));
                } else {
                    sb.append(Character.toLowerCase(ca[i]));
                }
            }
            
            return sb.toString();
        }
    }
    
    static final class EmptyVariables
        implements TitleExpression.Variables
    {
        public String get(String n)
        {
            return null;
        }
    }
}
