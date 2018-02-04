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

public class Trace {
    
    private String name = null;
    private final List<Label> labels = new ArrayList<Label>();

    protected Trace(String name) {
	this.name = name;
    }

    public void add(Label l) {
	this.labels.add(l);
    }

    public String getName() { return this.name; }
    public String getGUIName() { return getName().replace("Trace$","τ"); }

    public List<Label> getLabelsSequence() {
	return this.labels;
    }

    public void outputToDotFile(String dotFilePathStr) {
	Path dotFilePath = FileSystems.getDefault().getPath(dotFilePathStr);
	try (BufferedWriter writer = Files.newBufferedWriter(dotFilePath, StandardCharsets.UTF_8)) {
	    writer.write("digraph " + this.name.replace("$","") + " {\n");
	    int stepNb = 0;
	    String prevLabelId = null;
	    for (Label l:this.labels) {
		stepNb += 1;
		String labelId = "label" + stepNb;
		writer.write("  " + labelId + " [shape=box, label=\"" + l.getDotName() + "\"];\n");
		if ( prevLabelId != null ) {
		    writer.write("  " + prevLabelId + " -> " + labelId + ";\n");
		}
		prevLabelId = labelId;
	    }
	    writer.write("}\n");
	    writer.close();
	} catch (IOException e) {
	    System.err.format("IOException: %s%n", e);
	}
    }

    public String toString() {
	String res = this.name + ": " ;
	for (Label l:this.labels) {
	    if ( ! l.isEmpty() ) {
		res = res + l + " -> ";
	    }
	}
	return res;
    }

}
