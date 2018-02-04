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

import edu.uci.ics.jung.graph.*;

import java.util.*;

public class GraphGenerator {

    public static Graph<State,Transition> generateTransitionsGraph(MDCSolution mdc) {
	DirectedGraph<State,Transition> graph = new DirectedSparseGraph<State,Transition>();
	// Adding vertices
	Collection<State> states = mdc.getStates();
	for (State s:states) {
	    graph.addVertex(s);
	}
	// Adding edges
	Collection<Transition> transitions = mdc.getTransitions();
	for (Transition t:transitions) {
	    State from = t.getPrestate();
	    State to = t.getPoststate();
	    graph.addEdge(t, from, to);
	}
	System.out.println("Generated graph is: " + graph.toString());
	return graph;
    }

    public static Graph<State,Transition> generateExecutionGraph(Execution exec) {
	DirectedGraph<State,Transition> graph = new DirectedSparseGraph<State,Transition>();
	// Adding vertices
	Collection<State> states = exec.getStates();
	for (State s:states) {
	    graph.addVertex(s);
	}
	// Adding edges
	List<Transition> transitions = exec.getTransitions();
	for (int i = 0; i < transitions.size(); i++) {
	    NumberedTransition t = new NumberedTransition(transitions.get(i), i+1);
	    State from = t.getPrestate();
	    State to = t.getPoststate();
	    graph.addEdge((Transition) t, from, to);
	}
	System.out.println("Generated graph is: " + graph.toString());
	return graph;
    }
    
}
