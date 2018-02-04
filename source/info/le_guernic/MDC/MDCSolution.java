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
import java.nio.file.*;
import java.nio.charset.*;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.*;
import edu.mit.csail.sdg.alloy4compiler.translator.*;

public class MDCSolution {

    private final Map<String,State> states = new HashMap<String,State>();
    private final Map<String,Label> labels = new HashMap<String,Label>();
    private final Map<String,Transition> transitions = new HashMap<String,Transition>();
    private final Map<String,Execution> executions = new TreeMap<String,Execution>();
    private final Map<String,Trace> traces = new TreeMap<String,Trace>();
    //private Label EmptyLabel;
    //private Label NoLabel;
    private final Map<String,Set<String>> varMapping = new TreeMap<String,Set<String>>();


    public MDCSolution(Module alloyWorld, A4Solution alloySolution) throws Err {
	if ( alloySolution.satisfiable() ) {
	    // You can also write the outcome to an XML file
	    alloySolution.writeXML("alloy_example_output.xml");
	    Iterable<ExprVar> atoms = alloySolution.getAllAtoms();
	    Map<String,Sig> signatures = new HashMap<String,Sig>();
	    for (Sig sig:alloySolution.getAllReachableSigs()) {
		if (sig.label.toString().startsWith("this/")) {
		    String sigName = sig.label.toString().substring(5);
		    signatures.put(sigName,sig);
		}
	    }
	    // System.out.println(">>> " + signatures);
	    Map<String,Sig.Field> relations = new HashMap<String,Sig.Field>();
	    for (String sigName : signatures.keySet()) {
		for (Sig.Field field:signatures.get(sigName).getFields()) {
		    String relName = field.label.toString();
		    relations.put(sigName+"."+relName,field);
		}
	    }
	    // System.out.println(">>> " + relations);
	    
	    A4TupleSet emptyLabels = alloySolution.eval(signatures.get("EmptyLabel"));
	    for (A4Tuple l:emptyLabels) {
		getLabel(l.atom(0)).setEmpty(true);
	    }
	    
	    A4TupleSet preStates = alloySolution.eval(relations.get("Transition.preState"));
	    A4TupleSet postStates = alloySolution.eval(relations.get("Transition.postState"));
	    A4TupleSet triggers = alloySolution.eval(relations.get("Transition.trigger"));
	    A4TupleSet effects = alloySolution.eval(relations.get("Transition.effect"));
	    for (A4Tuple e:preStates) {
		// System.out.println(">>>>>>>>> " + e.atom(0) + " -> " + e.atom(1));
		Transition t = getTransition(e.atom(0));
		t.setPrestate(getState(e.atom(1)));
	    }
	    for (A4Tuple e:postStates) {
		// System.out.println(">>>>>>>>> " + e.atom(0) + " -> " + e.atom(1));
		Transition t = getTransition(e.atom(0));
		t.setPoststate(getState(e.atom(1)));
	    }
	    for (A4Tuple e:triggers) {
		// System.out.println(">>>>>>>>> " + e.atom(0) + " -> " + e.atom(1));
		Transition t = getTransition(e.atom(0));
		t.setTrigger(getLabel(e.atom(1)));
	    }
	    for (A4Tuple e:effects) {
		// System.out.println(">>>>>>>>> " + e.atom(0) + " -> " + e.atom(1));
		Transition t = getTransition(e.atom(0));
		t.setEffect(getLabel(e.atom(1)));
	    }
	    
	    A4TupleSet initSt = alloySolution.eval(relations.get("Execution.initialState"));
	    Map<String,ArrayList<Transition>> transMap = new HashMap<String,ArrayList<Transition>>();
	    for (A4Tuple e:initSt) {
		Execution exec = getExecution(e.atom(0));
		exec.initWith(getState(e.atom(1)));
		transMap.put(e.atom(0), new ArrayList<Transition>());
	    }
	    A4TupleSet trans = alloySolution.eval(relations.get("Execution.transitions"));
	    for (A4Tuple r:trans) {
		Execution exec = getExecution(r.atom(0));
		int pos = Integer.parseInt(r.atom(1));
		Transition t = getTransition(r.atom(2));
		ArrayList<Transition> arr = transMap.get(r.atom(0));
		arr.ensureCapacity(pos + 1);
		arr.add(pos,t);
	    }
	    transMap.forEach(
			     (k,v) -> {
				 Execution exec = getExecution(k);
				 v.forEach( (t) -> { exec.step(t); } );
			     }
			     );
	    
	    A4TupleSet traces = alloySolution.eval(signatures.get("Trace"));
	    Map<String,ArrayList<Label>> labelsMap = new HashMap<String,ArrayList<Label>>();
	    for (A4Tuple r:traces) {
		Trace t = getTrace(r.atom(0));
		labelsMap.put(r.atom(0), new ArrayList<Label>());
	    }
	    A4TupleSet labels = alloySolution.eval(relations.get("Trace.content"));
	    for (A4Tuple r:labels) {
		Trace t = getTrace(r.atom(0));
		int pos = Integer.parseInt(r.atom(1));
		Label l = getLabel(r.atom(2));
		ArrayList<Label> arr = labelsMap.get(r.atom(0));
		arr.ensureCapacity(pos + 1);
		arr.add(pos,l);
	    }
	    labelsMap.forEach(
			     (k,v) -> {
				 Trace trace = getTrace(k);
				 v.forEach( (l) -> { trace.add(l); } );
			     }
			     );
	    for (ExprVar skol:alloySolution.getAllSkolems()) {
		A4TupleSet ts = (A4TupleSet) alloySolution.eval((Expr) skol);
		Set<String> entities = new HashSet<String>();
		for (A4Tuple t:ts) {
		    entities.add(t.toString());
		}
		varMapping.put(skol.label, entities);
	    }
	}
    }

