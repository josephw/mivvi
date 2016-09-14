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

package org.kafsemo.mivvi.rss;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class FeedConfigurationDialog extends JDialog
{
    final FeedListTableModel fltm;
    final JTable table;

    public FeedConfigurationDialog(JFrame owner)
    {
        super(owner, "Configure Feeds");

        fltm = new FeedListTableModel();
        
        setModal(true);

        table = new JTable(fltm);
        
        final JPasswordField jpf = new JPasswordField();
        final char f = jpf.getEchoChar();
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JPasswordField()));
        table.getColumnModel().getColumn(2).setCellRenderer(new TableCellRenderer(){
            JLabel jl = new JLabel();

            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column)
            {
                String s = (String)value;
                if (s == null)
                    s = "";
                StringBuffer sb = new StringBuffer(s.length());
                for (int i = 0 ; i < s.length() ; i++) {
                    sb.append(f);
                }
                jl.setText(sb.toString());
                jl.setFont(jpf.getFont());
                jl.setOpaque(true);

                if (isSelected) {
                    jl.setForeground(table.getSelectionForeground());
                    jl.setBackground(table.getSelectionBackground());
                } else {
                    jl.setForeground(jpf.getForeground());
                    jl.setBackground(jpf.getBackground());
                }
                return jl;
            }
        });
        
        getContentPane().add(BorderLayout.CENTER, new JScrollPane(table));
        
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(Box.createHorizontalStrut(10));
        JButton jb = new JButton("OK");
        jb.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                ok();
            }
        });
        b.add(jb);
        b.add(Box.createHorizontalStrut(10));
        jb = new JButton("Cancel");
        jb.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });
        b.add(jb);
        b.add(Box.createHorizontalStrut(10));
        b.add(Box.createHorizontalGlue());

        getContentPane().add(BorderLayout.PAGE_END, b);

        pack();
        
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e)
            {
                cancel();
            }
        });
    }

    boolean wasOk;

    void ok()
    {
        table.getDefaultEditor(String.class).stopCellEditing();

/*        try {
            fltm.save();
        } catch (Exception x) {
            System.err.println(x);
        }
*/        wasOk = true;
        setVisible(false);
    }

    void cancel()
    {
        wasOk = false;
        setVisible(false);
    }
}
