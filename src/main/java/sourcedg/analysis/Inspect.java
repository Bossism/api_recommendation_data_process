package sourcedg.analysis;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.io.File;
import java.io.FileNotFoundException;

public class Inspect {

    public static void main(String[] args) throws FileNotFoundException {
        // Parse the code you want to inspect:
        CompilationUnit cu = JavaParser.parse(new File("data/TestMain.java"));
        // Now comes the inspection code:
        System.out.println(cu);

        cu.accept(new ModifierVisitor<Void>() {
            /**
             * For every if-statement, see if it has a comparison using "!=".
             * Change it to "==" and switch the "then" and "else" statements around.
             */
            @Override
            public Visitable visit(SwitchStmt n, Void arg) {
                NodeList<SwitchEntryStmt> entries = n.getEntries();
                for (SwitchEntryStmt entryStmt : entries) {
                    System.out.println(entryStmt);
                }
                return super.visit(n, arg);
            }
        }, null);

    }

}
