/**
 * Copyright (c) 2018, Gurvan LE GUERNIC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package info.le_guernic.MDC;

// import info.le_guernic.MDC.State;
// import info.le_guernic.MDC.Transition;

import java.util.*;
//import java.io.File;
//import java.io.IOException;

import edu.uci.ics.jung.graph.Graph;
// import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
// import edu.uci.ics.jung.visualization.VisualizationViewer;
// // import edu.uci.ics.jung.algorithms.layout.Layout;
// import edu.uci.ics.jung.algorithms.layout.FRLayout;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.table.TableModel;
import java.lang.reflect.InvocationTargetException;

import edu.mit.csail.sdg.alloy4compiler.ast.Command;

public final class VisualizerGUI {

    private JFrame frame;
    private JComboBox<Command> commandComboBox;
    private JButton runButton;
    private JButton nextButton;
    private JPanel transitionsDisplay;
    private JTabbedPane executionsDisplay;
    private JTable tracesDisplay;
    private JTextArea textInfoArea;

    public VisualizerGUI() {}

    private void initGUI(final Collection<Command> commands, final ActionListener actionsHandler) {
        //Create and set up the window.
        frame = new JFrame("BDM Metamodel Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	final JPanel controlPanel = new JPanel(new FlowLayout());
	commandComboBox = new JComboBox<Command>();
	for(Command cmd:commands){
	    commandComboBox.addItem(cmd);
	}
	controlPanel.add(commandComboBox);
	runButton = new JButton("Run");
	runButton.addActionListener(actionsHandler);
	runButton.setActionCommand("Run command");
	controlPanel.add(runButton);
	nextButton = new JButton("Next");
	nextButton.addActionListener(actionsHandler);
	nextButton.setActionCommand("Find next solution");
	controlPanel.add(nextButton);
	
	transitionsDisplay = new JPanel();
	executionsDisplay = new JTabbedPane();
	tracesDisplay = new JTable(new TraceTableModel());
	/** Does not do what I want
	tracesDisplay = new JTable(new TraceTableModel()){
            public boolean getScrollableTracksViewportWidth()
            {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
	*/
	// tracesDisplay.getColumnModel().getColumn(0).setPreferredWidth(100);
	// tracesDisplay.getColumnModel().getColumn(1).setPreferredWidth(200);
	tracesDisplay.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // if need be
	tracesDisplay.setFillsViewportHeight(true);
	
	final JSplitPane ETspliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, executionsDisplay, new JScrollPane(tracesDisplay));
	/** Does not do what I want
	tracesDisplay.getParent().addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(final ComponentEvent e) {
		    if (tracesDisplay.getPreferredSize().width < tracesDisplay.getParent().getWidth()) {
			tracesDisplay.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		    } else {
			tracesDisplay.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		    }
		}
	    });
	*/
	//tracesDisplay.setFillsViewportHeight(true);
	//tracesDisplay.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // if need be
	ETspliter.setResizeWeight(0.5);
	final JSplitPane graphsSpliter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, transitionsDisplay, ETspliter);
	graphsSpliter.setOneTouchExpandable(true);
	graphsSpliter.setResizeWeight(0.4);

	textInfoArea = new JTextArea();
	textInfoArea.setLineWrap(true);
	textInfoArea.setWrapStyleWord(true);
	textInfoArea.setEditable(false);
	final JScrollPane textInfoAreaSP = new JScrollPane(textInfoArea);
	
	final JSplitPane topSpliter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphsSpliter, textInfoAreaSP);
	topSpliter.setOneTouchExpandable(true);
	topSpliter.setResizeWeight(1.0);
	
	frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(controlPanel, BorderLayout.NORTH);
        frame.getContentPane().add(topSpliter, BorderLayout.CENTER);
	
        frame.setPreferredSize(new Dimension(800,600));
        frame.pack();
        frame.setVisible(true);
    }
    
    public static VisualizerGUI createAndStart(Collection<Command> commands, ActionListener actionsHandler) {
	VisualizerGUI gui = new VisualizerGUI();
	final Runnable guiRunner =
	    new Runnable() {
		public void run() {
		    gui.initGUI(commands, actionsHandler);
		    // gui.pack();
		}
	    };
	try {
	    javax.swing.SwingUtilities.invokeAndWait(guiRunner);
	} catch (InterruptedException e) {
	    System.out.println(">>> WARNING <<< InterruptedException while creating the GUI");
	} catch (InvocationTargetException e) {
	    System.out.println(">>> WARNING <<< InvocationTargetException while creating the GUI");
	}
	return gui;
    }

    public Command getSelectedCommand() {
	return commandComboBox.getItemAt(commandComboBox.getSelectedIndex());
    }

    public void clear() {
	initGUIupdate();
	finalizeGUIupdate();
    }

    public void initGUIupdate() {
	// transitionsDisplay.removeAll();
	executionsDisplay.removeAll();
	final TableModel tm = tracesDisplay.getModel();
	if (tm instanceof TraceTableModel) { ((TraceTableModel) tm).removeAllTraces(); }
	textInfoArea.setText(null);
    }

    public void pack() {
	frame.pack();
    }

    public void finalizeGUIupdate() {
	frame.revalidate();
	// frame.validate();
	// frame.repaint();
	// frame.setVisible(true);
    }

    public void setTransitions(Graph<State,Transition> g) {
	if ( g != null ) {
	    Container tDispParent = transitionsDisplay.getParent();
	    int currentDividerLocation = 0;
	    if (tDispParent instanceof JSplitPane) {
		currentDividerLocation = ((JSplitPane) tDispParent).getDividerLocation();
	    }
	    Dimension tDispDim = transitionsDisplay.getSize(null);
	    tDispDim.setSize(tDispDim.getWidth() * 0.9, tDispDim.getHeight() * 0.9);
	    // Create visualization
	    MDCGraphPane gPane = new MDCGraphPane(g, tDispDim);
	    // Insert vv in GUI
	    if (tDispParent instanceof JSplitPane) {
		transitionsDisplay = gPane;
		((JSplitPane) tDispParent).setLeftComponent(transitionsDisplay);
		((JSplitPane) tDispParent).setDividerLocation(currentDividerLocation);
	    }
	    else {
		transitionsDisplay.removeAll();
		transitionsDisplay.add(gPane);
	    }
	} else {
	    popupInfo("Error while updating transitions graph: graph object is null");
	}
    }

    public void setTransitionsInED(Graph<State,Transition> g) {
	final Runnable guiRunner =
	    new Runnable() {
		public void run() { setTransitions(g); }
	    };
	// javax.swing.SwingUtilities.invokeLater(guiRunner);
	try {
	    javax.swing.SwingUtilities.invokeAndWait(guiRunner);
	} catch (InterruptedException e) {
	    System.out.println(">>> WARNING <<< InterruptedException while creating the GUI");
	} catch (InvocationTargetException e) {
	    System.out.println(">>> WARNING <<< InvocationTargetException while creating the GUI");
	}
    }

    public void addExecution(String execName, Graph<State,Transition> g) {
	if ( g != null ) {
	    Dimension tDispDim = executionsDisplay.getSize(null);
	    tDispDim.setSize(tDispDim.getWidth() * 0.9, (tDispDim.getHeight() - 20) * 0.9);
	    MDCGraphPane gPane = new MDCGraphPane(g, tDispDim);
	    executionsDisplay.addTab(execName, gPane);
	} else {
	    popupInfo("Error while adding execution '" + execName + "': graph object is null");
	}
    }

    public void addExecutionInED(String execName, Graph<State,Transition> g) {
	final Runnable guiRunner =
	    new Runnable() {
		public void run() { addExecution(execName, g); }
	    };
	// javax.swing.SwingUtilities.invokeLater(guiRunner);
	try {
	    javax.swing.SwingUtilities.invokeAndWait(guiRunner);
	} catch (InterruptedException e) {
	    System.out.println(">>> WARNING <<< InterruptedException while creating the GUI");
	} catch (InvocationTargetException e) {
	    System.out.println(">>> WARNING <<< InvocationTargetException while creating the GUI");
	}
    }

    public void addTraces(Collection<Trace> traces) {
	final TableModel tm = tracesDisplay.getModel();
	if (tm instanceof TraceTableModel) {
	    ((TraceTableModel) tm).addTraces(traces);
	}
    }

    public void addTraceInED(Collection<Trace> traces) {
	final Runnable guiRunner =
	    new Runnable() {
		public void run() { addTraces(traces); }
	    };
	// javax.swing.SwingUtilities.invokeLater(guiRunner);
	try {
	    javax.swing.SwingUtilities.invokeAndWait(guiRunner);
	} catch (InterruptedException e) {
	    System.out.println(">>> WARNING <<< InterruptedException while creating the GUI");
	} catch (InvocationTargetException e) {
	    System.out.println(">>> WARNING <<< InvocationTargetException while creating the GUI");
	}
    }

    public void logInfo(String info) {
	textInfoArea.append(info);
    }

    public void popupInfo(String info) {
	JOptionPane.showMessageDialog(frame, info, "Warning", JOptionPane.INFORMATION_MESSAGE);
    }
}
