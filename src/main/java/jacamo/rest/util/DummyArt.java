package jacamo.rest.util;

import cartago.Artifact;
import cartago.OPERATION;

public class DummyArt extends Artifact {
    public void init() {
    }

    @OPERATION public void doDefineObsProperty(String obName, Object args) {
        defineObsProperty(obName, args);
        //System.out.println("** new ob "+obName+"("+args+")");
    }
    
    @OPERATION public void doUpdateObsProperty(String obName, Object args) {
        getObsProperty(obName).updateValues(args);
        //System.out.println("** update ob "+obName+"("+args+")");
    }

    @OPERATION public void doSignal(String signal, Object args) {
        if (args == null || args.toString().equals("null")) {
            signal(signal);
            System.out.println("** signal "+signal);
        } else {
            signal(signal, args);
            System.out.println("** signal "+signal+"("+args+")");
        }
    }
}

