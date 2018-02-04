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

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;

public class Execution {
    
    private String name = null;
    private State initialState = null;
    private State finalState = null;
    private final List<Transition> transitions = new ArrayList<Transition>();

    protected Execution(String name) {
	this.name = name;
    }

    public void initWith(State s) {
	this.initialState = s;
	this.finalState = s;
    }

    public void step(Transition t) {
	if (t.getPrestate() != this.finalState) {
	    // Should handle this case ... that should never arise
	} else {
	    this.transitions.add(t);
	    this.finalState = t.getPoststate();
	}
    }

    public String getName() { return this.name; }
    public String getGUIName() { return getName().replace("Execution$","E"); }

    public Collection<State> getStates() {
	Collection<State> states = (Collection<State>) new HashSet<State>();
	if (initialState != null) states.add(initialState);
	for (Transition t:this.transitions) {
	    // states.add(t.getPrestate());
	    states.add(t.getPoststate());
	}
	return states;
    }

    public List<Transition> getTransitions() {
	return ((List<Transition>) transitions);
    }
    
    public void outputToDotFile(String dotFilePathStr) {
	Path dotFilePath = FileSystems.getDefault().getPath(dotFilePathStr);
	try (BufferedWriter writer = Files.newBufferedWriter(dotFilePath, StandardCharsets.UTF_8)) {
	    writer.write("digraph " + this.name.replace("$","") + " {\n");
	    int stepNb = 0;
	    writer.write("  state" + stepNb + " [label=\"" + this.initialState.getDotName() + "\"];\n");
	    for (Transition t:this.transitions) {
		stepNb += 1;
		writer.write("  state" + stepNb + " [label=\"" + t.getPoststate().getDotName() + "\"];\n");
		String trigger = t.getTrigger().getDotName();
		String effect = t.getEffect().getDotName();
		writer.write("  state" + (stepNb - 1) + " -> state" + stepNb + " [label=\"" + trigger + "/" + effect + "\"];\n");
	    }
	    writer.write("}\n");
	    writer.close();
	} catch (IOException e) {
	    System.err.format("IOException: %s%n", e);
	}
    }

    public String toString() {
	String res = this.name + ": " + this.initialState.toString();
	for (Transition t:this.transitions) {
	    res = res + " --(" + t.getName() + ": " + t.getTrigger() + " | " + t.getEffect() + " )--> " + t.getPoststate();
	}
	return res;
    }

}
