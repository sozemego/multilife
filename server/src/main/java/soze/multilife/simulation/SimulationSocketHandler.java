package soze.multilife.simulation;

import org.webbitserver.BaseWebSocketHandler;

/**
 * Created by KJurek on 21.02.2017.
 */
public class SimulationSocketHandler extends BaseWebSocketHandler {

    private final Simulation simulation;

    public SimulationSocketHandler(Simulation simulation) {
        this.simulation = simulation;
    }

}
