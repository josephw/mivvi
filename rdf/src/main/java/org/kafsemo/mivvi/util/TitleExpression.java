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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleExpression
{
    private final String template;
    
    public TitleExpression(String template)
    {
        this.template = template;
    }

    public String evaluate(Variables vars)
    {
        StringBuffer sb = new StringBuffer(template.length() * 2);
        
        Pattern p = Pattern.compile("\\$\\{([\\.\\w]*)\\}");
        
        Matcher m = p.matcher(template);
        int o = 0;
        while (m.find()) {
            sb.append(template.substring(o, m.start()));
            
            String name = m.group(1);
            String v = vars.get(name);
            if (v != null) {
                sb.append(v);
            } else {
                sb.append("${" + name + "}");
            }
            
            o = m.end();
        }
        
        sb.append(template.substring(o));
        
        return sb.toString();
    }
    
    public interface Variables
    {
        String get(String n);
    }
}
