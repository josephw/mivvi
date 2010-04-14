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

package org.kafsemo.mivvi.desktop;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

public class MivviDataDownloadFrame extends JFrame implements ActionListener
{
    ProgressStatus psMain, psIndividual;

    JProgressBar jpMain, jpIndividual;

    JLabel jlIndividual;

    final Timer timer;
    
    public MivviDataDownloadFrame(ProgressStatus main, ProgressStatus individual)
    {
        super("Updating Mivvi Data");

        /* Objects to track progress */
        this.psMain = main;
        this.psIndividual = individual;
        
        psIndividual.label = "Checking...";
        
        psIndividual.indeterminate = true;

        jpMain = new JProgressBar();
        jpIndividual = new JProgressBar();

        jlIndividual = new JLabel(psIndividual.label);

        timer = new Timer(250, this);

        /* GUI */
        Box b = Box.createVerticalBox();
        b.add(Box.createVerticalGlue());
        
        JLabel tl = new JLabel("Downloading Mivvi Data");
        
        Box b3 = Box.createHorizontalBox();
        b3.add(Box.createHorizontalGlue());
        b3.add(tl);
        b3.add(Box.createHorizontalGlue());
        getContentPane().add(BorderLayout.PAGE_START, b3);
        
        synchronized (psMain) {
            if (psMain.maximum > 1) {
                b.add(jpMain);
                b.add(Box.createVerticalStrut(10));
                b.add(jlIndividual);
            }
        }
        b.add(jpIndividual);
        b.add(Box.createVerticalStrut(10));
        b.add(Box.createVerticalGlue());

        
        JButton jb = new JButton("Cancel");
        jb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                synchronized (psIndividual) {
                    psIndividual.cancelled = true;
                }
            }
        });
        
        b.add(Box.createVerticalStrut(10));
        
        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalGlue());
        b2.add(Box.createHorizontalStrut(10));
        b2.add(jb);
        b2.add(Box.createHorizontalStrut(10));
        b2.add(Box.createHorizontalGlue());

        b.add(b2);
        b.add(Box.createVerticalGlue());

        b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));// LineBorder(Color.GREEN));

        getContentPane().add(BorderLayout.CENTER, b);

        pack();

        if (getWidth() < 250)
            setSize(400, getHeight());

        setLocationRelativeTo(null);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        synchronized (psMain) {
            jpMain.setIndeterminate(psMain.indeterminate);
            jpMain.setMaximum(psMain.maximum);
            jpMain.setValue(psMain.value);
        }

        synchronized (psIndividual) {
            jpIndividual.setIndeterminate(psIndividual.indeterminate);
            jpIndividual.setMaximum(psIndividual.maximum);
            jpIndividual.setValue(psIndividual.value);
            jlIndividual.setText(psIndividual.label);
        }
    }
    
    public void stop()
    {
        timer.stop();
    }

    public ProgressStatus getIndividualProgressStatus()
    {
        return psIndividual;
    }

    public ProgressStatus getMainProgressStatus()
    {
        return psMain;
    }
    
    public static MivviDataDownloadFrame showDownloadProgress(ProgressStatus psMain, ProgressStatus psIndividual)
    {
        MivviDataDownloadFrame mddf = new MivviDataDownloadFrame(psMain, psIndividual);
        
        mddf.setVisible(true);

        mddf.timer.start();
        
        return mddf;
    }
}
