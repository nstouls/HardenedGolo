package org.eclipse.golo.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.eclipse.golo.cli.command.DiagnoseCommand.DiagnoseModeValidator;
import org.eclipse.golo.cli.command.spi.CliCommand;
import org.eclipse.golo.compiler.GoloClassLoader;
import org.eclipse.golo.compiler.GoloCompilationException;


import org.eclipse.golo.compiler.GoloCompiler;
import org.eclipse.golo.compiler.ir.GoloFunction;
import org.eclipse.golo.compiler.ir.GoloModule;
import org.eclipse.golo.compiler.ir.ModuleImport;
import org.eclipse.golo.compiler.parser.ASTCompilationUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@Parameters(commandNames = {"tests"}, commandDescription = "Run all the test with @Test decorator")
public class TestCommand implements CliCommand {
	
	@Parameter(description = "Golo source files (*.golo and directories)")
	List<String> files = new LinkedList<>();
	
	@Parameter(names = {"--run"}, description = "run tests")
	boolean run = false;

	private List<String> classpath;

	@Override
	public void execute() throws Throwable {
		
		try {
			GoloCompiler compiler = new GoloCompiler();
			File result = null;
			for(String file : this.files)
				result = extractTests(file, compiler);
			if(run){
				run(result,compiler);
				
			}
		} catch (GoloCompilationException e) {
			handleCompilationException(e);
		}
		
	}

	private void run(File result, GoloCompiler compiler) {
		Class<?> loadedClass = null;
		try (FileInputStream in = new FileInputStream(result)) {
			URLClassLoader primaryClassLoader = primaryClassLoader(this.classpath);
			GoloClassLoader loader = new GoloClassLoader(primaryClassLoader);
	        loadedClass = loader.load(result.getName(), in);
	        
	        callRun(loadedClass, null);
	        
	        
		} catch (GoloCompilationException e) {
	        handleCompilationException(e);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}	
	}

	private File extractTests(String goloFile, GoloCompiler compiler) {
		GoloModule module;
		File testFunctions = null;
		
		try {
			module = compiler.transform(compiler.parse(goloFile));
	        Set<GoloFunction> functions = module.getFunctions();
	        
	        String pathFile = goloFile.substring(0, goloFile.lastIndexOf("\\")+1);
	        testFunctions = new File(pathFile + "TestFunctions.golo");
	        
	        testFunctions.createNewFile();
	        PrintWriter write = new PrintWriter(testFunctions);
	        
	        write.println("module TestFunctions");
	        write.println("import " + module.getPackageAndClass().className());
	        for(ModuleImport imp : module.getImports())
	        	write.println("import " + imp.getPackageAndClass());
	        write.println();
	        
	        
	        write.println("function main = |args| {");
	        getTestFunction(functions, write);
	        write.println("}");
	        write.close();
	        
	     
		} catch (IOException e) {
			System.out.println("[error] " + goloFile + " does not exist or could not be opened.");
		}
		return testFunctions;
		
	}

	private void getTestFunction(Set<GoloFunction> functions, PrintWriter write) {
		for(GoloFunction f : functions){
			if(f.hasDecorators()){
				List<org.eclipse.golo.compiler.ir.Decorator> dec = f.getDecorators();
				for(org.eclipse.golo.compiler.ir.Decorator d : dec)
					if(d.getExpressionStatement().toString().equals("Ref{name=Test}"))
						write.println("	" + f.getName() + "()");
			}
		}
	}
}

