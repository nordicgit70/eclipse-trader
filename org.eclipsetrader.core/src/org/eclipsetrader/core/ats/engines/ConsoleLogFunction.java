/*
 * Copyright (c) 2013 Bart Oortwijn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bart Oortwijn - initial implementation
 */
package org.eclipsetrader.core.ats.engines;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ConsoleLogFunction extends ScriptableObject {

	private static final long serialVersionUID = 4974876312437440783L;
    private final Log log = LogFactory.getLog(getClass());
    protected String text;

    public ConsoleLogFunction() {
    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj, boolean inNewExpr) throws Exception {
    	ConsoleLogFunction result = new ConsoleLogFunction();

    	int index = 0;
        if (args.length >= index + 1) {
            result.jsSet_text(args[index]);
            index++;
        }
    	
    	result.log.info(result.text);
    	
        return result;
    }
    
	@Override
	public String getClassName() {
        return "ConsoleLog"; //$NON-NLS-1$
	}

    public String jsGet_text() {
        return text;
    }

    public void jsSet_text(Object arg) {
        text = Context.toString(arg);
    }
}
