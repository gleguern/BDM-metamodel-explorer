// Copyright (c) 2018, Gurvan LE GUERNIC
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 
// * Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
// 
// * Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
// 
// * Neither the name of the copyright holder nor the names of its
//   contributors may be used to endorse or promote products derived from
//   this software without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

module MDC

// STATES
sig State {}

// LABELS
sig Label {}
one sig NoLabel extends Label {}
lone sig EmptyLabel in Label {}
sig TLabel in Label {}
sig ELabel in Label {}
fact {
	(TLabel & ELabel) in EmptyLabel
	NoLabel not in (ELabel + TLabel + EmptyLabel)
}

// TRACES
sig Trace {
	content : seq Label
}
fun projectionFct  [ofTrace:seq Label, onLabels:set Label] : seq Label {
  (ofTrace.isEmpty)
  implies ofTrace
  else (
    (ofTrace.first in onLabels)
    implies projectionFct[ofTrace.rest, onLabels].insert[0, ofTrace.first]
    else projectionFct[ofTrace.rest, onLabels]
  )
}
/* pred projection_internal [ofTrace:seq Label, onLabels:set Label, isTrace:seq Label] {
  ofTrace.isEmpty
  implies isTrace = ofTrace
  else (
    (ofTrace.first in onLabels)
    implies (
      ofTrace.first = isTrace.first and
      projection_internal[ofTrace.rest, onLabels, isTrace.rest]
    )
    else projection_internal[ofTrace.rest, onLabels, isTrace]
  )
} */
pred projection_internal [ofTrace:seq Label, onLabels:set Label, isTrace:seq Label] {
	(NoLabel not in (ofTrace.elems + onLabels + isTrace.elems)) and 
	(isTrace.elems in onLabels) and
	(some iTrace : seq Label |
		#iTrace = #ofTrace
		and iTrace.elems in (onLabels + NoLabel)
		and ( all i : ofTrace.inds |
			ofTrace[i] in onLabels
			implies iTrace[i] = ofTrace[i]
			else iTrace[i] = NoLabel
		)
		and #isTrace = (#iTrace).minus[#(iTrace.indsOf[NoLabel])]
		and (all i : iTrace.inds |
			iTrace[i] = NoLabel
			or iTrace[i] = isTrace[i.minus[#(iTrace.subseq[0,i].indsOf[NoLabel])]]
		)
	)
}
pred projection [ofTrace:one Trace, onLabels:set Label, isTrace:one Trace] {
	projection_internal[ofTrace.content, onLabels, isTrace.content]
	// isTrace.content = projectionFct[ofTrace.content, onLabels]
}

// TRANSITIONS
sig Transition {
	preState : one State,
	postState : one State,
	trigger : one TLabel,
	effect : one ELabel
}

// EXECUTIONS
sig Execution {
	initialState : one State,
	transitions : seq Transition,
	finalState : one State,
}{
	transitions.isEmpty
	implies (initialState = finalState)
	else (
		transitions.first.preState = initialState and
		transitions.last.postState = finalState
	)
	all i:Int |
		(i in transitions.butlast.inds)
			=> transitions[i].postState =  transitions[plus[i,1]].preState
}
pred transitionTrace[ofTrans:Transition, isTrace:seq Label] {
	(EmptyLabel not in isTrace.elems)
	and (#isTrace = 2 implies (isTrace.first = ofTrans.trigger and isTrace.last = ofTrans.effect))
	and (
		#isTrace = 1 implies (
			(ofTrans.trigger = EmptyLabel or ofTrans.effect = EmptyLabel)
			and (isTrace.first = ofTrans.trigger or isTrace.last = ofTrans.effect)
		)
	)
	and (#isTrace = 0 implies (ofTrans.trigger = EmptyLabel and ofTrans.effect = EmptyLabel))
}
/*
pred executionTrace_internal[ofExec:seq Transition, isTrace:seq Label] {
	ofExec.isEmpty
	implies isTrace.isEmpty
	else (
		some trHd, trTl : seq Label | (
			isTrace = trHd.append[trTl] and
			transitionTrace[ofExec.first, trHd] and
			executionTrace_internal[ofExec.rest, trTl]
		)
	)
} */
pred executionTrace_internal[ofExec:seq Transition, isTrace:seq Label] {
	no ((EmptyLabel + NoLabel) & isTrace.elems) 	and
	(some iTrace : seq Label |
		#iTrace = 2.mul[#ofExec]
		and (all i : ofExec.inds |
			transitionTrace[ ofExec[i], isTrace.subseq[ i.mul[2], i.mul[2].plus[1] ] ]
		)
		and #isTrace = (#iTrace).minus[#(iTrace.indsOf[EmptyLabel])]
		and (all i : iTrace.inds |
			iTrace[i] = EmptyLabel
			or iTrace[i] = isTrace[i.minus[#(iTrace.subseq[0,i].indsOf[EmptyLabel])]]
		)
	)
}
pred executionTrace[ofExec:one Execution, isTrace:one Trace] {
	executionTrace_internal[ofExec.transitions, isTrace.content]
}

// RUNS
pred someLongExecution {
	some e : Execution | #(e.transitions.elems.preState) > 4
}
pred someLongExecutionWithItsTrace {
	some e : Execution, te : Trace |
		#(e.transitions.elems.preState) > 4
		and executionTrace[e,te]
}
pred someLongExecutionWithItsProjectedTrace {
	some e : Execution, te,tp : Trace, l : set Label |
		#(e.transitions.elems.preState) > 4
		and executionTrace[e,te]
		and projection[te,l,tp]
		and #(tp.content.elems) < #(te.content.elems)
}
run someLongExecution {someLongExecution} for 6 but 5 Int, 10 seq
run someLongExecutionWithItsTrace {someLongExecutionWithItsTrace} for 6 but 5 Int, 10 seq
run someLongExecutionWithItsProjectedTrace {someLongExecutionWithItsProjectedTrace} for 6 but 5 Int, 10 seq