    // OUPUT METHODS

    public void outputTransitionsToDotFile(String dotFilePathStr) {
	Path dotFilePath = FileSystems.getDefault().getPath(dotFilePathStr);
	try (BufferedWriter writer = Files.newBufferedWriter(dotFilePath, StandardCharsets.UTF_8)) {
	    writer.write("digraph Transitions {\n");
	    for (Transition t:getTransitions()) {
		String pre = t.getPrestate().getDotName();
		String post = t.getPoststate().getDotName();
		String trigger = t.getTrigger().getDotName();
		String effect = t.getEffect().getDotName();
		writer.write("  " + pre + "->" + post + " [label=\"" + trigger + "/" + effect + "\"];\n");
	    }
	    writer.write("}\n");
	    writer.close();
	} catch (IOException e) {
	    System.err.format("IOException: %s%n", e);
	}
    }

    // STATE RELATED METHODS

    public State getState(String name) {
	if (states.containsKey(name)) {
	    return states.get(name);
	} else {
	    State s = new State(name);
	    states.put(name, s);
	    return s;
	}
    }

    public Collection<State> getStates() {
	return states.values();
    }

    public void printStates() {
	System.out.println(states);
    }

    // LABEL RELATED METHODS
    
    public Label getLabel(String name) {
	if (labels.containsKey(name)) {
	    return labels.get(name);
	} else {
	    Label s = new Label(name);
	    labels.put(name, s);
	    return s;
	}
    }

    public void printLabels() {
	System.out.println(labels);
    }
    
    // TRANSITION RELATED METHODS

    public Transition getTransition(String name) {
	if (transitions.containsKey(name)) {
	    return transitions.get(name);
	} else {
	    Transition s = new Transition(name);
	    transitions.put(name, s);
	    return s;
	}
    }

    public Collection<Transition> getTransitions() {
	return transitions.values();
    }

    public void printTransitions() {
	System.out.println(transitions);
    }

    // EXECUTION RELATED METHODS

    public Execution getExecution(String name) {
	if (executions.containsKey(name)) {
	    return executions.get(name);
	} else {
	    Execution s = new Execution(name);
	    executions.put(name, s);
	    return s;
	}
    }

    public Collection<Execution> getExecutions() {
	return executions.values();
    }

    public void printExecutions() {
	System.out.println(executions);
    }

    // TRACE RELATED METHODS

    public Trace getTrace(String name) {
	if (traces.containsKey(name)) {
	    return traces.get(name);
	} else {
	    Trace s = new Trace(name);
	    traces.put(name, s);
	    return s;
	}
    }

    public Collection<Trace> getTraces() {
	return traces.values();
    }

    public void printTraces() {
	System.out.println(traces);
    }

    // VARIABLES MAPPING METHODS
    
    public Map<String,Set<String>> getVariablesMapping() {
	return varMapping;
    }
}
