package jacamo.rest.util;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

public class DummyArt extends Artifact {
    public void init() {
    }

    @OPERATION public void doDefineObsProperty(String obName, Object arg) {
        defineObsProperty(obName, arg);
        //System.out.println("** new ob "+obName+"("+arg+")");
    }
    
    @OPERATION public void doUpdateObsProperty(String obName, Object arg) {
        ObsProperty op = getObsProperty(obName);
        if (op == null) {
            defineObsProperty(obName, arg);
        } else {
            op.updateValues(arg);
        }
        //System.out.println("** update ob "+obName+"("+arg+")");
    }

    @OPERATION public void doSignal(String signal, Object arg) {
        if (arg == null || arg.toString().equals("null")) {
            signal(signal);
            //System.out.println("** signal "+signal);
        } else {
            signal(signal, arg);
            //System.out.println("** signal "+signal+"("+arg+")");
        }
    }
}

