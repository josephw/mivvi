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

package org.kafsemo.mivvi.tv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.kafsemo.mivvi.desktop.AppState;

public class ChannelConfigurationFrame extends JDialog
{
    final AppState appState;
    final ChannelSelectionModel csm;
    final ChannelAvailability ca;

    ChannelConfigurationFrame(JFrame parent, AppState state, List<String> channels, ChannelAvailability ca)
    {
        super(parent, "Channels", true);

        this.appState = state;

        csm = new ChannelSelectionModel(channels);
        this.ca = ca;

        JScrollPane jsp;

        final JList jl = new JList(csm);

        jl.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e)
            {
                int i = jl.locationToIndex(e.getPoint());
                if (i < 0)
                    return;

                csm.toggle(i);
            }
        });

        jl.setCellRenderer(new ListCellRenderer(){
            JCheckBox jcb = new JCheckBox();

            Border noFocusBorder =
                BorderFactory.createEmptyBorder(1, 1, 1, 1);

            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                jcb.setText((String)value);

                jcb.setSelected(csm.isSelected((String)value));

                 jcb.setBackground(isSelected ?
                         jl.getSelectionBackground() : jl.getBackground());
                 jcb.setForeground(isSelected ?
                         jl.getSelectionForeground() : jl.getForeground());
                 jcb.setEnabled(jl.isEnabled());
                 jcb.setFont(jl.getFont());
                 jcb.setFocusPainted(false);
                 jcb.setBorderPainted(true);
                 jcb.setBorder(isSelected ?
                  UIManager.getBorder(
                   "List.focusCellHighlightBorder") : noFocusBorder);
                 
                return jcb;
            }
        });
        jsp = new JScrollPane(jl);

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(BorderLayout.CENTER, jsp);

        Box b2 = Box.createHorizontalBox();
        b2.add(Box.createHorizontalGlue());
        
        JButton cfgButton = new JButton("Available...");
        cfgButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                showConfigurationDialog(ChannelConfigurationFrame.this);
            }
        });
        b2.add(cfgButton);
        b2.add(Box.createHorizontalStrut(10));
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                wasOKd = true;
                setVisible(false);
//                dispose();
            }
        });
        b2.add(okButton);
        b2.add(Box.createHorizontalStrut(10));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });
        b2.add(cancelButton);
        b2.add(Box.createHorizontalGlue());

        getContentPane().add(BorderLayout.PAGE_END, b2);

        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        pack();

        if (getHeight() < 500)
            setSize(getWidth(), 500);
    }

    void cancel()
    {
        wasOKd = false;
        System.err.println("(cancelled)");
        setVisible(false);
//        dispose();
    }

    private boolean wasOKd = false;

    boolean wasOKd()
    {
        return wasOKd;
    }

    private String okString;
    JList ojl;
    private JOptionPane configurationOptionPane;
    
    private JOptionPane getConfigurationOptionPane()
    {
        if (configurationOptionPane == null) {
            ChannelSetter[] sa = new ChannelSetter[ca.ccl.size() + 1];
            for (int j = 0 ; j < ca.ccl.size() ; j++) {
                sa[j] = new ChannelSetter(ca.ccl.get(j));
            }
            
            sa[sa.length - 1] = new ChannelSetter(null);
    
            ojl = new JList(sa);
            ojl.setSelectedIndex(0);
            ojl.setBorder(BorderFactory.createEtchedBorder());
    
            okString = UIManager.getString("OptionPane.okButtonText");
    
            configurationOptionPane = new JOptionPane(new JComponent[]{ojl}, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                    new String[]{okString, UIManager.getString("OptionPane.cancelButtonText")}, null);
        }
        
        return configurationOptionPane;
    }

    boolean showConfigurationDialog(Component parent)
    {
        JOptionPane optionPane = getConfigurationOptionPane();
        
        optionPane.createDialog(parent, "Available Television").setVisible(true);
        
        if (okString.equals(optionPane.getValue())) {
//            System.err.println(csm.selected);
            ChannelSetter cs = (ChannelSetter)ojl.getSelectedValue();
            boolean needsConfiguration = cs.applyTo(csm.selected, csm.allChannels);
            csm.updated();
 //           System.err.println("Applied " + cs);
 //           System.err.println(csm.selected);
            return needsConfiguration;
        } else {
 //           System.err.println("Okay was not clicked");
            return false;
        }
    }

    static class ChannelSetter
    {
        private ChannelAvailability.ChannelCollection cc;

        ChannelSetter(ChannelAvailability.ChannelCollection cc)
        {
            this.cc = cc;
        }
        
        boolean applyTo(Set<String> s, Set<String> all)
        {
            if (cc != null) {
                s.clear();
                s.addAll(cc.available(all));
                return false;
            } else {
                return true;
            }
        }
        
        public String toString()
        {
            return (cc != null) ? cc.name : "I'll pick channels myself";
        }
    }
    
    static class ChannelSelectionModel extends DefaultListModel
    {
        final Set<String> allChannels;
//        private final TokenSetFile selected;

        final Set<String> selected;

        ChannelSelectionModel(List<String> channels)
        {
            allChannels = new HashSet<String>(channels);
            Iterator<String> i = channels.iterator();
            while (i.hasNext())
                addElement(i.next());
            this.selected = new HashSet<String>();
        }

        void setSelected(Collection<String> c)
        {
            selected.clear();
            selected.addAll(c);
        }

        public boolean isSelected(String channel)
        {
            return selected.contains(channel);
        }

        boolean isUnconfigured()
        {
            return selected.isEmpty();
        }

        public void toggle(int i)
        {
            String channel = (String)get(i);

            if (selected.contains(channel))
                selected.remove(channel);
            else
                selected.add(channel);
            
            fireContentsChanged(this, i, i);
        }
        
        void updated()
        {
            fireContentsChanged(this, 0, size());
        }
    }
}
