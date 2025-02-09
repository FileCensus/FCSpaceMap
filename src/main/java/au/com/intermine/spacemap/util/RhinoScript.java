/*
 *
 * FCSpaceMap
 *
 * Copyright (C) 1997-2025  Intermine Pty Ltd. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package au.com.intermine.spacemap.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class RhinoScript {

	private String _script;
	private Context _context;
	private Scriptable _scope;
	private Script _compiledScript;

	public RhinoScript(String script) {
		_script = script;
	}

	public Object compile() {
		_context = new ContextFactory().enterContext();
		_scope = new ImporterTopLevel(_context, true);
		_compiledScript = _context.compileString(_script, "RHINO SCRIPT", 1, null);
		Object result = _compiledScript.exec(_context, _scope);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getGlobal(String name) {
		T result = null;
		if (_scope != null) {
			result = (T) _scope.get(name, _scope);
		}	
		
		return result;
	}

	public Object callFunction(String name, Object... args) {
		Object result = null;
		Object fObj = _scope.get(name, _scope);
		if (fObj instanceof Function) {
			Function f = (Function) fObj;
			Object[] wrappedArgs = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				wrappedArgs[i] = Context.javaToJS(args[i], _scope);
			}
			Object fRes = f.call(_context, _scope, _scope, wrappedArgs);
			result = Context.jsToJava(fRes, Object.class);
		} else {
			throw new RuntimeException("'" + name + "' could not be found, or is not a function (" + fObj + ")");
		}

		return result;
	}

	@SuppressWarnings("deprecation")
	public static Object executeRhinoFunction(String script, String funcname, Object... args) {
		Object result = null;
		if (script != null && !script.equals("")) {
			try {
				Context cx = Context.enter();
				// create scope
				Scriptable scope = new ImporterTopLevel(cx, true);
				// compile and execute the script
				Script compiledScript = cx.compileString(script, "RHINO SCRIPT ACTION", 1, null);
				compiledScript.exec(cx, scope);
				if (funcname != null) {
					Object fObj = scope.get(funcname, scope);
					if (fObj instanceof Function) {
						Function f = (Function) fObj;
						Object[] wrappedArgs = new Object[args.length];
						for (int i = 0; i < args.length; i++) {
							wrappedArgs[i] = Context.javaToJS(args[i], scope);
						}
						Object fRes = f.call(cx, scope, scope, wrappedArgs);
						result = Context.jsToJava(fRes, Object.class);
					} else {
						throw new RuntimeException("Could not find function '" + funcname + "'");
					}
				} else {
					result = compiledScript.exec(cx, scope);
				}
				Context.exit();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return result;
	}

	public static String getUserScript() {
		try {
			// First look to see if there is a system property called 'user.script.file'...
			
			String scriptpath = System.getProperty("user.script.file");
			InputStream is =  null;
			
			if (scriptpath == null) {			
				is = RhinoScript.class.getResourceAsStream("/au/com/intermine/spacemap/resource/default.js");
			} else {
				is = new FileInputStream(scriptpath);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				builder.append(line).append((char) Character.LINE_SEPARATOR);
				line = reader.readLine();
			}
			reader.close();
			is.close();
			return builder.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public Scriptable getScope() {
		return _scope;
	}

	public Context getContext() {
		return _context;
	}

}
