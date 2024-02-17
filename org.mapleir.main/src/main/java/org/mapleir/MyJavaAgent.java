package org.mapleir;

import org.mapleir.asm.ClassHelper;
import org.mapleir.asm.ClassNode;
import org.mapleir.asm.MethodNode;
import org.mapleir.context.IRCache;
import org.mapleir.ir.algorithms.BoissinotDestructor;
import org.mapleir.ir.algorithms.LocalsReallocator;
import org.mapleir.ir.cfg.BasicBlock;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.mapleir.ir.code.Expr;
import org.mapleir.ir.code.Stmt;
import org.mapleir.ir.code.expr.ArithmeticExpr;
import org.mapleir.ir.code.expr.ArithmeticExpr.Operator;
import org.mapleir.ir.code.expr.invoke.StaticInvocationExpr;
import org.mapleir.ir.codegen.ControlFlowGraphDumper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.topdank.banalysis.asm.desc.Description;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class MyJavaAgent {
    public MyJavaAgent() {
    }

    public static void premain(String args, Instrumentation instr) {
        System.out.println("Hello from premain!");
        instrument(args, instr);
    }

    public static void agentmain(String args, Instrumentation instr) {
        System.out.println("Hello from agentmain!");
        instrument(args, instr);
    }

    private static void instrument(String args, Instrumentation instr) {
        instr.addTransformer(new JavaInstrumentTransformer(), false);
    }
}

class JavaInstrumentTransformer implements ClassFileTransformer {
    public JavaInstrumentTransformer() {
    }

    // Returns true if any changes were made. Otherwise, returns false
    private static boolean processCfg(ControlFlowGraph cfg) {
        boolean anyChanges = false;
        for (BasicBlock bb : cfg.vertices()) {
            for (Stmt stmt : bb) {
                for (Expr e : stmt.enumerateOnlyChildren()) {
                    if (e instanceof ArithmeticExpr) {
                        var e1 = (ArithmeticExpr) e;
                        var op = e1.getOperator();
                        if (op == Operator.ADD || op == Operator.SUB || op == Operator.MUL) {
                            // Replace arithmetic operation with function call to static helper function
                            String helperFuncName = "checked_" + op.name().toLowerCase();
                            assert e1.getLeft().getType() == e1.getRight().getType() && e1.getLeft().getType() == e1.getType();
                            Type type = e1.getLeft().getType();
                            if (type == Type.LONG_TYPE || type == Type.INT_TYPE || type == Type.SHORT_TYPE) {
                                String argumentDesc = type.getDescriptor(); // J I S B etc.
                                assert Description.isPrimitive(argumentDesc);
                                String helperFuncDesc = "(%s%s)%s".formatted(argumentDesc, argumentDesc, argumentDesc);
                                Expr[] args = new Expr[] { e1.getLeft(), e1.getRight() };
                                Arrays.stream(args).forEach(argExpr -> argExpr.setParent(null)); // before reparenting, set parent to null. in MapleIR, expr nodes may only belong to one parent node
                                var instrumentedOp = new StaticInvocationExpr(args, "UbsanHelper", helperFuncName, helperFuncDesc);
                                // Replace the arithmetic expr in the statement AST
                                e.getParent().writeAt(instrumentedOp, e.getParent().indexOf(e));
                                anyChanges = true;
                            }
                        }
                    }
                }
            }
        }
        return anyChanges;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        ClassNode cn = null;
        try {
            cn = ClassHelper.create(new ByteArrayInputStream(classfileBuffer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Do not instrument the actual UbsanHelper!
        // Otherwise this leads to an infinite recursion :-)
        if (cn.getName().equals("UbsanHelper")) {
            return classfileBuffer;
        }

        IRCache irFactory = new IRCache();

        for (MethodNode mn : cn.getMethods()) {
            // if (!mn.getName().equals("merge"))
            //     continue;

            ControlFlowGraph cfg = irFactory.getNonNull(mn);

            // Replace arithmetic with calls to helper funcs to do checked versions
            boolean madeChanges = processCfg(cfg);
            if (!madeChanges) {
                continue; // no need to recompile this class, there aren't arithmetic operators to instrument.
            }

            // Sanity and consistency checks
            cfg.verify();

            // Leave SSA form
            BoissinotDestructor.leaveSSA(cfg);
            LocalsReallocator.realloc(cfg);
            cfg.verify();

            // Export to bytes
            System.err.println("[UBSAN] Rewriting " + cn.getDisplayName() + "." + mn.getName());
            (new ControlFlowGraphDumper(cfg, mn)).dump();
            // System.out.println(InsnListUtils.insnListToString(mn.node.instructions));
        }

        byte[] result = ClassHelper.toByteArray(cn, ClassWriter.COMPUTE_FRAMES);
        return result;
    }
}
