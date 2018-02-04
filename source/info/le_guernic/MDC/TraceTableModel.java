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
import javax.swing.table.AbstractTableModel;

public class TraceTableModel extends AbstractTableModel {

    private final String[] header = {"Name", "Sequence"};
    private List<Trace> data = new ArrayList<Trace>();
  
    public int getRowCount() {
        return data.size();
    }
 
    public int getColumnCount() {
        return header.length;
    }
 
    public String getColumnName(int columnIndex) {
        return header[columnIndex];
    }
 
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex){
            case 0:
                return data.get(rowIndex).getGUIName();
            case 1:
		final List<String> labelsStr = new ArrayList<String>();
		for (Label l : data.get(rowIndex).getLabelsSequence()) {
		    labelsStr.add(l.getGUIName());
		}
                return String.join(" ; ", labelsStr);
            default:
                return null; // Should never arise with current implementation
        }
    }
 
    public int silentlyAddTrace(Trace t) {
        if ( data.add(t) ){
	    return data.size() - 1;
	} else {
	    return -1;
	}
    }
 
    public void addTrace(Trace t) {
        final int rowIdx = silentlyAddTrace(t);
	if (rowIdx >= 0) {
	    fireTableRowsInserted(rowIdx, rowIdx);
	}
    }
 
    public int silentlyAddTraces(Collection<Trace> traces) {
	final int oldNbTraces = data.size();
        data.addAll(traces);
        return data.size() - oldNbTraces;
    }
 
    public void addTraces(Collection<Trace> traces) {
	final int oldNbTraces = data.size();
	final int nbTracesAdded = silentlyAddTraces(traces);
        if (nbTracesAdded > 0) {
	    fireTableRowsInserted(oldNbTraces, oldNbTraces + nbTracesAdded);
	}
    }
 
    public int silentlyRemoveAllTraces() {
	final int nbTracesRemoved = data.size();
        data = new ArrayList<Trace>();;
	return nbTracesRemoved;
    }
 
    public void removeAllTraces() {
	final int nbTracesRemoved = silentlyRemoveAllTraces();
        if (nbTracesRemoved > 0) {
	    fireTableRowsDeleted(0, nbTracesRemoved - 1);
	}
    }
}
