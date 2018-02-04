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
import info.le_guernic.MDC.*;

import java.util.*;
import java.io.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.ConstList;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
// import edu.mit.csail.sdg.alloy4compiler.ast.Command;
// import edu.mit.csail.sdg.alloy4compiler.ast.Module;
// import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
// import edu.mit.csail.sdg.alloy4compiler.ast.ExprVar;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.*;
// import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
// import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
// import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
// import edu.mit.csail.sdg.alloy4viz.VizGUI;

import edu.uci.ics.jung.graph.Graph;

/** This class allows a more appropriate visualization of MDC models. */

public class Visualizer implements ActionListener {

    private A4Options options;
    private A4Reporter reporter;
    
    private VisualizerGUI gui = null;

    private Module alloyWorld = null;
    private Command alloyExecutedCmd = null;
    private A4Solution alloySolution = null;

    public Visualizer(String filename) throws Err {
	this.options = new A4Options();
	this.options.solver = A4Options.SatSolver.SAT4J;
	this.reporter =	new A4Reporter() {
	    // For example, here we choose to display each "warning" by printing it to System.out
	    @Override public void warning(ErrorWarning msg) {
		System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
		System.out.flush();
	    }
	};
	
	System.out.println("\n=========== Parsing+Typechecking '"+filename+"' =============");
	alloyWorld = CompUtil.parseEverything_fromFile(reporter, null, filename);

	System.out.println("\n=========== Initializing GUI =============");
	gui =  VisualizerGUI.createAndStart(alloyWorld.getAllCommands(), this);
    }
    
    public static void main(String[] args) throws Err {
	if (args.length == 1) {
	    Visualizer viz = new Visualizer(args[0]);
	} else {
	    System.out.println("Usage: Visualizer alsFile");
	}
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("Run command")) {
	    try {
		alloyExecutedCmd = gui.getSelectedCommand();
		run();
	    } catch (Err err) {
		System.out.println(">>> Error <<< while running command!");
	    }
	}
	if (e.getActionCommand().equals("Find next solution")) {
	    try {
		next();
	    } catch (Err err) {
		System.out.println(">>> Error <<< while running command!");
	    }
	}
    }

    public void run() throws Err {
	System.out.println("\n=========== Executing command '" + alloyExecutedCmd + "' =============");
	alloySolution = TranslateAlloyToKodkod.execute_command(reporter, alloyWorld.getAllReachableSigs(), alloyExecutedCmd, options);
	if (alloySolution.satisfiable()) {
	    updateGUIwithSolution();
	} else {
	    gui.popupInfo("No solution!");
	}
    }

    public void next() throws Err {
	System.out.println("\n=========== Looking for next solution =============");
	alloySolution = alloySolution.next();
	if (alloySolution.satisfiable()) {
	    updateGUIwithSolution();
	} else {
	    gui.popupInfo("No more solutions!");
	}
    }

    public void updateGUIwithSolution() throws Err {
	// Print the outcome
	System.out.println(alloySolution);
	gui.initGUIupdate();
	// If satisfiable...
	if (alloySolution.satisfiable()) {
	    MDCSolution mdc = new MDCSolution(alloyWorld, alloySolution);
	    
	    System.out.println("\n>>>>>>>>>> Transitions <<<<<<<<<<");
	    for (Transition t:mdc.getTransitions()) {
		System.out.println(t.toString());
	    }
	    System.out.println("\n>>>>>>>>>> Executions <<<<<<<<<<");
	    for (Execution e:mdc.getExecutions()) {
		System.out.println(e.toString());
	    }
	    System.out.println("\n>>>>>>>>>> Traces <<<<<<<<<<");
	    for (Trace t:mdc.getTraces()) {
		System.out.println(t.toString());
	    }

	    Graph<State,Transition> transGraph = GraphGenerator.generateTransitionsGraph(mdc);
	    System.out.println("\n>>>>>>>>>> Graph <<<<<<<<<<\n" + transGraph + "\n");
	    gui.setTransitions(transGraph);
	    //
	    for (Execution exec:mdc.getExecutions()) {
		Graph<State,Transition> execGraph = GraphGenerator.generateExecutionGraph(exec);
		gui.addExecution(exec.getGUIName(), execGraph);
	    }
	    //
	    gui.addTraces(mdc.getTraces());
	    //
	    gui.logInfo("Executed command:");
	    for (Func f:alloyWorld.getAllFunc()) {
		if (f.label.equals("this/"+alloyExecutedCmd.label)) {
		    gui.logInfo(" " + alloyExecutedCmd.label + "\n");
		    gui.logInfo("  " + f.getBody() + "\n");
		}
	    }
	    gui.logInfo("\n");
	    gui.logInfo("Variables mapping:\n");
	    for (Map.Entry<String, Set<String>> e : mdc.getVariablesMapping().entrySet()) {
		gui.logInfo("  - " + e.getKey() + " -> " + e.getValue() + "\n");
	    }
	}
	gui.finalizeGUIupdate();
    }
}
