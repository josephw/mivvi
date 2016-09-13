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
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kafsemo.mivvi.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

/**
 * @author joe
 */
public class ProgressDialog
{
    private final JProgressBar pb;
    private final JLabel jl1, jl2;
    private final JOptionPane p;
    private final JDialog jd;

    ProgressDialog(Component comp, String message)
    {
        pb = new JProgressBar();
        pb.setIndeterminate(true);

        jl1 = new JLabel(message);

        jl2 = new JLabel();
        jl2.setHorizontalAlignment(JLabel.CENTER);

        p = new JOptionPane(new JComponent[]{jl1, pb, jl2}, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new String[]{UIManager.getString("OptionPane.cancelButtonText")}, null);
        
        jd = p.createDialog(comp, "Progress...");
        jd.setResizable(true);
        jd.setModal(false);

//        pb.setStringPainted(true);
        jd.pack();
    }
    
    void close()
    {
        jd.setVisible(false);
        jd.dispose();
    }
    
    void setNote(String s)
    {
        jl2.setText(s);

    }
    
    void setMaximum(int m)
    {
        pb.setIndeterminate(false);
        pb.setMaximum(m);
    }
    
    void setProgress(int n)
    {
        pb.setValue(n);
    }

    private static boolean significantlyLarger(int a, int b)
    {
        return (a - b) > (a / 10);
    }

    void packLarger(Component comp)
    {
        Dimension pref = jd.getPreferredSize();
//        Dimension mx = jd.getMaximumSize();
        Dimension d = jd.getSize();

        if (significantlyLarger(pref.width, d.width) ||
                pref.height > d.height
)//                || (mx.width < d.width || mx.height > d.height))
        {
            jd.pack();
            jd.setLocationRelativeTo(comp);
        }
    }
    
    void show()
    {
        jd.setVisible(true);
    }

    boolean isCanceled()
    {
        return !JOptionPane.UNINITIALIZED_VALUE.equals(p.getValue());
    }
}
