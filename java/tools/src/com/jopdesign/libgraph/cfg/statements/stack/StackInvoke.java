/*
 * Copyright (c) 2007,2008, Stefan Hepp
 *
 * This file is part of JOPtimizer.
 *
 * JOPtimizer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * JOPtimizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jopdesign.libgraph.cfg.statements.stack;

import com.jopdesign.libgraph.cfg.statements.common.InvokeStmt;
import com.jopdesign.libgraph.cfg.statements.quad.QuadInvoke;
import com.jopdesign.libgraph.cfg.statements.quad.QuadStatement;
import com.jopdesign.libgraph.cfg.variable.Variable;
import com.jopdesign.libgraph.cfg.variable.VariableTable;
import com.jopdesign.libgraph.struct.ConstantMethod;
import com.jopdesign.libgraph.struct.ConstantValue;
import com.jopdesign.libgraph.struct.TypeException;
import com.jopdesign.libgraph.struct.type.TypeInfo;

/**
 * @author Stefan Hepp, e0026640@student.tuwien.ac.at
 */
public abstract class StackInvoke extends InvokeStmt implements StackStatement, StackAssign {

    public StackInvoke(ConstantMethod method, int invokeType) throws TypeException {
        super(method, invokeType);
    }

    public TypeInfo[] getPopTypes() {

        TypeInfo[] types;

        if ( getInvokeType() == TYPE_STATIC) {
            types = getParameterTypes();
        } else {
            TypeInfo[] params = getParameterTypes();
            types = new TypeInfo[params.length+1];

            types[0] = TypeInfo.CONST_OBJECTREF;
            for (int i = 0; i < params.length; i++ ) {
                types[i+1] = params[i];
            }
        }

        return types;
    }

    public TypeInfo[] getPushTypes() {
        TypeInfo retType = getResultType();
        return retType != null && retType.getType() != TypeInfo.TYPE_VOID ?
                new TypeInfo[] {retType} : new TypeInfo[0];
    }

    public QuadStatement[] getQuadCode(TypeInfo[] stack, VariableTable varTable) throws TypeException {
        TypeInfo[] paramTypes = getParameterTypes();
        Variable[] params = new Variable[paramTypes.length];

        for (int i = 0; i < params.length; i++) {
            params[i] = varTable.getDefaultStackVariable(stack.length - params.length + i);
        }

        if ( getInvokeType() == TYPE_STATIC ) {
            Variable s0 = varTable.getDefaultStackVariable(stack.length - params.length);
            return new QuadStatement[] { new QuadInvoke(getMethodConstant(),
                getPushTypes().length != 0 ? s0 : null, params) };
        } else {
            Variable s0 = varTable.getDefaultStackVariable(stack.length - params.length - 1);
            return new QuadStatement[] { new QuadInvoke(getMethodConstant(), getInvokeType(),
                getPushTypes().length != 0 ? s0 : null, s0, params) };
        }
    }

    public String getCodeLine() {
        return getInvokeName() + " " + getMethodConstant().getClassName() + "." +
                getMethodConstant().getMethodName() + getMethodConstant().getSignature();
    }

    public ConstantValue[] getConstantValues(ConstantValue[] input) {
        return new ConstantValue[0];
    }

}