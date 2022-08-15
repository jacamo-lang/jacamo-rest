// CArtAgO artifact code for project python_jacamo

package example;

import cartago.*;

public class Counter extends Artifact {
    void init(int initialValue) {
        defineObsProperty("count", initialValue);
    }

    @OPERATION
    void inc() {
        ObsProperty prop = getObsProperty("count");
        prop.updateValue(prop.intValue()+1);
        signal("tick");
    }

    /** the following operations are defined to allow the API REST to change observable properties */

    @OPERATION public void doDefineObsProperty(String obName, Object arg) {
        defineObsProperty(obName, arg);
    }

    @OPERATION public void doUpdateObsProperty(String obName, Object arg) {
        ObsProperty op = getObsProperty(obName);
        if (op == null) {
            defineObsProperty(obName, arg);
        } else {
            op.updateValues(arg);
        }
    }

}
