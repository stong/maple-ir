package org.rsdeob.stdlib.ir.transform.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rsdeob.stdlib.collections.NullPermeableHashMap;
import org.rsdeob.stdlib.collections.SetCreator;
import org.rsdeob.stdlib.ir.CodeBody;
import org.rsdeob.stdlib.ir.Local;
import org.rsdeob.stdlib.ir.StatementGraph;
import org.rsdeob.stdlib.ir.StatementVisitor;
import org.rsdeob.stdlib.ir.api.ICodeListener;
import org.rsdeob.stdlib.ir.expr.VarExpression;
import org.rsdeob.stdlib.ir.stat.CopyVarStatement;
import org.rsdeob.stdlib.ir.stat.Statement;
import org.rsdeob.stdlib.ir.stat.SyntheticStatement;

public class UsesAnalyserImpl implements ICodeListener<Statement> {

	private final CodeBody body;
	private final StatementGraph graph;
	private final DefinitionAnalyser definitions;
	private final NullPermeableHashMap<CopyVarStatement, Set<Statement>> uses;
//	private final NullPermeableHashMap<Statement, Set<Local>> used;
	private final Map<CopyVarStatement, SyntheticStatement> synth;
	private final LinkedList<Statement> queue;
	
	public UsesAnalyserImpl(CodeBody body, StatementGraph graph, DefinitionAnalyser defs) {
		this.body = body;
		this.graph = graph;
		definitions = defs;
		uses = new NullPermeableHashMap<>(new SetCreator<>());
//		used = new NullPermeableHashMap<>(new SetCreator<>());
		synth = new HashMap<>();
		queue = new LinkedList<>();
		init();
		commit();
	}
	
	public Set<Statement> getUses(CopyVarStatement def) {
		return uses.get(def);
	}
	
	private void init() {
		queue.addAll(graph.vertices());
	}

	private void appendQueue(Statement s) {
		if(!queue.contains(s)) {
			queue.add(s);
		}
	}
	
	@Override
	public void update(Statement n) {
		appendQueue(n);
	}

	@Override
	public void preRemove(Statement n) {
		queue(n);
		remove(n);
	}

	@Override
	public void postRemove(Statement n) {
		definitions.commit();
	}

	@Override
	public void insert(Statement p, Statement s, Statement n) {
		throw new RuntimeException();
	}
	
	@Override
	public void replaced(Statement old, Statement n) {
		throw new RuntimeException();
	}

	@Override
	public void commit() {
		while(!queue.isEmpty()) {
			Statement s = queue.removeLast();
			build(s);
		}
	}
	
	public void queue(Statement stmt) {
		Set<Local> locals = new HashSet<>();
		new StatementVisitor(stmt) {
			@Override
			public Statement visit(Statement s) {
				if(s instanceof VarExpression) {
					locals.add(((VarExpression) s).getLocal());
				}
				return s;
			}
		}.visit();
		
		Map<Local, Set<CopyVarStatement>> dmap = definitions.in(stmt);
		for(Local l : locals) {
			// System.out.println("local: " + l + " in " + dmap);
			for(CopyVarStatement def : dmap.get(l)) {
				for(Statement use : uses.get(def)) {
					Statement from = def;
					if(synth.containsKey(from)) {
						from = synth.get(from);
					}
					Set<Statement> trail = graph.wanderAllTrails(from, use);
					trail.remove(stmt);
					for(Statement s : trail) {
						appendQueue(s);
					}
				}
			}
		}
	}
	
	public void remove(Statement stmt) {
//		if(stmt instanceof CopyVarStatement) {
//			if(!uses.getNonNull((CopyVarStatement)stmt).isEmpty()){
//				throw new UnsupportedOperationException("Can't remove " + stmt + " with uses: " + uses.get(stmt));
//			}
//		}
		
		if(stmt instanceof SyntheticStatement || synth.get(stmt) != null) {
			throw new UnsupportedOperationException(stmt.toString() + ", type: " + stmt.getClass().getCanonicalName());
		}
		
		uses.remove(stmt);
//		used.remove(stmt);
		
		// this shouldn't do anything now, since we don't
		// allow Def statements which have uses to be removed.
		
		 for(Entry<CopyVarStatement, Set<Statement>> e : uses.entrySet()) {
		 	e.getValue().remove(stmt);
		 }
	}
	
	public void build(Statement stmt) {
//		Set<Local> varSet = used.getNonNull(stmt);
//		varSet.clear();
		
		if(stmt instanceof SyntheticStatement) {
			SyntheticStatement syn = (SyntheticStatement) stmt;
			if(syn.getStatement() instanceof CopyVarStatement) {
				// System.out.println("for " + stmt);
				synth.put((CopyVarStatement) syn.getStatement(), syn);
			}
		}
		
		StatementVisitor vis = new StatementVisitor(stmt) {
			@Override
			public Statement visit(Statement s) {
				if(s instanceof VarExpression) {
					VarExpression var = (VarExpression) s;
					Local local = var.getLocal();
					
//					varSet.add(local);

					NullPermeableHashMap<Local, Set<CopyVarStatement>> defMaps = definitions.in(stmt);
					try {
						Set<CopyVarStatement> defs = defMaps.get(local);
						for(CopyVarStatement def : defs) {
							uses.getNonNull(def).add(stmt);
						}
					} catch(Exception e) {
						System.out.println("at " + stmt.getId() + " " + stmt);
						System.out.println("  > " + s);
						System.out.println("   defs: " + defMaps);
						System.out.println(body);
						throw e;
					}
				}
				return s;
			}
		};
		vis.visit();
	}
}