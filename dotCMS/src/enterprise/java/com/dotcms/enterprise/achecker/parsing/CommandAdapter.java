/* 
* Licensed to dotCMS LLC under the dotCMS Enterprise License (the
* “Enterprise License”) found below 
* 
* Copyright (c) 2023 dotCMS Inc.
* 
* With regard to the dotCMS Software and this code:
* 
* This software, source code and associated documentation files (the
* "Software")  may only be modified and used if you (and any entity that
* you represent) have:
* 
* 1. Agreed to and are in compliance with, the dotCMS Subscription Terms
* of Service, available at https://www.dotcms.com/terms (the “Enterprise
* Terms”) or have another agreement governing the licensing and use of the
* Software between you and dotCMS. 2. Each dotCMS instance that uses
* enterprise features enabled by the code in this directory is licensed
* under these agreements and has a separate and valid dotCMS Enterprise
* server key issued by dotCMS.
* 
* Subject to these terms, you are free to modify this Software and publish
* patches to the Software if you agree that dotCMS and/or its licensors
* (as applicable) retain all right, title and interest in and to all such
* modifications and/or patches, and all such modifications and/or patches
* may only be used, copied, modified, displayed, distributed, or otherwise
* exploited with a valid dotCMS Enterprise license for the correct number
* of dotCMS instances.  You agree that dotCMS and/or its licensors (as
* applicable) retain all right, title and interest in and to all such
* modifications.  You are not granted any other rights beyond what is
* expressly stated herein.  Subject to the foregoing, it is forbidden to
* copy, merge, publish, distribute, sublicense, and/or sell the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
* 
* For all third party components incorporated into the dotCMS Software,
* those components are licensed under the original license provided by the
* owner of the applicable component.
*/

package com.dotcms.enterprise.achecker.parsing;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.dotcms.repackage.org.nfunk.jep.JEP;
import com.dotcms.repackage.org.nfunk.jep.ParseException;
import com.dotcms.repackage.org.nfunk.jep.function.PostfixMathCommand;

import com.dotcms.enterprise.achecker.validation.FunctionRepository;

public class CommandAdapter extends PostfixMathCommand {
	
	private FunctionRepository delegate;
	
	private JEP parser;
	
	private Method method;
	
	public CommandAdapter(JEP parser, FunctionRepository delegate, String name) {
		super();
		
		this.parser = parser;
		this.delegate = delegate;
		
		for ( Method method : delegate.getClass().getMethods()) {
			if ( method.getName().equals(name) ) {
				this.method = method;
				numberOfParameters = this.method.getParameterTypes().length;
				return;
			}
		}

		throw new IllegalArgumentException("Method not found: " + name);
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run(Stack inStack) throws ParseException {
				
		// check the stack
		checkStack(inStack);

		// Prepare global variable for function 
		delegate.setGlobalVariable("global_e", parser.getVarValue("global_e"));
		delegate.setGlobalVariable("global_check_id", parser.getVarValue("global_check_id"));
		delegate.setGlobalVariable("global_content_dom", parser.getVarValue("global_content_dom"));
		
		try {

			if ( numberOfParameters == 0 ) {
				Object ret = method.invoke(delegate);
				if ( ret != null )
					inStack.push(ret);
			}
			else {
				if ( numberOfParameters == -1 ) {
					int realSize = inStack.size();
					List<Object> list = new ArrayList<>(realSize);
					for ( int i = 0; i < realSize; i ++ )
						list.add(inStack.pop());
					Object ret = method.invoke(delegate, list);
					if ( ret != null )
						inStack.push(ret);
				}
				else {
					Object[] params = new Object[numberOfParameters];
					for ( int i = 0; i < numberOfParameters; i ++ ) {
						params[numberOfParameters - i - 1] = inStack.pop();
					}
					Object ret = method.invoke(delegate, params);
					if ( ret != null ) {
						inStack.push(ret);
					}
				}
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw new ParseException("Error in method: " + t.getMessage());
		}

	}

}
