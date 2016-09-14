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

package org.kafsemo.mivvi.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import org.kafsemo.mivvi.desktop.AppState;

public class EpisodeTreeCellRenderer extends JPanel implements TreeCellRenderer
{
//    private String value = "";
    JComponent jcb = new JComponent(){};
    JLabel jl = new JLabel();

//    private final AppState state;

    EpisodeTreeCellRenderer(AppState state)
    {
//        this.state = state;

        setLayout(null);

        Color bg = UIManager.getColor("Tree.textBackground"),
            fg = UIManager.getColor("Tree.textForeground");

        setBackground(bg);
        setForeground(fg);
//        jcb.setBackground(bg);
//        jl.setForeground(fg);
        this.add(jcb);
        this.add(jl);
    }

    private boolean implicit, selected;

    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus)
    {
        jl.setFont(tree.getFont());

        /*
         * Icons are *important*; this is (obviously) wholly inadequate ATM.
         * Individual programs should have their own icons, where possible
         * (ideally, favicons from main site pages). Seasons
         * could use Apple-alias-style derived icons, or something.
         */

        if (leaf) {
            jl.setIcon(UIManager.getIcon("Tree.leafIcon"));
        } else if (expanded) {
            jl.setIcon(UIManager.getIcon("Tree.openIcon"));
        } else {
            jl.setIcon(UIManager.getIcon("Tree.closedIcon"));
        }

//        this.value = value.toString();
        jl.setText(value.toString());
        jl.setForeground(Color.BLACK);

        Checkable c = (Checkable)value;

        this.implicit = c.implicit;
        this.selected = c.isSelected();

        if (c instanceof EpisodeTreeNode) {
            EpisodeTreeNode n = (EpisodeTreeNode)c;
            if (n.icon != null)
                jl.setIcon(n.icon);

        }

        if (c.isLocal) {
            jl.setFont(jl.getFont().deriveFont(Font.BOLD));
        }

        if (c.isPartiallyLocal) {
            jl.setForeground(Color.BLUE);
        } else if (selected) {
            jl.setForeground(UIManager.getColor("Tree.selectionForeground"));
        }

        if (selected) {
            jl.setBackground(UIManager.getColor("Tree.selectionBackground"));
        } else {
            jl.setBackground(UIManager.getColor("Tree.textBackground"));
        }

        return this;
    }

    public Dimension getPreferredSize()
    {
        Dimension a = ui.getPreferredSize(),
            b = jl.getPreferredSize();

        return new Dimension(a.width + b.width + PAD, Math.max(a.height, b.height) + 2);
    }

    private static final int PAD = 10;

    static TristateUI ui = TristateUI.getInstance();

    public void paintComponent(Graphics g)
    {
        Dimension d = getSize();

        Dimension a = ui.getPreferredSize();

        jcb.setBounds(0, (d.height - a.height) / 2, a.width, a.height);

        Graphics g2 = g.create(0, (d.height - a.height) / 2, a.width, a.height);

        ui.paint(g2, jcb, selected, implicit, false);

        int labelWidth = d.width - a.width - PAD,
            labelHeight = d.height;

        jl.setBounds(a.width + PAD, 0, labelWidth, labelHeight);

        g2 = g.create(a.width + PAD, 0, labelWidth, labelHeight);

        Color c = jl.getBackground();
        if (c != null) {
            g2.setColor(c);
            g2.fillRect(0, 0, labelWidth, labelHeight);
        }
        jl.paint(g2);
    }

    public static void getCheckboxRect(Dimension bounds, Rectangle t)
    {
        Dimension a = ui.getPreferredSize();

        t.setBounds(0, (bounds.height - a.height) / 2, a.width, a.height);
    }
}
